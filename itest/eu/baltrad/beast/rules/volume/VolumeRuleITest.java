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

import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.fc.db.FileEntry;
import junit.framework.TestCase;

/**
 * @author Anders Henja
 *
 */
public class VolumeRuleITest extends TestCase {
  private VolumeRule classUnderTest = null;
  private Catalog catalog = null;
  
  private static String[] FIXTURES = {
    "fixtures/ODIM_H5_scan_seang_20090501T120000Z_1.h5", // elangle 0.5
    "fixtures/ODIM_H5_scan_seang_20090501T120000Z_2.h5", // elangle 1.0
    "fixtures/ODIM_H5_scan_seang_20090501T120000Z_3.h5", // elangle 1.5
    "fixtures/ODIM_H5_scan_seang_20090501T120000Z_4.h5", // elangle 2.0
    "fixtures/ODIM_H5_scan_seang_20090501T120000Z_5.h5", // elangle 2.5
    "fixtures/ODIM_H5_scan_seang_20090501T120000Z_6.h5", // elangle 4.0
    "fixtures/ODIM_H5_scan_seang_20090501T120000Z_7.h5", // elangle 8.0
    "fixtures/ODIM_H5_scan_seang_20090501T120000Z_8.h5", // elangle 14.0
    "fixtures/ODIM_H5_scan_seang_20090501T120000Z_9.h5", // elangle 24.0
    "fixtures/ODIM_H5_scan_seang_20090501T120000Z_10.h5" // elangle 40.0
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
    TimeoutManager timeoutManager = (TimeoutManager)context.getBean("timeoutmanager");
    classUnderTest = new VolumeRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
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
    classUnderTest.setElevationMin(0);
    classUnderTest.setElevationMax(24.0);
    
    FileEntry f = catalog.getCatalog().store(getFilePath(FIXTURES[0]));
    String fixture0_path = catalog.getCatalog().storage().store(f);
    IBltMessage result = classUnderTest.handle(createDataMessage(f));
    assertNull(result);
    
    f = catalog.getCatalog().store(getFilePath(FIXTURES[1]));
    String fixture1_path = catalog.getCatalog().storage().store(f);
    result = classUnderTest.handle(createDataMessage(f));
    assertNull(result);

    f = catalog.getCatalog().store(getFilePath(FIXTURES[2]));
    String fixture2_path = catalog.getCatalog().storage().store(f);
    result = classUnderTest.handle(createDataMessage(f));
    assertNull(result);
    
    f = catalog.getCatalog().store(getFilePath(FIXTURES[3]));
    String fixture3_path = catalog.getCatalog().storage().store(f);
    result = classUnderTest.handle(createDataMessage(f));
    assertNull(result);

    f = catalog.getCatalog().store(getFilePath(FIXTURES[9]));
    result = classUnderTest.handle(createDataMessage(f));
    assertNotNull(result);
    assertTrue(result instanceof BltGenerateMessage);
    BltGenerateMessage gmsg = (BltGenerateMessage)result;
    
    assertEquals("eu.baltrad.beast.GenerateVolume", gmsg.getAlgorithm());
    String[] files = gmsg.getFiles();
    assertEquals(4, files.length);
    assertEquals(fixture0_path, files[0]);
    assertEquals(fixture1_path, files[1]);
    assertEquals(fixture2_path, files[2]);
    assertEquals(fixture3_path, files[3]);
    
    String[] args = gmsg.getArguments();
    assertEquals("--source=seang", args[0]);
    assertEquals("--date=20090501", args[1]);
    assertEquals("--time=120000", args[2]);
  }
}
