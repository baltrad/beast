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
package eu.baltrad.beast.itest;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Singleton helper function...
 * @author Anders Henja
 */
public class BeastDBTestHelper {
  private DataSource source = null;
  private IDataTypeFactory factory = null;
  private String baltradDbUri = null;
  private String baltradDbPth = null;
  
  /**
   * Default constructor
   */
  public BeastDBTestHelper() {
  }
  
  /**
   * Sets the baltrad db uri
   * @param uri the uri
   */
  public void setBaltradDbUri(String uri) {
    this.baltradDbUri = uri;
  }
  
  /**
   * Returns the baltrad db uri
   * @return the baltrad db uri
   */
  public String getBaltradDbUri() {
    return this.baltradDbUri;
  }

  /**
   * Sets the baltrad db path
   * @param pth the pth
   */
  public void setBaltradDbPth(String pth) {
    this.baltradDbPth = pth;
  }
  
  /**
   * Returns the baltrad db pth
   * @return the baltrad db pth
   */
  public String getBaltradDbPth() {
    return this.baltradDbPth;
  }
  
  
  /**
   * Setter for setting a data type factory used by dbunit
   * @param factory - the factory
   */
  public void setDataFactory(IDataTypeFactory factory) {
    this.factory = factory;
  }
  
  /**
   * Setter for setting the data source that should be used.
   * @param source the data source
   */
  public void setDataSource(DataSource source) {
    this.source = source;
  }
  
  /**
   * Returns an IDatabaseConnection
   * @param conn a database connection
   * @return the IDatabase connection
   * @throws Exception
   */
  protected IDatabaseConnection getConnection(Connection conn) throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(conn);
    connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, factory);    
    return connection;
  }
  
  /**
   * Extracts only the class name (no package included).
   * @param clz the class
   * @return the name
   */
  @SuppressWarnings("unchecked")
  public static String getClassName(Class clz) {
    String nm = clz.getName();
    int li = nm.lastIndexOf(".");
    if (li > 0) {
      nm = nm.substring(li+1);
    }
    return nm;
  }
  
  /**
   * The data source
   * @return the data source
   */
  public DataSource getSource() {
    return this.source;
  }
  
  /**
   * Loads an application context from a test case by using the
   * <ClassName>-context.xml and load it as a resource.
   * @param tc the test case
   * @return the application context
   */
  public static ApplicationContext loadContext(TestCase tc) {
    String cln = getClassName(tc.getClass());
    String cname = cln + "-context.xml";
    File f = new File(tc.getClass().getResource(cname).getFile());
    return new ClassPathXmlApplicationContext("file:"+f.getAbsolutePath());
  }
  
  /**
   * Performs a clean insert of the test case data
   * @param source
   * @param tc
   */
  public void cleanInsert(TestCase tc) throws Exception {
    Connection conn = source.getConnection();
    SimpleJdbcTemplate template = new SimpleJdbcTemplate(source);
    try {
      template.update("delete from beast_router_dest");
      template.update("delete from beast_groovy_rules");
      template.update("delete from beast_composite_sources");
      template.update("delete from beast_composite_rules");
      template.update("delete from beast_adaptors_xmlrpc");
      template.update("delete from beast_adaptors");
      template.update("delete from beast_router_rules");
      IDatabaseConnection connection = getConnection(conn);
      DatabaseOperation.CLEAN_INSERT.execute(connection, getXlsDataset(tc, null));
    } finally {
      DataSourceUtils.releaseConnection(conn, source);
    }   
  }
  
  /**
   * Creates a XlsDataSet from an excel file according to
   * <ClassName>[-<extras>].xls 
   * @param tc the test case
   * @param extras an extra string if wanted, e.g. result
   * @return the dataset
   * @throws Exception
   */
  public XlsDataSet getXlsDataset(TestCase tc, String extras) throws Exception {
    String cln = getClassName(tc.getClass());
    String cname = cln;
    if (extras != null) {
      cname = cname + "-" + extras;
    }
    cname = cname + ".xls";
    File f = new File(tc.getClass().getResource(cname).getFile());
    return new XlsDataSet(f);
  }
  
  /**
   * Returns the specified table from the database
   * @param source the data source
   * @param name the name of the table
   * @return the table
   * @throws Exception
   */
  public ITable getDatabaseTable(String name) throws Exception {
    Connection conn = source.getConnection();
    try {
      IDatabaseConnection connection = getConnection(conn);
      IDataSet dataset = connection.createDataSet();
      return dataset.getTable(name);
    } finally {
      DataSourceUtils.releaseConnection(conn, source);
    }     
  }
  
  /**
   * Returns a specific sheet from an excel document
   * @param tc the test case
   * @param extras the extras field
   * @param name the name of the sheet.
   * @return the table
   * @throws Exception
   */
  public ITable getXlsTable(TestCase tc, String extras, String name) throws Exception {
    IDataSet dataset = getXlsDataset(tc, extras);
    return dataset.getTable(name);
  }
  
  public void createBaltradDbPath() {
    File f = new File(getBaltradDbPth());
    if (!f.exists()) {
      f.mkdir();
    } else if (!f.isDirectory()) {
      throw new RuntimeException(""+getBaltradDbPth() + " exists but is not a directory");
    }
  }
  
  /**
   * Removes all entries from the baltrad db
   * @throws Exception
   */
  public void purgeBaltradDB() throws Exception {
    String pth = getBaltradDbPth();
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(java.io.File dir, String name) {
        return name.endsWith(".h5");
      }
    };
    java.io.File dir = new java.io.File(pth);
    String[] list = dir.list(filter);
    for (String n : list) {
      (new java.io.File(pth, n)).delete();
    }

    SimpleJdbcTemplate template = new SimpleJdbcTemplate(getSource());
    template.update("delete from bdb_files");
  }
  
  /**
   * Prints the classpath for debugging purposes.
   */
  public void printClasspath() {
    ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
    URL[] urls = ((URLClassLoader)sysClassLoader).getURLs();
    for(int i=0; i< urls.length; i++) {
      System.out.println("CP: " + urls[i].getFile());
    }         
  }
}
