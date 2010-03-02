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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.adaptor.xmlrpc.XmlRpcAdaptor;
import eu.baltrad.beast.adaptor.xmlrpc.XmlRpcAdaptorConfiguration;
import eu.baltrad.beast.itest.BeastDBTestHelper;

/**
 * Verifies that the BltAdaptorManager uses the registered adaptors properly and
 * ensures that the database tables are updated accordingly. Uses the XMLRPC
 * adaptor implementation for performing these tests.
 * @author Anders Henja
 */
public class BltAdaptorManagerDBTest extends TestCase {
  private BltAdaptorManager classUnderTest = null;
  private ApplicationContext context = null;
  private BeastDBTestHelper helper = null;
  
  public BltAdaptorManagerDBTest(String name) {
    super(name);
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("testHelper");
  }
  
  /**
   * Setup of test
   */
  public void setUp() throws Exception {
   helper.cleanInsert(this);
   List<IAdaptorConfigurationManager> types = new ArrayList<IAdaptorConfigurationManager>();
   types.add((IAdaptorConfigurationManager)context.getBean("xmlrpcmgr"));
   
   classUnderTest = new BltAdaptorManager();
   classUnderTest.setDataSource((DataSource)context.getBean("dataSource"));
   classUnderTest.setTypeRegistry(types);
   classUnderTest.afterPropertiesSet();
  }
  
  /**
   * Teardown of test
   */
  public void tearDown() throws Exception {
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

  public void testAfterPropertiesSet() throws Exception {
    Set<String> names = classUnderTest.getAvailableAdaptors();
    assertEquals(true, names.contains("A2"));
    assertEquals(true, names.contains("A3"));
  }
  
  public void testRegister() throws Exception {
    XmlRpcAdaptorConfiguration config = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("XMLRPC", "ABC");
    config.setURL("http://someone/somewhere/somewhereelse");
    config.setTimeout(1000);
    
    // Execute test
    IAdaptor result = classUnderTest.register(config);
    
    // verify
    verifyDatabaseTables("register");
    assertTrue(result.getClass() == XmlRpcAdaptor.class);
    assertEquals("ABC", result.getName());
    assertEquals("ABC", classUnderTest.getAdaptor("ABC").getName());
  }
  
  public void testRegister_duplicate() throws Exception {
    XmlRpcAdaptorConfiguration config = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("XMLRPC", "A3");
    config.setURL("http://someone/somewhere/somewhereelse");
    config.setTimeout(1000);
    
    // Execute test
    try {
      classUnderTest.register(config);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
    verifyDatabaseTables(null);
    XmlRpcAdaptor result = (XmlRpcAdaptor)classUnderTest.getAdaptor("A3");
    assertTrue(result.getClass() == XmlRpcAdaptor.class);
    assertEquals("A3", result.getName());
    assertEquals("http://something/else", result.getURL());
  }
  
  public void testUnregister() throws Exception {
    // Execute test
    classUnderTest.unregister("A2");
    
    // verify
    verifyDatabaseTables("unregister");
    assertEquals(null, classUnderTest.getAdaptor("A2"));
  }
}
