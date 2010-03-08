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
package eu.baltrad.beast.adaptor;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.router.Route;

/**
 * @author Anders Henja
 */
public interface IAdaptor {
  /**
   * Returns the name that uniquely identifies this adaptor
   * @return the name of the adaptor
   */
  public String getName();
  
  /**
   * Handles a route. If this adaptor could not handle the route an AdaptorException should be
   * thrown.
   * @param route the route to handle
   * @throws AdaptorException
   */
  public void handle(Route route);
  
  /**
   * Same as {@link #handle(Route)} but with the possibility to get the result
   * to the callback
   * @param route the route
   * @param callback the callback
   * @throws AdaptorException
   */
  public void handle(Route route, IAdaptorCallback callback);
  
  /**
   * This is a non-routed version of the message sending, when using
   * this function, The adaptor must know the destination of the message
   * so it is optional to implement this function. If not supported an AdaptorException
   * should be thrown.
   * @param msg the message to send
   * @param callback the callback that should get the response
   * @throws AdaptorException - on error or if not supported.
   */
  public void handle(IBltMessage msg, IAdaptorCallback callback);
}
