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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.rules.RuleFilterManager;
import eu.baltrad.beast.rules.PropertyManager;

public class DistributionRuleManagerTest extends TestCase {
  private MockControl propertyManagerControl;
  private PropertyManager propertyManager;
  private MockControl filterManagerControl;
  private RuleFilterManager filterManager;
  private MockControl filterControl;
  private IFilter filter;
  private DistributionRuleManager classUnderTest;

  public void setUp() {
    propertyManagerControl = MockClassControl.createControl(PropertyManager.class);
    propertyManager = (PropertyManager)propertyManagerControl.getMock();
    filterManagerControl = MockClassControl.createControl(RuleFilterManager.class);
    filterManager = (RuleFilterManager)filterManagerControl.getMock();
    filterControl = MockControl.createControl(IFilter.class);
    IFilter filter = (IFilter)filterControl.getMock();
    classUnderTest = new DistributionRuleManager();
    classUnderTest.setPropertyManager(propertyManager);
    classUnderTest.setRuleFilterManager(filterManager);
  }

  private void replay() {
    propertyManagerControl.replay();
    filterManagerControl.replay();
  }

  private void verify() {
    propertyManagerControl.verify();
    filterManagerControl.verify();
  }

  public void testDelete() {
    propertyManager.deleteProperties(1);
    filterManager.deleteFilters(1);
    replay();

    classUnderTest.delete(1);
    verify();
  }

  public void testLoad() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("destination", "ftp://u:p@h/d");
    Map<String, IFilter> filters = new HashMap<String, IFilter>();
    filters.put("match", filter);

    propertyManager.loadProperties(1);
    propertyManagerControl.setReturnValue(props);
    filterManager.loadFilters(1);
    filterManagerControl.setReturnValue(filters);
    replay();

    DistributionRule rule = (DistributionRule)classUnderTest.load(1);
    assertEquals("ftp://u:p@h/d", rule.getDestination().toString());
    assertSame(filter, rule.getFilter());
    verify();
  }

  public void testStore() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("destination", "ftp://u:p@h/d");
    Map<String, IFilter> filters = new HashMap<String, IFilter>();
    filters.put("match", filter);
    DistributionRule rule = new DistributionRule();
    rule.setDestination("ftp://u:p@h/d");
    rule.setFilter(filter);
    propertyManager.storeProperties(1, props);
    filterManager.storeFilters(1, filters);
    replay();

    classUnderTest.store(1, rule);
    verify();
  }

  public void testUpdate() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("destination", "ftp://u:p@h/d");
    Map<String, IFilter> filters = new HashMap<String, IFilter>();
    filters.put("match", filter);
    DistributionRule rule = new DistributionRule();
    rule.setDestination("ftp://u:p@h/d");
    rule.setFilter(filter);
    propertyManager.updateProperties(1, props);
    filterManager.updateFilters(1, filters);
    replay();

    classUnderTest.update(1, rule);
    verify();
  }
}
