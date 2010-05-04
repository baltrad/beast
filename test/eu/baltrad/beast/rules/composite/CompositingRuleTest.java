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
package eu.baltrad.beast.rules.composite;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.DateTime;
import eu.baltrad.beast.db.filters.TimeIntervalFilter;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.fc.Date;
import eu.baltrad.fc.Time;
import eu.baltrad.fc.oh5.File;

/**
 * @author Anders Henja
 */
public class CompositingRuleTest extends TestCase {
  private MockControl catalogControl = null;
  private Catalog catalog = null;
  private CompositingRule classUnderTest = null;
  
  private static interface ICompositingMethods {
    public TimeIntervalFilter createFilter(DateTime nominalTime);
    public IBltMessage createMessage(DateTime nominalTime, List<CatalogEntry> entries);
    public boolean areCriteriasMet(List<CatalogEntry> entries);
    public DateTime getNominalTime(Date d, Time t);
  };
  
  protected void setUp() throws Exception {
    catalogControl = MockClassControl.createControl(Catalog.class);
    catalog = (Catalog)catalogControl.getMock();
    classUnderTest = new CompositingRule();
    classUnderTest.setCatalog(catalog);
  }
  
  protected void tearDown() throws Exception {
    catalogControl = null;
    catalog = null;
    classUnderTest = null;
  }
  
  protected void replay() {
    catalogControl.replay();
  }
  
  protected void verify() {
    catalogControl.verify();
  }
  
  public void testSetInterval() throws Exception {
    int[] valid = {1,2,3,4,5,6,10,12,15,20,30,60};
    for (int v : valid) {
      classUnderTest.setInterval(v);
    }
  }

  public void testSetInterval_invalid() throws Exception {
    int[] invalid = {0,7,8,9,11,13,14,16,17,18,19,21,22,23,24,25,26,27,28,29,35,40,61,62};
    for (int v : invalid) {
      try {
        classUnderTest.setInterval(v);
        fail("Expected IllegalArgumentException");
      } catch (IllegalArgumentException e) {
        // pass
      }
    }
  }
  
  public void testHandle() throws Exception {
    Date date = new Date(2010, 1, 1);
    Time time = new Time(10, 0, 0);
    DateTime nominalTime = new DateTime();
    TimeIntervalFilter filter = new TimeIntervalFilter();
    
    MockControl fileControl = MockClassControl.createControl(File.class);
    File file = (File)fileControl.getMock();
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();
    
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    
    IBltMessage message = new IBltMessage( ) {};
    
    BltDataMessage dataMessage = new BltDataMessage();
    dataMessage.setFile(file);

    file.what_object();
    fileControl.setReturnValue("PVOL");
    file.what_date();
    fileControl.setReturnValue(date);
    file.what_time();
    fileControl.setReturnValue(time);
    methods.getNominalTime(date, time);
    methodsControl.setReturnValue(nominalTime);
    methods.createFilter(nominalTime);
    methodsControl.setReturnValue(filter);
    catalog.fetch(filter);
    catalogControl.setReturnValue(entries);
    methods.areCriteriasMet(entries);
    methodsControl.setReturnValue(true);
    methods.createMessage(nominalTime, entries);
    methodsControl.setReturnValue(message);
    
    classUnderTest = new CompositingRule() {
      protected TimeIntervalFilter createFilter(DateTime nominalTime) {
        return methods.createFilter(nominalTime);
      }
      protected IBltMessage createMessage(DateTime nt, List<CatalogEntry> e) {
        return methods.createMessage(nt, e);
      }
      protected boolean areCriteriasMet(List<CatalogEntry> e) {
        return methods.areCriteriasMet(e);
      }
      protected DateTime getNominalTime(Date d, Time t) {
        return methods.getNominalTime(d, t);
      }

    };
    classUnderTest.setCatalog(catalog);
    
    replay();
    fileControl.replay();
    methodsControl.replay();
    
    IBltMessage result = classUnderTest.handle(dataMessage);
    
    verify();
    fileControl.verify();
    methodsControl.verify();
    
    assertSame(message, result);
  }

  public void testHandle_criteriasNotMet() throws Exception {
    Date date = new Date(2010, 1, 1);
    Time time = new Time(10, 0, 0);
    DateTime nominalTime = new DateTime();
    
    TimeIntervalFilter filter = new TimeIntervalFilter();
    
    MockControl fileControl = MockClassControl.createControl(File.class);
    File file = (File)fileControl.getMock();
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();
    
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    
    BltDataMessage dataMessage = new BltDataMessage();
    dataMessage.setFile(file);

    file.what_object();
    fileControl.setReturnValue("PVOL");
    file.what_date();
    fileControl.setReturnValue(date);
    file.what_time();
    fileControl.setReturnValue(time);
    methods.getNominalTime(date, time);
    methodsControl.setReturnValue(nominalTime);
    methods.createFilter(nominalTime);
    methodsControl.setReturnValue(filter);
    catalog.fetch(filter);
    catalogControl.setReturnValue(entries);
    methods.areCriteriasMet(entries);
    methodsControl.setReturnValue(false);
    
    classUnderTest = new CompositingRule() {
      protected TimeIntervalFilter createFilter(DateTime nt) {
        return methods.createFilter(nt);
      }
      protected IBltMessage createMessage(DateTime nt, List<CatalogEntry> e) {
        return methods.createMessage(nt, e);
      }
      protected boolean areCriteriasMet(List<CatalogEntry> e) {
        return methods.areCriteriasMet(e);
      }
      protected DateTime getNominalTime(Date d, Time t) {
        return methods.getNominalTime(d, t);
      }
    };
    classUnderTest.setCatalog(catalog);
    
    replay();
    fileControl.replay();
    methodsControl.replay();
    
    IBltMessage result = classUnderTest.handle(dataMessage);
    
    verify();
    fileControl.verify();
    methodsControl.verify();
    
    assertEquals(null, result);
  }
  
  
  public void testHandle_notBltDataMessage() throws Exception {
    IBltMessage msg = new IBltMessage() {};
    
    replay();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verify();
    assertEquals(null, result);
  }

  public void testHandle_notPolarVolume() throws Exception {
    MockControl fileControl = MockClassControl.createControl(File.class);
    File file = (File)fileControl.getMock();
    
    BltDataMessage dataMessage = new BltDataMessage();
    dataMessage.setFile(file);

    file.what_object();
    fileControl.setReturnValue("IMAGE");

    replay();
    fileControl.replay();
    
    IBltMessage result = classUnderTest.handle(dataMessage);
    
    verify();
    fileControl.verify();
    
    assertEquals(null, result);
  }
  
  protected CatalogEntry createCatalogEntry(String source, String file) {
    CatalogEntry result = new CatalogEntry();
    result.setSource(source);
    result.setPath(file);
    return result;
  }
  
  public void testAreCriteriasMet() {
    List<String> sources = new ArrayList<String>();
    sources.add("searl");
    sources.add("sekkr");
    sources.add("seosu");
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("sekkr",null));
    entries.add(createCatalogEntry("seosu",null));
    entries.add(createCatalogEntry("selul",null));
    entries.add(createCatalogEntry("searl",null));
    
    classUnderTest.setSources(sources);
    
    boolean result = classUnderTest.areCriteriasMet(entries);
    
    assertEquals(true, result);
  }

  public void testAreCriteriasMet_false() {
    List<String> sources = new ArrayList<String>();
    sources.add("searl");
    sources.add("sekkr");
    sources.add("seosu");
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("sekkr",null));
    entries.add(createCatalogEntry("selul",null));
    entries.add(createCatalogEntry("searl",null));
    
    classUnderTest.setSources(sources);
    
    boolean result = classUnderTest.areCriteriasMet(entries);
    
    assertEquals(false, result);
  }
  
  protected boolean arrayContains(String[] arr, String value) {
    for (String x : arr) {
      if (x.equals(value)) {
        return true;
      }
    }
    return false;
  }
  
  public void testCreateMessage() {
    Date date = new Date(2010, 2, 1);
    Time time = new Time(1, 0, 0);
    DateTime nominalTime = new DateTime(date, time);
    
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("sekkr","/tmp/sekkr.h5"));
    entries.add(createCatalogEntry("selul","/tmp/selul.h5"));
    entries.add(createCatalogEntry("searl","/tmp/searl.h5"));
    
    classUnderTest.setArea("blt_composite");
    
    List<String> sources = new ArrayList<String>();
    sources.add("sekkr");
    sources.add("selul");
    sources.add("searl");
    classUnderTest.setSources(sources);
    
    IBltMessage result = classUnderTest.createMessage(nominalTime, entries);
    assertTrue(result instanceof BltGenerateMessage);
    BltGenerateMessage msg = (BltGenerateMessage)result;
    assertEquals("eu.baltrad.beast.GenerateComposite", msg.getAlgorithm());
    String[] files = msg.getFiles();
    assertEquals(3, files.length);
    assertTrue(arrayContains(files, "/tmp/sekkr.h5"));
    assertTrue(arrayContains(files, "/tmp/selul.h5"));
    assertTrue(arrayContains(files, "/tmp/searl.h5"));
    String[] arguments = msg.getArguments();
    assertEquals(3, arguments.length);
    assertEquals("--area=blt_composite", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
  }
  
  public void testCreateFilter() throws Exception {
    Date startDate = new Date(2010,1,1);
    Time startTime = new Time(1,2,0);
    Date stopDate = new Date(2010,1,1);
    Time stopTime = new Time(1,3,0);
    final DateTime startDT = new DateTime(startDate, startTime);
    final DateTime stopDT = new DateTime(stopDate, stopTime);
    
    classUnderTest = new CompositingRule() {
      protected DateTime getStop(DateTime dt) {
        return stopDT;
      };
    };
    
    TimeIntervalFilter result = classUnderTest.createFilter(startDT);
    assertNotNull(result);
    assertSame(startDate, result.getStartDate());
    assertSame(startTime, result.getStartTime());
    assertSame(stopDate, result.getStopDate());
    assertSame(stopTime, result.getStopTime());
    assertEquals("PVOL", result.getObject());
  }
  
  public void testGetNominalTime() throws Exception {
    classUnderTest.setInterval(10);

    DateTime[][] TIME_TABLE=new DateTime[][] {
        {new DateTime(2010,1,1,1,9,0), new DateTime(2010,1,1,1,0,0)},
        {new DateTime(2010,1,1,1,11,0), new DateTime(2010,1,1,1,10,0)},
        {new DateTime(2010,1,1,1,20,0), new DateTime(2010,1,1,1,20,0)},
        {new DateTime(2010,1,1,1,39,0), new DateTime(2010,1,1,1,30,0)},
        {new DateTime(2010,1,1,1,59,0), new DateTime(2010,1,1,1,50,0)},
    };
    
    for (int i = 0; i < TIME_TABLE.length; i++) {
      DateTime dtResult = classUnderTest.getNominalTime(TIME_TABLE[i][0].getDate(), TIME_TABLE[i][0].getTime());
      assertTrue("TT["+i+"] not as expected", dtResult.equals(TIME_TABLE[i][1]));
    }
  }

  public void testGetStopTime() throws Exception {
    classUnderTest.setInterval(10);

    DateTime[][] TIME_TABLE=new DateTime[][] {
        {new DateTime(2010,1,1,1,9,0), new DateTime(2010,1,1,1,10,0)},
        {new DateTime(2010,1,1,1,11,0), new DateTime(2010,1,1,1,20,0)},
        {new DateTime(2010,1,1,1,20,0), new DateTime(2010,1,1,1,30,0)},
        {new DateTime(2010,1,1,1,39,0), new DateTime(2010,1,1,1,40,0)},
        {new DateTime(2010,1,1,1,59,0), new DateTime(2010,1,1,2,0,0)},
        {new DateTime(2010,1,1,23,59,0), new DateTime(2010,1,2,0,0,0)},
    };

    for (int i = 0; i < TIME_TABLE.length; i++) {
      DateTime dtResult = classUnderTest.getStop(TIME_TABLE[i][0]);
      assertTrue("TT["+i+"] not as expected", dtResult.equals(TIME_TABLE[i][1]));
    }
  }
  
  public void testGetFilesFromEntries() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("seang", "/tmp/1.h5", new Date(2010,1,1), new Time(10,1,1)));
    entries.add(createCatalogEntry("seang", "/tmp/2.h5", new Date(2010,1,1), new Time(10,1,2)));
    entries.add(createCatalogEntry("sehud", "/tmp/3.h5", new Date(2010,1,1), new Time(10,1,1)));
    entries.add(createCatalogEntry("seosu", "/tmp/4.h5", new Date(2010,1,1), new Time(10,1,2)));
    entries.add(createCatalogEntry("sevan", "/tmp/5.h5", new Date(2010,1,1), new Time(10,1,2)));
    
    List<String> sources = new ArrayList<String>();
    sources.add("seang");
    sources.add("sehud");
    sources.add("seosu");
    classUnderTest.setSources(sources);
    
    List<String> result = classUnderTest.getFilesFromEntries(new DateTime(2010,1,1,10,1,1), entries);
    assertEquals(3, result.size());
    assertTrue(result.contains("/tmp/1.h5"));
    assertTrue(result.contains("/tmp/3.h5"));
    assertTrue(result.contains("/tmp/4.h5"));
  }
  
  private CatalogEntry createCatalogEntry(String src, String file, Date date, Time time) {
    CatalogEntry result = new CatalogEntry();
    result.setSource(src);
    result.setPath(file);
    result.setDate(date);
    result.setTime(time);
    return result;
  }
}
