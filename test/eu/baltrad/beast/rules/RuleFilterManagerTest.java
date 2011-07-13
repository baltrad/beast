/* --------------------------------------------------------------------
Copyright (C) 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.MockControl;

import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.db.IFilterManager;

public class RuleFilterManagerTest extends TestCase {
  private static interface RuleFilterManagerMethods {
    List<Integer> getRuleFilterIds(int ruleId);
    Map<String, Integer> getRuleFilterKeyIdMap(int ruleId);
    void deleteFilters(int ruleId);
    void storeFilters(int ruleId, Map<String, IFilter> filters);
  };

  private MockControl methodsControl;
  private RuleFilterManagerMethods methods;
  private MockControl jdbcControl;
  private SimpleJdbcOperations jdbc;
  private MockControl filterManagerControl;
  private IFilterManager filterManager;
  private RuleFilterManager classUnderTest;

  protected void setUp() throws Exception {
    methodsControl = MockControl.createControl(RuleFilterManagerMethods.class);
    methods = (RuleFilterManagerMethods)methodsControl.getMock();
    jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    jdbcControl.setDefaultMatcher(MockControl.ARRAY_MATCHER);
    jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    filterManagerControl = MockControl.createControl(IFilterManager.class);
    filterManager = (IFilterManager)filterManagerControl.getMock();
    classUnderTest = new RuleFilterManager() {
      @Override
      protected List<Integer> getRuleFilterIds(int ruleId) {
        return methods.getRuleFilterIds(ruleId);
      }
      @Override
      protected Map<String, Integer> getRuleFilterKeyIdMap(int ruleId) {
        return methods.getRuleFilterKeyIdMap(ruleId);
      }
    };
    classUnderTest.setSimpleJdbcOperations(jdbc);
    classUnderTest.setFilterManager(filterManager);
  }

  protected void tearDown() throws Exception {
  }

  protected void replay() {
    methodsControl.replay();
    jdbcControl.replay();
    filterManagerControl.replay();
  }

  protected void verify() {
    methodsControl.verify();
    jdbcControl.verify();
    filterManagerControl.verify();
  }

  public void testDeleteFilters() {
    IFilter filter1 = createFakeFilter(10);
    IFilter filter2 = createFakeFilter(20);
    List<Integer> filterIds = new ArrayList<Integer>();
    filterIds.add(filter1.getId());
    filterIds.add(filter2.getId());
    
    methods.getRuleFilterIds(1);
    methodsControl.setReturnValue(filterIds);
    jdbc.update(
      "delete from beast_rule_filters where rule_id=?",
      new Object[]{1}
    );
    jdbcControl.setReturnValue(1);
    filterManager.load(10);
    filterManagerControl.setReturnValue(filter1);
    filterManager.remove(filter1);
    filterManager.load(20);
    filterManagerControl.setReturnValue(filter2);
    filterManager.remove(filter2);
    replay();
    
    classUnderTest.deleteFilters(1);
    verify();
  }

  public void testLoadFilters() {
    IFilter filter1 = createFakeFilter(10);
    IFilter filter2 = createFakeFilter(20);
    HashMap<String, Integer> filterKeyIdMap = new HashMap<String, Integer>();
    filterKeyIdMap.put("key1", filter1.getId());
    filterKeyIdMap.put("key2", filter2.getId());

    methods.getRuleFilterKeyIdMap(1);
    methodsControl.setReturnValue(filterKeyIdMap);
    filterManager.load(10);
    filterManagerControl.setReturnValue(filter1);
    filterManager.load(20);
    filterManagerControl.setReturnValue(filter2);
    replay();

    Map<String, IFilter> filters = classUnderTest.loadFilters(1);
    assertSame(filter1, filters.get("key1"));
    assertSame(filter2, filters.get("key2"));
    verify();
  }

  public void testStoreFilters() {
    Map<String, IFilter> filters = new HashMap<String, IFilter>();
    IFilter filter1 = createFakeFilter(1);
    IFilter filter2 = createFakeFilter(2);
    filters.put("key1", filter1);
    filters.put("key2", filter2);

    filterManager.store(filter1);
    jdbc.update(
        "insert into beast_rule_filters " +
        "(rule_id, key, filter_id) values (?, ?, ?)",
        new Object[]{1, "key1", 1}
    );
    jdbcControl.setReturnValue(1);
    filterManager.store(filter2);
    jdbc.update(
        "insert into beast_rule_filters " +
        "(rule_id, key, filter_id) values (?, ?, ?)",
        new Object[]{1, "key2", 2}
    );
    jdbcControl.setReturnValue(1);
    replay();

    classUnderTest.storeFilters(1, filters);
  }

  public void testUpdateFilters() {
    classUnderTest = new RuleFilterManager() {
      @Override
      public void deleteFilters(int ruleId) {
        methods.deleteFilters(ruleId);
      }

      @Override
      public void storeFilters(int ruleId, Map<String, IFilter> filters) {
        methods.storeFilters(ruleId, filters);
      }
    };

    Map<String, IFilter> filters = new HashMap<String, IFilter>();
    IFilter filter1 = createFakeFilter(1);
    IFilter filter2 = createFakeFilter(2);
    filters.put("key1", filter1);
    filters.put("key2", filter2);

    methods.deleteFilters(1);
    methods.storeFilters(1, filters);
    replay();

    classUnderTest.updateFilters(1, filters);
    verify();
  }

  private IFilter createFakeFilter(Integer id) {
    MockControl filterControl = MockControl.createControl(IFilter.class);
    IFilter filter = (IFilter)filterControl.getMock();
    filter.getId();
    filterControl.setReturnValue(id);
    filterControl.replay();
    return filter;
  }
}
