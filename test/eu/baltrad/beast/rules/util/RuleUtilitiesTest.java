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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;

import eu.baltrad.fc.DateTime;
import eu.baltrad.fc.FileCatalog;
import eu.baltrad.fc.LocalStorage;
import eu.baltrad.fc.db.FileEntry;


/**
 * @author Anders Henja
 */
public class RuleUtilitiesTest extends TestCase {
  private RuleUtilities classUnderTest = null;
  private MockControl catalogControl = null;
  private Catalog catalog = null;
  private MockControl fileCatalogControl = null;
  private FileCatalog fileCatalog = null;
  private MockControl storageControl = null;
  private LocalStorage storage = null;
  
  public void setUp() throws Exception {
    catalogControl = MockClassControl.createControl(Catalog.class);
    catalog = (Catalog)catalogControl.getMock();
    fileCatalogControl = MockClassControl.createControl(FileCatalog.class);
    fileCatalog = (FileCatalog)fileCatalogControl.getMock();
    storageControl = MockClassControl.createControl(LocalStorage.class);
    storage = (LocalStorage)storageControl.getMock();
    classUnderTest = new RuleUtilities();
    classUnderTest.setCatalog(catalog);
  }
  
  public void tearDown() throws Exception {
    classUnderTest = null;
    storage = null;
    storageControl = null;
    fileCatalog = null;
    fileCatalogControl = null;
    catalog = null;
    catalogControl = null;
  }

  protected void replay() {
    catalogControl.replay();
    fileCatalogControl.replay();
    storageControl.replay();
  }
  
  protected void verify() {
    catalogControl.verify();
    fileCatalogControl.verify();
    storageControl.verify();
  }

  public void testGetEntryBySource() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("seang", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seang", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sehud", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seosu", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sevan", new DateTime(2010,1,1,10,1,2)));
    
    CatalogEntry result = classUnderTest.getEntryBySource("seang", entries);
    assertEquals(entries.get(0), result);
    result = classUnderTest.getEntryBySource("sehud", entries);
    assertEquals(entries.get(2), result);
    result = classUnderTest.getEntryBySource("sevan", entries);
    assertEquals(entries.get(4), result);
    result = classUnderTest.getEntryBySource("senone", entries);
    assertNull(result);
  }

  public void testGetEntriesByClosestTime() {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("seang", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seang", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sehud", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seosu", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("seosu", new DateTime(2010,1,1,10,1,2)));
    
    List<CatalogEntry> result = classUnderTest.getEntriesByClosestTime(new DateTime(2010,1,1,10,1,1), entries);
    
    assertEquals(3, result.size());
    assertTrue(result.contains(entries.get(0)));
    assertTrue(result.contains(entries.get(2)));
    assertTrue(result.contains(entries.get(3)));
  }

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

    List<CatalogEntry> result = classUnderTest.getEntriesBySources(sources, entries);

    assertEquals(4, result.size());
    assertTrue(result.contains(entries.get(0)));
    assertTrue(result.contains(entries.get(1)));
    assertTrue(result.contains(entries.get(2)));
    assertTrue(result.contains(entries.get(3)));
  }

  public void testGetFilesFromEntries() {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("seang", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seang", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sehud", new DateTime(2010,1,1,10,1,1)));

    catalog.getCatalog();
    catalogControl.setReturnValue(fileCatalog);
    fileCatalog.storage();
    fileCatalogControl.setReturnValue(storage);
    storage.store(entries.get(0).getFileEntry());
    storageControl.setReturnValue("/tmp/1.h5");
    storage.store(entries.get(1).getFileEntry());
    storageControl.setReturnValue("/tmp/2.h5");
    storage.store(entries.get(2).getFileEntry());
    storageControl.setReturnValue("/tmp/3.h5");
    replay();

    List<String> result = classUnderTest.getFilesFromEntries(entries);

    verify();
    assertEquals(3, result.size());
    assertTrue(result.contains("/tmp/1.h5"));
    assertTrue(result.contains("/tmp/2.h5"));
    assertTrue(result.contains("/tmp/3.h5"));
  }

  public void testGetSourcesFromEntries() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("seang", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seang", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sehud", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seosu", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sevan", new DateTime(2010,1,1,10,1,2)));
    
    List<String> result = classUnderTest.getSourcesFromEntries(entries);
    
    assertEquals(4, result.size());
    assertTrue(result.contains("seang"));
    assertTrue(result.contains("sehud"));
    assertTrue(result.contains("seosu"));
    assertTrue(result.contains("sevan"));
  }

  public void testGetSourcesFromEntries_noEntries() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    
    List<String> result = classUnderTest.getSourcesFromEntries(entries);
    
    assertEquals(0, result.size());
  }
  
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

  public void testCreateNominalTime_dateAndTime() throws Exception {
    DateTime[][] TIME_TABLE=new DateTime[][] {
        {new DateTime(2010,1,1,1,9,0), new DateTime(2010,1,1,1,0,0)},
        {new DateTime(2010,1,1,1,11,0), new DateTime(2010,1,1,1,10,0)},
        {new DateTime(2010,1,1,1,20,0), new DateTime(2010,1,1,1,20,0)},
        {new DateTime(2010,1,1,1,39,0), new DateTime(2010,1,1,1,30,0)},
        {new DateTime(2010,1,1,1,59,0), new DateTime(2010,1,1,1,50,0)},
    };
    
    for (int i = 0; i < TIME_TABLE.length; i++) {
      DateTime dtResult = classUnderTest.createNominalTime(TIME_TABLE[i][0].date(), TIME_TABLE[i][0].time(), 10);
      assertTrue("TT["+i+"] not as expected", dtResult.equals(TIME_TABLE[i][1]));
    }
  }

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

  public void testTrigger() throws Exception {
    DateTime d1 = new DateTime(2010,1,1,1,1,1);
    DateTime d2 = new DateTime(2010,1,1,1,1,1);
    
    assertEquals(false, classUnderTest.isTriggered(21, d2));
    classUnderTest.trigger(21, d1);
    assertEquals(true, classUnderTest.isTriggered(21, d2));
  }
  
  public void testTrigger_differentTime() throws Exception {
    DateTime d1 = new DateTime(2010,1,1,1,1,1);
    DateTime d2 = new DateTime(2010,1,1,1,2,1);
    
    classUnderTest.trigger(21, d1);
    assertEquals(false, classUnderTest.isTriggered(21, d2));
  }
  
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
    MockControl entryControl = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry entry = (CatalogEntry)entryControl.getMock();
    MockControl fileEntryControl = MockClassControl.createControl(FileEntry.class);
    FileEntry fileEntry = (FileEntry)fileEntryControl.getMock();
    entry.getFileEntry();
    entryControl.setReturnValue(fileEntry, MockControl.ZERO_OR_MORE);
    entry.getSource();
    entryControl.setReturnValue(src, MockControl.ZERO_OR_MORE);
    entry.getDateTime();
    entryControl.setReturnValue(dt, MockControl.ZERO_OR_MORE);
    entryControl.replay();
    fileEntryControl.replay();
    return entry;
  }
}
