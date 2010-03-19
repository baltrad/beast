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
   * The application context defining this instances
   */
  private ApplicationContext context = null;
  
  /**
   * Default constructor
   */
  protected BaltradXmlRpcServer(int port, ApplicationContext context, XmlRpcHandlerMapping mapping) {
    server = new WebServer(port);
    server.getXmlRpcServer().setHandlerMapping(mapping);
    this.context = context;
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
   * Returns the context defining this instance
   * @return the context
   */
  public ApplicationContext getContext() {
    return this.context;
  }
  
  /**
   * Creates a server instance from a list of arguments.
   *   --port=<port> The port to use, must be > 1024 (mandatory)
   *   --context=<contexturi> The context uri to use, default "classpath:*xmlrpcserver-context.xml"
   * @param args See description
   * @return a server instance on success
   * @throws a Throwable
   */
  public static BaltradXmlRpcServer createServerFromArguments(String[] args) {
    String path="classpath:*xmlrpcserver-context.xml";
    Integer port = 0;
    for (String str : args) {
      if (str.startsWith("--port=")) {
        String portstr = str.substring("--port=".length());
        port = Integer.parseInt(portstr);
      } else if (str.startsWith("--context")) {
        path = str.substring("--context=".length());
      } else {
        throw new IllegalArgumentException("Only allowed arguments are --port=<port> and --context=<context uri>");
      }
    }
    if (port <= 1024) {
      throw new IllegalArgumentException("Portnumber must be > 1024");
    }
    ApplicationContext context = new FileSystemXmlApplicationContext(path);
    XmlRpcHandlerMapping mapping = (XmlRpcHandlerMapping)context.getBean("pgfwkhandler");
    BaltradXmlRpcServer server = new BaltradXmlRpcServer(port, context, mapping);
    return server;
  }
  
  /**
   * Main function for starting the server
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    createServerFromArguments(args).start();
  }
}
