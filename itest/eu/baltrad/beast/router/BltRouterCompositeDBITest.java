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
package eu.baltrad.beast.router;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.router.impl.BltRouter;
import eu.baltrad.beast.rules.RuleException;
import eu.baltrad.beast.rules.composite.CompositingRule;
import eu.baltrad.beast.scheduler.IBeastScheduler;

/**
 * @author Anders Henja
 */
public class BltRouterCompositeDBITest extends TestCase {
  private BltRouter classUnderTest = null;
  private ApplicationContext context = null;
  private BeastDBTestHelper helper = null;
  private IBeastScheduler scheduler = null;
  
  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.purgeBaltradDB();
    helper.tearDown();
    helper.cleanInsert(this);
    classUnderTest = (BltRouter)context.getBean("router");
    classUnderTest.afterPropertiesSet();
    scheduler = (IBeastScheduler)context.getBean("beastscheduler");
  }
  
  public void tearDown() throws Exception {
    context = null;
    helper = null;
    classUnderTest = null;
    scheduler = null;
  }

  /**
   * Verifies the database table with an excel sheet.
   * @param extras
   * @throws Exception
   */
  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_router_rules");
    ITable actual = helper.getDatabaseTable("beast_router_rules");
    Assertion.assertEquals(expected, actual);

    expected = helper.getXlsTable(this, extras, "beast_adaptors");
    actual = helper.getDatabaseTable("beast_adaptors");
    Assertion.assertEqualsIgnoreCols(expected, actual, new String[]{"adaptor_id"});

    expected = helper.getXlsTable(this, extras, "beast_router_dest");
    actual = helper.getDatabaseTable("beast_router_dest");
    Assertion.assertEquals(expected, actual);
    
    expected = helper.getXlsTable(this, extras, "beast_scheduled_jobs");
    actual = helper.getDatabaseTable("beast_scheduled_jobs");
    Assertion.assertEquals(expected, actual);
  }
  
  public void testLoadCompositingDef() {
    RouteDefinition def = classUnderTest.getDefinition("admin");
    assertNotNull(def);
    assertEquals("admin", def.getName());
    assertEquals("Karl", def.getAuthor());
    assertEquals("blt_composite", def.getRuleType());
    assertEquals(20, ((CompositingRule)def.getRule()).getTimeout());
    assertEquals(true, ((CompositingRule)def.getRule()).isScanBased());
  }
  
  public void testRemoveCompositeWithScheduledJob() throws Exception {
    scheduler.register("0 0 0 * 1 ?", "admin");
    try {
      classUnderTest.deleteDefinition("admin");
      fail("Expected RuleException");
    } catch (RuleException e) {
    }
    verifyDatabaseTables("removeCompositeWithScheduledJob");
    RouteDefinition def = classUnderTest.getDefinition("admin");
    assertNotNull(def);
    assertEquals("admin", def.getName());
  }
  
  public void testRemoveCompositeWithScheduledJobRemoved() throws Exception {
    int id = scheduler.register("0 0 0 * 1 ?", "admin");
    try {
      classUnderTest.deleteDefinition("admin");
      fail("Expected RuleException");
    } catch (RuleException e) {
    }
    scheduler.unregister(id);
    classUnderTest.deleteDefinition("admin");
    verifyDatabaseTables("removeCompositeWithScheduledJobRemoved");
    RouteDefinition def = classUnderTest.getDefinition("admin");
    assertNull(def);
  }
}
