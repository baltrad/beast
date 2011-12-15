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
package eu.baltrad.beast.log;

import org.apache.log4j.Logger;

/**
 * @author Anders Henja
 */
public class BeastReport {
  /**
   * The string to use for getting hold of the BEAST logger
   */
  private final static String MODULE="BEAST";
  
  /**
   * The logger
   */
  private final static Logger logger = Logger.getLogger(MODULE);
  
  /**
   * Reports the message.
   * @param message - the message
   */
  public static void info(String message) {
    logger.info(message);
  }
  
  /**
   * Reports the message.
   * @param message - the message
   */
  public static void warn(String message) {
    logger.warn(message);
  }
  
  /**
   * Reports the message.
   * @param message - the message
   */
  public static void error(String message) {
    logger.error(message);
  }
  
  /**
   * Reports the message.
   * @param message - the message
   */
  public static void fatal(String message) {
    logger.fatal(message);
  }
}
