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
package eu.baltrad.beast.rules.bdb;

import java.util.Map;

import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleManager;
import eu.baltrad.beast.rules.IRulePropertyAccess;
import eu.baltrad.beast.rules.PropertyManager;

/**
 */

public class BdbTrimAgeRuleManager implements IRuleManager {
  /**
   * property manager
   */
  private PropertyManager propManager = null;

  /**
   * @param manager the manager to set
   */
  public void setPropertyManager(PropertyManager manager) {
    propManager = manager;
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#delete(int)
   */
  @Override
  public void delete(int ruleId) {
    propManager.deleteProperties(ruleId);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#load(int)
   */
  @Override
  public IRule load(int ruleId) {
    Map<String, String> props = propManager.loadProperties(ruleId);
    BdbTrimAgeRule rule = createRule();
    rule.setProperties(props);
    return rule;
  }
  
  /**
   * create new BdbTrimAgeRule instance
   */
  protected BdbTrimAgeRule createRule() {
    return new BdbTrimAgeRule();
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#store(int,
   *      eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void store(int ruleId, IRule rule) {
    IRulePropertyAccess sprops = (IRulePropertyAccess)rule;
    if (sprops != null) {
      propManager.storeProperties(ruleId, sprops.getProperties());
    }
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#update(int,
   *      eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void update(int ruleId, IRule rule) {
    IRulePropertyAccess sprops = (IRulePropertyAccess)rule;
    if (sprops != null) {
      propManager.updateProperties(ruleId, sprops.getProperties());
    }
  }
}
