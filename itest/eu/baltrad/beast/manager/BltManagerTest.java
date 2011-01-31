/* --------------------------------------------------------------------
Copyright (C) 2009-2010 Swedish Meteorological and Hydrological Institute, SMHI,

This file is part of the Beast library.

Beast library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Beast library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with the Beast library library.  If not, see <http://www.gnu.org/licenses/>.
------------------------------------------------------------------------*/
package eu.baltrad.beast.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.adaptor.IAdaptorCallback;
import eu.baltrad.beast.adaptor.IBltAdaptorManager;
import eu.baltrad.beast.adaptor.xmlrpc.XmlRpcAdaptor;
import eu.baltrad.beast.adaptor.xmlrpc.XmlRpcAdaptorConfiguration;
import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.pgfwk.BaltradXmlRpcServer;
import eu.baltrad.beast.router.IRouterManager;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.groovy.GroovyRule;
import eu.baltrad.beast.rules.groovy.GroovyRuleManager;

/**
 * More of a overall system view test, performs all steps to verify
 * that the use-cases work properly. I.e. registering of adaptors,
 * adding of new router rules, message routing, etc etc.
 * 
 * @author Anders Henja
 */
public class BltManagerTest extends TestCase {
  /**
   * The class under test
   */
  private BltMessageManager classUnderTest = null;
  
  /**
   * The xmlrpc server 
   */
  private BaltradXmlRpcServer server = null;
  
  /**
   * The timing generator plugin
   */
  private TimingGeneratorPlugin generator = null;
  
  /**
   * Helper for working with db during the tests
   */
  private BeastDBTestHelper helper = null;
  
  private GroovyRuleManager groovymgr = null;
  
  /**
   * Extracts only the class name (no package included).
   * @param clz the class
   * @return the name
   */
  @SuppressWarnings({"unchecked" })
  private static String getClassName(Class clz) {
    String nm = clz.getName();
    int li = nm.lastIndexOf(".");
    if (li > 0) {
      nm = nm.substring(li+1);
    }
    return nm;
  }
  
  public void setUp() throws Exception {
    ApplicationContext dbcontext = BeastDBTestHelper.loadDbContext(this);
    helper = (BeastDBTestHelper)dbcontext.getBean("helper");
    helper.tearDown(); // remove everything from db before we start the system
    
    String cln = getClassName(this.getClass());
    String cname = cln + "-context.xml";
    File f = new File(this.getClass().getResource(cname).getFile());
    String[] args = new String[]{
        "--port=55555",
        "--context=file:"+f.getAbsolutePath()};

    server = BaltradXmlRpcServer.createServerFromArguments(args);
    server.start();
    try {
      Thread.sleep(1000);
    } catch (Throwable t) {
      // pass
    }
    classUnderTest = (BltMessageManager)server.getContext().getBean("manager");
    generator = (TimingGeneratorPlugin)server.getContext().getBean("a.TimingGenerator");
    generator.reset();
    groovymgr = (GroovyRuleManager)server.getContext().getBean("groovymgr");
  }

  public void tearDown() throws Exception {
    server.shutdown();
    server = null;
    classUnderTest = null;
    generator = null;
    generator= null;
    groovymgr = null;
  }
  
  private static class TestCallback implements IAdaptorCallback {
    volatile int nrerror = 0;
    volatile int nrsuccess = 0;
    volatile int nrtimeout = 0;
    
    public synchronized void error(IBltMessage message, Throwable t) {
      System.out.println("Callback: ERROR");
      nrerror++;
      notifyAll();
    }
    public synchronized void success(IBltMessage message, Object result) {
      //System.out.println("Callback: SUCCESS");
      nrsuccess++;
      notifyAll();
    }
    public synchronized void timeout(IBltMessage message) {
      //System.out.println("Callback: TIMEOUT");
      nrtimeout++;
      notifyAll();
    }
    public int waitForEntries(int nrentries, long timeout) {
      long currtime = System.currentTimeMillis();
      long endtime = currtime + timeout;
      int n = nrerror + nrsuccess + nrtimeout;
      while (currtime < endtime && nrentries != n) {
        try {
          wait (endtime - currtime);
        } catch (Throwable t) {
          
        }
        n = nrerror + nrsuccess + nrtimeout;
        currtime = System.currentTimeMillis();
      }
      return n;
    }
  }
  
  public void XtestManage() throws Exception {
    IBltAdaptorManager adaptorManager =
      (IBltAdaptorManager)server.getContext().getBean("adaptormanager");
    IRouterManager routerManager = (IRouterManager)server.getContext().getBean("router");

    // Create adaptor configuration
    XmlRpcAdaptorConfiguration config = 
      (XmlRpcAdaptorConfiguration)adaptorManager.createConfiguration(XmlRpcAdaptorConfiguration.TYPE, "PGFWK");
    config.setTimeout(5000);
    config.setURL("http://localhost:55555/xmlrpc");
    
    // Register the adaptor in the database
    XmlRpcAdaptor adaptor = (XmlRpcAdaptor)adaptorManager.register(config);
    TestCallback cb = new TestCallback();
    adaptor.setCallback(cb);

    // Create and register the routes
    RouteDefinition fwddef = getForwardDefinition("PGFWK");
    RouteDefinition alertdef = getAlertDefinition("PGFWK");
    routerManager.storeDefinition(fwddef);
    routerManager.storeDefinition(alertdef);
    
    // And now send away like you never have before.
    for (int i = 0; i < 50; i++) {
      BltGenerateMessage gmsg = new BltGenerateMessage();
      gmsg.setAlgorithm("a.TimingGenerator");
      gmsg.setArguments(new String[]{""+System.currentTimeMillis()});
      classUnderTest.manage(gmsg);
      
      BltAlertMessage amsg = new BltAlertMessage();
      amsg.setCode("E0001");
      amsg.setMessage(""+System.currentTimeMillis());
      classUnderTest.manage(amsg);     
    }
    
    long nrtimes = generator.waitForResponse(100, 30000);
    int nrresponses = cb.waitForEntries(10000, 5000);
    assertEquals(100, nrtimes);
    assertEquals(100, nrresponses);
    System.out.println("Avg time: " + (generator.getTotalTime()/nrtimes));
  }
  
  protected RouteDefinition getForwardDefinition(String recipient) {
    RouteDefinition def = new RouteDefinition();
    List<String> recipients = new ArrayList<String>();
    recipients.add(recipient);
    def.setName("bltmgrtestfwdrule");
    def.setAuthor("anders");
    def.setDescription("test");
    def.setRecipients(recipients);
    def.setRule(getForwardingRule());
    return def;
  }
  
  protected GroovyRule getForwardingRule() {
    StringBuffer buf = new StringBuffer();
    buf.append("import eu.baltrad.beast.rules.IScriptableRule;\n");
    buf.append("import eu.baltrad.beast.message.IBltMessage;\n");
    buf.append("import eu.baltrad.beast.message.mo.BltGenerateMessage;\n");
    buf.append("public class ForwardingRule implements IScriptableRule {\n");
    buf.append("  public IBltMessage handle(IBltMessage message) {\n");
    buf.append("    if (message.getClass() == BltGenerateMessage.class) {\n");
    buf.append("      return message;\n");
    buf.append("    }\n");
    buf.append("  }\n");
    buf.append("}\n");
    GroovyRule rule = groovymgr.createRule();
    rule.setScript(buf.toString());
    return rule;
  }

  protected RouteDefinition getAlertDefinition(String recipient) {
    RouteDefinition def = new RouteDefinition();
    List<String> recipients = new ArrayList<String>();
    recipients.add(recipient);
    def.setName("bltmgrtestalertrule");
    def.setAuthor("anders");
    def.setDescription("test");
    def.setRecipients(recipients);
    def.setRule(getAlertToGenerateRule());
    return def;
  }
  
  protected GroovyRule getAlertToGenerateRule() {
    StringBuffer buf = new StringBuffer();
    buf.append("import eu.baltrad.beast.rules.IScriptableRule;\n");
    buf.append("import eu.baltrad.beast.message.IBltMessage;\n");
    buf.append("import eu.baltrad.beast.message.mo.BltGenerateMessage;\n");
    buf.append("import eu.baltrad.beast.message.mo.BltAlertMessage;\n");
    buf.append("public class ForwardingRule implements IScriptableRule {\n");
    buf.append("  public IBltMessage handle(IBltMessage message) {\n");
    buf.append("    BltGenerateMessage result = null;\n");
    buf.append("    if (message.getClass() == BltAlertMessage.class) {\n");
    buf.append("      String xtime = ((BltAlertMessage)message).getMessage();\n");
    buf.append("      result = new BltGenerateMessage();\n");
    buf.append("      result.setAlgorithm(\"a.TimingGenerator\");\n");
    buf.append("      result.setArguments([xtime] as String[]);\n");
    buf.append("    }\n");
    buf.append("    return result;\n");
    buf.append("  }\n");
    buf.append("}\n");
    GroovyRule rule = groovymgr.createRule();
    rule.setScript(buf.toString());
    return rule;
  }
  
  public void testIt() {
    
  }
}
