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

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.itest.BeastDBTestHelper;

public class AttributeFilterManagerDBTest extends TestCase {
  private AttributeFilterManager classUnderTest;
  private AbstractApplicationContext context;
  private BeastDBTestHelper helper;

  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.cleanInsert(this);
    classUnderTest = new AttributeFilterManager();
    SimpleJdbcOperations template = (SimpleJdbcOperations)context.getBean("jdbcTemplate");
    classUnderTest.setJdbcTemplate(template);
  }

  public void tearDown() throws Exception {
    context.close();
  }

  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_attr_filters");
    ITable actual = helper.getDatabaseTable("beast_attr_filters");
    Assertion.assertEquals(expected, actual);
  }

  public void testStore() throws Exception {
    AttributeFilter f = new AttributeFilter();
    f.setId(3);
    f.setAttribute("_bdb/source_name");
    f.setOperator(AttributeFilter.Operator.IN);
    f.setValueType(AttributeFilter.ValueType.STRING);
    f.setValue("seang, searl");
    f.setNegated(true);

    classUnderTest.store(f);
    verifyDatabaseTables("store");
  }

  public void testLoad() {
    AttributeFilter f = classUnderTest.load(1);
    assertEquals("what/object", f.getAttribute());
    assertEquals(AttributeFilter.Operator.EQ, f.getOperator());
    assertEquals(AttributeFilter.ValueType.STRING, f.getValueType());
    assertEquals("PVOL", f.getValue());
    assertEquals(true, f.isNegated());
  }

  public void testUpdate() throws Exception {
    AttributeFilter f = new AttributeFilter();
    f.setId(2);
    f.setAttribute("where/nrays");
    f.setOperator(AttributeFilter.Operator.EQ);
    f.setValueType(AttributeFilter.ValueType.LONG);
    f.setValue("100");
    f.setNegated(true);
    
    classUnderTest.update(f);
    verifyDatabaseTables("update");
  }

  public void testRemove() throws Exception {
    AttributeFilter f = new AttributeFilter();
    f.setId(2);

    classUnderTest.remove(f);
    verifyDatabaseTables("remove");
  }
}
