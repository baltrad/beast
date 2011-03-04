/*
Copyright 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

This file is part of Beast library.

Beast library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Beast library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Beast library.  If not, see <http://www.gnu.org/licenses/>.
*/

package eu.baltrad.beast.db;

/**
 * manage persisting and loading IFilter instances
 */
public interface IFilterManager {
  /**
   * store a filter
   * @param filter the filter to store
   */
  public void store(IFilter filter);

  /**
   * load a filter
   * @param id the filter identifier
   */
  public IFilter load(int id);

  /**
   * update a filter
   * @param filter filter to update
   */
  public void update(IFilter filter);

  /**
   * remove a filter
   * @param filter filter to remove
   */
  public void remove(IFilter filter);
};
