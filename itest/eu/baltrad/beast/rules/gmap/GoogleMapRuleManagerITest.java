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

package eu.baltrad.beast.rules.gmap;

import static org.junit.Assert.assertEquals;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.dao.DataAccessException;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import static org.junit.Assert.fail;

/**
 *
 * @author Anders Henja
 * @date Mar 23, 2012
 */
public class GoogleMapRuleManagerITest {
  private AbstractApplicationContext context = null;
  private BeastDBTestHelper helper = null;
  private GoogleMapRuleManager classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);

    helper = (BeastDBTestHelper)context.getBean("helper");
    helper.tearDown();
    helper.purgeBaltradDB();
    
    helper.cleanInsert(this);

    classUnderTest = (GoogleMapRuleManager)context.getBean("bltgmapmgr");
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
    helper = null;
    context.close();
  }
 
  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_gmap_rules");
    ITable actual = helper.getDatabaseTable("beast_gmap_rules");
    Assertion.assertEquals(expected, actual);
  }

  @Test
  public void testLoad_1() throws Exception  {
    GoogleMapRule rule = (GoogleMapRule)classUnderTest.load(1);
    assertEquals("sswe", rule.getArea());
    assertEquals("/tmp/path", rule.getPath());
    assertEquals(true, rule.isUseAreaInPath());
  }
  
  @Test
  public void testLoad_2() throws Exception {
    GoogleMapRule rule = (GoogleMapRule)classUnderTest.load(2);
    assertEquals("sswe_map", rule.getArea());
    assertEquals(null, rule.getPath());
    assertEquals(false, rule.isUseAreaInPath());
  }
  
  @Test
  public void testStore() throws Exception {
    GoogleMapRule rule = new GoogleMapRule();
    rule.setArea("nswe_map");
    rule.setPath("/tmp");
    rule.setUseAreaInPath(false);
    classUnderTest.store(4, rule);
    
    verifyDatabaseTables("store");
  }

  @Test
  public void testStore_noReferencedRule() throws Exception {
    GoogleMapRule rule = new GoogleMapRule();
    rule.setArea("nswe_map");
    rule.setPath("/tmp");
    
    try {
      classUnderTest.store(5, rule);
      fail("Expected DataAccessException");
    } catch (DataAccessException e) {
      // pass
    }
    
    verifyDatabaseTables(null);
  }

  @Test
  public void testStore_nullArea() throws Exception {
    GoogleMapRule rule = new GoogleMapRule();
    rule.setArea(null);
    rule.setPath("/tmp");
    
    try {
      classUnderTest.store(4, rule);
      fail("Expected DataAccessException");
    } catch (DataAccessException e) {
      // pass
    }
    
    verifyDatabaseTables(null);
  }
  
  @Test
  public void testUpdate() throws Exception {
    GoogleMapRule rule = new GoogleMapRule();
    rule.setArea("nswe_map");
    rule.setPath("/tmp");
    rule.setUseAreaInPath(false);
    
    classUnderTest.update(1, rule);
    
    verifyDatabaseTables("update");
  }
  
  @Test
  public void testDelete() throws Exception {
    classUnderTest.delete(1);
    verifyDatabaseTables("delete");
  }
}
