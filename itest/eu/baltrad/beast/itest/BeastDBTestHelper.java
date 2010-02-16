package eu.baltrad.beast.itest;
import java.io.File;
import java.sql.Connection;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DataSourceUtils;

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

/**
 * @author Anders Henja
 *
 */
public class BeastDBTestHelper {
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
  public static void cleanInsert(DataSource source, TestCase tc) throws Exception {
    Connection conn = source.getConnection();
    try {
      IDatabaseConnection connection = new DatabaseConnection(conn);
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
  public static XlsDataSet getXlsDataset(TestCase tc, String extras) throws Exception {
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
  public static ITable getDatabaseTable(DataSource source, String name) throws Exception {
    Connection conn = source.getConnection();
    try {
      IDatabaseConnection connection = new DatabaseConnection(conn);
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
  public static ITable getXlsTable(TestCase tc, String extras, String name) throws Exception {
    IDataSet dataset = getXlsDataset(tc, extras);
    return dataset.getTable(name);
  }
}
