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

import java.util.Map;

/**
 * Interface for supporting log messages belonging to various modules.
 * 
 * @author Anders Henja
 * @date 2011-12-17
 */
public interface ILogMessageRepository {
  /**
   * Returns a log message for the specified module and error code
   * @param module the module name
   * @param ecode the error code
   * @return the log message or null if no message found
   */
  public LogMessage getMessage(String module, String ecode);
  
  /**
   * Returns all messages belonging to a specific module. The
   * map has error codes as keys and log messages as values.
   * @param module the specific module
   * @return the messages, might be empty but will never be null
   */
  public Map<String,LogMessage> getModuleMessages(String module);
}
