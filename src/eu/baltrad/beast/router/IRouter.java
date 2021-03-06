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
 * Creates the routed messages for a message that should be sent. I.e. one message
 * can actually be intended for more than one target.
 * @author Anders Henja
 */
public interface IRouter {
  /**
   * Creates a list of routed messages that can be used for sending the messages
   * to several recipients.
   * @param msg the message that should be routed.
   * @return a list of multi routed messages
   */
  public List<IMultiRoutedMessage> getMultiRoutedMessages(IBltMessage msg);
  
  /**
   * Almost the same as {@link #getMultiRoutedMessages(IBltMessage)} but it returns
   * several IRoutedMessage's instead of several MultiRoutedMessage
   * @param msg the message
   * @return a list of zero or more routed messages
   */
  public List<IRoutedMessage> getRoutedMessages(IBltMessage msg);
}
