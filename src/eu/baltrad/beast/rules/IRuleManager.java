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

/**
 * @author Anders Henja
 *
 */
public interface IRuleManager {
  /**
   * Persists the rule if there is any reason to do so
   * @param rule the rule to persist
   * @throws RuleException if rule not can be loaded
   */
  public void store(int rule_id, IRule rule);
  
  /**
   * Loads a rule
   * @param rule_id the rule to load
   * @return a rule
   * @throws RuleException if rule not can be loaded
   */
  public IRule load(int rule_id); 
  
  /**
   * Updates the rule with specified id
   * @param rule_id the id of the rule to update
   * @param rule the rule
   */
  public void update(int rule_id, IRule rule);
  
  /**
   * Deletes the rule with the specified id
   * @param rule_id the id of the rule to delete
   */
  public void delete(int rule_id);
  
  /**
   * @return a rule associated with this manager
   */
  public IRule createRule();
}
