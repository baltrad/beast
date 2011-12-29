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

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.context.support.AbstractApplicationContext;

import eu.baltrad.bdb.FileCatalog;
import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.db.FileQuery;
import eu.baltrad.bdb.db.FileResult;
import eu.baltrad.bdb.util.DateTime;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;

public class BdbTrimAgeRuleITest extends TestCase {
  private AbstractApplicationContext context = null;
  private FileCatalog catalog = null;
  private BeastDBTestHelper helper = null;
  private BdbTrimAgeRule classUnderTest = null;

  private MockControl methodsControl = null;
  private BdbTrimAgeRuleMethods methods = null;

  private static interface BdbTrimAgeRuleMethods {
    public DateTime getCurrentDateTime();
  }

  private static String[] FIXTURES = {
    "fixtures/Z_SCAN_C_ESWI_20101016080000_seang_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_searl_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080000_sease_000000.h5",
    "fixtures/Z_SCAN_C_ESWI_20101016080500_seang_000000.h5",
  };

  private static Map<String, UUID> fileUuidMap;

  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    catalog = (FileCatalog)context.getBean("fc");
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.createBaltradDbPath();

    helper.purgeBaltradDB();

    classUnderTest = new BdbTrimAgeRule();
    classUnderTest.setFileCatalog(catalog);

    methodsControl = MockControl.createControl(BdbTrimAgeRuleMethods.class);
    methods = (BdbTrimAgeRuleMethods)methodsControl.getMock();

    fileUuidMap = new HashMap<String, UUID>();

    for (String s: FIXTURES) {
      FileEntry e = catalog.store(new FileInputStream(getFilePath(s)));
      fileUuidMap.put(s, e.getUuid());
    }
  }

  public void tearDown() throws Exception {
    catalog = null;
    helper = null;
    classUnderTest = null;
    methodsControl = null;
    methods = null;
    fileUuidMap = null;
    context.close();
  }
  
  private String getFilePath(String resource) throws Exception {
    java.io.File f = new java.io.File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }

  public void testHandle() throws Exception {
    classUnderTest = new BdbTrimAgeRule() {
      protected DateTime getCurrentDateTime() {
        return methods.getCurrentDateTime();
      }
    };
    classUnderTest.setFileCatalog(catalog);
    classUnderTest.setFileAgeLimit(300);
    
    methods.getCurrentDateTime();
    methodsControl.setReturnValue(new DateTime(2010, 10, 16, 8, 6, 0));
    methodsControl.replay();

    classUnderTest.handle(new BltTriggerJobMessage());
    UUID uuid = fileUuidMap.get("fixtures/Z_SCAN_C_ESWI_20101016080500_seang_000000.h5");
    FileResult rset = catalog.getDatabase().execute(new FileQuery());
    try {
      assertEquals(1, rset.size());
      assertTrue(rset.next());
      assertEquals(uuid, rset.getFileEntry().getUuid());
    } finally {
      rset.close();
    }
  }
}
