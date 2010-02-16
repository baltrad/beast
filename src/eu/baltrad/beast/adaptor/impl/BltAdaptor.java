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
package eu.baltrad.beast.adaptor.impl;

import java.util.Map;

import eu.baltrad.beast.adaptor.AdaptorException;
import eu.baltrad.beast.adaptor.IAdaptor;
import eu.baltrad.beast.router.Route;

/**
 * @author Anders Henja
 */
public class BltAdaptor implements IAdaptor {
  /**
   * The registered adaptors
   */
  private Map<String, IAdaptor> adaptors = null;
  
  /**
   * Sets the adaptors, mostly used for test purposes. The adaptors
   * are read from the database.
   * @param adaptors the adaptors
   */
  void setAdaptors(Map<String, IAdaptor> adaptors) {
    this.adaptors = adaptors;
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.IAdaptor#handle(eu.baltrad.beast.router.Route)
   */
  @Override
  public void handle(Route route) {
    IAdaptor adaptor = adaptors.get(route.getDestination());
    if (adaptor == null) {
      throw new AdaptorException("No adaptor able to handle the route");
    }
    adaptor.handle(route);
  }
}
