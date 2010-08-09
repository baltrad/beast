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
package eu.baltrad.beast.db;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.fc.FileCatalog;
import eu.baltrad.fc.Query;
import eu.baltrad.fc.ResultSet;
import eu.baltrad.fc.expr.Expression;
import eu.baltrad.fc.expr.ExpressionFactory;
import eu.baltrad.fc.oh5.File;

/**
 * @author Anders Henja
 *
 */
public class BaltradDBITest extends TestCase {
  private ApplicationContext context = null;
  private BeastDBTestHelper helper = null;
  private FileCatalog catalogue = null;
  private String baltradDbPath = null;
  
  private static String[] FIXTURES = {
    "fixtures/pvol_seang_20090501T120000Z.h5",
    "fixtures/pvol_searl_20090501T120000Z.h5",
    "fixtures/pvol_sease_20090501T120000Z.h5",
    "fixtures/pvol_sehud_20090501T120000Z.h5",
    "fixtures/pvol_sekir_20090501T120000Z.h5",
    "fixtures/pvol_sekkr_20090501T120000Z.h5",
    "fixtures/pvol_selek_20090501T120000Z.h5",
    "fixtures/pvol_selul_20090501T120000Z.h5",
    "fixtures/pvol_seosu_20090501T120000Z.h5",
    "fixtures/pvol_seovi_20090501T120000Z.h5",
    "fixtures/pvol_sevar_20090501T120000Z.h5",
    "fixtures/pvol_sevil_20090501T120000Z.h5"
  };
  
  public BaltradDBITest(String name) {
    super(name);
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    baltradDbPath = helper.getBaltradDbPth();
    catalogue = new FileCatalog(helper.getBaltradDbUri(), baltradDbPath);
  }
  
  private String getFilePath(String resource) throws Exception {
    java.io.File f = new java.io.File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }
  
  public void setUp() throws Exception {
    helper.purgeBaltradDB();
    long startTime = System.currentTimeMillis();
    for (String n : FIXTURES) {
      File result = catalogue.catalog(getFilePath(n));
      assertNotNull(result);
    }
    System.out.println("Catalogued " + FIXTURES.length + " files in " + (System.currentTimeMillis() - startTime) + "ms");
  }
  
  public void tearDown() throws Exception {
    catalogue = null;
  }
  
  public void XtestLoadWithNonExistingPath() {
    catalogue = new FileCatalog(helper.getBaltradDbUri(), "/mr/yoda");
  }
  
  public void test_find_seang() throws Exception {
    Query q = catalogue.query();
    ExpressionFactory xpr = new ExpressionFactory();
    q.fetch(xpr.attribute("file:path"));
    q.filter(xpr.attribute("what/source:node").eq(xpr.string("seang")));
    ResultSet rs = q.execute();
    assertEquals(1, rs.size());
    rs.next();
    String result = rs.string(0);
    rs.delete();
    assertEquals(baltradDbPath+"/Z_PVOL_C_ESWI_20090501120000_seang_000000.h5", result);
  }

  public void test_find_all() throws Exception {
    Query q = catalogue.query();
    ExpressionFactory xpr = new ExpressionFactory();
    Set<String> result = new HashSet<String>();
    
    q.fetch(xpr.attribute("what/source:node"));

    ResultSet rs = q.execute();
    assertEquals(12, rs.size());
    while (rs.next()) {
      result.add(rs.string(0));
    }
    rs.delete();
    assertTrue(result.contains("sekir"));
    assertTrue(result.contains("selul"));
    assertTrue(result.contains("seosu"));
    assertTrue(result.contains("seovi"));
    assertTrue(result.contains("sehud"));
    assertTrue(result.contains("selek"));
    assertTrue(result.contains("searl"));
    assertTrue(result.contains("sease"));
    assertTrue(result.contains("sevil"));
    assertTrue(result.contains("sevar"));
    assertTrue(result.contains("seang"));
    assertTrue(result.contains("sekkr"));
  }
  
  public void test_find_sekir_or_selul() throws Exception {
    Query q = catalogue.query();
    ExpressionFactory xpr = new ExpressionFactory();
    
    q.fetch(xpr.attribute("what/source:node"));
    Expression e1 = xpr.attribute("what/source:node").eq(xpr.string("sekir"));
    Expression e2 = xpr.attribute("what/source:node").eq(xpr.string("selul"));
    q.filter(xpr.or_(e1, e2));
    ResultSet rs = q.execute();
    assertEquals(2, rs.size());
    rs.next();
    String result1 = rs.string(0);
    rs.next();
    String result2 = rs.string(0);
    rs.delete();
    if (result1.equals("sekir")) {
      assertEquals("selul", result2);
    } else if (result1.equals("selul")) {
      assertEquals("sekir", result2);
    } else {
      fail("Expected to get sekir and selul");
    }
  }
  
  public void test_find_all_but_sekir() throws Exception {
    Query q = catalogue.query();
    ExpressionFactory xpr = new ExpressionFactory();
    Set<String> result = new HashSet<String>();
    
    q.fetch(xpr.attribute("what/source:node"));
    q.filter(xpr.attribute("what/source:node").ne(xpr.string("sekir")));
    ResultSet rs = q.execute();
    assertEquals(11, rs.size());
    while (rs.next()) {
      result.add(rs.string(0));
    }
    rs.delete();
    assertTrue(result.contains("selul"));
    assertTrue(result.contains("seosu"));
    assertTrue(result.contains("seovi"));
    assertTrue(result.contains("sehud"));
    assertTrue(result.contains("selek"));
    assertTrue(result.contains("searl"));
    assertTrue(result.contains("sease"));
    assertTrue(result.contains("sevil"));
    assertTrue(result.contains("sevar"));
    assertTrue(result.contains("seang"));
    assertTrue(result.contains("sekkr"));
  }

  public void test_find_elangles_searl() throws Exception {
    Query q = catalogue.query();
    ExpressionFactory xpr = new ExpressionFactory();
    Set<Double> result = new HashSet<Double>();
    
    q.fetch(xpr.attribute("what/source:node"));
    q.fetch(xpr.attribute("where/elangle"));
    q.filter(xpr.attribute("what/source:node").eq(xpr.string("searl")));
    q.filter(xpr.attribute("where/elangle").between(xpr.real(-1.0), xpr.real(5.0)));
    
    ResultSet rs = q.execute();
    assertEquals(6, rs.size());
    while (rs.next()) {
      assertEquals("searl", rs.string(0));
      result.add(rs.real(1));
    }
    rs.delete();
    assertTrue(result.contains(0.5));
    assertTrue(result.contains(1.0));
    assertTrue(result.contains(1.5));
    assertTrue(result.contains(2.0));
    assertTrue(result.contains(2.5));
    assertTrue(result.contains(4.0));
  }  
}
