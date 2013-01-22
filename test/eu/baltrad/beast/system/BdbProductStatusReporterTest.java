/* --------------------------------------------------------------------
Copyright (C) 2009-2013 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.system;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.bdb.FileCatalog;
import eu.baltrad.bdb.db.Database;
import eu.baltrad.bdb.db.FileQuery;
import eu.baltrad.bdb.db.FileResult;
import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.expr.ExpressionFactory;
import eu.baltrad.bdb.util.DateTime;

/**
 * @author Anders Henja
 */
public class BdbProductStatusReporterTest extends EasyMockSupport {
  static interface MethodMock {
    public FileQuery createFileQuery();
    public DateTime createFromDateTime(int minutes);
    public List<Expression> createSourceList(String src);
    public List<Expression> createProductsList(String src);
    public Expression createSearchFilter(String... args);
  };
  
  private BdbProductStatusReporter classUnderTest = null;

  private FileCatalog fileCatalog = null;
  
  @Before
  public void setUp() throws Exception {
    fileCatalog = createMock(FileCatalog.class);
    classUnderTest = new BdbProductStatusReporter();
    classUnderTest.setFileCatalog(fileCatalog);
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
    fileCatalog = null;
  }
  
  @Test
  public void testGetName() {
    String result = classUnderTest.getName();
    Assert.assertEquals("bdb_product_status", result);
  }
  
  @Test
  public void testGetStatus() {
    FileQuery fq = createMock(FileQuery.class);
    Database db = createMock(Database.class);
    final MethodMock methods = createMock(MethodMock.class);
    Expression filter = createMock(Expression.class);
    FileResult fr = createMock(FileResult.class);
    
    classUnderTest = new BdbProductStatusReporter() {
      @Override
      protected FileQuery createFileQuery() {
        return methods.createFileQuery();
      }
      @Override
      protected Expression createSearchFilter(String... args) {
        return methods.createSearchFilter(args);
      }
    };
    classUnderTest.setFileCatalog(fileCatalog);
    
    expect(methods.createFileQuery()).andReturn(fq);
    expect(methods.createSearchFilter("products=SCAN,PVOL","sources=searl","minute=5")).andReturn(filter);
    fq.setLimit(1);
    fq.setFilter(filter);
    expect(fileCatalog.getDatabase()).andReturn(db);
    expect(db.execute(fq)).andReturn(fr);
    expect(fr.next()).andReturn(true);
    
    replayAll();
    
    Set<SystemStatus> result = classUnderTest.getStatus("products=SCAN,PVOL", "sources=searl", "minute=5");
    
    verifyAll();
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
  }
  
  @Test
  public void testGetStatus_noMatch() {
    FileQuery fq = createMock(FileQuery.class);
    Database db = createMock(Database.class);
    final MethodMock methods = createMock(MethodMock.class);
    Expression filter = createMock(Expression.class);
    FileResult fr = createMock(FileResult.class);
    
    classUnderTest = new BdbProductStatusReporter() {
      @Override
      protected FileQuery createFileQuery() {
        return methods.createFileQuery();
      }
      @Override
      protected Expression createSearchFilter(String... args) {
        return methods.createSearchFilter(args);
      }
    };
    classUnderTest.setFileCatalog(fileCatalog);
    
    expect(methods.createFileQuery()).andReturn(fq);
    expect(methods.createSearchFilter("products=SCAN,PVOL","sources=searl","minute=5")).andReturn(filter);
    fq.setLimit(1);
    fq.setFilter(filter);
    expect(fileCatalog.getDatabase()).andReturn(db);
    expect(db.execute(fq)).andReturn(fr);
    expect(fr.next()).andReturn(false); // Only difference
    
    replayAll();
    
    Set<SystemStatus> result = classUnderTest.getStatus("products=SCAN,PVOL", "sources=searl", "minute=5");
    
    verifyAll();
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.COMMUNICATION_PROBLEM));
  }
  
  @Test
  public void testCreateSearchFilter() {
    final MethodMock mock = createMock(MethodMock.class);

    ExpressionFactory xpr = new ExpressionFactory();
    DateTime dt = new DateTime(2013,1,11,12,30,0);
    List<Expression> slist = new ArrayList<Expression>();
    List<Expression> plist = new ArrayList<Expression>();
    
    Expression dtfilter = xpr.ge(xpr.combinedDateTime("what/date", "what/time"), xpr.literal(dt));
    slist.add(xpr.or(xpr.eq(xpr.attribute("_bdb/source_name"), xpr.literal("searl")),
                     xpr.eq(xpr.attribute("what/source:CMT"), xpr.literal("searl"))));
    plist.add(xpr.eq(xpr.attribute("what/object"), xpr.literal("SCAN")));
    plist.add(xpr.eq(xpr.attribute("what/object"), xpr.literal("PVOL")));
    
    List<Expression> qfilter = new ArrayList<Expression>();
    qfilter.add(dtfilter);
    qfilter.add(xpr.or(slist));
    qfilter.add(xpr.or(plist));
    Expression expected = xpr.and(qfilter);
    
    // We mock the time so that we get a known time, rest can be tested as is.
    classUnderTest = new BdbProductStatusReporter() {
      @Override
      protected DateTime createFromDateTime(int minutes) {
        return mock.createFromDateTime(minutes);
      }
    };
    
    expect(mock.createFromDateTime(10)).andReturn(dt);

    replayAll();
    
    Expression result = classUnderTest.createSearchFilter("products=SCAN,PVOL","sources=searl","minutes=10");
    
    verifyAll();
    Assert.assertEquals(expected, result);
  }
  
  
  @Test
  public void testCreateFromDateTime() {
    final GregorianCalendar mycalendar = new GregorianCalendar();
    mycalendar.set(Calendar.YEAR, 2013);
    mycalendar.set(Calendar.MONTH, 0);
    mycalendar.set(Calendar.DAY_OF_MONTH, 5);
    mycalendar.set(Calendar.HOUR_OF_DAY, 12);
    mycalendar.set(Calendar.MINUTE, 35);
    mycalendar.set(Calendar.SECOND, 10);
    
    classUnderTest = new BdbProductStatusReporter() {
      @Override
      protected GregorianCalendar createCalendar() {
        return mycalendar;
      }
    };
    
    DateTime result = classUnderTest.createFromDateTime(5);
    
    Assert.assertEquals(2013, result.getDate().year());
    Assert.assertEquals(1, result.getDate().month());
    Assert.assertEquals(5, result.getDate().day());
    Assert.assertEquals(12, result.getTime().hour());
    Assert.assertEquals(30, result.getTime().minute());
    Assert.assertEquals(0, result.getTime().second());
  }
  
  @Test
  public void testCreateSourceList() {
    ExpressionFactory xpr = new ExpressionFactory();
    Expression e1 = xpr.or(xpr.eq(xpr.attribute("_bdb/source_name"), xpr.literal("abc")),
                           xpr.eq(xpr.attribute("what/source:CMT"), xpr.literal("abc")));
    Expression e2 = xpr.or(xpr.eq(xpr.attribute("_bdb/source_name"), xpr.literal("def")),
                           xpr.eq(xpr.attribute("what/source:CMT"), xpr.literal("def")));
    Expression e3 = xpr.or(xpr.eq(xpr.attribute("_bdb/source_name"), xpr.literal("ghi")),
                           xpr.eq(xpr.attribute("what/source:CMT"), xpr.literal("ghi")));
    
    List<Expression> result = classUnderTest.createSourceList("abc,def,ghi");

    Assert.assertEquals(3, result.size());
    Assert.assertEquals(e1, result.get(0));
    Assert.assertEquals(e2, result.get(1));
    Assert.assertEquals(e3, result.get(2));
  }
  
  @Test
  public void testCreateProductsList() {
    ExpressionFactory xpr = new ExpressionFactory();
    Expression e1 = xpr.eq(xpr.attribute("what/object"), xpr.literal("SCAN"));
    Expression e2 = xpr.eq(xpr.attribute("what/object"), xpr.literal("PVOL"));
    Expression e3 = xpr.eq(xpr.attribute("what/object"), xpr.literal("COMP"));
    
    List<Expression> result = classUnderTest.createProductsList("SCAN,PVOL,COMP");

    Assert.assertEquals(3, result.size());
    Assert.assertEquals(e1, result.get(0));
    Assert.assertEquals(e2, result.get(1));
    Assert.assertEquals(e3, result.get(2));
    
  }
}
