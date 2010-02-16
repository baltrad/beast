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
package eu.baltrad.beast.router;

import eu.baltrad.beast.message.IBltMessage;

/**
 * @author Anders Henja
 */
public class Route {
  /**
   * The destination, for example name of adaptor or similar.
   */
  private String destination = null;
  
  /**
   * The message that should be sent.
   */
  private IBltMessage message = null;

  /**
   * Default constructor;
   */
  public Route() {
  }
  
  /**
   * Constructor
   * @param destination - the destination
   * @param message - the message to send
   */
  public Route(String destination, IBltMessage message) {
    setDestination(destination);
    setMessage(message);
  }
  
  /**
   * Sets the message
   * @param msg the message to set
   */
  public void setMessage(IBltMessage msg) {
    this.message = msg;
  }
  
  /**
   * Returns the message associated with this route.
   * @return the message
   */
  public IBltMessage getMessage() {
    return this.message;
  }

  /**
   * Sets the destination.
   * @param destination - the destination to set
   */
  public void setDestination(String destination) {
    this.destination = destination;
  }
  
  /**
   * Returns the name of the destination, probably an adaptor.
   * @return the name of the destination
   */
  public String getDestination() {
    return this.destination;
  }
}
