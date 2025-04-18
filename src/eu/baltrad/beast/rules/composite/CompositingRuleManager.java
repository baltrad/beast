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
package eu.baltrad.beast.rules.composite;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleManager;
import eu.baltrad.beast.rules.RuleFilterManager;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class CompositingRuleManager implements IRuleManager {
  /**
   * The jdbc template
   */
  private JdbcOperations template = null;
  
  /**
   * The rule utilities
   */
  private IRuleUtilities ruleUtilities = null;
  
  /**
   * The catalog
   */
  private Catalog catalog = null;
  
  /**
   * The timeout manager
   */
  private TimeoutManager timeoutManager = null;
  
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
   * @param manager the timeout manager to set
   */
  public void setTimeoutManager(TimeoutManager manager) {
    this.timeoutManager = manager;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRuleManager#delete(int)
   */
  @Override
  public void delete(int ruleId) {
    storeSources(ruleId, null);
    storeDetectors(ruleId, null);
    storeFilter(ruleId, null);
    template.update("delete from beast_composite_rules where rule_id=?",
        new Object[]{ruleId});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#load(int)
   */
  @Override
  public IRule load(int ruleId) {
    CompositingRule rule = template.queryForObject(
        "select * from beast_composite_rules where rule_id=?",
        getCompsiteRuleMapper(),
        new Object[]{ruleId});
    rule.setFilter(loadFilter(ruleId));
    return rule;
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#store(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void store(int ruleId, IRule rule) {
    CompositingRule crule = (CompositingRule)rule;
    String area = crule.getArea();
    int interval = crule.getInterval();
    int timeout = crule.getTimeout();
    boolean byscan = crule.isScanBased();
    int selection_method = crule.getSelectionMethod();
    String method = crule.getMethod();
    String prodpar = crule.getProdpar();
    int maxAgeLimit = crule.getMaxAgeLimit();
    boolean applygra = crule.isApplyGRA();
    double ZR_A = crule.getZR_A();
    double ZR_b = crule.getZR_b();
    boolean ignoreMalfunc = crule.isIgnoreMalfunc();
    boolean ctfilter = crule.isCtFilter();
    String qitotalField = crule.getQitotalField();
    String quantity = crule.getQuantity();
    String options = crule.getOptions();
    boolean nominal_timeout = crule.isNominalTimeout();
    int qualityControlMode = crule.getQualityControlMode();
    boolean reprocess_quality = crule.isReprocessQuality();
    
    template.update(
        "insert into beast_composite_rules (rule_id, area, interval, timeout, byscan, selection_method, method, prodpar, max_age_limit, applygra, ZR_A, ZR_b, ignore_malfunc, ctfilter, qitotal_field, quantity, options, nominal_timeout, qc_mode, reprocess_quality)"+
        " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new Object[]{ruleId, area, interval, timeout, byscan, selection_method, method, prodpar, maxAgeLimit, applygra, ZR_A, ZR_b, ignoreMalfunc, ctfilter, qitotalField, quantity, options, nominal_timeout, qualityControlMode, reprocess_quality});
    storeSources(ruleId, crule.getSources());
    storeDetectors(ruleId, crule.getDetectors());
    storeFilter(ruleId, crule.getFilter());
    crule.setRuleId(ruleId);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#update(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void update(int ruleId, IRule rule) {
    CompositingRule crule = (CompositingRule)rule;
    template.update(
        "update beast_composite_rules set area=?, interval=?, timeout=?, byscan=?, selection_method=?, method=?, prodpar=?, max_age_limit=?, applygra=?, ZR_A=?, ZR_b=?, ignore_malfunc=?, ctfilter=?, qitotal_field=?, quantity=?, options=?, nominal_timeout=?, qc_mode=?, reprocess_quality=? where rule_id=?",
        new Object[]{crule.getArea(), crule.getInterval(), crule.getTimeout(), crule.isScanBased(), crule.getSelectionMethod(), crule.getMethod(), crule.getProdpar(), crule.getMaxAgeLimit(), crule.isApplyGRA(), crule.getZR_A(), crule.getZR_b(), crule.isIgnoreMalfunc(), crule.isCtFilter(), crule.getQitotalField(), crule.getQuantity(), crule.getOptions(), crule.isNominalTimeout(), crule.getQualityControlMode(), crule.isReprocessQuality(), ruleId});
    storeSources(ruleId, crule.getSources());
    storeDetectors(ruleId, crule.getDetectors());
    storeFilter(ruleId, crule.getFilter());
    crule.setRuleId(ruleId);
  }
  
  /**
   * Stores the sources. The previous sources will be removed before
   * setting the new ones.
   * @param rule_id
   * @param sources
   */
  protected void storeSources(int rule_id, List<String> sources) {
    template.update("delete from beast_composite_sources where rule_id=?",
        new Object[]{rule_id});
    if (sources != null) {
      for (String src : sources) {
        template.update("insert into beast_composite_sources (rule_id, source)"+
            " values (?,?)", new Object[]{rule_id, src});
      }
    }
  }
  
  /**
   * Returns a list of sources connected to the rule_id
   * @param rule_id the rule id
   * @return a list of sources
   */
  protected List<String> getSources(int rule_id) {
    return template.query(
        "select source from beast_composite_sources where rule_id=?",
        getSourceMapper(),
        new Object[]{rule_id});
  }
  
  /**
   * Stores the detectors for this compositing rule
   * @param rule_id the rule id these detectors should belong to
   * @param detectors the detectors
   */
  protected void storeDetectors(int rule_id, List<String> detectors) {
    template.update("delete from beast_composite_detectors where rule_id=?",
        new Object[]{rule_id});
    if (detectors != null) {
      for (String src : detectors) {
        template.update("insert into beast_composite_detectors (rule_id, name)"+
            " values (?,?)", new Object[]{rule_id, src});
      }
    }    
  }
  
  /**
   * Stores the associated filter
   * @param rule_id the rule_id
   * @param filter the filter to store
   */
  protected void storeFilter(int rule_id, IFilter filter) {
    if (filter != null) {
      Map<String, IFilter> filters = new HashMap<String, IFilter>();
      filters.put("match", filter);
      filterManager.storeFilters(rule_id, filters);
    } else {
      filterManager.deleteFilters(rule_id);
    }
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
   * Returns a list of sources connected to the rule_id
   * @param rule_id the rule id
   * @return a list of sources
   */
  protected List<String> getDetectors(int rule_id) {
    return template.query(
        "select name from beast_composite_detectors where rule_id=?",
        getDetectorMapper(),
        new Object[]{rule_id});
  }
  
  /**
   * @return the CompositingRule mapper
   */
  protected RowMapper<CompositingRule> getCompsiteRuleMapper() {
    return new RowMapper<CompositingRule>() {
      @Override
      public CompositingRule mapRow(ResultSet rs, int rnum)
          throws SQLException {
        CompositingRule result = createRule();
        int rule_id = rs.getInt("rule_id");
        result.setRuleId(rule_id);
        result.setArea(rs.getString("area"));
        result.setInterval(rs.getInt("interval"));
        result.setTimeout(rs.getInt("timeout"));
        result.setScanBased(rs.getBoolean("byscan"));
        result.setSelectionMethod(rs.getInt("selection_method"));
        result.setMethod(rs.getString("method"));
        result.setProdpar(rs.getString("prodpar"));
        result.setMaxAgeLimit(rs.getInt("max_age_limit"));
        result.setApplyGRA(rs.getBoolean("applygra"));
        result.setZR_A(rs.getDouble("ZR_A"));
        result.setZR_b(rs.getDouble("ZR_b"));
        result.setIgnoreMalfunc(rs.getBoolean("ignore_malfunc"));
        result.setCtFilter(rs.getBoolean("ctfilter"));
        result.setQitotalField(rs.getString("qitotal_field"));
        result.setQuantity(rs.getString("quantity"));
        result.setOptions(rs.getString("options"));
        result.setNominalTimeout(rs.getBoolean("nominal_timeout"));
        result.setQualityControlMode(rs.getInt("qc_mode"));
        result.setReprocessQuality(rs.getBoolean("reprocess_quality"));
        result.setSources(getSources(rule_id));
        result.setDetectors(getDetectors(rule_id));
        return result;
      }
    };
  }
  
  /**
   * @return the source mapper
   */
  protected  RowMapper<String> getSourceMapper() { 
    return new RowMapper<String>() {
      public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString("source");
      }
    };
  }

  /**
   * @return the detector mapper
   */
  protected  RowMapper<String> getDetectorMapper() { 
    return new RowMapper<String>() {
      public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString("name");
      }
    };
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRuleManager#createRule()
   */
  @Override
  public CompositingRule createRule() {
    CompositingRule result = new CompositingRule();
    result.setCatalog(catalog);
    result.setRuleUtilities(ruleUtilities);
    result.setTimeoutManager(timeoutManager);
    result.afterPropertiesSet();
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
