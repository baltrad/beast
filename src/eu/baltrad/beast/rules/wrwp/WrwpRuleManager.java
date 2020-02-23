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
 */
public class WrwpRuleManager implements IRuleManager {
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
  public void store(int ruleId, IRule rule) {
    WrwpRule wrule = (WrwpRule)rule;
    int interval = wrule.getInterval();
    int maxheight = wrule.getMaxheight();
    int mindistance = wrule.getMindistance();
    int maxdistance = wrule.getMaxdistance();
    double minelangle = wrule.getMinelevationangle();
    double maxelangle = wrule.getMaxelevationangle();
    double minvelocitythresh = wrule.getMinvelocitythreshold();
    double maxvelocitythresh = wrule.getMaxvelocitythreshold();
    int minsamplesizereflectivity = wrule.getMinsamplesizereflectivity();
    int minsamplesizewind = wrule.getMinsamplesizewind();
    
    String fields = wrule.getFieldsAsStr();
    
    template.update(
        "INSERT INTO beast_wrwp_rules" +
        " (rule_id, interval, maxheight, mindistance, maxdistance, minelangle, maxelangle, minvelocitythresh, maxvelocitythresh, minsamplesizereflectivity, minsamplesizewind, fields)" +
        " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)", 
        new Object[]{ruleId, interval, maxheight, mindistance, maxdistance, minelangle, maxelangle, minvelocitythresh, maxvelocitythresh, minsamplesizereflectivity, minsamplesizewind, fields});
    
    updateSources(ruleId, wrule.getSources());
    
    storeFilter(ruleId, wrule.getFilter());
    
    wrule.setRuleId(ruleId);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#load(int)
   */
  @Override
  public IRule load(int ruleId) {
    WrwpRule rule = template.queryForObject("SELECT * FROM beast_wrwp_rules WHERE rule_id=?", 
        getWrwpRuleMapper(),
        new Object[]{ruleId});
    rule.setFilter(loadFilter(ruleId));
    return rule;
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#update(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void update(int ruleId, IRule rule) {
    WrwpRule wrule = (WrwpRule)rule;
    int interval = wrule.getInterval();
    int maxheight = wrule.getMaxheight();
    int mindistance = wrule.getMindistance();
    int maxdistance = wrule.getMaxdistance();
    double minelangle = wrule.getMinelevationangle();
    double maxelangle = wrule.getMaxelevationangle();
    double minvelocitythresh = wrule.getMinvelocitythreshold();
    double maxvelocitythresh = wrule.getMaxvelocitythreshold();
    int minsamplesizereflectivity = wrule.getMinsamplesizereflectivity();
    int minsamplesizewind = wrule.getMinsamplesizewind();
    String fields = wrule.getFieldsAsStr();
    
    template.update(
        "UPDATE beast_wrwp_rules SET interval=?, maxheight=?, mindistance=?," +
        " maxdistance=?, minelangle=?, maxelangle=?, minvelocitythresh=?, maxvelocitythresh=?, minsamplesizereflectivity=?, minsamplesizewind=?, fields=? WHERE rule_id=?",
        new Object[]{interval, maxheight, mindistance, maxdistance, minelangle, maxelangle, minvelocitythresh, maxvelocitythresh, minsamplesizereflectivity, minsamplesizewind, fields, ruleId});
    
    updateSources(ruleId, wrule.getSources());
    
    storeFilter(ruleId, wrule.getFilter());
    
    wrule.setRuleId(ruleId);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#delete(int)
   */
  @Override
  public void delete(int ruleId) {
    updateSources(ruleId, null);
    storeFilter(ruleId, null);
    template.update("DELETE FROM beast_wrwp_rules WHERE rule_id=?", new Object[]{ruleId});
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
  protected void updateSources(int ruleId, List<String> sources) {
    template.update("DELETE FROM beast_wrwp_sources WHERE rule_id=?", new Object[]{ruleId});
    if (sources != null) {
      for (String s : sources) {
        template.update("INSERT INTO beast_wrwp_sources (rule_id, source) VALUES (?,?)",
            new Object[]{ruleId, s});
      }
    }
  }
  
  /**
   * Returns the sources associated with this rule
   * @param ruleId the rule id
   * @return the list of sources
   */
  protected List<String> getSources(int ruleId) {
    return template.query("SELECT source FROM beast_wrwp_sources WHERE rule_id=?", 
        getSourceMapper(), 
        new Object[]{ruleId});
  }
  
  /**
   * The mapper when reading a wrwp rule from the database
   * @return the mapper
   */
  protected RowMapper<WrwpRule> getWrwpRuleMapper() {
    return new RowMapper<WrwpRule>() {
      @Override
      public WrwpRule mapRow(ResultSet rs, int ri) throws SQLException {
        WrwpRule result = (WrwpRule)createRule();
        result.setRuleId(rs.getInt("rule_id"));
        result.setInterval(rs.getInt("interval"));
        result.setMaxheight(rs.getInt("maxheight"));
        result.setMindistance(rs.getInt("mindistance"));
        result.setMaxdistance(rs.getInt("maxdistance"));
        result.setMinelevationangle(rs.getDouble("minelangle"));
        result.setMaxelevationangle(rs.getDouble("maxelangle"));
        result.setMinvelocitythreshold(rs.getDouble("minvelocitythresh"));
        result.setMaxvelocitythreshold(rs.getDouble("maxvelocitythresh"));
        result.setMinsamplesizereflectivity(rs.getInt("minsamplesizereflectivity"));
        result.setMinsamplesizewind(rs.getInt("minsamplesizewind"));
        String fields = rs.getString("fields");
        result.setFields(fields == null ? "" : fields);

        result.setSources(getSources(result.getRuleId()));
        return result;
      }
    };
  }
  
  /**
   * @return the mapper when reading the wrwp sources from the database
   */
  protected RowMapper<String> getSourceMapper() {
    return new RowMapper<String>() {
      @Override
      public String mapRow(ResultSet rs, int arg1) throws SQLException {
        return rs.getString("source");
      }
    };
  }
  
  /**
   * Stores the associated filter
   * @param ruleId the ruleId
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
