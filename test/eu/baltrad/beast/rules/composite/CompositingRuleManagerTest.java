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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
public class CompositingRuleManagerTest extends EasyMockSupport {
  private static interface ManagerMethods {
    public CompositingRule createRule();
    public void storeSources(int rule_id, List<String> sources);
    public List<String> getSources(int rule_id);
    public void storeDetectors(int rule_id, List<String> detectors);
    public List<String> getDetectors(int rule_id);
  };

  private CompositingRuleManager classUnderTest = null;
  private RuleFilterManager filterManager = null;
  private JdbcOperations jdbc = null;
  
  @Before
  public void setUp() throws Exception {
    jdbc = createMock(JdbcOperations.class);
    filterManager = createMock(RuleFilterManager.class);
    classUnderTest = new CompositingRuleManager();
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
    
    classUnderTest = new CompositingRuleManager() {
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
    
    expect(jdbc.update("delete from beast_composite_rules where rule_id=?",
        new Object[]{13})).andReturn(0);
    filterManager.deleteFilters(13);
    
    replayAll();
    
    classUnderTest.delete(13);
    
    verifyAll();
  }

  @Test
  public void testLoad() throws Exception {
    CompositingRule rule = new CompositingRule();
    HashMap<String, IFilter> filters = new HashMap<String, IFilter>();
    IFilter filter = createMock(IFilter.class);
    filters.put("match", filter);
    final RowMapper<CompositingRule> mapper = new RowMapper<CompositingRule>() {
      public CompositingRule mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };
    classUnderTest = new CompositingRuleManager() {
      protected RowMapper<CompositingRule> getCompsiteRuleMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setFilterManager(filterManager);
    
    expect(jdbc.queryForObject("select * from beast_composite_rules where rule_id=?",
        mapper,
        new Object[]{13})).andReturn(rule);
    expect(filterManager.loadFilters(13)).andReturn(filters);
    
    replayAll();
    
    CompositingRule result = (CompositingRule)classUnderTest.load(13);
    
    verifyAll();
    assertSame(rule, result);
  }
  
  @Test
  public void testStore() throws Exception {
    final ManagerMethods methods = createMock(ManagerMethods.class);
    
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
    rule.setApplyGRA(true);
    rule.setZR_A(10.0);
    rule.setZR_b(5.0);
    rule.setIgnoreMalfunc(true);
    rule.setCtFilter(true);
    rule.setQitotalField("se.baltrad.something");
    rule.setQuantity("VRAD");
    rule.setNominalTimeout(true);
    rule.setQualityControlMode(CompositingRule.QualityControlMode_ANALYZE);
    rule.setReprocessQuality(false);
    
    expect(jdbc.update(
        "insert into beast_composite_rules (rule_id, area, interval, timeout, byscan, selection_method, method, prodpar, applygra, ZR_A, ZR_b, ignore_malfunc, ctfilter, qitotal_field, quantity, nominal_timeout, qc_mode, reprocess_quality)"+
        " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new Object[]{13, "seang", 12, 20, true, CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL, CompositingRule.PPI, "0.5", true, 10.0, 5.0, true, true, "se.baltrad.something", "VRAD", true, CompositingRule.QualityControlMode_ANALYZE, false}))
          .andReturn(0);
    
    methods.storeSources(13, sources);
    methods.storeDetectors(13, detectors);
    filterManager.deleteFilters(13);
    
    classUnderTest = new CompositingRuleManager() {
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
    rule.setApplyGRA(true);
    rule.setZR_A(10.0);
    rule.setZR_b(5.0);
    rule.setIgnoreMalfunc(true);
    rule.setCtFilter(true);
    rule.setQitotalField("se.baltrad.something");
    rule.setQuantity("NOOP");
    rule.setNominalTimeout(true);
    rule.setQualityControlMode(CompositingRule.QualityControlMode_ANALYZE);
    rule.setReprocessQuality(true);
    
    expect(jdbc.update("update beast_composite_rules set area=?, interval=?, timeout=?, byscan=?, selection_method=?, method=?, prodpar=?, applygra=?, ZR_A=?, ZR_b=?, ignore_malfunc=?, ctfilter=?, qitotal_field=?, quantity=?, nominal_timeout=?, qc_mode=?, reprocess_quality=? where rule_id=?",
        new Object[]{"seang", 12, 20, true, CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL, CompositingRule.PPI, "0.5", true, 10.0, 5.0, true, true, "se.baltrad.something", "NOOP", true, CompositingRule.QualityControlMode_ANALYZE, true, 13}))
        .andReturn(0);
    
    methods.storeSources(13, sources);
    methods.storeDetectors(13, detectors);
    filterManager.deleteFilters(13);
    
    classUnderTest = new CompositingRuleManager() {
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
    
    expect(jdbc.update("delete from beast_composite_sources where rule_id=?",
        new Object[]{13})).andReturn(0);
    
    expect(jdbc.update(
        "insert into beast_composite_sources (rule_id, source)"+
        " values (?,?)", new Object[]{13, "A"})).andReturn(0);

    expect(jdbc.update(
        "insert into beast_composite_sources (rule_id, source)"+
        " values (?,?)", new Object[]{13, "B"})).andReturn(0);
    
    replayAll();
    
    classUnderTest.storeSources(13, sources);
    
    verifyAll();
  }
  
  @Test
  public void testStoreSources_nullSources() throws Exception {
    expect(jdbc.update("delete from beast_composite_sources where rule_id=?",
        new Object[]{13})).andReturn(0);
    
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
    classUnderTest = new CompositingRuleManager() {
      protected RowMapper<String> getSourceMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    
    expect(jdbc.query("select source from beast_composite_sources where rule_id=?",
        mapper,
        new Object[]{13})).andReturn(sources);
    
    replayAll();
    
    List<String> result = classUnderTest.getSources(13);
    
    verifyAll();
    assertSame(sources, result);
  }
  
  @Test
  public void testStoreDetectors() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("A");
    sources.add("B");
    
    expect(jdbc.update("delete from beast_composite_detectors where rule_id=?",
        new Object[]{13})).andReturn(0);
    
    expect(jdbc.update(
        "insert into beast_composite_detectors (rule_id, name)"+
        " values (?,?)", new Object[]{13, "A"})).andReturn(0);

    expect(jdbc.update(
        "insert into beast_composite_detectors (rule_id, name)"+
        " values (?,?)", new Object[]{13, "B"})).andReturn(0);
    
    replayAll();
    
    classUnderTest.storeDetectors(13, sources);
    
    verifyAll();
  }

  @Test
  public void testStoreDetectors_null() throws Exception {
    expect(jdbc.update("delete from beast_composite_detectors where rule_id=?",
        new Object[]{13})).andReturn(0);
    
    replayAll();
    
    classUnderTest.storeDetectors(13, null);
    
    verifyAll();
  }
  
  @Test
  public void testGetDetectors() throws Exception {
    List<String> detectors = new ArrayList<String>();
    
    final RowMapper<String> mapper = new RowMapper<String>() {
      public String mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };
    classUnderTest = new CompositingRuleManager() {
      protected RowMapper<String> getDetectorMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    
    expect(jdbc.query("select name from beast_composite_detectors where rule_id=?",
        mapper,
        new Object[]{13})).andReturn(detectors);
    
    replayAll();
    
    List<String> result = classUnderTest.getDetectors(13);
    
    verifyAll();
    assertSame(detectors, result);
  }
  
  @Test
  public void testGetCompositeRuleMapper() throws Exception {
    ResultSet rs = createMock(ResultSet.class);
    List<String> sources = new ArrayList<String>();
    List<String> detectors = new ArrayList<String>();
    
    final ManagerMethods method = createMock(ManagerMethods.class);
    CompositingRule arule = new CompositingRule();
    
    expect(method.createRule()).andReturn(arule);
    
    expect(rs.getInt("rule_id")).andReturn(10);
    expect(rs.getString("area")).andReturn("abc");
    expect(rs.getInt("interval")).andReturn(15);
    expect(rs.getInt("timeout")).andReturn(20);
    expect(rs.getBoolean("byscan")).andReturn(true);
    expect(rs.getInt("selection_method")).andReturn(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    expect(rs.getString("method")).andReturn(CompositingRule.PPI);
    expect(rs.getString("prodpar")).andReturn("0.5");
    expect(rs.getBoolean("applygra")).andReturn(true);
    expect(rs.getDouble("ZR_A")).andReturn(10.0);
    expect(rs.getDouble("ZR_b")).andReturn(5.0);
    expect(rs.getBoolean("ignore_malfunc")).andReturn(true);
    expect(rs.getBoolean("ctfilter")).andReturn(true);
    expect(rs.getString("qitotal_field")).andReturn("se.baltrad.something");
    expect(rs.getString("quantity")).andReturn("VRAD");
    expect(rs.getBoolean("nominal_timeout")).andReturn(true);
    expect(rs.getInt("qc_mode")).andReturn(1);
    expect(rs.getBoolean("reprocess_quality")).andReturn(true);

    expect(method.getSources(10)).andReturn(sources);
    expect(method.getDetectors(10)).andReturn(detectors);
    
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
    
    RowMapper<CompositingRule> mapper = classUnderTest.getCompsiteRuleMapper();

    replayAll();
    
    CompositingRule result = mapper.mapRow(rs, 1);

    verifyAll();
    assertEquals("abc", result.getArea());
    assertEquals(15, result.getInterval());
    assertSame(sources, result.getSources());
    assertEquals(10, result.getRuleId());
    assertEquals(20, result.getTimeout());
    assertEquals(true, result.isScanBased());
    assertEquals(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL, result.getSelectionMethod());
    assertEquals(CompositingRule.PPI, result.getMethod());
    assertEquals("0.5", result.getProdpar());
    assertEquals(true, result.isApplyGRA());
    assertEquals(10.0, result.getZR_A(), 4);
    assertEquals(5.0, result.getZR_b(), 4);
    assertEquals(true, result.isIgnoreMalfunc());
    assertEquals(true, result.isCtFilter());
    assertEquals("se.baltrad.something", result.getQitotalField());
    assertEquals("VRAD", result.getQuantity());
    assertEquals(true, result.isNominalTimeout());
    assertEquals(CompositingRule.QualityControlMode_ANALYZE, result.getQualityControlMode());
    assertEquals(true, result.isReprocessQuality());
  }  
  
  @Test
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
