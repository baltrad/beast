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
package eu.baltrad.beast.adaptor;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.adaptor.xmlrpc.XmlRpcAdaptor;
import eu.baltrad.beast.adaptor.xmlrpc.XmlRpcAdaptorConfiguration;
import eu.baltrad.beast.adaptor.xmlrpc.XmlRpcConfigurationManager;
import eu.baltrad.beast.itest.BeastDBTestHelper;
import junit.framework.TestCase;

/**
 * @author Anders Henja
 *
 */
public class BltAdaptorManagerDBTest extends TestCase {
  private BltAdaptorManager classUnderTest = null;
  private ApplicationContext context = null;
  private BeastDBTestHelper helper = null;
  
  public BltAdaptorManagerDBTest(String name) {
    super(name);
    //context = BeastDBTestHelper.loadContext(this);
    //classUnderTest = (BltAdaptorManager)context.getBean("adaptorManager");
    //helper = (BeastDBTestHelper)context.getBean("testHelper");
  }
  
  /**
   * Setup of test
   */
  public void setUp() throws Exception {
    //helper.cleanInsert(this);
  }
  
  /**
   * Teardown of test
   */
  public void tearDown() throws Exception {
  }
  
  public void testThis() {
    
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
  }

  public void XtestRegister() throws Exception {
    XmlRpcAdaptorConfiguration config = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("XMLRPC", "ABC");
    config.setURL("http://someone/somewhere/somewhereelse");
    config.setTimeout(1000);
    
    // Execute test
    IAdaptor result = classUnderTest.register(config);
    
    // verify
    verifyDatabaseTables("register");
    assertTrue(result.getClass() == XmlRpcAdaptor.class);
    assertEquals("ABC", result.getName());
    
/*    
    XmlRpcAdaptorConfiguration config = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("ABC");
    config.setURL("http://someone/somewhere/insummertime");
    config.setTimeout(8000);
    
    XmlRpcAdaptor result = (XmlRpcAdaptor)classUnderTest.store(4, config);

    verifyDatabaseTables("store");
    assertNotNull(result);
    assertEquals("http://someone/somewhere/insummertime", result.getURL());
    assertEquals(8000, result.getTimeout());
*/    
  }
}
