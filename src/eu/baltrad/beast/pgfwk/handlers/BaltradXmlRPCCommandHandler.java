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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.XmlRpcRequest;

/**
 * @author Anders Henja
 */
public class BaltradXmlRPCCommandHandler implements XmlRpcHandler {
  
  private static class IOStreamPreemptier extends Thread {
    private StringBuffer msgbuffer = new StringBuffer();
    private InputStream is = null;
    
    private IOStreamPreemptier(InputStream is) {
      this.is = is;
    }
    
    public void run() {
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line=null;
        while ( (line = br.readLine()) != null) {
          msgbuffer.append(line+"\n");
        }
      } catch (IOException e) {
        e.printStackTrace();  
      }
    }
    
    @SuppressWarnings("unused")
    public String getTrace() {
      return msgbuffer.toString();
    }
  };
  
  /**
   * @see org.apache.xmlrpc.XmlRpcHandler#execute(org.apache.xmlrpc.XmlRpcRequest)
   */
  @Override
  public Object execute(XmlRpcRequest request) throws XmlRpcException {
    String command = (String)request.getParameter(0);
    Runtime rt = Runtime.getRuntime();
    Object result = new Integer(-1);
    try {
      Process proc = rt.exec(command);
      IOStreamPreemptier errreader = new IOStreamPreemptier(proc.getErrorStream()); 
      IOStreamPreemptier outreader = new IOStreamPreemptier(proc.getInputStream()); 
  
      errreader.start();
      outreader.start();
      
      int v = proc.waitFor();
      result = new Integer(v);
    } catch (Throwable e) {
    }
    return result;
  }
}
