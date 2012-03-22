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
import static org.junit.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

public class CoreFilterManagerTest extends EasyMockSupport {
  private static interface IMethods {
    Map<String, Object> sqlSelectFilter(int id);
    int sqlInsertFilter(String filterType);
    void sqlDeleteFilter(int id);
  };

  private IMethods methods;
  private IFilterManager filterManager;
  private IFilter filter;
  private Map<String, IFilterManager> filterManagerMap;
  private CoreFilterManager classUnderTest;

  @Before
  public void setUp() throws Exception {
    methods = createMock(IMethods.class);
    filterManager = createMock(IFilterManager.class);
    filter = createMock(IFilter.class);

    filterManagerMap = new HashMap<String, IFilterManager>();
    filterManagerMap.put("mock", filterManager);

    classUnderTest = new CoreFilterManager() {
      public Map<String, Object> sqlSelectFilter(int id) {
        return methods.sqlSelectFilter(id);
      }
      public int sqlInsertFilter(String filterType) {
        return methods.sqlInsertFilter(filterType);
      }
      public void sqlDeleteFilter(int id) {
        methods.sqlDeleteFilter(id);
      }
    };
    classUnderTest.setSubManagers(filterManagerMap);
  }

  @Test
  public void testLoad() {
    Map<String, Object> values = new HashMap<String, Object>();
    values.put("type", "mock");
    
    expect(methods.sqlSelectFilter(1)).andReturn(values);
    expect(filterManager.load(1)).andReturn(filter);
    filter.setId(1);
    
    replayAll();

    IFilter f = classUnderTest.load(1);
    verifyAll();
    assertSame(f, filter);
  }

  @Test
  public void testUpdate() {
    expect(filter.getType()).andReturn("mock");
    filterManager.update(filter);
    
    replayAll();
    
    classUnderTest.update(filter);
    
    verifyAll();
  }

  @Test
  public void testRemove() {
    expect(filter.getType()).andReturn("mock");
    expect(filter.getId()).andReturn(new Integer(1));
    filterManager.remove(filter);
    methods.sqlDeleteFilter(1);
    filter.setId(null);
    
    replayAll();
    
    classUnderTest.remove(filter);
    
    verifyAll();
  }
}
