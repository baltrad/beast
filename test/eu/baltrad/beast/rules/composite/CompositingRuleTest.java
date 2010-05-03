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
import eu.baltrad.fc.oh5.Source;

/**
 * @author Anders Henja
 */
public class CompositingRuleTest extends TestCase {
  private MockControl catalogControl = null;
  private Catalog catalog = null;
  private CompositingRule classUnderTest = null;
  
  private static interface ICompositingMethods {
    public TimeIntervalFilter createFilter(Date d, Time t);
    public IBltMessage createMessage(Date d, Time t, List<CatalogEntry> entries);
    public boolean areCriteriasMet(List<CatalogEntry> entries);
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
    methods.createFilter(date, time);
    methodsControl.setReturnValue(filter);
    catalog.fetch(filter);
    catalogControl.setReturnValue(entries);
    methods.areCriteriasMet(entries);
    methodsControl.setReturnValue(true);
    methods.createMessage(date, time, entries);
    methodsControl.setReturnValue(message);
    
    classUnderTest = new CompositingRule() {
      protected TimeIntervalFilter createFilter(Date d, Time t) {
        return methods.createFilter(d, t);
      }
      protected IBltMessage createMessage(Date d, Time t, List<CatalogEntry> e) {
        return methods.createMessage(d, t, e);
      }
      protected boolean areCriteriasMet(List<CatalogEntry> e) {
        return methods.areCriteriasMet(e);
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
    methods.createFilter(date, time);
    methodsControl.setReturnValue(filter);
    catalog.fetch(filter);
    catalogControl.setReturnValue(entries);
    methods.areCriteriasMet(entries);
    methodsControl.setReturnValue(false);
    
    classUnderTest = new CompositingRule() {
      protected TimeIntervalFilter createFilter(Date d, Time t) {
        return methods.createFilter(d, t);
      }
      protected IBltMessage createMessage(Date d, Time t, List<CatalogEntry> e) {
        return methods.createMessage(d, t, e);
      }
      protected boolean areCriteriasMet(List<CatalogEntry> e) {
        return methods.areCriteriasMet(e);
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
  
  public void testCreateMessage() {
    Date date = new Date(2010, 2, 1);
    Time time = new Time(1, 0, 0);
    
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("sekkr","/tmp/sekkr.h5"));
    entries.add(createCatalogEntry("selul","/tmp/selul.h5"));
    entries.add(createCatalogEntry("searl","/tmp/searl.h5"));
    
    classUnderTest.setArea("blt_composite");
    
    IBltMessage result = classUnderTest.createMessage(date, time, entries);
    assertTrue(result instanceof BltGenerateMessage);
    BltGenerateMessage msg = (BltGenerateMessage)result;
    assertEquals("eu.baltrad.beast.GenerateComposite", msg.getAlgorithm());
    String[] files = msg.getFiles();
    assertEquals(3, files.length);
    assertEquals("/tmp/sekkr.h5", files[0]);
    assertEquals("/tmp/selul.h5", files[1]);
    assertEquals("/tmp/searl.h5", files[2]);
    String[] arguments = msg.getArguments();
    assertEquals(3, arguments.length);
    assertEquals("--area=blt_composite", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
  }
  
  
  public void testCreateFilter() throws Exception {
    final Date date = new Date(2010,1,1);
    final Time time = new Time(1,2,0);
    Date startDate = new Date(2010,1,1);
    Time startTime = new Time(1,2,0);
    Date stopDate = new Date(2010,1,1);
    Time stopTime = new Time(1,3,0);
    final DateTime startDT = new DateTime(startDate, startTime);
    final DateTime stopDT = new DateTime(stopDate, stopTime);
    
    classUnderTest = new CompositingRule() {
      protected DateTime getStart(DateTime dt) {
        return startDT;
      };
      protected DateTime getStop(DateTime dt) {
        return stopDT;
      };
    };
    
    TimeIntervalFilter result = classUnderTest.createFilter(date, time);
    assertNotNull(result);
    assertSame(startDate, result.getStartDate());
    assertSame(startTime, result.getStartTime());
    assertSame(stopDate, result.getStopDate());
    assertSame(stopTime, result.getStopTime());
    assertEquals("PVOL", result.getObject());
  }
  
  public void testGetStartTime() throws Exception {
    classUnderTest.setInterval(10);

    DateTime[][] TIME_TABLE=new DateTime[][] {
        {new DateTime(2010,1,1,1,9,0), new DateTime(2010,1,1,1,0,0)},
        {new DateTime(2010,1,1,1,11,0), new DateTime(2010,1,1,1,10,0)},
        {new DateTime(2010,1,1,1,20,0), new DateTime(2010,1,1,1,20,0)},
        {new DateTime(2010,1,1,1,39,0), new DateTime(2010,1,1,1,30,0)},
        {new DateTime(2010,1,1,1,59,0), new DateTime(2010,1,1,1,50,0)},
    };
    
    for (int i = 0; i < TIME_TABLE.length; i++) {
      DateTime dtResult = classUnderTest.getStart(TIME_TABLE[i][0]);
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
  
  public void XtestHandle() {
    MockControl file1Control = MockClassControl.createControl(File.class);
    File file1 = (File)file1Control.getMock();
    MockControl sourceControl = MockClassControl.createControl(Source.class);
    Source source = (Source)sourceControl.getMock();
    
    BltDataMessage message = new BltDataMessage();
    message.setFile(file1);
    
    // Time: hour, minute, second, ms
    Time t1 = new Time(1, 0, 0, 0);
    Date d1 = new Date(2010, 04, 27);
    
    file1.what_object();
    file1Control.setReturnValue("PVOL");
    file1.what_time();
    file1Control.setReturnValue(t1);
    file1.what_date();
    file1Control.setReturnValue(d1);
    file1.source();
    file1Control.setReturnValue(source);
    source.node_id();
    sourceControl.setReturnValue("seang");
    
    file1Control.replay();
    sourceControl.replay();
    
    classUnderTest.handle(message);
    
    file1Control.verify();
    sourceControl.verify();
  }
}
