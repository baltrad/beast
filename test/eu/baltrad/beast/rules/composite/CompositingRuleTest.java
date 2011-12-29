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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.springframework.beans.factory.BeanInitializationException;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.bdb.util.Time;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.filters.LowestAngleFilter;
import eu.baltrad.beast.db.filters.TimeIntervalFilter;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.router.IMultiRoutedMessage;
import eu.baltrad.beast.rules.timer.ITimeoutRule;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 */
public class CompositingRuleTest extends TestCase {
  private MockControl catalogControl = null;
  private Catalog catalog = null;
  private MockControl ruleUtilControl = null;
  private IRuleUtilities ruleUtil = null;
  private CompositingRule classUnderTest = null;
  private MockControl timeoutControl = null;
  private TimeoutManager timeoutManager = null;
  
  private static interface ICompositingMethods {
    public CompositeTimerData createTimerData(IBltMessage message);
    public List<CatalogEntry> fetchEntries(DateTime nominalTime);
    public TimeIntervalFilter createFilter(DateTime nominalTime);
    public List<CatalogEntry> fetchScanEntries(DateTime nominalTime);
    public LowestAngleFilter createScanFilter(DateTime nominalTime);
    public IBltMessage createMessage(DateTime nominalTime, List<CatalogEntry> entries);
    public boolean areCriteriasMet(List<CatalogEntry> entries);
  };
  
  protected void setUp() throws Exception {
    catalogControl = MockClassControl.createControl(Catalog.class);
    catalog = (Catalog)catalogControl.getMock();
    timeoutControl = MockClassControl.createControl(TimeoutManager.class);
    timeoutManager = (TimeoutManager)timeoutControl.getMock();
    ruleUtilControl = MockClassControl.createControl(IRuleUtilities.class);
    ruleUtil = (IRuleUtilities)ruleUtilControl.getMock();
    classUnderTest = new CompositingRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setRuleUtilities(ruleUtil);
  }
  
  protected void tearDown() throws Exception {
    catalogControl = null;
    catalog = null;
    classUnderTest = null;
  }
  
  protected void replay() {
    catalogControl.replay();
    timeoutControl.replay();
    ruleUtilControl.replay();
  }
  
  protected void verify() {
    catalogControl.verify();
    timeoutControl.verify();
    ruleUtilControl.verify();
  }
  
  public void testSetGetRuleId() throws Exception {
    classUnderTest.setRuleId(103);
    assertEquals(103, classUnderTest.getRuleId());
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
  
  public void testTimeout() throws Exception {
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();
    DateTime dt = new DateTime(2010, 4, 15, 10, 15, 0);
    CompositeTimerData ctd = new CompositeTimerData(15, dt);
    IBltMessage resultMessage = new IBltMessage() {
    };
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    List<String> recipients = new ArrayList<String>();
    
    methods.fetchEntries(dt);
    methodsControl.setReturnValue(entries);
    ruleUtil.isTriggered(25, dt);
    ruleUtilControl.setReturnValue(false);
    methods.createMessage(dt, entries);
    methodsControl.setReturnValue(resultMessage);
    ruleUtil.trigger(25, dt);
    
    // continuing with mocking after classUnderTest declaration
    
    classUnderTest = new CompositingRule() {
      protected List<CatalogEntry> fetchEntries(DateTime nominalTime) {
        return methods.fetchEntries(nominalTime);
      }
      protected IBltMessage createMessage(DateTime nominalTime, List<CatalogEntry> entries) {
        return methods.createMessage(nominalTime, entries);
      }
    };
    classUnderTest.setRecipients(recipients);
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setRuleId(25);
    
    replay();
    methodsControl.replay();
    
    IBltMessage result = classUnderTest.timeout(15, ITimeoutRule.TIMEOUT, ctd);
    
    verify();
    methodsControl.verify();
    assertSame(resultMessage, ((IMultiRoutedMessage)result).getMessage());
    assertSame(recipients, ((IMultiRoutedMessage)result).getDestinations());
  }
  
  public void testTimeout_alreadyTriggered() throws Exception {
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();
    DateTime dt = new DateTime(2010, 4, 15, 10, 15, 0);
    CompositeTimerData ctd = new CompositeTimerData(15, dt);
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    List<String> recipients = new ArrayList<String>();
    
    methods.fetchEntries(dt);
    methodsControl.setReturnValue(entries);
    ruleUtil.isTriggered(25, dt);
    ruleUtilControl.setReturnValue(true);
    
    // continuing with mocking after classUnderTest declaration
    
    classUnderTest = new CompositingRule() {
      protected List<CatalogEntry> fetchEntries(DateTime nominalTime) {
        return methods.fetchEntries(nominalTime);
      }
      protected IBltMessage createMessage(DateTime nominalTime, List<CatalogEntry> entries) {
        return methods.createMessage(nominalTime, entries);
      }
    };
    classUnderTest.setRecipients(recipients);
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setRuleId(25);
    
    replay();
    methodsControl.replay();
    
    IBltMessage result = classUnderTest.timeout(15, ITimeoutRule.TIMEOUT, ctd);
    
    verify();
    methodsControl.verify();
    assertNull(result);
  }
  
  public void testAfterPropertiesSet() throws Exception {
    CompositingRule classUnderTest = new CompositingRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setTimeoutManager(timeoutManager);
    
    classUnderTest.afterPropertiesSet();
  }

  public void testAfterPropertiesSet_missingCatalog() throws Exception {
    CompositingRule classUnderTest = new CompositingRule();
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setTimeoutManager(timeoutManager);
    
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected BeanInitializationException");
    } catch (BeanInitializationException e) {
      // pass
    }
  }

  public void testAfterPropertiesSet_missingRuleUtilities() throws Exception {
    CompositingRule classUnderTest = new CompositingRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected BeanInitializationException");
    } catch (BeanInitializationException e) {
      // pass
    }
  }

  public void testAfterPropertiesSet_missingTimeoutManager() throws Exception {
    CompositingRule classUnderTest = new CompositingRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtil);
    
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected BeanInitializationException");
    } catch (BeanInitializationException e) {
      // pass
    }
  }

  public void testCreateTimerData() throws Exception {
    Date date = new Date(2010, 1, 1);
    Time time = new Time(10, 0, 0);
    DateTime nominalTime = new DateTime(date, time);

    MockControl fileControl = MockClassControl.createControl(FileEntry.class);
    FileEntry file = (FileEntry)fileControl.getMock();
    MockControl metadataControl = MockClassControl.createControl(Metadata.class);
    Metadata metadata = (Metadata)metadataControl.getMock();

    BltDataMessage dataMessage = new BltDataMessage();
    dataMessage.setFileEntry(file);

    file.getMetadata();
    fileControl.setReturnValue(metadata, MockControl.ONE_OR_MORE);
    metadata.getWhatObject();
    metadataControl.setReturnValue("PVOL");
    metadata.getWhatDate();
    metadataControl.setReturnValue(date);
    metadata.getWhatTime();
    metadataControl.setReturnValue(time);
    ruleUtil.createNominalTime(date, time, 10);
    ruleUtilControl.setReturnValue(nominalTime);
    ruleUtil.isTriggered(25, nominalTime);
    ruleUtilControl.setReturnValue(false);
    
    classUnderTest.setRuleId(25);
    classUnderTest.setInterval(10);
    
    replay();
    fileControl.replay();
    metadataControl.replay();
    
    CompositeTimerData result = classUnderTest.createTimerData(dataMessage);
    
    verify();
    fileControl.verify();
    metadataControl.verify();
    assertSame(nominalTime, result.getDateTime());
    assertEquals(25, result.getRuleId());
  }

  public void testCreateTimerData_alreadyTriggered() throws Exception {
    Date date = new Date(2010, 1, 1);
    Time time = new Time(10, 0, 0);
    DateTime nominalTime = new DateTime(date, time);

    MockControl fileControl = MockClassControl.createControl(FileEntry.class);
    FileEntry file = (FileEntry)fileControl.getMock();
    MockControl metadataControl = MockClassControl.createControl(Metadata.class);
    Metadata metadata = (Metadata)metadataControl.getMock();

    BltDataMessage dataMessage = new BltDataMessage();
    dataMessage.setFileEntry(file);

    
    file.getMetadata();
    fileControl.setReturnValue(metadata, MockControl.ONE_OR_MORE);
    metadata.getWhatObject();
    metadataControl.setReturnValue("PVOL");
    metadata.getWhatDate();
    metadataControl.setReturnValue(date);
    metadata.getWhatTime();
    metadataControl.setReturnValue(time);
    ruleUtil.createNominalTime(date, time, 10);
    ruleUtilControl.setReturnValue(nominalTime);
    ruleUtil.isTriggered(25, nominalTime);
    ruleUtilControl.setReturnValue(true);
    
    classUnderTest.setRuleId(25);
    classUnderTest.setInterval(10);
    
    replay();
    fileControl.replay();
    metadataControl.replay();
    
    CompositeTimerData result = classUnderTest.createTimerData(dataMessage);
    
    verify();
    fileControl.verify();
    metadataControl.verify();
    assertNull(result);
  }

  public void testCreateTimerData_notVolume() throws Exception {
    MockControl fileControl = MockClassControl.createControl(FileEntry.class);
    FileEntry file = (FileEntry)fileControl.getMock();
    MockControl metadataControl = MockClassControl.createControl(Metadata.class);
    Metadata metadata = (Metadata)metadataControl.getMock();
    Date d = new Date(2010, 1, 1);
    Time t = new Time(10, 1, 15);
    DateTime dt = new DateTime();
    
    BltDataMessage dataMessage = new BltDataMessage();
    dataMessage.setFileEntry(file);

    file.getMetadata();
    fileControl.setReturnValue(metadata, MockControl.ONE_OR_MORE);
    metadata.getWhatObject();
    metadataControl.setReturnValue("IMAGE");
    metadata.getWhatDate();
    metadataControl.setReturnValue(d);
    metadata.getWhatTime();
    metadataControl.setReturnValue(t);
    ruleUtil.createNominalTime(d, t, 10);
    ruleUtilControl.setReturnValue(dt);
    ruleUtil.isTriggered(25, dt);
    ruleUtilControl.setReturnValue(false);
    classUnderTest.setRuleId(25);
    classUnderTest.setInterval(10);
    
    replay();
    fileControl.replay();
    metadataControl.replay();
    
    CompositeTimerData result = classUnderTest.createTimerData(dataMessage);
    
    verify();
    fileControl.verify();
    metadataControl.verify();
    assertEquals(null, result);
  }

  public void testCreateTimerData_notBltDataMessage() throws Exception {
    IBltMessage dataMessage = new IBltMessage() {};
    
    classUnderTest.setRuleId(25);
    classUnderTest.setInterval(10);
    
    replay();
    
    CompositeTimerData result = classUnderTest.createTimerData(dataMessage);
    
    verify();
    assertEquals(null, result);
  }

  public void testCreateTimerData_nullMessage() throws Exception {
    classUnderTest.setRuleId(25);
    classUnderTest.setInterval(10);
    
    replay();
    
    CompositeTimerData result = classUnderTest.createTimerData(null);
    
    verify();
    assertEquals(null, result);
  }

  
  protected CatalogEntry createCatalogEntry(String source) {
    MockControl entryControl = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry entry = (CatalogEntry)entryControl.getMock();
    entry.getSource();
    entryControl.setReturnValue(source, MockControl.ZERO_OR_MORE);
    entry.getUuid();
    entryControl.setReturnValue("uuid", MockControl.ZERO_OR_MORE);
    entryControl.replay();
    return entry;
  }
  
  public void testAreCriteriasMet() {
    List<String> sources = new ArrayList<String>();
    sources.add("searl");
    sources.add("sekkr");
    sources.add("seosu");
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("sekkr"));
    entries.add(createCatalogEntry("seosu"));
    entries.add(createCatalogEntry("selul"));
    entries.add(createCatalogEntry("searl"));
    List<String> entrySources = new ArrayList<String>();
    entrySources.add("sekkr");
    entrySources.add("seosu");
    entrySources.add("selul");
    entrySources.add("searl");
    
    classUnderTest.setSources(sources);
    ruleUtil.getSourcesFromEntries(entries);
    ruleUtilControl.setReturnValue(entrySources);
    
    replay();
    boolean result = classUnderTest.areCriteriasMet(entries);
    verify();
    
    assertEquals(true, result);
  }

  public void testAreCriteriasMet_false() {
    List<String> sources = new ArrayList<String>();
    sources.add("searl");
    sources.add("sekkr");
    sources.add("seosu");
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("sekkr"));
    entries.add(createCatalogEntry("selul"));
    entries.add(createCatalogEntry("searl"));
    List<String> entrySources = new ArrayList<String>();
    entrySources.add("sekkr");
    entrySources.add("selul");
    entrySources.add("searl");
    
    ruleUtil.getSourcesFromEntries(entries);
    ruleUtilControl.setReturnValue(entrySources);
    
    classUnderTest.setSources(sources);
    replay();
    boolean result = classUnderTest.areCriteriasMet(entries);
    verify();
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
    List<String> detectors = new ArrayList<String>();
    detectors.add("ropo");
    detectors.add("sigge");
    detectors.add("nisse");
    
    // actual entries don't matter, just make the list of different size to distinguish
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("sekkr"));

    List<CatalogEntry> entriesByTime = new ArrayList<CatalogEntry>();
    entriesByTime.add(createCatalogEntry("sekkr"));
    entriesByTime.add(createCatalogEntry("selul"));

    List<CatalogEntry> entriesBySources = new ArrayList<CatalogEntry>();
    entriesBySources.add(createCatalogEntry("sekkr"));
    entriesBySources.add(createCatalogEntry("selul"));
    entriesBySources.add(createCatalogEntry("searl"));
    
    List<String> fileEntries = new ArrayList<String>();
    fileEntries.add("/tmp/sekkr.h5");
    fileEntries.add("/tmp/selul.h5");
    fileEntries.add("/tmp/searl.h5");
    
    classUnderTest.setArea("blt_composite");
    
    List<String> sources = new ArrayList<String>();
    sources.add("sekkr");
    sources.add("selul");
    sources.add("searl");
    classUnderTest.setSources(sources);
    
    ruleUtil.getEntriesByClosestTime(nominalTime, entries);
    ruleUtilControl.setReturnValue(entriesByTime);
    ruleUtil.getEntriesBySources(sources, entriesByTime);
    ruleUtilControl.setReturnValue(entriesBySources);
    ruleUtil.getFilesFromEntries(entriesBySources);
    ruleUtilControl.setReturnValue(fileEntries);

    classUnderTest.setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    classUnderTest.setDetectors(detectors);
    
    classUnderTest.setMethod(CompositingRule.PPI);
    classUnderTest.setProdpar("0.5");
    
    replay();
    IBltMessage result = classUnderTest.createMessage(nominalTime, entries);
    verify();
    
    assertTrue(result instanceof BltGenerateMessage);
    BltGenerateMessage msg = (BltGenerateMessage)result;
    assertEquals("eu.baltrad.beast.GenerateComposite", msg.getAlgorithm());
    String[] files = msg.getFiles();
    assertEquals(3, files.length);
    assertTrue(arrayContains(files, "/tmp/sekkr.h5"));
    assertTrue(arrayContains(files, "/tmp/selul.h5"));
    assertTrue(arrayContains(files, "/tmp/searl.h5"));
    String[] arguments = msg.getArguments();
    assertEquals(7, arguments.length);
    assertEquals("--area=blt_composite", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--selection=HEIGHT_ABOVE_SEALEVEL", arguments[3]);
    assertEquals("--anomaly-qc=ropo,sigge,nisse", arguments[4]);
    assertEquals("--method=ppi", arguments[5]);
    assertEquals("--prodpar=0.5", arguments[6]);
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

    ruleUtil.createNextNominalTime(startDT, 10);
    ruleUtilControl.setReturnValue(stopDT);
    
    replay();
    classUnderTest.setInterval(10);
    TimeIntervalFilter result = classUnderTest.createFilter(startDT);
    
    verify();
    assertNotNull(result);
    assertSame(startDT, result.getStartDateTime());
    assertSame(stopDT, result.getStopDateTime());
    assertEquals("PVOL", result.getObject());
  }
  
  protected boolean compareDT(DateTime o1, DateTime o2) {
    return o1.equals(o2);
  }

  public void testCreateCompositeScanMessage() throws Exception {
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();
    BltGenerateMessage createdMessage = new BltGenerateMessage();
    
    DateTime pdt = new DateTime();
    DateTime dt = new DateTime();
    DateTime ndt = new DateTime();
    
    Map<String, Double> prevAngles = new HashMap<String, Double>();
    Map<String, Double> currAngles = new HashMap<String, Double>();
    List<String> sources = new ArrayList<String>();
    List<CatalogEntry> currEntries = new ArrayList<CatalogEntry>();

    CatalogEntry e1 = createCatalogEntry("sekkr", pdt, 0.1);
    currEntries.add(e1);
    CatalogEntry e2 = createCatalogEntry("searl", pdt, 0.5);
    currEntries.add(e2);

    sources.add("sekkr");
    sources.add("searl");
    
    prevAngles.put("sekkr", 0.1);
    prevAngles.put("searl", 0.5);

    currAngles.put("sekkr", 0.1);
    currAngles.put("searl", 0.5);
    
    CompositeTimerData data = new CompositeTimerData(1, dt);
    data.setPreviousAngles(prevAngles);
    ruleUtil.createNextNominalTime(dt, 10);
    ruleUtilControl.setReturnValue(ndt);
    ruleUtil.fetchLowestSourceElevationAngle(dt, ndt, sources);
    ruleUtilControl.setReturnValue(currAngles);

    methods.createMessage(dt, currEntries);
    methodsControl.setReturnValue(createdMessage);
    methods.fetchScanEntries(dt);
    methodsControl.setReturnValue(currEntries);

    replay();
    methodsControl.replay();
    
    classUnderTest = new CompositingRule() {
      protected IBltMessage createMessage(DateTime nominalDT, List<CatalogEntry> entries) {
        return methods.createMessage(nominalDT, entries);
      }

      protected List<CatalogEntry> fetchScanEntries(DateTime nominalTime) {
        return methods.fetchScanEntries(nominalTime);
      }
    };
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setSources(sources);
    classUnderTest.setInterval(10);
    
    BltGenerateMessage msg = (BltGenerateMessage)classUnderTest.createCompositeScanMessage(data);
    
    verify();
    methodsControl.verify();
    assertSame(createdMessage, msg);
  }

  public void testCreateCompositeScanMessage_missingSources() throws Exception {
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();
    
    DateTime dt = new DateTime();
    DateTime ndt = new DateTime();
    Map<String, Double> prevAngles = new HashMap<String, Double>();
    Map<String, Double> currAngles = new HashMap<String, Double>();
    List<String> sources = new ArrayList<String>();
    sources.add("sekkr");
    sources.add("searl");
    
    prevAngles.put("sekkr", 0.1);
    prevAngles.put("searl", 0.5);

    currAngles.put("sekkr", 0.1);
    
    CompositeTimerData data = new CompositeTimerData(1, dt);
    data.setPreviousAngles(prevAngles);
    ruleUtil.createNextNominalTime(dt, 10);
    ruleUtilControl.setReturnValue(ndt);
    ruleUtil.fetchLowestSourceElevationAngle(dt, ndt, sources);
    ruleUtilControl.setReturnValue(currAngles);
    
    replay();
    methodsControl.replay();
    
    classUnderTest = new CompositingRule() {
      protected IBltMessage createMessage(DateTime nominalDT, List<CatalogEntry> entries) {
        return methods.createMessage(nominalDT, entries);
      }
    };
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setSources(sources);
    classUnderTest.setInterval(10);
    
    BltGenerateMessage msg = (BltGenerateMessage)classUnderTest.createCompositeScanMessage(data);
    
    verify();
    methodsControl.verify();
    assertNull(msg);
  }

  public void testCreateCompositeScanMessage_toHighElevation() throws Exception {
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();
    
    DateTime dt = new DateTime();
    DateTime ndt = new DateTime();
    Map<String, Double> prevAngles = new HashMap<String, Double>();
    Map<String, Double> currAngles = new HashMap<String, Double>();
    List<String> sources = new ArrayList<String>();
    sources.add("sekkr");
    
    prevAngles.put("sekkr", 0.1);

    currAngles.put("sekkr", 0.2);
    
    CompositeTimerData data = new CompositeTimerData(1, dt);
    data.setPreviousAngles(prevAngles);
    ruleUtil.createNextNominalTime(dt, 10);
    ruleUtilControl.setReturnValue(ndt);
    ruleUtil.fetchLowestSourceElevationAngle(dt, ndt, sources);
    ruleUtilControl.setReturnValue(currAngles);
    
    replay();
    methodsControl.replay();
    
    classUnderTest = new CompositingRule() {
      protected IBltMessage createMessage(DateTime nominalDT, List<CatalogEntry> entries) {
        return methods.createMessage(nominalDT, entries);
      }
    };
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setSources(sources);
    classUnderTest.setInterval(10);
    
    BltGenerateMessage msg = (BltGenerateMessage)classUnderTest.createCompositeScanMessage(data);
    
    verify();
    methodsControl.verify();
    assertNull(msg);
  }

  public void testFetchScanEntries() throws Exception {
    MockControl methodsControl = MockControl.createControl(ICompositingMethods.class);
    final ICompositingMethods methods = (ICompositingMethods)methodsControl.getMock();
    MockControl filterControl = MockClassControl.createControl(LowestAngleFilter.class);
    LowestAngleFilter filter = (LowestAngleFilter)filterControl.getMock();
    DateTime dt = new DateTime(2010,1,1,10,0,0);
    CatalogEntry e1 = new CatalogEntry();
    CatalogEntry e2 = new CatalogEntry();
    List<CatalogEntry> entries1 = new ArrayList<CatalogEntry>();
    List<CatalogEntry> entries2 = new ArrayList<CatalogEntry>();
    List<String> sources = new ArrayList<String>();

    entries1.add(e1);
    entries2.add(e2);
    sources.add("seang");
    sources.add("sekkr");

    methods.createScanFilter(dt);
    methodsControl.setReturnValue(filter);
    filter.setSource("seang");
    catalog.fetch(filter);
    catalogControl.setReturnValue(entries1);
    filter.setSource("sekkr");
    catalog.fetch(filter);
    catalogControl.setReturnValue(entries2);

    classUnderTest = new CompositingRule() {
      protected LowestAngleFilter createScanFilter(DateTime nt) {
        return methods.createScanFilter(nt);
      }
    };
    classUnderTest.setCatalog(catalog);
    classUnderTest.setSources(sources);

    replay();
    methodsControl.replay();
    filterControl.replay();

    List<CatalogEntry> result = classUnderTest.fetchScanEntries(dt);

    verify();
    methodsControl.verify();
    filterControl.verify();
    
    assertTrue(result.contains(e1));
    assertTrue(result.contains(e2));
  }

  public void testCreateScanFilter() throws Exception {
    Date startDate = new Date(2010,1,1);
    Time startTime = new Time(1,2,0);
    Date stopDate = new Date(2010,1,1);
    Time stopTime = new Time(1,3,0);
    final DateTime startDT = new DateTime(startDate, startTime);
    final DateTime stopDT = new DateTime(stopDate, stopTime);

    ruleUtil.createNextNominalTime(startDT, 10);
    ruleUtilControl.setReturnValue(stopDT);
    
    replay();
    classUnderTest.setInterval(10);
    LowestAngleFilter result = classUnderTest.createScanFilter(startDT);
    
    verify();
    assertNotNull(result);
    assertSame(startDT, result.getStart());
    assertSame(stopDT, result.getStop());
  }
  
  public void testSetGetSelectionMethod() throws Exception {
    assertEquals(CompositingRule.SelectionMethod_NEAREST_RADAR, classUnderTest.getSelectionMethod());
    classUnderTest.setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    assertEquals(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL, classUnderTest.getSelectionMethod());
  }
  
  public void testSetGetSelectionMethod_exception() throws Exception {
    try {
      classUnderTest.setSelectionMethod(99);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
    assertEquals(CompositingRule.SelectionMethod_NEAREST_RADAR, classUnderTest.getSelectionMethod());
  }

  public void testSetGetDetectors() throws Exception {
    List<String> detectors = new ArrayList<String>();
    assertEquals(0, classUnderTest.getDetectors().size());
    classUnderTest.setDetectors(detectors);
    assertSame(detectors, classUnderTest.getDetectors());
  }

  public void testSetGetDetectors_null() throws Exception {
    List<String> detectors = new ArrayList<String>();
    classUnderTest.setDetectors(detectors);
    classUnderTest.setDetectors(null);
    assertNotNull(classUnderTest.getDetectors());
    assertEquals(0, classUnderTest.getDetectors().size());
    assertNotSame(detectors, classUnderTest.getDetectors());
  }

  private CatalogEntry createCatalogEntry(String src, DateTime dt, double elangle) {
    MockControl entryControl = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry entry = (CatalogEntry)entryControl.getMock();
    entry.getSource();
    entryControl.setReturnValue(src, MockControl.ZERO_OR_MORE);
    entry.getDateTime();
    entryControl.setReturnValue(dt, MockControl.ZERO_OR_MORE);
    entry.getAttribute("/dataset1/where/elangle");
    entryControl.setReturnValue(new Double(elangle), MockControl.ZERO_OR_MORE);
    entry.getUuid();
    entryControl.setReturnValue("uuid", MockControl.ZERO_OR_MORE);
    entryControl.replay();
    return entry;
  }
}
