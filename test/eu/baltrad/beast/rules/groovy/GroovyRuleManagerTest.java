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

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

/**
 * @author Anders Henja
 *
 */
public class GroovyRuleManagerTest extends TestCase {
  private static interface ManagerMethods {
    public GroovyRule createRule(String script);
  };

  private GroovyRuleManager classUnderTest = null;
  private MockControl jdbcControl = null;
  private SimpleJdbcOperations jdbc = null;
  
  protected void setUp() throws Exception {
    jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    
    classUnderTest = new GroovyRuleManager();
    classUnderTest.setJdbcTemplate(jdbc);
  }
  
  protected void tearDown() throws Exception {
    jdbc = null;
    jdbcControl = null;
    classUnderTest = null;
  }
  
  protected void replay() {
    jdbcControl.replay();
  }
  
  protected void verify() {
    jdbcControl.verify();
  }
  
  public void testDelete() throws Exception {
    jdbc.update("delete from beast_groovy_rules where rule_id=?", new Object[]{0});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    replay();
    classUnderTest.delete(0);
    verify();
  }
  
  public void testLoad() throws Exception {
    GroovyRule kallerule = new GroovyRule();
    
    MockControl methodsControl = MockControl.createControl(ManagerMethods.class);
    
    final ManagerMethods methods = (ManagerMethods)methodsControl.getMock();
    classUnderTest = new GroovyRuleManager() {
      protected GroovyRule createRule(String script) {
        return methods.createRule(script);
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    
    
    jdbc.queryForObject("select definition from beast_groovy_rules where rule_id=?",
        String.class, new Object[]{0});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue("KALLE");
    methods.createRule("KALLE");
    methodsControl.setReturnValue(kallerule);
    
    replay();
    methodsControl.replay();
    
    GroovyRule result = (GroovyRule)classUnderTest.load(0);
    
    verify();
    methodsControl.verify();
    assertSame(kallerule, result);
  }
  
  public void testStore() throws Exception {
    GroovyRule rule = new GroovyRule() {
      public String getScript() {
        return "KALLE";
      }
    };

    jdbc.update("insert into beast_groovy_rules (rule_id, definition) values (?,?)",
        new Object[]{0,"KALLE"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    replay();
    
    classUnderTest.store(0, rule);
    
    verify();
  }
  
  public void testUpdate() throws Exception {
    GroovyRule rule = new GroovyRule() {
      public String getScript() {
        return "KALLE";
      }
    };

    jdbc.update("update beast_groovy_rules set definition=? where rule_id=?",
        new Object[]{0,"KALLE"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    replay();
    
    classUnderTest.update(0, rule);
    
    verify();
  }
}
