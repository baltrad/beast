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

import org.springframework.context.support.AbstractApplicationContext;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.fc.db.Database;
import eu.baltrad.fc.db.AttributeQuery;
import eu.baltrad.fc.db.AttributeResult;
import eu.baltrad.fc.db.FileEntry;
import eu.baltrad.fc.expr.Expression;
import eu.baltrad.fc.expr.ExpressionFactory;
import eu.baltrad.fc.oh5.hl.HlFile;

/**
 * @author Anders Henja
 *
 */
public class BaltradDBITest extends TestCase {
  private AbstractApplicationContext context = null;
  private BeastDBTestHelper helper = null;
  private Database db = null;
  private String baltradDbPath = null;
  private Map<String, String> uuidMap = null;
  
  private static String[] FIXTURES = {
    "fixtures/Z_SCAN_C_ESWI_20101023180000_seang_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101023180000_searl_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101023180000_sease_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101023180000_sehud_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101023180000_sekir_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101023180000_sekkr_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101023180000_selek_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101023180000_selul_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101023180000_seosu_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101023180000_seovi_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101023180000_sevar_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101023180000_sevil_000000.h5"    
  };
  
  private String getFilePath(String resource) throws Exception {
    java.io.File f = new java.io.File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }
  
  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("helper");
    baltradDbPath = helper.getBaltradDbPth();
    db = Database.create(helper.getBaltradDbUri());
    uuidMap = new HashMap<String, String>();
    helper.purgeBaltradDB();
    long startTime = System.currentTimeMillis();
    for (String n : FIXTURES) {
      HlFile f = new HlFile(getFilePath(n));
      FileEntry result = db.store(f);
      assertNotNull(result);
      uuidMap.put(n, result.uuid());
    }
    System.out.println("Catalogued " + FIXTURES.length + " files in " + (System.currentTimeMillis() - startTime) + "ms");
  }
  
  public void tearDown() throws Exception {
    db.delete();
    helper = null;
    uuidMap = null;
    baltradDbPath = null;
    context.close();
  }
  
  public void test_find_seang() throws Exception {
    AttributeQuery q = new AttributeQuery();
    ExpressionFactory xpr = new ExpressionFactory();
    q.fetch("uuid", xpr.attribute("file:uuid"));
    q.filter(xpr.attribute("what/source:_name").eq(xpr.string("seang")));
    AttributeResult rs = db.execute(q);
    assertEquals(1, rs.size());
    rs.next();
    String result = rs.string("uuid");
    rs.delete();
    String seang_uuid = (String)uuidMap.get("fixtures/Z_SCAN_C_ESWI_20101023180000_seang_000000.h5");
    assertEquals(seang_uuid, result);
  }

  public void test_find_all() throws Exception {
    AttributeQuery q = new AttributeQuery();
    ExpressionFactory xpr = new ExpressionFactory();
    Set<String> result = new HashSet<String>();
    
    q.fetch("uuid", xpr.attribute("what/source:_name"));

    AttributeResult rs = db.execute(q);
    assertEquals(12, rs.size());
    while (rs.next()) {
      result.add(rs.string("uuid"));
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
    AttributeQuery q = new AttributeQuery();
    ExpressionFactory xpr = new ExpressionFactory();
    
    q.fetch("source", xpr.attribute("what/source:_name"));
    Expression e1 = xpr.attribute("what/source:_name").eq(xpr.string("sekir"));
    Expression e2 = xpr.attribute("what/source:_name").eq(xpr.string("selul"));
    q.filter(xpr.or_(e1, e2));
    AttributeResult rs = db.execute(q);
    assertEquals(2, rs.size());
    rs.next();
    String result1 = rs.string("source");
    rs.next();
    String result2 = rs.string("source");
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
    AttributeQuery q = new AttributeQuery();
    ExpressionFactory xpr = new ExpressionFactory();
    Set<String> result = new HashSet<String>();
    
    q.fetch("source", xpr.attribute("what/source:_name"));
    q.filter(xpr.attribute("what/source:_name").ne(xpr.string("sekir")));
    AttributeResult rs = db.execute(q);
    assertEquals(11, rs.size());
    while (rs.next()) {
      result.add(rs.string("source"));
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
}
