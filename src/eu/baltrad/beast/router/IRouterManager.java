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
package eu.baltrad.beast.router;

import java.util.List;

/**
 * Manages all routes and synchronizes them with the database.
 * 
 * @author Anders Henja
 */
public interface IRouterManager {
  /**
   * Gets the current routing definitions
   * 
   * @return a list of routing definitions
   */
  public List<RouteDefinition> getDefinitions();

  /**
   * Returns the definition with specified name
   * 
   * @param name - the name
   * @return the associated definintion
   */
  public RouteDefinition getDefinition(String name);

  /**
   * Stores a definition in the database
   * @param def the definition to store
   * @throws RuleException if something is erroneous with the definition
   */
  public void storeDefinition(RouteDefinition def);
  
  /**
   * Updates the definition in the database
   * @param def the definition to update
   * @throws RuleException if something is erroneous with the definition
   */
  public void updateDefinition(RouteDefinition def);
  
  /**
   * Deletes the definition with the specified name
   * @param name the name of the definition to be removed.
   */
  public void deleteDefinition(String name);

}
