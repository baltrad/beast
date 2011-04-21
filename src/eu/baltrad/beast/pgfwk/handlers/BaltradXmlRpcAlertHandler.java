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

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.XmlRpcRequest;

import eu.baltrad.beast.pgfwk.IAlertPlugin;

/**
 * @author Anders Henja
 */
public class BaltradXmlRpcAlertHandler implements XmlRpcHandler {
  /**
   * A list of plugins
   */
  private List<IAlertPlugin> plugins = new ArrayList<IAlertPlugin>();

  /**
   * @see org.apache.xmlrpc.XmlRpcHandler#execute(org.apache.xmlrpc.XmlRpcRequest)
   */
  @Override
  public Object execute(XmlRpcRequest request) throws XmlRpcException {
    String ecode = (String)request.getParameter(0);
    String message = (String)request.getParameter(1);
    for (IAlertPlugin plugin: plugins) {
      try {
        plugin.alert(ecode, message);
      } catch (RuntimeException t) {
        t.printStackTrace();
      }
    }
    return new Integer(0);
  }
  
  /**
   * Initializes the alert handler with the specified alert plugins
   * @param plugins a list of plugins
   */
  public void setPlugins(List<IAlertPlugin> plugins) {
    this.plugins = plugins;
  }
}
