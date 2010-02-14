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

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.dbunit.DBTestCase;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.operation.DatabaseOperation;

import eu.baltrad.beast.router.impl.BltRouter;

import junit.framework.TestCase;


/**
 * Tests the database routines
 * @author Anders Henja
 */
public class BltRouterDBTest extends DBTestCase {
  private BltRouter classUnderTest = null;
  private BasicDataSource source = null;
  
  public BltRouterDBTest(String name) {
    super(name);
    source = new BasicDataSource();
    source.setDriverClassName("org.hsqldb.jdbcDriver");
    source.setUrl("jdbc:hsqldb:sample");
    source.setUsername("sa");
    source.setPassword("");
  }

  /**
   * @see org.dbunit.DatabaseTestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    classUnderTest = new BltRouter();
    classUnderTest.setDataSource(source);
  }

  /**
   * @see org.dbunit.DatabaseTestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    classUnderTest = null;
  }

  
  /**
   * @see org.dbunit.DatabaseTestCase#getDataSet()
   */
  @Override
  protected IDataSet getDataSet() throws Exception {
    File f = new File(this.getClass().getResource("BltRouterDBTest.xls").getFile());
    return new XlsDataSet(f);
  }
  
  /**
   * @see org.dbunit.DatabaseTestCase#getTearDownOperation()
   */
  @Override
  protected DatabaseOperation getTearDownOperation() throws Exception
  {
    return DatabaseOperation.DELETE;
  }
  
  public void testDeleteDefinition() throws Exception {
    classUnderTest.deleteDefinition("X2");
  }

  /**
   * @see org.dbunit.DatabaseTestCase#getDatabaseTester()
   */
  @Override
  protected IDatabaseTester getDatabaseTester() throws Exception {
    DataSourceDatabaseTester tester = new DataSourceDatabaseTester(source);
    return tester;
  }
}
