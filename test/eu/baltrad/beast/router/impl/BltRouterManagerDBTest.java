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
package eu.baltrad.beast.router.impl;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleManager;

/**
 * @author Anders Henja
 *
 */
public class BltRouterManagerDBTest extends EasyMockSupport {
  private static interface IMethods {
    void storeRecipients(int rule_id, List<String> recipients);
  };
  
  private BltRouter classUnderTest = null;
  private SimpleJdbcOperations jdbc = null;
  private IRuleManager ruleManager = null;
  private Map<String,IRuleManager> ruleManagerMap = null;
  private IMethods methods = null;
  
  @Before
  public void setUp() throws Exception {
    jdbc = createMock(SimpleJdbcOperations.class);
    ruleManager = createMock(IRuleManager.class);
    methods = createMock(IMethods.class);
    
    classUnderTest = new BltRouter() {
      protected void storeRecipients(int rule_id, List<String> recipients) {
        methods.storeRecipients(rule_id, recipients);
      };
    };
    classUnderTest.setJdbcTemplate(jdbc);
    
    ruleManagerMap = new HashMap<String,IRuleManager>();
    ruleManagerMap.put("sometype", ruleManager);
    classUnderTest.setRuleManagers(ruleManagerMap);
  }

  @After
  public void tearDown() throws Exception {
    jdbc = null;
    ruleManager = null;
    classUnderTest = null;
  }

  @Test
  public void testDeleteDefinition() throws Exception {
    List<RouteDefinition> defs = new ArrayList<RouteDefinition>();
    RouteDefinition d = new RouteDefinition();
    d.setName("KALLE");
    defs.add(d);
    classUnderTest.setDefinitions(defs);
    
    Map<String, Object> values = new HashMap<String, Object>();
    values.put("rule_id", 3);
    values.put("type", "sometype");
    
    expect(jdbc.queryForMap("select rule_id, type from beast_router_rules where name=?",
        new Object[]{"KALLE"})).andReturn(values);
    
    ruleManager.delete(3);
    
    expect(jdbc.update("delete from beast_router_dest where rule_id=?",
        new Object[]{3})).andReturn(0);
    
    expect(jdbc.update("delete from beast_router_rules where rule_id=?",
        new Object[]{3})).andReturn(0);
    
    replayAll();
    
    classUnderTest.deleteDefinition("KALLE");
    
    verifyAll();
    assertNull(classUnderTest.getDefinition("KALLE"));
  }

  @Test
  public void testStoreDefinition() {
    List<String> recipients = new ArrayList<String>();
    IRule rule = new IRule() {
      public String getType() {
        return "sometype";
      }
      public IBltMessage handle(IBltMessage message) {
        return null;
      }
      public boolean isValid() {
        return true;
      }
    };
    RouteDefinition def = new RouteDefinition();
    def.setActive(true);
    def.setAuthor("nisse");
    def.setDescription("some description");
    def.setName("some name");
    def.setRecipients(recipients);
    def.setRule(rule);
    
    expect(jdbc.update("insert into beast_router_rules (name,type,author,description,active)"+
        " values (?,?,?,?,?)",
        new Object[]{"some name", "sometype", "nisse", "some description", true})).andReturn(0);
    
    expect(jdbc.queryForInt("select rule_id from beast_router_rules where name=?",
        new Object[]{"some name"})).andReturn(13);

    ruleManager.store(13, rule);
    methods.storeRecipients(13, recipients);
    
    replayAll();
    
    classUnderTest.storeDefinition(def);
    
    verifyAll();
    assertSame(def, classUnderTest.getDefinition("some name"));
  }
  
  @Test
  public void testUpdateDefinition() throws Exception {
    List<RouteDefinition> defs = new ArrayList<RouteDefinition>();
    RouteDefinition d = new RouteDefinition();
    d.setName("some name");
    defs.add(d);
    classUnderTest.setDefinitions(defs);
    
    // add another rule manager for update
    IRuleManager newRuleManager = createMock(IRuleManager.class);
    ruleManagerMap.put("newtype", newRuleManager);
    
    List<String> recipients = new ArrayList<String>();
    IRule rule = new IRule() {
      public String getType() {
        return "newtype";
      }
      public IBltMessage handle(IBltMessage message) {
        return null;
      }
      public boolean isValid() {
        return true;
      }      
    };
    RouteDefinition def = new RouteDefinition();
    def.setActive(true);
    def.setAuthor("nisse");
    def.setDescription("some description");
    def.setName("some name");
    def.setRecipients(recipients);
    def.setRule(rule);
    
    Map<String, Object> values = new HashMap<String, Object>();
    values.put("rule_id", 3);
    values.put("type", "sometype");
    
    expect(jdbc.queryForMap("select rule_id, type from beast_router_rules where name=?",
        new Object[]{"some name"})).andReturn(values);
    ruleManager.delete(3);
    
    expect(jdbc.update("update beast_router_rules set type=?, author=?, description=?, active=? where rule_id=?",
        new Object[]{"newtype", "nisse", "some description", true, 3})).andReturn(0);
    
    newRuleManager.store(3, rule);
    
    methods.storeRecipients(3, recipients);
    
    replayAll();
    
    classUnderTest.updateDefinition(def);
    
    verifyAll();
    assertSame(def, classUnderTest.getDefinition("some name"));
  }
  
  @Test
  public void testAfterPropertiesSet() throws Exception {
    List<RouteDefinition> definitions = new ArrayList<RouteDefinition>();
    
    final ParameterizedRowMapper<RouteDefinition> mapper = new ParameterizedRowMapper<RouteDefinition>() {
      public RouteDefinition mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };
    
    classUnderTest = new BltRouter() {
      protected ParameterizedRowMapper<RouteDefinition> getRouteDefinitionMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);

    expect(jdbc.query("select rule_id, name,type,author,description,active from beast_router_rules",
        mapper)).andReturn(definitions);
    
    replayAll();
    
    classUnderTest.afterPropertiesSet();
    
    verifyAll();
    assertSame(definitions, classUnderTest.getDefinitions());
  }
  
  @Test
  public void testStoreRecipients() throws Exception {
    List<String> recipients = new ArrayList<String>();
    recipients.add("A");
    recipients.add("B");
    
    expect(jdbc.update("delete from beast_router_dest where rule_id=?",
        new Object[]{13})).andReturn(0);
    
    expect(jdbc.update("insert into beast_router_dest (rule_id, recipient) values (?,?)",
        new Object[]{13, "A"})).andReturn(0);

    expect(jdbc.update("insert into beast_router_dest (rule_id, recipient) values (?,?)",
        new Object[]{13, "B"})).andReturn(0);
    
    classUnderTest = new BltRouter();
    classUnderTest.setJdbcTemplate(jdbc);
    
    replayAll();
    
    classUnderTest.storeRecipients(13, recipients);
    
    verifyAll();
  }
}
