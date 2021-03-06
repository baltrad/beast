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
package eu.baltrad.beast.scheduler;

import java.util.List;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jdbc.core.JdbcOperations;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.manager.IBltMessageManager;
import eu.baltrad.beast.message.IBltMessage;

/**
 * @author Anders Henja
 *
 */
public class BeastSchedulerITest extends TestCase {
  private AbstractApplicationContext context = null;
  private BeastScheduler classUnderTest = null;
  private BeastDBTestHelper helper = null;
  
  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("helper");
    helper.purgeBaltradDB();
    helper.cleanInsert(this);
    classUnderTest = new BeastScheduler();
    classUnderTest.setJdbcTemplate((JdbcOperations)context.getBean("jdbcTemplate"));
    classUnderTest.setMessageManager(new IBltMessageManager() {
      public void shutdown() {
      }
      public void manage(IBltMessage message) {
      }
    });
    classUnderTest.afterPropertiesSet();
  }
  
  public void tearDown() throws Exception {
    classUnderTest.destroy();
    classUnderTest = null;
    helper = null;
    context.close();
  }
  
  protected String getFilePath(String resource) throws Exception {
    java.io.File f = new java.io.File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }

  /**
   * Verifies the database table with an excel sheet.
   * @param extras
   * @throws Exception
   */
  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_scheduled_jobs");
    ITable actual = helper.getDatabaseTable("beast_scheduled_jobs");
    Assertion.assertEquals(expected, actual);
  }
  
  public void testGetSchedule() throws Exception {
    List<CronEntry> result = classUnderTest.getSchedule();
    assertEquals(3, result.size());
    
    assertEquals(1, result.get(0).getId());
    assertEquals("A", result.get(0).getName());
    assertEquals("0 * * * * ?", result.get(0).getExpression());
    
    assertEquals(2, result.get(1).getId());
    assertEquals("B", result.get(1).getName());
    assertEquals("1 * * * * ?", result.get(1).getExpression());
    
    assertEquals(3, result.get(2).getId());
    assertEquals("A", result.get(2).getName());
    assertEquals("2 * * * * ?", result.get(2).getExpression());
  }

  public void testRegister() throws Exception {
    int result = classUnderTest.register("0 * * * * ?", "C");
    assertEquals(4, result);
    CronEntry entry = classUnderTest.getEntry(4);
    assertEquals(4, entry.getId());
    assertEquals("C", entry.getName());
    assertEquals("0 * * * * ?", entry.getExpression());
    verifyDatabaseTables("register");
  }

  public void testRegister_nonExisting() throws Exception {
    try {
      classUnderTest.register("0 * * * * ?", "D");
      fail("Expected SchedulerException");
    } catch (SchedulerException e) {
      // pass
    }
    assertNull(classUnderTest.getEntry(4));
    verifyDatabaseTables(null);
  }
  
  private static class DummyManager implements IBltMessageManager {
    private volatile boolean triggered = false;
    @Override
    public synchronized void manage(IBltMessage message) {
      triggered = true;
      notifyAll();
    }
    @Override
    public void shutdown() {
    }
    
    public synchronized boolean waitForManage(long timeout) {
      long now = System.currentTimeMillis();
      long then = now + timeout;
      while (then > now && triggered == false) {
        try {
          wait(then - now);
        } catch (Throwable t) {
          // pass
        }
        now = System.currentTimeMillis();
      }
      return triggered;
    }
  }

  public void testRegister_verifyScheduleStart() throws Exception {
    DummyManager mgr = new DummyManager();
    
    classUnderTest = new BeastScheduler();
    classUnderTest.setJdbcTemplate((JdbcOperations)context.getBean("jdbcTemplate"));
    classUnderTest.setMessageManager(mgr);
    classUnderTest.afterPropertiesSet();
    classUnderTest.register("* * * * * ?", "C");
    boolean triggered = mgr.waitForManage(5000);
    assertEquals(true, triggered);
  }

  public void testReregister() throws Exception {
    classUnderTest.reregister(2, "2 * * * * ?", "C");
    
    CronEntry entry = classUnderTest.getEntry(2);
    assertEquals(2, entry.getId());
    assertEquals("C", entry.getName());
    assertEquals("2 * * * * ?", entry.getExpression());
    verifyDatabaseTables("reregister");
  }

  public void testReregister_same() throws Exception {
    classUnderTest.reregister(2, "2 * * * * ?", "B");
    
    CronEntry entry = classUnderTest.getEntry(2);
    assertEquals(2, entry.getId());
    assertEquals("B", entry.getName());
    assertEquals("2 * * * * ?", entry.getExpression());
    verifyDatabaseTables("reregister-same");
  }

  public void testReregister_nonExisting() throws Exception {
    try {
      classUnderTest.reregister(2, "2 * * * * ?", "D");
      fail("Expected SchedulerException");
    } catch (SchedulerException e) {
      // pass
    }
    
    CronEntry entry = classUnderTest.getEntry(2);
    assertEquals(2, entry.getId());
    assertEquals("B", entry.getName());
    assertEquals("1 * * * * ?", entry.getExpression());
    verifyDatabaseTables(null);
  }
  
  public void testUnregister() throws Exception {
    classUnderTest.unregister(2);
    verifyDatabaseTables("unregister");
    assertNull(classUnderTest.getEntry(2));
  }
  
  public void testGetEntry() throws Exception {
    CronEntry e1 = classUnderTest.getEntry(1);
    CronEntry e2 = classUnderTest.getEntry(2);
    CronEntry e3 = classUnderTest.getEntry(3);
    
    assertEquals(1, e1.getId());
    assertEquals("A", e1.getName());
    assertEquals("0 * * * * ?", e1.getExpression());
    
    assertEquals(2, e2.getId());
    assertEquals("B", e2.getName());
    assertEquals("1 * * * * ?", e2.getExpression());
    
    assertEquals(3, e3.getId());
    assertEquals("A", e3.getName());
    assertEquals("2 * * * * ?", e3.getExpression());
  }

  public void testGetEntry_nonExisting() throws Exception {
    CronEntry result = classUnderTest.getEntry(4);
    assertNull(result);
  }
  
  public void testGetScheduleByJob() throws Exception {
    List<CronEntry> result = classUnderTest.getSchedule("A");
    assertEquals(2, result.size());

    assertEquals(1, result.get(0).getId());
    assertEquals("A", result.get(0).getName());
    assertEquals("0 * * * * ?", result.get(0).getExpression());
    
    assertEquals(3, result.get(1).getId());
    assertEquals("A", result.get(1).getName());
    assertEquals("2 * * * * ?", result.get(1).getExpression());
  }
  
  public void testGetScheduleByJob_nothingFound() throws Exception {
    List<CronEntry> result = classUnderTest.getSchedule("nisse");
    assertEquals(0, result.size());
  }
}
