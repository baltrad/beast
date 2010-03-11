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
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import eu.baltrad.beast.adaptor.IBltAdaptorManager;
import eu.baltrad.beast.adaptor.xmlrpc.XmlRpcAdaptorConfiguration;
import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.pgfwk.BaltradXmlRpcServer;
import eu.baltrad.beast.router.IRouterManager;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleFactory;

/**
 * More of a overall system view test, performs all steps to verify
 * that the use-cases work properly. I.e. registering of adaptors,
 * adding of new router rules, message routing, etc etc..
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
   * The rule factory
   */
  private IRuleFactory factory = null;
  
  /**
   * Extracts only the class name (no package included).
   * @param clz the class
   * @return the name
   */
  @SuppressWarnings("unchecked")
  private static String getClassName(Class clz) {
    String nm = clz.getName();
    int li = nm.lastIndexOf(".");
    if (li > 0) {
      nm = nm.substring(li+1);
    }
    return nm;
  }
  
  protected void initializeDatabase() throws Exception {
    String cln = getClassName(this.getClass());
    String cname = cln + "-dbcontext.xml";
    File f = new File(this.getClass().getResource(cname).getFile());
    
    ApplicationContext dbcontext = new ClassPathXmlApplicationContext("file:"+f.getAbsolutePath());
    SimpleJdbcTemplate template = (SimpleJdbcTemplate)dbcontext.getBean("jdbcTemplate");
    template.update("delete from router_dest");
    template.update("delete from adaptors_xmlrpc");
    template.update("delete from adaptors");
    template.update("delete from router_rules");
  }
  
  public void setUp() throws Exception {
    initializeDatabase();
    String cln = getClassName(this.getClass());
    String cname = cln + "-context.xml";
    File f = new File(this.getClass().getResource(cname).getFile());
    ApplicationContext context = new FileSystemXmlApplicationContext("file:"+f.getAbsolutePath());
    server = (BaltradXmlRpcServer)context.getBean("rpcserver");
    server.start();
    classUnderTest = (BltMessageManager)server.getContext().getBean("manager");
    factory = (IRuleFactory)server.getContext().getBean("rulefactory");
    generator = (TimingGeneratorPlugin)server.getContext().getBean("a.TimingGenerator");
  }
  
  public void tearDown() throws Exception {
    server.shutdown();
    server = null;
    classUnderTest = null;
    generator = null;
    factory = null;
  }
  
  public void testManage_forwardGenerateRule() throws Exception {
    IBltAdaptorManager adaptorManager =
      (IBltAdaptorManager)server.getContext().getBean("adaptormanager");
    IRouterManager routerManager = (IRouterManager)server.getContext().getBean("router");

    // Create adaptor configuration
    XmlRpcAdaptorConfiguration config = 
      (XmlRpcAdaptorConfiguration)adaptorManager.createConfiguration(XmlRpcAdaptorConfiguration.TYPE, "PGFWK");
    config.setTimeout(5000);
    config.setURL("http://localhost:55555/xmlrpc");
    
    // Register the adaptor in the database
    adaptorManager.register(config);
    
    // Create routing rule
    RouteDefinition def = new RouteDefinition();
    List<String> recipients = new ArrayList<String>();
    recipients.add("PGFWK");
    def.setName("bltmanagertestroute");
    def.setAuthor("anders");
    def.setDescription("test");
    def.setRecipients(recipients);
    // Scripted groovy rule
    IRule rule = factory.create("groovy", getForwardingRule());
    def.setRule(rule);
    
    // Register the routing rule in the database
    routerManager.storeDefinition(def);
    
    generator.reset();
    
    for (int i = 0; i < 20; i++) {
      BltGenerateMessage message = new BltGenerateMessage();
      message.setAlgorithm("a.TimingGenerator");
      message.setArguments(new String[]{""+System.currentTimeMillis()});
      classUnderTest.manage(message);
    }
    
    long nrtimes = generator.waitForResponse(20, 5000);
    assertEquals(20, nrtimes);
    System.out.println("Avg time: " + (generator.getTotaltime()/nrtimes));
    assertTrue(20 > (generator.getTotaltime()/nrtimes));
  }

  public void testManage_alertToGenerateRule() throws Exception {
    IBltAdaptorManager adaptorManager =
      (IBltAdaptorManager)server.getContext().getBean("adaptormanager");
    IRouterManager routerManager = (IRouterManager)server.getContext().getBean("router");

    // Create adaptor configuration
    XmlRpcAdaptorConfiguration config = 
      (XmlRpcAdaptorConfiguration)adaptorManager.createConfiguration(XmlRpcAdaptorConfiguration.TYPE, "PGFWK");
    config.setTimeout(5000);
    config.setURL("http://localhost:55555/xmlrpc");
    
    // Register the adaptor in the database
    adaptorManager.register(config);
    
    // Create routing rule
    RouteDefinition def = new RouteDefinition();
    List<String> recipients = new ArrayList<String>();
    recipients.add("PGFWK");
    def.setName("bltmanagertestroute");
    def.setAuthor("anders");
    def.setDescription("test");
    def.setRecipients(recipients);
    // Scripted groovy rule
    IRule rule = factory.create("groovy", getAlertToGenerateRule());
    def.setRule(rule);
    
    // Register the routing rule in the database
    routerManager.storeDefinition(def);
    
    generator.reset();
    
    for (int i = 0; i < 20; i++) {
      BltAlertMessage message = new BltAlertMessage();
      message.setCode("E0001");
      message.setMessage(""+System.currentTimeMillis());
      classUnderTest.manage(message);
    }
    
    long nrtimes = generator.waitForResponse(20, 5000);
    assertEquals(20, nrtimes);
    System.out.println("Avg time: " + (generator.getTotaltime()/nrtimes));
    assertTrue(25 > (generator.getTotaltime()/nrtimes));
  }
  
  
  protected String getForwardingRule() {
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
    return buf.toString();
  }

  protected String getAlertToGenerateRule() {
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
    return buf.toString();
  }
  
}
