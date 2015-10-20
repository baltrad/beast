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

import static org.easymock.EasyMock.expect;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcOperations;

public class CombinedFilterManagerTest extends EasyMockSupport {
  private IFilterManager childManager;
  private JdbcOperations jdbcOps;
  private CombinedFilterManager classUnderTest;

  @Before
  public void setUp() {
    childManager = createMock(IFilterManager.class);
    jdbcOps = createMock(JdbcOperations.class);
    classUnderTest = new CombinedFilterManager();
    classUnderTest.setJdbcTemplate(jdbcOps);
    classUnderTest.setChildManager(childManager);
  }

  @Test
  public void testStore() {
    CombinedFilter f = new CombinedFilter();
    f.setMatchType(CombinedFilter.MatchType.ALL);
    f.setId(3);
    IFilter cf1 = createMock(IFilter.class);
    IFilter cf2 = createMock(IFilter.class);
    f.addChildFilter(cf1);
    f.addChildFilter(cf2);

    expect(cf1.getId()).andReturn(1);
    expect(cf2.getId()).andReturn(2);
    
    expect(jdbcOps.update(
      "insert into beast_combined_filters(filter_id, match_type) values (?, ?)",
      new Object[]{3, "ALL"})).andReturn(1);
    childManager.store(cf1);
    childManager.store(cf2);
    
    expect(jdbcOps.update(
      "insert into beast_combined_filter_children (filter_id, child_id) values (?, ?)",
      new Object[]{3, 1})).andReturn(1);
    
    expect(jdbcOps.update(
      "insert into beast_combined_filter_children (filter_id, child_id) values (?, ?)",
      new Object[]{3, 2})).andReturn(1);

    replayAll();

    classUnderTest.store(f);
    
    verifyAll();
  }

  @Test
  public void testRemove() {
    CombinedFilter f = new CombinedFilter();
    f.setId(10);
    IFilter cf1 = createMock(IFilter.class);
    IFilter cf2 = createMock(IFilter.class);
    f.addChildFilter(cf1);
    f.addChildFilter(cf2);
    
    expect(jdbcOps.update(
      "delete from beast_combined_filter_children where filter_id=?",
      new Object[]{10})).andReturn(0);
    
    childManager.remove(cf1);
    childManager.remove(cf2);
    
    expect(jdbcOps.update(
      "delete from beast_combined_filters where filter_id=?",
      new Object[]{10})).andReturn(0);

    replayAll();

    classUnderTest.remove(f);
    
    verifyAll();
  }
}
