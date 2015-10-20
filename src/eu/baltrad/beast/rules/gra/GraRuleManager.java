/* --------------------------------------------------------------------
Copyright (C) 2009-2014 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.rules.gra;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleManager;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class GraRuleManager implements IRuleManager {
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
  public void store(int rule_id, IRule rule) {
    GraRule arule = (GraRule)rule;
    String area = arule.getArea();
    String dfield = arule.getDistancefield();
    int fhours = arule.getFilesPerHour();
    int aloss = arule.getAcceptableLoss();
    String otype = arule.getObjectType();
    String quantity = arule.getQuantity();
    double zrA = arule.getZrA();
    double zrB = arule.getZrB();
    int firstTermUTC = arule.getFirstTermUTC();
    int interval = arule.getInterval();
    
    template.update("INSERT INTO beast_gra_rules (rule_id, area, distancefield, files_per_hour, acceptable_loss, object_type, quantity, zra, zrb, first_term_utc, interval) VALUES (?,?,?,?,?,?,?,?,?,?,?)", 
        new Object[]{rule_id, area, dfield, fhours, aloss, otype, quantity, zrA, zrB, firstTermUTC, interval});
    
    arule.setRuleId(rule_id);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#load(int)
   */
  @Override
  public IRule load(int rule_id) {
    return template.queryForObject("SELECT * FROM beast_gra_rules WHERE rule_id=?", 
        getGraRuleMapper(),
        new Object[]{rule_id});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#update(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void update(int rule_id, IRule rule) {
    GraRule arule = (GraRule)rule;
    String area = arule.getArea();
    String dfield = arule.getDistancefield();
    int fhours = arule.getFilesPerHour();
    int acceptable_loss = arule.getAcceptableLoss();
    String otype = arule.getObjectType();
    String quantity = arule.getQuantity();
    double zra = arule.getZrA();
    double zrb = arule.getZrB();
    int firstTermUTC = arule.getFirstTermUTC();
    int interval = arule.getInterval();
    
    template.update("UPDATE beast_gra_rules SET " +
        "area=?, distancefield=?, files_per_hour=?, acceptable_loss=?, object_type=?, quantity=?, zra=?, zrb=?, first_term_utc=?, interval=? WHERE rule_id=?",
        new Object[]{area, dfield, fhours, acceptable_loss, otype, quantity, zra, zrb, firstTermUTC, interval, rule_id});
    
    arule.setRuleId(rule_id);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#delete(int)
   */
  @Override
  public void delete(int rule_id) {
    template.update("DELETE FROM beast_gra_rules WHERE rule_id=?", new Object[]{rule_id});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#createRule()
   */
  @Override
  public GraRule createRule() {
    GraRule result = new GraRule();
    result.setCatalog(catalog);
    result.setRuleUtilities(ruleUtilities);
    return result;
  }

  /**
   * @return the gra rule mapper to use for creating a rule from the database result.
   */
  protected RowMapper<GraRule> getGraRuleMapper() {
    return new RowMapper<GraRule>() {
      @Override
      public GraRule mapRow(ResultSet rs, int rnum)
          throws SQLException {
        GraRule result = createRule();
        int rule_id = rs.getInt("rule_id");
        result.setRuleId(rule_id);
        result.setArea(rs.getString("area"));
        result.setDistancefield(rs.getString("distancefield"));
        result.setFilesPerHour(rs.getInt("files_per_hour"));
        result.setAcceptableLoss(rs.getInt("acceptable_loss"));
        result.setObjectType(rs.getString("object_type"));
        result.setQuantity(rs.getString("quantity"));
        result.setZrA(rs.getDouble("zra"));
        result.setZrB(rs.getDouble("zrb"));
        result.setFirstTermUTC(rs.getInt("first_term_utc"));
        result.setInterval(rs.getInt("interval"));
        return result;
      }
    };
  }
}
