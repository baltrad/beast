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
package eu.baltrad.beast.adaptor;

/**
 * Thrown when there is a problem with an address.
 * @author Anders Henja
 */
public class AdaptorAddressException extends AdaptorException {
  /**
   * The default serial uid 
   */
  private static final long serialVersionUID = 1L;

  /**
   * @see AdaptorException#AdaptorException()
   */
  public AdaptorAddressException() {
    super();
  }
  
  /**
   * @see AdaptorException#AdaptorException(String)
   */
  public AdaptorAddressException(String message) {
    super(message);
  }

  /**
   * @see AdaptorException#AdaptorException(Throwable)
   */
  public AdaptorAddressException(Throwable t) {
    super(t);
  }

  /**
   * @see AdaptorException#AdaptorException(String, Throwable)
   */
  public AdaptorAddressException(String message, Throwable t) {
    super(message, t);
  }
}
