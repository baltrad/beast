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

package eu.baltrad.beast.rules.acrr;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.rules.RuleFilterManager;
import eu.baltrad.beast.rules.util.IRuleUtilities;
import eu.baltrad.beast.rules.util.RuleUtilities;

/**
 * @author Anders Henja
 */
public class AcrrRuleManagerTest extends EasyMockSupport {
  private JdbcOperations jdbc = null;
  private AcrrRuleManager classUnderTest = null;
  private RuleFilterManager filterManager = null;
  private interface Methods {
    public Map<String, IFilter> createMatchFilter(int rule_id, IFilter filter);
  };
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    jdbc = createMock(JdbcOperations.class);
    filterManager = createMock(RuleFilterManager.class);
    classUnderTest = new AcrrRuleManager();
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setFilterManager(filterManager);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    jdbc = null;
    filterManager = null;
    classUnderTest = null;
  }

  @Test
  public void test_store() {
    AcrrRule rule = new AcrrRule();
    rule.setArea("nrd_swe");
    rule.setDistancefield("eu.d.field");
    rule.setFilesPerHour(6);
    rule.setHours(2);
    rule.setAcceptableLoss(10);
    rule.setObjectType("COMP");
    rule.setQuantity("DBZH");
    rule.setZrA(100.0);
    rule.setZrB(0.5);
    rule.setApplyGRA(true);
    rule.setProductId("pn151");
    expect(jdbc.update(
        "INSERT INTO beast_acrr_rules (rule_id, area, distancefield, files_per_hour, hours, acceptable_loss, object_type, quantity, zra, zrb, applygra, productid) " +
        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
        new Object[]{3,"nrd_swe","eu.d.field",6,2,10,"COMP","DBZH",100.0,0.5,true,"pn151"})).andReturn(0);
    filterManager.deleteFilters(3);
    
    replayAll();
    
    classUnderTest.store(3, rule);
    
    verifyAll();
    assertEquals(3, rule.getRuleId());
  }
  
  @Test
  public void test_store_with_filter() {
    AcrrRule rule = new AcrrRule();
    IFilter filter = createMock(IFilter.class);
    final Methods methodsMock = createMock(Methods.class);
    final Map<String, IFilter> filters = new HashMap<String, IFilter>();
    
    rule.setArea("nrd_swe");
    rule.setDistancefield("eu.d.field");
    rule.setFilesPerHour(6);
    rule.setHours(2);
    rule.setAcceptableLoss(10);
    rule.setObjectType("COMP");
    rule.setQuantity("DBZH");
    rule.setZrA(100.0);
    rule.setZrB(0.5);
    rule.setApplyGRA(true);
    rule.setProductId("pn151");
    rule.setFilter(filter);
    
    expect(jdbc.update(
        "INSERT INTO beast_acrr_rules (rule_id, area, distancefield, files_per_hour, hours, acceptable_loss, object_type, quantity, zra, zrb, applygra, productid) " +
        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
        new Object[]{3,"nrd_swe","eu.d.field",6,2,10,"COMP","DBZH",100.0,0.5,true,"pn151"})).andReturn(0);
    expect(methodsMock.createMatchFilter(3, filter)).andReturn(filters);
    filterManager.updateFilters(3, filters);

    classUnderTest = new AcrrRuleManager() {
      protected Map<String, IFilter> createMatchFilter(int rule_id, IFilter filter) {
        return methodsMock.createMatchFilter(rule_id, filter);
      };
    };
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setFilterManager(filterManager);
    
    replayAll();
    
    classUnderTest.store(3, rule);
    
    verifyAll();
    assertEquals(3, rule.getRuleId());
  }
  
  @Test
  public void test_load() {
    AcrrRule rule = new AcrrRule();
    HashMap<String, IFilter> filters = new HashMap<String, IFilter>();
    IFilter filter = createMock(IFilter.class);
    filters.put("match", filter);
    
    final RowMapper<AcrrRule> mapper = new RowMapper<AcrrRule>() {
      public AcrrRule mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };

    classUnderTest = new AcrrRuleManager() {
      @Override
      protected RowMapper<AcrrRule> getAcrrRuleMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setFilterManager(filterManager);
    
    expect(jdbc.queryForObject("SELECT * FROM beast_acrr_rules WHERE rule_id=?",
        mapper, new Object[]{3})).andReturn(rule);
    expect(filterManager.loadFilters(3)).andReturn(filters);
    
    replayAll();
    
    AcrrRule result = (AcrrRule)classUnderTest.load(3);
    
    verifyAll();
    assertSame(rule, result);
    assertSame(filter, result.getFilter());
  }
  
  @Test
  public void test_update() {
    AcrrRule rule = new AcrrRule();
    rule.setArea("nrd_swe");
    rule.setDistancefield("eu.d.field");
    rule.setFilesPerHour(6);
    rule.setHours(2);
    rule.setAcceptableLoss(10);
    rule.setObjectType("COMP");
    rule.setQuantity("DBZH");
    rule.setZrA(100.0);
    rule.setZrB(0.5);
    rule.setApplyGRA(true);
    rule.setProductId("pn151");
    
    expect(jdbc.update("UPDATE beast_acrr_rules SET "+
      "area=?, distancefield=?, files_per_hour=?, hours=?, acceptable_loss=?, object_type=?, quantity=?, zra=?, zrb=?, applygra=?, productid=? WHERE rule_id=?", 
        new Object[]{"nrd_swe", "eu.d.field", 6, 2, 10, "COMP", "DBZH", 100.0, 0.5, true, "pn151", 3})).andReturn(0);
    filterManager.deleteFilters(3);
    
    replayAll();
    
    classUnderTest.update(3, rule);
    
    verifyAll();
    assertEquals(3, rule.getRuleId());
  }
  
  @Test
  public void test_update_with_filter() {
    AcrrRule rule = new AcrrRule();
    IFilter filter = createMock(IFilter.class);
    final Methods methodsMock = createMock(Methods.class);
    final Map<String, IFilter> filters = new HashMap<String, IFilter>();
    
    rule.setArea("nrd_swe");
    rule.setDistancefield("eu.d.field");
    rule.setFilesPerHour(6);
    rule.setHours(2);
    rule.setAcceptableLoss(10);
    rule.setObjectType("COMP");
    rule.setQuantity("DBZH");
    rule.setZrA(100.0);
    rule.setZrB(0.5);
    rule.setApplyGRA(true);
    rule.setProductId("pn151");
    rule.setFilter(filter);
    
    expect(jdbc.update("UPDATE beast_acrr_rules SET "+
      "area=?, distancefield=?, files_per_hour=?, hours=?, acceptable_loss=?, object_type=?, quantity=?, zra=?, zrb=?, applygra=?, productid=? WHERE rule_id=?", 
        new Object[]{"nrd_swe", "eu.d.field", 6, 2, 10, "COMP", "DBZH", 100.0, 0.5, true, "pn151", 3})).andReturn(0);
    expect(methodsMock.createMatchFilter(3, filter)).andReturn(filters);
    filterManager.updateFilters(3, filters);
    
    classUnderTest = new AcrrRuleManager() {
      protected Map<String, IFilter> createMatchFilter(int rule_id, IFilter filter) {
        return methodsMock.createMatchFilter(rule_id, filter);
      };
    };
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setFilterManager(filterManager);
    
    replayAll();
    
    classUnderTest.update(3, rule);
    
    verifyAll();
    assertEquals(3, rule.getRuleId());
  }
  
  @Test
  public void test_delete() {
    expect(jdbc.update("DELETE FROM beast_acrr_rules WHERE rule_id=?",
        new Object[]{3})).andReturn(0);
    filterManager.deleteFilters(3);
    
    replayAll();
    
    classUnderTest.delete(3);
    
    verifyAll();
  }
  
  @Test
  public void test_createRule() {
    Catalog catalog = new Catalog();
    RuleUtilities utils = new RuleUtilities();
    
    classUnderTest = new AcrrRuleManager();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(utils);
    
    AcrrRule result = (AcrrRule)classUnderTest.createRule();
    assertSame(catalog, result.getCatalog());
    assertSame(utils, result.getRuleUtilities());
  }
  
  @Test
  public void test_mapper() throws Exception {
    IRuleUtilities utils = createMock(IRuleUtilities.class);
    Catalog cat = createMock(Catalog.class);
    
    ResultSet rs = createMock(ResultSet.class);
    expect(rs.getInt("rule_id")).andReturn(3);
    expect(rs.getString("area")).andReturn("nrd_swe");
    expect(rs.getString("distancefield")).andReturn("eu.e.field");
    expect(rs.getInt("files_per_hour")).andReturn(3);
    expect(rs.getInt("hours")).andReturn(2);
    expect(rs.getInt("acceptable_loss")).andReturn(10);
    expect(rs.getString("object_type")).andReturn("IMAGE");
    expect(rs.getString("quantity")).andReturn("DBZH");
    expect(rs.getDouble("zra")).andReturn(100.0);
    expect(rs.getDouble("zrb")).andReturn(0.5);
    expect(rs.getBoolean("applygra")).andReturn(true);
    expect(rs.getString("productid")).andReturn("pn151");
    classUnderTest.setRuleUtilities(utils);
    classUnderTest.setCatalog(cat);
    
    replayAll();
    
    RowMapper<AcrrRule> mapper = classUnderTest.getAcrrRuleMapper();
    
    AcrrRule result = mapper.mapRow(rs, 1);
    
    verifyAll();
    assertEquals(3, result.getRuleId());
    assertEquals("nrd_swe", result.getArea());
    assertEquals("eu.e.field", result.getDistancefield());
    assertEquals(3, result.getFilesPerHour());
    assertEquals(2, result.getHours());
    assertEquals(10, result.getAcceptableLoss());
    assertEquals("IMAGE", result.getObjectType());
    assertEquals("DBZH", result.getQuantity());
    assertEquals(100.0, result.getZrA(), 4);
    assertEquals(0.5, result.getZrB(), 4);
    assertEquals(true, result.isApplyGRA());
    assertEquals("pn151", result.getProductId());
    assertSame(utils, result.getRuleUtilities());
    assertSame(cat, result.getCatalog());
  }
}
