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

import org.easymock.MockControl;

import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

public class CombinedFilterManagerTest extends TestCase {
  private MockControl childManagerControl;
  private IFilterManager childManager;
  private MockControl jdbcOpsControl;
  private SimpleJdbcOperations jdbcOps;
  private CombinedFilterManager classUnderTest;

  public void setUp() {
    childManagerControl = MockControl.createControl(IFilterManager.class);
    childManager = (IFilterManager)childManagerControl.getMock();
    jdbcOpsControl = MockControl.createControl(SimpleJdbcOperations.class);
    jdbcOpsControl.setDefaultMatcher(MockControl.ARRAY_MATCHER);
    jdbcOps = (SimpleJdbcOperations)jdbcOpsControl.getMock();
    classUnderTest = new CombinedFilterManager();
    classUnderTest.setJdbcTemplate(jdbcOps);
    classUnderTest.setChildManager(childManager);
  }

  private void replay() {
    childManagerControl.replay();
    jdbcOpsControl.replay();
  }

  private void verify() {
    childManagerControl.verify();
    jdbcOpsControl.verify();
  }

  private IFilter createFakeFilter(Integer id) {
    MockControl filterControl = MockControl.createControl(IFilter.class);
    IFilter filter = (IFilter)filterControl.getMock();
    filter.getId();
    filterControl.setReturnValue(id);
    filterControl.replay();
    return filter;
  }

  public void testRemove() {
    CombinedFilter f = new CombinedFilter();
    f.setId(10);
    IFilter cf1 = createFakeFilter(1);
    IFilter cf2 = createFakeFilter(2);
    f.addChildFilter(cf1);
    f.addChildFilter(cf2);
    
    jdbcOps.update(
      "delete from beast_combined_filter_children where filter_id=?",
      new Object[]{10}
    );
    jdbcOpsControl.setReturnValue(0);
    childManager.remove(cf1);
    childManager.remove(cf2);
    jdbcOps.update(
      "delete from beast_combined_filters where filter_id=?",
      new Object[]{10}
    );
    jdbcOpsControl.setReturnValue(0);
    replay();

    classUnderTest.remove(f);
    verify();
  }
}
