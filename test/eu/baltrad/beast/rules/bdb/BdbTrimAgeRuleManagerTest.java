/* --------------------------------------------------------------------
Copyright (C) 2009-2010 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.rules.bdb;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.PropertyManager;


public class BdbTrimAgeRuleManagerTest extends TestCase {
  private BdbTrimAgeRuleManager classUnderTest = null;
  private MockControl methodsControl = null;
  private BdbTrimAgeRuleManagerMethods methods = null;
  private MockControl managerControl = null;
  private PropertyManager manager = null;
  private MockControl ruleControl = null;
  private BdbTrimAgeRule rule = null;
  private Map<String, String> props = null;

  private static interface BdbTrimAgeRuleManagerMethods {
    public BdbTrimAgeRule createRule();
  }

  public void setUp() throws Exception {
    classUnderTest = new BdbTrimAgeRuleManager();
    methodsControl = MockControl.createControl(BdbTrimAgeRuleManagerMethods.class);
    methods = (BdbTrimAgeRuleManagerMethods)methodsControl.getMock();
    managerControl = MockClassControl.createControl(PropertyManager.class);
    manager = (PropertyManager)managerControl.getMock();
    ruleControl = MockClassControl.createControl(BdbTrimAgeRule.class);
    rule = (BdbTrimAgeRule)ruleControl.getMock();
    classUnderTest.setPropertyManager(manager);
    props = new HashMap<String, String>();
  }

  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  protected void replay() {
    methodsControl.replay();
    managerControl.replay();
    ruleControl.replay();
  }

  protected void verify() {
    methodsControl.verify();
    managerControl.verify();
    ruleControl.verify();
  }

  public void testDelete() {
    manager.deleteProperties(1);
    replay();

    classUnderTest.delete(1);
    verify();
  }

  public void testLoad() {
    classUnderTest = new BdbTrimAgeRuleManager() {
      protected BdbTrimAgeRule createRule() {
        return methods.createRule();
      }
    };
    classUnderTest.setPropertyManager(manager);

    manager.loadProperties(1);
    managerControl.setReturnValue(props);
    methods.createRule();
    methodsControl.setReturnValue(rule);
    rule.setProperties(props);
    replay();

    IRule result = classUnderTest.load(1);
    verify();
    assertSame(result, rule);
  }

  public void testStore() {
    rule.getProperties();
    ruleControl.setReturnValue(props);
    manager.storeProperties(1, props);
    replay();

    classUnderTest.store(1, rule);
    verify();
  }

  public void testUpdate() {
    rule.getProperties();
    ruleControl.setReturnValue(props);
    manager.updateProperties(1, props);
    replay();

    classUnderTest.update(1, rule);
    verify();
  }
}
