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

import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.util.IRuleUtilities;
import eu.baltrad.fc.DateTime;
import eu.baltrad.fc.oh5.File;

/**
 * @author Anders Henja
 *
 */
public class CompositingRuleITest extends TestCase {
  private ApplicationContext context = null;
  private Catalog catalog = null;
  private CompositingRule classUnderTest = null;
  private BeastDBTestHelper helper = null;
  private TimeoutManager timeoutManager = null;
  private IRuleUtilities ruleutil = null;
  
  private static String[] FIXTURES = {
    "fixtures/pvol_seang_20090501T110100Z.h5",
    "fixtures/pvol_seang_20090501T111600Z.h5",
    "fixtures/pvol_searl_20090501T110100Z.h5",
    "fixtures/pvol_sease_20090501T110200Z.h5",
    "fixtures/pvol_sease_20090501T111600Z.h5",
    "fixtures/pvol_sehud_20090501T110100Z.h5"};
  
  private static String[] SCAN_DATA_0 = {
    "fixtures/Z_SCAN_C_ESWI_20101016080000_seang_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_searl_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_sease_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_sehud_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_sekir_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_sekkr_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_selek_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_selul_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_seosu_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_seovi_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_sevar_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_sevil_000000.h5"
  };

  private static String[] SCAN_DATA_1 = {
    "fixtures/Z_SCAN_C_ESWI_20101016080500_seang_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_searl_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_sease_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_sehud_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_sekir_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_sekkr_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_selek_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_selul_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_seosu_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_seovi_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_sevar_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_sevil_000000.h5"
  };
  
  
  public CompositingRuleITest(String name) {
    super(name);
    context = BeastDBTestHelper.loadContext(this);
    catalog = (Catalog)context.getBean("catalog");
    ruleutil = (IRuleUtilities)context.getBean("ruleutil");
    timeoutManager = (TimeoutManager)context.getBean("timeoutmanager");
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.createBaltradDbPath();
  }

  public void setUp() throws Exception {
    helper.purgeBaltradDB();

    classUnderTest = new CompositingRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleutil);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setTimeout(0); // no timeouts
    
    for (String s: SCAN_DATA_0) {
      catalog.getCatalog().catalog(getFilePath(s));
    }
  }
  
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  private String getFilePath(String resource) throws Exception {
    java.io.File f = new java.io.File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }
  
  protected boolean arrayContains(String[] arr, String value) {
    for (String x : arr) {
      if (x.equals(value)) {
        return true;
      }
    }
    return false;
  }
  
  public void testHandleVolume() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("seang");
    sources.add("searl");
    sources.add("sehud");
    
    classUnderTest.setArea("baltrad_composite");
    classUnderTest.setInterval(10);
    classUnderTest.setSources(sources);
    
    File f = catalog.getCatalog().catalog(getFilePath(FIXTURES[0]));
    IBltMessage result = classUnderTest.handle(createDataMessage(f));
    assertNull(result);
    
    f = catalog.getCatalog().catalog(getFilePath(FIXTURES[2]));
    result = classUnderTest.handle(createDataMessage(f));
    assertNull(result);

    f = catalog.getCatalog().catalog(getFilePath(FIXTURES[3]));
    result = classUnderTest.handle(createDataMessage(f));
    assertNull(result);

    f = catalog.getCatalog().catalog(getFilePath(FIXTURES[5]));
    result = classUnderTest.handle(createDataMessage(f));
    assertNotNull(result);
    
    BltGenerateMessage genmsg = (BltGenerateMessage)result;
    assertEquals("eu.baltrad.beast.GenerateComposite", genmsg.getAlgorithm());
    String[] files = genmsg.getFiles();
    assertEquals(3, files.length);
    assertTrue(arrayContains(files, helper.getBaltradDbPth() + "/Z_PVOL_C_ESWI_20090501110100_searl_000000.h5"));
    assertTrue(arrayContains(files, helper.getBaltradDbPth() + "/Z_PVOL_C_ESWI_20090501110100_sehud_000000.h5"));
    assertTrue(arrayContains(files, helper.getBaltradDbPth() + "/Z_PVOL_C_ESWI_20090501110100_seang_000000.h5"));
  }
  
  public void testHandleScans() throws Exception {
    File f = null;
    IBltMessage result = null;
    List<String> sources = new ArrayList<String>();
    sources.add("seang");
    sources.add("searl");
    sources.add("sease");
    
    classUnderTest.setArea("baltrad_composite");
    classUnderTest.setInterval(5);
    classUnderTest.setSources(sources);
    classUnderTest.setTimeout(10000);
    classUnderTest.setScanBased(true);
    
    f = catalog.getCatalog().catalog(getFilePath(SCAN_DATA_1[0]));
    result = classUnderTest.handle(createDataMessage(f));
    assertNull(result);
    
    f = catalog.getCatalog().catalog(getFilePath(SCAN_DATA_1[1]));
    result = classUnderTest.handle(createDataMessage(f));
    assertNull(result);
    
    f = catalog.getCatalog().catalog(getFilePath(SCAN_DATA_1[2]));
    result = classUnderTest.handle(createDataMessage(f));
    assertNotNull(result);
    
    BltGenerateMessage msg = (BltGenerateMessage)result;
    assertEquals("eu.baltrad.beast.GenerateComposite", msg.getAlgorithm());
  }
  
  public void XtestLowest() {
    DateTime start = new DateTime(2010,10,16,8,0,0);
    DateTime stop = new DateTime(2010,10,16,8,5,0);
    List<String> sources = new ArrayList<String>();
    sources.add("seang");
    sources.add("searl");
    sources.add("sease");
    
    List<CatalogEntry> entries = ruleutil.fetchLowestSourceElevationAngle(start, stop, sources);
    System.out.println("ENTRIES: " + entries.size());
  }
  
  protected BltDataMessage createDataMessage(File f) {
    BltDataMessage result = new BltDataMessage();
    result.setFile(f);
    return result;
  }
}
