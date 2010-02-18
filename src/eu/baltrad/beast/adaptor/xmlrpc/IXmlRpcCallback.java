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
package eu.baltrad.beast.adaptor.xmlrpc;

import eu.baltrad.beast.message.IBltMessage;

/**
 * Callback that can be used to identify results of the
 * {@link XmlRpcAdaptor#handle(eu.baltrad.beast.router.Route)}.
 * @author Anders Henja
 */
public interface IXmlRpcCallback {
  /**
   * Called on success
   * @param message - the message
   * @param result - the result
   */
  public void success(IBltMessage message, Object result);
  
  /**
   * Will be called if a timeout occurs
   * @param message the message that got a timeout
   */
  public void timeout(IBltMessage message);
  
  /**
   * Will be called if an error occurs during processing of the message
   * @param message the message that resulted in an error
   * @param t the throwable
   */
  public void error(IBltMessage message, Throwable t);
}
