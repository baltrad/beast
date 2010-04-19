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
package eu.baltrad.beast.rules.timer;

/**
 * A listener that can be placed on a TimeoutTask to be notified
 * of events that has occured for the timeout.
 * @author Anders Henja
 */
public interface ITimeoutTaskListener {
  /**
   * Notification that a timeout occured
   * @param id the id
   */
  public void timeoutNotification(long id, ITimeoutRule rule);
  
  /**
   * Notification that a timeout has been cancelled
   * @param id the id
   */
  public void cancelNotification(long id, ITimeoutRule rule);
}
