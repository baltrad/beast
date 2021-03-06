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
package eu.baltrad.beast.rules.volume;

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
 * Manager class for managing the volume generation
 * 
 * @author Anders Henja
 */
public class VolumeRuleManager implements IRuleManager {
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
   * @param template
   *          the jdbc template to set
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
    template.update("delete from beast_volume_rules where rule_id=?",
        new Object[] { ruleId });
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#load(int)
   */
  @Override
  public IRule load(int ruleId) {
    VolumeRule rule = template.queryForObject(
        "select * from beast_volume_rules where rule_id=?",
        getVolumeRuleMapper(), new Object[] { ruleId });
    rule.setFilter(loadFilter(ruleId));
    return rule;
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#store(int,
   *      eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void store(int ruleId, IRule rule) {
    VolumeRule vrule = (VolumeRule)rule;
    int interval = vrule.getInterval();
    int timeout = vrule.getTimeout();
    boolean nominal_timeout = vrule.isNominalTimeout();
    boolean ascending = vrule.isAscending();
    double minelev = vrule.getElevationMin();
    double maxelev = vrule.getElevationMax();
    String elangles = vrule.getElevationAngles();
    boolean adaptive_elangles = vrule.isAdaptiveElevationAngles();
    int qc_mode = vrule.getQualityControlMode();
    List<String> sources = vrule.getSources();
    List<String> detectors = vrule.getDetectors();
    
    template.update("insert into beast_volume_rules" +
                    " (rule_id, interval, timeout, nominal_timeout, ascending, minelev, maxelev, elangles, adaptive_elangles, qc_mode) values" +
                    " (?,?,?,?,?,?,?,?,?,?)", 
                    new Object[]{ruleId, interval, timeout, nominal_timeout, ascending, minelev, maxelev, elangles, adaptive_elangles,  qc_mode});
    
    storeSources(ruleId, sources);
    storeDetectors(ruleId, detectors);
    storeFilter(ruleId, vrule.getFilter());
    vrule.setRuleId(ruleId);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#update(int,
   *      eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void update(int ruleId, IRule rule) {
    VolumeRule vrule = (VolumeRule)rule;
    int interval = vrule.getInterval();
    int timeout = vrule.getTimeout();
    boolean nominal_timeout = vrule.isNominalTimeout();
    boolean ascending = vrule.isAscending();
    double minelev = vrule.getElevationMin();
    double maxelev = vrule.getElevationMax();
    String elangles = vrule.getElevationAngles();
    boolean adaptive_elangles = vrule.isAdaptiveElevationAngles();    
    int qc_mode = vrule.getQualityControlMode();
    List<String> sources = vrule.getSources();
    List<String> detectors = vrule.getDetectors();
    
    template.update("update beast_volume_rules set" +
                    " interval=?, timeout=?, nominal_timeout=?, ascending=?, minelev=?, maxelev=?, elangles=?, adaptive_elangles=?, qc_mode=? where rule_id=?", 
                    new Object[]{interval, timeout, nominal_timeout, ascending, minelev, maxelev, elangles, adaptive_elangles, qc_mode, ruleId});
    
    storeSources(ruleId, sources);
    storeDetectors(ruleId, detectors);
    storeFilter(ruleId, vrule.getFilter());
    vrule.setRuleId(ruleId);    
  }

  /**
   * Stores the sources for the rule
   * @param rule_id the rule_id
   * @param sources the sources to store
   */
  protected void storeSources(int rule_id, List<String> sources) {
    template.update("delete from beast_volume_sources where rule_id=?",
        new Object[]{rule_id});
    if (sources != null) {
      for (String src : sources) {
        template.update("insert into beast_volume_sources (rule_id, source)"+
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
        "select source from beast_volume_sources where rule_id=?",
        getSourceMapper(),
        new Object[]{rule_id});
  }
  
  /**
   * Stores the detectors for this volume rule
   * @param rule_id the rule id these detectors should belong to
   * @param detectors the detectors
   */
  protected void storeDetectors(int rule_id, List<String> detectors) {
    template.update("delete from beast_volume_detectors where rule_id=?",
        new Object[]{rule_id});
    if (detectors != null) {
      for (String src : detectors) {
        template.update("insert into beast_volume_detectors (rule_id, name)"+
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
        "select name from beast_volume_detectors where rule_id=?",
        getDetectorMapper(),
        new Object[]{rule_id});
  }
  
  /**
   * @return the VolumeRule mapper
   */
  protected RowMapper<VolumeRule> getVolumeRuleMapper() {
    return new RowMapper<VolumeRule>() {
      @Override
      public VolumeRule mapRow(ResultSet rs, int rnum) throws SQLException {
        VolumeRule result = createRule();
        int rule_id = rs.getInt("rule_id");
        int interval = rs.getInt("interval");
        int timeout = rs.getInt("timeout");
        boolean nominal_timeout = rs.getBoolean("nominal_timeout");
        boolean ascending = rs.getBoolean("ascending");
        double mine = rs.getDouble("minelev");
        double maxe = rs.getDouble("maxelev");
        String elangles = rs.getString("elangles");
        boolean adaptive_elangles = rs.getBoolean("adaptive_elangles");
        
        int qc_mode = rs.getInt("qc_mode");
        List<String> sources = getSources(rule_id);
        List<String> detectors = getDetectors(rule_id);
        result.setRuleId(rule_id);
        result.setInterval(interval);
        result.setTimeout(timeout);
        result.setNominalTimeout(nominal_timeout);
        result.setAscending(ascending);
        result.setElevationMin(mine);
        result.setElevationMax(maxe);
        result.setElevationAngles(elangles);
        result.setAdaptiveElevationAngles(adaptive_elangles);
        result.setQualityControlMode(qc_mode);
        result.setSources(sources);
        result.setDetectors(detectors);
        return result;
      }
    };
  }
  
  /**
   * @return the source mapper
   */
  protected RowMapper<String> getSourceMapper() { 
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
  public VolumeRule createRule() {
    VolumeRule result = new VolumeRule();
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
