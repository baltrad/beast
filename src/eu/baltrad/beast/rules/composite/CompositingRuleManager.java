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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleManager;

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
   * The logger
   */
  private static Logger logger = LogManager.getLogger(CompositingRuleManager.class);
  
  /**
   * @param template the jdbc template to set
   */
  public void setJdbcTemplate(SimpleJdbcOperations template) {
    this.template = template;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRuleManager#delete(int)
   */
  @Override
  public void delete(int ruleId) {
    logger.debug("delete("+ruleId+")");
    storeSources(ruleId, null);
    template.update("delete from beast_composite_rules where rule_id=?",
        new Object[]{ruleId});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#load(int)
   */
  @Override
  public IRule load(int ruleId) {
    logger.debug("load("+ruleId+")");
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
    logger.debug("store("+ruleId+", IRule)");

    CompositingRule crule = (CompositingRule)rule;
    String area = crule.getArea();
    int interval = crule.getInterval();
    int timeout = crule.getTimeout();
    boolean byscan = crule.isScanBased();
    
    template.update(
        "insert into beast_composite_rules (rule_id, area, interval, timeout, byscan)"+
        " values (?,?,?,?,?)", new Object[]{ruleId, area, interval, timeout,byscan});
    storeSources(ruleId, crule.getSources());
    crule.setRuleId(ruleId);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#update(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void update(int ruleId, IRule rule) {
    logger.debug("update("+ruleId+", IRule)");

    CompositingRule crule = (CompositingRule)rule;
    template.update(
        "update beast_composite_rules set area=?, interval=?, timeout=?, byscan=? where rule_id=?",
        new Object[]{crule.getArea(), crule.getInterval(), crule.getTimeout(), crule.isScanBased(), ruleId});
    storeSources(ruleId, crule.getSources());
    crule.setRuleId(ruleId);
  }
  
  /**
   * Stores the sources. The previous sources will be removed before
   * setting the new ones.
   * @param rule_id
   * @param sources
   */
  protected void storeSources(int rule_id, List<String> sources) {
    logger.debug("storeSources("+rule_id+", List<String>)");
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
    logger.debug("getSources("+rule_id+")");
    return template.query(
        "select source from beast_composite_sources where rule_id=?",
        getSourceMapper(),
        new Object[]{rule_id});
  }
  
  /**
   * @return the CompositingRule mapper
   */
  protected ParameterizedRowMapper<CompositingRule> getCompsiteRuleMapper() {
    logger.debug("getCompsiteRuleMapper()");
    return new ParameterizedRowMapper<CompositingRule>() {
      @Override
      public CompositingRule mapRow(ResultSet rs, int rnum)
          throws SQLException {
        CompositingRule result = new CompositingRule();
        int rule_id = rs.getInt("rule_id");
        result.setRuleId(rule_id);
        result.setArea(rs.getString("area"));
        result.setInterval(rs.getInt("interval"));
        result.setTimeout(rs.getInt("timeout"));
        result.setScanBased(rs.getBoolean("byscan"));
        result.setSources(getSources(rule_id));
        return result;
      }
    };
  }
  
  /**
   * @return the source mapper
   */
  protected  ParameterizedRowMapper<String> getSourceMapper() { 
    logger.debug("getSourceMapper()");
    return new ParameterizedRowMapper<String>() {
      public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString("source");
      }
    };
  }
}
