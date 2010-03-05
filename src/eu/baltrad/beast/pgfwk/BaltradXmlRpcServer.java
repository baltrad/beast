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

import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.webserver.WebServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * The main xml-rpc server that listens to XMLRPC requests and ensures that the
 * correct plugin is called.
 * @author Anders Henja
 */
public class BaltradXmlRpcServer {
  /**
   * The web server 
   */
  private WebServer server = null;

  /**
   * The port number this server should be listening at
   */
  private int port = 56565;
  
  /**
   * The handler mapping
   */
  private XmlRpcHandlerMapping mapping = null;
  
  /**
   * Default constructor
   */
  public BaltradXmlRpcServer() {
  }

  /**
   * @param port the port to set
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * @return the port
   */
  public int getPort() {
    return port;
  }
 

  /**
   * @param mapping the mapping to set
   */
  public void setMapping(XmlRpcHandlerMapping mapping) {
    this.mapping = mapping;
  }

  /**
   * @return the mapping
   */
  public XmlRpcHandlerMapping getMapping() {
    return mapping;
  }  
  
  /**
   * Starts the XMLRPC server
   */
  public void start() throws IOException {
    server.start();
  }

  /**
   * Terminates the server
   */
  public void shutdown() {
    server.shutdown();
  }
  
  /**
   * Verifies that the argumens sent to the main function are correct. I.e.
   * either no context uri or one context uri.
   * @param args the arguments
   * @return a context uri
   * @throws IllegalArgumentException if the provided arguments are not valid
   */
  public static String getContextUriFromArguments(String[] args) throws IllegalArgumentException {
    String path = "classpath:etc/xmlrpcserver-context.xml";
    if (args.length == 1) {
      path = args[0];
    } else if (args.length > 1) {
      throw new IllegalArgumentException("Usage: " + BaltradXmlRpcServer.class.getName() + " [context-url]");
    }   
    return path;
  }
  
  /**
   * Main function for starting the server
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    String path = getContextUriFromArguments(args);
    ApplicationContext context = new FileSystemXmlApplicationContext(path);
    BaltradXmlRpcServer server = (BaltradXmlRpcServer)context.getBean("rpcserver");
    server.start();
  }
}
