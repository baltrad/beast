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
import java.util.List;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleManager;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class Site2DRuleManager implements IRuleManager {
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
   * @see eu.baltrad.beast.rules.IRuleManager#store(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void store(int rule_id, IRule rule) {
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
    
    template.update(
        "INSERT INTO beast_site2d_rules (rule_id, area, interval, byscan, method, prodpar, applygra, ZR_A, ZR_b, ignore_malfunc, ctfilter, pcsid, xscale, yscale)"+
        " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new Object[]{rule_id, area, interval, byscan, method, prodpar, applygra, zrA, zrb, ignoremalfunc, ctfilter, pcsid, xscale, yscale});
    
    storeSources(rule_id, sources);
    storeDetectors(rule_id, detectors);
    srule.setRuleId(rule_id);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#load(int)
   */
  @Override
  public IRule load(int rule_id) {
    return template.queryForObject(
        "select * from beast_site2d_rules where rule_id=?",
        getSite2DRuleMapper(),
        new Object[]{rule_id});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#update(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void update(int rule_id, IRule rule) {
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

    template.update("UPDATE beast_site2d_rules" +
        " SET area=?, interval=?, byscan=?, method=?, prodpar=?, applygra=?, ZR_A=?, ZR_b=?, ignore_malfunc=?, ctfilter=?, pcsid=?, xscale=?, yscale=?" +
        " WHERE rule_id=?", new Object[]{area, interval, byscan, method, prodpar, applygra, zrA, zrb, ignoremalfunc, ctfilter, pcsid, xscale, yscale, rule_id});
    storeSources(rule_id, sources);
    storeDetectors(rule_id, detectors);
    srule.setRuleId(rule_id);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#delete(int)
   */
  @Override
  public void delete(int rule_id) {
    storeSources(rule_id, null);
    storeDetectors(rule_id, null);
    template.update("delete from beast_site2d_rules where rule_id=?",
        new Object[]{rule_id});
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

  public SimpleJdbcOperations getTemplate() {
    return template;
  }

  public void setTemplate(SimpleJdbcOperations template) {
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
   * @param rule_id
   * @param sources
   */
  protected void storeSources(int rule_id, List<String> sources) {
    template.update("delete from beast_site2d_sources where rule_id=?",
        new Object[]{rule_id});
    if (sources != null) {
      for (String src : sources) {
        template.update("insert into beast_site2d_sources (rule_id, source)"+
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
        "select source from beast_site2d_sources where rule_id=?",
        getSourceMapper(),
        new Object[]{rule_id});
  }
  
  /**
   * Stores the detectors for this compositing rule
   * @param rule_id the rule id these detectors should belong to
   * @param detectors the detectors
   */
  protected void storeDetectors(int rule_id, List<String> detectors) {
    template.update("delete from beast_site2d_detectors where rule_id=?",
        new Object[]{rule_id});
    if (detectors != null) {
      for (String src : detectors) {
        template.update("insert into beast_site2d_detectors (rule_id, name)"+
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
        "select name from beast_site2d_detectors where rule_id=?",
        getDetectorMapper(),
        new Object[]{rule_id});
  }
  
  /**
   * @return the CompositingRule mapper
   */
  protected ParameterizedRowMapper<Site2DRule> getSite2DRuleMapper() {
    return new ParameterizedRowMapper<Site2DRule>() {
      @Override
      public Site2DRule mapRow(ResultSet rs, int rnum)
          throws SQLException {
        Site2DRule result = createRule();
        int rule_id = rs.getInt("rule_id");
        result.setRuleId(rule_id);
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
}
