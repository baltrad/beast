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

package eu.baltrad.beast.rules.site2d;

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
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class Site2DRuleManager implements IRuleManager {
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
   * filter manager
   */
  private RuleFilterManager filterManager;
  
  /**
   * @see eu.baltrad.beast.rules.IRuleManager#store(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void store(int ruleId, IRule rule) {
    Site2DRule srule = (Site2DRule)rule;
    String area = srule.getArea();
    int interval = srule.getInterval();
    List<String> detectors = srule.getDetectors();
    List<String> sources = srule.getSources();
    String method = srule.getMethod();
    String prodpar = srule.getProdpar();
    boolean applygra = srule.isApplyGRA();
    boolean ctfilter = srule.isCtFilter();
    boolean ignoremalfunc = srule.isIgnoreMalfunc();
    boolean byscan = srule.isScanBased();
    double zrA = srule.getZR_A();
    double zrb = srule.getZR_b();
    String pcsid = srule.getPcsid();
    double xscale = srule.getXscale();
    double yscale = srule.getYscale();
    String options = srule.getOptions();
    int qualityControlMode = srule.getQualityControlMode();
    
    template.update(
        "INSERT INTO beast_site2d_rules (rule_id, area, interval, byscan, method, prodpar, applygra, ZR_A, ZR_b, ignore_malfunc, ctfilter, pcsid, xscale, yscale, options, qc_mode)"+
        " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new Object[]{ruleId, area, interval, byscan, method, prodpar, applygra, zrA, zrb, ignoremalfunc, ctfilter, pcsid, xscale, yscale, options, qualityControlMode});
    
    storeSources(ruleId, sources);
    storeDetectors(ruleId, detectors);
    storeFilter(ruleId, srule.getFilter());
    srule.setRuleId(ruleId);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#load(int)
   */
  @Override
  public IRule load(int ruleId) {
    Site2DRule rule = template.queryForObject(
        "select * from beast_site2d_rules where rule_id=?",
        getSite2DRuleMapper(),
        new Object[]{ruleId});
    
    rule.setFilter(loadFilter(ruleId));
    return rule;
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#update(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void update(int ruleId, IRule rule) {
    Site2DRule srule = (Site2DRule)rule;
    String area = srule.getArea();
    int interval = srule.getInterval();
    List<String> detectors = srule.getDetectors();
    List<String> sources = srule.getSources();
    String method = srule.getMethod();
    String prodpar = srule.getProdpar();
    boolean applygra = srule.isApplyGRA();
    boolean ctfilter = srule.isCtFilter();
    boolean ignoremalfunc = srule.isIgnoreMalfunc();
    boolean byscan = srule.isScanBased();
    double zrA = srule.getZR_A();
    double zrb = srule.getZR_b();
    String pcsid = srule.getPcsid();
    double xscale = srule.getXscale();
    double yscale = srule.getYscale();
    String options = srule.getOptions();
    int qualityControlMode = srule.getQualityControlMode();
    
    template.update("UPDATE beast_site2d_rules" +
        " SET area=?, interval=?, byscan=?, method=?, prodpar=?, applygra=?, ZR_A=?, ZR_b=?, ignore_malfunc=?, ctfilter=?, pcsid=?, xscale=?, yscale=?, options=?, qc_mode=?" +
        " WHERE rule_id=?", new Object[]{area, interval, byscan, method, prodpar, applygra, zrA, zrb, ignoremalfunc, ctfilter, pcsid, xscale, yscale, options, qualityControlMode, ruleId});
    storeSources(ruleId, sources);
    storeDetectors(ruleId, detectors);
    storeFilter(ruleId, srule.getFilter());
    srule.setRuleId(ruleId);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#delete(int)
   */
  @Override
  public void delete(int ruleId) {
    storeSources(ruleId, null);
    storeDetectors(ruleId, null);
    storeFilter(ruleId, null);
    template.update("delete from beast_site2d_rules where rule_id=?",
        new Object[]{ruleId});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#createRule()
   */
  @Override
  public Site2DRule createRule() {
    Site2DRule rule = new Site2DRule();
    rule.setCatalog(catalog);
    rule.setRuleUtilities(ruleUtilities);
    rule.afterPropertiesSet();
    return rule;
  }

  public JdbcOperations getTemplate() {
    return template;
  }

  public void setTemplate(JdbcOperations template) {
    this.template = template;
  }

  public IRuleUtilities getRuleUtilities() {
    return ruleUtilities;
  }

  public void setRuleUtilities(IRuleUtilities ruleUtilities) {
    this.ruleUtilities = ruleUtilities;
  }

  public Catalog getCatalog() {
    return catalog;
  }

  public void setCatalog(Catalog catalog) {
    this.catalog = catalog;
  }
  
  /**
   * Stores the sources. The previous sources will be removed before
   * setting the new ones.
   * @param ruleId
   * @param sources
   */
  protected void storeSources(int ruleId, List<String> sources) {
    template.update("delete from beast_site2d_sources where rule_id=?",
        new Object[]{ruleId});
    if (sources != null) {
      for (String src : sources) {
        template.update("insert into beast_site2d_sources (rule_id, source)"+
            " values (?,?)", new Object[]{ruleId, src});
      }
    }
  }
  
  /**
   * Returns a list of sources connected to the rule_id
   * @param ruleId the rule id
   * @return a list of sources
   */
  protected List<String> getSources(int ruleId) {
    return template.query(
        "select source from beast_site2d_sources where rule_id=?",
        getSourceMapper(),
        new Object[]{ruleId});
  }
  
  /**
   * Stores the detectors for this compositing rule
   * @param ruleId the rule id these detectors should belong to
   * @param detectors the detectors
   */
  protected void storeDetectors(int ruleId, List<String> detectors) {
    template.update("delete from beast_site2d_detectors where rule_id=?",
        new Object[]{ruleId});
    if (detectors != null) {
      for (String src : detectors) {
        template.update("insert into beast_site2d_detectors (rule_id, name)"+
            " values (?,?)", new Object[]{ruleId, src});
      }
    }    
  }
  
  /**
   * Stores the associated filter
   * @param ruleId the rule_id
   * @param filter the filter to store
   */
  protected void storeFilter(int ruleId, IFilter filter) {
    if (filter != null) {
      Map<String, IFilter> filters = new HashMap<String, IFilter>();
      filters.put("match", filter);
      filterManager.storeFilters(ruleId, filters);
    } else {
      filterManager.deleteFilters(ruleId);
    }
  }
  
  /**
   * Loads the filter for the rule
   * @param ruleId the rule
   * @return the filter if any otherwise null
   */
  protected IFilter loadFilter(int ruleId) {
    IFilter result = null;
    Map<String, IFilter> filters = filterManager.loadFilters(ruleId);
    if (filters.containsKey("match")) {
      result = filters.get("match");
    }
    return result;
  }
 
  /**
   * Returns a list of sources connected to the rule_id
   * @param ruleId the rule id
   * @return a list of sources
   */
  protected List<String> getDetectors(int ruleId) {
    return template.query(
        "select name from beast_site2d_detectors where rule_id=?",
        getDetectorMapper(),
        new Object[]{ruleId});
  }
  
  /**
   * @return the CompositingRule mapper
   */
  protected RowMapper<Site2DRule> getSite2DRuleMapper() {
    return new RowMapper<Site2DRule>() {
      @Override
      public Site2DRule mapRow(ResultSet rs, int rnum)
          throws SQLException {
        Site2DRule result = createRule();
        int ruleId = rs.getInt("rule_id");
        result.setRuleId(ruleId);
        result.setArea(rs.getString("area"));
        result.setInterval(rs.getInt("interval"));
        result.setScanBased(rs.getBoolean("byscan"));
        result.setMethod(rs.getString("method"));
        result.setProdpar(rs.getString("prodpar"));
        result.setApplyGRA(rs.getBoolean("applygra"));
        result.setZR_A(rs.getDouble("ZR_A"));
        result.setZR_b(rs.getDouble("ZR_b"));
        result.setIgnoreMalfunc(rs.getBoolean("ignore_malfunc"));
        result.setCtFilter(rs.getBoolean("ctfilter"));
        result.setPcsid(rs.getString("pcsid"));
        result.setXscale(rs.getDouble("xscale"));
        result.setYscale(rs.getDouble("yscale"));
        result.setOptions(rs.getString("options"));
        result.setQualityControlMode(rs.getInt("qc_mode"));
        result.setSources(getSources(ruleId));
        result.setDetectors(getDetectors(ruleId));
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
