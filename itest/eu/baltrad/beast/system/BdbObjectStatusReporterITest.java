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
import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;

import eu.baltrad.bdb.FileCatalog;
import eu.baltrad.beast.itest.BeastDBTestHelper;

/**
 * @author Anders Henja
 *
 */
public class BdbObjectStatusReporterITest {
  private BdbObjectStatusReporter classUnderTest = null;
  private AbstractApplicationContext context = null;
  private BeastDBTestHelper helper = null;
  private FileCatalog fc = null;
  
  private static String[] FIXTURES = new String[] {
    "fixtures/Z_SCAN_C_ESWI_20101016080000_seang_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_seang_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_searl_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_seang_000001.h5", // O1
    "fixtures/Z_SCAN_C_ESWI_20101016080500_sehud_000000.h5", // O2
    "fixtures/Z_SCAN_C_ESWI_20101016080500_selek_000000.h5", // O3  
    "fixtures/comp_20101016080000_swegmaps_2000.h5"
  };
  
  @Before
  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    fc = (FileCatalog)context.getBean("fc"); 
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.createBaltradDbPath();
    helper.purgeBaltradDB();

    classUnderTest = new BdbObjectStatusReporter();
    classUnderTest.setFileCatalog(fc);
    
    for (String s: FIXTURES) {
      fc.store(new FileInputStream(getFilePath(s)));
    }
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
    helper = null;
    context.close();
  }
  
  long getMinuteOffsetFromNow(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
    GregorianCalendar c = new GregorianCalendar();
    c.setTimeZone(TimeZone.getTimeZone("UTC"));
    c.set(Calendar.YEAR, year);
    c.set(Calendar.MONTH, month);
    c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    c.set(Calendar.HOUR_OF_DAY, hourOfDay);
    c.set(Calendar.MINUTE, minute);
    c.set(Calendar.SECOND, second);
    
    GregorianCalendar now = new GregorianCalendar();

    return ((now.getTimeInMillis() - c.getTimeInMillis()) / (60 * 1000));
  }
  
  @Test
  public void testGetStatus_searl() {
    GregorianCalendar c = new GregorianCalendar();
    c.setTimeZone(TimeZone.getTimeZone("UTC"));
    c.set(Calendar.YEAR, 2010);
    c.set(Calendar.MONTH, 9);
    c.set(Calendar.DAY_OF_MONTH, 16);
    c.set(Calendar.HOUR_OF_DAY, 8);
    c.set(Calendar.MINUTE, 5);
    c.set(Calendar.SECOND, 0);
    
    GregorianCalendar now = new GregorianCalendar();
    
    // This is a very long time back in time but it ought to show that it works properly by
    // first verifying that we get a hit and then (-4 minutes) that we don't
    long minute = ((now.getTimeInMillis() - c.getTimeInMillis()) / (60 * 1000)) + 2 ;
    
    Map<String,Object> values = new HashMap<String, Object>();
    values.put("objects", "SCAN");
    values.put("sources", "searl");
    values.put("minutes", minute);
    
    Set<SystemStatus> result = classUnderTest.getStatus(values);
    
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
    
    values.put("minutes", minute - 4);
    result = classUnderTest.getStatus(values);

    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.COMMUNICATION_PROBLEM));
  }

  @Test
  public void testGetStatus_OptionalAttribute_elangle_0_5() {
    long minute = getMinuteOffsetFromNow(2010,9,16,8,0,0) + 2;
    
    Map<String,Object> values = new HashMap<String, Object>();
    values.put("objects", "SCAN");
    values.put("sources", "seang");
    values.put("minutes", minute);
    values.put("where/elangle", "0.5");
    
    // Verify that we can find the specified elangle
    Set<SystemStatus> result = classUnderTest.getStatus(values);
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
  }

  @Test
  public void testGetStatus_OptionalAttribute_elangle_1_5() {
    long minute = getMinuteOffsetFromNow(2010,9,16,8,0,0) + 2;
    
    Map<String,Object> values = new HashMap<String, Object>();
    values.put("objects", "SCAN");
    values.put("sources", "seang");
    values.put("minutes", minute);
    values.put("where/elangle", "1.5");
    
    // Verify that we can find the specified elangle
    Set<SystemStatus> result = classUnderTest.getStatus(values);
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
  }

  @Test
  public void testGetStatus_OptionalAttribute_elangle_2_0_doesntexist() {
    long minute = getMinuteOffsetFromNow(2010,9,16,8,0,0) + 2;
    
    Map<String,Object> values = new HashMap<String, Object>();
    values.put("objects", "SCAN");
    values.put("sources", "seang");
    values.put("minutes", minute);
    values.put("where/elangle", "1.0");
    
    // Verify that we can find the specified elangle
    Set<SystemStatus> result = classUnderTest.getStatus(values);
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.COMMUNICATION_PROBLEM));
  }

  @Test
  public void testGetStatus_OptionalAttribute_malfunc_True() {
    long minute = getMinuteOffsetFromNow(2010,9,16,8,5,0) + 2;
    
    Map<String,Object> values = new HashMap<String, Object>();
    values.put("objects", "SCAN");
    values.put("sources", "sehud");
    values.put("minutes", minute);
    values.put("how/malfunc", "True");
    
    // Verify
    Set<SystemStatus> result = classUnderTest.getStatus(values);
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
    
    // And just ensure that we dont find anything with malfunc = False
    values.put("how/malfunc", "False");
    result = classUnderTest.getStatus(values);
    Assert.assertTrue(result.contains(SystemStatus.COMMUNICATION_PROBLEM));
  }

  @Test
  public void testGetStatus_OptionalAttribute_malfunc_False() {
    long minute = getMinuteOffsetFromNow(2010,9,16,8,5,0) + 2;
    
    Map<String,Object> values = new HashMap<String, Object>();
    values.put("objects", "SCAN");
    values.put("sources", "selek");
    values.put("minutes", minute);
    values.put("how/malfunc", "False");
    
    // Verify
    Set<SystemStatus> result = classUnderTest.getStatus(values);
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));

    // And just ensure that we dont find anything with malfunc = True
    values.put("how/malfunc", "True");
    result = classUnderTest.getStatus(values);
    Assert.assertTrue(result.contains(SystemStatus.COMMUNICATION_PROBLEM));
  }

  @Test
  public void testGetStatus_OptionalAttribute_mixed() {
    long minute = getMinuteOffsetFromNow(2010,9,16,8,5,0) + 2;
    
    Map<String,Object> values = new HashMap<String, Object>();
    values.put("objects", "SCAN");
    values.put("sources", "selek");
    values.put("minutes", minute);
    values.put("where/a1gate", "0");
    values.put("where/nrays", "420");
    values.put("where/rstart", "0");
    
    // Verify
    Set<SystemStatus> result = classUnderTest.getStatus(values);
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));

    // And just ensure that we dont find anything other values
    values.put("where/a1gate", "1");
    result = classUnderTest.getStatus(values);
    Assert.assertTrue(result.contains(SystemStatus.COMMUNICATION_PROBLEM));

    // And just ensure that we dont find anything other values
    values.put("where/a1gate", "0");
    values.put("where/nrays", "421");
    result = classUnderTest.getStatus(values);
    Assert.assertTrue(result.contains(SystemStatus.COMMUNICATION_PROBLEM));

    // And just ensure that we dont find anything other values
    values.put("where/nrays", "420");
    values.put("where/rstart", "10");
    result = classUnderTest.getStatus(values);
    Assert.assertTrue(result.contains(SystemStatus.COMMUNICATION_PROBLEM));

    // And now test that we can reset it back again to get a hit
    values.put("where/rstart", "0");
    result = classUnderTest.getStatus(values);
    Assert.assertTrue(result.contains(SystemStatus.OK));
  }

  @Test
  public void testGetStatus_OptionalAttribute_mixed_2() {
    long minute = getMinuteOffsetFromNow(2010,9,16,8,5,0) + 2;
    
    Map<String,Object> values = new HashMap<String, Object>();
    values.put("objects", "SCAN");
    values.put("sources", "selek,searl");
    values.put("minutes", minute);
    values.put("how/system", "ERIC");
    
    // Verify
    Set<SystemStatus> result = classUnderTest.getStatus(values);
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
  }
  
  @Test
  public void testGetStatus_pvol_searl() {
    GregorianCalendar c = new GregorianCalendar();

    c.set(Calendar.YEAR, 2010);
    c.set(Calendar.MONTH, 9);
    c.set(Calendar.DAY_OF_MONTH, 16);
    c.set(Calendar.HOUR_OF_DAY, 8);
    c.set(Calendar.MINUTE, 5);
    c.set(Calendar.SECOND, 0);
    
    GregorianCalendar now = new GregorianCalendar();
    
    // This is a very long time back in time but it ought to show that it works properly by
    // first verifying that we get a hit and then (-4 minutes) that we don't
    long minute = ((now.getTimeInMillis() - c.getTimeInMillis()) / (60 * 1000)) + 2 ;
    Map<String,Object> values = new HashMap<String, Object>();
    values.put("objects", "PVOL");
    values.put("sources", "searl");
    values.put("minutes", minute);

    Set<SystemStatus> result = classUnderTest.getStatus(values);
    
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.COMMUNICATION_PROBLEM));
  }
  
  @Test
  public void testGetStatus_comp_swegmaps_2000() {
    GregorianCalendar c = new GregorianCalendar();

    c.set(Calendar.YEAR, 2010);
    c.set(Calendar.MONTH, 9);
    c.set(Calendar.DAY_OF_MONTH, 16);
    c.set(Calendar.HOUR_OF_DAY, 8);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    
    GregorianCalendar now = new GregorianCalendar();
    
    // This is a very long time back in time but it ought to show that it works properly by
    // first verifying that we get a hit and then (-4 minutes) that we don't
    long minute = ((now.getTimeInMillis() - c.getTimeInMillis()) / (60 * 1000)) + 2 ;
    Map<String,Object> values = new HashMap<String, Object>();
    values.put("objects", "COMP");
    values.put("sources", "swegmaps_2000");
    values.put("minutes", minute);

    Set<SystemStatus> result = classUnderTest.getStatus(values);
    
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));

  }
  
  private String getFilePath(String resource) throws Exception {
    File f = new File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }
}
