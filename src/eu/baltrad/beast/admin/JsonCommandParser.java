/* --------------------------------------------------------------------
Copyright (C) 2009-2021 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.admin;

import java.io.InputStream;

/**
 * Interface for processing commands
 * @author anders
 */
public interface JsonCommandParser {
  /**
   * Parses a JSON request if supported.
   * @throws a RuntimeException if not handled
   */
  public Command parse(InputStream inputStream);
  
  /**
   * Parses a JSON request if supported.
   * @throws a RuntimeException if not handled
   */
  public Command parse(String s);
  
  /**
   * @return the help as a json text
   */
  public String getHelp();
  
  /**
   * Returns help about specified method. If method is null, then this is same as calling {@link #getHelp()}
   * @param method the method
   * @return the help text
   */
  public String getHelp(String method);
}
