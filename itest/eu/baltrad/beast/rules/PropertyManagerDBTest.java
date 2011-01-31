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
package eu.baltrad.beast.rules;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.itest.BeastDBTestHelper;

public class PropertyManagerDBTest extends TestCase {
  private PropertyManager classUnderTest = null;
  private ApplicationContext context = null;
  private BeastDBTestHelper helper = null;

  public PropertyManagerDBTest(String name) {
    super(name);
  }

  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.cleanInsert(this);
    classUnderTest = new PropertyManager();
    SimpleJdbcOperations template = (SimpleJdbcOperations)context.getBean("jdbcTemplate");
    classUnderTest.setJdbcTemplate(template);
  }

  public void tearDown() throws Exception {
    helper = null;
    context = null;
    classUnderTest = null;
  }

  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_rule_properties");
    ITable actual = helper.getDatabaseTable("beast_rule_properties");
    Assertion.assertEquals(expected, actual);
  }

  public void testDeleteProperties() throws Exception {
    classUnderTest.deleteProperties(1);
    
    verifyDatabaseTables("deleteProperties");
  }

  public void testLoadProperties() {
    Map<String, String> props = classUnderTest.loadProperties(1);
    assertEquals(2, props.size());
    assertEquals("value1", props.get("key1"));
    assertEquals("value2", props.get("key2"));
  }

  public void testStoreProperties() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("key3", "value3");
    props.put("key4", "value4");
    
    classUnderTest.storeProperties(2, props);
    verifyDatabaseTables("storeProperties");
  }

  public void testUpdateProperties() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("key5", "value5");
    props.put("key6", "value6");
    
    classUnderTest.updateProperties(1, props);
    verifyDatabaseTables("updateProperties");
  }
}
