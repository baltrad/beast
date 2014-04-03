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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.rules.acrr.AcrrRule;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class WrwpRuleManagerITest {
  private AbstractApplicationContext dbcontext = null;
  private AbstractApplicationContext context = null;

  private BeastDBTestHelper helper = null;
  private WrwpRuleManager classUnderTest = null;
  private SimpleJdbcOperations template = null;
  private Catalog catalog = null;
  private IRuleUtilities utilities = null;

  @Before
  public void setUp() throws Exception {
    dbcontext = BeastDBTestHelper.loadDbContext(this);
    helper = (BeastDBTestHelper)dbcontext.getBean("helper");
    helper.tearDown();
    helper.purgeBaltradDB();
    
    helper.cleanInsert(this);
    template = (SimpleJdbcOperations)dbcontext.getBean("jdbcTemplate");
    
    context = BeastDBTestHelper.loadContext(this);
    catalog = (Catalog)context.getBean("catalog");
    utilities = (IRuleUtilities)context.getBean("ruleutil");

    classUnderTest = new WrwpRuleManager();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(utilities);
    classUnderTest.setJdbcTemplate(template);
  }
   
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
    helper = null;
    template = null;
    catalog = null;
    utilities = null;
    context.close();
    dbcontext.close();
  }
  
  @Test
  public void test_store() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("seang");
    sources.add("searl");
    sources.add("sekkr");
    sources.add("seosu");

    WrwpRule rule = new WrwpRule();
    rule.setInterval(300);
    rule.setMaxheight(15000);
    rule.setMindistance(1000);
    rule.setMaxdistance(20000);
    rule.setMinelevationangle(1.5);
    rule.setMinvelocitythreshold(0.5);
    rule.setSources(sources);
    
    classUnderTest.store(4, rule);
    
    verifyDatabaseTables("store");
    
    assertEquals(4, rule.getRuleId());
  }
  
  @Test
  public void test_load() throws Exception {
    WrwpRule rule = (WrwpRule)classUnderTest.load(2);
    assertEquals(400, rule.getInterval());
    assertEquals(20000, rule.getMaxheight());
    assertEquals(1000, rule.getMindistance());
    assertEquals(10000, rule.getMaxdistance());
    assertEquals(2.5, rule.getMinelevationangle(), 4);
    assertEquals(3.5, rule.getMinvelocitythreshold(), 4);
    assertEquals(3, rule.getSources().size());
    assertTrue(rule.getSources().contains("sekkr"));
    assertTrue(rule.getSources().contains("selul"));
    assertTrue(rule.getSources().contains("seang"));
  }
  
  @Test
  public void test_update() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("seang");
    sources.add("seosu");

    WrwpRule rule = new WrwpRule();
    rule.setInterval(700);
    rule.setMaxheight(999);
    rule.setMindistance(99);
    rule.setMaxdistance(5000);
    rule.setMinelevationangle(1.1);
    rule.setMinvelocitythreshold(0.1);
    rule.setSources(sources);

    classUnderTest.update(2, rule);
    
    verifyDatabaseTables("update");
  }
  
  @Test
  public void test_delete() throws Exception {
    classUnderTest.delete(2);
    verifyDatabaseTables("delete");
  }
  
  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_wrwp_rules");
    ITable actual = helper.getDatabaseTable("beast_wrwp_rules");
    ITable expectedsrc = helper.getXlsTable(this, extras, "beast_wrwp_sources");
    ITable actualsrc = helper.getDatabaseTable("beast_wrwp_sources");
    Assertion.assertEquals(expected, actual);
    Assertion.assertEquals(expectedsrc, actualsrc);
  }
  
  private String getFilePath(String resource) throws Exception {
    File f = new File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }
}