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
package eu.baltrad.beast.db;

import eu.baltrad.fc.db.AttributeQuery;

/**
 * @author Anders Henja
 *
 */
public interface ICatalogFilter {
  /**
   * Applies this filters rules to the query. If you need to fetch specific attributes
   * do that in here as well, but remember that the order of the fetches must be
   * visible when running getExtraAttributes.
   * 
   * That means that if apply adds:
   *   query.fetch("something");
   *   query.fetch("else");
   * then getExtraAttributes must return:
   *   "something", "else" in this specific order.
   * 
   * @param query the query to apply the filtering rules to
   */
  void apply(AttributeQuery query);
  
  /**
   * These attributes will be accessible within the CatalogEntry
   * @return the extra attributes to fetch (may be NULL or empty)
   */
  String[] getExtraAttributes();
}
