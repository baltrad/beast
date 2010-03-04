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

import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.server.XmlRpcNoSuchHandlerException;

import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class BaltradXMLRPCServerTest extends TestCase {
  public void testGetHandler() throws Exception {
    XmlRpcHandler handler1 = new XmlRpcHandler() {
      public Object execute(XmlRpcRequest request) throws XmlRpcException {return null;}
    };
    XmlRpcHandler handler2 = new XmlRpcHandler() {
      public Object execute(XmlRpcRequest request) throws XmlRpcException {return null;}
    };
    Map<String, XmlRpcHandler> handlers = new HashMap<String, XmlRpcHandler>();
    handlers.put("handler1", handler1);
    handlers.put("handler2", handler2);
    
    BaltradXMLRPCServer classUnderTest = new BaltradXMLRPCServer(12345);
    classUnderTest.setHandlers(handlers);
    
    // Execute test
    XmlRpcHandler result = classUnderTest.getHandler("handler2");
    
    // verify
    assertSame(handler2, result);
  }

  public void testGetHandler_noneFound() throws Exception {
    XmlRpcHandler handler1 = new XmlRpcHandler() {
      public Object execute(XmlRpcRequest request) throws XmlRpcException {return null;}
    };
    XmlRpcHandler handler2 = new XmlRpcHandler() {
      public Object execute(XmlRpcRequest request) throws XmlRpcException {return null;}
    };
    Map<String, XmlRpcHandler> handlers = new HashMap<String, XmlRpcHandler>();
    handlers.put("handler1", handler1);
    handlers.put("handler2", handler2);
    
    BaltradXMLRPCServer classUnderTest = new BaltradXMLRPCServer(12345);
    classUnderTest.setHandlers(handlers);
    
    // Execute test
    try {
      classUnderTest.getHandler("handler3");
      fail("Expected XmlRpcNoSuchHandlerException");
    } catch (XmlRpcNoSuchHandlerException e) {
      // pass
    }
  }
  
  public void testGetHandler_null() throws Exception {
    XmlRpcHandler handler1 = new XmlRpcHandler() {
      public Object execute(XmlRpcRequest request) throws XmlRpcException {return null;}
    };
    XmlRpcHandler handler2 = new XmlRpcHandler() {
      public Object execute(XmlRpcRequest request) throws XmlRpcException {return null;}
    };
    Map<String, XmlRpcHandler> handlers = new HashMap<String, XmlRpcHandler>();
    handlers.put("handler1", handler1);
    handlers.put("handler2", handler2);
    
    BaltradXMLRPCServer classUnderTest = new BaltradXMLRPCServer(12345);
    classUnderTest.setHandlers(handlers);
    
    // Execute test
    try {
      classUnderTest.getHandler(null);
      fail("Expected XmlRpcNoSuchHandlerException");
    } catch (XmlRpcNoSuchHandlerException e) {
      // pass
    }
  }  
}
