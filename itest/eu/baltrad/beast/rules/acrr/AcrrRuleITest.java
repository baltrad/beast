/* --------------------------------------------------------------------
Copyright (C) 2009-2013 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.rules.acrr;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.springframework.context.support.AbstractApplicationContext;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.rules.util.IRuleUtilities;
import junit.framework.TestCase;

/**
 * @author Anders Henja
 *
 */
public class AcrrRuleITest extends TestCase {
  private AbstractApplicationContext context = null;
  private AcrrRule classUnderTest = null;
  private Catalog catalog = null;
  private IRuleUtilities ruleUtilities = null;

  private static String[] FIXTURES = {
    "fixtures/acrr_fixture_20100101100000.h5",
    "fixtures/acrr_fixture_20100101101500.h5",
    "fixtures/acrr_fixture_20100101103000.h5",
    "fixtures/acrr_fixture_20100101104500.h5",
    "fixtures/acrr_fixture_20100101110000.h5",
    "fixtures/acrr_fixture_20100101111500.h5",
    "fixtures/acrr_fixture_20100101113000.h5",
    "fixtures/acrr_fixture_20100101114500.h5",
    "fixtures/acrr_fixture_20100101120000.h5",
    "fixtures/acrr_fixture_20100101121500.h5",
    "fixtures/acrr_fixture_20100101110000_dup.h5"
  };
  
  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    BeastDBTestHelper helper = (BeastDBTestHelper)context.getBean("helper");
    helper.tearDown();
    helper.purgeBaltradDB();
    
    catalog = (Catalog)context.getBean("catalog");
    ruleUtilities = (IRuleUtilities)context.getBean("ruleUtilities");
    
    classUnderTest = new AcrrRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtilities);
    
    for (int i = 0; i < FIXTURES.length; i++) {
      FileEntry f = catalog.getCatalog().store(new FileInputStream(getFilePath(FIXTURES[i])));
    }
  }
  
  public void tearDown() throws Exception {
    catalog = null;
    classUnderTest = null;
    ruleUtilities = null;    
    context.close();
  }

  public void testFindFiles() throws Exception {
//    for (int i = 0; i < FIXTURES.length; i++) {
//      FileEntry f = catalog.getCatalog().store(new FileInputStream(getFilePath(FIXTURES[i])));
//    }
    classUnderTest.setArea("testgmaps_2000");
    classUnderTest.setObjectType("IMAGE");
    classUnderTest.setFilesPerHour(4);
    classUnderTest.setHours(2);
    
    List<CatalogEntry> entries = classUnderTest.findFiles(new DateTime(2010,01,01,12,00,00));
    // The returned files should be in order and we should always get the last stored one
    assertEquals(9, entries.size());
    assertEquals(new DateTime(2010,01,01,10,00,00), entries.get(0).getDateTime());
    assertEquals(new DateTime(2010,01,01,10,15,00), entries.get(1).getDateTime());
    assertEquals(new DateTime(2010,01,01,10,30,00), entries.get(2).getDateTime());
    assertEquals(new DateTime(2010,01,01,10,45,00), entries.get(3).getDateTime());
    assertEquals(new DateTime(2010,01,01,11,00,00), entries.get(4).getDateTime());
    assertEquals(1.1, (Double)entries.get(4).getAttribute("/dataset1/data1/what/gain"), 4);
    assertEquals(new DateTime(2010,01,01,11,15,00), entries.get(5).getDateTime());
    assertEquals(new DateTime(2010,01,01,11,30,00), entries.get(6).getDateTime());
    assertEquals(new DateTime(2010,01,01,11,45,00), entries.get(7).getDateTime());
    assertEquals(new DateTime(2010,01,01,12,00,00), entries.get(8).getDateTime());
  }
  
  public void testFindFiles_border() throws Exception {
    classUnderTest.setArea("testgmaps_2000");
    classUnderTest.setObjectType("IMAGE");
    classUnderTest.setFilesPerHour(4);
    classUnderTest.setHours(2);

    List<CatalogEntry> entries = classUnderTest.findFiles(new DateTime(2010,01,01,12,14,00));
    assertEquals(9, entries.size());
    assertEquals(new DateTime(2010,01,01,10,00,00), entries.get(0).getDateTime());
    assertEquals(new DateTime(2010,01,01,12,00,00), entries.get(8).getDateTime());
    
    entries = classUnderTest.findFiles(new DateTime(2010,01,01,12,15,00));
    assertEquals(9, entries.size());
    assertEquals(new DateTime(2010,01,01,10,15,00), entries.get(0).getDateTime());
    assertEquals(new DateTime(2010,01,01,12,15,00), entries.get(8).getDateTime());

    entries = classUnderTest.findFiles(new DateTime(2010,01,01,11,59,00));
    assertEquals(8, entries.size());
    assertEquals(new DateTime(2010,01,01,10,00,00), entries.get(0).getDateTime());
    assertEquals(new DateTime(2010,01,01,11,45,00), entries.get(7).getDateTime());
  }
  
  private String getFilePath(String resource) throws Exception {
    File f = new File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }
}
