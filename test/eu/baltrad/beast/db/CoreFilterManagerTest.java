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

import org.easymock.MockControl;

public class CoreFilterManagerTest extends TestCase {
  private static interface IMethods {
    Map<String, Object> sqlSelectFilter(int id);
    int sqlInsertFilter(String filterType);
    void sqlDeleteFilter(int id);
  };

  private MockControl methodsControl;
  private IMethods methods;
  private MockControl filterManagerControl;
  private IFilterManager filterManager;
  private MockControl filterControl;
  private IFilter filter;
  private Map<String, IFilterManager> filterManagerMap;
  private CoreFilterManager classUnderTest;

  public void setUp() throws Exception {
    methodsControl = MockControl.createControl(IMethods.class);
    methods = (IMethods)methodsControl.getMock();
    filterManagerControl = MockControl.createControl(IFilterManager.class);
    filterManager = (IFilterManager)filterManagerControl.getMock();
    filterControl = MockControl.createControl(IFilter.class);
    filter = (IFilter)filterControl.getMock();

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

  private void replay() {
    methodsControl.replay();
    filterManagerControl.replay();
    filterControl.replay();
  }

  private void verify() {
    methodsControl.verify();
    filterManagerControl.verify();
    filterControl.verify();
  }

  public void testStore() {
    
  }

  public void testLoad() {
    Map<String, Object> values = new HashMap<String, Object>();
    values.put("type", "mock");
    
    methods.sqlSelectFilter(1);
    methodsControl.setReturnValue(values);
    filterManager.load(1);
    filterManagerControl.setReturnValue(filter);
    filter.setId(1);
    replay();

    IFilter f = classUnderTest.load(1);
    verify();
    assertSame(f, filter);
  }

  public void testUpdate() {
    filter.getType();
    filterControl.setReturnValue("mock");
    filterManager.update(filter);
    replay();
    
    classUnderTest.update(filter);
    verify();
  }

  public void testRemove() {
    filter.getType();
    filterControl.setReturnValue("mock", MockControl.ONE_OR_MORE);
    filter.getId();
    filterControl.setReturnValue(new Integer(1), MockControl.ONE_OR_MORE);
    filterManager.remove(filter);
    methods.sqlDeleteFilter(1);
    filter.setId(null);
    replay();
    
    classUnderTest.remove(filter);
    verify();
  }
}
