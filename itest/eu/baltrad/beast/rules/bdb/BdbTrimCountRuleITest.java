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

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import org.springframework.context.support.AbstractApplicationContext;

import eu.baltrad.bdb.FileCatalog;
import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.db.FileQuery;
import eu.baltrad.bdb.db.FileResult;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;

public class BdbTrimCountRuleITest extends TestCase {
  private AbstractApplicationContext context = null;
  private FileCatalog catalog = null;
  private BeastDBTestHelper helper = null;
  private BdbTrimCountRule classUnderTest = null;

  private static String[] FIXTURES = {
    "fixtures/Z_SCAN_C_ESWI_20101016080000_seang_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_searl_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_sease_000000.h5",
  };

  private static Map<String, UUID> fileUuidMap;

  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    catalog = (FileCatalog)context.getBean("fc");
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.createBaltradDbPath();    
    helper.purgeBaltradDB();
    fileUuidMap = new HashMap<String, UUID>();

    classUnderTest = new BdbTrimCountRule();
    classUnderTest.setFileCatalog(catalog);

    for (String s: FIXTURES) {
      FileEntry e = catalog.store(new FileInputStream(getFilePath(s)));
      // sleep for a second. Storage timestamp in BDB is with second precision
      Thread.sleep(1000); 
      fileUuidMap.put(s, e.getUuid());
    }
  }

  public void tearDown() throws Exception {
    catalog = null;
    helper = null;
    classUnderTest = null;
    fileUuidMap = null;
    context.close();
  }
  
  private File getFilePath(String resource) throws Exception {
    File f = new File(this.getClass().getResource(resource).getFile());
    return f.getAbsoluteFile();
  }

  public void testHandle() throws Exception {
    classUnderTest.setFileCountLimit(1);
    classUnderTest.handle(new BltTriggerJobMessage());

    UUID sease_uuid = fileUuidMap.get("fixtures/Z_SCAN_C_ESWI_20101016080000_sease_000000.h5");
    
    FileResult rset = catalog.getDatabase().execute(new FileQuery());
    try {
      assertTrue(rset.next());
      assertEquals(sease_uuid, rset.getFileEntry().getUuid());
      assertFalse(rset.next());
    } finally {
      rset.close();
    }
  }

  public void testHandleLimitNotMet() throws Exception {
    classUnderTest.setFileCountLimit(3);
    classUnderTest.handle(new BltTriggerJobMessage());

    FileResult rset = catalog.getDatabase().execute(new FileQuery());
    try {
      assertEquals(3, rset.size());
    } finally {
      rset.close();
    }
  }
}
