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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.TimingOutCallback;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.adaptor.AdaptorAddressException;
import eu.baltrad.beast.adaptor.AdaptorException;
import eu.baltrad.beast.adaptor.IAdaptorCallback;
import eu.baltrad.beast.message.IBltMessage;

/**
 * @author Anders Henja
 */
public class XmlRpcAdaptorTest extends EasyMockSupport {
  private static interface HandleMethodMock {
    public void handle(IBltMessage message, IAdaptorCallback callback);
  };
  
  private IXmlRpcCommandGenerator generator = null;
  private XmlRpcClient rpcClient = null;
  private TimingOutCallback timeoutCB = null;
  private XmlRpcAdaptor classUnderTest = null;
  
  
  @Before
  public void setUp() throws Exception {
    generator = createMock(IXmlRpcCommandGenerator.class);
    rpcClient = createMock(XmlRpcClient.class);
    timeoutCB = createMock(TimingOutCallback.class);
    
    classUnderTest = new XmlRpcAdaptor() {
      protected TimingOutCallback createTimeout(long timeout) {
        return timeoutCB;
      }      
    };
    classUnderTest.setGenerator(generator);
    classUnderTest.setRpcClient(rpcClient);
  }

  @After
  public void tearDown() throws Exception {
    generator = null;
    rpcClient = null;
    timeoutCB = null;
    classUnderTest = null;
  }
  
  @Test
  public void testGetType() throws Throwable {
    replayAll();
    assertEquals(XmlRpcAdaptorConfiguration.TYPE, classUnderTest.getType());
    verifyAll();
  }
  
  @Test
  public void testHandle() throws Throwable {
    final HandleMethodMock handleMock = createMock(HandleMethodMock.class);
    IAdaptorCallback adaptorCb = createMock(IAdaptorCallback.class);

    IBltMessage message = new IBltMessage(){};
    handleMock.handle(message, adaptorCb);

    XmlRpcAdaptor classUnderTest = new XmlRpcAdaptor() {
      public void handle(IBltMessage message, IAdaptorCallback callback) {
        handleMock.handle(message, callback);
      }
    };
    classUnderTest.setCallback(adaptorCb);
    
    replayAll();
    
    // Execute test
    classUnderTest.handle(message);

    // Verify
    verifyAll();
  }

  @Test
  public void testHandle_withCb() throws Throwable {
    final HandleMethodMock handleMock = createMock(HandleMethodMock.class);
    IAdaptorCallback adaptorCb = createMock(IAdaptorCallback.class);

    IBltMessage message = new IBltMessage(){};
    
    handleMock.handle(message, adaptorCb);

    XmlRpcAdaptor classUnderTest = new XmlRpcAdaptor() {
      public void handle(IBltMessage message, IAdaptorCallback callback) {
        handleMock.handle(message, callback);
      }
    };
    
    replayAll();
    
    // Execute test
    classUnderTest.handle(message, adaptorCb);

    // Verify
    verifyAll();
  }
  
  @Test
  public void testHandleMessage_withCb_success() throws Throwable {
    IBltMessage message = new IBltMessage(){};
    XmlRpcCommand cmd = new XmlRpcCommand();
    IAdaptorCallback callback = createMock(IAdaptorCallback.class);
    Object[] rpcArgs = new Object[]{};
    cmd.setMethod("command");
    cmd.setObjects(rpcArgs);
    
    Integer cbReturnCode = new Integer(0);
    expect(generator.generate(message)).andReturn(cmd);
    rpcClient.executeAsync("command", rpcArgs, timeoutCB);
    expect(timeoutCB.waitForResponse()).andReturn(cbReturnCode);
    callback.success(message, cbReturnCode);
    
    replayAll();
    
    // Execute test
    classUnderTest.handle(message,callback);

    // verify
    verifyAll();
  }

  @Test
  public void testHandleMessage_withCb_success_nullCb() throws Throwable {
    IBltMessage message = new IBltMessage(){};
    XmlRpcCommand cmd = new XmlRpcCommand();
    Object[] rpcArgs = new Object[]{};
    cmd.setMethod("command");
    cmd.setObjects(rpcArgs);
    
    Integer cbReturnCode = new Integer(0);
    expect(generator.generate(message)).andReturn(cmd);
    rpcClient.executeAsync("command", rpcArgs, timeoutCB);
    expect(timeoutCB.waitForResponse()).andReturn(cbReturnCode);
    
    replayAll();
    
    // Execute test
    classUnderTest.handle(message, null);

    // verify
    verifyAll();
  }
  
  @Test
  public void testHandleMessage_withCb_timeout() throws Throwable {
    IBltMessage message = new IBltMessage(){};
    XmlRpcCommand cmd = new XmlRpcCommand();
    IAdaptorCallback callback = createMock(IAdaptorCallback.class);
    Object[] rpcArgs = new Object[]{};
    cmd.setMethod("command");
    cmd.setObjects(rpcArgs);

    expect(generator.generate(message)).andReturn(cmd);
    rpcClient.executeAsync("command", rpcArgs, timeoutCB);
    expect(timeoutCB.waitForResponse()).andThrow(new TimingOutCallback.TimeoutException(0, "x"));
    callback.timeout(message);
    replayAll();
    
    // Execute test
    classUnderTest.handle(message,callback);

    // verify
    verifyAll();
  }

  @Test
  public void testHandleMessage_withCb_timeout_nullCb() throws Throwable {
    IBltMessage message = new IBltMessage(){};
    XmlRpcCommand cmd = new XmlRpcCommand();
    Object[] rpcArgs = new Object[]{};
    cmd.setMethod("command");
    cmd.setObjects(rpcArgs);

    expect(generator.generate(message)).andReturn(cmd);
    rpcClient.executeAsync("command", rpcArgs, timeoutCB);
    expect(timeoutCB.waitForResponse()).andThrow(new TimingOutCallback.TimeoutException(0, "x"));
    
    replayAll();
    
    // Execute test
    classUnderTest.handle(message, null);

    // verify
    verifyAll();
  }
  
  @Test
  public void testHandleMessage_withCb_error() throws Throwable {
    IBltMessage message = new IBltMessage(){};
    XmlRpcCommand cmd = new XmlRpcCommand();
    IAdaptorCallback callback = createMock(IAdaptorCallback.class);
    Object[] rpcArgs = new Object[]{};
    cmd.setMethod("command");
    cmd.setObjects(rpcArgs);

    expect(generator.generate(message)).andReturn(cmd);
    rpcClient.executeAsync("command", rpcArgs, timeoutCB);
    Throwable t = new NullPointerException();
    expect(timeoutCB.waitForResponse()).andThrow(t);
    callback.error(message, t);
    
    replayAll();
    
    // Execute test
    classUnderTest.handle(message,callback);

    // verify
    verifyAll();
  }

  @Test
  public void testHandleMessage_withCb_error_nullCb() throws Throwable {
    IBltMessage message = new IBltMessage(){};
    XmlRpcCommand cmd = new XmlRpcCommand();
    Object[] rpcArgs = new Object[]{};
    cmd.setMethod("command");
    cmd.setObjects(rpcArgs);

    expect(generator.generate(message)).andReturn(cmd);
    rpcClient.executeAsync("command", rpcArgs, timeoutCB);
    Throwable t = new NullPointerException();
    expect(timeoutCB.waitForResponse()).andThrow(t);

    replayAll();
    
    // Execute test
    classUnderTest.handle(message, null);

    // verify
    verifyAll();
  }
  
  @Test
  public void testHandleMessage_withCb_executeThrowsXmlRpcException() throws Exception {
    IBltMessage message = new IBltMessage(){};
    XmlRpcCommand cmd = new XmlRpcCommand();
    IAdaptorCallback callback = createMock(IAdaptorCallback.class);
    Object[] args = new Object[]{};
    cmd.setMethod("command");
    cmd.setObjects(args);
    
    expect(generator.generate(message)).andReturn(cmd);
    rpcClient.executeAsync("command", args, timeoutCB);
    EasyMock.expectLastCall().andThrow(new XmlRpcException("xys"));

    replayAll();
    
    // Execute test
    try {
      classUnderTest.handle(message, callback);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }

    // verify
    verifyAll();
  }  
  
  @Test
  public void testSetUrl() throws Exception {
    final XmlRpcClientConfigImpl xmlRpcConfig = createMock(XmlRpcClientConfigImpl.class);
    final URL url = new URL("http://localhost");
    
    XmlRpcClient client = new XmlRpcClient();
    
    xmlRpcConfig.setServerURL(url);
    
    replayAll();
    
    classUnderTest = new XmlRpcAdaptor() {
      protected XmlRpcClientConfigImpl createConfig() {
        return xmlRpcConfig;
      }
      protected URL createUrl(String str) {
        return url;
      }      
    };
    classUnderTest.setRpcClient(client);
    
    // execute test
    classUnderTest.setUrl("http://localhost");
    XmlRpcClientConfigImpl result = (XmlRpcClientConfigImpl)client.getConfig();
    
    // verify
    verifyAll();
    assertSame(result, xmlRpcConfig);
    assertEquals("http://localhost", classUnderTest.getUrl());
  }
  
  @Test
  public void testSetUrl_malformed() throws Exception {
    final XmlRpcClientConfigImpl xmlRpcConfig = createMock(XmlRpcClientConfigImpl.class);
    
    XmlRpcClient client = new XmlRpcClient();
    
    replayAll();
    
    classUnderTest = new XmlRpcAdaptor() {
      protected XmlRpcClientConfigImpl createConfig() {
        return xmlRpcConfig;
      }
    };
    classUnderTest.setRpcClient(client);
    
    // execute test
    try {
      classUnderTest.setUrl("ht tp://localhost");
      fail("Expected AdaptorAddressException");
    } catch (AdaptorAddressException e) {
      // pass
    }
    
    // verify
    verifyAll();
  }
  
  @Test
  public void testCreateUrl() throws Exception {
    String[] LEGAL_URLS = {"http://localhost", "http://localhost:1010",
        "http://localhost/something", "http://localhost:1010/something"};
    for (String url : LEGAL_URLS) {
      URL result = classUnderTest.createUrl(url);
      assertNotNull(result);
    }
  }
  
  @Test
  public void testCreateUrl_illegal() throws Exception {
    String[] ILLEGAL_URLS = {"hXtp://localhost", "http",
        "localhost/something", "1234"};
    for (String url : ILLEGAL_URLS) {
      try {
        classUnderTest.createUrl(url);
        fail("Expected AdaptorAddressException");
      } catch (AdaptorAddressException e) {
        // pass
      }
    }
  }
}
