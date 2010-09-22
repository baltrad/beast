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
package eu.baltrad.beast.adaptor.xmlrpc;

import junit.framework.TestCase;
import eu.baltrad.beast.adaptor.IAdaptorCallback;
import eu.baltrad.beast.itest.XmlRpcTestServer;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.message.mo.BltCommandMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;

/**
 * Attempts to test that the xml-rpc client server communication works.
 * Might be some threading issues in the code below but let's hope not.
 * @author Anders Henja
 */
public class XmlRpcAdaptorITest extends TestCase {
  
  private XmlRpcTestServer server = null;
  
  private class TestCallback implements IAdaptorCallback {
    private IBltMessage msg = null;
    boolean wasError = false;
    boolean wasTimeout = false;
    boolean wasSuccess = false;
    
    public void error(IBltMessage message, Throwable t) {
      wasError = true;
      msg = message;
    }
    
    public void success(IBltMessage message, Object result) {
      wasSuccess = true;
      msg = message;
    }
    
    public void timeout(IBltMessage message) {
      wasTimeout = true;
      msg = message;
    }
    
    public boolean isError() {
      return wasError;
    }
    public boolean isTimeout() {
      return wasTimeout;
    }
    public boolean isSuccess() {
      return wasSuccess;
    }
    public IBltMessage getMessage() {
      return msg;
    }
  };
  
  public void setUp() throws Exception {
    server = new XmlRpcTestServer(55555, new Integer(0));
    server.start();
  }
  
  public void tearDown() throws Exception {
    server.shutdown();
  }
  
  public void testHandleBltCommandMessage() throws Exception {
    TestCallback cb = new TestCallback();
    XmlRpcAdaptor adaptor = new XmlRpcAdaptor();
    XmlRpcCommandGenerator generator = new XmlRpcCommandGenerator();
    adaptor.setUrl("http://localhost:55555/xmlrpc");
    adaptor.setGenerator(generator);
    adaptor.setCallback(cb);
    
    BltCommandMessage msg = new BltCommandMessage();
    msg.setCommand("ls -l");

    adaptor.handle(msg);

    String method = server.waitForRequest(2000);
    assertEquals("execute", method);
    assertEquals("ls -l", server.getRequestParameters()[0]);
    assertEquals(true, cb.isSuccess());
    assertSame(msg, cb.getMessage());
  }
  
  public void testHandleBltAlertMessage() throws Exception {
    TestCallback cb = new TestCallback();
    XmlRpcAdaptor adaptor = new XmlRpcAdaptor();
    XmlRpcCommandGenerator generator = new XmlRpcCommandGenerator();
    adaptor.setUrl("http://localhost:55555/xmlrpc");
    adaptor.setGenerator(generator);
    adaptor.setCallback(cb);
    
    BltAlertMessage msg = new BltAlertMessage();
    msg.setCode("E0001");
    msg.setMessage("Alert message");

    adaptor.handle(msg);
    
    String method = server.waitForRequest(2000);
    assertEquals("alert", method);
    assertEquals("E0001", server.getRequestParameters()[0]);
    assertEquals("Alert message", server.getRequestParameters()[1]);
    assertEquals(true, cb.isSuccess());
    assertSame(msg, cb.getMessage());
  }

  public void testHandleBltGenerateMessage() throws Exception {
    TestCallback cb = new TestCallback();
    XmlRpcAdaptor adaptor = new XmlRpcAdaptor();
    XmlRpcCommandGenerator generator = new XmlRpcCommandGenerator();
    adaptor.setUrl("http://localhost:55555/xmlrpc");
    adaptor.setGenerator(generator);
    adaptor.setCallback(cb);
    
    BltGenerateMessage msg = new BltGenerateMessage();
    msg.setAlgorithm("com.test.something.Algorithm");
    msg.setFiles(new String[]{"/file/1.h5", "/file/2.h5"});
    msg.setArguments(new String[]{"-k", "10"});

    adaptor.handle(msg);
    
    String method = server.waitForRequest(2000);
    assertEquals("generate", method);
    String algorithm = (String)server.getRequestParameters()[0];
    
    Object[] files = (Object[])server.getRequestParameters()[1];
    Object[] args = (Object[])server.getRequestParameters()[2];
    
    assertEquals("com.test.something.Algorithm", algorithm);
    assertEquals(2, files.length);
    assertEquals("/file/1.h5", files[0]);
    assertEquals("/file/2.h5", files[1]);
    assertEquals(2, args.length);
    assertEquals("-k", args[0]);
    assertEquals("10", args[1]);
    assertEquals(true, cb.isSuccess());
    assertSame(msg, cb.getMessage());
  }
  
  public void testHandle_timeout() throws Exception {
    server.setResponseTimeout(2000);
    
    TestCallback cb = new TestCallback();
    XmlRpcAdaptor adaptor = new XmlRpcAdaptor();
    XmlRpcCommandGenerator generator = new XmlRpcCommandGenerator();
    adaptor.setUrl("http://localhost:55555/xmlrpc");
    adaptor.setGenerator(generator);
    adaptor.setTimeout(1000);
    adaptor.setCallback(cb);
    
    BltAlertMessage msg = new BltAlertMessage();
    msg.setCode("E0001");
    msg.setMessage("Alert message");

    adaptor.handle(msg);
    
    String method = server.waitForRequest(3000);
    assertEquals("alert", method);
    assertEquals(true, cb.isTimeout());
    assertSame(msg, cb.getMessage());
  }

  public void testHandle_error() throws Exception {
    TestCallback cb = new TestCallback();
    XmlRpcAdaptor adaptor = new XmlRpcAdaptor();
    XmlRpcCommandGenerator generator = new XmlRpcCommandGenerator();
    adaptor.setUrl("http://localhost:55556/xmlrpc");
    adaptor.setGenerator(generator);
    adaptor.setTimeout(1000);
    adaptor.setCallback(cb);
    
    BltAlertMessage msg = new BltAlertMessage();
    msg.setCode("E0001");
    msg.setMessage("Alert message");

    adaptor.handle(msg);
    
    assertEquals(true, cb.isError());
    assertSame(msg, cb.getMessage());
  }  
}
