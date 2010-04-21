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
package eu.baltrad.beast.message.mo;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.router.IRoutedMessage;

/**
 * @author Anders Henja
 *
 */
public class BltRoutedMessage implements IBltMessage, IRoutedMessage {
  /**
   * The message that is routed
   */
  private IBltMessage message = null;
  
  /**
   * The destination of the message
   */
  private String destination = null;
  
  /**
   * Constructor
   */
  public BltRoutedMessage() {
  }

  /**
   * @param message the message to set
   */
  public void setMessage(IBltMessage message) {
    this.message = message;
  }

  /**
   * @return the message
   */
  @Override
  public IBltMessage getMessage() {
    return message;
  }

  /**
   * @param destinations the destinations to set
   */
  public void setDestination(String destination) {
    this.destination = destination;
  }

  /**
   * @see eu.baltrad.beast.router.IRoutedMessage#getDestination()
   */
  @Override
  public String getDestination() {
    return this.destination;
  }
}
