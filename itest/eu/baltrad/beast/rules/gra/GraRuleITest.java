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

package eu.baltrad.beast.rules.gra;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;
import eu.baltrad.beast.rules.acrr.AcrrRule;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class GraRuleITest  extends TestCase {
  private AbstractApplicationContext context = null;
  private GraRule classUnderTest = null;
  private Catalog catalog = null;
  private IRuleUtilities ruleUtilities = null;

  private static String[] FIXTURES = {
    "fixtures/gra_fixture_20100101100000.h5",
    "fixtures/gra_fixture_20100101101500.h5",
    "fixtures/gra_fixture_20100101103000.h5",
    "fixtures/gra_fixture_20100101104500.h5",
    "fixtures/gra_fixture_20100101110000.h5",
    "fixtures/gra_fixture_20100101111500.h5",
    "fixtures/gra_fixture_20100101113000.h5",
    "fixtures/gra_fixture_20100101114500.h5",
    "fixtures/gra_fixture_20100101120000.h5",
    "fixtures/gra_fixture_20100101121500.h5",
    "fixtures/gra_fixture_20100101110000_dup.h5"
  };
  
  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    BeastDBTestHelper helper = (BeastDBTestHelper)context.getBean("helper");
    helper.tearDown();
    helper.purgeBaltradDB();
    
    catalog = (Catalog)context.getBean("catalog");
    ruleUtilities = (IRuleUtilities)context.getBean("ruleUtilities");
    
    classUnderTest = new GraRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtilities);
    
    for (int i = 0; i < FIXTURES.length; i++) {
      FileEntry f = catalog.getCatalog().store(new FileInputStream(getFilePath(FIXTURES[i])));
      //System.out.println("Stored file : " + f.getUuid().toString());
    }
  }
  
  public void tearDown() throws Exception {
    catalog = null;
    classUnderTest = null;
    ruleUtilities = null;    
    context.close();
  }

  public void testHandle() throws Exception {
    classUnderTest = new GraRule() {
      protected DateTime getNowDT() {
        return new DateTime(2010,1,1,11,30);
      }
    };
    classUnderTest.setArea("testgmaps_2000");
    classUnderTest.setZrA(10.0);
    classUnderTest.setZrB(2.0);
    classUnderTest.setObjectType("IMAGE");
    classUnderTest.setFilesPerHour(4);
    classUnderTest.setHours(2);
    classUnderTest.setFirstTermUTC(11);
    classUnderTest.setInterval(1);
    classUnderTest.setAcceptableLoss(50);
    classUnderTest.setQuantity("DBZH");
    classUnderTest.setDistancefield("se.smhi.rave.somedistance");

    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtilities);
    
    
    BltTriggerJobMessage msg = new BltTriggerJobMessage();

    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(msg);
    
    Map<DateTime, FileEntry> entries = new HashMap<DateTime, FileEntry>();
    String[] files = result.getFiles();
    assertEquals(5, files.length);
    for (String s : files) {
      FileEntry fe = catalog.getCatalog().getDatabase().getFileEntry(UUID.fromString(s));
      entries.put(new DateTime(fe.getMetadata().getWhatDate(), fe.getMetadata().getWhatTime()), fe);
    }
    assertTrue(entries.containsKey(new DateTime(2010,01,01,10,00,00)));
    assertTrue(entries.containsKey(new DateTime(2010,01,01,10,15,00)));
    assertTrue(entries.containsKey(new DateTime(2010,01,01,10,30,00)));
    assertTrue(entries.containsKey(new DateTime(2010,01,01,10,45,00)));
    assertTrue(entries.containsKey(new DateTime(2010,01,01,11,00,00)));
    
    assertEquals("eu.baltrad.beast.CreateGraCoefficient", result.getAlgorithm());
    
    String[] arguments = result.getArguments();
    List<String> arglist = Arrays.asList(arguments);
    assertEquals(10, arguments.length);
    assertTrue(arglist.contains("--area=testgmaps_2000"));
    assertTrue(arglist.contains("--date=20100101"));
    assertTrue(arglist.contains("--time=110000"));
    assertTrue(arglist.contains("--zra=10.0"));
    assertTrue(arglist.contains("--zrb=2.0"));
    assertTrue(arglist.contains("--hours=2"));
    assertTrue(arglist.contains("--N=9"));
    assertTrue(arglist.contains("--accept=50"));
    assertTrue(arglist.contains("--quantity=DBZH"));
    assertTrue(arglist.contains("--distancefield=se.smhi.rave.somedistance"));
  }

  public void testHandle_2() throws Exception {
    classUnderTest = new GraRule() {
      protected DateTime getNowDT() {
        return new DateTime(2010,1,1,12,05);
      }
    };
    classUnderTest.setArea("testgmaps_2000");
    classUnderTest.setZrA(10.0);
    classUnderTest.setZrB(2.0);
    classUnderTest.setObjectType("IMAGE");
    classUnderTest.setFilesPerHour(4);
    classUnderTest.setHours(2);
    classUnderTest.setFirstTermUTC(11);
    classUnderTest.setInterval(1);
    classUnderTest.setAcceptableLoss(50);
    classUnderTest.setQuantity("DBZH");
    classUnderTest.setDistancefield("se.smhi.rave.somedistance");

    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtilities);
    
    
    BltTriggerJobMessage msg = new BltTriggerJobMessage();

    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(msg);
    
    Map<DateTime, FileEntry> entries = new HashMap<DateTime, FileEntry>();
    String[] files = result.getFiles();
    assertEquals(9, files.length);
    for (String s : files) {
      FileEntry fe = catalog.getCatalog().getDatabase().getFileEntry(UUID.fromString(s));
      entries.put(new DateTime(fe.getMetadata().getWhatDate(), fe.getMetadata().getWhatTime()), fe);
    }
    assertTrue(entries.containsKey(new DateTime(2010,01,01,10,00,00)));
    assertTrue(entries.containsKey(new DateTime(2010,01,01,10,15,00)));
    assertTrue(entries.containsKey(new DateTime(2010,01,01,10,30,00)));
    assertTrue(entries.containsKey(new DateTime(2010,01,01,10,45,00)));
    assertTrue(entries.containsKey(new DateTime(2010,01,01,11,00,00)));
    assertTrue(entries.containsKey(new DateTime(2010,01,01,11,15,00)));
    assertTrue(entries.containsKey(new DateTime(2010,01,01,11,30,00)));
    assertTrue(entries.containsKey(new DateTime(2010,01,01,11,45,00)));
    assertTrue(entries.containsKey(new DateTime(2010,01,01,12,00,00)));
    
    assertEquals("eu.baltrad.beast.CreateGraCoefficient", result.getAlgorithm());
    
    String[] arguments = result.getArguments();
    List<String> arglist = Arrays.asList(arguments);
    assertEquals(10, arguments.length);
    assertTrue(arglist.contains("--area=testgmaps_2000"));
    assertTrue(arglist.contains("--date=20100101"));
    assertTrue(arglist.contains("--time=120000"));
    assertTrue(arglist.contains("--zra=10.0"));
    assertTrue(arglist.contains("--zrb=2.0"));
    assertTrue(arglist.contains("--hours=2"));
    assertTrue(arglist.contains("--N=9"));
    assertTrue(arglist.contains("--accept=50"));
    assertTrue(arglist.contains("--quantity=DBZH"));
    assertTrue(arglist.contains("--distancefield=se.smhi.rave.somedistance"));
  }

  public void testHandle_3() throws Exception {
    classUnderTest = new GraRule() {
      protected DateTime getNowDT() {
        return new DateTime(2010,1,1,11,00);
      }
    };
    classUnderTest.setArea("testgmaps_2000");
    classUnderTest.setObjectType("IMAGE");
    classUnderTest.setFilesPerHour(4);
    classUnderTest.setHours(2);
    classUnderTest.setFirstTermUTC(11);
    classUnderTest.setInterval(1);

    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtilities);
    
    
    BltTriggerJobMessage msg = new BltTriggerJobMessage();

    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(msg);
    
    Map<DateTime, FileEntry> entries = new HashMap<DateTime, FileEntry>();
    String[] files = result.getFiles();
    assertEquals(5, files.length);
    for (String s : files) {
      FileEntry fe = catalog.getCatalog().getDatabase().getFileEntry(UUID.fromString(s));
      entries.put(new DateTime(fe.getMetadata().getWhatDate(), fe.getMetadata().getWhatTime()), fe);
    }
    assertTrue(entries.containsKey(new DateTime(2010,01,01,10,00,00)));
    assertTrue(entries.containsKey(new DateTime(2010,01,01,10,15,00)));
    assertTrue(entries.containsKey(new DateTime(2010,01,01,10,30,00)));
    assertTrue(entries.containsKey(new DateTime(2010,01,01,10,45,00)));
    assertTrue(entries.containsKey(new DateTime(2010,01,01,11,00,00)));
  }

  public void testHandle_4() throws Exception {
    classUnderTest = new GraRule() {
      protected DateTime getNowDT() {
        return new DateTime(2010,1,1,10,59);
      }
    };
    classUnderTest.setArea("testgmaps_2000");
    classUnderTest.setObjectType("IMAGE");
    classUnderTest.setFilesPerHour(4);
    classUnderTest.setHours(2);
    classUnderTest.setFirstTermUTC(11);
    classUnderTest.setInterval(1);

    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtilities);
    
    
    BltTriggerJobMessage msg = new BltTriggerJobMessage();

    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(msg);
    
    Map<DateTime, FileEntry> entries = new HashMap<DateTime, FileEntry>();
    String[] files = result.getFiles();
    assertEquals(1, files.length);
    for (String s : files) {
      FileEntry fe = catalog.getCatalog().getDatabase().getFileEntry(UUID.fromString(s));
      entries.put(new DateTime(fe.getMetadata().getWhatDate(), fe.getMetadata().getWhatTime()), fe);
    }
    assertTrue(entries.containsKey(new DateTime(2010,01,01,10,00,00)));
  }
  
  private String getFilePath(String resource) throws Exception {
    File f = new File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }
}
