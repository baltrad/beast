/* --------------------------------------------------------------------
Copyright (C) 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.log;

/**
 * Beast reporter. Will report messages related to the BEAST framework.
 * @author Anders Henja
 * @date Dec 15, 2011
 */
public interface IBeastReporter {
  /**
   * Reports the message.
   * @param message - the message
   */
  public void info(String message);
  
  /**
   * Reports the message.
   * @param message - the message
   */
  public void warn(String message);
  
  /**
   * Reports the message.
   * @param message - the message
   */
  public void error(String message);
  
  /**
   * Reports the message.
   * @param message - the message
   */
  public void fatal(String message);  
}
