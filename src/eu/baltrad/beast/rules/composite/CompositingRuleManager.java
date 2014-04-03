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
import java.util.List;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleManager;
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
  private SimpleJdbcOperations template = null;
  
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
   * @param template the jdbc template to set
   */
  public void setJdbcTemplate(SimpleJdbcOperations template) {
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
    template.update("delete from beast_composite_rules where rule_id=?",
        new Object[]{ruleId});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#load(int)
   */
  @Override
  public IRule load(int ruleId) {
    return template.queryForObject(
        "select * from beast_composite_rules where rule_id=?",
        getCompsiteRuleMapper(),
        new Object[]{ruleId});
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
    boolean applygra = crule.isApplyGRA();
    double ZR_A = crule.getZR_A();
    double ZR_b = crule.getZR_b();
    
    template.update(
        "insert into beast_composite_rules (rule_id, area, interval, timeout, byscan, selection_method, method, prodpar, applygra, ZR_A, ZR_b)"+
        " values (?,?,?,?,?,?,?,?,?,?,?)", new Object[]{ruleId, area, interval, timeout, byscan, selection_method, method, prodpar, applygra, ZR_A, ZR_b});
    storeSources(ruleId, crule.getSources());
    storeDetectors(ruleId, crule.getDetectors());
    crule.setRuleId(ruleId);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#update(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void update(int ruleId, IRule rule) {
    CompositingRule crule = (CompositingRule)rule;
    template.update(
        "update beast_composite_rules set area=?, interval=?, timeout=?, byscan=?, selection_method=?, method=?, prodpar=?, applygra=?, ZR_A=?, ZR_b=? where rule_id=?",
        new Object[]{crule.getArea(), crule.getInterval(), crule.getTimeout(), crule.isScanBased(), crule.getSelectionMethod(), crule.getMethod(), crule.getProdpar(), crule.isApplyGRA(), crule.getZR_A(), crule.getZR_b(), ruleId});
    storeSources(ruleId, crule.getSources());
    storeDetectors(ruleId, crule.getDetectors());
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
  protected ParameterizedRowMapper<CompositingRule> getCompsiteRuleMapper() {
    return new ParameterizedRowMapper<CompositingRule>() {
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
        result.setApplyGRA(rs.getBoolean("applygra"));
        result.setZR_A(rs.getDouble("ZR_A"));
        result.setZR_b(rs.getDouble("ZR_b"));
        result.setSources(getSources(rule_id));
        result.setDetectors(getDetectors(rule_id));
        return result;
      }
    };
  }
  
  /**
   * @return the source mapper
   */
  protected  ParameterizedRowMapper<String> getSourceMapper() { 
    return new ParameterizedRowMapper<String>() {
      public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString("source");
      }
    };
  }

  /**
   * @return the detector mapper
   */
  protected  ParameterizedRowMapper<String> getDetectorMapper() { 
    return new ParameterizedRowMapper<String>() {
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
}
