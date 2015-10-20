/* --------------------------------------------------------------------
Copyright (C) 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.rules.gmap;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

import eu.baltrad.beast.db.Catalog;

/**
 *
 * @author Anders Henja
 * @date Mar 23, 2012
 */
public class GoogleMapRuleManagerTest extends EasyMockSupport {
  private JdbcOperations template = null;
  private Catalog catalog = null;
  private GoogleMapRuleManager classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    template = createMock(JdbcOperations.class);
    catalog = createMock(Catalog.class);
    classUnderTest = new GoogleMapRuleManager();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setJdbcTemplate(template);
  }
  
  @After
  public void tearDown() throws Exception {
    template = null;
    catalog = null;
    classUnderTest = null;
  }
  
  @Test
  public void testStore() throws Exception {
    GoogleMapRule rule = new GoogleMapRule();
    rule.setArea("swe");
    rule.setPath("pth");
    
    expect(template.update("insert into beast_gmap_rules (rule_id, area, path) values (?,?,?)",
        new Object[]{10, "swe", "pth"})).andReturn(1);
    
    replayAll();
    
    classUnderTest.store(10, rule);
    
    verifyAll();
  }

  @Test
  public void testLoad() throws Exception {
    GoogleMapRule rule = new GoogleMapRule();
    
    final RowMapper<GoogleMapRule> mapper = new RowMapper<GoogleMapRule>() {
      @Override
      public GoogleMapRule mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };
    classUnderTest = new GoogleMapRuleManager() {
      protected RowMapper<GoogleMapRule> getGmapRuleMapper() {
        return mapper;
      }
    };
    classUnderTest.setJdbcTemplate(template);
    
    expect(template.queryForObject("select * from beast_gmap_rules where rule_id=?", 
        mapper, new Object[]{10})).andReturn(rule);
    
    replayAll();
    
    GoogleMapRule result = (GoogleMapRule)classUnderTest.load(10);
    
    verifyAll();
    assertSame(rule, result);
  }
  
  @Test
  public void testUpdate() throws Exception {
    GoogleMapRule rule = new GoogleMapRule();
    rule.setArea("swe");
    rule.setPath("pth");
    
    expect(template.update("update beast_gmap_rules set area=?, path=? where rule_id=?",
        new Object[]{"swe", "pth", 10})).andReturn(1);
    
    replayAll();
    
    classUnderTest.update(10, rule);
    
    verifyAll();
  }
  
  @Test
  public void testDelete() throws Exception {
    expect(template.update("delete from beast_gmap_rules where rule_id=?", 
        new Object[]{10})).andReturn(1);
    
    replayAll();
    
    classUnderTest.delete(10);
    
    verifyAll();
  }
  
  @Test
  public void testCreateRule() throws Exception {
    GoogleMapRule result = (GoogleMapRule)classUnderTest.createRule();
    assertSame(catalog, result.getCatalog());
  }
  
  @Test
  public void testGmapRowMapper() throws Exception {
    RowMapper<GoogleMapRule> mapper = classUnderTest.getGmapRuleMapper();
    ResultSet rs = createMock(ResultSet.class);
    
    expect(rs.getString("area")).andReturn("sswe");
    expect(rs.getString("path")).andReturn("/tmp/path");
    
    replayAll();
    
    GoogleMapRule result = mapper.mapRow(rs, 0);
    
    verifyAll();
    assertEquals("sswe", result.getArea());
    assertEquals("/tmp/path", result.getPath());
    assertSame(catalog, result.getCatalog());
  }
}
