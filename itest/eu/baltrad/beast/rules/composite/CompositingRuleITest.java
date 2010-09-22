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
import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.timer.TimeoutManager;
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
  
  private static String[] FIXTURES = {
    "fixtures/pvol_seang_20090501T110100Z.h5",
    "fixtures/pvol_seang_20090501T111600Z.h5",
    "fixtures/pvol_searl_20090501T110100Z.h5",
    "fixtures/pvol_sease_20090501T110200Z.h5",
    "fixtures/pvol_sease_20090501T111600Z.h5",
    "fixtures/pvol_sehud_20090501T110100Z.h5"};
  
  public CompositingRuleITest(String name) {
    super(name);
    context = BeastDBTestHelper.loadContext(this);
    catalog = (Catalog)context.getBean("catalog");
    timeoutManager = (TimeoutManager)context.getBean("timeoutmanager");
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.createBaltradDbPath();
  }

  public void setUp() throws Exception {
    helper.purgeBaltradDB();

    classUnderTest = new CompositingRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setTimeoutManager(timeoutManager);
    classUnderTest.setTimeout(0); // no timeouts
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
  
  public void testHandle() throws Exception {
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
  
  protected BltDataMessage createDataMessage(File f) {
    BltDataMessage result = new BltDataMessage();
    result.setFile(f);
    return result;
  }
}
