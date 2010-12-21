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

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.filters.PolarScanAngleFilter;
import eu.baltrad.beast.db.filters.TimeIntervalFilter;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.timer.TimeoutManager;
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
  
  private static interface VolumeRuleMethods {
    public VolumeTimerData createTimerData(IBltMessage message);
    public List<CatalogEntry> fetchEntries(DateTime nominalTime);
    public TimeIntervalFilter createFilter(DateTime nominalTime);
    public IBltMessage createMessage(DateTime nominalTime, List<CatalogEntry> entries);
    public DateTime getPreviousDateTime(DateTime now, String source);
    public List<Double> getPreviousElevationAngles(DateTime previousDateTime, String source);
    public boolean areCriteriasMet(List<CatalogEntry> entries);
    public DateTime getNominalTime(Date d, Time t);
    public void initialize();
  };

  public void setUp() throws Exception {
    methodsControl = MockControl.createControl(VolumeRuleMethods.class);
    methods = (VolumeRuleMethods)methodsControl.getMock();
    catalogControl = MockClassControl.createControl(Catalog.class);
    catalog = (Catalog)catalogControl.getMock();
    timeoutControl = MockClassControl.createControl(TimeoutManager.class);
    timeoutManager = (TimeoutManager)timeoutControl.getMock();
    classUnderTest = new VolumeRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setTimeout(0); // No timeout initially
  }

  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  protected void replay() {
    methodsControl.replay();
    catalogControl.replay();
    timeoutControl.replay();
  }

  protected void verify() {
    methodsControl.verify();
    catalogControl.verify();
    timeoutControl.verify();
  }
  
  public void testAreCriteriasMet_noPreviousScan_noHit() throws Exception {
    DateTime now = new DateTime();
    String source = "seang";
    List<Double> noAnglesList = new ArrayList<Double>();
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry(new Double(1.0)));
    entries.add(createCatalogEntry(new Double(5.0)));
    entries.add(createCatalogEntry(new Double(10.0)));
    
    classUnderTest = new VolumeRule() {
      protected DateTime getPreviousDateTime(DateTime now, String source) {
        return methods.getPreviousDateTime(now, source);
      }
      protected List<Double> getPreviousElevationAngles(DateTime time, String source) {
        return methods.getPreviousElevationAngles(time, source);
      }
    };
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setElevationMax(11.0);
    classUnderTest.setAscending(true);
    
    methods.getPreviousDateTime(now, source);
    methodsControl.setReturnValue(null);
    methods.getPreviousElevationAngles(null, source);
    methodsControl.setReturnValue(noAnglesList);

    replay();
    
    boolean result = classUnderTest.areCriteriasMet(entries, now, "seang");
    
    verify();
    assertEquals(false, result);
  }

  public void testAreCriteriasMet_noPreviousScan_hit() throws Exception {
    DateTime now = new DateTime();
    String source = "seang";
    List<Double> noAnglesList = new ArrayList<Double>();
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry(new Double(1.0)));
    entries.add(createCatalogEntry(new Double(5.0)));
    entries.add(createCatalogEntry(new Double(11.0)));

    classUnderTest = new VolumeRule() {
      protected DateTime getPreviousDateTime(DateTime now, String source) {
        return methods.getPreviousDateTime(now, source);
      }
      protected List<Double> getPreviousElevationAngles(DateTime time, String source) {
        return methods.getPreviousElevationAngles(time, source);
      }
    };
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setElevationMax(11.0);
    classUnderTest.setAscending(true);
    
    methods.getPreviousDateTime(now, source);
    methodsControl.setReturnValue(null);
    methods.getPreviousElevationAngles(null, source);
    methodsControl.setReturnValue(noAnglesList);

    replay();
    
    boolean result = classUnderTest.areCriteriasMet(entries, now, "seang");
    
    verify();
    assertEquals(true, result);
  }

  public void testAreCriteriasMet_previousScan() throws Exception {
    DateTime now = new DateTime();
    DateTime previousTime = new DateTime(2010,1,1,12,0,0);
    String source = "seang";
    List<Double> anglesList = new ArrayList<Double>();
    anglesList.add(new Double(1.0));
    anglesList.add(new Double(5.0));
    anglesList.add(new Double(10.0));
    
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    entries.add(createCatalogEntry(new Double(1.0)));
    entries.add(createCatalogEntry(new Double(5.0)));
    entries.add(createCatalogEntry(new Double(10.0)));
    
    classUnderTest = new VolumeRule() {
      protected DateTime getPreviousDateTime(DateTime now, String source) {
        return methods.getPreviousDateTime(now, source);
      }
      protected List<Double> getPreviousElevationAngles(DateTime time, String source) {
        return methods.getPreviousElevationAngles(time, source);
      }
    };
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setElevationMax(11.0);
    classUnderTest.setAscending(true);
    
    methods.getPreviousDateTime(now, source);
    methodsControl.setReturnValue(previousTime);
    methods.getPreviousElevationAngles(previousTime, source);
    methodsControl.setReturnValue(anglesList);

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

    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.createMessage(nt, entries);
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
  
  public void testCreateAngleFilter() {
    DateTime nt = new DateTime(2010,1,1,12,0,0);
    classUnderTest.setElevationMax(10.0);
    classUnderTest.setElevationMin(2.0);
    PolarScanAngleFilter result = classUnderTest.createAngleFilter(nt, "searl");
    assertEquals("searl", result.getSource());
    assertEquals(nt, result.getDateTime());
    assertEquals(PolarScanAngleFilter.ASCENDING, result.getSortOrder());
  }

  public void testCreatePreviousTimeFilter() {
    DateTime nt = new DateTime(2010,1,1,12,0,0);
    TimeIntervalFilter result = classUnderTest.createPreviousTimeFilter(nt, "searl");
    
    assertEquals(1, result.getLimit());
    assertEquals("SCAN", result.getObject());
    assertEquals("searl", result.getSource());
    assertEquals(null, result.getStartDateTime());
    assertEquals(nt, result.getStopDateTime());
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
