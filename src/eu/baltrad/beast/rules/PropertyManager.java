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
package eu.baltrad.beast.rules;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

/**
 * Manage rule properties stored in beast_rule_properties
 */
public class PropertyManager {
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
   * delete rule properties from the database
   * @param ruleId id of the rule whose properties should be deleted
   */
  public void deleteProperties(int ruleId) {
    template.update("delete from beast_rule_properties where rule_id=?",
        new Object[]{ruleId});
  }

  /**
   * load rule properties from the database
   * @param ruleId id of the rule properties should be loaded for
   * @return map of properties
   */
  public Map<String, String> loadProperties(int ruleId) {
    final Map<String, String> props = new HashMap<String, String>();
    RowCallbackHandler rch = new RowCallbackHandler() {
      @Override
      public void processRow(ResultSet rs) throws SQLException {
        props.put(rs.getString("key"), rs.getString("value"));
      }
    };

    JdbcOperations ops = template.getJdbcOperations();
    ops.query("select * from beast_rule_properties where rule_id=?",
              new Object[]{ruleId}, rch);
    return props;
  }
  
  /**
   * store rule properties in the database
   * @param ruleId id of the rule these properties are for
   * @param props map of properties
   */
  public void storeProperties(int ruleId, Map<String, String> props) {
    for (String key : props.keySet()) {
      template.update("insert into beast_rule_properties " +
                      "(rule_id, key, value) values (?, ?, ?)",
                      new Object[]{ruleId, key, props.get(key)});
    }
  }
  
  /**
   * update rule properties in the database
   * @param ruleId id of the rule these properties are for
   * @param props map of properties
   */
  public void updateProperties(int ruleId, Map<String, String> props) {
    deleteProperties(ruleId);
    storeProperties(ruleId, props);
  }
}
