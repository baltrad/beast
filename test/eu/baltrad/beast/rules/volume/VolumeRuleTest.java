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
package eu.baltrad.beast.rules.volume;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanInitializationException;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.oh5.MetadataMatcher;
import eu.baltrad.bdb.oh5.Source;
import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.bdb.util.Time;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.db.filters.TimeIntervalFilter;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 * 
 */
public class VolumeRuleTest extends EasyMockSupport {
  private VolumeRule classUnderTest = null;
  private VolumeRuleMethods methods = null;
  private Catalog catalog = null;
  private TimeoutManager timeoutManager = null;
  private IRuleUtilities utilities = null;
  private MetadataMatcher matcher = null;
  
  private static interface VolumeRuleMethods {
    public TimeIntervalFilter createFilter(DateTime nominalTime, String source);
    public boolean replaceScanElevation(List<CatalogEntry> entries, CatalogEntry entry, Time nominalTime);
    public List<CatalogEntry> createCatalogEntryList();
    public VolumeTimerData createTimerData(IBltMessage msg);
    public boolean isHandled(VolumeTimerData data);
    public List<CatalogEntry> fetchAllCurrentEntries(DateTime nominalTime, String source);
    public boolean areCriteriasMet(List<CatalogEntry> entries, DateTime dt, String source);
    public List<CatalogEntry> filterEntries(List<CatalogEntry> entries, Time nominalTime);
    public IBltMessage createMessage(DateTime nominalTime, List<CatalogEntry> entries);
  };

  @Before
  public void setUp() throws Exception {
    methods = createMock(VolumeRuleMethods.class);
    catalog = createMock(Catalog.class);
    timeoutManager = createMock(TimeoutManager.class);
    utilities = createMock(IRuleUtilities.class);
    matcher = createMock(MetadataMatcher.class);
    
    classUnderTest = new VolumeRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setRuleUtilities(utilities);
    classUnderTest.setMatcher(matcher);
    classUnderTest.setTimeout(0); // No timeout initially
    classUnderTest.setRuleId(10);
  }

  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
 
  @Test
  public void testHandle_noScanData() throws Exception {
    IBltMessage msg = new IBltMessage() {};
    
    expect(methods.createTimerData(msg)).andReturn(null);
    
    VolumeRule classUnderTest = new VolumeRule() {
      public VolumeTimerData createTimerData(IBltMessage msg) {
        return methods.createTimerData(msg);
      }
    };
    
    replayAll();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verifyAll();
    assertNull(result);
  }
  
  @Test
  public void testHandle_alreadyHandled() throws Exception {
    IBltMessage msg = new IBltMessage() {};
    VolumeTimerData data = new VolumeTimerData(1, new DateTime(), "some");
    
    expect(methods.createTimerData(msg)).andReturn(data);
    expect(methods.isHandled(data)).andReturn(true);
    
    VolumeRule classUnderTest = new VolumeRule() {
      public VolumeTimerData createTimerData(IBltMessage msg) {
        return methods.createTimerData(msg);
      }
      public boolean isHandled(VolumeTimerData data) {
        return methods.isHandled(data);
      }
    };
    
    replayAll();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verifyAll();
    assertNull(result);
  }
  
  @Test
  public void testHandle() throws Exception {
    BltDataMessage msg = createMock(BltDataMessage.class);
    FileEntry fileEntry = createMock(FileEntry.class);
    UUID ruid = UUID.randomUUID();
    DateTime nominalTime = new DateTime();
    VolumeTimerData data = new VolumeTimerData(1, nominalTime, "some");
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    List<CatalogEntry> newentries = new ArrayList<CatalogEntry>();
    BltGenerateMessage message = new BltGenerateMessage();
    
    expect(methods.createTimerData(msg)).andReturn(data);
    expect(methods.isHandled(data)).andReturn(false);
    expect(methods.fetchAllCurrentEntries(nominalTime, "some")).andReturn(entries);
    expect(timeoutManager.getRegisteredTask(data)).andReturn(null);
    expect(methods.areCriteriasMet(entries, nominalTime, "some")).andReturn(true);
    expect(methods.filterEntries(entries, nominalTime.getTime())).andReturn(newentries);
    expect(methods.createMessage(nominalTime, newentries)).andReturn(message);
    expect(msg.getFileEntry()).andReturn(fileEntry).anyTimes();
    expect(fileEntry.getUuid()).andReturn(ruid).anyTimes();
    
    VolumeRule classUnderTest = new VolumeRule() {
      public VolumeTimerData createTimerData(IBltMessage msg) {
        return methods.createTimerData(msg);
      }
      public boolean isHandled(VolumeTimerData data) {
        return methods.isHandled(data);
      }
      protected List<CatalogEntry> fetchAllCurrentEntries(DateTime nominalTime, String source) {
        return methods.fetchAllCurrentEntries(nominalTime, source);
      }
      protected boolean areCriteriasMet(List<CatalogEntry> entries, DateTime dt, String source) {
        return methods.areCriteriasMet(entries, dt, source);
      }
      public List<CatalogEntry> filterEntries(List<CatalogEntry> entries, Time nominalTime) {
        return methods.filterEntries(entries, nominalTime);
      }
      public IBltMessage createMessage(DateTime nominalTime, List<CatalogEntry> entries) {
        return methods.createMessage(nominalTime, entries);
      }
    };
    classUnderTest.setTimeoutManager(timeoutManager);
    
    replayAll();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verifyAll();
    assertSame(message, result);
  }

  @Test
  public void testHandle_registerTimeout() throws Exception {
    BltDataMessage msg = createMock(BltDataMessage.class);
    FileEntry fileEntry = createMock(FileEntry.class);
    UUID ruid = UUID.randomUUID();
    DateTime nominalTime = new DateTime();
    VolumeTimerData data = new VolumeTimerData(1, nominalTime, "some");
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    
    VolumeRule classUnderTest = new VolumeRule() {
      public VolumeTimerData createTimerData(IBltMessage msg) {
        return methods.createTimerData(msg);
      }
      public boolean isHandled(VolumeTimerData data) {
        return methods.isHandled(data);
      }
      protected List<CatalogEntry> fetchAllCurrentEntries(DateTime nominalTime, String source) {
        return methods.fetchAllCurrentEntries(nominalTime, source);
      }
      protected boolean areCriteriasMet(List<CatalogEntry> entries, DateTime dt, String source) {
        return methods.areCriteriasMet(entries, dt, source);
      }
    };

    expect(methods.createTimerData(msg)).andReturn(data);
    expect(methods.isHandled(data)).andReturn(false);
    expect(methods.fetchAllCurrentEntries(nominalTime, "some")).andReturn(entries);
    expect(timeoutManager.getRegisteredTask(data)).andReturn(null);
    expect(methods.areCriteriasMet(entries, nominalTime, "some")).andReturn(false);
    expect(utilities.getTimeoutTime(nominalTime, true, 10000)).andReturn(10000L);
    expect(timeoutManager.register(classUnderTest, 10000L, data)).andReturn(1L);
    expect(msg.getFileEntry()).andReturn(fileEntry).anyTimes();
    expect(fileEntry.getUuid()).andReturn(ruid).anyTimes();
    
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setRuleUtilities(utilities);
    classUnderTest.setTimeout(10);
    classUnderTest.setNominalTimeout(true);
    
    replayAll();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verifyAll();
    assertNull(result);
  }
  
  @Test
  public void testAreCriteriasMet_noHit() throws Exception {
    DateTime now = new DateTime();
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry(new Double(1.0)));
    entries.add(createCatalogEntry(new Double(5.0)));
    entries.add(createCatalogEntry(new Double(10.0)));
    
    classUnderTest.setElevationMax(11.0);
    classUnderTest.setAscending(true);
    
    replayAll();
    
    boolean result = classUnderTest.areCriteriasMet(entries, now, "seang");
    
    verifyAll();
    assertEquals(false, result);
  }

  @Test
  public void createTimerData_noFilter() throws Exception {
    Time t = new Time();
    Date d = new Date();
    Source s = new Source("aname");
    DateTime dt = new DateTime();
    FileEntry fe = createMock(FileEntry.class);
    Metadata m = createMock(Metadata.class);

    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(fe);
    
    expect(fe.getMetadata()).andReturn(m).anyTimes();
    expect(m.getWhatObject()).andReturn("SCAN");
    expect(m.getWhatTime()).andReturn(t);
    expect(m.getWhatDate()).andReturn(d);
    expect(fe.getSource()).andReturn(s);
    expect(utilities.createNominalTime(d, t, 15)).andReturn(dt);
    
    classUnderTest = new VolumeRule();
    classUnderTest.setRuleId(99);
    classUnderTest.setRuleUtilities(utilities);
    
    replayAll();
    
    VolumeTimerData result = classUnderTest.createTimerData(msg);
    
    verifyAll();
    assertNotNull(result);
    assertSame(dt, result.getDateTime());
    assertEquals(99, result.getRuleId());
    assertEquals("aname", result.getSource());
  }

  @Test
  public void createTimerData_filter() throws Exception {
    Time t = new Time();
    Date d = new Date();
    Source s = new Source("aname");
    DateTime dt = new DateTime();
    FileEntry fe = createMock(FileEntry.class);
    Metadata m = createMock(Metadata.class);
    IFilter filter = createMock(IFilter.class);
    Expression xpr = createMock(Expression.class);
    
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(fe);
    
    expect(fe.getMetadata()).andReturn(m).anyTimes();
    expect(m.getWhatObject()).andReturn("SCAN");
    expect(m.getWhatTime()).andReturn(t);
    expect(m.getWhatDate()).andReturn(d);
    expect(fe.getSource()).andReturn(s);
    expect(utilities.createNominalTime(d, t, 15)).andReturn(dt);
    expect(filter.getExpression()).andReturn(xpr);
    expect(matcher.match(m, xpr)).andReturn(true);
    
    classUnderTest = new VolumeRule();
    classUnderTest.setRuleId(99);
    classUnderTest.setRuleUtilities(utilities);
    classUnderTest.setFilter(filter);
    classUnderTest.setMatcher(matcher);
    
    replayAll();
    
    VolumeTimerData result = classUnderTest.createTimerData(msg);
    
    verifyAll();
    assertNotNull(result);
    assertSame(dt, result.getDateTime());
    assertEquals(99, result.getRuleId());
    assertEquals("aname", result.getSource());
  }  

  @Test
  public void createTimerData_filterNotMatching() throws Exception {
    Time t = new Time();
    Date d = new Date();
    Source s = new Source("aname");
    DateTime dt = new DateTime();
    FileEntry fe = createMock(FileEntry.class);
    Metadata m = createMock(Metadata.class);
    IFilter filter = createMock(IFilter.class);
    Expression xpr = createMock(Expression.class);
    
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(fe);
    
    expect(fe.getMetadata()).andReturn(m).anyTimes();
    expect(m.getWhatObject()).andReturn("SCAN");
    expect(m.getWhatTime()).andReturn(t);
    expect(m.getWhatDate()).andReturn(d);
    expect(fe.getSource()).andReturn(s);
    expect(utilities.createNominalTime(d, t, 15)).andReturn(dt);
    expect(filter.getExpression()).andReturn(xpr);
    expect(matcher.match(m, xpr)).andReturn(false);
    
    classUnderTest = new VolumeRule();
    classUnderTest.setRuleId(99);
    classUnderTest.setRuleUtilities(utilities);
    classUnderTest.setFilter(filter);
    classUnderTest.setMatcher(matcher);
    
    replayAll();
    
    VolumeTimerData result = classUnderTest.createTimerData(msg);
    
    verifyAll();
    assertNull(result);
  }  
  
  @Test
  public void testAreCriteriasMet_hit() throws Exception {
    DateTime now = new DateTime();
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry(new Double(1.0)));
    entries.add(createCatalogEntry(new Double(5.0)));
    entries.add(createCatalogEntry(new Double(11.0)));

    classUnderTest.setElevationMax(11.0);
    classUnderTest.setAscending(true);
    
    replayAll();
    
    boolean result = classUnderTest.areCriteriasMet(entries, now, "seang");
    
    verifyAll();
    assertEquals(true, result);
  }

  @Test
  public void testAreCriteriasMet_withElevationAngles_hit() throws Exception {
    DateTime now = new DateTime();
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry(new Double(1.0)));
    entries.add(createCatalogEntry(new Double(5.0)));
    entries.add(createCatalogEntry(new Double(10.0)));
    entries.add(createCatalogEntry(new Double(11.0)));

    classUnderTest.setElevationMax(12.0);
    classUnderTest.setAscending(true);
    classUnderTest.setElevationAngles("1.0,5.0,10.0,11.0");
    
    replayAll();
    
    boolean result = classUnderTest.areCriteriasMet(entries, now, "seang");
    
    verifyAll();
    assertEquals(true, result);
  }
  
  @Test
  public void testAreCriteriasMet_withElevationAngles_nohit() throws Exception {
    DateTime now = new DateTime();
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry(new Double(1.0)));
    entries.add(createCatalogEntry(new Double(5.0)));
    entries.add(createCatalogEntry(new Double(11.0)));

    classUnderTest.setElevationMax(11.0);
    classUnderTest.setAscending(true);
    classUnderTest.setElevationAngles("1.0,5.0,10.0,11.0");
    
    replayAll();
    
    boolean result = classUnderTest.areCriteriasMet(entries, now, "seang");
    
    verifyAll();
    assertEquals(false, result);
  }
  
  @Test
  public void testCreateMessage() throws Exception {
    Date date = new Date(2010, 2, 1);
    Time time = new Time(1, 0, 0);
    DateTime nt = new DateTime(date, time);
    
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("searl"));
    entries.add(createCatalogEntry("searl"));
    entries.add(createCatalogEntry("searl"));

    List<String> fileEntries = new ArrayList<String>();
    fileEntries.add("uuid-1");
    fileEntries.add("uuid-2");
    fileEntries.add("uuid-3");

    List<String> detectors = new ArrayList<String>();
    detectors.add("ropo");
    detectors.add("nisse");
    
    classUnderTest.setDetectors(detectors);
    
    expect(utilities.getUuidStringsFromEntries(entries)).andReturn(fileEntries);

    replayAll();

    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.createMessage(nt, entries);

    verifyAll();
    assertEquals("eu.baltrad.beast.GenerateVolume", result.getAlgorithm());
    String[] files = result.getFiles();
    assertEquals(3, files.length);
    assertEquals(files[0], "uuid-1");
    assertEquals(files[1], "uuid-2");
    assertEquals(files[2], "uuid-3");
    String[] arguments = result.getArguments();
    assertEquals(7, arguments.length);
    assertEquals("--source=searl", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--anomaly-qc=ropo,nisse", arguments[3]);
    assertEquals("--qc-mode=ANALYZE_AND_APPLY", arguments[4]);
    assertEquals("--algorithm_id=10-searl",arguments[5]);
    assertEquals("--merge=true", arguments[6]);
  }

  @Test
  public void testCreateMessage_noDetectors() throws Exception {
    Date date = new Date(2010, 2, 1);
    Time time = new Time(1, 0, 0);
    DateTime nt = new DateTime(date, time);
    
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("searl"));
    entries.add(createCatalogEntry("searl"));
    entries.add(createCatalogEntry("searl"));

    List<String> fileEntries = new ArrayList<String>();
    fileEntries.add("uuid-1");
    fileEntries.add("uuid-2");
    fileEntries.add("uuid-3");
    
    expect(utilities.getUuidStringsFromEntries(entries)).andReturn(fileEntries);

    replayAll();

    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.createMessage(nt, entries);

    verifyAll();
    assertEquals("eu.baltrad.beast.GenerateVolume", result.getAlgorithm());
    String[] files = result.getFiles();
    assertEquals(3, files.length);
    assertEquals(files[0], "uuid-1");
    assertEquals(files[1], "uuid-2");
    assertEquals(files[2], "uuid-3");
    String[] arguments = result.getArguments();
    assertEquals(5, arguments.length);
    assertEquals("--source=searl", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--algorithm_id=10-searl",arguments[3]);
    assertEquals("--merge=true", arguments[4]);
  }
  
  @Test
  public void testCreateMessage_withAnalyse() throws Exception {
    Date date = new Date(2010, 2, 1);
    Time time = new Time(1, 0, 0);
    DateTime nt = new DateTime(date, time);
    
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("searl"));
    entries.add(createCatalogEntry("searl"));
    entries.add(createCatalogEntry("searl"));

    List<String> fileEntries = new ArrayList<String>();
    fileEntries.add("uuid-1");
    fileEntries.add("uuid-2");
    fileEntries.add("uuid-3");

    List<String> detectors = new ArrayList<String>();
    detectors.add("ropo");
    detectors.add("nisse");
    
    classUnderTest.setDetectors(detectors);
    classUnderTest.setQualityControlMode(VolumeRule.QualityControlMode_ANALYZE);
    expect(utilities.getUuidStringsFromEntries(entries)).andReturn(fileEntries);

    replayAll();

    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.createMessage(nt, entries);

    verifyAll();
    assertEquals("eu.baltrad.beast.GenerateVolume", result.getAlgorithm());
    String[] files = result.getFiles();
    assertEquals(3, files.length);
    assertEquals(files[0], "uuid-1");
    assertEquals(files[1], "uuid-2");
    assertEquals(files[2], "uuid-3");
    String[] arguments = result.getArguments();
    assertEquals(7, arguments.length);
    assertEquals("--source=searl", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
    assertEquals("--anomaly-qc=ropo,nisse", arguments[3]);
    assertEquals("--qc-mode=ANALYZE", arguments[4]);
    assertEquals("--algorithm_id=10-searl",arguments[5]);
    assertEquals("--merge=true", arguments[6]);
  }
  
  @Test
  public void testCreateMessage_noEntries() throws Exception {
    DateTime nt = new DateTime(2010, 2, 1, 1, 0, 0);
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.createMessage(nt, entries);
    assertEquals(null, result);
  }

  @Test
  public void testCreateMessage_nullEntries() throws Exception {
    DateTime nt = new DateTime(2010, 2, 1, 1, 0, 0);
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.createMessage(nt, null);
    assertEquals(null, result);
  }
  
  @Test
  public void testFetchAllCurrentEntries() throws Exception {
    DateTime nominalTime = new DateTime(2010,1,1,0,0,0);
    TimeIntervalFilter filter = new TimeIntervalFilter();
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    
    expect(methods.createFilter(nominalTime, "seang")).andReturn(filter);
    expect(catalog.fetch(filter)).andReturn(entries);
    
    classUnderTest = new VolumeRule() {
      protected TimeIntervalFilter createFilter(DateTime nominalTime, String source) {
        return methods.createFilter(nominalTime, source);
      }
    };
    classUnderTest.setCatalog(catalog);
    
    replayAll();
    
    List<CatalogEntry> result = classUnderTest.fetchAllCurrentEntries(nominalTime, "seang");
    
    verifyAll();
    assertSame(entries, result);
  }
  
  @Test
  public void testCreateFilter() {
    DateTime nt = new DateTime(2010,1,1,12,0,0);
    DateTime nextNt = new DateTime(2010,1,1,12,15,0);
    
    classUnderTest.setElevationMax(10.0);
    classUnderTest.setElevationMin(2.0);
    classUnderTest.setInterval(15*60);

    expect(utilities.createNextNominalTime(nt, 15*60)).andReturn(nextNt);
    
    replayAll();
    
    TimeIntervalFilter result = classUnderTest.createFilter(nt, "searl");
    
    verifyAll();
    
    assertEquals("searl", result.getSource());
    assertEquals(nt, result.getStartDateTime());
    assertEquals(nextNt, result.getStopDateTime());
    assertEquals("SCAN", result.getObject());
  }

  @Test
  public void testReplaceScanElevation() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    CatalogEntry e1 = createMock(CatalogEntry.class);
    CatalogEntry e2 = createMock(CatalogEntry.class);
    CatalogEntry e3 = createMock(CatalogEntry.class);
    CatalogEntry entry = createMock(CatalogEntry.class);
    
    Time nominalTime = new Time(10, 0, 0);
    
    entries.add(e1);
    entries.add(e2);
    entries.add(e3);
    
    expect(entry.getAttribute("/dataset1/what/starttime")).andReturn("100005");
    expect(entry.getAttribute("/dataset1/where/elangle")).andReturn(new Double(0.5));
    expect(e1.getAttribute("/dataset1/where/elangle")).andReturn(new Double(1.0));
    expect(e2.getAttribute("/dataset1/where/elangle")).andReturn(new Double(0.5));
    expect(e2.getAttribute("/dataset1/what/starttime")).andReturn("100006");

    replayAll();
    
    boolean result = classUnderTest.replaceScanElevation(entries, entry, nominalTime);
    
    verifyAll();
    assertEquals(true, result);
    assertSame(entry, entries.get(1));
  }

  @Test
  public void testReplaceScanElevation_noReplacement() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    CatalogEntry e1 = createMock(CatalogEntry.class);
    CatalogEntry e2 = createMock(CatalogEntry.class);
    CatalogEntry e3 = createMock(CatalogEntry.class);
    CatalogEntry entry = createMock(CatalogEntry.class);
    
    Time nominalTime = new Time(10, 0, 0);
    
    entries.add(e1);
    entries.add(e2);
    entries.add(e3);
    
    expect(entry.getAttribute("/dataset1/what/starttime")).andReturn("100005");
    expect(entry.getAttribute("/dataset1/where/elangle")).andReturn(new Double(0.5));
    expect(e1.getAttribute("/dataset1/where/elangle")).andReturn(new Double(1.0));
    expect(e2.getAttribute("/dataset1/where/elangle")).andReturn(new Double(0.5));
    expect(e2.getAttribute("/dataset1/what/starttime")).andReturn("100004");
    
    replayAll();
    
    boolean result = classUnderTest.replaceScanElevation(entries, entry, nominalTime);
    
    verifyAll();
    assertEquals(true, result);
    assertSame(e2, entries.get(1));
  }

  @Test
  public void testReplaceScanElevation_noMatchingElevation() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    CatalogEntry e1 = createMock(CatalogEntry.class);
    CatalogEntry e2 = createMock(CatalogEntry.class);
    CatalogEntry e3 = createMock(CatalogEntry.class);
    CatalogEntry entry = createMock(CatalogEntry.class);
    
    Time nominalTime = new Time(10, 0, 0);
    
    entries.add(e1);
    entries.add(e2);
    entries.add(e3);
    
    expect(entry.getAttribute("/dataset1/what/starttime")).andReturn("100005");
    expect(entry.getAttribute("/dataset1/where/elangle")).andReturn(new Double(0.5));
    expect(e1.getAttribute("/dataset1/where/elangle")).andReturn(new Double(1.0));
    expect(e2.getAttribute("/dataset1/where/elangle")).andReturn(new Double(1.5));
    expect(e3.getAttribute("/dataset1/where/elangle")).andReturn(new Double(2.5));
    
    replayAll();
    
    boolean result = classUnderTest.replaceScanElevation(entries, entry, nominalTime);

    verifyAll();
    
    assertEquals(false, result);
    assertSame(e1, entries.get(0));
    assertSame(e2, entries.get(1));
    assertSame(e3, entries.get(2));
  }

  @Test
  public void testFilterEntries() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    List<CatalogEntry> filtered = new ArrayList<CatalogEntry>();
    CatalogEntry e1 = createMock(CatalogEntry.class);
    CatalogEntry e2 = createMock(CatalogEntry.class);
    CatalogEntry e3 = createMock(CatalogEntry.class);
    CatalogEntry e4 = createMock(CatalogEntry.class);
    methods = createMock(VolumeRuleMethods.class);

    Time nominalTime = new Time(10,0,0);
    entries.add(e1);
    entries.add(e2);
    entries.add(e3);
    entries.add(e4);

    expect(methods.createCatalogEntryList()).andReturn(filtered);
    expect(e1.getAttribute("/dataset1/where/elangle")).andReturn(new Double(0.5));
    expect(e2.getAttribute("/dataset1/where/elangle")).andReturn(new Double(1.0));
    expect(methods.replaceScanElevation(filtered, e2, nominalTime)).andReturn(false);
    expect(e3.getAttribute("/dataset1/where/elangle")).andReturn(new Double(2.0));
    expect(e4.getAttribute("/dataset1/where/elangle")).andReturn(new Double(1.0));
    expect(methods.replaceScanElevation(filtered, e4, nominalTime)).andReturn(true);
    
    replayAll();
    
    classUnderTest = new VolumeRule() {
      protected boolean replaceScanElevation(List<CatalogEntry> entries, CatalogEntry entry, Time nominalTime) {
        return methods.replaceScanElevation(entries, entry, nominalTime);
      }
      protected List<CatalogEntry> createCatalogEntryList() {
        return methods.createCatalogEntryList();
      }
    };
    classUnderTest.setElevationMin(0.7);
    classUnderTest.setElevationMax(1.5);
    
    List<CatalogEntry> result = classUnderTest.filterEntries(entries, nominalTime);

    verifyAll();
    
    assertSame(filtered, result);
    assertEquals(1, result.size());
  }

  @Test
  public void testAfterPropertiesSet() throws Exception {
    VolumeRule classUnderTest = new VolumeRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(utilities);
    classUnderTest.setTimeoutManager(timeoutManager);

    classUnderTest.afterPropertiesSet();
  }

  @Test
  public void testAfterPropertiesSet_missingCatalog() throws Exception {
    VolumeRule classUnderTest = new VolumeRule();
    classUnderTest.setRuleUtilities(utilities);
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
    VolumeRule classUnderTest = new VolumeRule();
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
    VolumeRule classUnderTest = new VolumeRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(utilities);

    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected BeanInitializationException");
    } catch (BeanInitializationException e) {
      // pass
    }
  }
  
  @Test
  public void testSetElevationAngles() {
    classUnderTest.setElevationAngles("1.5,2,3.5,4");
    assertEquals(4, classUnderTest.getElevationAnglesAsDoubles().size());
    assertEquals(1.5, classUnderTest.getElevationAnglesAsDoubles().get(0), 2);
    assertEquals(2.0, classUnderTest.getElevationAnglesAsDoubles().get(1), 2);
    assertEquals(3.5, classUnderTest.getElevationAnglesAsDoubles().get(2), 2);
    assertEquals(4.0, classUnderTest.getElevationAnglesAsDoubles().get(3), 2);
  }
  
  @Test
  public void testSetElevationAngles_withWhiteSpaces() {
    classUnderTest.setElevationAngles(" 1.5 , 2, 3.5 , 4 ");
    assertEquals(4, classUnderTest.getElevationAnglesAsDoubles().size());
    assertEquals(1.5, classUnderTest.getElevationAnglesAsDoubles().get(0), 2);
    assertEquals(2.0, classUnderTest.getElevationAnglesAsDoubles().get(1), 2);
    assertEquals(3.5, classUnderTest.getElevationAnglesAsDoubles().get(2), 2);
    assertEquals(4.0, classUnderTest.getElevationAnglesAsDoubles().get(3), 2);
  }
  
  @Test
  public void testSetElevationAngles_empty() {
    classUnderTest.setElevationAngles("1.5,2,3.5,4");
    assertEquals(4, classUnderTest.getElevationAnglesAsDoubles().size());
    classUnderTest.setElevationAngles(null);
    assertEquals(0, classUnderTest.getElevationAnglesAsDoubles().size());
    classUnderTest.setElevationAngles("");
    assertEquals(0, classUnderTest.getElevationAnglesAsDoubles().size());
  }

  @Test
  public void testSetElevationAngles_badValues_alreadySet() {
    classUnderTest.setElevationAngles("1.5,2,3.5,4");
    try {
      classUnderTest.setElevationAngles("1.5.2,3.5,4");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
    assertEquals(4, classUnderTest.getElevationAnglesAsDoubles().size());
  }

  @Test
  public void testSetElevationAngles_badValues() {
    try {
      classUnderTest.setElevationAngles("1.5.2,3.5,4");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
    assertEquals(0, classUnderTest.getElevationAnglesAsDoubles().size());
  }
  
  @Test
  public void testGetElevationAngles() {
    classUnderTest.setElevationAngles("1.5,2,3.5,4");
    assertEquals("1.5,2.0,3.5,4.0", classUnderTest.getElevationAngles());
  }

  @Test
  public void testGetElevationAngles_withSpacesInSet() {
    classUnderTest.setElevationAngles(" 1.5 ,2, 3.5,4 ");
    assertEquals("1.5,2.0,3.5,4.0", classUnderTest.getElevationAngles());
  }

  
  protected CatalogEntry createCatalogEntry(Double elangle) {
    CatalogEntry entry = createMock(CatalogEntry.class);
    expect(entry.getAttribute("/dataset1/where/elangle")).andReturn(elangle).times(0,99);
    return entry;
  }
  
  protected CatalogEntry createCatalogEntry(String source) {
    CatalogEntry entry = createMock(CatalogEntry.class);
    expect(entry.getSource()).andReturn(source).times(0,99);
    return entry;
  }
}
