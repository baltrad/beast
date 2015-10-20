/* --------------------------------------------------------------------
Copyright (C) 2009-2012 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.rules.gmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleManager;

/**
 * Manager for keeping track of the google map rules
 * @author Anders Henja
 * @date 2012-03-21
 */
public class GoogleMapRuleManager implements IRuleManager {
  /**
   * The simple jdbc template
   */
  private JdbcOperations template = null;
  
  /**
   * The catalog for database access
   */
  private Catalog catalog = null;
  
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
   * @see eu.baltrad.beast.rules.IRuleManager#store(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void store(int rule_id, IRule rule) {
    GoogleMapRule grule = (GoogleMapRule)rule;
    String area = grule.getArea();
    String path = grule.getPath();
    template.update("insert into beast_gmap_rules (rule_id, area, path) values (?,?,?)",
        new Object[]{rule_id, area, path});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#load(int)
   */
  @Override
  public IRule load(int rule_id) {
    return template.queryForObject(
        "select * from beast_gmap_rules where rule_id=?",
        getGmapRuleMapper(),
        new Object[]{rule_id});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#update(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void update(int rule_id, IRule rule) {
    GoogleMapRule grule = (GoogleMapRule)rule;
    template.update("update beast_gmap_rules set area=?, path=? where rule_id=?",
        new Object[]{grule.getArea(), grule.getPath(), rule_id});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#delete(int)
   */
  @Override
  public void delete(int rule_id) {
    template.update("delete from beast_gmap_rules where rule_id=?",
        new Object[]{rule_id});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#createRule()
   */
  @Override
  public IRule createRule() {
    GoogleMapRule result = new GoogleMapRule();
    result.setCatalog(catalog);
    return result;
  }

  /**
   * Creates a db mapper for the google map rule
   * @return the row mapper
   */
  protected RowMapper<GoogleMapRule> getGmapRuleMapper() {
    return new RowMapper<GoogleMapRule>() {
      @Override
      public GoogleMapRule mapRow(ResultSet rs, int row) throws SQLException {
        GoogleMapRule result = (GoogleMapRule)createRule();
        result.setArea(rs.getString("area"));
        result.setPath(rs.getString("path"));
        return result;
      }
    };
  }
}
