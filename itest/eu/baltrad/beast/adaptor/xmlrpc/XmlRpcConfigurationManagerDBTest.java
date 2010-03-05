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
package eu.baltrad.beast.adaptor.xmlrpc;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.adaptor.AdaptorException;
import eu.baltrad.beast.itest.BeastDBTestHelper;

/**
 * @author Anders Henja
 *
 */
public class XmlRpcConfigurationManagerDBTest extends TestCase {
  private XmlRpcConfigurationManager classUnderTest = null;
  private ApplicationContext context = null;
  private BeastDBTestHelper helper = null;
  
  public XmlRpcConfigurationManagerDBTest(String name) {
    super(name);
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("testHelper");
  }
  
  /**
   * Setup of test
   */
  public void setUp() throws Exception {
    helper.cleanInsert(this);
    classUnderTest = new XmlRpcConfigurationManager();
    classUnderTest.setDataSource(helper.getSource());
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
    ITable expected = helper.getXlsTable(this, extras, "adaptors");
    ITable actual = helper.getDatabaseTable("adaptors");
    Assertion.assertEquals(expected, actual);

    expected = helper.getXlsTable(this, extras, "adaptors_xmlrpc");
    actual = helper.getDatabaseTable("adaptors_xmlrpc");
    Assertion.assertEquals(expected, actual);
  }
  
  public void testStore() throws Exception {
    XmlRpcAdaptorConfiguration config = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("ABC");
    config.setURL("http://someone/somewhere/insummertime");
    config.setTimeout(8000);
    
    XmlRpcAdaptor result = (XmlRpcAdaptor)classUnderTest.store(4, config);

    verifyDatabaseTables("store");
    assertNotNull(result);
    assertEquals("http://someone/somewhere/insummertime", result.getUrl());
    assertEquals(8000, result.getTimeout());
  }
  
  public void testStore_illegalAdaptorId() throws Exception {
    XmlRpcAdaptorConfiguration config = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("ABC");
    config.setURL("http://someone/somewhere/insummertime");
    config.setTimeout(8000);
    
    try {
      classUnderTest.store(5, config);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    verifyDatabaseTables(null);
  }
  
  public void testRemove() throws Exception {
    classUnderTest.remove(2);
    verifyDatabaseTables("remove");
  }
  
  public void testRemove_nonExisting() throws Exception {
    classUnderTest.remove(10);
    verifyDatabaseTables(null);
  }
}
