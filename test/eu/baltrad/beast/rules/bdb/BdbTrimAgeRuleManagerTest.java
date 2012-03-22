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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanInitializationException;

import eu.baltrad.bdb.FileCatalog;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.PropertyManager;

public class BdbTrimAgeRuleManagerTest extends EasyMockSupport {
  private BdbTrimAgeRuleManager classUnderTest = null;
  private BdbTrimAgeRuleManagerMethods methods = null;
  private PropertyManager manager = null;
  private FileCatalog fileCatalog = null;
  private BdbTrimAgeRule rule = null;
  private Map<String, String> props = null;

  private static interface BdbTrimAgeRuleManagerMethods {
    public BdbTrimAgeRule createRule();
  }

  @Before
  public void setUp() throws Exception {
    classUnderTest = new BdbTrimAgeRuleManager();
    methods = createMock(BdbTrimAgeRuleManagerMethods.class);
    manager = createMock(PropertyManager.class);
    rule = createMock(BdbTrimAgeRule.class);
    fileCatalog = createMock(FileCatalog.class);
    classUnderTest.setPropertyManager(manager);
    classUnderTest.setFileCatalog(fileCatalog);
    props = new HashMap<String, String>();
  }

  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  @Test
  public void testDelete() {
    manager.deleteProperties(1);
    replayAll();

    classUnderTest.delete(1);
    
    verifyAll();
  }

  @Test
  public void testLoad() {
    classUnderTest = new BdbTrimAgeRuleManager() {
      public BdbTrimAgeRule createRule() {
        return methods.createRule();
      }
    };
    classUnderTest.setPropertyManager(manager);
    classUnderTest.setFileCatalog(fileCatalog);

    expect(manager.loadProperties(1)).andReturn(props);
    expect(methods.createRule()).andReturn(rule);
    rule.setProperties(props);
    
    replayAll();

    IRule result = classUnderTest.load(1);
    
    verifyAll();
    assertSame(result, rule);
  }

  @Test
  public void testStore() {
    expect(rule.getProperties()).andReturn(props);
    manager.storeProperties(1, props);
    
    replayAll();

    classUnderTest.store(1, rule);
    
    verifyAll();
  }

  @Test
  public void testUpdate() {
    expect(rule.getProperties()).andReturn(props);
    manager.updateProperties(1, props);
    
    replayAll();

    classUnderTest.update(1, rule);
    
    verifyAll();
  }
  
  @Test
  public void testCreateRule() {
    BdbTrimAgeRule result = classUnderTest.createRule();
    assertSame(result.getFileCatalog(), fileCatalog);
  }
  
  @Test
  public void testCreateRule_noCatalog() {
    classUnderTest = new BdbTrimAgeRuleManager();
    try {
      classUnderTest.createRule();
      fail("Expected BeanInitializationException");
    } catch (BeanInitializationException e) {
      // pass
    }
  }
}
