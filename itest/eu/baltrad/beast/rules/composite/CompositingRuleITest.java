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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.support.AbstractApplicationContext;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.beast.db.AttributeFilter;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CombinedFilter;
import eu.baltrad.beast.db.CombinedFilter.MatchType;
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
public class CompositingRuleITest extends TestCase {
  private AbstractApplicationContext context = null;
  private Catalog catalog = null;
  private CompositingRule classUnderTest = null;
  private BeastDBTestHelper helper = null;
  private TimeoutManager timeoutManager = null;
  private IRuleUtilities ruleutil = null;
  
  private static String[] FIXTURES = {
    "fixtures/Z_PVOL_C_ESWI_20101023180100_seang.h5",
    "fixtures/Z_PVOL_C_ESWI_20101023181600_seang.h5",
    "fixtures/Z_PVOL_C_ESWI_20101023180100_searl.h5",
    "fixtures/Z_PVOL_C_ESWI_20101023180200_sease.h5",
    "fixtures/Z_PVOL_C_ESWI_20101023181600_sease.h5",
    "fixtures/Z_PVOL_C_ESWI_20101023180100_sehud.h5"};
  
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
  }

  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    catalog = (Catalog)context.getBean("catalog");
    ruleutil = (IRuleUtilities)context.getBean("ruleutil");
    timeoutManager = (TimeoutManager)context.getBean("timeoutmanager");
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.createBaltradDbPath();

    helper.purgeBaltradDB();

    classUnderTest = new CompositingRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleutil);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setTimeout(0); // no timeouts
    
    for (String s: SCAN_DATA_0) {
      catalog.getCatalog().store(new FileInputStream(getFilePath(s)));
    }
  }
  
  public void tearDown() throws Exception {
    catalog = null;
    classUnderTest = null;
    helper = null;
    timeoutManager = null;
    ruleutil = null;    
    context.close();
  }
  
  private String getFilePath(String resource) throws Exception {
    File f = new File(this.getClass().getResource(resource).getFile());
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
    
    FileEntry f = catalog.getCatalog().store(new FileInputStream(getFilePath(FIXTURES[0])));
    String seang_path = f.getUuid().toString();
    IBltMessage result = classUnderTest.handle(createDataMessage(f));
    assertNull(result);
    
    f = catalog.getCatalog().store(new FileInputStream(getFilePath(FIXTURES[2])));
    String searl_path = f.getUuid().toString();
    result = classUnderTest.handle(createDataMessage(f));
    assertNull(result);

    f = catalog.getCatalog().store(new FileInputStream(getFilePath(FIXTURES[3])));
    result = classUnderTest.handle(createDataMessage(f));
    assertNull(result);

    f = catalog.getCatalog().store(new FileInputStream(getFilePath(FIXTURES[5])));
    String sehud_path = f.getUuid().toString();
    result = classUnderTest.handle(createDataMessage(f));
    assertNotNull(result);
    
    BltGenerateMessage genmsg = (BltGenerateMessage)result;
    assertEquals("eu.baltrad.beast.GenerateComposite", genmsg.getAlgorithm());
    String[] files = genmsg.getFiles();
    assertEquals(3, files.length);
    assertTrue(arrayContains(files, searl_path));
    assertTrue(arrayContains(files, sehud_path));
    assertTrue(arrayContains(files, seang_path));
  }
  
  public void testHandleScans() throws Exception {
    FileEntry f = null;
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
    
    f = catalog.getCatalog().store(new FileInputStream(getFilePath(SCAN_DATA_1[0])));
    result = classUnderTest.handle(createDataMessage(f));
    assertNull(result);
    
    f = catalog.getCatalog().store(new FileInputStream(getFilePath(SCAN_DATA_1[1])));
    result = classUnderTest.handle(createDataMessage(f));
    assertNull(result);
    
    f = catalog.getCatalog().store(new FileInputStream(getFilePath(SCAN_DATA_1[2])));
    result = classUnderTest.handle(createDataMessage(f));
    assertNotNull(result);
    
    BltGenerateMessage msg = (BltGenerateMessage)result;
    assertEquals("eu.baltrad.beast.GenerateComposite", msg.getAlgorithm());
  }

  public void testHandle_alreadyHandled() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("seang");
    sources.add("searl");
    sources.add("sease");

    classUnderTest.setArea("baltrad_composite");
    classUnderTest.setInterval(5);
    classUnderTest.setSources(sources);
    classUnderTest.setTimeout(10000);
    classUnderTest.setScanBased(true);

    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023181000_seang_000000.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023181000_searl_000000.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023181000_sease_000000.h5")));

    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023181500_seang_000000.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023181500_searl_000000.h5")));
    // We have data in previous time period so a composite should be generated
    assertNotNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023181500_sease_000000.h5")));

    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023181600_seang_000000.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023181600_searl_000000.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023181600_sease_000000.h5")));

    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023182000_seang_000000.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023182000_searl_000000.h5")));
    // Next 5 minute interval is here, create a composite for this time period
    assertNotNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023182000_sease_000000.h5")));
  }
  
  public void testHandle_filterMalfuncScans() throws Exception {
    AttributeFilter filter = new AttributeFilter();
    filter.setAttribute("how/malfunc");
    filter.setValueType(AttributeFilter.ValueType.STRING);
    filter.setValue("True");
    filter.setOperator(AttributeFilter.Operator.NE);
    classUnderTest.setFilter(filter);
    
    List<String> sources = new ArrayList<String>();
    sources.add("seang");
    sources.add("searl");
    sources.add("sease");

    classUnderTest.setArea("baltrad_composite");
    classUnderTest.setInterval(5);
    classUnderTest.setSources(sources);
    classUnderTest.setTimeout(10000);
    classUnderTest.setScanBased(true);
    
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023181000_seang_000000.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023181000_searl_000000.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023181000_sease_000000.h5")));
    // next period
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023181500_seang_000000.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023181500_searl_malfunc_000000.h5")));
    // Since the scan above was filtered out, due to malfunc, no composite should be generated when adding sease below
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023181500_sease_000000.h5")));
    // First when adding the none-malfunc scan for searl, a composite shall be generated
    assertNotNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20101023181500_searl_000000.h5")));

    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20170202080000_searl_000000.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20170202080000_sease_malfunc_000000.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20170202080000_seang_000000.h5")));
    // next period - since sease was filtered out last period, it is sufficient to receive searl and seang in the next to create a composite
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20170202080500_searl_000000.h5")));
    assertNotNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/Z_SCAN_C_ESWI_20170202080500_seang_000000.h5")));
    
  }
  
  public void testHandle_filterPvolAngles() throws Exception {
    AttributeFilter attrFilter1 = new AttributeFilter();
    attrFilter1.setAttribute("where/elangle");
    attrFilter1.setValueType(AttributeFilter.ValueType.DOUBLE);
    attrFilter1.setValue("0.7");
    attrFilter1.setOperator(AttributeFilter.Operator.LT);
    
    AttributeFilter attrFilter2 = new AttributeFilter();
    attrFilter2.setAttribute("where/elangle");
    attrFilter2.setValueType(AttributeFilter.ValueType.DOUBLE);
    attrFilter2.setValue("25.0");
    attrFilter2.setOperator(AttributeFilter.Operator.GT);
    
    CombinedFilter combinedFilter = new CombinedFilter();
    combinedFilter.addChildFilter(attrFilter1);
    combinedFilter.addChildFilter(attrFilter2);
    combinedFilter.setMatchType(MatchType.ALL);
    
    classUnderTest.setFilter(combinedFilter);
    
    List<String> sources = new ArrayList<String>();
    sources.add("sease");
    sources.add("sekir");
    sources.add("sevil");
    sources.add("seang");

    classUnderTest.setArea("baltrad_composite");
    classUnderTest.setInterval(5);
    classUnderTest.setSources(sources);
    classUnderTest.setTimeout(10000);
    classUnderTest.setScanBased(false);
    
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/sease_pvol_20170203T010000Z.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/sekkr_pvol_20170203T010000Z.h5"))); // not in sources list
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/sekir_pvol_20170203T010000Z.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/seang_pvol_no05angle_20170203T010000Z.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/seang_pvol_no40angle_20170203T010000Z.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/sevil_pvol_20170203T010000Z.h5")));
    
    // since only three of the sources in the source-list were received with correct angles in the previous period, 
    // it is enough to receive those three sources in the next
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/sease_pvol_20170203T010500Z.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/sevil_pvol_no05angle_20170203T010500Z.h5")));
    assertNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/sekir_pvol_20170203T010500Z.h5")));
    // no composite generated for volume above, since sevil was filtered out. Need to add sevil volume the pass filter below
    assertNotNull(catalogAndHandle(classUnderTest, getFilePath("fixtures/sevil_pvol_20170203T010500Z.h5")));
    
  }
  
  protected BltDataMessage createDataMessage(FileEntry f) {
    BltDataMessage result = new BltDataMessage();
    result.setFileEntry(f);
    return result;
  }
  
  protected IBltMessage catalogAndHandle(CompositingRule rule, String path) throws Exception {
    FileEntry f = catalog.getCatalog().store(new FileInputStream(path));
    return rule.handle(createDataMessage(f));
  }
}
