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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.springframework.beans.factory.BeanInitializationException;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.filters.TimeIntervalFilter;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.util.IRuleUtilities;
import eu.baltrad.fc.Date;
import eu.baltrad.fc.DateTime;
import eu.baltrad.fc.Time;

/**
 * @author Anders Henja
 * 
 */
public class VolumeRuleTest extends TestCase {
  private VolumeRule classUnderTest = null;
  private MockControl methodsControl = null;
  private VolumeRuleMethods methods = null;
  private MockControl catalogControl = null;
  private Catalog catalog = null;
  private MockControl timeoutControl = null;
  private TimeoutManager timeoutManager = null;
  private MockControl utilitiesControl = null;
  private IRuleUtilities utilities = null;
  
  private static interface VolumeRuleMethods {
    public TimeIntervalFilter createFilter(DateTime nominalTime, String source);
    public boolean replaceScanElevation(List<CatalogEntry> entries, CatalogEntry entry, Time nominalTime);
    public List<CatalogEntry> createCatalogEntryList();
    public VolumeTimerData createTimerData(IBltMessage msg);
    public boolean isHandled(VolumeTimerData data);
  };

  public void setUp() throws Exception {
    methodsControl = MockControl.createControl(VolumeRuleMethods.class);
    methods = (VolumeRuleMethods)methodsControl.getMock();
    catalogControl = MockClassControl.createControl(Catalog.class);
    catalog = (Catalog)catalogControl.getMock();
    timeoutControl = MockClassControl.createControl(TimeoutManager.class);
    timeoutManager = (TimeoutManager)timeoutControl.getMock();
    utilitiesControl = MockControl.createControl(IRuleUtilities.class);
    utilities = (IRuleUtilities)utilitiesControl.getMock();
    classUnderTest = new VolumeRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setRuleUtilities(utilities);
    classUnderTest.setTimeout(0); // No timeout initially
  }

  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  protected void replay() {
    methodsControl.replay();
    catalogControl.replay();
    timeoutControl.replay();
    utilitiesControl.replay();
  }

  protected void verify() {
    methodsControl.verify();
    catalogControl.verify();
    timeoutControl.verify();
    utilitiesControl.verify();
  }
  
  public void testHandle_noScanData() throws Exception {
    IBltMessage msg = new IBltMessage() {};
    methods.createTimerData(msg);
    methodsControl.setReturnValue(null);
    
    VolumeRule classUnderTest = new VolumeRule() {
      public VolumeTimerData createTimerData(IBltMessage msg) {
        return methods.createTimerData(msg);
      }
    };
    
    replay();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verify();
    assertNull(result);
  }
  
  public void testHandle_alreadyHandled() throws Exception {
    IBltMessage msg = new IBltMessage() {};
    VolumeTimerData data = new VolumeTimerData(1, new DateTime(), "some");
    
    methods.createTimerData(msg);
    methodsControl.setReturnValue(data);
    methods.isHandled(data);
    methodsControl.setReturnValue(true);
    
    VolumeRule classUnderTest = new VolumeRule() {
      public VolumeTimerData createTimerData(IBltMessage msg) {
        return methods.createTimerData(msg);
      }
      public boolean isHandled(VolumeTimerData data) {
        return methods.isHandled(data);
      }
    };
    
    replay();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verify();
    assertNull(result);
    
  }
  
  
  public void testAreCriteriasMet_noHit() throws Exception {
    DateTime now = new DateTime();
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry(new Double(1.0)));
    entries.add(createCatalogEntry(new Double(5.0)));
    entries.add(createCatalogEntry(new Double(10.0)));
    
    classUnderTest.setElevationMax(11.0);
    classUnderTest.setAscending(true);
    
    replay();
    
    boolean result = classUnderTest.areCriteriasMet(entries, now, "seang");
    
    verify();
    assertEquals(false, result);
  }

  public void testAreCriteriasMet_hit() throws Exception {
    DateTime now = new DateTime();
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry(new Double(1.0)));
    entries.add(createCatalogEntry(new Double(5.0)));
    entries.add(createCatalogEntry(new Double(11.0)));

    classUnderTest.setElevationMax(11.0);
    classUnderTest.setAscending(true);
    
    replay();
    
    boolean result = classUnderTest.areCriteriasMet(entries, now, "seang");
    
    verify();
    assertEquals(true, result);
  }

  public void testCreateMessage() throws Exception {
    Date date = new Date(2010, 2, 1);
    Time time = new Time(1, 0, 0);
    DateTime nt = new DateTime(date, time);
    
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry("searl","/tmp/searl_1.h5"));
    entries.add(createCatalogEntry("searl","/tmp/searl_2.h5"));
    entries.add(createCatalogEntry("searl","/tmp/searl_3.h5"));

    List<String> fileEntries = new ArrayList<String>();
    fileEntries.add("/tmp/searl_1.h5");
    fileEntries.add("/tmp/searl_2.h5");
    fileEntries.add("/tmp/searl_3.h5");

    utilities.getFilesFromEntries(entries);
    utilitiesControl.setReturnValue(fileEntries);

    replay();

    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.createMessage(nt, entries);

    verify();
    assertEquals("eu.baltrad.beast.GenerateVolume", result.getAlgorithm());
    String[] files = result.getFiles();
    assertEquals(3, files.length);
    assertEquals(files[0], "/tmp/searl_1.h5");
    assertEquals(files[1], "/tmp/searl_2.h5");
    assertEquals(files[2], "/tmp/searl_3.h5");
    String[] arguments = result.getArguments();
    assertEquals(3, arguments.length);
    assertEquals("--source=searl", arguments[0]);
    assertEquals("--date=20100201", arguments[1]);
    assertEquals("--time=010000", arguments[2]);
  }

  public void testCreateMessage_noEntries() throws Exception {
    DateTime nt = new DateTime(2010, 2, 1, 1, 0, 0);
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.createMessage(nt, entries);
    assertEquals(null, result);
  }

  public void testCreateMessage_nullEntries() throws Exception {
    DateTime nt = new DateTime(2010, 2, 1, 1, 0, 0);
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.createMessage(nt, null);
    assertEquals(null, result);
  }
  
  public void testFetchAllCurrentEntries() throws Exception {
    DateTime nominalTime = new DateTime(2010,1,1,0,0,0);
    TimeIntervalFilter filter = new TimeIntervalFilter();
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    
    methods.createFilter(nominalTime, "seang");
    methodsControl.setReturnValue(filter);
    catalog.fetch(filter);
    catalogControl.setReturnValue(entries);
    
    classUnderTest = new VolumeRule() {
      protected TimeIntervalFilter createFilter(DateTime nominalTime, String source) {
        return methods.createFilter(nominalTime, source);
      }
    };
    classUnderTest.setCatalog(catalog);
    
    replay();
    
    List<CatalogEntry> result = classUnderTest.fetchAllCurrentEntries(nominalTime, "seang");
    
    verify();
    assertSame(entries, result);
  }
  
  public void testCreateFilter() {
    DateTime nt = new DateTime(2010,1,1,12,0,0);
    DateTime nextNt = new DateTime(2010,1,1,12,15,0);
    
    classUnderTest.setElevationMax(10.0);
    classUnderTest.setElevationMin(2.0);
    classUnderTest.setInterval(15*60);

    utilities.createNextNominalTime(nt, 15*60);
    utilitiesControl.setReturnValue(nextNt);
    
    replay();
    
    TimeIntervalFilter result = classUnderTest.createFilter(nt, "searl");
    
    verify();
    
    assertEquals("searl", result.getSource());
    assertEquals(nt, result.getStartDateTime());
    assertEquals(nextNt, result.getStopDateTime());
    assertEquals("SCAN", result.getObject());
  }

  public void testReplaceScanElevation() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    MockControl e1Control = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry e1 = (CatalogEntry)e1Control.getMock();
    
    MockControl e2Control = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry e2 = (CatalogEntry)e2Control.getMock();
    
    MockControl e3Control = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry e3 = (CatalogEntry)e3Control.getMock();
    
    MockControl entryControl = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry entry = (CatalogEntry)entryControl.getMock();
    
    Time nominalTime = new Time(10, 0, 0);
    
    entries.add(e1);
    entries.add(e2);
    entries.add(e3);
    
    entry.getAttribute("/dataset1/what/starttime");
    entryControl.setReturnValue("100005");
    entry.getAttribute("/dataset1/where/elangle");
    entryControl.setReturnValue(new Double(0.5));
    
    e1.getAttribute("/dataset1/where/elangle");
    e1Control.setReturnValue(new Double(1.0));
    e2.getAttribute("/dataset1/where/elangle");
    e2Control.setReturnValue(new Double(0.5));
    e2.getAttribute("/dataset1/what/starttime");
    e2Control.setReturnValue("100006");
    
    e1Control.replay();
    e2Control.replay();
    e3Control.replay();
    entryControl.replay();
    
    boolean result = classUnderTest.replaceScanElevation(entries, entry, nominalTime);
    
    e1Control.verify();
    e2Control.verify();
    e3Control.verify();
    entryControl.verify();

    assertEquals(true, result);
    assertSame(entry, entries.get(1));
  }

  public void testReplaceScanElevation_noReplacement() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    MockControl e1Control = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry e1 = (CatalogEntry)e1Control.getMock();
    
    MockControl e2Control = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry e2 = (CatalogEntry)e2Control.getMock();
    
    MockControl e3Control = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry e3 = (CatalogEntry)e3Control.getMock();
    
    MockControl entryControl = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry entry = (CatalogEntry)entryControl.getMock();
    
    Time nominalTime = new Time(10, 0, 0);
    
    entries.add(e1);
    entries.add(e2);
    entries.add(e3);
    
    entry.getAttribute("/dataset1/what/starttime");
    entryControl.setReturnValue("100005");
    entry.getAttribute("/dataset1/where/elangle");
    entryControl.setReturnValue(new Double(0.5));
    
    e1.getAttribute("/dataset1/where/elangle");
    e1Control.setReturnValue(new Double(1.0));
    e2.getAttribute("/dataset1/where/elangle");
    e2Control.setReturnValue(new Double(0.5));
    e2.getAttribute("/dataset1/what/starttime");
    e2Control.setReturnValue("100004");
    
    e1Control.replay();
    e2Control.replay();
    e3Control.replay();
    entryControl.replay();
    
    boolean result = classUnderTest.replaceScanElevation(entries, entry, nominalTime);
    
    e1Control.verify();
    e2Control.verify();
    e3Control.verify();
    entryControl.verify();

    assertEquals(true, result);
    assertSame(e2, entries.get(1));
  }

  public void testReplaceScanElevation_noMatchingElevation() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    MockControl e1Control = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry e1 = (CatalogEntry)e1Control.getMock();
    
    MockControl e2Control = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry e2 = (CatalogEntry)e2Control.getMock();
    
    MockControl e3Control = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry e3 = (CatalogEntry)e3Control.getMock();
    
    MockControl entryControl = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry entry = (CatalogEntry)entryControl.getMock();
    
    Time nominalTime = new Time(10, 0, 0);
    
    entries.add(e1);
    entries.add(e2);
    entries.add(e3);
    
    entry.getAttribute("/dataset1/what/starttime");
    entryControl.setReturnValue("100005");
    entry.getAttribute("/dataset1/where/elangle");
    entryControl.setReturnValue(new Double(0.5));
    
    e1.getAttribute("/dataset1/where/elangle");
    e1Control.setReturnValue(new Double(1.0));
    e2.getAttribute("/dataset1/where/elangle");
    e2Control.setReturnValue(new Double(1.5));
    e3.getAttribute("/dataset1/where/elangle");
    e3Control.setReturnValue(new Double(2.5));
    
    e1Control.replay();
    e2Control.replay();
    e3Control.replay();
    entryControl.replay();
    
    boolean result = classUnderTest.replaceScanElevation(entries, entry, nominalTime);
    
    e1Control.verify();
    e2Control.verify();
    e3Control.verify();
    entryControl.verify();

    assertEquals(false, result);
    
    assertSame(e1, entries.get(0));
    assertSame(e2, entries.get(1));
    assertSame(e3, entries.get(2));
  }
  
  public void testFilterEntries() throws Exception {
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    List<CatalogEntry> filtered = new ArrayList<CatalogEntry>();
    MockControl e1Control = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry e1 = (CatalogEntry)e1Control.getMock();
    
    MockControl e2Control = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry e2 = (CatalogEntry)e2Control.getMock();
    
    MockControl e3Control = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry e3 = (CatalogEntry)e3Control.getMock();

    MockControl e4Control = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry e4 = (CatalogEntry)e4Control.getMock();

    MockControl methodsControl = MockControl.createControl(VolumeRuleMethods.class);
    methods = (VolumeRuleMethods)methodsControl.getMock();

    Time nominalTime = new Time(10,0,0);
    entries.add(e1);
    entries.add(e2);
    entries.add(e3);
    entries.add(e4);

    methods.createCatalogEntryList();
    methodsControl.setReturnValue(filtered);
    e1.getAttribute("/dataset1/where/elangle");
    e1Control.setReturnValue(new Double(0.5));
    e2.getAttribute("/dataset1/where/elangle");
    e2Control.setReturnValue(new Double(1.0));
    methods.replaceScanElevation(filtered, e2, nominalTime);
    methodsControl.setReturnValue(false);
    e3.getAttribute("/dataset1/where/elangle");
    e3Control.setReturnValue(new Double(2.0));
    e4.getAttribute("/dataset1/where/elangle");
    e4Control.setReturnValue(new Double(1.0));
    methods.replaceScanElevation(filtered, e4, nominalTime);
    methodsControl.setReturnValue(true);
    
    e1Control.replay();
    e2Control.replay();
    e3Control.replay();
    e4Control.replay();
    methodsControl.replay();
    
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

    e1Control.verify();
    e2Control.verify();
    e3Control.verify();
    e4Control.verify();
    methodsControl.verify();
    assertSame(filtered, result);
    assertEquals(1, result.size());
  }

  public void testAfterPropertiesSet() throws Exception {
    VolumeRule classUnderTest = new VolumeRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(utilities);
    classUnderTest.setTimeoutManager(timeoutManager);

    classUnderTest.afterPropertiesSet();
  }

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
  
  
  protected CatalogEntry createCatalogEntry(Double elangle) {
    MockControl entryControl = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry entry = (CatalogEntry)entryControl.getMock();
    entry.getAttribute("/dataset1/where/elangle");
    entryControl.setReturnValue(elangle, MockControl.ZERO_OR_MORE);
    entryControl.replay();
    return entry;
  }
  
  protected CatalogEntry createCatalogEntry(String source, String path) {
    MockControl entryControl = MockClassControl.createControl(CatalogEntry.class);
    CatalogEntry entry = (CatalogEntry)entryControl.getMock();
    entry.getSource();
    entryControl.setReturnValue(source, MockControl.ZERO_OR_MORE);
    entry.getPath();
    entryControl.setReturnValue(path, MockControl.ZERO_OR_MORE);
    entryControl.replay();
    return entry;
  }
}
