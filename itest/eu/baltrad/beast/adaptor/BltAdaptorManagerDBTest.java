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

import java.util.List;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.easymock.MockControl;
import org.springframework.context.support.AbstractApplicationContext;

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
  AbstractApplicationContext dbcontext = null;
  AbstractApplicationContext context = null;
  private BltAdaptorManager classUnderTest = null;
  private BeastDBTestHelper helper = null;
  
  /**
   * Setup of test
   */
  public void setUp() throws Exception {
    dbcontext = BeastDBTestHelper.loadDbContext(this);
    helper = (BeastDBTestHelper)dbcontext.getBean("helper");
    helper.cleanInsert(this);
    context = BeastDBTestHelper.loadContext(this);
    classUnderTest = (BltAdaptorManager)context.getBean("adaptormgr");
  }
  
  /**
   * Teardown of test
   */
  public void tearDown() throws Exception {
    helper.tearDown();
    classUnderTest = null;
    context.close();
    dbcontext.close();
  }

  /**
   * Verifies the database table with an excel sheet.
   * @param extras
   * @throws Exception
   */
  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_adaptors");
    ITable actual = helper.getDatabaseTable("beast_adaptors");
    Assertion.assertEquals(expected, actual);

    expected = helper.getXlsTable(this, extras, "beast_adaptors_xmlrpc");
    actual = helper.getDatabaseTable("beast_adaptors_xmlrpc");
    Assertion.assertEquals(expected, actual);
  }

  public void testAfterPropertiesSet() throws Exception {
    List<String> names = classUnderTest.getAdaptorNames();
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
    assertEquals("http://something/else", result.getUrl());
  }

  public void testReregister_sameType() throws Exception {
    XmlRpcAdaptorConfiguration config = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("XMLRPC", "A3");
    config.setURL("http://someone/somewhere/somewhereelse");
    config.setTimeout(1000);
    
    // Execute test
    IAdaptor result = classUnderTest.reregister(config);
    
    // verify
    verifyDatabaseTables("reregister-sametype");
    assertTrue(result.getClass() == XmlRpcAdaptor.class);
    assertEquals("A3", result.getName());
    assertEquals("http://someone/somewhere/somewhereelse",
        ((XmlRpcAdaptor)classUnderTest.getAdaptor("A3")).getUrl());
  }

  public void testReregister_differentType() throws Exception {
    // Just create a phony http manager...
    MockControl httpAdaptorControl = MockControl.createControl(IAdaptorConfigurationManager.class);
    IAdaptorConfigurationManager httpAdaptorManager = (IAdaptorConfigurationManager)httpAdaptorControl.getMock();
    
    XmlRpcAdaptorConfiguration config = (XmlRpcAdaptorConfiguration)classUnderTest.createConfiguration("XMLRPC", "A1");
    config.setURL("http://someone/somewhere/somewhereelse");
    config.setTimeout(1000);
    
    classUnderTest.getTypeRegistry().put("HTTP", httpAdaptorManager);
    
    httpAdaptorManager.remove(1);
    
    httpAdaptorControl.replay();
    
    // Execute test
    IAdaptor result = classUnderTest.reregister(config);
    
    // verify
    httpAdaptorControl.verify();
    verifyDatabaseTables("reregister-differenttype");
    assertTrue(result.getClass() == XmlRpcAdaptor.class);
    assertEquals("A1", result.getName());
    assertEquals("http://someone/somewhere/somewhereelse",
        ((XmlRpcAdaptor)classUnderTest.getAdaptor("A1")).getUrl());
  }
  
  
  public void testUnregister() throws Exception {
    // Execute test
    classUnderTest.unregister("A2");
    
    // verify
    verifyDatabaseTables("unregister");
    assertEquals(null, classUnderTest.getAdaptor("A2"));
  }
}
