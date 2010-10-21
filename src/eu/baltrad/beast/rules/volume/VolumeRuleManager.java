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
import java.util.List;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleManager;

/**
 * Manager class for managing the volume generation
 * 
 * @author Anders Henja
 */
public class VolumeRuleManager implements IRuleManager {
  /**
   * The jdbc template
   */
  private SimpleJdbcOperations template = null;

  /**
   * @param template
   *          the jdbc template to set
   */
  public void setJdbcTemplate(SimpleJdbcOperations template) {
    this.template = template;
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#delete(int)
   */
  @Override
  public void delete(int ruleId) {
    storeSources(ruleId, null);
    template.update("delete from beast_volume_rules where rule_id=?",
        new Object[] { ruleId });
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#load(int)
   */
  @Override
  public IRule load(int ruleId) {
    return template.queryForObject(
        "select * from beast_volume_rules where rule_id=?",
        getVolumeRuleMapper(), new Object[] { ruleId });
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
    boolean ascending = vrule.isAscending();
    double minelev = vrule.getElevationMin();
    double maxelev = vrule.getElevationMax();
    List<String> sources = vrule.getSources();

    template.update("insert into beast_volume_rules" +
                    " (rule_id, interval, timeout, ascending, minelev, maxelev) values" +
                    " (?,?,?,?,?,?)", 
                    new Object[]{ruleId, interval, timeout, ascending, minelev, maxelev});
    
    storeSources(ruleId, sources);
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
    boolean ascending = vrule.isAscending();
    double minelev = vrule.getElevationMin();
    double maxelev = vrule.getElevationMax();
    List<String> sources = vrule.getSources();

    template.update("update beast_volume_rules set" +
                    " interval=?, timeout=?, ascending=?, minelev=?, maxelev=? where rule_id=?", 
                    new Object[]{interval, timeout, ascending, minelev, maxelev, ruleId});
    
    storeSources(ruleId, sources);
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
   * @return the VolumeRule mapper
   */
  protected ParameterizedRowMapper<VolumeRule> getVolumeRuleMapper() {
    return new ParameterizedRowMapper<VolumeRule>() {
      @Override
      public VolumeRule mapRow(ResultSet rs, int rnum) throws SQLException {
        VolumeRule result = new VolumeRule();
        int rule_id = rs.getInt("rule_id");
        int interval = rs.getInt("interval");
        int timeout = rs.getInt("timeout");
        boolean ascending = rs.getBoolean("ascending");
        double mine = rs.getDouble("minelev");
        double maxe = rs.getDouble("maxelev");
        List<String> sources = getSources(rule_id);
        result.setRuleId(rule_id);
        result.setInterval(interval);
        result.setTimeout(timeout);
        result.setAscending(ascending);
        result.setElevationMin(mine);
        result.setElevationMax(maxe);
        result.setSources(sources);
        return result;
      }
    };
  }
  
  /**
   * @return the source mapper
   */
  protected ParameterizedRowMapper<String> getSourceMapper() { 
    return new ParameterizedRowMapper<String>() {
      public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString("source");
      }
    };
  }
}
