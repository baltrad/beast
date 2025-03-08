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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanInitializationException;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.bdb.util.Time;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.filters.CompositingRuleFilter;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.router.IMultiRoutedMessage;
import eu.baltrad.beast.rules.timer.ITimeoutRule;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.timer.TimeoutTask;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 */
public class CompositingRuleTest extends EasyMockSupport {
  private Catalog catalog = null;
  private IRuleUtilities ruleUtil = null;
  private TimeoutManager timeoutManager = null;
  
  private CompositingRule classUnderTest = null;
  
  private static interface ICompositingMethods {
    public CompositeTimerData createTimerData(IBltMessage message);
    public Map<String, CatalogEntry> fetchEntriesMap(CompositingRuleFilter filter);
    public CompositingRuleFilter createFilter(DateTime nominalTime);
    public IBltMessage createMessage(DateTime nominalTime, Map<String, CatalogEntry> entries);
    public IBltMessage createComposite(IBltMessage message, CompositingRuleFilter ruleFilter);
    public DateTime getNominalTimeFromFile(FileEntry file);
    public DateTime getDateTimeFromFile(FileEntry file);
    public boolean dateTimeExceedsMaxAgeLimit(DateTime dateTime);
  };

  @Before
  public void setUp() throws Exception {
    catalog = createMock(Catalog.class);
    timeoutManager = createMock(TimeoutManager.class);
    ruleUtil = createMock(IRuleUtilities.class);
    classUnderTest = new CompositingRule();
    classUnderTest.setRuleId(10);
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setRuleUtilities(ruleUtil);
  }

  @After
  public void tearDown() throws Exception {
    catalog = null;
    classUnderTest = null;
  }
  
  @Test
  public void testSetGetRuleId() throws Exception {
    classUnderTest.setRuleId(103);
    assertEquals(103, classUnderTest.getRuleId());
  }
  
  @Test
  public void testSetInterval() throws Exception {
    int[] valid = {1,2,3,4,5,6,10,12,15,20,30,60};
    for (int v : valid) {
      classUnderTest.setInterval(v);
    }
  }

  @Test
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
  
  @Test
  public void testGetType() {
    assertEquals("blt_composite", classUnderTest.getType());
  }
  
  @Test
  public void testApplyGRA() {
    assertEquals(false, classUnderTest.isApplyGRA());
    classUnderTest.setApplyGRA(true);
    assertEquals(true, classUnderTest.isApplyGRA());
  }
  
  @Test
  public void testZR_A() {
    assertEquals(200.0, classUnderTest.getZR_A(), 4);
    classUnderTest.setZR_A(10.0);
    assertEquals(10.0, classUnderTest.getZR_A(), 4);
  }
  
  @Test
  public void testZR_b() {
    assertEquals(1.6, classUnderTest.getZR_b(), 4);
    classUnderTest.setZR_b(10.0);
    assertEquals(10.0, classUnderTest.getZR_b(), 4);
  }
  
  @Test
  public void testExceedsMaxAgeLimit_disabled() {
    int disabledIndicator = -1;
    classUnderTest.setMaxAgeLimit(disabledIndicator);
    
    Calendar fileDate = GregorianCalendar.getInstance();
    fileDate.add(Calendar.MINUTE, -1000);
    
    DateTime fileDateTime = classUnderTest.getRuleUtilities().createDateTime(fileDate.getTime());
    
    boolean dateTimeExceedsLimit = classUnderTest.dateTimeExceedsMaxAgeLimit(fileDateTime);
    
    assertEquals(false, dateTimeExceedsLimit);
  }
  
  @Test
  public void testExceedsMaxAgeLimit_exceeding() {
    int maxAgeLimitMinutes = 60;
    classUnderTest.setMaxAgeLimit(maxAgeLimitMinutes);
    
    Calendar fileDate = GregorianCalendar.getInstance();
    fileDate.add(Calendar.MINUTE, -(maxAgeLimitMinutes + 1)); // exceeding limit with 1 minute
    
    DateTime fileDateTime = new DateTime(2018, 2, 26, 5, 14, 30);
    DateTime nowDateTime = new DateTime(2018, 2, 26, 6, 15, 00); // 60 minutes and 30 seconds after file time
    
    expect(ruleUtil.nowDT()).andReturn(nowDateTime);
    
    replayAll();
    
    boolean dateTimeExceedsLimit = classUnderTest.dateTimeExceedsMaxAgeLimit(fileDateTime);
    
    assertEquals(true, dateTimeExceedsLimit);
  }
  
  @Test
  public void testExceedsMaxAgeLimit_notExceeding() {
    int maxAgeLimitMinutes = 60;
    classUnderTest.setMaxAgeLimit(maxAgeLimitMinutes);
    
    Calendar fileDate = GregorianCalendar.getInstance();
    fileDate.add(Calendar.MINUTE, -(maxAgeLimitMinutes + 1)); // exceeding limit with 1 minute
    
    DateTime fileDateTime = new DateTime(2018, 2, 26, 5, 14, 30);
    DateTime nowDateTime = new DateTime(2018, 2, 26, 6, 14, 00); // 59 minutes and 30 seconds after file time
    
    expect(ruleUtil.nowDT()).andReturn(nowDateTime);
    
    replayAll();
    
    boolean dateTimeExceedsLimit = classUnderTest.dateTimeExceedsMaxAgeLimit(fileDateTime);
    
    assertEquals(false, dateTimeExceedsLimit);
  }

  @Test
  public void testTimeout() throws Exception {
    final ICompositingMethods methods = createMock(ICompositingMethods.class);
    DateTime dt = new DateTime(2010, 4, 15, 10, 15, 0);
    CompositeTimerData ctd = new CompositeTimerData(15, dt);
    IBltMessage resultMessage = new IBltMessage() {
    };
    Map<String,CatalogEntry> entries = new HashMap<String, CatalogEntry>();
    List<String> recipients = new ArrayList<String>();
    
    CompositingRuleFilter filter = createMock(CompositingRuleFilter.class);
    
    expect(methods.createFilter(dt)).andReturn(filter);
    expect(methods.fetchEntriesMap(filter)).andReturn(entries);
    expect(ruleUtil.isTriggered(25, dt)).andReturn(false);
    expect(methods.createMessage(dt, entries)).andReturn(resultMessage);
    ruleUtil.trigger(25, dt);
    
    // continuing with mocking after classUnderTest declaration
    
    classUnderTest = new CompositingRule() {
      protected Map<String, CatalogEntry> fetchEntriesMap(CompositingRuleFilter filter) {
        return methods.fetchEntriesMap(filter);
      }
      protected IBltMessage createMessage(DateTime nominalTime, Map<String, CatalogEntry> entries) {
        return methods.createMessage(nominalTime, entries);
      }
      protected CompositingRuleFilter createFilter(DateTime nominalTime) {
        return methods.createFilter(nominalTime);
      }
    };
    classUnderTest.setRecipients(recipients);
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setRuleId(25);
    
    replayAll();
    
    IBltMessage result = classUnderTest.timeout(15, ITimeoutRule.TIMEOUT, ctd);
    
    verifyAll();
    assertSame(resultMessage, ((IMultiRoutedMessage)result).getMessage());
    assertSame(recipients, ((IMultiRoutedMessage)result).getDestinations());
  }

  @Test
  public void testTimeout_alreadyTriggered() throws Exception {
    final ICompositingMethods methods = createMock(ICompositingMethods.class);
    DateTime dt = new DateTime(2010, 4, 15, 10, 15, 0);
    CompositeTimerData ctd = new CompositeTimerData(15, dt);
    Map<String,CatalogEntry> entries = new HashMap<String,CatalogEntry>();
    List<String> recipients = new ArrayList<String>();
    
    CompositingRuleFilter filter = createMock(CompositingRuleFilter.class);
    
    expect(methods.createFilter(dt)).andReturn(filter);
    
    expect(methods.fetchEntriesMap(filter)).andReturn(entries);
    expect(ruleUtil.isTriggered(25, dt)).andReturn(true);
    
    // continuing with mocking after classUnderTest declaration
    
    classUnderTest = new CompositingRule() {
      protected Map<String,CatalogEntry> fetchEntriesMap(CompositingRuleFilter filter) {
        return methods.fetchEntriesMap(filter);
      }
      protected IBltMessage createMessage(DateTime nominalTime, Map<String,CatalogEntry> entries) {
        return methods.createMessage(nominalTime, entries);
      }
      protected CompositingRuleFilter createFilter(DateTime nominalTime) {
        return methods.createFilter(nominalTime);
      }
    };
    classUnderTest.setRecipients(recipients);
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setRuleId(25);

    replayAll();
    
    IBltMessage result = classUnderTest.timeout(15, ITimeoutRule.TIMEOUT, ctd);
    
    verifyAll();
    assertNull(result);
  }

  @Test
  public void testAfterPropertiesSet() throws Exception {
    CompositingRule classUnderTest = new CompositingRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setTimeoutManager(timeoutManager);
    
    classUnderTest.afterPropertiesSet();
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
  public void testCreateTimerData() throws Exception {
    Date date = new Date(2010, 1, 1);
    Time time = new Time(10, 0, 0);
    DateTime nominalTime = new DateTime(date, time);
    
    FileEntry file = createMock(FileEntry.class);
    Metadata metadata = createMock(Metadata.class);

    BltDataMessage dataMessage = new BltDataMessage();
    dataMessage.setFileEntry(file);

    expect(file.getMetadata()).andReturn(metadata).times(1);
    expect(metadata.getWhatDate()).andReturn(date);
    expect(metadata.getWhatTime()).andReturn(time);
    expect(ruleUtil.createNominalTime(date, time, 10)).andReturn(nominalTime);
    
    expect(ruleUtil.isTriggered(25, nominalTime)).andReturn(false);
    
    classUnderTest.setRuleId(25);
    classUnderTest.setInterval(10);
    classUnderTest.setQuantity("VRAD");
    replayAll();
    
    CompositeTimerData result = classUnderTest.createTimerData(dataMessage);
    
    verifyAll();
    assertSame(nominalTime, result.getDateTime());
    assertEquals(25, result.getRuleId());
  }
  
  @Test
  public void testCreateTimerData_alreadyTriggered() throws Exception {
    Date date = new Date(2010, 1, 1);
    Time time = new Time(10, 0, 0);
    DateTime nominalTime = new DateTime(date, time);

    FileEntry file = createMock(FileEntry.class);
    Metadata metadata = createMock(Metadata.class);

    BltDataMessage dataMessage = new BltDataMessage();
    dataMessage.setFileEntry(file);
    
    expect(file.getMetadata()).andReturn(metadata).times(1);
    expect(metadata.getWhatDate()).andReturn(date);
    expect(metadata.getWhatTime()).andReturn(time);
    expect(ruleUtil.createNominalTime(date, time, 10)).andReturn(nominalTime);
    expect(ruleUtil.isTriggered(25, nominalTime)).andReturn(true);
    
    classUnderTest.setRuleId(25);
    classUnderTest.setInterval(10);
    classUnderTest.setQuantity("DBZH");
    
    replayAll();
    
    CompositeTimerData result = classUnderTest.createTimerData(dataMessage);
    
    verifyAll();
    assertNull(result);
  }

  @Test
  public void testCreateTimerData_notBltDataMessage() throws Exception {
    IBltMessage dataMessage = new IBltMessage() {};
    
    classUnderTest.setRuleId(25);
    classUnderTest.setInterval(10);
    
    replayAll();
    
    CompositeTimerData result = classUnderTest.createTimerData(dataMessage);
    
    verifyAll();
    assertEquals(null, result);
  }

  @Test
  public void testCreateTimerData_nullMessage() throws Exception {
    classUnderTest.setRuleId(25);
    classUnderTest.setInterval(10);
    
    replayAll();
    
    CompositeTimerData result = classUnderTest.createTimerData(null);
    
    verifyAll();
    assertEquals(null, result);
  }

  protected CatalogEntry createCatalogEntry(String source) {
    CatalogEntry entry = createMock(CatalogEntry.class);
    return entry;
  }
  
  protected CatalogEntry createExpectCatalogEntry(String source) {
    CatalogEntry entry = createMock(CatalogEntry.class);
    expect(entry.getUuid()).andReturn("uuid-"+source);
    return entry;
  }
  
  protected boolean arrayContains(String[] arr, String value) {
    for (String x : arr) {
      if (x.equals(value)) {
        return true;
      }
    }
    return false;
  }
  
  @Test
  public void testCreateMessage() {
    Date date = new Date(2010, 2, 1);
    Time time = new Time(1, 0, 0);
    DateTime nominalTime = new DateTime(date, time);
    List<String> detectors = new ArrayList<String>();
    detectors.add("ropo");
    detectors.add("sigge");
    detectors.add("nisse");
    
    // actual entries don't matter, just make the list of different size to distinguish
    Map<String, CatalogEntry> entries = new HashMap<String, CatalogEntry>();
    List<CatalogEntry> entriesBySources = new ArrayList<CatalogEntry>(entries.values());
    
    List<String> fileEntries = new ArrayList<String>();
    fileEntries.add("uuid-1");
    fileEntries.add("uuid-2");
    fileEntries.add("uuid-3");

    List<String> usedSources = new ArrayList<String>();
    
    classUnderTest.setArea("blt_composite");
    
    List<String> sources = new ArrayList<String>();
    sources.add("sekkr");
    sources.add("selul");
    sources.add("searl");
    classUnderTest.setSources(sources);
    
    expect(ruleUtil.getUuidStringsFromEntries(entriesBySources)).andReturn(fileEntries);
    ruleUtil.reportRadarSourceUsage(sources, usedSources);
    
    classUnderTest.setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    classUnderTest.setDetectors(detectors);
    
    classUnderTest.setMethod(CompositingRule.PPI);
    classUnderTest.setProdpar("0.5");
    classUnderTest.setOptions("factory:any");
    replayAll();
    
    IBltMessage result = classUnderTest.createMessage(nominalTime, entries);
    
    verifyAll();
    
    assertTrue(result instanceof BltGenerateMessage);
    BltGenerateMessage msg = (BltGenerateMessage)result;
    assertEquals("eu.baltrad.beast.GenerateComposite", msg.getAlgorithm());
    String[] files = msg.getFiles();
    assertEquals(3, files.length);
    assertTrue(arrayContains(files, "uuid-1"));
    assertTrue(arrayContains(files, "uuid-2"));
    assertTrue(arrayContains(files, "uuid-3"));
    String[] arguments = msg.getArguments();
    assertEquals(12, arguments.length);
    assertEquals("--area=blt_composite", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--selection=HEIGHT_ABOVE_SEALEVEL", arguments[3]);
    assertEquals("--anomaly-qc=ropo,sigge,nisse", arguments[4]);
    assertEquals("--qc-mode=ANALYZE_AND_APPLY", arguments[5]);
    assertEquals("--method=ppi", arguments[6]);
    assertEquals("--prodpar=0.5", arguments[7]);
    assertEquals("--quantity=DBZH", arguments[8]);
    assertEquals("--options=factory:any", arguments[9]);
    assertEquals("--algorithm_id=10", arguments[10]);
    assertEquals("--merge=true", arguments[11]);
  }

  @Test
  public void testCreateMessage_applyGRA() {
    Date date = new Date(2010, 2, 1);
    Time time = new Time(1, 0, 0);
    DateTime nominalTime = new DateTime(date, time);
    List<String> detectors = new ArrayList<String>();
    detectors.add("ropo");
    detectors.add("sigge");
    detectors.add("nisse");
    
    // actual entries don't matter, just make the list of different size to distinguish
    Map<String, CatalogEntry> entries = new HashMap<String, CatalogEntry>();
    List<CatalogEntry> entriesBySources = new ArrayList<CatalogEntry>(entries.values());
    
    List<String> fileEntries = new ArrayList<String>();
    fileEntries.add("uuid-1");
    fileEntries.add("uuid-2");
    fileEntries.add("uuid-3");

    List<String> usedSources = new ArrayList<String>();
    
    classUnderTest.setArea("blt_composite");
    
    List<String> sources = new ArrayList<String>();
    sources.add("sekkr");
    sources.add("selul");
    sources.add("searl");
    classUnderTest.setSources(sources);
    
    expect(ruleUtil.getUuidStringsFromEntries(entriesBySources)).andReturn(fileEntries);
    ruleUtil.reportRadarSourceUsage(sources, usedSources);
    
    classUnderTest.setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    classUnderTest.setDetectors(detectors);
    classUnderTest.setQualityControlMode(CompositingRule.QualityControlMode_ANALYZE);
    classUnderTest.setMethod(CompositingRule.PPI);
    classUnderTest.setProdpar("0.5");
    classUnderTest.setApplyGRA(true);
    classUnderTest.setZR_A(100.0);
    classUnderTest.setZR_b(1.4);
    
    replayAll();
    
    IBltMessage result = classUnderTest.createMessage(nominalTime, entries);
    
    verifyAll();
    
    assertTrue(result instanceof BltGenerateMessage);
    BltGenerateMessage msg = (BltGenerateMessage)result;
    assertEquals("eu.baltrad.beast.GenerateComposite", msg.getAlgorithm());
    String[] files = msg.getFiles();
    assertEquals(3, files.length);
    assertTrue(arrayContains(files, "uuid-1"));
    assertTrue(arrayContains(files, "uuid-2"));
    assertTrue(arrayContains(files, "uuid-3"));
    String[] arguments = msg.getArguments();
    assertEquals(14, arguments.length);
    assertEquals("--area=blt_composite", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--selection=HEIGHT_ABOVE_SEALEVEL", arguments[3]);
    assertEquals("--anomaly-qc=ropo,sigge,nisse", arguments[4]);
    assertEquals("--qc-mode=ANALYZE", arguments[5]);
    assertEquals("--method=ppi", arguments[6]);
    assertEquals("--prodpar=0.5", arguments[7]);
    assertEquals("--applygra=true", arguments[8]);
    assertEquals("--zrA=100.0", arguments[9]);
    assertEquals("--zrb=1.4", arguments[10]);
    assertEquals("--quantity=DBZH", arguments[11]);
    assertEquals("--algorithm_id=10", arguments[12]);
    assertEquals("--merge=true", arguments[13]);
  }

  @Test
  public void testCreateMessage_ignoreMalfunc() {
    Date date = new Date(2010, 2, 1);
    Time time = new Time(1, 0, 0);
    DateTime nominalTime = new DateTime(date, time);
    List<String> detectors = new ArrayList<String>();
    detectors.add("ropo");
    detectors.add("sigge");
    detectors.add("nisse");
    
    // actual entries don't matter, just make the list of different size to distinguish
    Map<String, CatalogEntry> entries = new HashMap<String, CatalogEntry>();
    List<CatalogEntry> entriesBySources = new ArrayList<CatalogEntry>(entries.values());
    
    List<String> fileEntries = new ArrayList<String>();
    fileEntries.add("uuid-1");
    fileEntries.add("uuid-2");
    fileEntries.add("uuid-3");

    List<String> usedSources = new ArrayList<String>();
    
    classUnderTest.setArea("blt_composite");
    
    List<String> sources = new ArrayList<String>();
    sources.add("sekkr");
    sources.add("selul");
    sources.add("searl");
    classUnderTest.setSources(sources);
    
    expect(ruleUtil.getUuidStringsFromEntries(entriesBySources)).andReturn(fileEntries);
    ruleUtil.reportRadarSourceUsage(sources, usedSources);
    
    classUnderTest.setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    classUnderTest.setDetectors(detectors);
    
    classUnderTest.setMethod(CompositingRule.PPI);
    classUnderTest.setProdpar("0.5");
    classUnderTest.setApplyGRA(true);
    classUnderTest.setIgnoreMalfunc(true);
    classUnderTest.setZR_A(100.0);
    classUnderTest.setZR_b(1.4);
    
    replayAll();
    
    IBltMessage result = classUnderTest.createMessage(nominalTime, entries);
    
    verifyAll();
    
    assertTrue(result instanceof BltGenerateMessage);
    BltGenerateMessage msg = (BltGenerateMessage)result;
    assertEquals("eu.baltrad.beast.GenerateComposite", msg.getAlgorithm());
    String[] files = msg.getFiles();
    assertEquals(3, files.length);
    assertTrue(arrayContains(files, "uuid-1"));
    assertTrue(arrayContains(files, "uuid-2"));
    assertTrue(arrayContains(files, "uuid-3"));
    String[] arguments = msg.getArguments();
    assertEquals(15, arguments.length);
    assertEquals("--area=blt_composite", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--selection=HEIGHT_ABOVE_SEALEVEL", arguments[3]);
    assertEquals("--anomaly-qc=ropo,sigge,nisse", arguments[4]);
    assertEquals("--qc-mode=ANALYZE_AND_APPLY", arguments[5]);
    assertEquals("--method=ppi", arguments[6]);
    assertEquals("--prodpar=0.5", arguments[7]);
    assertEquals("--applygra=true", arguments[8]);
    assertEquals("--zrA=100.0", arguments[9]);
    assertEquals("--zrb=1.4", arguments[10]);
    assertEquals("--ignore-malfunc=true", arguments[11]);
    assertEquals("--quantity=DBZH", arguments[12]);
    assertEquals("--algorithm_id=10", arguments[13]);
    assertEquals("--merge=true", arguments[14]);
    
  }

  @Test
  public void testCreateMessage_ctFilter() {
    Date date = new Date(2010, 2, 1);
    Time time = new Time(1, 0, 0);
    DateTime nominalTime = new DateTime(date, time);
    List<String> detectors = new ArrayList<String>();
    detectors.add("ropo");
    detectors.add("sigge");
    detectors.add("nisse");
    
    // actual entries don't matter, just make the list of different size to distinguish
    Map<String, CatalogEntry> entries = new HashMap<String, CatalogEntry>();
    List<CatalogEntry> entriesBySources = new ArrayList<CatalogEntry>(entries.values());
    
    List<String> fileEntries = new ArrayList<String>();
    fileEntries.add("uuid-1");
    fileEntries.add("uuid-2");
    fileEntries.add("uuid-3");

    List<String> usedSources = new ArrayList<String>();
    
    classUnderTest.setArea("blt_composite");
    
    List<String> sources = new ArrayList<String>();
    sources.add("sekkr");
    sources.add("selul");
    sources.add("searl");
    classUnderTest.setSources(sources);
    
    expect(ruleUtil.getUuidStringsFromEntries(entriesBySources)).andReturn(fileEntries);
    ruleUtil.reportRadarSourceUsage(sources, usedSources);
    
    classUnderTest.setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    classUnderTest.setDetectors(detectors);
    
    classUnderTest.setMethod(CompositingRule.PPI);
    classUnderTest.setProdpar("0.5");
    classUnderTest.setApplyGRA(true);
    classUnderTest.setIgnoreMalfunc(true);
    classUnderTest.setCtFilter(true);
    classUnderTest.setZR_A(100.0);
    classUnderTest.setZR_b(1.4);
    
    replayAll();
    
    IBltMessage result = classUnderTest.createMessage(nominalTime, entries);
    
    verifyAll();
    
    assertTrue(result instanceof BltGenerateMessage);
    BltGenerateMessage msg = (BltGenerateMessage)result;
    assertEquals("eu.baltrad.beast.GenerateComposite", msg.getAlgorithm());
    String[] files = msg.getFiles();
    assertEquals(3, files.length);
    assertTrue(arrayContains(files, "uuid-1"));
    assertTrue(arrayContains(files, "uuid-2"));
    assertTrue(arrayContains(files, "uuid-3"));
    String[] arguments = msg.getArguments();
    assertEquals(16, arguments.length);
    assertEquals("--area=blt_composite", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--selection=HEIGHT_ABOVE_SEALEVEL", arguments[3]);
    assertEquals("--anomaly-qc=ropo,sigge,nisse", arguments[4]);
    assertEquals("--qc-mode=ANALYZE_AND_APPLY", arguments[5]);
    assertEquals("--method=ppi", arguments[6]);
    assertEquals("--prodpar=0.5", arguments[7]);
    assertEquals("--applygra=true", arguments[8]);
    assertEquals("--zrA=100.0", arguments[9]);
    assertEquals("--zrb=1.4", arguments[10]);
    assertEquals("--ignore-malfunc=true", arguments[11]);
    assertEquals("--ctfilter=True", arguments[12]);
    assertEquals("--quantity=DBZH", arguments[13]);
    assertEquals("--algorithm_id=10", arguments[14]);
    assertEquals("--merge=true", arguments[15]);
  }
  
  @Test
  public void testCreateMessage_reprocessQuality() {
    Date date = new Date(2010, 2, 1);
    Time time = new Time(1, 0, 0);
    DateTime nominalTime = new DateTime(date, time);
    List<String> detectors = new ArrayList<String>();
    detectors.add("ropo");
    detectors.add("sigge");
    detectors.add("nisse");
    
    // actual entries don't matter, just make the list of different size to distinguish
    Map<String, CatalogEntry> entries = new HashMap<String, CatalogEntry>();
    List<CatalogEntry> entriesBySources = new ArrayList<CatalogEntry>(entries.values());
    
    List<String> fileEntries = new ArrayList<String>();
    fileEntries.add("uuid-1");
    fileEntries.add("uuid-2");
    fileEntries.add("uuid-3");

    List<String> usedSources = new ArrayList<String>();
    
    classUnderTest.setArea("blt_composite");
    
    List<String> sources = new ArrayList<String>();
    sources.add("sekkr");
    sources.add("selul");
    sources.add("searl");
    classUnderTest.setSources(sources);
    
    expect(ruleUtil.getUuidStringsFromEntries(entriesBySources)).andReturn(fileEntries);
    ruleUtil.reportRadarSourceUsage(sources, usedSources);
    
    classUnderTest.setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    classUnderTest.setDetectors(detectors);
    
    classUnderTest.setMethod(CompositingRule.PPI);
    classUnderTest.setProdpar("0.5");
    classUnderTest.setApplyGRA(true);
    classUnderTest.setIgnoreMalfunc(true);
    classUnderTest.setCtFilter(true);
    classUnderTest.setReprocessQuality(true);
    classUnderTest.setZR_A(100.0);
    classUnderTest.setZR_b(1.4);
    
    replayAll();
    
    IBltMessage result = classUnderTest.createMessage(nominalTime, entries);
    
    verifyAll();
    
    assertTrue(result instanceof BltGenerateMessage);
    BltGenerateMessage msg = (BltGenerateMessage)result;
    assertEquals("eu.baltrad.beast.GenerateComposite", msg.getAlgorithm());
    String[] files = msg.getFiles();
    assertEquals(3, files.length);
    assertTrue(arrayContains(files, "uuid-1"));
    assertTrue(arrayContains(files, "uuid-2"));
    assertTrue(arrayContains(files, "uuid-3"));
    String[] arguments = msg.getArguments();
    assertEquals(17, arguments.length);
    assertEquals("--area=blt_composite", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--selection=HEIGHT_ABOVE_SEALEVEL", arguments[3]);
    assertEquals("--anomaly-qc=ropo,sigge,nisse", arguments[4]);
    assertEquals("--qc-mode=ANALYZE_AND_APPLY", arguments[5]);
    assertEquals("--method=ppi", arguments[6]);
    assertEquals("--prodpar=0.5", arguments[7]);
    assertEquals("--applygra=true", arguments[8]);
    assertEquals("--zrA=100.0", arguments[9]);
    assertEquals("--zrb=1.4", arguments[10]);
    assertEquals("--ignore-malfunc=true", arguments[11]);
    assertEquals("--ctfilter=True", arguments[12]);
    assertEquals("--quantity=DBZH", arguments[13]);
    assertEquals("--reprocess_qfields=True", arguments[14]);
    assertEquals("--algorithm_id=10", arguments[15]);
    assertEquals("--merge=true", arguments[16]);
  }

  @Test
  public void testCreateMessage_qiTotal() {
    Date date = new Date(2010, 2, 1);
    Time time = new Time(1, 0, 0);
    DateTime nominalTime = new DateTime(date, time);
    List<String> detectors = new ArrayList<String>();
    detectors.add("ropo");
    detectors.add("sigge");
    detectors.add("nisse");
    
    // actual entries don't matter, just make the list of different size to distinguish
    Map<String, CatalogEntry> entries = new HashMap<String, CatalogEntry>();
    List<CatalogEntry> entriesBySources = new ArrayList<CatalogEntry>(entries.values());
    
    List<String> fileEntries = new ArrayList<String>();
    fileEntries.add("uuid-1");
    fileEntries.add("uuid-2");
    fileEntries.add("uuid-3");

    List<String> usedSources = new ArrayList<String>();
    
    classUnderTest.setArea("blt_composite");
    
    List<String> sources = new ArrayList<String>();
    sources.add("sekkr");
    sources.add("selul");
    sources.add("searl");
    classUnderTest.setSources(sources);
    
    expect(ruleUtil.getUuidStringsFromEntries(entriesBySources)).andReturn(fileEntries);
    ruleUtil.reportRadarSourceUsage(sources, usedSources);
    
    classUnderTest.setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    classUnderTest.setDetectors(detectors);
    
    classUnderTest.setMethod(CompositingRule.PPI);
    classUnderTest.setProdpar("0.5");
    classUnderTest.setApplyGRA(true);
    classUnderTest.setIgnoreMalfunc(true);
    classUnderTest.setCtFilter(true);
    classUnderTest.setZR_A(100.0);
    classUnderTest.setZR_b(1.4);
    classUnderTest.setQitotalField("se.baltrad.some.field");
    
    replayAll();
    
    IBltMessage result = classUnderTest.createMessage(nominalTime, entries);
    
    verifyAll();
    
    assertTrue(result instanceof BltGenerateMessage);
    BltGenerateMessage msg = (BltGenerateMessage)result;
    assertEquals("eu.baltrad.beast.GenerateComposite", msg.getAlgorithm());
    String[] files = msg.getFiles();
    assertEquals(3, files.length);
    assertTrue(arrayContains(files, "uuid-1"));
    assertTrue(arrayContains(files, "uuid-2"));
    assertTrue(arrayContains(files, "uuid-3"));
    String[] arguments = msg.getArguments();
    assertEquals(17, arguments.length);
    assertEquals("--area=blt_composite", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--selection=HEIGHT_ABOVE_SEALEVEL", arguments[3]);
    assertEquals("--anomaly-qc=ropo,sigge,nisse", arguments[4]);
    assertEquals("--qc-mode=ANALYZE_AND_APPLY", arguments[5]);
    assertEquals("--method=ppi", arguments[6]);
    assertEquals("--prodpar=0.5", arguments[7]);
    assertEquals("--applygra=true", arguments[8]);
    assertEquals("--zrA=100.0", arguments[9]);
    assertEquals("--zrb=1.4", arguments[10]);
    assertEquals("--ignore-malfunc=true", arguments[11]);
    assertEquals("--ctfilter=True", arguments[12]);
    assertEquals("--qitotal_field=se.baltrad.some.field", arguments[13]);
    assertEquals("--quantity=DBZH", arguments[14]);
    assertEquals("--algorithm_id=10", arguments[15]);
    assertEquals("--merge=true", arguments[16]);
  }  

  @Test
  public void testCreateMessage_qiTotal_whiteSpace() {
    Date date = new Date(2010, 2, 1);
    Time time = new Time(1, 0, 0);
    DateTime nominalTime = new DateTime(date, time);
    List<String> detectors = new ArrayList<String>();
    detectors.add("ropo");
    detectors.add("sigge");
    detectors.add("nisse");
    
    // actual entries don't matter, just make the list of different size to distinguish
    Map<String, CatalogEntry> entries = new HashMap<String, CatalogEntry>();
    List<CatalogEntry> entriesBySources = new ArrayList<CatalogEntry>(entries.values());
    
    List<String> fileEntries = new ArrayList<String>();
    fileEntries.add("uuid-1");
    fileEntries.add("uuid-2");
    fileEntries.add("uuid-3");

    List<String> usedSources = new ArrayList<String>();
    
    classUnderTest.setArea("blt_composite");
    
    List<String> sources = new ArrayList<String>();
    sources.add("sekkr");
    sources.add("selul");
    sources.add("searl");
    classUnderTest.setSources(sources);
    
    expect(ruleUtil.getUuidStringsFromEntries(entriesBySources)).andReturn(fileEntries);
    ruleUtil.reportRadarSourceUsage(sources, usedSources);
    
    classUnderTest.setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    classUnderTest.setDetectors(detectors);
    
    classUnderTest.setMethod(CompositingRule.PPI);
    classUnderTest.setProdpar("0.5");
    classUnderTest.setApplyGRA(true);
    classUnderTest.setIgnoreMalfunc(true);
    classUnderTest.setCtFilter(true);
    classUnderTest.setZR_A(100.0);
    classUnderTest.setZR_b(1.4);
    classUnderTest.setQitotalField("");
    
    replayAll();
    
    IBltMessage result = classUnderTest.createMessage(nominalTime, entries);
    
    verifyAll();
    
    assertTrue(result instanceof BltGenerateMessage);
    BltGenerateMessage msg = (BltGenerateMessage)result;
    assertEquals("eu.baltrad.beast.GenerateComposite", msg.getAlgorithm());
    String[] files = msg.getFiles();
    assertEquals(3, files.length);
    assertTrue(arrayContains(files, "uuid-1"));
    assertTrue(arrayContains(files, "uuid-2"));
    assertTrue(arrayContains(files, "uuid-3"));
    String[] arguments = msg.getArguments();
    assertEquals(16, arguments.length);
    assertEquals("--area=blt_composite", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--selection=HEIGHT_ABOVE_SEALEVEL", arguments[3]);
    assertEquals("--anomaly-qc=ropo,sigge,nisse", arguments[4]);
    assertEquals("--qc-mode=ANALYZE_AND_APPLY", arguments[5]);
    assertEquals("--method=ppi", arguments[6]);
    assertEquals("--prodpar=0.5", arguments[7]);
    assertEquals("--applygra=true", arguments[8]);
    assertEquals("--zrA=100.0", arguments[9]);
    assertEquals("--zrb=1.4", arguments[10]);
    assertEquals("--ignore-malfunc=true", arguments[11]);
    assertEquals("--ctfilter=True", arguments[12]);
    assertEquals("--quantity=DBZH", arguments[13]);
    assertEquals("--algorithm_id=10", arguments[14]);
    assertEquals("--merge=true", arguments[15]);
  }
  
  @Test
  public void testFetchEntries_noEntries() throws Exception {
    CompositingRuleFilter filter = createMock(CompositingRuleFilter.class);
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    
    expect(catalog.fetch(filter)).andReturn(entries);
    
    classUnderTest.setCatalog(catalog);
    
    replayAll();
    
    Map<String, CatalogEntry> result = classUnderTest.fetchEntriesMap(filter);
    
    verifyAll();
    
    assertTrue(result.size() == 0);
  }
  
  @Test
  public void testFetchEntries_oneEntryPerSource() throws Exception {
    List<String> sources = new ArrayList<String>(Arrays.asList("source1", "source2", "last_source"));
    CompositingRuleFilter filter = createMock(CompositingRuleFilter.class);
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    
    classUnderTest.setSources(sources);
    
    for (String source : sources) {
      CatalogEntry entry = createMock(CatalogEntry.class);
      FileEntry fileEntry = createMock(FileEntry.class);
      
      expect(entry.getSource()).andReturn(source);
      expect(entry.getFileEntry()).andReturn(fileEntry);
      expect(filter.fileMatches(fileEntry)).andReturn(true);
      
      entries.add(entry);
    }
    
    expect(catalog.fetch(filter)).andReturn(entries);
    
    classUnderTest.setCatalog(catalog);
    
    replayAll();
    
    Map<String, CatalogEntry> result = classUnderTest.fetchEntriesMap(filter);
    
    verifyAll();
    
    assertTrue(result.size() == sources.size());
  }
  
  @Test
  public void testFetchEntries_twoEntriesPerSource() throws Exception {
    List<String> sources = new ArrayList<String>(Arrays.asList("source1", "source2", "last_source"));
    CompositingRuleFilter filter = createMock(CompositingRuleFilter.class);
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    
    classUnderTest.setSources(sources);
    
    for (String source : sources) {
      CatalogEntry entry1 = createMock(CatalogEntry.class);
      FileEntry fileEntry = createMock(FileEntry.class);
      
      expect(entry1.getSource()).andReturn(source);
      expect(entry1.getFileEntry()).andReturn(fileEntry);
      expect(filter.fileMatches(fileEntry)).andReturn(true);
      
      entries.add(entry1);
      
      // add one more entry for the same source. this entry shall not be returned however, 
      // since it is expecetd that filter has ordered the results in correct order and this 
      // is located after the previous one for the same source
      CatalogEntry entry2 = createMock(CatalogEntry.class);
      expect(entry2.getSource()).andReturn(source);
      entries.add(entry2);
    }
    
    expect(catalog.fetch(filter)).andReturn(entries);
    
    classUnderTest.setCatalog(catalog);
    
    replayAll();
    
    Map<String, CatalogEntry> result = classUnderTest.fetchEntriesMap(filter);
    
    verifyAll();
    
    assertTrue(result.size() == sources.size());
  }
  
  @Test
  public void testFetchEntries_oneEntryDoesNotMatchFilter() throws Exception {
    List<String> sources = new ArrayList<String>(Arrays.asList("source1", "source2", "last_source"));
    CompositingRuleFilter filter = createMock(CompositingRuleFilter.class);
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    
    classUnderTest.setSources(sources);
    
    for (String source : sources) {
      boolean fileMatches = true;
      if (source.equals("source2")) {
        fileMatches = false;
      }
      
      CatalogEntry entry = createMock(CatalogEntry.class);
      FileEntry fileEntry = createMock(FileEntry.class);
      
      expect(entry.getSource()).andReturn(source);
      expect(entry.getFileEntry()).andReturn(fileEntry);
      expect(filter.fileMatches(fileEntry)).andReturn(fileMatches);
      
      entries.add(entry);
    }
    
    expect(catalog.fetch(filter)).andReturn(entries);
    
    classUnderTest.setCatalog(catalog);
    
    replayAll();
    
    Map<String, CatalogEntry> result = classUnderTest.fetchEntriesMap(filter);
    
    verifyAll();
    
    assertTrue(result.size() == (sources.size() - 1));
    assertTrue(result.keySet().contains("source1"));
    assertTrue(result.keySet().contains("last_source"));
    assertTrue(!result.keySet().contains("source2"));
  }

  @Test
  public void testCreateFilter() throws Exception {
    Date startDate = new Date(2010,1,1);
    Time startTime = new Time(1,2,0);
    Date stopDate = new Date(2010,1,1);
    Time stopTime = new Time(1,3,0);
    final DateTime startDT = new DateTime(startDate, startTime);
    final DateTime stopDT = new DateTime(stopDate, stopTime);

    expect(ruleUtil.createNextNominalTime(startDT, 10)).andReturn(stopDT);
    
    replayAll();
    
    classUnderTest.setInterval(10);
    CompositingRuleFilter result = classUnderTest.createFilter(startDT);
    
    verifyAll();
    assertNotNull(result);
    assertSame(startDT, result.getStartDateTime());
    assertSame(stopDT, result.getStopDateTime());
    assertEquals("PVOL", result.getObject());
  }
  
  @Test
  public void testHandle_fileMatches() throws Exception {
    testHandle(true, false); 
  }
  
  @Test
  public void testHandle_fileDoesNotMatch() throws Exception {
    testHandle(false, false); 
  }
  
  @Test
  public void testHandle_exceedsMaxAgeLimit() throws Exception {
    testHandle(true, true); 
  }
  
  private void testHandle(boolean fileMatches, boolean exceedsMaxAgeLimit) {
    final ICompositingMethods methods = createMock(ICompositingMethods.class);

    FileEntry fileEntry = createMock(FileEntry.class);
    DateTime dateTime = new DateTime(2017, 02, 01, 15, 10, 0);
    DateTime nominalDateTime = new DateTime(2017, 02, 01, 15, 0, 0);
    CompositingRuleFilter filter = createMock(CompositingRuleFilter.class);
    
    BltDataMessage msg = new BltDataMessage();
    BltDataMessage genMsg = new BltDataMessage();
    msg.setFileEntry(fileEntry);

    classUnderTest = new CompositingRule() {
      @Override
      public IBltMessage createComposite(IBltMessage message, CompositingRuleFilter ruleFilter) {
        return methods.createComposite(message, ruleFilter);
      }
      
      protected CompositingRuleFilter createFilter(DateTime nominalTime) {
        return methods.createFilter(nominalTime);
      }
      
      protected DateTime getDateTimeFromFile(FileEntry file) {
        return methods.getDateTimeFromFile(file);
      }
      
      protected boolean dateTimeExceedsMaxAgeLimit(DateTime dateTime) {
        return methods.dateTimeExceedsMaxAgeLimit(dateTime);
      }
    };
    
    classUnderTest.setRuleUtilities(ruleUtil);

    expect(fileEntry.getUuid()).andReturn(new UUID(100, 100)).anyTimes();      

    expect(methods.getDateTimeFromFile(fileEntry)).andReturn(dateTime);
    expect(methods.dateTimeExceedsMaxAgeLimit(dateTime)).andReturn(exceedsMaxAgeLimit);
    expect(ruleUtil.createNominalTime(dateTime, classUnderTest.getInterval())).andReturn(nominalDateTime);
    expect(methods.createFilter(nominalDateTime)).andReturn(filter);
    if (!exceedsMaxAgeLimit) {
      expect(filter.fileMatches(fileEntry)).andReturn(fileMatches);      
    }
    
    if (fileMatches && !exceedsMaxAgeLimit) {
      expect(methods.createComposite(msg, filter)).andReturn(genMsg);
    }
    
    replayAll();
  
    IBltMessage result = classUnderTest.handle(msg);
    
    verifyAll();
    if (fileMatches && !exceedsMaxAgeLimit) {
      assertSame(result, genMsg);      
    } else {
      assertNull(result);
    }
  }
  
  @Test
  public void testCreateComposite_pvol() throws Exception {
    testCreateComposite(false, false, false, false); 
  }
  
  @Test
  public void testCreateComposite_pvol_sourceMissing() throws Exception {
    testCreateComposite(false, true, false, false); 
  }
  
  @Test
  public void testCreateComposite_scan() throws Exception {
    testCreateComposite(true, false, false, false); 
  }
  
  @Test
  public void testCreateComposite_scan_sourceMissing() throws Exception {
    testCreateComposite(true, true, false, false); 
  }
  
  @Test
  public void testCreateComposite_scan_angleMissing() throws Exception {
    testCreateComposite(true, false, true, false); 
  }
  
  @Test
  public void testCreateComposite_scan_noPreviousAngles() throws Exception {
    testCreateComposite(true, false, false, true); 
  }
  
  private void testCreateComposite(boolean scanBased, boolean sourceMissing, boolean scanAngleMissing, boolean noPreviousAngles) {
    final ICompositingMethods methods = createMock(ICompositingMethods.class);

    int ruleId = 567;
    FileEntry fileEntry = createMock(FileEntry.class);
    DateTime dateTime = new DateTime(2017, 02, 01, 15, 10, 0);
    CompositingRuleFilter filter = createMock(CompositingRuleFilter.class);
    
    CompositeTimerData timerData = createMock(CompositeTimerData.class);
    TimeoutTask timeoutTask = createMock(TimeoutTask.class);
    
    Map<String, CatalogEntry> currentEntries = new HashMap<String, CatalogEntry>();
    
    List<String> previousSources = new ArrayList<String>(Arrays.asList("source1", "source2", "last_source"));
    Map<String,Double> previousAngles = new HashMap<String,Double>();
    
    for (String source : previousSources) {
      
      Double previousAngle = new Double(0.5);
      Double currentAngle = new Double(previousAngle);
      if (scanAngleMissing && source.equals("source2")) {
        currentAngle = previousAngle + new Double(0.2);
      }
      
      if (!noPreviousAngles) {
        previousAngles.put(source, previousAngle);        
      }
      
      if (!(sourceMissing && source.equals("last_source"))) {
        CatalogEntry catalogEntry = createMock(CatalogEntry.class);
        
        if (scanBased && !(scanAngleMissing && source.equals("last_source")) && !(noPreviousAngles && !source.equals("source1"))) {
          expect(catalogEntry.getAttribute("/dataset1/where/elangle")).andReturn(currentAngle);
        }
        
        currentEntries.put(source, catalogEntry);        
      }
    }
    
    BltDataMessage msg = new BltDataMessage();
    BltDataMessage genMsg = new BltDataMessage();
    msg.setFileEntry(fileEntry);

    classUnderTest = new CompositingRule() {
      @Override
      protected CompositeTimerData createTimerData(IBltMessage message) {
        return methods.createTimerData(message);
      }
      
      protected CompositingRuleFilter createFilter(DateTime nominalTime) {
        return methods.createFilter(nominalTime);
      }
      
      protected Map<String, CatalogEntry> fetchEntriesMap(CompositingRuleFilter filter) {
        return methods.fetchEntriesMap(filter);
      }
      
      protected IBltMessage createMessage(DateTime nominalTime, Map<String, CatalogEntry> entries) {
        return methods.createMessage(nominalTime, entries);
      }
    };
    
    long ttId = 987;

    expect(methods.createTimerData(msg)).andReturn(timerData);
    expect(timeoutManager.getRegisteredTask(timerData)).andReturn(timeoutTask);
    expect(timeoutTask.getData()).andReturn(timerData);
    expect(methods.fetchEntriesMap(filter)).andReturn(currentEntries);
    expect(timerData.getPreviousSources()).andReturn(previousSources);
    
    if (scanBased) {
      int noOfCalls = currentEntries.size();
      if (noPreviousAngles) {
        noOfCalls = 1;
      } else if (scanAngleMissing) {
        noOfCalls = currentEntries.size() - 1;
      }
      expect(timerData.getPreviousAngles()).andReturn(previousAngles).times(noOfCalls);
    }
    
    if (!sourceMissing && !scanAngleMissing && !noPreviousAngles) {    
      expect(timerData.getDateTime()).andReturn(dateTime).times(2);
      expect(methods.createMessage(dateTime, currentEntries)).andReturn(genMsg);
      
      ruleUtil.trigger(ruleId, dateTime);
      
      expect(timeoutTask.getId()).andReturn(ttId);
      
      timeoutManager.unregister(ttId);
    }
    
    replayAll();
    
    classUnderTest.setScanBased(scanBased);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setRuleId(ruleId);
  
    IBltMessage result = classUnderTest.createComposite(msg, filter);
    
    verifyAll();

    if (sourceMissing || scanAngleMissing || noPreviousAngles) {
      assertNull(result);
    } else {      
      assertSame(result, genMsg);      
    }
  }
  
  @Test
  public void testSetGetSelectionMethod() throws Exception {
    assertEquals(CompositingRule.SelectionMethod_NEAREST_RADAR, classUnderTest.getSelectionMethod());
    classUnderTest.setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    assertEquals(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL, classUnderTest.getSelectionMethod());
  }
  
  @Test
  public void testSetGetSelectionMethod_exception() throws Exception {
    try {
      classUnderTest.setSelectionMethod(99);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
    assertEquals(CompositingRule.SelectionMethod_NEAREST_RADAR, classUnderTest.getSelectionMethod());
  }

  @Test
  public void testSetGetDetectors() throws Exception {
    List<String> detectors = new ArrayList<String>();
    assertEquals(0, classUnderTest.getDetectors().size());
    classUnderTest.setDetectors(detectors);
    assertSame(detectors, classUnderTest.getDetectors());
  }

  @Test
  public void testSetGetDetectors_null() throws Exception {
    List<String> detectors = new ArrayList<String>();
    classUnderTest.setDetectors(detectors);
    classUnderTest.setDetectors(null);
    assertNotNull(classUnderTest.getDetectors());
    assertEquals(0, classUnderTest.getDetectors().size());
    assertNotSame(detectors, classUnderTest.getDetectors());
  }
  
}
