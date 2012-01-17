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
 * A null reporter that doesn't do anything. 
 * @author Anders Henja
 */
public class NullReporter implements ISystemReporter {
  /**
   * @see eu.baltrad.beast.log.ISystemReporter#info(java.lang.String)
   */
  @Override
  public void info(String message) {
  }

  /**
   * @see eu.baltrad.beast.log.ISystemReporter#info(java.lang.String, java.lang.String, java.lang.Object[])
   */
  @Override
  public void info(String code, String message, Object... args) {
  }

  /**
   * @see eu.baltrad.beast.log.ISystemReporter#warn(java.lang.String, java.lang.String, java.lang.Object[])
   */
  @Override
  public void warn(String code, String message, Object... args) {
  }

  /**
   * @see eu.baltrad.beast.log.ISystemReporter#error(java.lang.String, java.lang.String, java.lang.Object[])
   */
  @Override
  public void error(String code, String message, Object... args) {
  }

  /**
   * @see eu.baltrad.beast.log.ISystemReporter#fatal(java.lang.String, java.lang.String, java.lang.Object[])
   */
  @Override
  public void fatal(String code, String message, Object... args) {
  }
}
