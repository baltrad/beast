/*
Copyright 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

This file is part of Beast library.

Beast library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Beast library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Beast library.  If not, see <http://www.gnu.org/licenses/>.
*/

package eu.baltrad.beast.db;

import java.util.Map;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.itest.BeastDBTestHelper;

public class CoreFilterManagerDBTest extends TestCase {
  private CoreFilterManager classUnderTest;
  private AbstractApplicationContext context;
  private BeastDBTestHelper helper;

  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.cleanInsert(this);
    classUnderTest = new CoreFilterManager();
    SimpleJdbcOperations template = (SimpleJdbcOperations)context.getBean("jdbcTemplate");
    classUnderTest.setJdbcTemplate(template);
  }

  public void tearDown() throws Exception {
    context.close();
  }

  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_filters");
    ITable actual = helper.getDatabaseTable("beast_filters");
    Assertion.assertEquals(expected, actual);
  }

  public void testSqlSelectFilter() {
    Map<String, Object> map = classUnderTest.sqlSelectFilter(1);
    assertEquals(1, ((Integer)map.get("filter_id")).intValue());
    assertEquals("type1", (String)map.get("type"));
  }

  public void testSqlInsertFilter() throws Exception {
    int id = classUnderTest.sqlInsertFilter("type2");
    verifyDatabaseTables("insertFilter");
    assertEquals(3, id);
  }

  public void testSqlDeleteFilter() throws Exception {
    classUnderTest.sqlDeleteFilter(1);
    verifyDatabaseTables("deleteFilter");
  }
}
