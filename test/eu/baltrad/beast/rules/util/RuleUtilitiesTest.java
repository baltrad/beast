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
package eu.baltrad.beast.rules.util;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.bdb.FileCatalog;
import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.storage.LocalStorage;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;

/**
 * @author Anders Henja
 */
public class RuleUtilitiesTest extends EasyMockSupport {
  private RuleUtilities classUnderTest = null;
  private Catalog catalog = null;
  private FileCatalog fileCatalog = null;
  private LocalStorage storage = null;

  @Before
  public void setUp() throws Exception {
    catalog = createMock(Catalog.class);
    fileCatalog = createMock(FileCatalog.class);
    storage = createMock(LocalStorage.class);
    classUnderTest = new RuleUtilities();
    classUnderTest.setCatalog(catalog);
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
    storage = null;
    fileCatalog = null;
    catalog = null;
  }

  @Test
  public void testGetEntryBySource() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("seang", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seang", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sehud", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seosu", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sevan", new DateTime(2010,1,1,10,1,2)));
    
    replayAll();
    
    CatalogEntry result = classUnderTest.getEntryBySource("seang", entries);
    
    assertEquals(entries.get(0), result);
    result = classUnderTest.getEntryBySource("sehud", entries);
    assertEquals(entries.get(2), result);
    result = classUnderTest.getEntryBySource("sevan", entries);
    assertEquals(entries.get(4), result);
    result = classUnderTest.getEntryBySource("senone", entries);
    assertNull(result);
    verifyAll();
  }

  @Test
  public void testGetEntriesByClosestTime() {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("seang", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seang", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sehud", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seosu", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("seosu", new DateTime(2010,1,1,10,1,2)));
    
    replayAll();
    
    List<CatalogEntry> result = classUnderTest.getEntriesByClosestTime(new DateTime(2010,1,1,10,1,1), entries);
    
    verifyAll();
    assertEquals(3, result.size());
    assertTrue(result.contains(entries.get(0)));
    assertTrue(result.contains(entries.get(2)));
    assertTrue(result.contains(entries.get(3)));
  }

  @Test
  public void testGetEntriesBySources() {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("seang", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seang", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sehud", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seosu", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sevan", new DateTime(2010,1,1,10,1,2)));
    
    List<String> sources = new ArrayList<String>();
    sources.add("seang");
    sources.add("sehud");
    sources.add("seosu");

    replayAll();
    
    List<CatalogEntry> result = classUnderTest.getEntriesBySources(sources, entries);

    verifyAll();
    assertEquals(4, result.size());
    assertTrue(result.contains(entries.get(0)));
    assertTrue(result.contains(entries.get(1)));
    assertTrue(result.contains(entries.get(2)));
    assertTrue(result.contains(entries.get(3)));
  }

  @Test
  public void testGetFilesFromEntries() {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    CatalogEntry ce1 = createMock(CatalogEntry.class);
    CatalogEntry ce2 = createMock(CatalogEntry.class);
    CatalogEntry ce3 = createMock(CatalogEntry.class);
    FileEntry fe1 = createMock(FileEntry.class);
    FileEntry fe2 = createMock(FileEntry.class);
    FileEntry fe3 = createMock(FileEntry.class);
    
    entries.add(ce1);
    entries.add(ce2);
    entries.add(ce3);

    expect(catalog.getCatalog()).andReturn(fileCatalog);
    expect(fileCatalog.getLocalStorage()).andReturn(storage);
    expect(ce1.getFileEntry()).andReturn(fe1);
    expect(storage.store(fe1)).andReturn(new File("/tmp/1.h5"));
    expect(ce2.getFileEntry()).andReturn(fe2);
    expect(storage.store(fe2)).andReturn(new File("/tmp/2.h5"));
    expect(ce3.getFileEntry()).andReturn(fe3);
    expect(storage.store(fe3)).andReturn(new File("/tmp/3.h5"));

    replayAll();

    List<String> result = classUnderTest.getFilesFromEntries(entries);

    verifyAll();
    assertEquals(3, result.size());
    assertTrue(result.contains("/tmp/1.h5"));
    assertTrue(result.contains("/tmp/2.h5"));
    assertTrue(result.contains("/tmp/3.h5"));
  }

  @Test
  public void testGetUuidStringsFromEntries() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    CatalogEntry ce1 = createMock(CatalogEntry.class);
    CatalogEntry ce2 = createMock(CatalogEntry.class);
    CatalogEntry ce3 = createMock(CatalogEntry.class);

    entries.add(ce1);
    entries.add(ce2);
    entries.add(ce3);
    
    expect(ce1.getUuid()).andReturn("uuid-1");
    expect(ce2.getUuid()).andReturn("uuid-2");
    expect(ce3.getUuid()).andReturn("uuid-3");
    
    replayAll();
    
    List<String> result = classUnderTest.getUuidStringsFromEntries(entries);
    
    verifyAll();
    assertEquals(3, result.size());
    assertEquals("uuid-1", result.get(0));
    assertEquals("uuid-2", result.get(1));
    assertEquals("uuid-3", result.get(2));
  }
  
  @Test
  public void testGetSourcesFromEntries() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("seang", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seang", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sehud", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seosu", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sevan", new DateTime(2010,1,1,10,1,2)));
    
    replayAll();
    
    List<String> result = classUnderTest.getSourcesFromEntries(entries);

    verifyAll();
    
    assertEquals(4, result.size());
    assertTrue(result.contains("seang"));
    assertTrue(result.contains("sehud"));
    assertTrue(result.contains("seosu"));
    assertTrue(result.contains("sevan"));
  }

  @Test
  public void testGetSourcesFromEntries_noEntries() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    
    List<String> result = classUnderTest.getSourcesFromEntries(entries);
    
    assertEquals(0, result.size());
  }
  
  @Test
  public void testCreateNominalTime() throws Exception {
    DateTime[][] TIME_TABLE=new DateTime[][] {
        {new DateTime(2010,1,1,1,9,0), new DateTime(2010,1,1,1,0,0)},
        {new DateTime(2010,1,1,1,11,0), new DateTime(2010,1,1,1,10,0)},
        {new DateTime(2010,1,1,1,20,0), new DateTime(2010,1,1,1,20,0)},
        {new DateTime(2010,1,1,1,39,0), new DateTime(2010,1,1,1,30,0)},
        {new DateTime(2010,1,1,1,59,0), new DateTime(2010,1,1,1,50,0)},
    };
    
    for (int i = 0; i < TIME_TABLE.length; i++) {
      DateTime dtResult = classUnderTest.createNominalTime(TIME_TABLE[i][0], 10);
      assertTrue("TT["+i+"] not as expected", dtResult.equals(TIME_TABLE[i][1]));
    }
  }

  @Test
  public void testCreateNominalTime_dateAndTime() throws Exception {
    DateTime[][] TIME_TABLE=new DateTime[][] {
        {new DateTime(2010,1,1,1,9,0), new DateTime(2010,1,1,1,0,0)},
        {new DateTime(2010,1,1,1,11,0), new DateTime(2010,1,1,1,10,0)},
        {new DateTime(2010,1,1,1,20,0), new DateTime(2010,1,1,1,20,0)},
        {new DateTime(2010,1,1,1,39,0), new DateTime(2010,1,1,1,30,0)},
        {new DateTime(2010,1,1,1,59,0), new DateTime(2010,1,1,1,50,0)},
    };
    
    for (int i = 0; i < TIME_TABLE.length; i++) {
      DateTime dtResult = classUnderTest.createNominalTime(TIME_TABLE[i][0].getDate(), TIME_TABLE[i][0].getTime(), 10);
      assertTrue("TT["+i+"] not as expected", dtResult.equals(TIME_TABLE[i][1]));
    }
  }

  @Test
  public void testCreateNextNominalTime() throws Exception {
    DateTime[][] TIME_TABLE=new DateTime[][] {
        {new DateTime(2010,1,1,1,9,0), new DateTime(2010,1,1,1,10,0)},
        {new DateTime(2010,1,1,1,11,0), new DateTime(2010,1,1,1,20,0)},
        {new DateTime(2010,1,1,1,20,0), new DateTime(2010,1,1,1,30,0)},
        {new DateTime(2010,1,1,1,39,0), new DateTime(2010,1,1,1,40,0)},
        {new DateTime(2010,1,1,1,59,0), new DateTime(2010,1,1,2,0,0)},
        {new DateTime(2010,1,1,23,59,0), new DateTime(2010,1,2,0,0,0)},
    };
    
    for (int i = 0; i < TIME_TABLE.length; i++) {
      DateTime dtResult = classUnderTest.createNextNominalTime(TIME_TABLE[i][0], 10);
      assertTrue("TT["+i+"] not as expected", dtResult.equals(TIME_TABLE[i][1]));
    }
  }

  @Test
  public void testCreatePrevNominalTime() throws Exception {
    DateTime[][] TIME_TABLE=new DateTime[][] {
        {new DateTime(2010,1,1,1,9,0), new DateTime(2010,1,1,0,50,0)},
        {new DateTime(2010,1,1,1,11,0), new DateTime(2010,1,1,1,0,0)},
        {new DateTime(2010,1,1,1,20,0), new DateTime(2010,1,1,1,10,0)},
        {new DateTime(2010,1,1,1,39,0), new DateTime(2010,1,1,1,20,0)},
        {new DateTime(2010,1,1,1,59,0), new DateTime(2010,1,1,1,40,0)},
        {new DateTime(2010,1,1,23,59,0), new DateTime(2010,1,1,23,40,0)},
        {new DateTime(2010,1,1,0,9,0), new DateTime(2009,12,31,23,50,0)},
    };
    
    for (int i = 0; i < TIME_TABLE.length; i++) {
      DateTime dtResult = classUnderTest.createPrevNominalTime(TIME_TABLE[i][0], 10);
      assertTrue("TT["+i+"] not as expected", dtResult.equals(TIME_TABLE[i][1]));
    }
  }

  @Test
  public void testTrigger() throws Exception {
    DateTime d1 = new DateTime(2010,1,1,1,1,1);
    DateTime d2 = new DateTime(2010,1,1,1,1,1);
    
    assertEquals(false, classUnderTest.isTriggered(21, d2));
    classUnderTest.trigger(21, d1);
    assertEquals(true, classUnderTest.isTriggered(21, d2));
  }
  
  @Test
  public void testTrigger_differentTime() throws Exception {
    DateTime d1 = new DateTime(2010,1,1,1,1,1);
    DateTime d2 = new DateTime(2010,1,1,1,2,1);
    
    classUnderTest.trigger(21, d1);
    assertEquals(false, classUnderTest.isTriggered(21, d2));
  }
  
  @Test
  public void testTrigger_severalFromSameRuleid() throws Exception {
    DateTime d1 = new DateTime(2010,1,1,1,1,1);
    DateTime d2 = new DateTime(2010,1,1,1,2,1);
    DateTime d3 = new DateTime(2010,1,1,1,3,1);
    
    classUnderTest.trigger(21, d1);
    classUnderTest.trigger(21, d2);
    classUnderTest.trigger(21, d3);
    assertEquals(true, classUnderTest.isTriggered(21, d1));
    assertEquals(true, classUnderTest.isTriggered(21, d2));
    assertEquals(true, classUnderTest.isTriggered(21, d3));
  }

  @Test
  public void testTrigger_backlog() throws Exception {
    DateTime d1 = new DateTime(2010,1,1,1,1,1);
    
    DateTime d2 = new DateTime(2010,1,1,1,2,1);
    
    classUnderTest.trigger(21, d1);
    
    for (int i = 0; i < 99; i++) {
      classUnderTest.trigger(22 + i, d2);
    }
    assertEquals(true, classUnderTest.isTriggered(21, d1));
    classUnderTest.trigger(122, d2);
    assertEquals(false, classUnderTest.isTriggered(21, d1));
  }

  
  private CatalogEntry createCatalogEntry(String src, DateTime dt) {
    CatalogEntry entry = createMock(CatalogEntry.class);
    FileEntry fileEntry = createMock(FileEntry.class);
    expect(entry.getFileEntry()).andReturn(fileEntry).times(0,99);
    expect(entry.getSource()).andReturn(src).times(0,99);
    expect(entry.getDateTime()).andReturn(dt).times(0,99);
    
    return entry;
  }
}
