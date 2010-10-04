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

import eu.baltrad.beast.ManagerContext;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.filters.TimeIntervalFilter;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.timer.ITimeoutRule;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.timer.TimeoutTask;
import eu.baltrad.fc.Date;
import eu.baltrad.fc.DateTime;
import eu.baltrad.fc.Time;
import eu.baltrad.fc.oh5.File;

/**
 * @author Anders Henja
 */
public class CompositingRuleTest extends TestCase {
  private MockControl catalogControl = null;
  private Catalog catalog = null;
  private CompositingRule classUnderTest = null;
  private MockControl timeoutControl = null;
  private TimeoutManager timeoutManager = null;
  
  private static interface ICompositingMethods {
    public CompositeTimerData createTimerData(IBltMessage message);
    public List<CatalogEntry> fetchEntries(DateTime nominalTime);
    public TimeIntervalFilter createFilter(DateTime nominalTime);
    public IBltMessage createMessage(DateTime nominalTime, List<CatalogEntry> entries);
    public boolean areCriteriasMet(List<CatalogEntry> entries);
    public DateTime getNominalTime(Date d, Time t);
    public void initialize();
  };
  
  protected void setUp() throws Exception {
    catalogControl = MockClassControl.createControl(Catalog.class);
    catalog = (Catalog)catalogControl.getMock();
    timeoutControl = MockClassControl.createControl(TimeoutManager.class);
    timeoutManager = (TimeoutManager)timeoutControl.getMock();
    classUnderTest = new CompositingRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
  }
  
  protected void tearDown() throws Exception {
    catalogControl = null;
    catalog = null;
    classUnderTest = null;
  }
  
  protected void replay() {
    catalogControl.replay();
    timeoutControl.replay();
  }
  
  protected void verify() {
    catalogControl.verify();
    timeoutControl.verify();
  }
  
  public void testSetGetRuleId() throws Exception {
    classUnderTest.setRuleid(103);
    assertEquals(103, classUnderTest.getRuleid());
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
  
  public void testGetType() {
    assertEquals("blt_composite", classUnderTest.getType());
  }
  
  public void testHandle() throws Exception {
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();

    IBltMessage message = new IBltMessage() {
    };
    IBltMessage resultMessage = new IBltMessage() {
    };
    DateTime dt = new DateTime(2010, 4, 15, 10, 15, 0);
    CompositeTimerData ctd = new CompositeTimerData(15, dt);
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    TimeoutTask tt = new TimeoutTask();
    tt.setId(10);
    methods.initialize();
    methods.createTimerData(message);
    methodsControl.setReturnValue(ctd);
    methods.fetchEntries(dt);
    methodsControl.setReturnValue(entries);
    timeoutManager.getRegisteredTask(ctd);
    timeoutControl.setReturnValue(tt);
    methods.areCriteriasMet(entries);
    methodsControl.setReturnValue(true);
    methods.createMessage(dt, entries);
    methodsControl.setReturnValue(resultMessage);
    timeoutManager.unregister(10);
    
    classUnderTest = new CompositingRule() {
      protected void initialize() {
        methods.initialize();
      }
      protected CompositeTimerData createTimerData(IBltMessage m) {
        return methods.createTimerData(m);
      }
      protected List<CatalogEntry> fetchEntries(DateTime nt) {
        return methods.fetchEntries(nt);
      }
      protected boolean areCriteriasMet(List<CatalogEntry> e) {
        return methods.areCriteriasMet(e);
      }
      protected IBltMessage createMessage(DateTime dt, List<CatalogEntry> e) {
        return methods.createMessage(dt, e);
      }
    };
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    
    replay();
    methodsControl.replay();
    
    IBltMessage result = classUnderTest.handle(message);
    
    verify();
    methodsControl.verify();
    assertSame(resultMessage, result);
  }

  public void testHandle_CriteriasNotFulfilled() throws Exception {
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();

    IBltMessage message = new IBltMessage() {
    };
    DateTime dt = new DateTime(2010, 4, 15, 10, 15, 0);
    CompositeTimerData ctd = new CompositeTimerData(15, dt);
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    TimeoutTask tt = new TimeoutTask();
    tt.setId(10);
    methods.initialize();
    methods.createTimerData(message);
    methodsControl.setReturnValue(ctd);
    methods.fetchEntries(dt);
    methodsControl.setReturnValue(entries);
    timeoutManager.getRegisteredTask(ctd);
    timeoutControl.setReturnValue(tt);
    methods.areCriteriasMet(entries);
    methodsControl.setReturnValue(false);
    
    classUnderTest = new CompositingRule() {
      protected void initialize() {
        methods.initialize();
      }
      protected CompositeTimerData createTimerData(IBltMessage m) {
        return methods.createTimerData(m);
      }
      protected List<CatalogEntry> fetchEntries(DateTime nt) {
        return methods.fetchEntries(nt);
      }
      protected boolean areCriteriasMet(List<CatalogEntry> e) {
        return methods.areCriteriasMet(e);
      }
      protected IBltMessage createMessage(DateTime dt, List<CatalogEntry> e) {
        return methods.createMessage(dt, e);
      }
    };
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    
    replay();
    methodsControl.replay();
    
    IBltMessage result = classUnderTest.handle(message);
    
    verify();
    methodsControl.verify();
    assertEquals(null, result);
  }

  public void testHandle_CriteriasNotMetAndNoRegisteredTimeout() throws Exception {
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();

    IBltMessage message = new IBltMessage() {
    };
    DateTime dt = new DateTime(2010, 4, 15, 10, 15, 0);
    CompositeTimerData ctd = new CompositeTimerData(15, dt);
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    TimeoutTask tt = new TimeoutTask();
    tt.setId(10);
    methods.initialize();
    methods.createTimerData(message);
    methodsControl.setReturnValue(ctd);
    methods.fetchEntries(dt);
    methodsControl.setReturnValue(entries);
    timeoutManager.getRegisteredTask(ctd);
    timeoutControl.setReturnValue(null);
    methods.areCriteriasMet(entries);
    methodsControl.setReturnValue(false);
    // continuing with mocking after classUnderTest declaration
    
    classUnderTest = new CompositingRule() {
      protected void initialize() {
        methods.initialize();
      }
      protected CompositeTimerData createTimerData(IBltMessage m) {
        return methods.createTimerData(m);
      }
      protected List<CatalogEntry> fetchEntries(DateTime nt) {
        return methods.fetchEntries(nt);
      }
      protected boolean areCriteriasMet(List<CatalogEntry> e) {
        return methods.areCriteriasMet(e);
      }
      protected IBltMessage createMessage(DateTime dt, List<CatalogEntry> e) {
        return methods.createMessage(dt, e);
      }
    };
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setTimeout(10*60);
    
    // continue with mocking...
    timeoutManager.register(classUnderTest, (long)1000*10*60, ctd);
    timeoutControl.setReturnValue((long)10);
    
    replay();
    methodsControl.replay();
    
    IBltMessage result = classUnderTest.handle(message);
    
    verify();
    methodsControl.verify();
    assertEquals(null, result);
  }
   
  public void testTimeout() throws Exception {
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();
    DateTime dt = new DateTime(2010, 4, 15, 10, 15, 0);
    CompositeTimerData ctd = new CompositeTimerData(15, dt);
    IBltMessage resultMessage = new IBltMessage() {
    };
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    
    methods.initialize();
    methods.fetchEntries(dt);
    methodsControl.setReturnValue(entries);
    methods.createMessage(dt, entries);
    methodsControl.setReturnValue(resultMessage);
    // continuing with mocking after classUnderTest declaration
    
    classUnderTest = new CompositingRule() {
      protected void initialize() {
        methods.initialize();
      }
      protected List<CatalogEntry> fetchEntries(DateTime nominalTime) {
        return methods.fetchEntries(nominalTime);
      }
      protected IBltMessage createMessage(DateTime nominalTime, List<CatalogEntry> entries) {
        return methods.createMessage(nominalTime, entries);
      }
    };
    
    replay();
    methodsControl.replay();
    
    IBltMessage result = classUnderTest.timeout(15, ITimeoutRule.TIMEOUT, ctd);
    
    verify();
    methodsControl.verify();
    assertSame(resultMessage, result);
  }
  
  public void testInitialize() throws Exception {
    new ManagerContext().setCatalog(catalog);
    new ManagerContext().setTimeoutManager(timeoutManager);
    CompositingRule classUnderTest = new CompositingRule();
    assertEquals(null, classUnderTest.catalog);
    assertEquals(null, classUnderTest.timeoutManager);
    classUnderTest.initialize();
    assertSame(catalog, classUnderTest.catalog);
    assertSame(timeoutManager, classUnderTest.timeoutManager);
  }
  
  public void testCreateTimerData() throws Exception {
    Date date = new Date(2010, 1, 1);
    Time time = new Time(10, 0, 0);
    DateTime nominalTime = new DateTime(date, time);

    MockControl fileControl = MockClassControl.createControl(File.class);
    File file = (File)fileControl.getMock();
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();

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
    
    classUnderTest = new CompositingRule() {
      public DateTime getNominalTime(Date d, Time t) {
        return methods.getNominalTime(d, t);
      }
    };
    classUnderTest.setRuleid(25);

    replay();
    fileControl.replay();
    methodsControl.replay();
    
    CompositeTimerData result = classUnderTest.createTimerData(dataMessage);
    
    verify();
    fileControl.verify();
    methodsControl.verify();
    assertSame(nominalTime, result.getDateTime());
    assertEquals(25, result.getRuleId());
  }

  public void testCreateTimerData_notVolume() throws Exception {
    MockControl fileControl = MockClassControl.createControl(File.class);
    File file = (File)fileControl.getMock();
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();

    BltDataMessage dataMessage = new BltDataMessage();
    dataMessage.setFile(file);

    
    file.what_object();
    fileControl.setReturnValue("IMAGE");
    
    classUnderTest = new CompositingRule() {
      public DateTime getNominalTime(Date d, Time t) {
        return methods.getNominalTime(d, t);
      }
    };
    classUnderTest.setRuleid(25);

    replay();
    fileControl.replay();
    methodsControl.replay();
    
    CompositeTimerData result = classUnderTest.createTimerData(dataMessage);
    
    verify();
    fileControl.verify();
    methodsControl.verify();
    assertEquals(null, result);
  }

  public void testCreateTimerData_notBltDataMessage() throws Exception {
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();

    IBltMessage dataMessage = new IBltMessage() {
    };
    
    classUnderTest = new CompositingRule() {
      public DateTime getNominalTime(Date d, Time t) {
        return methods.getNominalTime(d, t);
      }
    };
    classUnderTest.setRuleid(25);

    replay();
    methodsControl.replay();
    
    CompositeTimerData result = classUnderTest.createTimerData(dataMessage);
    
    verify();
    methodsControl.verify();
    assertEquals(null, result);
  }

  public void testCreateTimerData_nullMessage() throws Exception {
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();

    classUnderTest = new CompositingRule() {
      public DateTime getNominalTime(Date d, Time t) {
        return methods.getNominalTime(d, t);
      }
    };
    classUnderTest.setRuleid(25);

    replay();
    methodsControl.replay();
    
    CompositeTimerData result = classUnderTest.createTimerData(null);
    
    verify();
    methodsControl.verify();
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
  
  public void testFetchEntries() throws Exception {
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();
    DateTime dt = new DateTime(2010,1,1,10,0,0);
    TimeIntervalFilter filter = new TimeIntervalFilter();
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    
    methods.createFilter(dt);
    methodsControl.setReturnValue(filter);
    catalog.fetch(filter);
    catalogControl.setReturnValue(entries);
    
    classUnderTest = new CompositingRule() {
      protected TimeIntervalFilter createFilter(DateTime nt) {
        return methods.createFilter(nt);
      }
    };
    classUnderTest.setCatalog(catalog);
    
    replay();
    methodsControl.replay();
    
    List<CatalogEntry> result = classUnderTest.fetchEntries(dt);
    
    verify();
    methodsControl.verify();
    assertSame(entries, result);
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
    assertSame(startDT, result.getStartDateTime());
    assertSame(stopDT, result.getStopDateTime());
    assertEquals("PVOL", result.getObject());
  }
  
  protected boolean compareDT(DateTime o1, DateTime o2) {
    return (o1.date().year() == o2.date().year() &&
            o1.date().month() == o2.date().month() &&
            o1.date().day() == o2.date().day() &&
            o1.time().hour() == o2.time().hour() &&
            o1.time().minute() == o2.time().minute() &&
            o1.time().second() == o2.time().second());
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
      DateTime dtResult = classUnderTest.getNominalTime(TIME_TABLE[i][0].date(), TIME_TABLE[i][0].time());
      assertTrue("TT["+i+"] not as expected", compareDT(dtResult, TIME_TABLE[i][1]));
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
      assertTrue("TT["+i+"] not as expected", compareDT(dtResult, TIME_TABLE[i][1]));
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
    result.setDateTime(new DateTime(date, time));
    return result;
  }
}
