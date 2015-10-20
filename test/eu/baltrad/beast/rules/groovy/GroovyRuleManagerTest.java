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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * @author Anders Henja
 */
public class GroovyRuleManagerTest extends EasyMockSupport {
  private static interface ManagerMethods {
    public GroovyRule createRule(String script);
  };

  private GroovyRuleManager classUnderTest = null;
  private JdbcOperations jdbc = null;

  @Before
  public void setUp() throws Exception {
    jdbc = createMock(JdbcOperations.class);
    classUnderTest = new GroovyRuleManager();
    classUnderTest.setJdbcTemplate(jdbc);
  }

  @After
  public void tearDown() throws Exception {
    jdbc = null;
    classUnderTest = null;
  }

  @Test
  public void testDelete() throws Exception {
    expect(jdbc.update("delete from beast_groovy_rules where rule_id=?", new Object[]{0})).andReturn(0);
    
    replayAll();
    
    classUnderTest.delete(0);
    
    verifyAll();
  }
  
  @Test
  public void testLoad() throws Exception {
    GroovyRule kallerule = new GroovyRule();
    
    final ManagerMethods methods = createMock(ManagerMethods.class);
    classUnderTest = new GroovyRuleManager() {
      protected GroovyRule createRule(String script) {
        return methods.createRule(script);
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    
    
    expect(jdbc.queryForObject("select definition from beast_groovy_rules where rule_id=?",
        String.class, new Object[]{0})).andReturn("KALLE");
    expect(methods.createRule("KALLE")).andReturn(kallerule);
    
    replayAll();
    
    GroovyRule result = (GroovyRule)classUnderTest.load(0);
    
    verifyAll();
    assertSame(kallerule, result);
  }

  @Test
  public void testStore() throws Exception {
    GroovyRule rule = new GroovyRule() {
      public String getScript() {
        return "KALLE";
      }
    };

    expect(jdbc.update("insert into beast_groovy_rules (rule_id, definition) values (?,?)",
        new Object[]{0,"KALLE"})).andReturn(1);
    
    replayAll();
    
    classUnderTest.store(0, rule);
    
    verifyAll();
  }
  
  @Test
  public void testUpdate() throws Exception {
    GroovyRule rule = new GroovyRule() {
      public String getScript() {
        return "KALLE";
      }
    };

    expect(jdbc.update("update beast_groovy_rules set definition=? where rule_id=?",
        new Object[]{0,"KALLE"})).andReturn(1);
    
    replayAll();
    
    classUnderTest.update(0, rule);
    
    verifyAll();
  }
  
  @Test
  public void testCreateRule() throws Exception {
    GroovyRule result = (GroovyRule)classUnderTest.createRule();
    assertNotNull(result);
  }
}
