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

package eu.baltrad.beast.rules.scansun;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleManager;
import eu.baltrad.beast.rules.RuleException;

/**
 * @author Anders Henja
 *
 */
public class ScansunRuleManager implements IRuleManager {
  /**
   * The simple jdbc template
   */
  private JdbcOperations template = null;
  
  /**
   * @param template the jdbc template to set
   */
  public void setJdbcTemplate(JdbcOperations template) {
    this.template = template;
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#store(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void store(int rule_id, IRule rule) {
    ScansunRule srule = (ScansunRule)rule;
    List<String> sources = srule.getSources();

    if (template.queryForObject("SELECT COUNT(*) FROM beast_scansun_sources WHERE rule_id=?",
        int.class,
        rule_id) != 0) {
      throw new RuleException("sources for scansun rule '" + rule_id + "' already existing");
    }
    
    if (sources != null) {
      for (String src : sources) {
        template.update("INSERT INTO beast_scansun_sources (rule_id, source)"+
            " VALUES (?,?)", new Object[]{rule_id, src});
      }
    }    
    srule.setRuleId(rule_id);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#load(int)
   */
  @Override
  public IRule load(int rule_id) {
    ScansunRule rule = (ScansunRule)createRule();
    rule.setRuleId(rule_id);
    List<String> sources = template.query(
        "SELECT source FROM beast_scansun_sources WHERE rule_id=?",
        getSourceMapper(),
        new Object[]{rule_id});
    rule.setSources(sources);
    return rule;
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#update(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void update(int rule_id, IRule rule) {
    template.update("DELETE FROM beast_scansun_sources WHERE rule_id=?", new Object[]{rule_id});
    store(rule_id, rule);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#delete(int)
   */
  @Override
  public void delete(int rule_id) {
    template.update("DELETE FROM beast_scansun_sources WHERE rule_id=?", new Object[]{rule_id});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#createRule()
   */
  @Override
  public IRule createRule() {
    return new ScansunRule();
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
}
