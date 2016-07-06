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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.rules.RuleFilterManager;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.util.RuleUtilities;

/**
 * @author Anders Henja
 */
public class VolumeRuleManagerTest extends EasyMockSupport {
  private static interface ManagerMethods {
    public void storeSources(int rule_id, List<String> sources);
    public List<String> getSources(int rule_id);    
    public VolumeRule createRule();
    public void storeDetectors(int rule_id, List<String> detectors);
    public List<String> getDetectors(int rule_id);    
  };
  
  private VolumeRuleManager classUnderTest = null;
  private RuleFilterManager filterManager = null;
  private JdbcOperations jdbc = null;

  @Before
  public void setUp() throws Exception {
    jdbc = createMock(JdbcOperations.class);
    filterManager = createMock(RuleFilterManager.class);
    classUnderTest = new VolumeRuleManager();
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setFilterManager(filterManager);
  }

  @After
  public void tearDown() throws Exception {
    jdbc = null;
    classUnderTest = null;
  }

  @Test
  public void testDelete() throws Exception {
    final ManagerMethods methods = createMock(ManagerMethods.class);
    
    classUnderTest = new VolumeRuleManager() {
      protected void storeSources(int rule_id, List<String> sources) {
        methods.storeSources(rule_id, sources);
      }
      protected void storeDetectors(int rule_id, List<String> detectors) {
        methods.storeDetectors(rule_id, detectors);
      }      
    };
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setFilterManager(filterManager);
    
    methods.storeSources(13, null);
    methods.storeDetectors(13, null);
    
    expect(jdbc.update("delete from beast_volume_rules where rule_id=?",
        new Object[] { 13 })).andReturn(1);
    filterManager.deleteFilters(13);

    replayAll();
    
    classUnderTest.delete(13);

    verifyAll();
  }

  @Test
  public void testLoad() throws Exception {
    VolumeRule rule = new VolumeRule();
    HashMap<String, IFilter> filters = new HashMap<String, IFilter>();
    IFilter filter = createMock(IFilter.class);
    filters.put("match", filter);
    final RowMapper<VolumeRule> mapper = new RowMapper<VolumeRule>() {
      public VolumeRule mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };
    classUnderTest = new VolumeRuleManager() {
      protected RowMapper<VolumeRule> getVolumeRuleMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setFilterManager(filterManager);
    
    expect(jdbc.queryForObject("select * from beast_volume_rules where rule_id=?",
        mapper, new Object[] { 13 })).andReturn(rule);
    expect(filterManager.loadFilters(13)).andReturn(filters);
    replayAll();
    
    VolumeRule result = (VolumeRule) classUnderTest.load(13);
    
    verifyAll();
    assertSame(rule, result);
  }
  
  @Test
  public void testStore() throws Exception {
    final ManagerMethods methods = createMock(ManagerMethods.class);
    
    List<String> sources = new ArrayList<String>();
    List<String> detectors = new ArrayList<String>();
    
    VolumeRule rule = new VolumeRule();
    rule.setAscending(false);
    rule.setElevationMax(10.0);
    rule.setElevationMin(2.0);
    rule.setElevationAngles("1.0,2.0,3.0");
    rule.setInterval(6);
    rule.setTimeout(20);
    rule.setNominalTimeout(true);
    rule.setSources(sources);
    rule.setDetectors(detectors);
    
    expect(jdbc.update("insert into beast_volume_rules (rule_id, interval, timeout, nominal_timeout, " +
        "ascending, minelev, maxelev, elangles) values (?,?,?,?,?,?,?,?)", new Object[]{13, 6, 20, true, false, 2.0, 10.0, "1.0,2.0,3.0"})).andReturn(0);

    methods.storeSources(13, sources);
    methods.storeDetectors(13, detectors);
    filterManager.deleteFilters(13);
    
    classUnderTest = new VolumeRuleManager() {
      protected void storeSources(int rule_id, List<String> sources) {
        methods.storeSources(rule_id, sources);
      }
      protected void storeDetectors(int rule_id, List<String> detectors) {
        methods.storeDetectors(rule_id, detectors);
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setFilterManager(filterManager);
    
    replayAll();
    
    classUnderTest.store(13, rule);
    
    verifyAll();
    assertEquals(13, rule.getRuleId());
  }

  @Test
  public void testUpdate() throws Exception {
    final ManagerMethods methods = createMock(ManagerMethods.class);
    
    List<String> sources = new ArrayList<String>();
    List<String> detectors = new ArrayList<String>();
    
    VolumeRule rule = new VolumeRule();
    rule.setAscending(false);
    rule.setElevationMax(10.0);
    rule.setElevationMin(2.0);
    rule.setElevationAngles("1.0,2.0,3.0");
    rule.setInterval(6);
    rule.setTimeout(20);
    rule.setNominalTimeout(true);
    rule.setSources(sources);
    rule.setDetectors(detectors);
    
    expect(jdbc.update("update beast_volume_rules set interval=?, timeout=?, nominal_timeout=?, " +
        "ascending=?, minelev=?, maxelev=?, elangles=? where rule_id=?", new Object[]{6, 20, true, false, 2.0, 10.0, "1.0,2.0,3.0", 13}))
        .andReturn(1);
    methods.storeSources(13, sources);
    methods.storeDetectors(13, detectors);
    filterManager.deleteFilters(13);
    
    classUnderTest = new VolumeRuleManager() {
      protected void storeSources(int rule_id, List<String> sources) {
        methods.storeSources(rule_id, sources);
      }
      protected void storeDetectors(int rule_id, List<String> sources) {
        methods.storeDetectors(rule_id, sources);
      }      
    };
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setFilterManager(filterManager);
    
    replayAll();
    
    classUnderTest.update(13, rule);
    
    verifyAll();
    assertEquals(13, rule.getRuleId());
  }
  
  @Test
  public void testStoreSources() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("A");
    sources.add("B");
    
    expect(jdbc.update("delete from beast_volume_sources where rule_id=?",
        new Object[]{13})).andReturn(1);
    
    expect(jdbc.update(
        "insert into beast_volume_sources (rule_id, source)"+
        " values (?,?)", new Object[]{13, "A"})).andReturn(1);

    expect(jdbc.update(
        "insert into beast_volume_sources (rule_id, source)"+
        " values (?,?)", new Object[]{13, "B"})).andReturn(1);
    
    replayAll();
    
    classUnderTest.storeSources(13, sources);
    
    verifyAll();
  }
  
  @Test
  public void testStoreSources_nullSources() throws Exception {
    expect(jdbc.update("delete from beast_volume_sources where rule_id=?",
        new Object[]{13})).andReturn(1);
    
    replayAll();
    
    classUnderTest.storeSources(13, null);
    
    verifyAll();
  }
  
  @Test
  public void testGetSources() throws Exception {
    List<String> sources = new ArrayList<String>();
    
    final RowMapper<String> mapper = new RowMapper<String>() {
      public String mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };
    classUnderTest = new VolumeRuleManager() {
      protected RowMapper<String> getSourceMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    
    expect(jdbc.query("select source from beast_volume_sources where rule_id=?",
        mapper,
        new Object[]{13})).andReturn(sources);
    
    replayAll();
    
    List<String> result = classUnderTest.getSources(13);
    
    verifyAll();
    assertSame(sources, result);
  }
  
  @Test
  public void testGetVolumeRuleMapper() throws Exception {
    ResultSet rs = createMock(ResultSet.class);
    List<String> sources = new ArrayList<String>();
    List<String> detectors = new ArrayList<String>();
    final ManagerMethods method = createMock(ManagerMethods.class);
    VolumeRule volumeRule = new VolumeRule();
    
    expect(method.createRule()).andReturn(volumeRule);
    expect(rs.getInt("rule_id")).andReturn(10);
    expect(rs.getInt("interval")).andReturn(6);
    expect(rs.getInt("timeout")).andReturn(15);
    expect(rs.getBoolean("nominal_timeout")).andReturn(true);
    expect(rs.getBoolean("ascending")).andReturn(false);
    expect(rs.getDouble("minelev")).andReturn(2.0);
    expect(rs.getDouble("maxelev")).andReturn(10.0);
    expect(rs.getString("elangles")).andReturn("1.0,2.0,3.0");
    expect(method.getSources(10)).andReturn(sources);
    expect(method.getDetectors(10)).andReturn(detectors);
    
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
    
    RowMapper<VolumeRule> mapper = classUnderTest.getVolumeRuleMapper();
    
    replayAll();
    
    VolumeRule result = mapper.mapRow(rs, 1);

    verifyAll();
    assertEquals(10, result.getRuleId());
    assertEquals(6, result.getInterval());
    assertEquals(15, result.getTimeout());
    assertEquals(true, result.isNominalTimeout());
    assertEquals(false, result.isAscending());
    assertEquals(2.0, result.getElevationMin(), 4);
    assertEquals(10.0, result.getElevationMax(), 4);
    assertEquals("1.0,2.0,3.0", result.getElevationAngles());
    assertSame(sources, result.getSources());
  }
  
  @Test
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
  
  @Test
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
