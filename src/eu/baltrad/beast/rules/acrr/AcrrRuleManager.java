/* --------------------------------------------------------------------
Copyright (C) 2009-2013 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.rules.acrr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleManager;
import eu.baltrad.beast.rules.RuleFilterManager;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class AcrrRuleManager implements IRuleManager {
  /**
   * The jdbc template
   */
  private JdbcOperations template = null;
  
  /**
   * The catalog
   */
  private Catalog catalog = null;
  
  /**
   * The rule utilities
   */
  private IRuleUtilities ruleUtilities = null;
  
  /**
   * filter manager
   */
  private RuleFilterManager filterManager;
  
  /**
   * @param template the jdbc template to set
   */
  public void setJdbcTemplate(JdbcOperations template) {
    this.template = template;
  }
  
  /**
   * @param catalog the catalog to set
   */
  public void setCatalog(Catalog catalog) {
    this.catalog = catalog;
  }
  
  /**
   * @param utilities the rule utilities to set
   */
  public void setRuleUtilities(IRuleUtilities utilities) {
    this.ruleUtilities = utilities;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRuleManager#store(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void store(int rule_id, IRule rule) {
    AcrrRule arule = (AcrrRule)rule;
    String area = arule.getArea();
    String dfield = arule.getDistancefield();
    int fhours = arule.getFilesPerHour();
    int hours = arule.getHours();
    int aloss = arule.getAcceptableLoss();
    String otype = arule.getObjectType();
    String quantity = arule.getQuantity();
    double zrA = arule.getZrA();
    double zrB = arule.getZrB();
    boolean applygra = arule.isApplyGRA();
    
    template.update("INSERT INTO beast_acrr_rules (rule_id, area, distancefield, files_per_hour, hours, acceptable_loss, object_type, quantity, zra, zrb, applygra) VALUES (?,?,?,?,?,?,?,?,?,?,?)", 
        new Object[]{rule_id, area, dfield, fhours, hours, aloss, otype, quantity, zrA, zrB, applygra});
    
    arule.setRuleId(rule_id);
    storeFilter(rule_id, arule.getFilter());
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#load(int)
   */
  @Override
  public IRule load(int rule_id) {
    AcrrRule rule = template.queryForObject("SELECT * FROM beast_acrr_rules WHERE rule_id=?", 
        getAcrrRuleMapper(),
        new Object[]{rule_id});
    rule.setFilter(loadFilter(rule_id));
    return rule;
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#update(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void update(int rule_id, IRule rule) {
    AcrrRule arule = (AcrrRule)rule;
    String area = arule.getArea();
    String dfield = arule.getDistancefield();
    int fhours = arule.getFilesPerHour();
    int hours = arule.getHours();
    int acceptable_loss = arule.getAcceptableLoss();
    String otype = arule.getObjectType();
    String quantity = arule.getQuantity();
    double zra = arule.getZrA();
    double zrb = arule.getZrB();
    boolean applygra = arule.isApplyGRA();
    
    template.update("UPDATE beast_acrr_rules SET " +
        "area=?, distancefield=?, files_per_hour=?, hours=?, acceptable_loss=?, object_type=?, quantity=?, zra=?, zrb=?, applygra=? WHERE rule_id=?",
        new Object[]{area, dfield, fhours, hours, acceptable_loss, otype, quantity, zra, zrb, applygra, rule_id});
    
    arule.setRuleId(rule_id);
    storeFilter(rule_id, arule.getFilter());    
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#delete(int)
   */
  @Override
  public void delete(int rule_id) {
    template.update("DELETE FROM beast_acrr_rules WHERE rule_id=?", new Object[]{rule_id});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#createRule()
   */
  @Override
  public AcrrRule createRule() {
    AcrrRule result = new AcrrRule();
    result.setCatalog(catalog);
    result.setRuleUtilities(ruleUtilities);
    return result;
  }

  /**
   * @return the acrr rule mapper to use for creating a rule from the database result.
   */
  protected RowMapper<AcrrRule> getAcrrRuleMapper() {
    return new RowMapper<AcrrRule>() {
      @Override
      public AcrrRule mapRow(ResultSet rs, int rnum)
          throws SQLException {
        AcrrRule result = createRule();
        int rule_id = rs.getInt("rule_id");
        result.setRuleId(rule_id);
        result.setArea(rs.getString("area"));
        result.setDistancefield(rs.getString("distancefield"));
        result.setFilesPerHour(rs.getInt("files_per_hour"));
        result.setHours(rs.getInt("hours"));
        result.setAcceptableLoss(rs.getInt("acceptable_loss"));
        result.setObjectType(rs.getString("object_type"));
        result.setQuantity(rs.getString("quantity"));
        result.setZrA(rs.getDouble("zra"));
        result.setZrB(rs.getDouble("zrb"));
        result.setApplyGRA(rs.getBoolean("applygra"));
        return result;
      }
    };
  }
  
  /**
   * Stores the associated filter
   * @param rule_id the rule_id
   * @param filter the filter to store
   */
  protected void storeFilter(int rule_id, IFilter filter) {
    if (filter != null) {
      filterManager.updateFilters(rule_id, createMatchFilter(rule_id, filter));
    } else {
      filterManager.deleteFilters(rule_id);
    }
  }

  /**
   * Creates a match filter
   * @param rule_id the rule id
   * @param filter the filter
   * @return a map with match as filter
   */
  protected Map<String, IFilter> createMatchFilter(int rule_id, IFilter filter) {
    Map<String, IFilter> filters = new HashMap<String, IFilter>();
    filters.put("match", filter);
    return filters;
  }
  
  /**
   * Loads the filter for the rule
   * @param rule_id the rule
   * @return the filter if any otherwise null
   */
  protected IFilter loadFilter(int rule_id) {
    IFilter result = null;
    Map<String, IFilter> filters = filterManager.loadFilters(rule_id);
    if (filters.containsKey("match")) {
      result = filters.get("match");
    }
    return result;
  }
  
  /**
   * @return the filter manager
   */
  public RuleFilterManager getFilterManager() {
    return filterManager;
  }

  /**
   * @param filterManager the filter manager
   */
  public void setFilterManager(RuleFilterManager filterManager) {
    this.filterManager = filterManager;
  }
}
