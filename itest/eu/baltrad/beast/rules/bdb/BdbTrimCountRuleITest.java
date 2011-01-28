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
package eu.baltrad.beast.rules.bdb;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;
import eu.baltrad.fc.FileCatalog;
import eu.baltrad.fc.db.FileEntry;
import eu.baltrad.fc.db.FileResult;

public class BdbTrimCountRuleITest extends TestCase {
  private ApplicationContext context = null;
  private FileCatalog catalog = null;
  private BeastDBTestHelper helper = null;
  private BdbTrimCountRule classUnderTest = null;

  private static String[] FIXTURES = {
    "fixtures/Z_SCAN_C_ESWI_20101016080000_seang_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_searl_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_sease_000000.h5",
  };

  private static Map<String, String> fileUuidMap;

  public BdbTrimCountRuleITest(String name) {
    super(name);
    context = BeastDBTestHelper.loadContext(this);
    catalog = (FileCatalog)context.getBean("fc");
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.createBaltradDbPath();
  }

  public void setUp() throws Exception {
    helper.purgeBaltradDB();
    fileUuidMap = new HashMap<String, String>();

    classUnderTest = new BdbTrimCountRule();
    classUnderTest.setFileCatalog(catalog);

    for (String s: FIXTURES) {
      FileEntry e = catalog.store(getFilePath(s));
      fileUuidMap.put(s, e.uuid());
    }
  }

  private String getFilePath(String resource) throws Exception {
    java.io.File f = new java.io.File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }

  public void testHandle() throws Exception {
    classUnderTest.setFileCountLimit(1);
    classUnderTest.handle(new BltTriggerJobMessage());

    String sease_uuid = fileUuidMap.get("fixtures/Z_SCAN_C_ESWI_20101016080000_sease_000000.h5");
    
    FileResult rset = catalog.query_file().execute();
    try {
      assertTrue(rset.next());
      assertEquals(sease_uuid, rset.entry().uuid());
      assertFalse(rset.next());
    } finally {
      rset.delete();
    }
  }

  public void testHandleLimitNotMet() throws Exception {
    classUnderTest.setFileCountLimit(3);
    classUnderTest.handle(new BltTriggerJobMessage());

    FileResult rset = catalog.query_file().execute();
    try {
      assertEquals(3, rset.size());
    } finally {
      rset.delete();
    }
  }
}
