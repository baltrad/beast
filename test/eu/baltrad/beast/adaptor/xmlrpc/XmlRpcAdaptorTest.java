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

import java.net.URL;

import junit.framework.TestCase;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.TimingOutCallback;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import eu.baltrad.beast.adaptor.AdaptorAddressException;
import eu.baltrad.beast.adaptor.AdaptorException;
import eu.baltrad.beast.adaptor.IAdaptorCallback;
import eu.baltrad.beast.message.IBltMessage;

/**
 * @author Anders Henja
 */
public class XmlRpcAdaptorTest extends TestCase {
  private static interface HandleMethodMock {
    public void handle(IBltMessage message, IAdaptorCallback callback);
  };
  
  
  private MockControl generatorControl = null;
  private IXmlRpcCommandGenerator generator = null;
  private MockControl rpcClientControl = null;
  private XmlRpcClient rpcClient = null;
  private MockControl timeoutCBControl = null;
  private TimingOutCallback timeoutCB = null;
  private XmlRpcAdaptor classUnderTest = null;
  
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    generatorControl = MockControl.createControl(IXmlRpcCommandGenerator.class);
    generator = (IXmlRpcCommandGenerator)generatorControl.getMock();
    rpcClientControl = MockClassControl.createControl(XmlRpcClient.class);
    rpcClient = (XmlRpcClient)rpcClientControl.getMock();
    timeoutCBControl = MockClassControl.createControl(TimingOutCallback.class);
    timeoutCB = (TimingOutCallback)timeoutCBControl.getMock();
    
    classUnderTest = new XmlRpcAdaptor() {
      protected TimingOutCallback createTimeout(long timeout) {
        return timeoutCB;
      }      
    };
    classUnderTest.setGenerator(generator);
    classUnderTest.setRpcClient(rpcClient);
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    // TODO Auto-generated method stub
    super.tearDown();
    generatorControl = null;
    generator = null;
    rpcClientControl = null;
    rpcClient = null;
    timeoutCBControl = null;
    timeoutCB = null;
    classUnderTest = null;
  }

  /**
   * replays the mock controllers
   */
  protected void replay() {
    generatorControl.replay();
    rpcClientControl.replay();
    timeoutCBControl.replay();
  }
  
  /**
   * verifies the mock controllers
   */
  protected void verify() {
    generatorControl.verify();
    rpcClientControl.verify();
    timeoutCBControl.verify();
  }
  
  public void testGetType() throws Throwable {
    replay();
    assertEquals(XmlRpcAdaptorConfiguration.TYPE, classUnderTest.getType());
    verify();
  }
  
  public void testHandle() throws Throwable {
    MockControl handleControl = MockControl.createControl(HandleMethodMock.class);
    final HandleMethodMock handleMock = (HandleMethodMock)handleControl.getMock();
    MockControl adaptorCbControl = MockControl.createControl(IAdaptorCallback.class);
    IAdaptorCallback adaptorCb = (IAdaptorCallback)adaptorCbControl.getMock();

    IBltMessage message = new IBltMessage(){};
    handleMock.handle(message, adaptorCb);

    XmlRpcAdaptor classUnderTest = new XmlRpcAdaptor() {
      public void handle(IBltMessage message, IAdaptorCallback callback) {
        handleMock.handle(message, callback);
      }
    };
    classUnderTest.setCallback(adaptorCb);
    
    replay();
    adaptorCbControl.replay();
    handleControl.replay();
    
    // Execute test
    classUnderTest.handle(message);

    // Verify
    verify();
    handleControl.verify();
    adaptorCbControl.verify();
  }
  
  public void testHandle_withCb() throws Throwable {
    MockControl handleControl = MockControl.createControl(HandleMethodMock.class);
    final HandleMethodMock handleMock = (HandleMethodMock)handleControl.getMock();
    MockControl adaptorCbControl = MockControl.createControl(IAdaptorCallback.class);
    IAdaptorCallback adaptorCb = (IAdaptorCallback)adaptorCbControl.getMock();

    IBltMessage message = new IBltMessage(){};
    
    handleMock.handle(message, adaptorCb);

    XmlRpcAdaptor classUnderTest = new XmlRpcAdaptor() {
      public void handle(IBltMessage message, IAdaptorCallback callback) {
        handleMock.handle(message, callback);
      }
    };
    
    replay();
    adaptorCbControl.replay();
    handleControl.replay();
    
    // Execute test
    classUnderTest.handle(message, adaptorCb);

    // Verify
    verify();
    handleControl.verify();
    adaptorCbControl.verify();
  }
  
  public void testHandleMessage_withCb_success() throws Throwable {
    IBltMessage message = new IBltMessage(){};
    XmlRpcCommand cmd = new XmlRpcCommand();
    MockControl callbackControl = MockControl.createControl(IAdaptorCallback.class);
    IAdaptorCallback callback = (IAdaptorCallback)callbackControl.getMock();
    Object[] rpcArgs = new Object[]{};
    cmd.setMethod("command");
    cmd.setObjects(rpcArgs);
    
    Integer cbReturnCode = new Integer(0);
    generator.generate(message);
    generatorControl.setReturnValue(cmd);
    rpcClient.executeAsync("command", rpcArgs, timeoutCB);
    timeoutCB.waitForResponse();
    timeoutCBControl.setReturnValue(cbReturnCode);
    callback.success(message, cbReturnCode);
    replay();
    callbackControl.replay();
    
    // Execute test
    //Route route = new Route("XYZ", message);
    classUnderTest.handle(message,callback);

    // verify
    verify();
    callbackControl.verify();
  }

  public void testHandleMessage_withCb_success_nullCb() throws Throwable {
    IBltMessage message = new IBltMessage(){};
    XmlRpcCommand cmd = new XmlRpcCommand();
    Object[] rpcArgs = new Object[]{};
    cmd.setMethod("command");
    cmd.setObjects(rpcArgs);
    
    Integer cbReturnCode = new Integer(0);
    generator.generate(message);
    generatorControl.setReturnValue(cmd);
    rpcClient.executeAsync("command", rpcArgs, timeoutCB);
    timeoutCB.waitForResponse();
    timeoutCBControl.setReturnValue(cbReturnCode);
    replay();
    
    // Execute test
    //Route route = new Route("XYZ", message);
    classUnderTest.handle(message, null);

    // verify
    verify();
  }
  
  
  public void testHandleMessage_withCb_timeout() throws Throwable {
    IBltMessage message = new IBltMessage(){};
    XmlRpcCommand cmd = new XmlRpcCommand();
    MockControl callbackControl = MockControl.createControl(IAdaptorCallback.class);
    IAdaptorCallback callback = (IAdaptorCallback)callbackControl.getMock();
    Object[] rpcArgs = new Object[]{};
    cmd.setMethod("command");
    cmd.setObjects(rpcArgs);

    generator.generate(message);
    generatorControl.setReturnValue(cmd);
    rpcClient.executeAsync("command", rpcArgs, timeoutCB);
    timeoutCB.waitForResponse();
    timeoutCBControl.setThrowable(new TimingOutCallback.TimeoutException(0, "x"));
    callback.timeout(message);
    replay();
    callbackControl.replay();
    
    // Execute test
    //Route route = new Route("XYZ", message);
    classUnderTest.handle(message,callback);

    // verify
    verify();
    callbackControl.verify();
  }

  public void testHandleMessage_withCb_timeout_nullCb() throws Throwable {
    IBltMessage message = new IBltMessage(){};
    XmlRpcCommand cmd = new XmlRpcCommand();
    Object[] rpcArgs = new Object[]{};
    cmd.setMethod("command");
    cmd.setObjects(rpcArgs);

    generator.generate(message);
    generatorControl.setReturnValue(cmd);
    rpcClient.executeAsync("command", rpcArgs, timeoutCB);
    timeoutCB.waitForResponse();
    timeoutCBControl.setThrowable(new TimingOutCallback.TimeoutException(0, "x"));
    replay();
    
    // Execute test
    //Route route = new Route("XYZ", message);
    classUnderTest.handle(message, null);

    // verify
    verify();
  }
  
  public void testHandleMessage_withCb_error() throws Throwable {
    IBltMessage message = new IBltMessage(){};
    XmlRpcCommand cmd = new XmlRpcCommand();
    MockControl callbackControl = MockControl.createControl(IAdaptorCallback.class);
    IAdaptorCallback callback = (IAdaptorCallback)callbackControl.getMock();
    Object[] rpcArgs = new Object[]{};
    cmd.setMethod("command");
    cmd.setObjects(rpcArgs);

    generator.generate(message);
    generatorControl.setReturnValue(cmd);
    rpcClient.executeAsync("command", rpcArgs, timeoutCB);
    timeoutCB.waitForResponse();
    Throwable t = new NullPointerException();
    timeoutCBControl.setThrowable(t);
    callback.error(message, t);
    replay();
    callbackControl.replay();
    
    // Execute test
    //Route route = new Route("XYZ", message);
    classUnderTest.handle(message,callback);

    // verify
    verify();
    callbackControl.verify();
  }

  public void testHandleMessage_withCb_error_nullCb() throws Throwable {
    IBltMessage message = new IBltMessage(){};
    XmlRpcCommand cmd = new XmlRpcCommand();
    Object[] rpcArgs = new Object[]{};
    cmd.setMethod("command");
    cmd.setObjects(rpcArgs);

    generator.generate(message);
    generatorControl.setReturnValue(cmd);
    rpcClient.executeAsync("command", rpcArgs, timeoutCB);
    timeoutCB.waitForResponse();
    Throwable t = new NullPointerException();
    timeoutCBControl.setThrowable(t);
    replay();
    
    // Execute test
    //Route route = new Route("XYZ", message);
    classUnderTest.handle(message, null);

    // verify
    verify();
  }
  
  public void testHandleMessage_withCb_executeThrowsXmlRpcException() throws Exception {
    IBltMessage message = new IBltMessage(){};
    XmlRpcCommand cmd = new XmlRpcCommand();
    MockControl callbackControl = MockControl.createControl(IAdaptorCallback.class);
    IAdaptorCallback callback = (IAdaptorCallback)callbackControl.getMock();
    Object[] args = new Object[]{};
    cmd.setMethod("command");
    cmd.setObjects(args);
    
    generator.generate(message);
    generatorControl.setReturnValue(cmd);
    rpcClient.executeAsync("command", args, timeoutCB);
    rpcClientControl.setThrowable(new XmlRpcException("xys"));

    replay();
    callbackControl.replay();
    
    // Execute test
    //Route route = new Route("XYZ", message);
    try {
      classUnderTest.handle(message, callback);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }

    // verify
    verify();
    callbackControl.verify();
  }  
  
  public void testSetUrl() throws Exception {
    MockControl xmlRpcConfigControl = MockClassControl.createControl(XmlRpcClientConfigImpl.class);
    final XmlRpcClientConfigImpl xmlRpcConfig = (XmlRpcClientConfigImpl)xmlRpcConfigControl.getMock();
    final URL url = new URL("http://localhost");
    
    XmlRpcClient client = new XmlRpcClient();
    
    xmlRpcConfig.setServerURL(url);
    
    xmlRpcConfigControl.replay();
    
    
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
    xmlRpcConfigControl.verify();
    assertSame(result, xmlRpcConfig);
    assertEquals("http://localhost", classUnderTest.getUrl());
  }
  
  public void testSetUrl_malformed() throws Exception {
    MockControl xmlRpcConfigControl = MockClassControl.createControl(XmlRpcClientConfigImpl.class);
    final XmlRpcClientConfigImpl xmlRpcConfig = (XmlRpcClientConfigImpl)xmlRpcConfigControl.getMock();
    
    XmlRpcClient client = new XmlRpcClient();
    
    xmlRpcConfigControl.replay();
    
    
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
    xmlRpcConfigControl.verify();
  }
  
  public void testCreateUrl() throws Exception {
    String[] LEGAL_URLS = {"http://localhost", "http://localhost:1010",
        "http://localhost/something", "http://localhost:1010/something"};
    for (String url : LEGAL_URLS) {
      URL result = classUnderTest.createUrl(url);
      assertNotNull(result);
    }
  }
  
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
