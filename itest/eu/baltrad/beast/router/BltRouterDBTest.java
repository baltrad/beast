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
package eu.baltrad.beast.router;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.router.impl.BltRouter;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRuleFactory;
import eu.baltrad.beast.rules.RuleException;


/**
 * Tests the database routines
 * @author Anders Henja
 */
public class BltRouterDBTest extends TestCase {
  private BltRouter classUnderTest = null;
  private ApplicationContext context = null;
  private BeastDBTestHelper helper = null;
  
  public BltRouterDBTest(String name) {
    super(name);
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("testHelper");
  }

  private class DummyRule implements IRule {
    private String type = null;
    private String definition = null;
    public DummyRule(String type, String definition) {
      this.type = type;
      this.definition = definition;
    }
    public String getDefinition() {
      return this.definition;
    }
    public String getType() {
      return this.type;
    }
    public IBltMessage handle(IBltMessage message) {return null;}
  };
  
  public class DummyFactory implements IRuleFactory {
    public IRule create(String type, String definition) {
      return new DummyRule(type, definition);
    }
    public String[] getTypes() {
      return new String[]{"dummy"};
    }
    
  };
  
  /**
   * Setup of test
   */
  public void setUp() throws Exception {
    helper.cleanInsert(this);
    classUnderTest = new BltRouter();
    classUnderTest.setDataSource(helper.getSource());
    classUnderTest.setRuleFactory(new DummyFactory());
    classUnderTest.afterPropertiesSet();
  }
  
  /**
   * Teardown of test
   */
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  /**
   * Verifies the database table with an excel sheet.
   * @param extras
   * @throws Exception
   */
  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "router_rules");
    ITable actual = helper.getDatabaseTable("router_rules");
    Assertion.assertEquals(expected, actual);

    expected = helper.getXlsTable(this, extras, "adaptors");
    actual = helper.getDatabaseTable("adaptors");
    Assertion.assertEqualsIgnoreCols(expected, actual, new String[]{"adaptor_id"});

    expected = helper.getXlsTable(this, extras, "router_dest");
    actual = helper.getDatabaseTable("router_dest");
    Assertion.assertEquals(expected, actual);
  }

  public void testAfterPropertiesSet() throws Exception {
    List<RouteDefinition> result = classUnderTest.getDefinitions();
    assertEquals(3, result.size());
    RouteDefinition x1 = classUnderTest.getDefinition("X1");
    RouteDefinition x2 = classUnderTest.getDefinition("X2");
    RouteDefinition x3 = classUnderTest.getDefinition("X3");
    assertNotNull(x1);
    assertNotNull(x2);
    assertNotNull(x3);
  }
  
  public void testGetDefinition() throws Exception {
    RouteDefinition result = classUnderTest.getDefinition("X2");
    assertEquals("X2", result.getName());
    assertEquals("test", result.getRule().getType());
    assertEquals("Nils", result.getAuthor());
    assertEquals("nisses test", result.getDescription());
    assertEquals(true, result.isActive());
    assertEquals("nothing", result.getRule().getDefinition());
  }
  
  public void testDeleteDefinition() throws Exception {
    classUnderTest.deleteDefinition("X2");
    verifyDatabaseTables("deleteDefinition");
    RouteDefinition result = classUnderTest.getDefinition("X2");
    assertNull(result);
  }
  
  public void testStoreDefinition() throws Exception {
    List<String> recipients = new ArrayList<String>();
    recipients.add("A2");
    recipients.add("A3");
    
    RouteDefinition def = new RouteDefinition();
    def.setActive(false);
    def.setAuthor("tester");
    def.setDescription("test description");
    def.setName("X4");
    def.setRecipients(recipients);
    
    DummyRule rule = new DummyRule("test", "test definition");
    def.setRule(rule);
    
    classUnderTest.storeDefinition(def);

    verifyDatabaseTables("storeDefinition");
    RouteDefinition result = classUnderTest.getDefinition("X4");
    assertNotNull(result);
  }

  public void testStoreDefinition_duplicate() throws Exception {
    List<String> recipients = new ArrayList<String>();
    recipients.add("A1");
    
    RouteDefinition def = new RouteDefinition();
    def.setName("X1");
    def.setActive(false);
    def.setAuthor("tester");
    def.setDescription("test description");
    def.setRecipients(recipients);
    
    DummyRule rule = new DummyRule("test", "test definition");
    def.setRule(rule);
    
    try {
      classUnderTest.storeDefinition(def);
      fail("Expected RuleException");
    } catch (RuleException e) {
      // pass
    }

    verifyDatabaseTables(null);
    RouteDefinition result = classUnderTest.getDefinition("X1");
    assertEquals("Karl", result.getAuthor());
  }
  
  
  public void testUpdateDefinition() throws Exception {
    RouteDefinition def = classUnderTest.getDefinition("X2");
    def.setDescription("scoobys test");
    def.setActive(false);
    def.setAuthor("scooby");
    IRule rule = new DummyRule("ntest", "yada");
    def.setRule(rule);
    
    List<String> recipients = new ArrayList<String>();
    recipients.add("A2");
    def.setRecipients(recipients);
    
    classUnderTest.updateDefinition(def);
    
    verifyDatabaseTables("updateDefinition");
    RouteDefinition result = classUnderTest.getDefinition("X2");
    assertEquals(false, result.isActive());
    assertEquals("scoobys test", result.getDescription());
    assertEquals("scooby", result.getAuthor());
    rule = result.getRule();
    assertEquals("ntest", rule.getType());
    assertEquals("yada", rule.getDefinition());
  }
}
