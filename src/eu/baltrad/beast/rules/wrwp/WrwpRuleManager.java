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

package eu.baltrad.beast.rules.wrwp;

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
 */
public class WrwpRuleManager implements IRuleManager {
  /**
   * The jdbc template
   */
  private SimpleJdbcOperations template = null;
  
  /**
   * The catalog
   */
  private Catalog catalog = null;
  
  /**
   * The rule utilities
   */
  private IRuleUtilities ruleUtilities = null;
  
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
   * @see eu.baltrad.beast.rules.IRuleManager#store(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void store(int rule_id, IRule rule) {
    WrwpRule wrule = (WrwpRule)rule;
    int interval = wrule.getInterval();
    int maxheight = wrule.getMaxheight();
    int mindistance = wrule.getMindistance();
    int maxdistance = wrule.getMaxdistance();
    double minelangle = wrule.getMinelevationangle();
    double minvelocitythresh = wrule.getMinvelocitythreshold();
    
    template.update(
        "INSERT INTO beast_wrwp_rules" +
        " (rule_id, interval, maxheight, mindistance, maxdistance, minelangle, minvelocitythresh)" +
        " VALUES (?,?,?,?,?,?,?)", 
        new Object[]{rule_id, interval, maxheight, mindistance, maxdistance, minelangle, minvelocitythresh});
    
    updateSources(rule_id, wrule.getSources());
    
    wrule.setRuleId(rule_id);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#load(int)
   */
  @Override
  public IRule load(int rule_id) {
    return template.queryForObject("SELECT * FROM beast_wrwp_rules WHERE rule_id=?", 
        getWrwpRuleMapper(),
        new Object[]{rule_id});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#update(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void update(int rule_id, IRule rule) {
    WrwpRule wrule = (WrwpRule)rule;
    int interval = wrule.getInterval();
    int maxheight = wrule.getMaxheight();
    int mindistance = wrule.getMindistance();
    int maxdistance = wrule.getMaxdistance();
    double minelangle = wrule.getMinelevationangle();
    double minvelocitythresh = wrule.getMinvelocitythreshold();
    
    template.update(
        "UPDATE beast_wrwp_rules SET interval=?, maxheight=?, mindistance=?," +
        " maxdistance=?, minelangle=?, minvelocitythresh=? WHERE rule_id=?",
        new Object[]{interval, maxheight, mindistance, maxdistance, minelangle, minvelocitythresh, rule_id});
    
    updateSources(rule_id, wrule.getSources());
    
    wrule.setRuleId(rule_id);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#delete(int)
   */
  @Override
  public void delete(int rule_id) {
    updateSources(rule_id, null);
    template.update("DELETE FROM beast_wrwp_rules WHERE rule_id=?", new Object[]{rule_id});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#createRule()
   */
  @Override
  public IRule createRule() {
    WrwpRule result = new WrwpRule();
    result.setCatalog(catalog);
    result.setRuleUtilities(ruleUtilities);
    return result;
  }

  /**
   * Updates the sources
   * @param sources the sources that should be added for the specific rule
   */
  protected void updateSources(int rule_id, List<String> sources) {
    template.update("DELETE FROM beast_wrwp_sources WHERE rule_id=?", new Object[]{rule_id});
    if (sources != null) {
      for (String s : sources) {
        template.update("INSERT INTO beast_wrwp_sources (rule_id, source) VALUES (?,?)",
            new Object[]{rule_id, s});
      }
    }
  }
  
  /**
   * Returns the sources associated with this rule
   * @param rule_id the rule id
   * @return the list of sources
   */
  protected List<String> getSources(int rule_id) {
    return template.query("SELECT source FROM beast_wrwp_sources WHERE rule_id=?", 
        getSourceMapper(), 
        new Object[]{rule_id});
  }
  
  /**
   * The mapper when reading a wrwp rule from the database
   * @return the mapper
   */
  protected ParameterizedRowMapper<WrwpRule> getWrwpRuleMapper() {
    return new ParameterizedRowMapper<WrwpRule>() {
      @Override
      public WrwpRule mapRow(ResultSet rs, int ri) throws SQLException {
        WrwpRule result = (WrwpRule)createRule();
        result.setRuleId(rs.getInt("rule_id"));
        result.setInterval(rs.getInt("interval"));
        result.setMaxheight(rs.getInt("maxheight"));
        result.setMindistance(rs.getInt("mindistance"));
        result.setMaxdistance(rs.getInt("maxdistance"));
        result.setMinelevationangle(rs.getDouble("minelangle"));
        result.setMinvelocitythreshold(rs.getDouble("minvelocitythresh"));
        result.setSources(getSources(result.getRuleId()));
        return result;
      }
    };
  }
  
  /**
   * @return the mapper when reading the wrwp sources from the database
   */
  protected ParameterizedRowMapper<String> getSourceMapper() {
    return new ParameterizedRowMapper<String>() {
      @Override
      public String mapRow(ResultSet rs, int arg1) throws SQLException {
        return rs.getString("source");
      }
    };
  }
}
