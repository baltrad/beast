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

package eu.baltrad.beast.rules.site2d;

import static org.easymock.EasyMock.expect;
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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.rules.RuleFilterManager;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class Site2DRuleManagerTest extends EasyMockSupport {
  private static interface Methods {
    public Site2DRule createRule();
    public void storeSources(int rule_id, List<String> sources);
    public List<String> getSources(int rule_id);
    public void storeDetectors(int rule_id, List<String> detectors);
    public List<String> getDetectors(int rule_id);
  };
  
  private JdbcOperations template = null;
  private IRuleUtilities ruleUtilities = null;
  private Catalog catalog = null;
  private Methods methods = null;
  private RuleFilterManager filterManager = null;
  private Site2DRuleManager classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new Site2DRuleManager() {
      @Override
      public Site2DRule createRule() {
        return methods.createRule();
      }
      @Override
      protected void storeSources(int rule_id, List<String> sources) {
        methods.storeSources(rule_id, sources);
      }
      @Override
      protected List<String> getSources(int rule_id) {
        return methods.getSources(rule_id);
      }
      @Override
      protected void storeDetectors(int rule_id, List<String> detectors) {
        methods.storeDetectors(rule_id, detectors);
      }
      @Override
      protected List<String> getDetectors(int rule_id) {
        return methods.getDetectors(rule_id);
      }
    };
    methods = createMock(Methods.class);
    template = createMock(JdbcOperations.class);
    ruleUtilities = createMock(IRuleUtilities.class);
    catalog = createMock(Catalog.class);
    filterManager = createMock(RuleFilterManager.class);
    classUnderTest.setTemplate(template);
    classUnderTest.setRuleUtilities(ruleUtilities);
    classUnderTest.setCatalog(catalog);
    classUnderTest.setFilterManager(filterManager);
  }
  
  @After
  public void tearDown() throws Exception {
    catalog = null;
    ruleUtilities = null;
    template = null;
    classUnderTest = null;
  }
  
  @Test
  public void store() {
    int ruleId = 11;
    Site2DRule rule = new Site2DRule();
    List<String> detectors = new ArrayList<String>();
    List<String> sources = new ArrayList<String>();
    rule.setApplyGRA(true);
    rule.setArea("nisse");
    rule.setCtFilter(false);
    rule.setDetectors(detectors);
    rule.setIgnoreMalfunc(true);
    rule.setInterval(15);
    rule.setMethod(Site2DRule.CAPPI);
    rule.setProdpar("10,10");
    rule.setScanBased(true);
    rule.setSources(sources);
    rule.setZR_A(1.1);
    rule.setZR_b(0.1);
    rule.setPcsid("apcs");
    rule.setXscale(3000.0);
    rule.setYscale(1000.0);
    
    classUnderTest.setFilterManager(filterManager);

    expect(template.update("INSERT INTO beast_site2d_rules " +
      "(rule_id, area, interval, byscan, method, prodpar, applygra, ZR_A, ZR_b, ignore_malfunc, ctfilter, pcsid, xscale, yscale) " +
      "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", ruleId, "nisse", 15, true, "cappi", "10,10", true, 1.1, 0.1, true, false, "apcs", 3000.0, 1000.0)).andReturn(0);
    filterManager.deleteFilters(ruleId);
    
    methods.storeSources(ruleId, sources);
    
    methods.storeDetectors(ruleId, detectors);
    
    replayAll();
    
    classUnderTest.store(ruleId, rule);
    
    verifyAll();
  }

  @Test
  public void store_updateFailure() {
    Site2DRule rule = new Site2DRule();
    List<String> detectors = new ArrayList<String>();
    List<String> sources = new ArrayList<String>();
    rule.setApplyGRA(true);
    rule.setArea("nisse");
    rule.setCtFilter(false);
    rule.setDetectors(detectors);
    rule.setIgnoreMalfunc(true);
    rule.setInterval(15);
    rule.setMethod(Site2DRule.CAPPI);
    rule.setProdpar("10,10");
    rule.setScanBased(true);
    rule.setSources(sources);
    rule.setZR_A(1.1);
    rule.setZR_b(0.1);
    rule.setPcsid("apcs");
    rule.setXscale(3000.0);
    rule.setYscale(1000.0);

    expect(template.update("INSERT INTO beast_site2d_rules " +
      "(rule_id, area, interval, byscan, method, prodpar, applygra, ZR_A, ZR_b, ignore_malfunc, ctfilter, pcsid, xscale, yscale) " +
      "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 11, "nisse", 15, true, "cappi", "10,10", true, 1.1, 0.1, true, false, "apcs", 3000.0, 1000.0)).andThrow(new DataAccessException("X"){
        private static final long serialVersionUID = 1L;});
    
    replayAll();

    try {
      classUnderTest.store(11, rule);
      fail("Expected DataAccessException");
    } catch (DataAccessException e) {
      //pass
    }
    
    verifyAll();
  }
  
  @Test
  public void update() {
    int ruleId = 11;
    Site2DRule rule = new Site2DRule();
    List<String> detectors = new ArrayList<String>();
    List<String> sources = new ArrayList<String>();
    rule.setApplyGRA(true);
    rule.setArea("nisse");
    rule.setCtFilter(false);
    rule.setDetectors(detectors);
    rule.setIgnoreMalfunc(true);
    rule.setInterval(15);
    rule.setMethod(Site2DRule.CAPPI);
    rule.setProdpar("10,10");
    rule.setScanBased(true);
    rule.setSources(sources);
    rule.setZR_A(1.1);
    rule.setZR_b(0.1);
    rule.setPcsid("apcs");
    rule.setXscale(3000.0);
    rule.setYscale(1000.0);
    
    classUnderTest.setFilterManager(filterManager);

    expect(template.update("UPDATE beast_site2d_rules" +
      " SET area=?, interval=?, byscan=?, method=?, prodpar=?, applygra=?, ZR_A=?, ZR_b=?, ignore_malfunc=?, ctfilter=?, pcsid=?, xscale=?, yscale=?" +
      " WHERE rule_id=?", new Object[]{"nisse", 15, true, "cappi", "10,10", true, 1.1, 0.1, true, false, "apcs", 3000.0, 1000.0, ruleId})).andReturn(1);
    filterManager.deleteFilters(ruleId);
    
    methods.storeSources(ruleId, sources);
    
    methods.storeDetectors(ruleId, detectors);
    
    replayAll();
    
    classUnderTest.update(ruleId, rule);
    
    verifyAll();
  }
  
  @Test
  public void delete() {
    int ruleId = 13;
    methods.storeSources(ruleId, null);
    methods.storeDetectors(ruleId, null);
    
    classUnderTest.setFilterManager(filterManager);
    
    expect(template.update("delete from beast_site2d_rules where rule_id=?", new Object[]{ruleId})).andReturn(0);
    filterManager.deleteFilters(ruleId);
    
    replayAll();
    
    classUnderTest.delete(ruleId);
    
    verifyAll();
  }
  
  @Test
  public void load() {
    int ruleId = 15;
    Site2DRule rule = new Site2DRule();
    HashMap<String, IFilter> filters = new HashMap<String, IFilter>();
    IFilter filter = createMock(IFilter.class);
    filters.put("match", filter);
    final RowMapper<Site2DRule> mapper = new RowMapper<Site2DRule>() {
      public Site2DRule mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };
    classUnderTest = new Site2DRuleManager() {
      @Override
      protected RowMapper<Site2DRule> getSite2DRuleMapper() {
        return mapper;
      }
    };
    classUnderTest.setTemplate(template);
    classUnderTest.setFilterManager(filterManager);
    
    expect(template.queryForObject("select * from beast_site2d_rules where rule_id=?",
        mapper,
        new Object[]{ruleId})).andReturn(rule);
    expect(filterManager.loadFilters(ruleId)).andReturn(filters);
    
    replayAll();
    
    Site2DRule result = (Site2DRule)classUnderTest.load(ruleId);
    
    verifyAll();
    assertSame(rule, result);    
  }
  
  @Test
  public void testStoreSources() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("A");
    sources.add("B");
    
    expect(template.update("delete from beast_site2d_sources where rule_id=?",
        new Object[]{13})).andReturn(0);
    
    expect(template.update(
        "insert into beast_site2d_sources (rule_id, source)"+
        " values (?,?)", new Object[]{13, "A"})).andReturn(0);

    expect(template.update(
        "insert into beast_site2d_sources (rule_id, source)"+
        " values (?,?)", new Object[]{13, "B"})).andReturn(0);
    
    classUnderTest = new Site2DRuleManager();
    classUnderTest.setTemplate(template);

    replayAll();
    
    classUnderTest.storeSources(13, sources);
    
    verifyAll();
  }
  
  @Test
  public void testStoreSources_nullSources() throws Exception {
    expect(template.update("delete from beast_site2d_sources where rule_id=?",
        new Object[]{13})).andReturn(0);
    
    classUnderTest = new Site2DRuleManager();
    classUnderTest.setTemplate(template);
    
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
    classUnderTest = new Site2DRuleManager() {
      protected RowMapper<String> getSourceMapper() {
        return mapper;
      }
    };
    classUnderTest.setTemplate(template);
    
    expect(template.query("select source from beast_site2d_sources where rule_id=?",
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
    
    expect(template.update("delete from beast_site2d_detectors where rule_id=?",
        new Object[]{13})).andReturn(0);
    
    expect(template.update(
        "insert into beast_site2d_detectors (rule_id, name)"+
        " values (?,?)", new Object[]{13, "A"})).andReturn(0);

    expect(template.update(
        "insert into beast_site2d_detectors (rule_id, name)"+
        " values (?,?)", new Object[]{13, "B"})).andReturn(0);
    
    classUnderTest = new Site2DRuleManager();
    classUnderTest.setTemplate(template);
    
    replayAll();
    
    classUnderTest.storeDetectors(13, sources);
    
    verifyAll();
  }

  @Test
  public void testStoreDetectors_null() throws Exception {
    expect(template.update("delete from beast_site2d_detectors where rule_id=?",
        new Object[]{13})).andReturn(0);
    
    classUnderTest = new Site2DRuleManager();
    classUnderTest.setTemplate(template);
    
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
    classUnderTest = new Site2DRuleManager() {
      protected RowMapper<String> getDetectorMapper() {
        return mapper;
      }
    };
    classUnderTest.setTemplate(template);
    
    expect(template.query("select name from beast_site2d_detectors where rule_id=?",
        mapper,
        new Object[]{13})).andReturn(detectors);
    
    replayAll();
    
    List<String> result = classUnderTest.getDetectors(13);
    
    verifyAll();
    assertSame(detectors, result);
  }
}
