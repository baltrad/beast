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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.rules.util.IRuleUtilities;
/**
 * @author Anders Henja
 *
 */
public class Site2DRuleManagerITest extends TestCase {
  private AbstractApplicationContext dbcontext = null;
  private AbstractApplicationContext context = null;
  
  private BeastDBTestHelper helper = null;
  private Site2DRuleManager classUnderTest = null;
  private SimpleJdbcOperations template = null;
  private Catalog catalog = null;
  private IRuleUtilities utilities = null;

  public void setUp() throws Exception {
    dbcontext = BeastDBTestHelper.loadDbContext(this);
    helper = (BeastDBTestHelper)dbcontext.getBean("helper");
    helper.tearDown();
    helper.purgeBaltradDB();
    
    helper.cleanInsert(this);
    template = (SimpleJdbcOperations)dbcontext.getBean("jdbcTemplate");
    
    context = BeastDBTestHelper.loadContext(this);
    catalog = (Catalog)context.getBean("catalog");
    utilities = (IRuleUtilities)context.getBean("ruleUtilities");
    
    classUnderTest = new Site2DRuleManager();
    classUnderTest.setTemplate(template);
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(utilities);
  }
  
  public void tearDown() throws Exception {
    utilities = null;
    catalog = null;
    classUnderTest = null;
    helper = null;
    template = null;
    context.close();
    dbcontext.close();
  }
  
  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_site2d_rules");
    ITable actual = helper.getDatabaseTable("beast_site2d_rules");
    ITable expectedSources = helper.getXlsTable(this, extras, "beast_site2d_sources");
    ITable actualSources = helper.getDatabaseTable("beast_site2d_sources");
    ITable expectedDetectors = helper.getXlsTable(this, extras, "beast_site2d_detectors");
    ITable actualDetectors = helper.getDatabaseTable("beast_site2d_detectors");
    Assertion.assertEquals(expected, actual);
    Assertion.assertEquals(expectedSources, actualSources);
    Assertion.assertEquals(expectedDetectors, actualDetectors);
  }
  
  public void test_store() throws Exception {
    Site2DRule rule = new Site2DRule();
    List<String> detectors = new ArrayList<String>();
    detectors.add("sd1");
    detectors.add("sd2");
    List<String> sources = new ArrayList<String>();
    sources.add("ss1");
    sources.add("ss2");
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
    
    classUnderTest.store(5, rule);
    
    verifyDatabaseTables("store");
  }
  
  public void test_update() throws Exception {
    Site2DRule rule = new Site2DRule();
    List<String> detectors = new ArrayList<String>();
    detectors.add("sd1");
    detectors.add("sd2");
    List<String> sources = new ArrayList<String>();
    sources.add("ss1");
    sources.add("ss2");
    rule.setArea("nisse");
    rule.setInterval(10);
    rule.setScanBased(false);
    rule.setMethod(Site2DRule.CAPPI);
    rule.setProdpar("10,10");
    rule.setApplyGRA(true);
    rule.setZR_A(1.1);
    rule.setZR_b(0.1);
    rule.setIgnoreMalfunc(false);
    rule.setCtFilter(true);
    rule.setDetectors(detectors);
    rule.setSources(sources);
    
    classUnderTest.update(3, rule);
    
    verifyDatabaseTables("update");
  }
  
  public void test_delete() throws Exception {
    classUnderTest.delete(3);
    
    verifyDatabaseTables("delete");
  }
  
  //rule_id area  interval  byscan  method  prodpar applygra  ZR_A  ZR_b  ignore_malfunc  ctfilter

  //3               15  true  pcappi  1000  false 210 1.1 true  false


  public void test_load() throws Exception {
    Site2DRule rule = (Site2DRule)classUnderTest.load(3);

    assertEquals(3, rule.getRuleId());
    assertEquals(null, rule.getArea());
    assertEquals(15, rule.getInterval());
    assertEquals(true, rule.isScanBased());
    assertEquals(Site2DRule.PCAPPI, rule.getMethod());
    assertEquals("1000", rule.getProdpar());
    assertEquals(false, rule.isApplyGRA());
    assertEquals(210, rule.getZR_A(), 4);
    assertEquals(1.1, rule.getZR_b(), 4);
    assertEquals(true, rule.isIgnoreMalfunc());
    assertEquals(false, rule.isCtFilter());
  }

}
