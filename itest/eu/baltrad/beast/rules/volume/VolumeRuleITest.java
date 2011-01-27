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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.util.IRuleUtilities;
import eu.baltrad.fc.db.FileEntry;
import junit.framework.TestCase;

/**
 * @author Anders Henja
 *
 */
public class VolumeRuleITest extends TestCase {
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
  
  public VolumeRuleITest(String name) {
    super(name);
  }

  public void setUp() throws Exception {
    ApplicationContext context = BeastDBTestHelper.loadContext(this);
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
  }
  
  public void tearDown() throws Exception {
    catalog = null;
    classUnderTest = null;
  }
  
  private String getFilePath(String resource) throws Exception {
    java.io.File f = new java.io.File(this.getClass().getResource(resource).getFile());
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
      f = catalog.getCatalog().store(getFilePath(FIXTURES_1900[i]));
      fixpaths[i] = catalog.getCatalog().storage().store(f);
      result = classUnderTest.handle(createDataMessage(f));
      assertNull(result);
    }

    f = catalog.getCatalog().store(getFilePath(FIXTURES_1900[FIXTURES_1900.length - 3]));
    fixpaths[FIXTURES_1900.length - 3] = catalog.getCatalog().storage().store(f);
    result = classUnderTest.handle(createDataMessage(f));

    // And now, the two last scans will arrive and they should not trigger
    // a new volume generation.
    f = catalog.getCatalog().store(getFilePath(FIXTURES_1900[FIXTURES_1900.length - 2]));
    assertNull(classUnderTest.handle(createDataMessage(f)));

    f = catalog.getCatalog().store(getFilePath(FIXTURES_1900[FIXTURES_1900.length - 1]));
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
    String[] expected_1845_args = new String[]{"--source=sehud", "--date=20110126", "--time=184500"};
    validateMessage(h_1845.messages.get(0), expected_1845_args, h_1845.filemap.values());
    
    assertEquals(1, h_1900.triggeringFiles.size());
    assertEquals(1, h_1900.messages.size());
    assertEquals("fixtures/scan_sehud_40.0_20110126T190200Z.h5", h_1900.triggeringFiles.get(0));
    String[] expected_1900_args = new String[]{"--source=sehud", "--date=20110126", "--time=190000"};
    validateMessage(h_1900.messages.get(0), expected_1900_args, h_1900.filemap.values());

    assertEquals(1, h_1915.triggeringFiles.size());
    assertEquals(1, h_1915.messages.size());
    assertEquals("fixtures/scan_sehud_40.0_20110126T191700Z.h5", h_1915.triggeringFiles.get(0));
    String[] expected_1915_args = new String[]{"--source=sehud", "--date=20110126", "--time=191500"};
    validateMessage(h_1915.messages.get(0), expected_1915_args, h_1915.filemap.values());

    assertEquals(1, h_1930.triggeringFiles.size());
    assertEquals(1, h_1930.messages.size());
    assertEquals("fixtures/scan_sehud_40.0_20110126T193200Z.h5", h_1930.triggeringFiles.get(0));
    String[] expected_1930_args = new String[]{"--source=sehud", "--date=20110126", "--time=193000"};
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
    String[] expected_1845_args = new String[]{"--source=sehud", "--date=20110126", "--time=184500"};
    validateMessage(h_1845.messages.get(0), expected_1845_args, h_1845.filemap.values());

    assertEquals(1, h_1930.triggeringFiles.size());
    assertEquals(1, h_1930.messages.size());
    assertEquals("fixtures/scan_sehud_24.0_20110126T193200Z.h5", h_1930.triggeringFiles.get(0));
    String[] expected_1930_args = new String[]{"--source=sehud", "--date=20110126", "--time=193000"};
    validateMessage(h_1930.messages.get(0), expected_1930_args, h_1930.filemap.values());
  }  
  
  protected HandleHelper storeFiles(VolumeRule testclass, String[] fixtures) throws Exception {
    HandleHelper handled = new HandleHelper();
    
    for (String fname: fixtures) {
      FileEntry f = catalog.getCatalog().store(getFilePath(fname));
      String path = catalog.getCatalog().storage().store(f);
      handled.filemap.put(fname, path);
      BltGenerateMessage msg = (BltGenerateMessage)testclass.handle(createDataMessage(f));
      if (msg != null) {
        handled.triggeringFiles.add(fname);
        handled.messages.add(msg);
      }
    }
    return handled;
  }
  
  protected void validateMessage(BltGenerateMessage msg, String[] args, Collection<String> values) {
    assertEquals(msg.getArguments().length, args.length);
    for (int i = 0; i < args.length; i++) {
      assertEquals(msg.getArguments()[i], args[i]);
    }
    
    assertEquals(msg.getFiles().length, values.size());
    String[] msgfiles = msg.getFiles();
    for (String file : msgfiles) {
      assertTrue(values.contains(file));
    }
  }
}
