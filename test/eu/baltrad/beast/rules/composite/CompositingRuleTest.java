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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanInitializationException;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.expr.ExpressionFactory;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.oh5.MetadataMatcher;
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
public class CompositingRuleTest extends EasyMockSupport {
  private Catalog catalog = null;
  private IRuleUtilities ruleUtil = null;
  private TimeoutManager timeoutManager = null;
  private MetadataMatcher matcher = null;
  private ExpressionFactory xpr = null;
  
  private CompositingRule classUnderTest = null;
  
  private static interface ICompositingMethods {
    public CompositeTimerData createTimerData(IBltMessage message);
    public List<CatalogEntry> fetchEntries(DateTime nominalTime);
    public TimeIntervalFilter createFilter(DateTime nominalTime);
    public List<CatalogEntry> fetchScanEntries(DateTime nominalTime);
    public LowestAngleFilter createScanFilter(DateTime nominalTime);
    public IBltMessage createMessage(DateTime nominalTime, List<CatalogEntry> entries);
    public boolean areCriteriasMet(List<CatalogEntry> entries);
  };

  @Before
  public void setUp() throws Exception {
    catalog = createMock(Catalog.class);
    timeoutManager = createMock(TimeoutManager.class);
    ruleUtil = createMock(IRuleUtilities.class);
    matcher = createMock(MetadataMatcher.class);
    xpr = new ExpressionFactory();
    classUnderTest = new CompositingRule();
    classUnderTest.setRuleId(10);
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setMetadataMatcher(matcher);
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
  public void testTimeout() throws Exception {
    final ICompositingMethods methods = createMock(ICompositingMethods.class);
    DateTime dt = new DateTime(2010, 4, 15, 10, 15, 0);
    CompositeTimerData ctd = new CompositeTimerData(15, dt);
    IBltMessage resultMessage = new IBltMessage() {
    };
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    List<String> recipients = new ArrayList<String>();
    
    expect(methods.fetchEntries(dt)).andReturn(entries);
    expect(ruleUtil.isTriggered(25, dt)).andReturn(false);
    expect(methods.createMessage(dt, entries)).andReturn(resultMessage);
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
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    List<String> recipients = new ArrayList<String>();
    
    expect(methods.fetchEntries(dt)).andReturn(entries);
    expect(ruleUtil.isTriggered(25, dt)).andReturn(true);
    
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
    Expression e1 = createMock(Expression.class);
    Expression e2 = createMock(Expression.class);
    Expression e3 = createMock(Expression.class);
    
    FileEntry file = createMock(FileEntry.class);
    Metadata metadata = createMock(Metadata.class);

    BltDataMessage dataMessage = new BltDataMessage();
    dataMessage.setFileEntry(file);

    expect(file.getMetadata()).andReturn(metadata).times(4);
    expect(metadata.getWhatObject()).andReturn("PVOL");
    expect(metadata.getWhatDate()).andReturn(date);
    expect(metadata.getWhatTime()).andReturn(time);
    expect(ruleUtil.createNominalTime(date, time, 10)).andReturn(nominalTime);
    expect(matcher.match(metadata, xpr.eq(xpr.attribute("what/quantity"), xpr.literal("VRAD")))).andReturn(true);
    
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
  public void testCreateTimerData_noQuantity() throws Exception {
    Date date = new Date(2010, 1, 1);
    Time time = new Time(10, 0, 0);
    DateTime nominalTime = new DateTime(date, time);
    Expression e1 = createMock(Expression.class);
    Expression e2 = createMock(Expression.class);
    Expression e3 = createMock(Expression.class);
    
    FileEntry file = createMock(FileEntry.class);
    Metadata metadata = createMock(Metadata.class);

    BltDataMessage dataMessage = new BltDataMessage();
    dataMessage.setFileEntry(file);

    expect(file.getMetadata()).andReturn(metadata).times(3);
    expect(metadata.getWhatObject()).andReturn("PVOL");
    expect(metadata.getWhatDate()).andReturn(date);
    expect(metadata.getWhatTime()).andReturn(time);
    expect(ruleUtil.createNominalTime(date, time, 10)).andReturn(nominalTime);
    
    expect(ruleUtil.isTriggered(25, nominalTime)).andReturn(false);
    
    classUnderTest.setRuleId(25);
    classUnderTest.setInterval(10);
    classUnderTest.setQuantity(null);
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
    
    expect(file.getMetadata()).andReturn(metadata).times(4);
    expect(metadata.getWhatObject()).andReturn("PVOL");
    expect(metadata.getWhatDate()).andReturn(date);
    expect(metadata.getWhatTime()).andReturn(time);
    expect(ruleUtil.createNominalTime(date, time, 10)).andReturn(nominalTime);
    expect(matcher.match(metadata, xpr.eq(xpr.attribute("what/quantity"), xpr.literal("DBZH")))).andReturn(true);
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
  public void testCreateTimerData_notVolume() throws Exception {
    FileEntry file = createMock(FileEntry.class);
    Metadata metadata = createMock(Metadata.class);
    Date d = new Date(2010, 1, 1);
    Time t = new Time(10, 1, 15);
    DateTime dt = new DateTime();
    
    BltDataMessage dataMessage = new BltDataMessage();
    dataMessage.setFileEntry(file);

    expect(file.getMetadata()).andReturn(metadata).times(4);
    expect(metadata.getWhatObject()).andReturn("IMAGE");
    expect(metadata.getWhatDate()).andReturn(d);
    expect(metadata.getWhatTime()).andReturn(t);
    expect(ruleUtil.createNominalTime(d, t, 10)).andReturn(dt);
    expect(matcher.match(metadata, xpr.eq(xpr.attribute("what/quantity"), xpr.literal("DBZH")))).andReturn(true);
    expect(ruleUtil.isTriggered(25, dt)).andReturn(false);
    classUnderTest.setRuleId(25);
    classUnderTest.setInterval(10);
    
    replayAll();
    
    CompositeTimerData result = classUnderTest.createTimerData(dataMessage);
    
    verifyAll();
    assertEquals(null, result);
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

  @Test
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
    expect(ruleUtil.getSourcesFromEntries(entries)).andReturn(entrySources);
    
    replayAll();
    
    boolean result = classUnderTest.areCriteriasMet(entries);
    
    verifyAll();
    
    assertEquals(true, result);
  }

  @Test
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
    
    expect(ruleUtil.getSourcesFromEntries(entries)).andReturn(entrySources);
    
    classUnderTest.setSources(sources);
    
    replayAll();
    
    boolean result = classUnderTest.areCriteriasMet(entries);
    
    verifyAll();
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
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    List<CatalogEntry> entriesByTime = new ArrayList<CatalogEntry>();
    List<CatalogEntry> entriesBySources = new ArrayList<CatalogEntry>();
    
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
    
    expect(ruleUtil.getEntriesByClosestTime(nominalTime, entries)).andReturn(entriesByTime);
    expect(ruleUtil.getEntriesBySources(sources, entriesByTime)).andReturn(entriesBySources);
    expect(ruleUtil.getUuidStringsFromEntries(entriesBySources)).andReturn(fileEntries);
    expect(ruleUtil.getSourcesFromEntries(entriesBySources)).andReturn(usedSources);
    ruleUtil.reportRadarSourceUsage(sources, usedSources);
    
    classUnderTest.setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    classUnderTest.setDetectors(detectors);
    
    classUnderTest.setMethod(CompositingRule.PPI);
    classUnderTest.setProdpar("0.5");
    
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
    assertEquals(9, arguments.length);
    assertEquals("--area=blt_composite", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--selection=HEIGHT_ABOVE_SEALEVEL", arguments[3]);
    assertEquals("--anomaly-qc=ropo,sigge,nisse", arguments[4]);
    assertEquals("--method=ppi", arguments[5]);
    assertEquals("--prodpar=0.5", arguments[6]);
    assertEquals("--quantity=DBZH", arguments[7]);
    assertEquals("--algorithm_id=10", arguments[8]);
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
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    List<CatalogEntry> entriesByTime = new ArrayList<CatalogEntry>();
    List<CatalogEntry> entriesBySources = new ArrayList<CatalogEntry>();
    
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
    
    expect(ruleUtil.getEntriesByClosestTime(nominalTime, entries)).andReturn(entriesByTime);
    expect(ruleUtil.getEntriesBySources(sources, entriesByTime)).andReturn(entriesBySources);
    expect(ruleUtil.getUuidStringsFromEntries(entriesBySources)).andReturn(fileEntries);
    expect(ruleUtil.getSourcesFromEntries(entriesBySources)).andReturn(usedSources);
    ruleUtil.reportRadarSourceUsage(sources, usedSources);
    
    classUnderTest.setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    classUnderTest.setDetectors(detectors);
    
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
    assertEquals(12, arguments.length);
    assertEquals("--area=blt_composite", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--selection=HEIGHT_ABOVE_SEALEVEL", arguments[3]);
    assertEquals("--anomaly-qc=ropo,sigge,nisse", arguments[4]);
    assertEquals("--method=ppi", arguments[5]);
    assertEquals("--prodpar=0.5", arguments[6]);
    assertEquals("--applygra=true", arguments[7]);
    assertEquals("--zrA=100.0", arguments[8]);
    assertEquals("--zrb=1.4", arguments[9]);
    assertEquals("--quantity=DBZH", arguments[10]);
    assertEquals("--algorithm_id=10", arguments[11]);
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
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    List<CatalogEntry> entriesByTime = new ArrayList<CatalogEntry>();
    List<CatalogEntry> entriesBySources = new ArrayList<CatalogEntry>();
    
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
    
    expect(ruleUtil.getEntriesByClosestTime(nominalTime, entries)).andReturn(entriesByTime);
    expect(ruleUtil.getEntriesBySources(sources, entriesByTime)).andReturn(entriesBySources);
    expect(ruleUtil.getUuidStringsFromEntries(entriesBySources)).andReturn(fileEntries);
    expect(ruleUtil.getSourcesFromEntries(entriesBySources)).andReturn(usedSources);
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
    assertEquals(13, arguments.length);
    assertEquals("--area=blt_composite", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--selection=HEIGHT_ABOVE_SEALEVEL", arguments[3]);
    assertEquals("--anomaly-qc=ropo,sigge,nisse", arguments[4]);
    assertEquals("--method=ppi", arguments[5]);
    assertEquals("--prodpar=0.5", arguments[6]);
    assertEquals("--applygra=true", arguments[7]);
    assertEquals("--zrA=100.0", arguments[8]);
    assertEquals("--zrb=1.4", arguments[9]);
    assertEquals("--ignore-malfunc=true", arguments[10]);
    assertEquals("--quantity=DBZH", arguments[11]);
    assertEquals("--algorithm_id=10", arguments[12]);
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
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    List<CatalogEntry> entriesByTime = new ArrayList<CatalogEntry>();
    List<CatalogEntry> entriesBySources = new ArrayList<CatalogEntry>();
    
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
    
    expect(ruleUtil.getEntriesByClosestTime(nominalTime, entries)).andReturn(entriesByTime);
    expect(ruleUtil.getEntriesBySources(sources, entriesByTime)).andReturn(entriesBySources);
    expect(ruleUtil.getUuidStringsFromEntries(entriesBySources)).andReturn(fileEntries);
    expect(ruleUtil.getSourcesFromEntries(entriesBySources)).andReturn(usedSources);
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
    assertEquals(14, arguments.length);
    assertEquals("--area=blt_composite", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--selection=HEIGHT_ABOVE_SEALEVEL", arguments[3]);
    assertEquals("--anomaly-qc=ropo,sigge,nisse", arguments[4]);
    assertEquals("--method=ppi", arguments[5]);
    assertEquals("--prodpar=0.5", arguments[6]);
    assertEquals("--applygra=true", arguments[7]);
    assertEquals("--zrA=100.0", arguments[8]);
    assertEquals("--zrb=1.4", arguments[9]);
    assertEquals("--ignore-malfunc=true", arguments[10]);
    assertEquals("--ctfilter=True", arguments[11]);
    assertEquals("--quantity=DBZH", arguments[12]);
    assertEquals("--algorithm_id=10", arguments[13]);
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
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    List<CatalogEntry> entriesByTime = new ArrayList<CatalogEntry>();
    List<CatalogEntry> entriesBySources = new ArrayList<CatalogEntry>();
    
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
    
    expect(ruleUtil.getEntriesByClosestTime(nominalTime, entries)).andReturn(entriesByTime);
    expect(ruleUtil.getEntriesBySources(sources, entriesByTime)).andReturn(entriesBySources);
    expect(ruleUtil.getUuidStringsFromEntries(entriesBySources)).andReturn(fileEntries);
    expect(ruleUtil.getSourcesFromEntries(entriesBySources)).andReturn(usedSources);
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
    assertEquals(15, arguments.length);
    assertEquals("--area=blt_composite", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--selection=HEIGHT_ABOVE_SEALEVEL", arguments[3]);
    assertEquals("--anomaly-qc=ropo,sigge,nisse", arguments[4]);
    assertEquals("--method=ppi", arguments[5]);
    assertEquals("--prodpar=0.5", arguments[6]);
    assertEquals("--applygra=true", arguments[7]);
    assertEquals("--zrA=100.0", arguments[8]);
    assertEquals("--zrb=1.4", arguments[9]);
    assertEquals("--ignore-malfunc=true", arguments[10]);
    assertEquals("--ctfilter=True", arguments[11]);
    assertEquals("--qitotal_field=se.baltrad.some.field", arguments[12]);
    assertEquals("--quantity=DBZH", arguments[13]);
    assertEquals("--algorithm_id=10", arguments[14]);
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
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    List<CatalogEntry> entriesByTime = new ArrayList<CatalogEntry>();
    List<CatalogEntry> entriesBySources = new ArrayList<CatalogEntry>();
    
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
    
    expect(ruleUtil.getEntriesByClosestTime(nominalTime, entries)).andReturn(entriesByTime);
    expect(ruleUtil.getEntriesBySources(sources, entriesByTime)).andReturn(entriesBySources);
    expect(ruleUtil.getUuidStringsFromEntries(entriesBySources)).andReturn(fileEntries);
    expect(ruleUtil.getSourcesFromEntries(entriesBySources)).andReturn(usedSources);
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
    assertEquals(14, arguments.length);
    assertEquals("--area=blt_composite", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--selection=HEIGHT_ABOVE_SEALEVEL", arguments[3]);
    assertEquals("--anomaly-qc=ropo,sigge,nisse", arguments[4]);
    assertEquals("--method=ppi", arguments[5]);
    assertEquals("--prodpar=0.5", arguments[6]);
    assertEquals("--applygra=true", arguments[7]);
    assertEquals("--zrA=100.0", arguments[8]);
    assertEquals("--zrb=1.4", arguments[9]);
    assertEquals("--ignore-malfunc=true", arguments[10]);
    assertEquals("--ctfilter=True", arguments[11]);
    assertEquals("--quantity=DBZH", arguments[12]);
    assertEquals("--algorithm_id=10", arguments[13]);
  }
  
  @Test
  public void testFetchEntries() throws Exception {
    final ICompositingMethods methods = createMock(ICompositingMethods.class);
    DateTime dt = new DateTime(2010,1,1,10,0,0);
    TimeIntervalFilter filter = new TimeIntervalFilter();
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    
    expect(methods.createFilter(dt)).andReturn(filter);
    expect(catalog.fetch(filter)).andReturn(entries);
    
    classUnderTest = new CompositingRule() {
      protected TimeIntervalFilter createFilter(DateTime nt) {
        return methods.createFilter(nt);
      }
    };
    classUnderTest.setCatalog(catalog);
    
    replayAll();
    
    List<CatalogEntry> result = classUnderTest.fetchEntries(dt);
    
    verifyAll();
    assertSame(entries, result);
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
    TimeIntervalFilter result = classUnderTest.createFilter(startDT);
    
    verifyAll();
    assertNotNull(result);
    assertSame(startDT, result.getStartDateTime());
    assertSame(stopDT, result.getStopDateTime());
    assertEquals("PVOL", result.getObject());
  }
  
  protected boolean compareDT(DateTime o1, DateTime o2) {
    return o1.equals(o2);
  }

  @Test
  public void testCreateCompositeScanMessage() throws Exception {
    final ICompositingMethods methods = createMock(ICompositingMethods.class);
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
    expect(ruleUtil.createNextNominalTime(dt, 10)).andReturn(ndt);
    expect(ruleUtil.fetchLowestSourceElevationAngle(dt, ndt, sources, "DBZH")).andReturn(currAngles);

    expect(methods.createMessage(dt, currEntries)).andReturn(createdMessage);
    expect(methods.fetchScanEntries(dt)).andReturn(currEntries);

    replayAll();
    
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
    
    verifyAll();
    assertSame(createdMessage, msg);
  }
  
  @Test
  public void testCreateCompositeScanMessage_withOtherQuantity() throws Exception {
    final ICompositingMethods methods = createMock(ICompositingMethods.class);
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
    expect(ruleUtil.createNextNominalTime(dt, 10)).andReturn(ndt);
    expect(ruleUtil.fetchLowestSourceElevationAngle(dt, ndt, sources, "VRAD")).andReturn(currAngles);

    expect(methods.createMessage(dt, currEntries)).andReturn(createdMessage);
    expect(methods.fetchScanEntries(dt)).andReturn(currEntries);

    replayAll();
    
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
    classUnderTest.setQuantity("VRAD");
    
    BltGenerateMessage msg = (BltGenerateMessage)classUnderTest.createCompositeScanMessage(data);
    
    verifyAll();
    assertSame(createdMessage, msg);
  }
  
  @Test
  public void testCreateCompositeScanMessage_missingSources() throws Exception {
    final ICompositingMethods methods = createMock(ICompositingMethods.class);
    
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
    expect(ruleUtil.createNextNominalTime(dt, 10)).andReturn(ndt);
    expect(ruleUtil.fetchLowestSourceElevationAngle(dt, ndt, sources, "DBZH")).andReturn(currAngles);
    
    replayAll();
    
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
    
    verifyAll();
    assertNull(msg);
  }

  @Test
  public void testCreateCompositeScanMessage_toHighElevation() throws Exception {
    final ICompositingMethods methods = createMock(ICompositingMethods.class);
    
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
    expect(ruleUtil.createNextNominalTime(dt, 10)).andReturn(ndt);
    expect(ruleUtil.fetchLowestSourceElevationAngle(dt, ndt, sources, "DBZH")).andReturn(currAngles);
    
    replayAll();
    
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
    
    verifyAll();
    assertNull(msg);
  }

  @Test
  public void testFetchScanEntries() throws Exception {
    final ICompositingMethods methods = createMock(ICompositingMethods.class);
    LowestAngleFilter filter = createMock(LowestAngleFilter.class);
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

    expect(methods.createScanFilter(dt)).andReturn(filter);
    filter.setSource("seang");
    expect(catalog.fetch(filter)).andReturn(entries1);
    filter.setSource("sekkr");
    expect(catalog.fetch(filter)).andReturn(entries2);

    classUnderTest = new CompositingRule() {
      protected LowestAngleFilter createScanFilter(DateTime nt) {
        return methods.createScanFilter(nt);
      }
    };
    classUnderTest.setCatalog(catalog);
    classUnderTest.setSources(sources);

    replayAll();

    List<CatalogEntry> result = classUnderTest.fetchScanEntries(dt);

    verifyAll();
    
    assertTrue(result.contains(e1));
    assertTrue(result.contains(e2));
  }

  @Test
  public void testCreateScanFilter() throws Exception {
    Date startDate = new Date(2010,1,1);
    Time startTime = new Time(1,2,0);
    Date stopDate = new Date(2010,1,1);
    Time stopTime = new Time(1,3,0);
    final DateTime startDT = new DateTime(startDate, startTime);
    final DateTime stopDT = new DateTime(stopDate, stopTime);

    expect(ruleUtil.createNextNominalTime(startDT, 10)).andReturn(stopDT);
    
    replayAll();
    classUnderTest.setInterval(10);
    LowestAngleFilter result = classUnderTest.createScanFilter(startDT);
    
    verifyAll();
    assertNotNull(result);
    assertSame(startDT, result.getStart());
    assertSame(stopDT, result.getStop());
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
  
  private CatalogEntry createCatalogEntry(String src, DateTime dt, double elangle) {
    CatalogEntry entry = createMock(CatalogEntry.class);
    return entry;
  }
}
