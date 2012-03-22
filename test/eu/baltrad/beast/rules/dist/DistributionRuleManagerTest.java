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
package eu.baltrad.beast.rules.dist;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanInitializationException;

import eu.baltrad.bdb.storage.LocalStorage;
import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.rules.PropertyManager;
import eu.baltrad.beast.rules.RuleFilterManager;

public class DistributionRuleManagerTest extends EasyMockSupport {
  private PropertyManager propertyManager;
  private RuleFilterManager filterManager;
  private IFilter filter;
  private LocalStorage localStorage;
  private DistributionRuleManager classUnderTest;

  @Before
  public void setUp() {
    propertyManager = createMock(PropertyManager.class);
    filterManager = createMock(RuleFilterManager.class);
    filter = createMock(IFilter.class);
    localStorage = createMock(LocalStorage.class);
    classUnderTest = new DistributionRuleManager();
    classUnderTest.setPropertyManager(propertyManager);
    classUnderTest.setRuleFilterManager(filterManager);
    classUnderTest.setLocalStorage(localStorage);
  }

  @Test
  public void testDelete() {
    propertyManager.deleteProperties(1);
    filterManager.deleteFilters(1);
    replayAll();

    classUnderTest.delete(1);
    verifyAll();
  }

  @Test
  public void testLoad() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("destination", "ftp://u:p@h/d");
    Map<String, IFilter> filters = new HashMap<String, IFilter>();
    filters.put("match", filter);

    expect(propertyManager.loadProperties(1)).andReturn(props);
    expect(filterManager.loadFilters(1)).andReturn(filters);

    replayAll();

    DistributionRule rule = (DistributionRule)classUnderTest.load(1);
    
    verifyAll();
    assertEquals("ftp://u:p@h/d", rule.getDestination().toString());
    assertSame(filter, rule.getFilter());
    assertSame(localStorage, rule.getLocalStorage());
  }

  @Test
  public void testStore() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("destination", "ftp://u:p@h/d");
    Map<String, IFilter> filters = new HashMap<String, IFilter>();
    filters.put("match", filter);
    DistributionRule rule = new DistributionRule(localStorage);
    rule.setDestination("ftp://u:p@h/d");
    rule.setFilter(filter);
    
    propertyManager.storeProperties(1, props);
    filterManager.storeFilters(1, filters);
    
    replayAll();

    classUnderTest.store(1, rule);
    
    verifyAll();
  }

  @Test
  public void testUpdate() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("destination", "ftp://u:p@h/d");
    Map<String, IFilter> filters = new HashMap<String, IFilter>();
    filters.put("match", filter);
    DistributionRule rule = new DistributionRule(localStorage);
    rule.setDestination("ftp://u:p@h/d");
    rule.setFilter(filter);
    propertyManager.updateProperties(1, props);
    filterManager.updateFilters(1, filters);
    
    replayAll();

    classUnderTest.update(1, rule);
    
    verifyAll();
  }

  @Test
  public void testAfterPropertiesSet() {
    classUnderTest = new DistributionRuleManager();
    classUnderTest.setLocalStorage(localStorage);
    classUnderTest.afterPropertiesSet();
  }
  
  @Test
  public void testAfterPropertiesSet_missingCatalog() {
    classUnderTest = new DistributionRuleManager();
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected BeanInitializationException");
    } catch (BeanInitializationException e) {
      // pass
    }
  }
}
