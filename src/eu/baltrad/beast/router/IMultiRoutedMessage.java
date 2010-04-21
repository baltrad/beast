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

import java.util.List;

import eu.baltrad.beast.message.IBltMessage;

/**
 * A message that should be sent to several destinations.
 * @author Anders Henja
 */
public interface IMultiRoutedMessage {
  /**
   * Returns the message that should be sent to the destination.
   * @return the message
   */
  public IBltMessage getMessage();
  
  /**
   * Returns a list of destinations (adaptor names).
   * @return the list of destinations
   */
  public List<String> getDestinations();
}
