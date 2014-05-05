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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.rules.RuleException;

/**
 * @author Anders Henja
 *
 */
public class ScansunRuleManagerITest extends TestCase {
  private AbstractApplicationContext dbcontext = null;
  private AbstractApplicationContext context = null;
  
  private BeastDBTestHelper helper = null;
  private ScansunRuleManager classUnderTest = null;
  private SimpleJdbcOperations template = null;

  public void setUp() throws Exception {
    dbcontext = BeastDBTestHelper.loadDbContext(this);
    helper = (BeastDBTestHelper)dbcontext.getBean("helper");
    helper.tearDown();
    helper.purgeBaltradDB();
    
    helper.cleanInsert(this);
    template = (SimpleJdbcOperations)dbcontext.getBean("jdbcTemplate");
    
    context = BeastDBTestHelper.loadContext(this);
    
    classUnderTest = new ScansunRuleManager();
    classUnderTest.setJdbcTemplate(template);
  }
  
  public void tearDown() throws Exception {
    classUnderTest = null;
    helper = null;
    template = null;
    context.close();
    dbcontext.close();
  }
 
  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_scansun_sources");
    ITable actual = helper.getDatabaseTable("beast_scansun_sources");
    Assertion.assertEquals(expected, actual);
  }
  
  public void test_store() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("sease");
    sources.add("senar");
    ScansunRule rule = new ScansunRule();
    rule.setSources(sources);
    
    classUnderTest.store(5, rule);
    
    verifyDatabaseTables("store");
  }
  
  public void test_store_already_existing() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("sease");
    sources.add("senar");
    ScansunRule rule = new ScansunRule();
    rule.setSources(sources);
    
    try {
      classUnderTest.store(4, rule);
      fail("Expected RuleException");
    } catch (RuleException re) {
      // pass
    }
    
    verifyDatabaseTables(null);
  }

  public void test_load() throws Exception {
    ScansunRule rule = (ScansunRule)classUnderTest.load(3);
    assertEquals(2, rule.getSources().size());
    assertEquals("sease", rule.getSources().get(0));
    assertEquals("searl", rule.getSources().get(1));
  }
  
  public void test_load_2() throws Exception {
    ScansunRule rule = (ScansunRule)classUnderTest.load(4);
    assertEquals(1, rule.getSources().size());
    assertEquals("selul", rule.getSources().get(0));
  }

  public void test_load_3() throws Exception {
    ScansunRule rule = (ScansunRule)classUnderTest.load(5);
    assertEquals(0, rule.getSources().size());
  }
  
  public void test_update() throws Exception {
    ScansunRule rule = new ScansunRule();
    List<String> sources = new ArrayList<String>();
    sources.add("sekir");
    sources.add("seang");   
    sources.add("selek");   
    rule.setSources(sources);
    classUnderTest.update(3, rule);
    verifyDatabaseTables("update");
  }
  
  public void test_delete() throws Exception {
    classUnderTest.delete(3);
    verifyDatabaseTables("delete");
  }
}
