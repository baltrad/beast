/* --------------------------------------------------------------------
Copyright (C) 2009-2013 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.rules.wrwp;

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
import eu.baltrad.beast.rules.util.IRuleUtilities;
import eu.baltrad.beast.rules.util.RuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class WrwpRuleManagerTest extends EasyMockSupport {
  static interface Methods {
    public void updateSources(int rule_id, List<String> sources);
    public List<String> getSources(int rule_id);
    public RowMapper<String> getSourceMapper();
  }
  
  private JdbcOperations jdbc = null;
  private WrwpRuleManager classUnderTest = null;
  private RuleFilterManager filterManager = null;
  private Methods methods = null;
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    jdbc = createMock(JdbcOperations.class);
    filterManager = createMock(RuleFilterManager.class);
    methods = createMock(Methods.class);
    
    classUnderTest = new WrwpRuleManager() {
      @Override
      protected void updateSources(int rule_id, List<String> sources) {
        methods.updateSources(rule_id, sources);
      }
      @Override
      protected List<String> getSources(int rule_id) {
        return methods.getSources(rule_id);
      }
      @Override
      protected RowMapper<String> getSourceMapper() {
        return methods.getSourceMapper();
      }
    };    
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setFilterManager(filterManager);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    jdbc = null;
    methods = null;
    classUnderTest = null;
  }

  @Test
  public void test_store() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("seang");
    sources.add("searl");
    WrwpRule rule = new WrwpRule();
    rule.setInterval(300);
    rule.setMaxheight(5000);
    rule.setMindistance(1000);
    rule.setMaxdistance(10000);
    rule.setMinelevationangle(1.5);
    rule.setMaxelevationangle(43.0);
    rule.setConditionalMinElevationAngle(11.5);
    rule.setHeightThreshold(3000.0);
    rule.setMinNyquistInterval(7.5);
    rule.setNumberGapBins(9);
    rule.setMinNumberGapSamples(7);
    rule.setMaxNumberStandardDeviations(3);
    rule.setMaxVelocityDeviation(12.0);
    rule.setWrwpProcessingMethod("KNMI");
    rule.setMinvelocitythreshold(10.0);
    rule.setMaxvelocitythreshold(46.0);
    rule.setMinsamplesizereflectivity(30);
    rule.setMinsamplesizewind(29);
    rule.setSources(sources);
    rule.setFields("dd,aa");

    expect(jdbc.update("INSERT INTO beast_wrwp_rules " + 
    		"(rule_id, interval, maxheight, mindistance, maxdistance, minelangle, maxelangle, mincondelangle, heightthres, minnyquistinterval, ngapbins, minngap, maxnstd, maxvdiff, wrwpmethod, minvelocitythresh, maxvelocitythresh, minsamplesizereflectivity, minsamplesizewind, fields) " +
        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
        new Object[]{10, 300, 5000, 1000, 10000, 1.5, 43.0, 11.5, 3000.0, 7.5, 9, 7, 3, 12.0, "KNMI", 10.0, 46.0, 30, 29, "dd,aa"})).andReturn(1);
    
    filterManager.deleteFilters(10);
    
    methods.updateSources(10, sources);
    
    replayAll();
    
    classUnderTest.store(10, rule);
    
    verifyAll();
    assertEquals(10, rule.getRuleId());
  }
  
  @Test
  public void test_load() throws Exception {
    WrwpRule rule = new WrwpRule();
    
    HashMap<String, IFilter> filters = new HashMap<String, IFilter>();
    IFilter filter = createMock(IFilter.class);
    filters.put("match", filter);
    
    final RowMapper<WrwpRule> mapper = new RowMapper<WrwpRule>() {
      public WrwpRule mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };

    classUnderTest = new WrwpRuleManager() {
      @Override
      protected RowMapper<WrwpRule> getWrwpRuleMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setFilterManager(filterManager);
    
    expect(jdbc.queryForObject("SELECT * FROM beast_wrwp_rules WHERE rule_id=?",
        mapper, new Object[]{3})).andReturn(rule);
    
    expect(filterManager.loadFilters(3)).andReturn(filters);
    
    replayAll();
    
    WrwpRule result = (WrwpRule)classUnderTest.load(3);
    
    verifyAll();
    assertSame(rule, result);    
  }
  
  @Test
  public void test_update() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("seang");
    sources.add("searl");
    WrwpRule rule = new WrwpRule();
    rule.setInterval(300);
    rule.setMaxheight(5000);
    rule.setMindistance(1000);
    rule.setMaxdistance(10000);
    rule.setMinelevationangle(1.5);
    rule.setMaxelevationangle(43.0);
    rule.setConditionalMinElevationAngle(11.5);
    rule.setHeightThreshold(3000.0);
    rule.setMinNyquistInterval(7.5);
    rule.setNumberGapBins(9);
    rule.setMinNumberGapSamples(7);
    rule.setMaxNumberStandardDeviations(3);
    rule.setMaxVelocityDeviation(12.0);
    rule.setWrwpProcessingMethod("KNMI");
    rule.setMinvelocitythreshold(10.0);
    rule.setMaxvelocitythreshold(45.0);
    rule.setMinsamplesizereflectivity(30);
    rule.setMinsamplesizewind(29);
    rule.setFields("aa,dd");
    rule.setSources(sources);

    expect(jdbc.update(
        "UPDATE beast_wrwp_rules SET interval=?, maxheight=?, mindistance=?," +
        " maxdistance=?, minelangle=?, maxelangle=?, mincondelangle=?, heightthres=?, minnyquistinterval=?, ngapbins=?, minngap=?, maxnstd=?, maxvdiff=?, wrwpmethod=?, minvelocitythresh=?, maxvelocitythresh=?, minsamplesizereflectivity=?, minsamplesizewind=?, fields=? WHERE rule_id=?",
        new Object[]{300, 5000, 1000, 10000, 1.5, 43.0, 11.5, 3000.0, 7.5, 9, 7, 3, 12.0, "KNMI", 10.0, 45.0, 30, 29, "aa,dd", 12})).andReturn(1);
    methods.updateSources(12, sources);
    
    filterManager.deleteFilters(12);
    
    replayAll();
    
    classUnderTest.update(12, rule);
    
    verifyAll();
    assertEquals(12, rule.getRuleId());
  }
  
  @Test
  public void test_delete() throws Exception {
    methods.updateSources(12, null);
    
    expect(jdbc.update("DELETE FROM beast_wrwp_rules WHERE rule_id=?",
      new Object[]{12})).andReturn(0);
    
    filterManager.deleteFilters(12);
      
    replayAll();
      
    classUnderTest.delete(12);
     
    verifyAll();
  }
  
  @Test
  public void test_createRule() {
    Catalog catalog = new Catalog();
    RuleUtilities utils = new RuleUtilities();
    
    classUnderTest = new WrwpRuleManager();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(utils);
    
    WrwpRule result = (WrwpRule)classUnderTest.createRule();
    assertSame(catalog, result.getCatalog());
    assertSame(utils, result.getRuleUtilities());
  }
  
  @Test
  public void test_updateSources() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("seang");
    sources.add("searl");
    
    expect(jdbc.update("DELETE FROM beast_wrwp_sources WHERE rule_id=?",
        new Object[]{13})).andReturn(2);
    expect(jdbc.update("INSERT INTO beast_wrwp_sources (rule_id, source) VALUES (?,?)",
        new Object[]{13, "seang"})).andReturn(1);
    expect(jdbc.update("INSERT INTO beast_wrwp_sources (rule_id, source) VALUES (?,?)",
        new Object[]{13, "searl"})).andReturn(1);
    
    classUnderTest = new WrwpRuleManager();
    classUnderTest.setJdbcTemplate(jdbc);
    
    replayAll();
    
    classUnderTest.updateSources(13, sources);
    
    verifyAll();
  }
  
  @Test
  public void test_updateSources_nullSource() throws Exception {
    expect(jdbc.update("DELETE FROM beast_wrwp_sources WHERE rule_id=?",
        new Object[]{13})).andReturn(2);
    
    classUnderTest = new WrwpRuleManager();
    classUnderTest.setJdbcTemplate(jdbc);
    
    replayAll();
    
    classUnderTest.updateSources(13, null);
    
    verifyAll();
  }
  
  @Test
  public void test_getSources() throws Exception {
    List<String> sources = new ArrayList<String>();
    RowMapper<String> mapper = new RowMapper<String>() {
      @Override
      public String mapRow(ResultSet rs, int arg1) throws SQLException {
        return null;
      }
    };
    
    classUnderTest = new WrwpRuleManager() {
      @Override
      protected RowMapper<String> getSourceMapper() {
        return methods.getSourceMapper();
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    
    expect(methods.getSourceMapper()).andReturn(mapper);
    
    expect(jdbc.query("SELECT source FROM beast_wrwp_sources WHERE rule_id=?", 
        mapper,
        new Object[]{3})).andReturn(sources);
    
    replayAll();
    
    List<String> result = classUnderTest.getSources(3);
    
    verifyAll();
    assertSame(sources, result);
  }
  
  @Test
  public void test_wrwp_rule_mapper() throws Exception {
    IRuleUtilities utils = createMock(IRuleUtilities.class);
    Catalog cat = createMock(Catalog.class);
    ResultSet rs = createMock(ResultSet.class);

    List<String> sources = new ArrayList<String>();
    
    expect(rs.getInt("rule_id")).andReturn(3);
    expect(rs.getInt("interval")).andReturn(300);
    expect(rs.getInt("maxheight")).andReturn(5000);
    expect(rs.getInt("mindistance")).andReturn(1000);
    expect(rs.getInt("maxdistance")).andReturn(10000);
    expect(rs.getDouble("minelangle")).andReturn(4.5);
    expect(rs.getDouble("maxelangle")).andReturn(43.0);
    expect(rs.getDouble("mincondelangle")).andReturn(11.5);
    expect(rs.getDouble("heightthres")).andReturn(3000.0);
    expect(rs.getDouble("minnyquistinterval")).andReturn(7.5);
    expect(rs.getInt("ngapbins")).andReturn(9);
    expect(rs.getInt("minngap")).andReturn(7);
    expect(rs.getInt("maxnstd")).andReturn(3);
    expect(rs.getDouble("maxvdiff")).andReturn(12.0);
    expect(rs.getString("wrwpmethod")).andReturn("KNMI");
    expect(rs.getDouble("minvelocitythresh")).andReturn(1.5);
    expect(rs.getDouble("maxvelocitythresh")).andReturn(50.0);
    expect(rs.getInt("minsamplesizereflectivity")).andReturn(30);
    expect(rs.getInt("minsamplesizewind")).andReturn(29);
    
    expect(rs.getString("fields")).andReturn("aa,dd");
    
    expect(methods.getSources(3)).andReturn(sources);
    
    classUnderTest.setCatalog(cat);
    classUnderTest.setRuleUtilities(utils);
    
    replayAll();
    
    WrwpRule result = classUnderTest.getWrwpRuleMapper().mapRow(rs, 1); 
    
    verifyAll();
    assertEquals(3, result.getRuleId());
    assertEquals(300, result.getInterval());
    assertEquals(5000, result.getMaxheight());
    assertEquals(1000, result.getMindistance());
    assertEquals(10000, result.getMaxdistance());
    assertEquals(4.5, result.getMinelevationangle(), 4);
    assertEquals(43.0, result.getMaxelevationangle(), 4);
    assertEquals(11.5, result.getConditionalMinElevationAngle(), 4);
    assertEquals(3000.0, result.getHeightThreshold(), 4);
    assertEquals(7.5, result.getMinNyquistInterval(), 4);
    assertEquals(9, result.getNumberGapBins());
    assertEquals(7, result.getMinNumberGapSamples());
    assertEquals(3, result.getMaxNumberStandardDeviations());
    assertEquals(12.0, result.getMaxVelocityDeviation(), 4);
    assertEquals("KNMI", result.getWrwpProcessingMethod());
    assertEquals(1.5, result.getMinvelocitythreshold(), 4);
    assertEquals(50.0, result.getMaxvelocitythreshold(), 4);
    assertEquals(30, result.getMinsamplesizereflectivity());
    assertEquals(29, result.getMinsamplesizewind());
    assertEquals("aa,dd", result.getFieldsAsStr());
    assertSame(sources, result.getSources());
    assertSame(cat, result.getCatalog());
    assertSame(utils, result.getRuleUtilities());
  }

  @Test
  public void test_wrwp_rule_mapper_fields_null() throws Exception {
    IRuleUtilities utils = createMock(IRuleUtilities.class);
    Catalog cat = createMock(Catalog.class);
    ResultSet rs = createMock(ResultSet.class);

    List<String> sources = new ArrayList<String>();
    
    expect(rs.getInt("rule_id")).andReturn(3);
    expect(rs.getInt("interval")).andReturn(300);
    expect(rs.getInt("maxheight")).andReturn(5000);
    expect(rs.getInt("mindistance")).andReturn(1000);
    expect(rs.getInt("maxdistance")).andReturn(10000);
    expect(rs.getDouble("minelangle")).andReturn(4.5);
    expect(rs.getDouble("maxelangle")).andReturn(43.0);
    expect(rs.getDouble("mincondelangle")).andReturn(11.5);
    expect(rs.getDouble("heightthres")).andReturn(3000.0);
    expect(rs.getDouble("minnyquistinterval")).andReturn(7.5);
    expect(rs.getInt("ngapbins")).andReturn(9);
    expect(rs.getInt("minngap")).andReturn(7);
    expect(rs.getInt("maxnstd")).andReturn(3);
    expect(rs.getDouble("maxvdiff")).andReturn(12.0);
    expect(rs.getString("wrwpmethod")).andReturn("KNMI");
    expect(rs.getDouble("minvelocitythresh")).andReturn(1.5);
    expect(rs.getDouble("maxvelocitythresh")).andReturn(50.0);
    expect(rs.getInt("minsamplesizereflectivity")).andReturn(30);
    expect(rs.getInt("minsamplesizewind")).andReturn(29);
    expect(rs.getString("fields")).andReturn(null);
    
    expect(methods.getSources(3)).andReturn(sources);
    
    classUnderTest.setCatalog(cat);
    classUnderTest.setRuleUtilities(utils);
    
    replayAll();
    
    WrwpRule result = classUnderTest.getWrwpRuleMapper().mapRow(rs, 1); 
    
    verifyAll();
    assertEquals(3, result.getRuleId());
    assertEquals(300, result.getInterval());
    assertEquals(5000, result.getMaxheight());
    assertEquals(1000, result.getMindistance());
    assertEquals(10000, result.getMaxdistance());
    assertEquals(4.5, result.getMinelevationangle(), 4);
    assertEquals(43.0, result.getMaxelevationangle(), 4);
    assertEquals(11.5, result.getConditionalMinElevationAngle(), 4);
    assertEquals(3000.0, result.getHeightThreshold(), 4);
    assertEquals(7.5, result.getMinNyquistInterval(), 4);
    assertEquals(9, result.getNumberGapBins());
    assertEquals(7, result.getMinNumberGapSamples());
    assertEquals(3, result.getMaxNumberStandardDeviations());
    assertEquals(12.0, result.getMaxVelocityDeviation(), 4);
    assertEquals("KNMI", result.getWrwpProcessingMethod());
    assertEquals(1.5, result.getMinvelocitythreshold(), 4);
    assertEquals(50.0, result.getMaxvelocitythreshold(), 4);
    assertEquals(30, result.getMinsamplesizereflectivity());
    assertEquals(29, result.getMinsamplesizewind());
    assertEquals("", result.getFieldsAsStr());
    assertSame(sources, result.getSources());
    assertSame(cat, result.getCatalog());
    assertSame(utils, result.getRuleUtilities());
  }
  
  @Test
  public void test_source_mapper() throws Exception {
    ResultSet rs = createMock(ResultSet.class);
   
    expect(rs.getString("source")).andReturn("seang");
    
    classUnderTest = new WrwpRuleManager();
    
    replayAll();
    
    String result = classUnderTest.getSourceMapper().mapRow(rs, 0);
    
    verifyAll();
    assertEquals("seang", result);
  }
}
