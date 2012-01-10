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
package eu.baltrad.beast.rules.volume;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.util.RuleUtilities;

/**
 * @author Anders Henja
 * 
 */
public class VolumeRuleManagerTest extends TestCase {
  private static interface ManagerMethods {
    public void storeSources(int rule_id, List<String> sources);
    public List<String> getSources(int rule_id);    
    public VolumeRule createRule();
    public void storeDetectors(int rule_id, List<String> detectors);
    public List<String> getDetectors(int rule_id);    
  };
  
  private VolumeRuleManager classUnderTest = null;
  private MockControl jdbcControl = null;
  private SimpleJdbcOperations jdbc = null;

  protected void setUp() throws Exception {
    jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    jdbc = (SimpleJdbcOperations) jdbcControl.getMock();

    classUnderTest = new VolumeRuleManager();
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
    
    classUnderTest = new VolumeRuleManager() {
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
    jdbc.update("delete from beast_volume_rules where rule_id=?",
        new Object[] { 13 });
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);

    replay();
    methodsControl.replay();
    
    classUnderTest.delete(13);

    verify();
    methodsControl.verify();
  }

  public void testLoad() throws Exception {
    VolumeRule rule = new VolumeRule();
    final ParameterizedRowMapper<VolumeRule> mapper = new ParameterizedRowMapper<VolumeRule>() {
      public VolumeRule mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };
    classUnderTest = new VolumeRuleManager() {
      protected ParameterizedRowMapper<VolumeRule> getVolumeRuleMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);

    jdbc.queryForObject("select * from beast_volume_rules where rule_id=?",
        mapper, new Object[] { 13 });
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(rule);

    replay();
    VolumeRule result = (VolumeRule) classUnderTest.load(13);
    verify();
    assertSame(rule, result);
  }
  
  public void testStore() throws Exception {
    MockControl methodsControl = MockControl.createControl(ManagerMethods.class);
    final ManagerMethods methods = (ManagerMethods)methodsControl.getMock();
    
    List<String> sources = new ArrayList<String>();
    List<String> detectors = new ArrayList<String>();
    
    VolumeRule rule = new VolumeRule();
    rule.setAscending(false);
    rule.setElevationMax(10.0);
    rule.setElevationMin(2.0);
    rule.setInterval(6);
    rule.setTimeout(20);
    rule.setSources(sources);
    rule.setDetectors(detectors);
    
    jdbc.update("insert into beast_volume_rules (rule_id, interval, timeout, " +
        "ascending, minelev, maxelev) values (?,?,?,?,?,?)", new Object[]{13, 6, 20, false, 2.0, 10.0});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    methods.storeSources(13, sources);
    methods.storeDetectors(13, detectors);
    
    classUnderTest = new VolumeRuleManager() {
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
  }
  
  public void testUpdate() throws Exception {
    MockControl methodsControl = MockControl.createControl(ManagerMethods.class);
    final ManagerMethods methods = (ManagerMethods)methodsControl.getMock();
    
    List<String> sources = new ArrayList<String>();
    List<String> detectors = new ArrayList<String>();
    
    VolumeRule rule = new VolumeRule();
    rule.setAscending(false);
    rule.setElevationMax(10.0);
    rule.setElevationMin(2.0);
    rule.setInterval(6);
    rule.setTimeout(20);
    rule.setSources(sources);
    rule.setDetectors(detectors);
    
    jdbc.update("update beast_volume_rules set interval=?, timeout=?, " +
        "ascending=?, minelev=?, maxelev=? where rule_id=?", new Object[]{6, 20, false, 2.0, 10.0, 13});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    methods.storeSources(13, sources);
    methods.storeDetectors(13, detectors);
    
    classUnderTest = new VolumeRuleManager() {
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
    
  }
  
  public void testStoreSources() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("A");
    sources.add("B");
    
    jdbc.update("delete from beast_volume_sources where rule_id=?",
        new Object[]{13});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    jdbc.update(
        "insert into beast_volume_sources (rule_id, source)"+
        " values (?,?)", new Object[]{13, "A"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);

    jdbc.update(
        "insert into beast_volume_sources (rule_id, source)"+
        " values (?,?)", new Object[]{13, "B"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    
    replay();
    
    classUnderTest.storeSources(13, sources);
    
    verify();
  }
  
  public void testStoreSources_nullSources() throws Exception {
    jdbc.update("delete from beast_volume_sources where rule_id=?",
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
    classUnderTest = new VolumeRuleManager() {
      protected ParameterizedRowMapper<String> getSourceMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    
    jdbc.query("select source from beast_volume_sources where rule_id=?",
        mapper,
        new Object[]{13});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(sources);
    
    replay();
    
    List<String> result = classUnderTest.getSources(13);
    
    verify();
    assertSame(sources, result);
  }
  
  public void testGetVolumeRuleMapper() throws Exception {
    MockControl rsControl = MockControl.createControl(ResultSet.class);
    ResultSet rs = (ResultSet)rsControl.getMock();
    List<String> sources = new ArrayList<String>();
    List<String> detectors = new ArrayList<String>();
    MockControl methodControl = MockControl.createControl(ManagerMethods.class);
    final ManagerMethods method = (ManagerMethods)methodControl.getMock();
    VolumeRule volumeRule = new VolumeRule();
    
    method.createRule();
    methodControl.setReturnValue(volumeRule);
    rs.getInt("rule_id");
    rsControl.setReturnValue(10);
    rs.getInt("interval");
    rsControl.setReturnValue(6);
    rs.getInt("timeout");
    rsControl.setReturnValue(15);
    rs.getBoolean("ascending");
    rsControl.setReturnValue(false);
    rs.getDouble("minelev");
    rsControl.setReturnValue(2.0);
    rs.getDouble("maxelev");
    rsControl.setReturnValue(10.0);
    method.getSources(10);
    methodControl.setReturnValue(sources);
    method.getDetectors(10);
    methodControl.setReturnValue(detectors);
    
    classUnderTest = new VolumeRuleManager() {
      protected List<String> getSources(int rule_id) {
        return method.getSources(rule_id);
      }
      protected List<String> getDetectors(int rule_id) {
          return method.getDetectors(rule_id);
        }
      public VolumeRule createRule() {
        return method.createRule();
      }
    };
    
    ParameterizedRowMapper<VolumeRule> mapper = classUnderTest.getVolumeRuleMapper();
    
    rsControl.replay();
    methodControl.replay();
    
    VolumeRule result = mapper.mapRow(rs, 1);
    
    rsControl.verify();
    methodControl.verify();
    assertEquals(10, result.getRuleId());
    assertEquals(6, result.getInterval());
    assertEquals(15, result.getTimeout());
    assertEquals(false, result.isAscending());
    assertEquals(2.0, result.getElevationMin());
    assertEquals(10.0, result.getElevationMax());
    assertSame(sources, result.getSources());
  }
  
  public void testCreateRule() throws Exception {
    Catalog catalog = new Catalog();
    RuleUtilities utilities = new RuleUtilities();
    TimeoutManager manager = new TimeoutManager();
    
    classUnderTest = new VolumeRuleManager();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(utilities);
    classUnderTest.setTimeoutManager(manager);
    
    VolumeRule result = classUnderTest.createRule();
    
    assertSame(result.getCatalog(), catalog);
    assertSame(result.getRuleUtilities(), utilities);
    assertSame(result.getTimeoutManager(), manager);
  }
  
  public void testCreateRule_missingCatalog() throws Exception {
    RuleUtilities utilities = new RuleUtilities();
    TimeoutManager manager = new TimeoutManager();
    
    classUnderTest = new VolumeRuleManager();
    classUnderTest.setRuleUtilities(utilities);
    classUnderTest.setTimeoutManager(manager);

    try {
      classUnderTest.createRule();
      fail("Expected BeanInitializationException");
    } catch (BeanInitializationException e) {
      // pass
    }
  }
}
