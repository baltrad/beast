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
package eu.baltrad.beast.rules.composite;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.util.RuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class CompositingRuleManagerTest extends TestCase {
  private static interface ManagerMethods {
    public CompositingRule createRule();
    public void storeSources(int rule_id, List<String> sources);
    public List<String> getSources(int rule_id);
    public void storeDetectors(int rule_id, List<String> detectors);
    public List<String> getDetectors(int rule_id);
  };

  private CompositingRuleManager classUnderTest = null;
  private MockControl jdbcControl = null;
  private SimpleJdbcOperations jdbc = null;
  
  protected void setUp() throws Exception {
    jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    
    classUnderTest = new CompositingRuleManager();
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
    MockControl methodsControl = MockControl.createControl(ManagerMethods.class);
    final ManagerMethods methods = (ManagerMethods)methodsControl.getMock();
    
    classUnderTest = new CompositingRuleManager() {
      protected void storeSources(int rule_id, List<String> sources) {
        methods.storeSources(rule_id, sources);
      }
      protected void storeDetectors(int rule_id, List<String> detectors) {
        methods.storeDetectors(rule_id, detectors);
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    
    methods.storeSources(13, null);
    methods.storeDetectors(13, null);
    
    jdbc.update("delete from beast_composite_rules where rule_id=?",
        new Object[]{13});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    replay();
    methodsControl.replay();
    
    classUnderTest.delete(13);
    
    verify();
    methodsControl.verify();
  }
  
  public void testLoad() throws Exception {
    CompositingRule rule = new CompositingRule();
    final ParameterizedRowMapper<CompositingRule> mapper = new ParameterizedRowMapper<CompositingRule>() {
      public CompositingRule mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };
    classUnderTest = new CompositingRuleManager() {
      protected ParameterizedRowMapper<CompositingRule> getCompsiteRuleMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    
    jdbc.queryForObject("select * from beast_composite_rules where rule_id=?",
        mapper,
        new Object[]{13});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(rule);
    
    replay();
    CompositingRule result = (CompositingRule)classUnderTest.load(13);
    verify();
    assertSame(rule, result);
  }
  
  public void testStore() throws Exception {
    MockControl methodsControl = MockControl.createControl(ManagerMethods.class);
    final ManagerMethods methods = (ManagerMethods)methodsControl.getMock();
    
    List<String> sources = new ArrayList<String>();
    List<String> detectors = new ArrayList<String>();
    
    CompositingRule rule = new CompositingRule();
    rule.setArea("seang");
    rule.setInterval(12);
    rule.setSources(sources);
    rule.setTimeout(20);
    rule.setScanBased(true);
    rule.setDetectors(detectors);
    rule.setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    rule.setMethod(CompositingRule.PPI);
    rule.setProdpar("0.5");
    jdbc.update(
        "insert into beast_composite_rules (rule_id, area, interval, timeout, byscan, selection_method, method, prodpar)"+
        " values (?,?,?,?,?,?,?,?)", new Object[]{13, "seang", 12, 20, true, CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL, CompositingRule.PPI, "0.5"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    methods.storeSources(13, sources);
    methods.storeDetectors(13, detectors);
    
    classUnderTest = new CompositingRuleManager() {
      protected void storeSources(int rule_id, List<String> sources) {
        methods.storeSources(rule_id, sources);
      }
      protected void storeDetectors(int rule_id, List<String> detectors) {
        methods.storeDetectors(rule_id, detectors);
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    
    replay();
    methodsControl.replay();
    
    classUnderTest.store(13, rule);
    
    verify();
    methodsControl.verify();
    assertEquals(13, rule.getRuleId());
  }
  
  public void testUpdate() throws Exception {
    MockControl methodsControl = MockControl.createControl(ManagerMethods.class);
    final ManagerMethods methods = (ManagerMethods)methodsControl.getMock();
    
    List<String> sources = new ArrayList<String>();
    List<String> detectors = new ArrayList<String>();
    CompositingRule rule = new CompositingRule();
    rule.setArea("seang");
    rule.setInterval(12);
    rule.setSources(sources);
    rule.setTimeout(20);
    rule.setScanBased(true);
    rule.setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    rule.setDetectors(detectors);
    rule.setMethod(CompositingRule.PPI);
    rule.setProdpar("0.5");
    jdbc.update("update beast_composite_rules set area=?, interval=?, timeout=?, byscan=?, selection_method=?, method=?, prodpar=? where rule_id=?",
        new Object[]{"seang", 12, 20, true, CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL, CompositingRule.PPI, "0.5", 13});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    methods.storeSources(13, sources);
    methods.storeDetectors(13, detectors);
    
    classUnderTest = new CompositingRuleManager() {
      protected void storeSources(int rule_id, List<String> sources) {
        methods.storeSources(rule_id, sources);
      }
      protected void storeDetectors(int rule_id, List<String> sources) {
        methods.storeDetectors(rule_id, sources);
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    
    replay();
    methodsControl.replay();
    
    classUnderTest.update(13, rule);
    
    verify();
    methodsControl.verify();
    assertEquals(13, rule.getRuleId());
  }
  
  public void testStoreSources() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("A");
    sources.add("B");
    
    jdbc.update("delete from beast_composite_sources where rule_id=?",
        new Object[]{13});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    jdbc.update(
        "insert into beast_composite_sources (rule_id, source)"+
        " values (?,?)", new Object[]{13, "A"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);

    jdbc.update(
        "insert into beast_composite_sources (rule_id, source)"+
        " values (?,?)", new Object[]{13, "B"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    replay();
    
    classUnderTest.storeSources(13, sources);
    
    verify();
  }
  
  public void testStoreSources_nullSources() throws Exception {
    jdbc.update("delete from beast_composite_sources where rule_id=?",
        new Object[]{13});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    replay();
    classUnderTest.storeSources(13, null);
    verify();
  }
  
  public void testGetSources() throws Exception {
    List<String> sources = new ArrayList<String>();
    
    final ParameterizedRowMapper<String> mapper = new ParameterizedRowMapper<String>() {
      public String mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };
    classUnderTest = new CompositingRuleManager() {
      protected ParameterizedRowMapper<String> getSourceMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    
    jdbc.query("select source from beast_composite_sources where rule_id=?",
        mapper,
        new Object[]{13});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(sources);
    
    replay();
    
    List<String> result = classUnderTest.getSources(13);
    
    verify();
    assertSame(sources, result);
  }
  
  public void testStoreDetectors() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("A");
    sources.add("B");
    
    jdbc.update("delete from beast_composite_detectors where rule_id=?",
        new Object[]{13});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    jdbc.update(
        "insert into beast_composite_detectors (rule_id, name)"+
        " values (?,?)", new Object[]{13, "A"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);

    jdbc.update(
        "insert into beast_composite_detectors (rule_id, name)"+
        " values (?,?)", new Object[]{13, "B"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    replay();
    
    classUnderTest.storeDetectors(13, sources);
    
    verify();
  }

  public void testStoreDetectors_null() throws Exception {
    jdbc.update("delete from beast_composite_detectors where rule_id=?",
        new Object[]{13});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    replay();
    classUnderTest.storeDetectors(13, null);
    verify();
  }
  
  public void testGetDetectors() throws Exception {
    List<String> detectors = new ArrayList<String>();
    
    final ParameterizedRowMapper<String> mapper = new ParameterizedRowMapper<String>() {
      public String mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };
    classUnderTest = new CompositingRuleManager() {
      protected ParameterizedRowMapper<String> getDetectorMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    
    jdbc.query("select name from beast_composite_detectors where rule_id=?",
        mapper,
        new Object[]{13});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(detectors);
    
    replay();
    
    List<String> result = classUnderTest.getDetectors(13);
    
    verify();
    assertSame(detectors, result);
  }
  
  public void testGetCompositeRuleMapper() throws Exception {
    MockControl rsControl = MockControl.createControl(ResultSet.class);
    ResultSet rs = (ResultSet)rsControl.getMock();
    List<String> sources = new ArrayList<String>();
    List<String> detectors = new ArrayList<String>();
    
    MockControl methodControl = MockControl.createControl(ManagerMethods.class);
    final ManagerMethods method = (ManagerMethods)methodControl.getMock();
    CompositingRule arule = new CompositingRule();
    
    method.createRule();
    methodControl.setReturnValue(arule);
    
    rs.getInt("rule_id");
    rsControl.setReturnValue(10);
    rs.getString("area");
    rsControl.setReturnValue("abc");
    rs.getInt("interval");
    rsControl.setReturnValue(15);
    rs.getInt("timeout");
    rsControl.setReturnValue(20);
    rs.getBoolean("byscan");
    rsControl.setReturnValue(true);
    rs.getInt("selection_method");
    rsControl.setReturnValue(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    rs.getString("method");
    rsControl.setReturnValue(CompositingRule.PPI);
    rs.getString("prodpar");
    rsControl.setReturnValue("0.5");
    
    method.getSources(10);
    methodControl.setReturnValue(sources);
    method.getDetectors(10);
    methodControl.setReturnValue(detectors);
    
    classUnderTest = new CompositingRuleManager() {
      protected List<String> getSources(int rule_id) {
        return method.getSources(rule_id);
      }
      protected List<String> getDetectors(int rule_id) {
        return method.getDetectors(rule_id);
      }      
      public CompositingRule createRule() {
        return method.createRule();
      }
    };
    
    ParameterizedRowMapper<CompositingRule> mapper = classUnderTest.getCompsiteRuleMapper();
    
    rsControl.replay();
    methodControl.replay();
    
    CompositingRule result = mapper.mapRow(rs, 1);
    
    rsControl.verify();
    methodControl.verify();
    assertEquals("abc", result.getArea());
    assertEquals(15, result.getInterval());
    assertSame(sources, result.getSources());
    assertEquals(10, result.getRuleId());
    assertEquals(20, result.getTimeout());
    assertEquals(true, result.isScanBased());
    assertEquals(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL, result.getSelectionMethod());
    assertEquals(CompositingRule.PPI, result.getMethod());
    assertEquals("0.5", result.getProdpar());
  }  
  
  public void testCreateRule() throws Exception {
    Catalog catalog = new Catalog();
    RuleUtilities utilities = new RuleUtilities();
    TimeoutManager manager = new TimeoutManager();
    
    classUnderTest = new CompositingRuleManager();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(utilities);
    classUnderTest.setTimeoutManager(manager);
    
    CompositingRule result = classUnderTest.createRule();
    
    assertSame(result.getCatalog(), catalog);
    assertSame(result.getRuleUtilities(), utilities);
    assertSame(result.getTimeoutManager(), manager);
  }
}
