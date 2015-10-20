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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jdbc.core.JdbcOperations;

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
    JdbcOperations template = (JdbcOperations)context.getBean("jdbcTemplate");
    classUnderTest.setJdbcTemplate(template);
    Map<String,IFilterManager> subManagers = new HashMap<String,IFilterManager>();
    AttributeFilterManager attributeFilterManager = new AttributeFilterManager();
    attributeFilterManager.setJdbcTemplate(template);
    subManagers.put("attr", attributeFilterManager);
    CombinedFilterManager combinedFilterManager = new CombinedFilterManager();
    combinedFilterManager.setChildManager(classUnderTest);
    combinedFilterManager.setJdbcTemplate(template);
    subManagers.put("combined", combinedFilterManager);
    classUnderTest.setSubManagers(subManagers);
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

  public void testStoreCombinedWithAttributeChildren() {
    AttributeFilter sourceFilter = new AttributeFilter();
    sourceFilter.setAttribute( "_bdb/source:WMO" );
    sourceFilter.setValueType( AttributeFilter.ValueType.STRING );
    sourceFilter.setOperator( AttributeFilter.Operator.IN );
    sourceFilter.setValue( "someValue" );

    AttributeFilter fileObjectFilter = new AttributeFilter();
    fileObjectFilter.setAttribute( "what/object" );
    fileObjectFilter.setValueType( AttributeFilter.ValueType.STRING );
    fileObjectFilter.setOperator( AttributeFilter.Operator.IN );
    fileObjectFilter.setValue( "anotherValue" );

    CombinedFilter combinedFilter = new CombinedFilter();
    combinedFilter.addChildFilter( sourceFilter );
    combinedFilter.addChildFilter( fileObjectFilter );
    combinedFilter.setMatchType( CombinedFilter.MatchType.ALL );

    classUnderTest.store( combinedFilter );
  }
}
