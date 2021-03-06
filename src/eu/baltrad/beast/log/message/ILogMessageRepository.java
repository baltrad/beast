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

package eu.baltrad.beast.log.message;

import java.util.List;

/**
 * Interface for supporting log messages belonging to various modules.
 * 
 * @author Anders Henja
 */
public interface ILogMessageRepository {
  /**
   * Returns a log message for the specified error code
   * @param ecode the error code
   * @return the log message or null if no message found
   */
  public LogMessage getMessage(String ecode);
  
  /**
   * Returns the module this error code belongs to
   * @param ecode the error code
   * @return the module or null if error code not could be found
   */
  public String getModule(String ecode);
  
  /**
   * Returns all messages belonging to a specific module. The
   * map has error codes as keys and log messages as values.
   * @param module the specific module
   * @return the messages, might be empty but will never be null
   */
  public List<LogMessage> getModuleMessages();
  
  /**
   * Returns a message for specified module, code. The provided message is
   * the default format string if no message can be found.
   * @param module the module
   * @param code the error code
   * @param message the default message
   * @param args the argument list
   * @return a message
   */
  public String getMessage(String code, String message, Object... args);
}
