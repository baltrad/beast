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
import java.util.Set;

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
public class BdbProductStatusReporterITest {
  private BdbProductStatusReporter classUnderTest = null;
  private AbstractApplicationContext context = null;
  private BeastDBTestHelper helper = null;
  private FileCatalog fc = null;
  
  private static String[] FIXTURES = new String[] {
    "fixtures/Z_SCAN_C_ESWI_20101016080000_seang_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_seang_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_searl_000000.h5",
    "fixtures/comp_20101016080000_swegmaps_2000.h5"
  };
  
  @Before
  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    fc = (FileCatalog)context.getBean("fc"); 
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.createBaltradDbPath();
    helper.purgeBaltradDB();

    classUnderTest = new BdbProductStatusReporter();
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
  
  @Test
  public void testGetStatus_searl() {
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
    
    Set<SystemStatus> result = classUnderTest.getStatus("products=SCAN", "sources=searl", "minutes="+minute);
    
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));
    
    result = classUnderTest.getStatus("products=SCAN","sources=searl", "minutes="+(minute-4));

    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.COMMUNICATION_PROBLEM));
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
    
    Set<SystemStatus> result = classUnderTest.getStatus("products=PVOL", "sources=searl", "minutes="+minute);
    
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
    
    Set<SystemStatus> result = classUnderTest.getStatus("products=COMP", "sources=swegmaps_2000", "minutes="+minute);
    
    Assert.assertEquals(1, result.size());
    Assert.assertTrue(result.contains(SystemStatus.OK));

  }
  
  private String getFilePath(String resource) throws Exception {
    File f = new File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }
}
