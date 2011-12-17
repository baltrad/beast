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
 * Implementation of the beast reporter
 * @author Anders Henja
 */
public class BeastReporter implements ISystemReporter{
  /**
   * The string to use for getting hold of the BEAST logger
   */
  private final static String MODULE = "BEAST";

  /**
   * The log message repository
   */
  private ILogMessageRepository repository = null;
  
  /**
   * The logger
   */
  Logger logger = Logger.getLogger(MODULE);
  
  /**
   * Sets the log message repository
   * @param repository the repository
   */
  public void setRepository(ILogMessageRepository repository) {
    this.repository = repository;
  }
  
  /**
   * Reports the message
   * @param message - the message
   */
  public void info(String message) {
    logger.info("IXXXXX: " + message);
  }
  
  /**
   * Reports the message.
   * @param message - the message
   */
  public void info(String code, String message, Object... args) {
    logger.info(String.format("I%s: %s", code, getMessage(code, message, args)));
  }
  
  /**
   * Reports the message.
   * @param message - the message
   */
  public void warn(String code, String message, Object... args) {
    logger.warn(String.format("W%s: %s", code, getMessage(code, message, args)));
  }
  
  /**
   * Reports the message.
   * @param message - the message
   */
  public void error(String code, String message, Object... args) {
    logger.error(String.format("E%s: %s", code, getMessage(code, message, args)));
  }
  
  /**
   * Reports the message.
   * @param message - the message
   */
  public void fatal(String code, String message, Object... args) {
    logger.fatal(String.format("F%s: %s", code, getMessage(code, message, args)));
  }
  
  /**
   * Creates a message according to the string format
   * @param code
   * @param message
   * @param objects
   * @return
   */
  protected String getMessage(String code, String message, Object... args) {
    String msg = null;
    if (repository != null) {
      LogMessage logmsg = repository.getMessage(MODULE, code);
      if (logmsg != null) {
        try {
          msg = String.format(logmsg.getMessage(), args);
        } catch (Exception e) {
          // let default message be used instead and that one should not fail
        }
      }
    }
    
    if (msg == null) {
      msg = String.format(message, args);
    }
    
    return msg;
  }
}
