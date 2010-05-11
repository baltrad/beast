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

import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.db.filters.TimeIntervalFilter;
import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.fc.Date;
import eu.baltrad.fc.FileCatalog;
import eu.baltrad.fc.Time;
import eu.baltrad.fc.oh5.File;
import junit.framework.TestCase;

/**
 * @author Anders Henja
 *
 */
public class CatalogITest extends TestCase {
  private ApplicationContext context = null;
  private BeastDBTestHelper helper = null;
  private FileCatalog catalog = null;
  private Catalog classUnderTest = null;
  
  private static String[] FIXTURES = {
    "fixtures/pvol_seang_20090501T120000Z.h5",
    "fixtures/pvol_searl_20090501T120000Z.h5",
    "fixtures/pvol_sease_20090501T120000Z.h5",
    "fixtures/pvol_sehud_20090501T120000Z.h5"};
  
  public CatalogITest(String name) {
    super(name);

    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.createBaltradDbPath();
    catalog = new FileCatalog(helper.getBaltradDbUri(), helper.getBaltradDbPth());
  }

  private String getFilePath(String resource) throws Exception {
    java.io.File f = new java.io.File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }
  
  public void setUp() throws Exception {
    classUnderTest = new Catalog();
    classUnderTest.setCatalog(catalog);
    
    helper.purgeBaltradDB();
    long startTime = System.currentTimeMillis();
    for (String n : FIXTURES) {
      File result = catalog.catalog(getFilePath(n));
      assertNotNull(result);
    }
    System.out.println("Catalogued " + FIXTURES.length + " files in " + (System.currentTimeMillis() - startTime) + "ms");
  }
  
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  public void testFetch_TimeIntervalFilter_PVOL() throws Exception {
    TimeIntervalFilter filter = new TimeIntervalFilter();
    
    filter.setObject("PVOL");
    filter.setStartDateTime(new Date(2009,5,1), new Time(12, 0, 0));
    filter.setStopDateTime(new Date(2009,5,1), new Time(12, 15, 0));
    
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
    filter.setStartDateTime(new Date(2009,5,1), new Time(11, 0, 0));
    filter.setStopDateTime(new Date(2009,5,1), new Time(11, 15, 0));
    
    List<CatalogEntry> result = classUnderTest.fetch(filter);
    
    assertEquals(0, result.size());
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
