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
package eu.baltrad.beast.pgfwk;

import java.io.File;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.adaptor.xmlrpc.XmlRpcAdaptor;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.message.mo.BltCommandMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;

/**
 * Runs some basic integration tests towards the xmlrpc server
 * to verify that messages are passed on correctly and also that
 * the result is propagated to the callback.
 * @author Anders Henja
 */
public class BaltradXmlRpcServerITest extends TestCase {
  private BaltradXmlRpcServer classUnderTest = null;
  private Integer serverPort=55555;
  
  /**
   * Extracts only the class name (no package included).
   * @param clz the class
   * @return the name
   */
  @SuppressWarnings({"rawtypes" })
  private static String getClassName(Class clz) {
    String nm = clz.getName();
    int li = nm.lastIndexOf(".");
    if (li > 0) {
      nm = nm.substring(li+1);
    }
    return nm;
  }
  
  public void setUp() throws Exception {
    String cln = getClassName(this.getClass());
    String cname = cln + "-context.xml";
    File f = new File(this.getClass().getResource(cname).getFile());
    String[] args = new String[]{
        "--port="+serverPort,
        "--context=file:"+f.getAbsolutePath()
    };
    classUnderTest = BaltradXmlRpcServer.createServerFromArguments(args);
    classUnderTest.start();
  }
  
  public void tearDown() throws Exception {
    classUnderTest.shutdown();
    classUnderTest = null;
  }
  
  public void testSendAlert() throws Exception {
    ApplicationContext context = classUnderTest.getContext();
    XmlRpcAdaptor adaptor = (XmlRpcAdaptor)context.getBean("xmlrpcadaptor");
    TestRpcCallback cb = (TestRpcCallback)context.getBean("xmlrpccallback");
    
    BltAlertMessage msg = new BltAlertMessage();
    msg.setCode("E1212");
    msg.setMessage("this message");
    
    cb.reset();
    adaptor.handle(msg);
    IBltMessage result = cb.waitForResponse(5000);
    assertSame(msg, result);
    assertEquals(0, cb.getResult());
  }
  
  public void testSendCommand() throws Exception {
    ApplicationContext context = classUnderTest.getContext();
    XmlRpcAdaptor adaptor = (XmlRpcAdaptor)context.getBean("xmlrpcadaptor");
    TestRpcCallback cb = (TestRpcCallback)context.getBean("xmlrpccallback");

    BltCommandMessage msg = new BltCommandMessage();
    msg.setCommand("ls -la");

    cb.reset();
    adaptor.handle(msg);
    IBltMessage result = cb.waitForResponse(5000);
    assertSame(msg, result);
    Object[] resp = (Object[])cb.getResult();
    assertEquals(0, resp[0]);
    assertFalse(resp[1].equals(""));
    assertTrue(resp[2].equals(""));
  }

  public void testSendCommand_withstderr() throws Exception {
    ApplicationContext context = classUnderTest.getContext();
    XmlRpcAdaptor adaptor = (XmlRpcAdaptor)context.getBean("xmlrpcadaptor");
    TestRpcCallback cb = (TestRpcCallback)context.getBean("xmlrpccallback");

    BltCommandMessage msg = new BltCommandMessage();
    msg.setCommand("ls -la 1>&2");
    cb.reset();
    
    // Execute
    adaptor.handle(msg);
    IBltMessage result = cb.waitForResponse(5000);
    
    // Verify
    assertSame(msg, result);
    Object[] resp = (Object[])cb.getResult();
    assertEquals(2, resp[0]);
    assertTrue(resp[1].equals(""));
    assertFalse(resp[2].equals(""));
  }
  
  public void testSendGenerate() throws Exception {
    ApplicationContext context = classUnderTest.getContext();
    XmlRpcAdaptor adaptor = (XmlRpcAdaptor)context.getBean("xmlrpcadaptor");
    TestRpcCallback cb = (TestRpcCallback)context.getBean("xmlrpccallback");
    
    BltGenerateMessage msg = new BltGenerateMessage();
    msg.setAlgorithm("a.TestAlgorithm");
    msg.setFiles(new String[]{"file:/x/y/z.h5", "file:/somewhere/x.h5"});
    msg.setArguments(new String[]{"a", "b"});

    cb.reset();
    
    // execute
    adaptor.handle(msg);
    
    // verify
    IBltMessage result = cb.waitForResponse(5000);
    assertSame(msg, result);
    Object resp = (Object)cb.getResult();
    assertEquals(0, resp);
    TestGeneratorPlugin plug = (TestGeneratorPlugin)context.getBean("a.TestAlgorithm");
    assertEquals("a.TestAlgorithm", plug.getAlgorithm());
    String[] files = plug.getFiles();
    assertEquals(2, files.length);
    assertEquals("file:/x/y/z.h5", files[0]);
    assertEquals("file:/somewhere/x.h5", files[1]);
    Object[] args = plug.getArgs();
    assertEquals(2, args.length);
    assertEquals("a", args[0]);
    assertEquals("b", args[1]);
  }

  public void XtestSendGenerate_withThrowable() throws Exception {
    ApplicationContext context = classUnderTest.getContext();
    XmlRpcAdaptor adaptor = (XmlRpcAdaptor)context.getBean("xmlrpcadaptor");
    TestRpcCallback cb = (TestRpcCallback)context.getBean("xmlrpccallback");
    
    BltGenerateMessage msg = new BltGenerateMessage();
    msg.setAlgorithm("a.FailedTestAlgorithm");
    msg.setFiles(new String[]{"file:/x/y/z.h5", "file:/somewhere/x.h5"});
    msg.setArguments(new String[]{"a", "b"});

    cb.reset();
    
    // execute
    adaptor.handle(msg);
    
    // verify
    IBltMessage result = cb.waitForResponse(5000);
    assertSame(msg, result);
    Object resp = (Object)cb.getResult();
    assertEquals(-1, resp);
  }

  public void XtestSendGenerate_noSuchPlugin() throws Exception {
    ApplicationContext context = classUnderTest.getContext();
    XmlRpcAdaptor adaptor = (XmlRpcAdaptor)context.getBean("xmlrpcadaptor");
    TestRpcCallback cb = (TestRpcCallback)context.getBean("xmlrpccallback");
    
    BltGenerateMessage msg = new BltGenerateMessage();
    msg.setAlgorithm("a.NonExistingAlgorithm");
    msg.setFiles(new String[]{"file:/x/y/z.h5", "file:/somewhere/x.h5"});
    msg.setArguments(new String[]{"a", "b"});

    cb.reset();
    
    // execute
    adaptor.handle(msg);
    
    // verify
    IBltMessage result = cb.waitForResponse(5000);
    assertSame(msg, result);
    assertNotNull(cb.getThrowable());
  }
}
