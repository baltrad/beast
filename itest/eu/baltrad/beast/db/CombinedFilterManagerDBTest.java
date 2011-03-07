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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;

import org.easymock.MockControl;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.itest.BeastDBTestHelper;

public class CombinedFilterManagerDBTest extends TestCase {
  private CombinedFilterManager classUnderTest;
  private ApplicationContext context;
  private BeastDBTestHelper helper;

  private MockControl filter1Control;
  private IFilter filter1;
  private MockControl filter2Control;
  private IFilter filter2;
  private MockControl childManagerControl;
  private IFilterManager childManager;

  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.cleanInsert(this);

    filter1Control = MockControl.createControl(IFilter.class);
    filter1 = (IFilter)filter1Control.getMock();
    filter2Control = MockControl.createControl(IFilter.class);
    filter2 = (IFilter)filter2Control.getMock();
    childManagerControl = MockControl.createControl(IFilterManager.class);
    childManager = (IFilterManager)childManagerControl.getMock();

    classUnderTest = new CombinedFilterManager();
    SimpleJdbcOperations template = (SimpleJdbcOperations)context.getBean("jdbcTemplate");
    classUnderTest.setJdbcTemplate(template);
    classUnderTest.setChildManager(childManager);
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
    filter1Control.replay();
    filter2Control.replay();
    childManagerControl.replay();
  }
  
  private void verify() {
    filter1Control.verify();
    filter2Control.verify();
    childManagerControl.verify();
  }

  public void testStore() throws Exception {
    CombinedFilter f = new CombinedFilter();
    List<IFilter> children = new ArrayList<IFilter>();
    children.add(filter1);
    children.add(filter2);
    f.setChildFilters(children);
    f.setId(8);
    f.setMatchType(CombinedFilter.MatchType.ANY);

    filter1.getId();
    filter1Control.setReturnValue(new Integer(1));
    filter2.getId();
    filter2Control.setReturnValue(new Integer(2));
    replay();
    
    classUnderTest.store(f);
    verify();
    verifyDatabaseTables("store");
  }

  public void testLoad() {
    childManager.load(1);
    childManagerControl.setReturnValue(filter1);
    childManager.load(2);
    childManagerControl.setReturnValue(filter2);
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

    filter1.getId();
    filter1Control.setReturnValue(new Integer(4));
    filter2.getId();
    filter2Control.setReturnValue(new Integer(5));
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
