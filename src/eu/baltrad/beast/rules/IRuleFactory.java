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
package eu.baltrad.beast.rules;

import java.util.List;

/**
 * Create any type of rule.
 * @author Anders Henja
 */
public interface IRuleFactory {
  /**
   * Creates a rule.
   * @param type - the type
   * @param definition - the definition
   * @return a rule
   * @throws RuleException if anything prevents this rule from be created
   */
  public IRule create(String type, String definition);
  
  /**
   * Returns a list of the currently supported types.
   * @return a list of types or an empty array if no types supported
   */
  public List<String> getTypes();
}
