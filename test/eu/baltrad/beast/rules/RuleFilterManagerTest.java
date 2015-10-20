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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcOperations;

import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.db.IFilterManager;

public class RuleFilterManagerTest extends EasyMockSupport {
  private static interface RuleFilterManagerMethods {
    List<Integer> getRuleFilterIds(int ruleId);
    Map<String, Integer> getRuleFilterKeyIdMap(int ruleId);
    void deleteFilters(int ruleId);
    void storeFilters(int ruleId, Map<String, IFilter> filters);
  };

  private RuleFilterManagerMethods methods;
  private JdbcOperations jdbc;
  private IFilterManager filterManager;
  private RuleFilterManager classUnderTest;

  @Before
  public void setUp() throws Exception {
    methods = createMock(RuleFilterManagerMethods.class);
    jdbc = createMock(JdbcOperations.class);
    filterManager = createMock(IFilterManager.class);
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

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testDeleteFilters() {
    IFilter filter1 = createMock(IFilter.class);
    IFilter filter2 = createMock(IFilter.class);
    List<Integer> filterIds = new ArrayList<Integer>();
    filterIds.add(10);
    filterIds.add(20);
    
    expect(methods.getRuleFilterIds(1)).andReturn(filterIds);
    
    expect(jdbc.update(
      "delete from beast_rule_filters where rule_id=?",
      new Object[]{1}
    )).andReturn(1);
    expect(filterManager.load(10)).andReturn(filter1);
    filterManager.remove(filter1);
    expect(filterManager.load(20)).andReturn(filter2);
    filterManager.remove(filter2);
    
    replayAll();
    
    classUnderTest.deleteFilters(1);
    
    verifyAll();
  }

  @Test
  public void testLoadFilters() {
    IFilter filter1 = createMock(IFilter.class);
    IFilter filter2 = createMock(IFilter.class);
    HashMap<String, Integer> filterKeyIdMap = new HashMap<String, Integer>();
    filterKeyIdMap.put("key1", new Integer(10));
    filterKeyIdMap.put("key2", new Integer(20));

    expect(methods.getRuleFilterKeyIdMap(1)).andReturn(filterKeyIdMap);
    expect(filterManager.load(10)).andReturn(filter1);
    expect(filterManager.load(20)).andReturn(filter2);

    replayAll();

    Map<String, IFilter> filters = classUnderTest.loadFilters(1);
    assertSame(filter1, filters.get("key1"));
    assertSame(filter2, filters.get("key2"));
    
    verifyAll();
  }

  @Test
  public void testStoreFilters() {
    Map<String, IFilter> filters = new HashMap<String, IFilter>();
    IFilter filter1 = createMock(IFilter.class);
    IFilter filter2 = createMock(IFilter.class);
    filters.put("key1", filter1);
    filters.put("key2", filter2);

    expect(filter1.getId()).andReturn(new Integer(1));
    expect(filter2.getId()).andReturn(new Integer(2));
    filterManager.store(filter1);
    expect(jdbc.update(
        "insert into beast_rule_filters " +
        "(rule_id, key, filter_id) values (?, ?, ?)",
        new Object[]{1, "key1", 1}
    )).andReturn(1);
    filterManager.store(filter2);
    expect(jdbc.update(
        "insert into beast_rule_filters " +
        "(rule_id, key, filter_id) values (?, ?, ?)",
        new Object[]{1, "key2", 2}
    )).andReturn(1);

    replayAll();

    classUnderTest.storeFilters(1, filters);
    
    verifyAll();
  }

  @Test
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
    IFilter filter1 = createMock(IFilter.class);
    IFilter filter2 = createMock(IFilter.class);
    filters.put("key1", filter1);
    filters.put("key2", filter2);

    methods.deleteFilters(1);
    methods.storeFilters(1, filters);
    
    replayAll();

    classUnderTest.updateFilters(1, filters);
    
    verifyAll();
  }
}
