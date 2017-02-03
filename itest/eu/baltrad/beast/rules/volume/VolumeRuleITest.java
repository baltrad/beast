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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.support.AbstractApplicationContext;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.beast.db.AttributeFilter;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.util.IRuleUtilities;
import junit.framework.TestCase;

/**
 * @author Anders Henja
 *
 */
public class VolumeRuleITest extends TestCase {
  private AbstractApplicationContext context = null;
  private VolumeRule classUnderTest = null;
  private Catalog catalog = null;
  private IRuleUtilities ruleUtilities = null;
  
  
  private static class HandleHelper {
    public List<String> triggeringFiles = new ArrayList<String>();
    public List<BltGenerateMessage> messages = new ArrayList<BltGenerateMessage>();
    public Map<String, String> filemap = new HashMap<String, String>();
  }
  
  private static String[] FIXTURES_1845 = {
    "fixtures/scan_sehud_0.5_20110126T184500Z.h5",
    "fixtures/scan_sehud_1.0_20110126T184600Z.h5",
    "fixtures/scan_sehud_1.5_20110126T184600Z.h5",
    "fixtures/scan_sehud_2.0_20110126T184600Z.h5",
    "fixtures/scan_sehud_2.5_20110126T184700Z.h5",
    "fixtures/scan_sehud_4.0_20110126T184700Z.h5",
    "fixtures/scan_sehud_8.0_20110126T184700Z.h5",
    "fixtures/scan_sehud_14.0_20110126T184700Z.h5",
    "fixtures/scan_sehud_24.0_20110126T184700Z.h5",
    "fixtures/scan_sehud_40.0_20110126T184700Z.h5",
    "fixtures/scan_sehud_0.5_20110126T185000Z.h5",
    "fixtures/scan_sehud_0.5_20110126T185500Z.h5"
  };
  
  private static String[] FIXTURES_1900 = {
    "fixtures/scan_sehud_0.5_20110126T190000Z.h5",
    "fixtures/scan_sehud_1.0_20110126T190100Z.h5",
    "fixtures/scan_sehud_1.5_20110126T190100Z.h5",
    "fixtures/scan_sehud_2.0_20110126T190100Z.h5",
    "fixtures/scan_sehud_2.5_20110126T190200Z.h5",
    "fixtures/scan_sehud_4.0_20110126T190200Z.h5",
    "fixtures/scan_sehud_8.0_20110126T190200Z.h5",
    "fixtures/scan_sehud_14.0_20110126T190200Z.h5",
    "fixtures/scan_sehud_24.0_20110126T190200Z.h5",
    "fixtures/scan_sehud_40.0_20110126T190200Z.h5",
    "fixtures/scan_sehud_0.5_20110126T190500Z.h5",
    "fixtures/scan_sehud_0.5_20110126T191000Z.h5"
  };
  
  private static String[] FIXTURES_1915 = {
    "fixtures/scan_sehud_0.5_20110126T191500Z.h5",
    "fixtures/scan_sehud_1.0_20110126T191600Z.h5",
    "fixtures/scan_sehud_1.5_20110126T191600Z.h5",
    "fixtures/scan_sehud_2.0_20110126T191600Z.h5",
    "fixtures/scan_sehud_2.5_20110126T191700Z.h5",
    "fixtures/scan_sehud_4.0_20110126T191700Z.h5",
    "fixtures/scan_sehud_8.0_20110126T191700Z.h5",
    "fixtures/scan_sehud_14.0_20110126T191700Z.h5",
    "fixtures/scan_sehud_24.0_20110126T191700Z.h5",
    "fixtures/scan_sehud_40.0_20110126T191700Z.h5",
    "fixtures/scan_sehud_0.5_20110126T192000Z.h5",
    "fixtures/scan_sehud_0.5_20110126T192500Z.h5"
  };
  
  private static String[] FIXTURES_1930 = {
    "fixtures/scan_sehud_0.5_20110126T193000Z.h5",
    "fixtures/scan_sehud_1.0_20110126T193100Z.h5",
    "fixtures/scan_sehud_1.5_20110126T193100Z.h5",
    "fixtures/scan_sehud_2.0_20110126T193100Z.h5",
    "fixtures/scan_sehud_2.5_20110126T193200Z.h5",
    "fixtures/scan_sehud_4.0_20110126T193200Z.h5",
    "fixtures/scan_sehud_8.0_20110126T193200Z.h5",
    "fixtures/scan_sehud_14.0_20110126T193200Z.h5",
    "fixtures/scan_sehud_24.0_20110126T193200Z.h5",
    "fixtures/scan_sehud_40.0_20110126T193200Z.h5",
    "fixtures/scan_sehud_0.5_20110126T193500Z.h5",
    "fixtures/scan_sehud_0.5_20110126T194000Z.h5"
  };  

  private static String[] FIXTURES_MIXED_LDR_ZDR = {
      "fixtures/scan_sehud_0.5_ldr_20110126T184500Z.h5",
      "fixtures/scan_sehud_0.5_zdr_20110126T184500Z.h5",
      "fixtures/scan_sehud_1.0_ldr_20110126T184500Z.h5",
      "fixtures/scan_sehud_1.0_zdr_20110126T184500Z.h5",
      "fixtures/scan_sehud_2.0_ldr_20110126T184600Z.h5",
      "fixtures/scan_sehud_2.0_zdr_20110126T184700Z.h5"
  };
  
  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    BeastDBTestHelper helper = (BeastDBTestHelper)context.getBean("helper");
    helper.tearDown();
    helper.purgeBaltradDB();
    
    catalog = (Catalog)context.getBean("catalog");
    ruleUtilities = (IRuleUtilities)context.getBean("ruleUtilities");
    
    TimeoutManager timeoutManager = (TimeoutManager)context.getBean("timeoutmanager");
    classUnderTest = new VolumeRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setRuleUtilities(ruleUtilities);
    classUnderTest.setTimeout(0);
    classUnderTest.setRuleId(10);
  }
  
  public void tearDown() throws Exception {
    catalog = null;
    classUnderTest = null;
    ruleUtilities = null;    
    context.close();
  }
  
  private String getFilePath(String resource) throws Exception {
    File f = new File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }
  
  protected BltDataMessage createDataMessage(FileEntry f) {
    BltDataMessage result = new BltDataMessage();
    result.setFileEntry(f);
    return result;
  }
  
  public void testHandle() throws Exception {
    String[] fixpaths = new String[FIXTURES_1900.length-2]; // The two final scans should not be used
    FileEntry f = null;
    IBltMessage result = null;
    classUnderTest.setElevationMin(0.0);
    classUnderTest.setElevationMax(40.0);

    HandleHelper h_1845 = storeFiles(classUnderTest, FIXTURES_1845);
    assertEquals(1, h_1845.triggeringFiles.size());
    assertEquals("fixtures/scan_sehud_40.0_20110126T184700Z.h5", h_1845.triggeringFiles.get(0));
    
    // We will trigger on 40.0 degrees. I.e.
    // scan_sehud_40.0_20110126T190200Z.h5
    // Then it will come two more scans that should not be included
    // in the volume. (0.5 degrees at 1905 and 1910).
    int len = FIXTURES_1900.length - 3;
    for (int i = 0; i < len; i++) {
      f = catalog.getCatalog().store(new FileInputStream(getFilePath(FIXTURES_1900[i])));
      fixpaths[i] = f.getUuid().toString(); //catalog.getCatalog().getLocalStorage().store(f).toString();
      result = classUnderTest.handle(createDataMessage(f));
      assertNull(result);
    }

    f = catalog.getCatalog().store(new FileInputStream(getFilePath(FIXTURES_1900[FIXTURES_1900.length - 3])));
    fixpaths[FIXTURES_1900.length - 3] = f.getUuid().toString(); //catalog.getCatalog().getLocalStorage().store(f).toString();
    result = classUnderTest.handle(createDataMessage(f));

    // And now, the two last scans will arrive and they should not trigger
    // a new volume generation.
    f = catalog.getCatalog().store(new FileInputStream(getFilePath(FIXTURES_1900[FIXTURES_1900.length - 2])));
    assertNull(classUnderTest.handle(createDataMessage(f)));

    f = catalog.getCatalog().store(new FileInputStream(getFilePath(FIXTURES_1900[FIXTURES_1900.length - 1])));
    assertNull(classUnderTest.handle(createDataMessage(f)));
    
    assertNotNull(result);
    assertTrue(result instanceof BltGenerateMessage);
    BltGenerateMessage gmsg = (BltGenerateMessage)result;
    
    assertEquals("eu.baltrad.beast.GenerateVolume", gmsg.getAlgorithm());
    String[] files = gmsg.getFiles();
    assertEquals(fixpaths.length, files.length);
    for (int i = 0; i < files.length; i++) {
      assertEquals(files[i], fixpaths[i]);
    }
    
    String[] args = gmsg.getArguments();
    assertEquals("--source=sehud", args[0]);
    assertEquals("--date=20110126", args[1]);
    assertEquals("--time=190000", args[2]);
  }
  
  public void testHandle_2() throws Exception {
    HandleHelper h_1845 = null;
    HandleHelper h_1900 = null;
    HandleHelper h_1915 = null;
    HandleHelper h_1930 = null;
    
    classUnderTest.setElevationMin(0.0);
    classUnderTest.setElevationMax(40.0);
    
    h_1845 = storeFiles(classUnderTest, FIXTURES_1845);
    h_1900 = storeFiles(classUnderTest, FIXTURES_1900);
    h_1915 = storeFiles(classUnderTest, FIXTURES_1915);
    h_1930 = storeFiles(classUnderTest, FIXTURES_1930);

    // Remove files that should not exist in the generated message
    assertNotNull(h_1845.filemap.remove(FIXTURES_1845[FIXTURES_1845.length-1]));
    assertNotNull(h_1845.filemap.remove(FIXTURES_1845[FIXTURES_1845.length-2]));
    assertNotNull(h_1900.filemap.remove(FIXTURES_1900[FIXTURES_1900.length-1]));
    assertNotNull(h_1900.filemap.remove(FIXTURES_1900[FIXTURES_1900.length-2]));
    assertNotNull(h_1915.filemap.remove(FIXTURES_1915[FIXTURES_1915.length-1]));
    assertNotNull(h_1915.filemap.remove(FIXTURES_1915[FIXTURES_1915.length-2]));
    assertNotNull(h_1930.filemap.remove(FIXTURES_1930[FIXTURES_1930.length-1]));
    assertNotNull(h_1930.filemap.remove(FIXTURES_1930[FIXTURES_1930.length-2]));

    assertEquals(1, h_1845.triggeringFiles.size());
    assertEquals(1, h_1845.messages.size());
    assertEquals("fixtures/scan_sehud_40.0_20110126T184700Z.h5", h_1845.triggeringFiles.get(0));
    String[] expected_1845_args = new String[]{"--source=sehud", "--date=20110126", "--time=184500", "--algorithm_id=10-sehud", "--merge=true"};
    validateMessage(h_1845.messages.get(0), expected_1845_args, h_1845.filemap.values());
    
    assertEquals(1, h_1900.triggeringFiles.size());
    assertEquals(1, h_1900.messages.size());
    assertEquals("fixtures/scan_sehud_40.0_20110126T190200Z.h5", h_1900.triggeringFiles.get(0));
    String[] expected_1900_args = new String[]{"--source=sehud", "--date=20110126", "--time=190000", "--algorithm_id=10-sehud", "--merge=true"};
    validateMessage(h_1900.messages.get(0), expected_1900_args, h_1900.filemap.values());

    assertEquals(1, h_1915.triggeringFiles.size());
    assertEquals(1, h_1915.messages.size());
    assertEquals("fixtures/scan_sehud_40.0_20110126T191700Z.h5", h_1915.triggeringFiles.get(0));
    String[] expected_1915_args = new String[]{"--source=sehud", "--date=20110126", "--time=191500", "--algorithm_id=10-sehud", "--merge=true"};
    validateMessage(h_1915.messages.get(0), expected_1915_args, h_1915.filemap.values());

    assertEquals(1, h_1930.triggeringFiles.size());
    assertEquals(1, h_1930.messages.size());
    assertEquals("fixtures/scan_sehud_40.0_20110126T193200Z.h5", h_1930.triggeringFiles.get(0));
    String[] expected_1930_args = new String[]{"--source=sehud", "--date=20110126", "--time=193000", "--algorithm_id=10-sehud", "--merge=true"};
    validateMessage(h_1930.messages.get(0), expected_1930_args, h_1930.filemap.values());
  }

  public void testHandle_3() throws Exception {
    HandleHelper h_1845 = null;
    HandleHelper h_1930 = null;
    
    classUnderTest.setElevationMin(0.0);
    classUnderTest.setElevationMax(20.0);
    
    h_1845 = storeFiles(classUnderTest, FIXTURES_1845);
    storeFiles(classUnderTest, FIXTURES_1900);
    storeFiles(classUnderTest, FIXTURES_1915);
    h_1930 = storeFiles(classUnderTest, FIXTURES_1930);

    // Remove files that should not exist in the generated message
    assertNotNull(h_1845.filemap.remove(FIXTURES_1845[FIXTURES_1845.length-1]));
    assertNotNull(h_1845.filemap.remove(FIXTURES_1845[FIXTURES_1845.length-2]));
    assertNotNull(h_1845.filemap.remove(FIXTURES_1845[FIXTURES_1845.length-3]));
    assertNotNull(h_1845.filemap.remove(FIXTURES_1845[FIXTURES_1845.length-4])); // 24.0 deg should not be included

    assertNotNull(h_1930.filemap.remove(FIXTURES_1930[FIXTURES_1930.length-1]));
    assertNotNull(h_1930.filemap.remove(FIXTURES_1930[FIXTURES_1930.length-2]));
    assertNotNull(h_1930.filemap.remove(FIXTURES_1930[FIXTURES_1930.length-3]));
    assertNotNull(h_1930.filemap.remove(FIXTURES_1930[FIXTURES_1930.length-4])); // 24.0 deg should not be included

    assertEquals(1, h_1845.triggeringFiles.size());
    assertEquals(1, h_1845.messages.size());
    assertEquals("fixtures/scan_sehud_24.0_20110126T184700Z.h5", h_1845.triggeringFiles.get(0));
    String[] expected_1845_args = new String[]{"--source=sehud", "--date=20110126", "--time=184500", "--algorithm_id=10-sehud", "--merge=true"};
    validateMessage(h_1845.messages.get(0), expected_1845_args, h_1845.filemap.values());

    assertEquals(1, h_1930.triggeringFiles.size());
    assertEquals(1, h_1930.messages.size());
    assertEquals("fixtures/scan_sehud_24.0_20110126T193200Z.h5", h_1930.triggeringFiles.get(0));
    String[] expected_1930_args = new String[]{"--source=sehud", "--date=20110126", "--time=193000", "--algorithm_id=10-sehud", "--merge=true"};
    validateMessage(h_1930.messages.get(0), expected_1930_args, h_1930.filemap.values());
  }  
  
  public void testHandleNoDuplicateAngles() throws Exception {
    HandleHelper h_1845 = null;
    FileEntry f = null;
    String path = null;
    Map<String, String> filemap = new HashMap<String, String>();
    
    classUnderTest.setElevationMin(0.0);
    classUnderTest.setElevationMax(40.0);
    
    h_1845 = storeFiles(classUnderTest, FIXTURES_1845);
    assertEquals(1, h_1845.triggeringFiles.size());
    
    // Now we should populate the database with all 1930 entries except the
    // final one.
    for (int i = 0; i < FIXTURES_1900.length - 1; i++) {
      f = catalog.getCatalog().store(new FileInputStream(getFilePath(FIXTURES_1900[i])));
      path = f.getUuid().toString(); //catalog.getCatalog().getLocalStorage().store(f).toString();
      filemap.put(FIXTURES_1900[i], path);
    }
    
    f = catalog.getCatalog().store(new FileInputStream(getFilePath(FIXTURES_1900[FIXTURES_1900.length-1])));
    path = f.getUuid().toString(); //catalog.getCatalog().getLocalStorage().store(f).toString();
    filemap.put(FIXTURES_1900[FIXTURES_1900.length-1], path);
    
    BltGenerateMessage msg = (BltGenerateMessage)classUnderTest.handle(createDataMessage(f));
    assertNotNull(msg);
    // We should not have the two duplicate 0.5 angles
    assertEquals(FIXTURES_1900.length-2, msg.getFiles().length);
    // Since it is ascending strategy, it is the 0.5 degree file closest to nominal time that should be used
    assertTrue(containsFile(msg.getFiles(), filemap.get(FIXTURES_1900[0])));
    assertTrue(containsFile(msg.getFiles(), filemap.get(FIXTURES_1900[1])));
    assertTrue(containsFile(msg.getFiles(), filemap.get(FIXTURES_1900[2])));
    assertTrue(containsFile(msg.getFiles(), filemap.get(FIXTURES_1900[3])));
    assertTrue(containsFile(msg.getFiles(), filemap.get(FIXTURES_1900[4])));
    assertTrue(containsFile(msg.getFiles(), filemap.get(FIXTURES_1900[5])));
    assertTrue(containsFile(msg.getFiles(), filemap.get(FIXTURES_1900[6])));
    assertTrue(containsFile(msg.getFiles(), filemap.get(FIXTURES_1900[7])));
    assertTrue(containsFile(msg.getFiles(), filemap.get(FIXTURES_1900[8])));
    assertTrue(containsFile(msg.getFiles(), filemap.get(FIXTURES_1900[9])));
  }
  
  public void testHandle_ascendingDescendingScans() throws Exception {
    classUnderTest.setElevationAngles("0.5,1.0,1.5,2.0,2.5,4.0,8.0,14.0,24.0,40.0");
    
    HandleHelper h_1845 = storeFiles(classUnderTest, FIXTURES_1845);
    assertEquals(1, h_1845.triggeringFiles.size());
    assertEquals("fixtures/scan_sehud_40.0_20110126T184700Z.h5", h_1845.triggeringFiles.get(0));
    
    // This is not a completely accurate way to test that ascending-descending scans are handled
    // properly since the datetimes are out of sync but it should be enough for verifying that we
    // are able to handle it.
    HandleHelper h_1900 = storeFiles(classUnderTest, FIXTURES_1900, true, -2);
    assertEquals(1, h_1900.triggeringFiles.size());
    assertEquals("fixtures/scan_sehud_0.5_20110126T190000Z.h5", h_1900.triggeringFiles.get(0));
  }

  public void testHandle_onlyLDR() throws Exception {
    AttributeFilter filter = new AttributeFilter();
    filter.setAttribute("how/task");
    filter.setValueType(AttributeFilter.ValueType.STRING);
    filter.setValue("ldr");
    filter.setOperator(AttributeFilter.Operator.EQ);
    classUnderTest.setFilter(filter);
    classUnderTest.setElevationMax(2.0);

    HandleHelper h = storeFiles(classUnderTest, FIXTURES_MIXED_LDR_ZDR);
    assertEquals(1, h.triggeringFiles.size());
    assertEquals(FIXTURES_MIXED_LDR_ZDR[4], h.triggeringFiles.get(0));
    BltGenerateMessage m = h.messages.get(0);
    assertEquals(3, m.getFiles().length);
    assertTrue(containsFile(m.getFiles(), h.filemap.get(FIXTURES_MIXED_LDR_ZDR[0])));
    assertTrue(containsFile(m.getFiles(), h.filemap.get(FIXTURES_MIXED_LDR_ZDR[2])));
    assertTrue(containsFile(m.getFiles(), h.filemap.get(FIXTURES_MIXED_LDR_ZDR[4])));
  }

  public void testHandle_onlyZDR() throws Exception {
    AttributeFilter filter = new AttributeFilter();
    filter.setAttribute("how/task");
    filter.setValueType(AttributeFilter.ValueType.STRING);
    filter.setValue("zdr");
    filter.setOperator(AttributeFilter.Operator.EQ);
    classUnderTest.setFilter(filter);
    classUnderTest.setElevationMax(2.0);

    HandleHelper h = storeFiles(classUnderTest, FIXTURES_MIXED_LDR_ZDR);
    assertEquals(1, h.triggeringFiles.size());
    assertEquals(FIXTURES_MIXED_LDR_ZDR[5], h.triggeringFiles.get(0));
    BltGenerateMessage m = h.messages.get(0);
    assertEquals(3, m.getFiles().length);
    assertTrue(containsFile(m.getFiles(), h.filemap.get(FIXTURES_MIXED_LDR_ZDR[1])));
    assertTrue(containsFile(m.getFiles(), h.filemap.get(FIXTURES_MIXED_LDR_ZDR[3])));
    assertTrue(containsFile(m.getFiles(), h.filemap.get(FIXTURES_MIXED_LDR_ZDR[5])));
  }
  
  protected void storeOneFile(VolumeRule testclass, HandleHelper handled, String filename) throws Exception  {
    FileEntry f = catalog.getCatalog().store(new FileInputStream(getFilePath(filename)));
    String path = f.getUuid().toString();
    handled.filemap.put(filename, path);
    BltGenerateMessage msg = (BltGenerateMessage)testclass.handle(createDataMessage(f));
    if (msg != null) {
      handled.triggeringFiles.add(filename);
      handled.messages.add(msg);
    }
  }
  
  protected HandleHelper storeFiles(VolumeRule testclass, String[] fixtures, boolean reverse, int startpos) throws Exception {
    HandleHelper handled = new HandleHelper();

    if (reverse) {
      for (int i = fixtures.length-1+startpos; i >= 0; i--) {
        storeOneFile(testclass, handled, fixtures[i]);
      }
    } else {
      for (String fname : fixtures) {
        storeOneFile(testclass, handled, fname);
      }
    }
    return handled;
  }

  protected HandleHelper storeFiles(VolumeRule testclass, String[] fixtures) throws Exception {
    return storeFiles(testclass, fixtures, false, 0);
  }
  
  protected void validateMessage(BltGenerateMessage msg, String[] args, Collection<String> values) {
    assertEquals(args.length, msg.getArguments().length);
    for (int i = 0; i < args.length; i++) {
      assertEquals(args[i], msg.getArguments()[i]);
    }
    
    assertEquals(values.size(), msg.getFiles().length);
    String[] msgfiles = msg.getFiles();
    for (String file : msgfiles) {
      assertTrue(values.contains(file));
    }
  }
  
  protected boolean containsFile(String[] files, String expected) {
    for (String f : files) {
      if (f.equals(expected)) {
        return true;
      }
    }
    return false;
  }
}
