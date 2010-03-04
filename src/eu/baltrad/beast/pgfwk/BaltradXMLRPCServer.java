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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcNoSuchHandlerException;
import org.apache.xmlrpc.webserver.WebServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * The main xml-rpc server that listens to XMLRPC requests and ensures that the
 * correct plugin is called.
 * @author Anders Henja
 */
public class BaltradXMLRPCServer implements XmlRpcHandlerMapping {
  /**
   * The registered handler functions.
   */
  private Map<String, XmlRpcHandler> handlers = new HashMap<String, XmlRpcHandler>();
  
  /**
   * The web server 
   */
  private WebServer server = null;
  
  /**
   * Default constructor
   */
  public BaltradXMLRPCServer(int port) {
    server = new WebServer(port);
    server.getXmlRpcServer().setHandlerMapping(this);
  }

  /**
   * Sets the handlers that are available.
   * @param handlers the handlers
   */
  public void setHandlers(Map<String, XmlRpcHandler> handlers) {
    this.handlers = handlers;
  }
  
  /**
   * Starts the XMLRPC server
   */
  public void startup() throws IOException {
    server.start();
  }

  /**
   * Terminates the server
   */
  public void shutdown() {
    server.shutdown();
  }
  
  /**
   * @see org.apache.xmlrpc.server.XmlRpcHandlerMapping#getHandler(java.lang.String)
   */
  @Override
  public XmlRpcHandler getHandler(String method)
      throws XmlRpcNoSuchHandlerException, XmlRpcException {
    XmlRpcHandler handler = handlers.get(method);
    if (handler == null) {
      throw new XmlRpcNoSuchHandlerException(""+method);
    }
    return handler;
  }
  
  /**
   * Main function for starting the server
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    ApplicationContext context = new FileSystemXmlApplicationContext("file:///projects/Baltrad/beast/etc/xmlrpcserver-context.xml");
    BaltradXMLRPCServer server = (BaltradXMLRPCServer)context.getBean("rpcserver");
    server.startup();
  }
}
