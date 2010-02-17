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
package eu.baltrad.beast.adaptor.impl;

import eu.baltrad.beast.adaptor.IAdaptor;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltCommandMessage;
import eu.baltrad.beast.router.Route;

/**
 * The XMLRPC adaptor
 * @author Anders Henja
 */
public class XmlRpcAdaptor implements IAdaptor {
  /**
   * @see eu.baltrad.beast.adaptor.IAdaptor#handle(eu.baltrad.beast.router.Route)
   */
  @Override
  public void handle(Route route) {
    IBltMessage message = route.getMessage();
    if (message.getClass() == BltCommandMessage.class) {
      handle((BltCommandMessage)message);
    }
  }

  /**
   * Handles a BltCommandMessage.
   * @param message the message to handle.
   */
  protected void handle(BltCommandMessage message) {
    String cmd = message.getCommand();
  }
  
/**
    Object[] objects = new Object[]{msg.getFilename(), msg.getProduct()};
    IXMLRPCMethod method = new XMLRPCMethod(uri, "generate", objects);
 */
}
