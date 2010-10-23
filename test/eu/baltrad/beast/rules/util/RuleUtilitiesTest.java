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
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.fc.DateTime;


/**
 * @author Anders Henja
 */
public class RuleUtilitiesTest extends TestCase {
  private RuleUtilities classUnderTest = null;
  
  public void setUp() throws Exception {
    classUnderTest = new RuleUtilities();
  }
  
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  public void testGetEntryBySource() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("seang", "/tmp/1.h5", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seang", "/tmp/2.h5", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sehud", "/tmp/3.h5", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seosu", "/tmp/4.h5", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sevan", "/tmp/5.h5", new DateTime(2010,1,1,10,1,2)));
    
    CatalogEntry result = classUnderTest.getEntryBySource("seang", entries);
    assertEquals("/tmp/1.h5", result.getPath());
    result = classUnderTest.getEntryBySource("sehud", entries);
    assertEquals("/tmp/3.h5", result.getPath());
    result = classUnderTest.getEntryBySource("sevan", entries);
    assertEquals("/tmp/5.h5", result.getPath());
    result = classUnderTest.getEntryBySource("senone", entries);
    assertNull(result);
  }
  
  public void testGetFilesFromEntries() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("seang", "/tmp/1.h5", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seang", "/tmp/2.h5", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sehud", "/tmp/3.h5", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seosu", "/tmp/4.h5", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sevan", "/tmp/5.h5", new DateTime(2010,1,1,10,1,2)));
    
    List<String> sources = new ArrayList<String>();
    sources.add("seang");
    sources.add("sehud");
    sources.add("seosu");
    
    List<String> result = classUnderTest.getFilesFromEntries(new DateTime(2010,1,1,10,1,1), sources, entries);
    
    assertEquals(3, result.size());
    assertTrue(result.contains("/tmp/1.h5"));
    assertTrue(result.contains("/tmp/3.h5"));
    assertTrue(result.contains("/tmp/4.h5"));    
  }

  public void testGetFilesFromEntries_noEntries() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    
    List<String> sources = new ArrayList<String>();
    sources.add("seang");
    sources.add("sehud");
    sources.add("seosu");
    
    List<String> result = classUnderTest.getFilesFromEntries(new DateTime(2010,1,1,10,1,1), sources, entries);
    
    assertEquals(0, result.size());
  }

  public void testGetSourcesFromEntries() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("seang", "/tmp/1.h5", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seang", "/tmp/2.h5", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sehud", "/tmp/3.h5", new DateTime(2010,1,1,10,1,1)));
    entries.add(createCatalogEntry("seosu", "/tmp/4.h5", new DateTime(2010,1,1,10,1,2)));
    entries.add(createCatalogEntry("sevan", "/tmp/5.h5", new DateTime(2010,1,1,10,1,2)));
    
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

  
  private CatalogEntry createCatalogEntry(String src, String file, DateTime dt) {
    CatalogEntry result = new CatalogEntry();
    result.setSource(src);
    result.setPath(file);
    result.setDateTime(dt);
    return result;
  }
}
