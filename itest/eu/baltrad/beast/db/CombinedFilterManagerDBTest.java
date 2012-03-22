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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.easymock.EasyMock;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.itest.BeastDBTestHelper;

public class CombinedFilterManagerDBTest extends TestCase {
  private CombinedFilterManager classUnderTest;
  private AbstractApplicationContext context;
  private BeastDBTestHelper helper;

  private IFilter filter1;
  private IFilter filter2;
  private IFilterManager childManager;

  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.cleanInsert(this);

    filter1 = createMock(IFilter.class);
    filter2 = createMock(IFilter.class);
    childManager = createMock(IFilterManager.class);

    classUnderTest = new CombinedFilterManager();
    SimpleJdbcOperations template = (SimpleJdbcOperations)context.getBean("jdbcTemplate");
    classUnderTest.setJdbcTemplate(template);
    classUnderTest.setChildManager(childManager);
  }

  public void tearDown() throws Exception {
    context.close();
  }

  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_combined_filters");
    ITable actual = helper.getDatabaseTable("beast_combined_filters");
    Assertion.assertEquals(expected, actual);

    expected = helper.getXlsTable(this, extras, "beast_combined_filter_children");
    actual = helper.getDatabaseTable("beast_combined_filter_children");
    Assertion.assertEquals(expected, actual);
  }

  private void replay() {
    EasyMock.replay(filter1, filter2, childManager);
  }
  
  private void verify() {
    EasyMock.verify(filter1, filter2, childManager);
  }

  public void testStore() throws Exception {
    CombinedFilter f = new CombinedFilter();
    List<IFilter> children = new ArrayList<IFilter>();
    children.add(filter1);
    children.add(filter2);
    f.setChildFilters(children);
    f.setId(8);
    f.setMatchType(CombinedFilter.MatchType.ANY);

    expect(filter1.getId()).andReturn(new Integer(1));
    childManager.store(filter1);
    expect(filter2.getId()).andReturn(new Integer(2));
    childManager.store(filter2);
    replay();
    
    classUnderTest.store(f);
    
    verify();
    verifyDatabaseTables("store");
  }

  public void testLoad() {
    expect(childManager.load(1)).andReturn(filter1);
    expect(childManager.load(2)).andReturn(filter2);

    replay();

    CombinedFilter f = classUnderTest.load(6);
    
    verify();
    assertEquals(CombinedFilter.MatchType.ALL, f.getMatchType());
    assertTrue(f.getChildFilters().contains(filter1));
    assertTrue(f.getChildFilters().contains(filter2));
  }

  public void testUpdate() throws Exception {
    CombinedFilter f = new CombinedFilter();
    f.setId(6);
    f.setMatchType(CombinedFilter.MatchType.ANY);
    List<IFilter> children = new ArrayList<IFilter>();
    children.add(filter1);
    children.add(filter2);
    f.setChildFilters(children);

    expect(filter1.getId()).andReturn(new Integer(4));
    expect(filter2.getId()).andReturn(new Integer(5));

    replay();

    classUnderTest.update(f);
    verify();
    verifyDatabaseTables("update");
  }

  public void testRemove() throws Exception {
    CombinedFilter f = new CombinedFilter();
    f.setId(6);

    classUnderTest.remove(f);
    verifyDatabaseTables("remove");
  }
}
