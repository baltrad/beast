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

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jdbc.core.JdbcOperations;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.rules.RuleFilterManager;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class AcrrRuleManagerITest extends TestCase {
  private AbstractApplicationContext dbcontext = null;
  private AbstractApplicationContext context = null;
  
  private BeastDBTestHelper helper = null;
  private AcrrRuleManager classUnderTest = null;
  private JdbcOperations template = null;
  private Catalog catalog = null;
  private IRuleUtilities utilities = null;
  private RuleFilterManager filterManager = null;

  public void setUp() throws Exception {
    dbcontext = BeastDBTestHelper.loadDbContext(this);
    helper = (BeastDBTestHelper)dbcontext.getBean("helper");
    helper.tearDown();
    helper.purgeBaltradDB();
    
    helper.cleanInsert(this);
    template = (JdbcOperations)dbcontext.getBean("jdbcTemplate");
    
    context = BeastDBTestHelper.loadContext(this);
    catalog = (Catalog)context.getBean("catalog");
    filterManager = (RuleFilterManager)context.getBean("filterManager");
    utilities = (IRuleUtilities)context.getBean("ruleutil");
    
    classUnderTest = new AcrrRuleManager();
    classUnderTest.setJdbcTemplate(template);
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(utilities);
    classUnderTest.setFilterManager(filterManager);
  }
  
  public void tearDown() throws Exception {
    classUnderTest = null;
    helper = null;
    template = null;
    catalog = null;
    utilities = null;
    filterManager = null;
    context.close();
    dbcontext.close();
  }
 
  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_acrr_rules");
    ITable actual = helper.getDatabaseTable("beast_acrr_rules");
    Assertion.assertEquals(expected, actual);
  }
  
  public void test_store() throws Exception {
    AcrrRule rule = new AcrrRule();
    rule.setArea("nrd_ose");
    rule.setDistancefield("eu.baltrad.osefield");
    rule.setFilesPerHour(4);
    rule.setHours(12);
    rule.setAcceptableLoss(20);
    rule.setObjectType("COMP");
    rule.setQuantity("DBZH");
    rule.setZrA(250.0);
    rule.setZrB(1.0);
    rule.setApplyGRA(true);
    rule.setProductId("pn151");
    
    classUnderTest.store(4, rule);
    
    verifyDatabaseTables("store");
  }
  
  public void test_load() throws Exception {
    AcrrRule result = (AcrrRule)classUnderTest.load(2);
    assertEquals(2, result.getRuleId());
    assertEquals("nrd_sswe", result.getArea());
    assertEquals("eu.baltrad.df2", result.getDistancefield());
    assertEquals(12, result.getFilesPerHour());
    assertEquals(24, result.getHours());
    assertEquals(0, result.getAcceptableLoss());
    assertEquals("COMP", result.getObjectType());
    assertEquals("TH", result.getQuantity());
    assertEquals(300.0, result.getZrA(), 4);
    assertEquals(0.6, result.getZrB(), 4);
    assertEquals(false, result.isApplyGRA());
    assertEquals("pn152", result.getProductId());
  }
  
  public void test_update() throws Exception {
    AcrrRule rule = new AcrrRule();
    rule.setArea("nrd_ose");
    rule.setDistancefield("eu.baltrad.osefield");
    rule.setFilesPerHour(6);
    rule.setHours(12);
    rule.setAcceptableLoss(80);
    rule.setObjectType("IMAGE");
    rule.setQuantity("DBZH");
    rule.setZrA(100.0);
    rule.setZrB(2.0);
    rule.setApplyGRA(true);
    rule.setProductId("pn154");
    
    classUnderTest.update(2, rule);
    
    verifyDatabaseTables("update");
  }
  
  public void test_delete() throws Exception {
    classUnderTest.delete(2);
    verifyDatabaseTables("delete");
  }
}
