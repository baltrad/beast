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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.MockControl;
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
public class BltRouterManagerDBTest extends TestCase {
  private static interface IMethods {
    void storeRecipients(int rule_id, List<String> recipients);
  };
  
  private BltRouter classUnderTest = null;
  private MockControl jdbcControl = null;
  private SimpleJdbcOperations jdbc = null;
  
  private MockControl ruleManagerControl = null;
  private IRuleManager ruleManager = null;
  private Map<String,IRuleManager> ruleManagerMap = null;
  
  private MockControl methodsControl = null;
  private IMethods methods = null;
  
  protected void setUp() throws Exception {
    jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    ruleManagerControl = MockControl.createControl(IRuleManager.class);
    ruleManager = (IRuleManager)ruleManagerControl.getMock();
    methodsControl = MockControl.createControl(IMethods.class);
    methods = (IMethods)methodsControl.getMock();
    
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
  
  protected void tearDown() throws Exception {
    jdbcControl = null;
    jdbc = null;
    ruleManagerControl = null;
    ruleManager = null;
    classUnderTest = null;
  }
  
  protected void replay() {
    jdbcControl.replay();
    ruleManagerControl.replay();
    methodsControl.replay();
  }
  
  protected void verify() {
    jdbcControl.verify();
    ruleManagerControl.verify();
    methodsControl.verify();
  }
  
  public void testDeleteDefinition() throws Exception {
    List<RouteDefinition> defs = new ArrayList<RouteDefinition>();
    RouteDefinition d = new RouteDefinition();
    d.setName("KALLE");
    defs.add(d);
    classUnderTest.setDefinitions(defs);
    
    Map<String, Object> values = new HashMap<String, Object>();
    values.put("rule_id", 3);
    values.put("type", "sometype");
    
    jdbc.queryForMap("select rule_id, type from beast_router_rules where name=?",
        new Object[]{"KALLE"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(values);
    
    ruleManager.delete(3);
    jdbc.update("delete from beast_router_dest where rule_id=?",
        new Object[]{3});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    jdbc.update("delete from beast_router_rules where rule_id=?",
        new Object[]{3});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    replay();
    
    classUnderTest.deleteDefinition("KALLE");
    
    verify();
    assertNull(classUnderTest.getDefinition("KALLE"));
  }

  public void testStoreDefinition() {
    List<String> recipients = new ArrayList<String>();
    IRule rule = new IRule() {
      public String getType() {
        return "sometype";
      }
      public IBltMessage handle(IBltMessage message) {
        return null;
      }
    };
    RouteDefinition def = new RouteDefinition();
    def.setActive(true);
    def.setAuthor("nisse");
    def.setDescription("some description");
    def.setName("some name");
    def.setRecipients(recipients);
    def.setRule(rule);
    
    jdbc.update("insert into beast_router_rules (name,type,author,description,active)"+
        " values (?,?,?,?,?)",
        new Object[]{"some name", "sometype", "nisse", "some description", true});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    jdbc.queryForInt("select rule_id from beast_router_rules where name=?",
        new Object[]{"some name"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(13);

    ruleManager.store(13, rule);
    methods.storeRecipients(13, recipients);
    
    replay();
    
    classUnderTest.storeDefinition(def);
    
    verify();
    assertSame(def, classUnderTest.getDefinition("some name"));
  }
  
  public void testUpdateDefinition() throws Exception {
    List<RouteDefinition> defs = new ArrayList<RouteDefinition>();
    RouteDefinition d = new RouteDefinition();
    d.setName("some name");
    defs.add(d);
    classUnderTest.setDefinitions(defs);
    
    // add another rule manager for update
    MockControl newRuleManagerControl = MockControl.createControl(IRuleManager.class);
    IRuleManager newRuleManager = (IRuleManager)newRuleManagerControl.getMock();
    ruleManagerMap.put("newtype", newRuleManager);
    
    List<String> recipients = new ArrayList<String>();
    IRule rule = new IRule() {
      public String getType() {
        return "newtype";
      }
      public IBltMessage handle(IBltMessage message) {
        return null;
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
    
    jdbc.queryForMap("select rule_id, type from beast_router_rules where name=?",
        new Object[]{"some name"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(values);
    ruleManager.delete(3);
    
    jdbc.update("update beast_router_rules set type=?, author=?, description=?, active=? where rule_id=?",
        new Object[]{"newtype", "nisse", "some description", true, 3});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    newRuleManager.store(3, rule);
    
    methods.storeRecipients(3, recipients);
    
    replay();
    
    classUnderTest.updateDefinition(def);
    
    verify();
    assertSame(def, classUnderTest.getDefinition("some name"));
  }
  
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

    jdbc.query("select rule_id, name,type,author,description,active from beast_router_rules",
        mapper);
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(definitions);
    
    replay();
    
    classUnderTest.afterPropertiesSet();
    
    verify();
    assertSame(definitions, classUnderTest.getDefinitions());
  }
  
  public void testStoreRecipients() throws Exception {
    List<String> recipients = new ArrayList<String>();
    recipients.add("A");
    recipients.add("B");
    
    jdbc.update("delete from beast_router_dest where rule_id=?",
        new Object[]{13});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    jdbc.update("insert into beast_router_dest (rule_id, recipient) values (?,?)",
        new Object[]{13, "A"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);

    jdbc.update("insert into beast_router_dest (rule_id, recipient) values (?,?)",
        new Object[]{13, "B"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    classUnderTest = new BltRouter();
    classUnderTest.setJdbcTemplate(jdbc);
    
    replay();
    
    classUnderTest.storeRecipients(13, recipients);
    
    verify();
  }
}
