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
package eu.baltrad.beast.itest;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcNoSuchHandlerException;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;

/**
 * Test server for beeing able to verify xmlrpc commands and stuff
 * @author Anders Henja
 */
public class XmlRpcTestServer extends Thread implements XmlRpcHandler, XmlRpcHandlerMapping {
  private WebServer server = null;
  private int port = 0;
  private Object response = null;
  private boolean stopped = false;
  private long alivetime = 5000; // Keep server running for 5000 ms as default.
  private long responseTimeout = 0;
  
  private String requestMethod = null;
  private Object[] requestParams = null;
  
  public XmlRpcTestServer(int port) {
    this(port,null);
  }

  public XmlRpcTestServer(int port, Object response) {
    this.port = port;
    this.response = response;
  }
  
  public void setAliveTime(long alivetime) {
    this.alivetime = alivetime;
  }

  public void setResponseTimeout(long timeout) {
    this.responseTimeout = timeout;
  }
  
  public void start() {
    server = new WebServer(port);
    XmlRpcServer xmlRpcServer = server.getXmlRpcServer();
    try {
      xmlRpcServer.setHandlerMapping(this);
      server.start();
    } catch (Throwable t) {
      t.printStackTrace();
      throw new RuntimeException("Failed to create server", t);
    }    
    // Will start the timer that automatically shuts down the server
    super.start();
  }
  
  public synchronized void shutdown() {
    if (stopped == false) {
      server.shutdown();
      stopped = true;
      notify();
    }
  }
  
  public synchronized Object execute(XmlRpcRequest request) throws XmlRpcException {
    requestMethod = request.getMethodName();
    int nparam = request.getParameterCount();
    requestParams = new Object[nparam];
    for (int i = 0; i < nparam; i++) {
      requestParams[i] = request.getParameter(i);
    }
    if (responseTimeout != 0) {
      try {
        Thread.sleep(responseTimeout);
      } catch (Throwable t) {
        //
      }
    }
    notify();
    return response;
  }

  public XmlRpcHandler getHandler(String method)
      throws XmlRpcNoSuchHandlerException, XmlRpcException {
    return this;
  }
  
  public synchronized String waitForRequest(long timeout) {
    long currtime = System.currentTimeMillis();
    long endtime = currtime + timeout;
    while (this.requestMethod == null && (currtime < endtime) && !stopped) {
      try {
        wait(endtime - currtime);
      } catch (Throwable t) {
      }
      currtime = System.currentTimeMillis();
    }
    shutdown();
    return this.requestMethod;
  }
  
  public Object[] getRequestParameters() {
    return this.requestParams;
  }
  
  public synchronized void run() {
    long currtime = System.currentTimeMillis();
    long endtime = currtime + alivetime;
    while (this.requestMethod == null && (currtime < endtime) && !stopped) {
      try {
        wait(endtime - currtime);
      } catch (Throwable t) {
      }
      currtime = System.currentTimeMillis();
    }
    shutdown();
  }
}
