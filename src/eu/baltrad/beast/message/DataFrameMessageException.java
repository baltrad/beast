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
package eu.baltrad.beast.message;

/**
 * @author Anders Henja
 */
public class DataFrameMessageException extends RuntimeException {
  /**
   * The default serial uid 
   */
  private static final long serialVersionUID = 1L;

  /**
   * @see RuntimeException#RuntimeException()
   */
  public DataFrameMessageException() {
    super();
  }
  
  /**
   * @see RuntimeException#RuntimeException(String)
   */
  public DataFrameMessageException(String message) {
    super(message);
  }

  /**
   * @see RuntimeException#RuntimeException(Throwable)
   */
  public DataFrameMessageException(Throwable t) {
    super(t);
  }

  /**
   * @see RuntimeException#RuntimeException(String, Throwable)
   */
  public DataFrameMessageException(String message, Throwable t) {
    super(message, t);
  }
}