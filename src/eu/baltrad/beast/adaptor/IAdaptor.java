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
   * Returns the type of this adaptor
   * @return the type of this adaptor (as defined when adding a new adaptor).
   */
  public String getType();
  
  /**
   * Handles a message. If this adaptor could not handle the message an AdaptorException
   * should be thrown. However, it is optional to throw the exception and instead
   * use a callback indicating an error, @see {@link #handle(IBltMessage, IAdaptorCallback)}.
   * This means that the adaptor itself MAY have a callback function that triggers
   * internally when this method is called.
   * @param msg the message
   * @throws AdaptorException
   */
  public void handle(IBltMessage msg);
  
  /**
   * Handles a message. This message will post a callback if provided.
   * @param msg the message to send
   * @param callback the callback that should get the response (MAY BE NULL)
   * @throws AdaptorException - on error or if not supported.
   */
  public void handle(IBltMessage msg, IAdaptorCallback callback);
}
