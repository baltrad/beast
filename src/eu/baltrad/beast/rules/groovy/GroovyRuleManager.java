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
package eu.baltrad.beast.rules.groovy;

import org.springframework.jdbc.core.JdbcOperations;

import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleManager;

/**
 * @author Anders Henja
 *
 */
public class GroovyRuleManager implements IRuleManager {
  /**
   * The JDBC template to use for db-operations
   */
  private JdbcOperations template = null;
  
  /**
   * Sets the jdbc template.
   * @param template the template
   */
  public void setJdbcTemplate(JdbcOperations template) {
    this.template = template;
  }  
  
  /**
   * @see eu.baltrad.beast.rules.IRuleManager#delete(int)
   */
  @Override
  public void delete(int ruleId) {
    template.update("delete from beast_groovy_rules where rule_id=?",
        new Object[]{ruleId});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#load(int)
   */
  @Override
  public IRule load(int ruleId) {
    String definition = template.queryForObject("select definition from beast_groovy_rules where rule_id=?",
        String.class, new Object[]{ruleId});
    return createRule(definition);
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#store(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void store(int ruleId, IRule rule) {
    GroovyRule grule = (GroovyRule)rule;
    template.update("insert into beast_groovy_rules (rule_id, definition) values (?,?)",
        new Object[]{ruleId, grule.getScript()});
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#update(int, eu.baltrad.beast.rules.IRule)
   */
  @Override
  public void update(int ruleId, IRule rule) {
    GroovyRule grule = (GroovyRule)rule;
    template.update("update beast_groovy_rules set definition=? where rule_id=?",
        new Object[]{ruleId, grule.getScript()});
  }
  
  /**
   * Creates an GroovyRule instance from a script
   * @param script the script
   * @return a groovy rule
   */
  protected GroovyRule createRule(String script) {
    GroovyRule rule = new GroovyRule();
    rule.setScriptInternal(script, true, false);
    return rule;
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleManager#createRule()
   */
  @Override
  public GroovyRule createRule() {
    return new GroovyRule();
  }
}
