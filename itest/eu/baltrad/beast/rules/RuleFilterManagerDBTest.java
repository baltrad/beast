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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.itest.BeastDBTestHelper;

import eu.baltrad.bdb.expr.Expression;

import eu.baltrad.beast.db.CoreFilterManager;
import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.db.IFilterManager;

public class RuleFilterManagerDBTest extends TestCase {
  private RuleFilterManager classUnderTest = null;
  private AbstractApplicationContext context = null;
  private BeastDBTestHelper helper = null;
  private CoreFilterManager coreFilterManager = null;

  public RuleFilterManagerDBTest(String name) {
    super(name);
  }

  private class FakeFilter implements IFilter {
    Integer id;

    @Override
    public Integer getId() { return id; }

    @Override
    public void setId(Integer id) { this.id = id; }

    @Override
    public String getType() { return "fake"; }

    @Override
    public Expression getExpression() { return null; }

    @Override
    public boolean isValid() { return true; }

  };

  private class FakeFilterManager implements IFilterManager {

    @Override
    public void store(IFilter filter) { }

    @Override
    public IFilter load(int id) { return new FakeFilter(); }

    @Override
    public void update(IFilter filter) { }

    @Override
    public void remove(IFilter filter) { }

  };

  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.cleanInsert(this);
    SimpleJdbcOperations template = (SimpleJdbcOperations)context.getBean("jdbcTemplate");
    coreFilterManager = new CoreFilterManager();
    Map<String, IFilterManager> subManagers = new HashMap<String, IFilterManager>();
    subManagers.put("fake", new FakeFilterManager());
    coreFilterManager.setSubManagers(subManagers);
    coreFilterManager.setJdbcTemplate(template);
    classUnderTest = new RuleFilterManager();
    classUnderTest.setSimpleJdbcOperations(template);
    classUnderTest.setFilterManager(coreFilterManager);
  }

  public void tearDown() throws Exception {
    helper = null;
    classUnderTest = null;
    context.close();
  }

  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_rule_filters");
    ITable actual = helper.getDatabaseTable("beast_rule_filters");
    Assertion.assertEquals(expected, actual);

    expected = helper.getXlsTable(this, extras, "beast_filters");
    actual = helper.getDatabaseTable("beast_filters");
    Assertion.assertEquals(expected, actual);
  }

  public void testDeleteFilters() throws Exception {
    classUnderTest.deleteFilters(2);

    verifyDatabaseTables("deleteFilters");
  }

  public void testLoadFilters() {
    Map<String, IFilter> filters = classUnderTest.loadFilters(2);
    assertEquals(2, filters.size());
    assertEquals(2, (int)filters.get("key1").getId());
    assertEquals(3, (int)filters.get("key2").getId());
  }

  public void testStoreFilters() {
    Map<String, IFilter> filters = new HashMap<String, IFilter>();
    filters.put("key4", new FakeFilter());
    filters.put("key5", new FakeFilter());

    classUnderTest.storeFilters(2, filters);
  }

  public void testUpdateFilters() {
    Map<String, IFilter> filters = new HashMap<String, IFilter>();
    filters.put("key4", new FakeFilter());
    filters.put("key5", new FakeFilter());

    classUnderTest.storeFilters(2, filters);
  }
}
