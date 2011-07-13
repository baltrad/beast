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
package eu.baltrad.beast.rules.dist;

import java.util.HashMap;
import java.util.Map;

import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleManager;
import eu.baltrad.beast.rules.IRulePropertyAccess;
import eu.baltrad.beast.rules.PropertyManager;
import eu.baltrad.beast.rules.RuleFilterManager;

/**
 */
public class DistributionRuleManager implements IRuleManager {
  /**
   * property manager
   */
  private PropertyManager propManager = null;
  
  /**
   * filter manager
   */
  private RuleFilterManager filterManager = null;
  
  /**
   * @param manager the manager to set
   */
  public void setPropertyManager(PropertyManager manager) {
    propManager = manager;
  }

  /**
   * @param manager the manager to set
   */
  public void setRuleFilterManager(RuleFilterManager manager) {
    filterManager = manager;
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#delete(int)
   */
  @Override
  public void delete(int ruleId) {
    propManager.deleteProperties(ruleId);
    filterManager.deleteFilters(ruleId);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#load(int)
   */
  @Override
  public IRule load(int ruleId) {
    DistributionRule rule = createRule();
    Map<String, String> props = propManager.loadProperties(ruleId);
    rule.setProperties(props);
    Map<String, IFilter> filters = filterManager.loadFilters(ruleId);
    rule.setFilter(filters.get("match"));
    return rule;
  }
  
  /**
   * create new BdbTrimAgeRule instance
   */
  @Override
  public DistributionRule createRule() {
    DistributionRule result = new DistributionRule();
    return result;
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#store(int,
   *      eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void store(int ruleId, IRule rule_) {
    DistributionRule rule = (DistributionRule)rule_;
    propManager.storeProperties(ruleId, rule.getProperties());
    Map<String, IFilter> filters = new HashMap<String, IFilter>();
    filters.put("match", rule.getFilter());
    filterManager.storeFilters(ruleId, filters);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#update(int,
   *      eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void update(int ruleId, IRule rule_) {
    DistributionRule rule = (DistributionRule)rule_;
    propManager.updateProperties(ruleId, rule.getProperties());
    Map<String, IFilter> filters = new HashMap<String, IFilter>();
    filters.put("match", rule.getFilter());
    filterManager.updateFilters(ruleId, filters);
  }
}
