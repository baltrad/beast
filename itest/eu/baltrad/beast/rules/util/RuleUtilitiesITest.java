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
package eu.baltrad.beast.rules.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.fc.DateTime;


/**
 * @author Anders Henja
 */
public class RuleUtilitiesITest extends TestCase {
  private ApplicationContext context = null;
  private Catalog catalog = null;
  private BeastDBTestHelper helper = null;
  private RuleUtilities classUnderTest = null;
  
  private static String[] FIXTURES = {
    "fixtures/Z_SCAN_C_ESWI_20101016080000_searl_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_sease_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_selul_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_sevar_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080200_searl_000001.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_searl_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_selul_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_sevar_000000.h5"  
  };
  
  public RuleUtilitiesITest(String name) {
    super(name);
  }

  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    catalog = (Catalog)context.getBean("catalog");
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.createBaltradDbPath();
    helper.purgeBaltradDB();
    classUnderTest = new RuleUtilities();
    classUnderTest.setCatalog(catalog);
    
    for (String s: FIXTURES) {
      catalog.getCatalog().store(getFilePath(s));
    }
  }
  
  private String getFilePath(String resource) throws Exception {
    java.io.File f = new java.io.File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }
  
  public void tearDown() throws Exception {
    context = null;
    catalog = null;
    helper = null;
    classUnderTest = null;
  }
  
  public void testFetchLowestSourceElevationAngle() throws Exception {
    DateTime start = new DateTime(2010,10,16,8,0,0);
    DateTime stop = new DateTime(2010,10,16,8,5,0);
    List<String> sources = new ArrayList<String>();
    sources.add("searl");
    sources.add("selul");
    sources.add("sevar");
    
    Map<String, Double> result = classUnderTest.fetchLowestSourceElevationAngle(start, stop, sources);
    assertEquals(3, result.size());
    assertEquals(0.5, result.get("searl"));
    assertEquals(0.5, result.get("selul"));
    assertEquals(0.5, result.get("sevar"));
  }

  public void testFetchLowestSourceElevationAngle_otherInterval() throws Exception {
    DateTime start = new DateTime(2010,10,16,8,5,0);
    DateTime stop = new DateTime(2010,10,16,8,10,0);
    List<String> sources = new ArrayList<String>();
    sources.add("searl");
    sources.add("selul");
    sources.add("sevar");
    
    Map<String, Double> result = classUnderTest.fetchLowestSourceElevationAngle(start, stop, sources);
    assertEquals(3, result.size());
    assertEquals(0.5, result.get("searl"));
    assertEquals(0.5, result.get("selul"));
    assertEquals(0.5, result.get("sevar"));
  }
  
  public void testFetchLowestSourceElevationAngle_missingSources() throws Exception {
    DateTime start = new DateTime(2010,10,16,8,5,0);
    DateTime stop = new DateTime(2010,10,16,8,10,0);
    List<String> sources = new ArrayList<String>();
    sources.add("searl");
    sources.add("selul");
    sources.add("sevar");
    sources.add("sease");
    
    Map<String, Double> result = classUnderTest.fetchLowestSourceElevationAngle(start, stop, sources);
    assertEquals(3, result.size());
    assertEquals(0.5, result.get("searl"));
    assertEquals(0.5, result.get("selul"));
    assertEquals(0.5, result.get("sevar"));
  }
  
  public void testGetRadarSources() throws Exception {
    List<String> sources = classUnderTest.getRadarSources();
    assertTrue(sources.size() > 0);
    assertTrue(sources.contains("searl"));
  }
  

}
