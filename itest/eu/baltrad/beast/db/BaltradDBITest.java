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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.fc.FileCatalog;
import eu.baltrad.fc.FileSystemError;
import eu.baltrad.fc.db.AttributeQuery;
import eu.baltrad.fc.db.AttributeResult;
import eu.baltrad.fc.db.FileEntry;
import eu.baltrad.fc.expr.Expression;
import eu.baltrad.fc.expr.ExpressionFactory;

/**
 * @author Anders Henja
 *
 */
public class BaltradDBITest extends TestCase {
  private BeastDBTestHelper helper = null;
  private FileCatalog catalogue = null;
  private String baltradDbPath = null;
  private Map<String, String> uuidMap = null;
  
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
    ApplicationContext context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("helper");
    baltradDbPath = helper.getBaltradDbPth();
    catalogue = new FileCatalog(helper.getBaltradDbUri(), baltradDbPath);
    uuidMap = new HashMap<String, String>();
  }
  
  private String getFilePath(String resource) throws Exception {
    java.io.File f = new java.io.File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }
  
  public void setUp() throws Exception {
    helper.purgeBaltradDB();
    long startTime = System.currentTimeMillis();
    for (String n : FIXTURES) {
      FileEntry result = catalogue.store(getFilePath(n));
      assertNotNull(result);
      uuidMap.put(n, result.uuid());
    }
    System.out.println("Catalogued " + FIXTURES.length + " files in " + (System.currentTimeMillis() - startTime) + "ms");
  }
  
  public void tearDown() throws Exception {
    catalogue = null;
  }
  
  public void testLoadWithNonExistingPath() {
    try {
      new FileCatalog(helper.getBaltradDbUri(), "/mr/yoda");
      fail("Expected FileSystemError");
    } catch (FileSystemError fse) {
      // pass
    }
  }
  
  public void test_find_seang() throws Exception {
    AttributeQuery q = catalogue.query_attribute();
    ExpressionFactory xpr = new ExpressionFactory();
    q.fetch(xpr.attribute("file:uuid"));
    q.filter(xpr.attribute("what/source:node").eq(xpr.string("seang")));
    AttributeResult rs = q.execute();
    assertEquals(1, rs.size());
    rs.next();
    String result = rs.string(0);
    rs.delete();
    String seang_uuid = (String)uuidMap.get("fixtures/pvol_seang_20090501T120000Z.h5");
    assertEquals(seang_uuid, result);
  }

  public void test_find_all() throws Exception {
    AttributeQuery q = catalogue.query_attribute();
    ExpressionFactory xpr = new ExpressionFactory();
    Set<String> result = new HashSet<String>();
    
    q.fetch(xpr.attribute("what/source:node"));

    AttributeResult rs = q.execute();
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
    AttributeQuery q = catalogue.query_attribute();
    ExpressionFactory xpr = new ExpressionFactory();
    
    q.fetch(xpr.attribute("what/source:node"));
    Expression e1 = xpr.attribute("what/source:node").eq(xpr.string("sekir"));
    Expression e2 = xpr.attribute("what/source:node").eq(xpr.string("selul"));
    q.filter(xpr.or_(e1, e2));
    AttributeResult rs = q.execute();
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
    AttributeQuery q = catalogue.query_attribute();
    ExpressionFactory xpr = new ExpressionFactory();
    Set<String> result = new HashSet<String>();
    
    q.fetch(xpr.attribute("what/source:node"));
    q.filter(xpr.attribute("what/source:node").ne(xpr.string("sekir")));
    AttributeResult rs = q.execute();
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
    AttributeQuery q = catalogue.query_attribute();
    ExpressionFactory xpr = new ExpressionFactory();
    Set<Double> result = new HashSet<Double>();
    
    q.fetch(xpr.attribute("what/source:node"));
    q.fetch(xpr.attribute("where/elangle"));
    q.filter(xpr.attribute("what/source:node").eq(xpr.string("searl")));
    q.filter(xpr.attribute("where/elangle").between(xpr.double_(-1.0), xpr.double_(5.0)));
    
    AttributeResult rs = q.execute();
    assertEquals(6, rs.size());
    while (rs.next()) {
      assertEquals("searl", rs.string(0));
      result.add(rs.double_(1));
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
