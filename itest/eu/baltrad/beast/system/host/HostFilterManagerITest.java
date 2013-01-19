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

package eu.baltrad.beast.system.host;

import static org.junit.Assert.*;

import java.util.List;

import junit.framework.Assert;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.itest.BeastDBTestHelper;

/**
 * @author Anders Henja
 *
 */
public class HostFilterManagerITest {
  private AbstractApplicationContext context = null;
  private HostFilterManager classUnderTest = null;
  private BeastDBTestHelper helper = null;
  
  @Before
  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("helper");
    helper.cleanInsert(this);
    classUnderTest = new HostFilterManager();
    classUnderTest.setTemplate((SimpleJdbcOperations)context.getBean("jdbcTemplate"));
  }

  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
    helper = null;
    context.close();
  }

  
  @Test
  public void testAdd() throws Exception {
    classUnderTest.add("192.168.3.3");
    classUnderTest.add("199.*.*.*");

    verifyDatabaseTables("add");

  }

  @Test
  public void testRemove() throws Exception {
    classUnderTest.remove("132.128.*.*");
    
    verifyDatabaseTables("remove");
  }

  @Test
  public void testAfterPropertiesSet() throws Exception {
    classUnderTest.afterPropertiesSet();
    
    List<String> result = classUnderTest.getPatterns();
    Assert.assertEquals(3, result.size());
    Assert.assertEquals("127.0.0.1", result.get(0));
    Assert.assertEquals("132.128.*.*", result.get(1));
    Assert.assertEquals("192.168.0.*", result.get(2));
  }
  
  /**
   * Verifies the database table with an excel sheet.
   * @param extras
   * @throws Exception
   */
  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_host_filter");
    ITable actual = helper.getDatabaseTable("beast_host_filter");

    Assertion.assertEquals(expected, actual);
  }
}
