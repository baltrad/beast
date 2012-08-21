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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import org.apache.xmlrpc.XmlRpcRequest;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.adaptor.http.IHttpConnector;
import eu.baltrad.beast.message.IBltDataFrameMessageFactory;
import eu.baltrad.beast.message.mo.BltDataFrameMessage;
import eu.baltrad.beast.pgfwk.IGeneratorPlugin;

/**
 * @author Anders Henja
 */
public class BaltradXmlRpcGenerateHandlerTest extends EasyMockSupport {
  private IBltDataFrameMessageFactory factory = null;
  private IHttpConnector connector = null;
  private BaltradXmlRpcGenerateHandler classUnderTest = null;
  private ApplicationContext context = null;

  
  static interface MockMethods {
    public String[] createStringArray(Object[] arr);
  };

  @Before
  public void setUp() throws Exception {
    factory = createMock(IBltDataFrameMessageFactory.class);
    connector = createMock(IHttpConnector.class);
    context = createMock(ApplicationContext.class);
    classUnderTest = new BaltradXmlRpcGenerateHandler();
    classUnderTest.setConnector(connector);
    classUnderTest.setDataFrameMessageFactory(factory);
    classUnderTest.setApplicationContext(context);
  }
  
  @After
  public void tearDown() throws Exception {
    factory = null;
    connector = null;
    context = null;
    classUnderTest = null;
  }
  
  @Test
  public void testCreateStringArray() {
    Object[] strs = new String[]{"a", "b"};
    String[] result = classUnderTest.createStringArray(strs);
    assertEquals(2, result.length);
    assertEquals("a", result[0]);
    assertEquals("b", result[1]);
  }
  
  @Test
  public void testCreateStringArray_null() {
    String[] result = classUnderTest.createStringArray(null);
    assertEquals(0,result.length);
  }

  @Test
  public void testCreateStringArray_empty() {
    String[] result = classUnderTest.createStringArray(new Object[]{});
    assertEquals(0,result.length);
  }
  
  @Test
  public void testExecute() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    XmlRpcRequest request = createMock(XmlRpcRequest.class);
    IGeneratorPlugin pluginMock = createMock(IGeneratorPlugin.class);
    
    Object[] ofiles = new Object[0];
    Object[] oargs = new Object[0];
    String[] files = new String[0];
    String[] args = new String[0];
    BltDataFrameMessage bltmessage = new BltDataFrameMessage();
    
    BaltradXmlRpcGenerateHandler classUnderTest = new BaltradXmlRpcGenerateHandler() {
      protected String[] createStringArray(Object[] arr) {
        return methods.createStringArray(arr);
      }
    };
    classUnderTest.setConnector(connector);
    classUnderTest.setDataFrameMessageFactory(factory);
    classUnderTest.setApplicationContext(context);

    expect(request.getParameter(0)).andReturn("somemethod");
    expect(request.getParameter(1)).andReturn(ofiles);
    expect(request.getParameter(2)).andReturn(oargs);
    
    expect(methods.createStringArray(ofiles)).andReturn(files);
    expect(methods.createStringArray(oargs)).andReturn(args);
    
    expect(context.getBean("somemethod")).andReturn(pluginMock);
    
    expect(pluginMock.generate("somemethod", files, args)).andReturn("filename");
    expect(factory.createMessage("filename")).andReturn(bltmessage);
    connector.send(bltmessage);
    
    replayAll();
    
    // Execute
    Object result = classUnderTest.execute(request);
    
    // verify
    verifyAll();
    assertEquals(0, result);
  }
  
  @Test
  public void testExecute_pluginNotFound() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    ApplicationContext context = createMock(ApplicationContext.class);
    XmlRpcRequest request = createMock(XmlRpcRequest.class);
    
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
    
    expect(request.getParameter(0)).andReturn("somemethod");
    expect(request.getParameter(1)).andReturn(ofiles);
    expect(request.getParameter(2)).andReturn(oargs);
    
    expect(methods.createStringArray(ofiles)).andReturn(files);
    expect(methods.createStringArray(oargs)).andReturn(args);
    
    expect(context.getBean("somemethod")).andReturn(null);

    replayAll();
    
    // Execute
    Object result = classUnderTest.execute(request);
    
    // verify
    verifyAll();
    assertEquals(-1, result);
  }
  
  @Test
  public void testExecute_pluginNotIGenerator() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    ApplicationContext context = createMock(ApplicationContext.class);
    XmlRpcRequest request = createMock(XmlRpcRequest.class);
    
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
    
    expect(request.getParameter(0)).andReturn("somemethod");
    expect(request.getParameter(1)).andReturn(ofiles);
    expect(request.getParameter(2)).andReturn(oargs);
    
    expect(methods.createStringArray(ofiles)).andReturn(files);
    expect(methods.createStringArray(oargs)).andReturn(args);
    
    expect(context.getBean("somemethod")).andReturn(plugin);

    replayAll();
    
    // Execute
    Object result = classUnderTest.execute(request);
    
    // verify
    verifyAll();
    assertEquals(-1, result);
  }  
  
//  @Test
//  public void testCreateMessage() throws Exception {
//    BaltradXmlRpcGenerateHandler classUnderTest = new BaltradXmlRpcGenerateHandler();
//    classUnderTest.setNodename("thisnode");
//    classUnderTest.setNodeaddress("thisaddress");
//    classUnderTest.setUrl("http://localhost");
//    
//    BltDataFrameMessage message = classUnderTest.createMessage("somefile");
//    
//    assertEquals("thisnode", message.getHeader("Node-Name"));
//    assertEquals("thisaddress", message.getHeader("Node-Address"));
//    assertEquals("application/x-hdf5", message.getHeader("Content-Type"));
//    assertEquals(Base64.encodeBase64String("http://localhost".getBytes()), message.getHeader("Content-MD5"));
//    assertEquals("somefile", message.getFilename());
//  }
}
