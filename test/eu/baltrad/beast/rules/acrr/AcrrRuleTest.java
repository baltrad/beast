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

package eu.baltrad.beast.rules.acrr;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanInitializationException;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.bdb.util.Time;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.filters.TimeSelectionFilter;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class AcrrRuleTest extends EasyMockSupport {
  private Catalog catalog = null;
  private IRuleUtilities ruleUtil = null;
  private AcrrRule classUnderTest = null;

  interface Methods {
    boolean isLessOrEqual(DateTime d1, DateTime d2);
    TimeSelectionFilter createFilter(DateTime sdt, DateTime edt, int interval);
    List<CatalogEntry> filterEntries(List<CatalogEntry> entries);
    List<CatalogEntry> findFiles(DateTime now);
    int compareStoredDateTime(CatalogEntry e1, CatalogEntry e2);
  };
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    catalog = createMock(Catalog.class);
    ruleUtil = createMock(IRuleUtilities.class);
    classUnderTest = new AcrrRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtil);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    catalog = null;
    ruleUtil = null;
  }

  @Test
  public void testSetGetRuleId() {
    classUnderTest.setRuleId(10);
    assertEquals(10, classUnderTest.getRuleId());
  }

  @Test
  public void testAfterPropertiesSet() throws Exception {
    classUnderTest = new AcrrRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.afterPropertiesSet();
  }
  
  @Test
  public void testAfterPropertiesSet_noCatalog() throws Exception {
    classUnderTest = new AcrrRule();
    classUnderTest.setRuleUtilities(ruleUtil);
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected BeanInitializationException");
    } catch (BeanInitializationException bie) {
      // pass
    }
  }
  
  @Test
  public void testAfterPropertiesSet_nothingSet() throws Exception
  {
    classUnderTest = new AcrrRule();
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected BeanInitializationException");
    } catch (BeanInitializationException bie) {
      // pass
    }
  }
  
  @Test
  public void testGetType() {
    assertEquals("blt_acrr", classUnderTest.getType());
  }
  
  @Test
  public void testIsValid() {
    assertEquals(true, classUnderTest.isValid());
  }
  
  @Test
  public void testArea() {
    assertEquals(null, classUnderTest.getArea());
    classUnderTest.setArea("swegmaps_2000");
    assertEquals("swegmaps_2000", classUnderTest.getArea());
    classUnderTest.setArea(null);
    assertEquals(null, classUnderTest.getArea());
  }

  @Test
  public void testZrA() {
    assertEquals(200.0, classUnderTest.getZrA(), 4);
    classUnderTest.setZrA(202.3);
    assertEquals(202.3, classUnderTest.getZrA(), 4);
  }

  @Test
  public void testZrB() {
    assertEquals(1.6, classUnderTest.getZrB(), 4);
    classUnderTest.setZrA(2.3);
    assertEquals(2.3, classUnderTest.getZrB(), 4);
  }

  @Test
  public void testFilesPerHour() {
    assertEquals(4, classUnderTest.getFilesPerHour());
    int[] acceptableFilesPerHour = new int[]{1,2,3,4,6,12};
    for (int x : acceptableFilesPerHour) {
      classUnderTest.setFilesPerHour(x);
    }
    
    classUnderTest.setFilesPerHour(4);
    int[] notAcceptableFilesPerHour = new int[]{0,5,7,8,9,10,11};
    for (int x : notAcceptableFilesPerHour) {
      try {
        classUnderTest.setFilesPerHour(x);
        fail("Excepted IllegalArgumentException");
        assertEquals(4, classUnderTest.getFilesPerHour());
      } catch (IllegalArgumentException iae) {
        // pass
      }
    }
  }

  @Test
  public void testAcceptableLoss() {
    assertEquals(0, classUnderTest.getAcceptableLoss());
    for (int x = 0; x <= 100; x++) {
      classUnderTest.setAcceptableLoss(x);
      assertEquals(x, classUnderTest.getAcceptableLoss());
    }
    
    classUnderTest.setAcceptableLoss(5);
    try {
      classUnderTest.setAcceptableLoss(-1);
      fail("Expected IllegalArgumentException");
      assertEquals(5, classUnderTest.getAcceptableLoss());
    } catch (IllegalArgumentException iae) {
      //pass
    }

    try {
      classUnderTest.setAcceptableLoss(101);
      fail("Expected IllegalArgumentException");
      assertEquals(5, classUnderTest.getAcceptableLoss());
    } catch (IllegalArgumentException iae) {
      //pass
    }
  }
  
  @Test
  public void test_findFiles() {
    DateTime now = new DateTime();
    DateTime nominalDt = new DateTime();
    DateTime startDt = new DateTime();
    TimeSelectionFilter filter = new TimeSelectionFilter();
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    List<CatalogEntry> filteredEntries = new ArrayList<CatalogEntry>();
    
    Calendar nominalC = createMock(Calendar.class);
    final Methods methods = createMock(Methods.class);
    
    expect(ruleUtil.createNominalTime(now, 15)).andReturn(nominalDt);
    expect(ruleUtil.createCalendar(nominalDt)).andReturn(nominalC);
    nominalC.add(Calendar.HOUR, -3);
    expect(ruleUtil.createDateTime(nominalC)).andReturn(startDt);
    expect(methods.createFilter(startDt, nominalDt, 15)).andReturn(filter);
    expect(catalog.fetch(filter)).andReturn(entries);
    expect(methods.filterEntries(entries)).andReturn(filteredEntries);
    
    classUnderTest = new AcrrRule() {
      @Override
      protected TimeSelectionFilter createFilter(DateTime sdt, DateTime edt, int interval) {
        return methods.createFilter(sdt, edt, interval);
      }
      @Override
      protected List<CatalogEntry> filterEntries(List<CatalogEntry> entries) {
        return methods.filterEntries(entries);
      }
    };
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setCatalog(catalog);
    
    classUnderTest.setFilesPerHour(4);
    classUnderTest.setHours(3);

    replayAll();
    
    List<CatalogEntry> result = classUnderTest.findFiles(now);
    
    verifyAll();
    assertSame(filteredEntries, result);
  }
  
  @Test
  public void handle() {
    BltTriggerJobMessage message = new BltTriggerJobMessage();
    
    // Setup
    final Methods methods = createMock(Methods.class);

    DateTime dtNow = new DateTime();
    DateTime nominalTime = new DateTime();
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    List<String> uuids = new ArrayList<String>();
    uuids.add("A");
    uuids.add("B");
    
    expect(ruleUtil.nowDT()).andReturn(dtNow);
    expect(ruleUtil.createNominalTime(dtNow, 15)).andReturn(nominalTime);
    expect(methods.findFiles(dtNow)).andReturn(entries);
    expect(ruleUtil.getUuidStringsFromEntries(entries)).andReturn(uuids);
    
    classUnderTest = new AcrrRule() {
      @Override
      protected List<CatalogEntry> findFiles(DateTime now) {
        return methods.findFiles(now);
      }
    };
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setCatalog(catalog);
    classUnderTest.setArea("swegmaps");
    classUnderTest.setZrA(10.0);
    classUnderTest.setZrB(11.0);
    classUnderTest.setFilesPerHour(4);
    classUnderTest.setHours(6);
    classUnderTest.setAcceptableLoss(20);
    classUnderTest.setQuantity("DBZH");
    classUnderTest.setDistancefield("se.distancefield");
    classUnderTest.setApplyGRA(true);
    
    
    replayAll();
    
    // Test
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(message);
    
    // Verify
    verifyAll();
    
    String dateStr = new Formatter().format("%d%02d%02d",nominalTime.getDate().year(), nominalTime.getDate().month(), nominalTime.getDate().day()).toString();
    String timeStr = new Formatter().format("%02d%02d%02d",nominalTime.getTime().hour(), nominalTime.getTime().minute(), nominalTime.getTime().second()).toString();
    
    assertEquals("eu.baltrad.beast.GenerateAcrr", result.getAlgorithm());
    assertEquals(2, result.getFiles().length);
    assertEquals("A", result.getFiles()[0]);
    assertEquals("B", result.getFiles()[1]);
    assertEquals(11, result.getArguments().length);
    assertEquals("--area=swegmaps", result.getArguments()[0]);
    assertEquals("--date="+dateStr, result.getArguments()[1]);
    assertEquals("--time="+timeStr, result.getArguments()[2]);
    assertEquals("--zra=10.0", result.getArguments()[3]);
    assertEquals("--zrb=11.0", result.getArguments()[4]);
    assertEquals("--hours=6", result.getArguments()[5]);
    assertEquals("--N=25", result.getArguments()[6]);
    assertEquals("--accept=20", result.getArguments()[7]);
    assertEquals("--quantity=DBZH", result.getArguments()[8]);
    assertEquals("--distancefield=se.distancefield", result.getArguments()[9]);
    assertEquals("--applygra=true", result.getArguments()[10]);
  }
  
  @Test
  public void test_filterEntries() {
    final Methods methods = createMock(Methods.class);

    final List<CatalogEntry> filteredEntries = new ArrayList<CatalogEntry>();
    final Map<DateTime,CatalogEntry> filteredValidEntries = new HashMap<DateTime, CatalogEntry>();
    
    DateTime d1 = new DateTime(2013,1,1,10,0,0);
    DateTime d2 = new DateTime(2013,1,1,10,15,0);
    DateTime d3 = new DateTime(2013,1,1,10,0,0);
    
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    
    CatalogEntry e1 = createMock(CatalogEntry.class);
    CatalogEntry e2 = createMock(CatalogEntry.class);
    CatalogEntry e3 = createMock(CatalogEntry.class);
    expect(e1.getDateTime()).andReturn(d1);
    expect(e2.getDateTime()).andReturn(d2);
    expect(e3.getDateTime()).andReturn(d3);
    expect(methods.compareStoredDateTime(e3, e1)).andReturn(-1);
    
    entries.add(e1);
    entries.add(e2);
    entries.add(e3);

    classUnderTest = new AcrrRule() {
      @Override
      protected int compareStoredDateTime(CatalogEntry e1, CatalogEntry e2) {
        int result = methods.compareStoredDateTime(e1, e2);
        return result;
      }
      
      // We use this method to make sure that we get correct information and that it is called
      // instead of mocking the call and mocking the hashmap etc.
      @Override
      protected List<CatalogEntry> filterEntries(List<CatalogEntry> entries, Map<DateTime,CatalogEntry> validEntries) {
        for (CatalogEntry e : entries) {
          filteredEntries.add(e);
        }
        for (DateTime dt : validEntries.keySet()) {
          filteredValidEntries.put(dt, validEntries.get(dt));
        }
        return filteredEntries;
      }
    };
    
    replayAll();
    
    List<CatalogEntry> result = classUnderTest.filterEntries(entries);
    
    verifyAll();
    assertSame(filteredEntries, result); // filterEntries is called
    assertEquals(3, filteredEntries.size());
    assertSame(e1, filteredEntries.get(0));
    assertSame(e2, filteredEntries.get(1));
    assertSame(e3, filteredEntries.get(2));
    assertEquals(2, filteredValidEntries.size());
    assertSame(e2, filteredValidEntries.get(d2));
    assertSame(e3, filteredValidEntries.get(d3));
  }
  
  @Test
  public void test_filterEntries_withValidEntries() {
    CatalogEntry e1 = createMock(CatalogEntry.class);
    CatalogEntry e2 = createMock(CatalogEntry.class);
    CatalogEntry e3 = createMock(CatalogEntry.class);
    DateTime d1 = new DateTime(2013,1,1,10,0,0);
    DateTime d2 = new DateTime(2013,1,1,10,15,0);
    DateTime d3 = new DateTime(2013,1,1,10,0,0);
    
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    Map<DateTime,CatalogEntry> validEntries = new HashMap<DateTime, CatalogEntry>();
    entries.add(e1);
    entries.add(e2);
    entries.add(e3);
    
    validEntries.put(d2, e2);
    validEntries.put(d3, e3);
    
    expect(e1.getDateTime()).andReturn(d1);
    expect(e1.getUuid()).andReturn("00000-00001");
    expect(e3.getUuid()).andReturn("00000-00003");
    expect(e2.getDateTime()).andReturn(d2);
    expect(e2.getUuid()).andReturn("00000-00002");
    expect(e2.getUuid()).andReturn("00000-00002");
    expect(e3.getDateTime()).andReturn(d3);
    expect(e3.getUuid()).andReturn("00000-00003");
    expect(e3.getUuid()).andReturn("00000-00003");
    
    replayAll();
    
    List<CatalogEntry> result = classUnderTest.filterEntries(entries, validEntries);
    
    verifyAll();
    assertEquals(2, result.size());
    assertSame(e2,result.get(0));
    assertSame(e3,result.get(1));
  }
  
  @Test
  public void test_compareStoredDateTime() {
    Date sd1 = new Date(2013,1,1);
    Time st1 = new Time(10,0,0);
    Date sd2 = new Date(2013,1,1);
    Time st2 = new Time(10,0,1);
    CatalogEntry e1 = createMock(CatalogEntry.class);
    CatalogEntry e2 = createMock(CatalogEntry.class);
    FileEntry f1 = createMock(FileEntry.class);
    FileEntry f2 = createMock(FileEntry.class);
    Calendar c1 = createMock(Calendar.class);
    Calendar c2 = createMock(Calendar.class);
    
    expect(e1.getFileEntry()).andReturn(f1);
    expect(e2.getFileEntry()).andReturn(f2);
    expect(f1.getStoredDate()).andReturn(sd1);
    expect(f1.getStoredTime()).andReturn(st1);
    expect(f2.getStoredDate()).andReturn(sd2);
    expect(f2.getStoredTime()).andReturn(st2);
    expect(ruleUtil.createCalendar(new DateTime(2013,1,1,10,0,0))).andReturn(c1);
    expect(ruleUtil.createCalendar(new DateTime(2013,1,1,10,0,1))).andReturn(c2);
    expect(c1.compareTo(c2)).andReturn(-1);

    replayAll();
    
    int result = classUnderTest.compareStoredDateTime(e1, e2);
    
    verifyAll();
    assertEquals(-1, result);
  }
  
  //@Test
  public void testGet() {
    classUnderTest.setArea("swegmaps_2000");
    classUnderTest.setHours(2);
    classUnderTest.setZrA(200.0);
    classUnderTest.setZrB(1.6);
    classUnderTest.setFilesPerHour(4); // 1,2,3,4,6,12  (=> 2,3,4,7,13 files)
                                       // 1 => 00, 00
                                       // 2 => 00, 30, 00
                                       // 3 => 00, 20, 40, 00
                                       // 4 => 00, 15, 30, 45, 00
                                       // 6 => 00, 10, 20, 30, 40, 50, 00
                                       //12 => 00, 05, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 00
  classUnderTest.setAcceptableLoss(5); // In percent
  }
  
  @Test
  public void testIsLessOrEqual() {
    DateTime dt1 = new DateTime();
    DateTime dt2 = new DateTime();
    Calendar c1 = createMock(Calendar.class);
    Calendar c2 = createMock(Calendar.class);
    expect(ruleUtil.createCalendar(dt1)).andReturn(c1);
    expect(ruleUtil.createCalendar(dt2)).andReturn(c2);
    expect(c1.compareTo(c2)).andReturn(-1);
    
    replayAll();
    
    assertEquals(true, classUnderTest.isLessOrEqual(dt1, dt2));
    
    verifyAll();
  }

  @Test
  public void testIsLessOrEqual_1() {
    DateTime dt1 = new DateTime();
    DateTime dt2 = new DateTime();
    Calendar c1 = createMock(Calendar.class);
    Calendar c2 = createMock(Calendar.class);
    expect(ruleUtil.createCalendar(dt1)).andReturn(c1);
    expect(ruleUtil.createCalendar(dt2)).andReturn(c2);
    expect(c1.compareTo(c2)).andReturn(1);
    
    replayAll();
    
    assertEquals(false, classUnderTest.isLessOrEqual(dt1, dt2));
    
    verifyAll();
  }

  @Test
  public void testIsLessOrEqual_2() {
    DateTime dt1 = new DateTime();
    DateTime dt2 = new DateTime();
    Calendar c1 = createMock(Calendar.class);
    Calendar c2 = createMock(Calendar.class);
    expect(ruleUtil.createCalendar(dt1)).andReturn(c1);
    expect(ruleUtil.createCalendar(dt2)).andReturn(c2);
    expect(c1.compareTo(c2)).andReturn(0);
    
    replayAll();
    
    assertEquals(true, classUnderTest.isLessOrEqual(dt1, dt2));
    
    verifyAll();
  }

  @Test
  public void test_createFilter() {
    final Methods mmock = createMock(Methods.class); 

    classUnderTest = new AcrrRule() {
      protected boolean isLessOrEqual(DateTime d1, DateTime d2) {
        return mmock.isLessOrEqual(d1, d2);
      }
    };
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setArea("nrdswe");
    classUnderTest.setObjectType("IMAGE");
    
    DateTime sdt = new DateTime(2013,1,1,10,0,0);
    DateTime edt = new DateTime(2013,1,1,11,0,0);

    expect(mmock.isLessOrEqual(new DateTime(2013,1,1,10,0,0), edt)).andReturn(true);
    expect(mmock.isLessOrEqual(new DateTime(2013,1,1,10,15,0), edt)).andReturn(true);
    expect(mmock.isLessOrEqual(new DateTime(2013,1,1,10,30,0), edt)).andReturn(false);
    
    replayAll();
    
    TimeSelectionFilter result = classUnderTest.createFilter(sdt, edt, 15);
    
    verifyAll();
    
    assertEquals("nrdswe", result.getSource());
    assertEquals("IMAGE", result.getObjectType());
    List<DateTime> dateTimes = result.getDateTimes();
    assertEquals(2,dateTimes.size());
    assertEquals(new DateTime(2013,1,1,10,0,0), dateTimes.get(0));
    assertEquals(new DateTime(2013,1,1,10,15,0), dateTimes.get(1));
  }
  
  @Test
  public void test_getInterval() {
    int[][] filesPerHour = new int[][]{
        {1,60},
        {2,30},
        {3,20},
        {4,15},
        {6,10},
        {12,5}};
    for (int[] h : filesPerHour) {
      classUnderTest.setFilesPerHour(h[0]);
      assertEquals(h[1], classUnderTest.getFilesPerHourInterval());
    }
  }
}
