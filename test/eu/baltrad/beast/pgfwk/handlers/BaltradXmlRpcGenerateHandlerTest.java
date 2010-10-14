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
package eu.baltrad.beast.pgfwk.handlers;

import org.apache.xmlrpc.XmlRpcRequest;
import org.easymock.MockControl;
import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.adaptor.http.IHttpConnector;
import eu.baltrad.beast.message.mo.BltDataFrameMessage;
import eu.baltrad.beast.pgfwk.IGeneratorPlugin;

import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class BaltradXmlRpcGenerateHandlerTest extends TestCase {
  static interface MockMethods {
    public String[] createStringArray(Object[] arr);
    public BltDataFrameMessage createMessage(String file);
  };
  
  public void testCreateStringArray() {
    BaltradXmlRpcGenerateHandler classUnderTest = new BaltradXmlRpcGenerateHandler();
    Object[] strs = new String[]{"a", "b"};
    String[] result = classUnderTest.createStringArray(strs);
    assertEquals(2, result.length);
    assertEquals("a", result[0]);
    assertEquals("b", result[1]);
  }
  
  public void testCreateStringArray_null() {
    BaltradXmlRpcGenerateHandler classUnderTest = new BaltradXmlRpcGenerateHandler();
    String[] result = classUnderTest.createStringArray(null);
    assertEquals(0,result.length);
  }

  public void testCreateStringArray_empty() {
    BaltradXmlRpcGenerateHandler classUnderTest = new BaltradXmlRpcGenerateHandler();
    String[] result = classUnderTest.createStringArray(new Object[]{});
    assertEquals(0,result.length);
  }
  
  public void testExecute() throws Exception {
    MockControl methodsControl = MockControl.createControl(MockMethods.class);
    final MockMethods methods = (MockMethods)methodsControl.getMock();
    
    MockControl contextControl = MockControl.createControl(ApplicationContext.class);
    ApplicationContext context = (ApplicationContext)contextControl.getMock();
    
    MockControl requestControl = MockControl.createControl(XmlRpcRequest.class);
    XmlRpcRequest request = (XmlRpcRequest)requestControl.getMock();
    
    MockControl pluginControl = MockControl.createControl(IGeneratorPlugin.class);
    IGeneratorPlugin pluginMock = (IGeneratorPlugin)pluginControl.getMock();
    
    MockControl connectorControl = MockControl.createControl(IHttpConnector.class);
    IHttpConnector connector = (IHttpConnector)connectorControl.getMock();
    
    
    Object[] ofiles = new Object[0];
    Object[] oargs = new Object[0];
    String[] files = new String[0];
    String[] args = new String[0];
    BltDataFrameMessage bltmessage = new BltDataFrameMessage();
    
    BaltradXmlRpcGenerateHandler classUnderTest = new BaltradXmlRpcGenerateHandler() {
      protected String[] createStringArray(Object[] arr) {
        return methods.createStringArray(arr);
      }
      protected BltDataFrameMessage createMessage(String file) {
        return methods.createMessage(file);
      }
    };
    classUnderTest.setApplicationContext(context);
    classUnderTest.setConnector(connector);

    request.getParameter(0);
    requestControl.setReturnValue("somemethod");
    request.getParameter(1);
    requestControl.setReturnValue(ofiles);
    request.getParameter(2);
    requestControl.setReturnValue(oargs);
    
    methods.createStringArray(ofiles);
    methodsControl.setReturnValue(files);
    methods.createStringArray(oargs);
    methodsControl.setReturnValue(args);
    
    context.getBean("somemethod");
    contextControl.setReturnValue(pluginMock);
    
    pluginMock.generate("somemethod", files, args);
    pluginControl.setReturnValue("filename");
    methods.createMessage("filename");
    methodsControl.setReturnValue(bltmessage);
    connector.send(bltmessage);
    
    methodsControl.replay();
    contextControl.replay();
    requestControl.replay();
    pluginControl.replay();
    
    // Execute
    Object result = classUnderTest.execute(request);
    
    // verify
    methodsControl.verify();
    contextControl.verify();
    requestControl.verify();
    pluginControl.verify();
    assertEquals(0, result);
  }
  
  public void testExecute_pluginNotFound() throws Exception {
    MockControl methodsControl = MockControl.createControl(MockMethods.class);
    final MockMethods methods = (MockMethods)methodsControl.getMock();
    
    MockControl contextControl = MockControl.createControl(ApplicationContext.class);
    ApplicationContext context = (ApplicationContext)contextControl.getMock();
    
    MockControl requestControl = MockControl.createControl(XmlRpcRequest.class);
    XmlRpcRequest request = (XmlRpcRequest)requestControl.getMock();
    
    Object[] ofiles = new Object[0];
    Object[] oargs = new Object[0];
    String[] files = new String[0];
    String[] args = new String[0];
    
    BaltradXmlRpcGenerateHandler classUnderTest = new BaltradXmlRpcGenerateHandler() {
      protected String[] createStringArray(Object[] arr) {
        return methods.createStringArray(arr);
      }
    };
    classUnderTest.setApplicationContext(context);
    
    request.getParameter(0);
    requestControl.setReturnValue("somemethod");
    request.getParameter(1);
    requestControl.setReturnValue(ofiles);
    request.getParameter(2);
    requestControl.setReturnValue(oargs);
    
    methods.createStringArray(ofiles);
    methodsControl.setReturnValue(files);
    methods.createStringArray(oargs);
    methodsControl.setReturnValue(args);
    
    context.getBean("somemethod");
    contextControl.setReturnValue(null);

    methodsControl.replay();
    contextControl.replay();
    requestControl.replay();
    
    // Execute
    Object result = classUnderTest.execute(request);
    
    // verify
    methodsControl.verify();
    contextControl.verify();
    requestControl.verify();
    assertEquals(-1, result);
  }
  
  public void testExecute_pluginNotIGenerator() throws Exception {
    MockControl methodsControl = MockControl.createControl(MockMethods.class);
    final MockMethods methods = (MockMethods)methodsControl.getMock();
    
    MockControl contextControl = MockControl.createControl(ApplicationContext.class);
    ApplicationContext context = (ApplicationContext)contextControl.getMock();
    
    MockControl requestControl = MockControl.createControl(XmlRpcRequest.class);
    XmlRpcRequest request = (XmlRpcRequest)requestControl.getMock();
    
    Object[] ofiles = new Object[0];
    Object[] oargs = new Object[0];
    String[] files = new String[0];
    String[] args = new String[0];
    
    Object plugin = new Object();
    
    BaltradXmlRpcGenerateHandler classUnderTest = new BaltradXmlRpcGenerateHandler() {
      protected String[] createStringArray(Object[] arr) {
        return methods.createStringArray(arr);
      }
    };
    classUnderTest.setApplicationContext(context);
    
    request.getParameter(0);
    requestControl.setReturnValue("somemethod");
    request.getParameter(1);
    requestControl.setReturnValue(ofiles);
    request.getParameter(2);
    requestControl.setReturnValue(oargs);
    
    methods.createStringArray(ofiles);
    methodsControl.setReturnValue(files);
    methods.createStringArray(oargs);
    methodsControl.setReturnValue(args);
    
    context.getBean("somemethod");
    contextControl.setReturnValue(plugin);

    methodsControl.replay();
    contextControl.replay();
    requestControl.replay();
    
    // Execute
    Object result = classUnderTest.execute(request);
    
    // verify
    methodsControl.verify();
    contextControl.verify();
    requestControl.verify();
    assertEquals(-1, result);
  }  
  
  public void testCreateMessage() throws Exception {
    BaltradXmlRpcGenerateHandler classUnderTest = new BaltradXmlRpcGenerateHandler();
    classUnderTest.setChannel("se_channel");
    classUnderTest.setSender("admin");
    
    BltDataFrameMessage message = classUnderTest.createMessage("somefile");
    
    assertEquals("se_channel", message.getChannel());
    assertEquals("admin", message.getSender());
    assertEquals("somefile", message.getFilename());
  }
}
