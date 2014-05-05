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

package eu.baltrad.beast.rules.scansun;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.rules.IRule;

/**
 * @author Anders Henja
 *
 */
public class ScansunRuleManagerTest extends EasyMockSupport {
  private static interface Methods {
    public void store(int rule_id, IRule rule);
    public List<String> getSources(int rule_id);
    public IRule createRule();
  };
  
  private ScansunRuleManager classUnderTest = null;
  private SimpleJdbcOperations jdbc = null;
  
  @Before
  public void setUp() throws Exception {
    jdbc = createMock(SimpleJdbcOperations.class);
    classUnderTest = new ScansunRuleManager();
    classUnderTest.setJdbcTemplate(jdbc);
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
    jdbc = null;
  }
  
  @Test
  public void store() {
    ScansunRule rule = new ScansunRule();
    List<String> sources = new ArrayList<String>();
    sources.add("A");
    sources.add("B");
    rule.setSources(sources);
    
    expect(jdbc.queryForInt("SELECT COUNT(*) FROM beast_scansun_sources WHERE rule_id=?",
        new Object[]{13})).andReturn(0);
    
    expect(jdbc.update(
        "INSERT INTO beast_scansun_sources (rule_id, source)"+
        " VALUES (?,?)", new Object[]{13, "A"})).andReturn(0);

    expect(jdbc.update(
        "INSERT INTO beast_scansun_sources (rule_id, source)"+
        " VALUES (?,?)", new Object[]{13, "B"})).andReturn(0);
    
    replayAll();
    
    classUnderTest.store(13, rule);
    
    verifyAll();
    assertEquals(13,rule.getRuleId());
  }
  
  @Test
  public void load() throws Exception {
    final Methods methods = createMock(Methods.class);
    
    List<String> sources = new ArrayList<String>();
    ScansunRule srule = createMock(ScansunRule.class);
    
    final ParameterizedRowMapper<String> mapper = new ParameterizedRowMapper<String>() {
      public String mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };
    classUnderTest = new ScansunRuleManager() {
      @Override
      protected ParameterizedRowMapper<String> getSourceMapper() {
        return mapper;
      }
      @Override
      public IRule createRule() {
        return methods.createRule();
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    expect(methods.createRule()).andReturn(srule);
    srule.setRuleId(13);
    srule.setSources(sources);
    
    expect(jdbc.query("SELECT source FROM beast_scansun_sources WHERE rule_id=?",
        mapper,
        new Object[]{13})).andReturn(sources);
    
    replayAll();
    
    ScansunRule result = (ScansunRule)classUnderTest.load(13);
    
    verifyAll();
    assertSame(srule, result);
  }
  
  @Test
  public void update() throws Exception {
    final Methods methods = createMock(Methods.class);
    ScansunRule srule = new ScansunRule();
    
    expect(jdbc.update("DELETE FROM beast_scansun_sources WHERE rule_id=?",
        new Object[]{13})).andReturn(0);
    methods.store(13, srule);
    
    replayAll();
    
    classUnderTest = new ScansunRuleManager() {
      @Override
      public void store(int rule_id, IRule rule) {
        methods.store(rule_id, rule);
      }
    };
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.update(13, srule);
    
    verifyAll();
  }
  
  @Test
  public void delete() throws Exception {
    expect(jdbc.update("DELETE FROM beast_scansun_sources WHERE rule_id=?", 13)).andReturn(1);
    replayAll();
    
    classUnderTest.delete(13);
    
    verifyAll();
  }
   
}
