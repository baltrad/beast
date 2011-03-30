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
package eu.baltrad.beast.db;

import java.util.List;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.db.filters.PolarScanAngleFilter;
import eu.baltrad.beast.db.filters.TimeIntervalFilter;
import eu.baltrad.beast.itest.BeastDBTestHelper;

import eu.baltrad.fc.DateTime;
import eu.baltrad.fc.FileCatalog;
import eu.baltrad.fc.LocalStorage;
import eu.baltrad.fc.NullStorage;
import eu.baltrad.fc.db.Database;
import eu.baltrad.fc.oh5.File;

/**
 * @author Anders Henja
 *
 */
public class CatalogITest extends TestCase {
  private ApplicationContext context = null;
  private BeastDBTestHelper helper = null;
  private Database db = null;
  private LocalStorage storage = null;
  private FileCatalog catalog = null;
  private Catalog classUnderTest = null;
  
  private static String[] FIXTURES = {
    "fixtures/Z_PVOL_C_ESWI_20101023180100_seang.h5",
    "fixtures/Z_PVOL_C_ESWI_20101023180100_searl.h5",
    "fixtures/Z_PVOL_C_ESWI_20101023180100_sease.h5",
    "fixtures/Z_PVOL_C_ESWI_20101023180100_sehud.h5"};

  private static String[] SCAN_FIXTURES = {
    "fixtures/scan_sevil_20090501123100Z_1.0.h5",
    "fixtures/scan_sevil_20090501123100Z_1.5.h5",
    "fixtures/scan_sevil_20090501123100Z_2.0.h5"
  };
  
  public CatalogITest(String name) {
    super(name);
  }

  private String getFilePath(String resource) throws Exception {
    java.io.File f = new java.io.File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }
  
  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.createBaltradDbPath();
    db = Database.create(helper.getBaltradDbUri());
    storage = new NullStorage();
    catalog = new FileCatalog(db, storage);
    
    classUnderTest = new Catalog();
    classUnderTest.setCatalog(catalog);
    
    helper.purgeBaltradDB();
    long startTime = System.currentTimeMillis();
    for (String n : FIXTURES) {
      File result = catalog.store(getFilePath(n));
      assertNotNull(result);
    }
    for (String n : SCAN_FIXTURES) {
      File result = catalog.store(getFilePath(n));
      assertNotNull(result);
    }
    System.out.println("Catalogued " + (FIXTURES.length+SCAN_FIXTURES.length) + " files in " + (System.currentTimeMillis() - startTime) + "ms");
  }
  
  public void tearDown() throws Exception {
    classUnderTest = null;
    context = null;
    helper = null;
    catalog.delete();
    storage.delete();
    db.delete();
  }
  
  public void testFetch_TimeIntervalFilter_PVOL() throws Exception {
    TimeIntervalFilter filter = new TimeIntervalFilter();
    
    filter.setObject("PVOL");
    filter.setStartDateTime(new DateTime(2010,10,23, 18, 0, 0));
    filter.setStopDateTime(new DateTime(2010,10,23, 18, 2, 0));
    
    List<CatalogEntry> result = classUnderTest.fetch(filter);
    
    assertEquals(4, result.size());
    assertTrue(getEntryBySource(result, "seang") != null);
    assertTrue(getEntryBySource(result, "searl") != null);
    assertTrue(getEntryBySource(result, "sease") != null);
    assertTrue(getEntryBySource(result, "sehud") != null);
  }

  public void testFetch_TimeIntervalFilter_PVOL_nothingFound() throws Exception {
    TimeIntervalFilter filter = new TimeIntervalFilter();
    
    filter.setObject("PVOL");
    filter.setStartDateTime(new DateTime(2009,5,1,11,0,0));
    filter.setStopDateTime(new DateTime(2009,5,1,11, 15, 0));
    
    List<CatalogEntry> result = classUnderTest.fetch(filter);
    
    assertEquals(0, result.size());
  }
  
  public void testFetch_TimeIntervalFilter_previous() throws Exception {
    // First we need to add one entry from the current time period
    catalog.store(getFilePath("fixtures/scan_sevil_20090501124600Z_1.0.h5"));
    
    // and now execute test, we shouldn't get the scan from specified time
    TimeIntervalFilter filter = new TimeIntervalFilter();
    filter.setStopDateTime(new DateTime(2009,5,1,12,46,0));
    filter.setSource("sevil");
    filter.setLimit(1);
    filter.setObject("SCAN");
    List<CatalogEntry> result = classUnderTest.fetch(filter);
    assertEquals(1, result.size());
    DateTime dt = result.get(0).getDateTime();
    assertEquals(2009, dt.date().year());
    assertEquals(5, dt.date().month());
    assertEquals(1, dt.date().day());
    assertEquals(12, dt.time().hour());
    assertEquals(31, dt.time().minute());
    assertEquals(0, dt.time().second());
  }
  
  public void testFetch_PolarScanAngleFilter_ascending() throws Exception {
    catalog.store(getFilePath("fixtures/scan_sevil_20090501124600Z_1.0.h5"));

    PolarScanAngleFilter filter = new PolarScanAngleFilter();
    filter.setSource("sevil");
    filter.setDateTime(new DateTime(2009,5,1,12,31,0));
    filter.setSortOrder(PolarScanAngleFilter.ASCENDING);
    List<CatalogEntry> result = classUnderTest.fetch(filter);
    assertEquals(3, result.size());
    assertEquals(1.0, result.get(0).getAttribute("/dataset1/where/elangle"));
    assertEquals(1.5, result.get(1).getAttribute("/dataset1/where/elangle"));
    assertEquals(2.0, result.get(2).getAttribute("/dataset1/where/elangle"));
  }

  public void testFetch_PolarScanAngleFilter_descending() throws Exception {
    catalog.store(getFilePath("fixtures/scan_sevil_20090501124600Z_1.0.h5"));
    
    PolarScanAngleFilter filter = new PolarScanAngleFilter();
    filter.setSource("sevil");
    filter.setDateTime(new DateTime(2009,5,1,12,31,0));
    filter.setSortOrder(PolarScanAngleFilter.DESCENDING);
    List<CatalogEntry> result = classUnderTest.fetch(filter);
    assertEquals(3, result.size());
    assertEquals(2.0, result.get(0).getAttribute("/dataset1/where/elangle"));
    assertEquals(1.5, result.get(1).getAttribute("/dataset1/where/elangle"));
    assertEquals(1.0, result.get(2).getAttribute("/dataset1/where/elangle"));
  }

  public void testFetch_PolarScanAngleFilter_minMaxElevation() throws Exception {
    catalog.store(getFilePath("fixtures/scan_sevil_20090501124600Z_1.0.h5"));
    
    PolarScanAngleFilter filter = new PolarScanAngleFilter();
    filter.setSource("sevil");
    filter.setDateTime(new DateTime(2009,5,1,12,31,0));
    filter.setSortOrder(PolarScanAngleFilter.ASCENDING);
    filter.setMinElevation(0.0);
    filter.setMaxElevation(1.5);
    
    List<CatalogEntry> result = classUnderTest.fetch(filter);
    assertEquals(2, result.size());
    assertEquals(1.0, result.get(0).getAttribute("/dataset1/where/elangle"));
    assertEquals(1.5, result.get(1).getAttribute("/dataset1/where/elangle"));
  }  
  
  protected CatalogEntry getEntryBySource(List<CatalogEntry> entries, String source) {
    for (CatalogEntry entry: entries) {
      if (entry.getSource().equals(source)) {
        return entry;
      }
    }
    return null;
  }
}
