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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class GraRuleTest extends EasyMockSupport {
  private Catalog catalog = null;
  private IRuleUtilities ruleUtil = null;
  private GraRule classUnderTest = null;

  private interface Methods {
    DateTime getNominalTime(DateTime now);

    List<CatalogEntry> findFiles(DateTime now);
  };

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    catalog = createMock(Catalog.class);
    ruleUtil = createMock(IRuleUtilities.class);
    classUnderTest = new GraRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtil);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    catalog = null;
    ruleUtil = null;
    classUnderTest = null;
  }

  @Test
  public void testSetGetRuleId() {
    classUnderTest.setRuleId(10);
    assertEquals(10, classUnderTest.getRuleId());
  }

  @Test
  public void testGetType() {
    assertEquals("blt_gra", classUnderTest.getType());
  }

  @Test
  public void testSetGetFirstTermUTC() {
    assertEquals(6, classUnderTest.getFirstTermUTC());
    for (int i = 0; i < 24; i++) {
      classUnderTest.setFirstTermUTC(i);
      assertEquals(i, classUnderTest.getFirstTermUTC());
    }
  }

  @Test
  public void testSetFirstTermUTC_invalid() {
    try {
      classUnderTest.setFirstTermUTC(24);
      fail("Expected IllegalArgument exception");
    } catch (IllegalArgumentException iae) {
      // pass
    }
    assertEquals(6, classUnderTest.getFirstTermUTC());
  }

  @Test
  public void testSetGetInterval() {
    assertEquals(12, classUnderTest.getInterval());
    classUnderTest.setInterval(1);
    assertEquals(1, classUnderTest.getInterval());
    classUnderTest.setInterval(2);
    assertEquals(2, classUnderTest.getInterval());
    classUnderTest.setInterval(3);
    assertEquals(3, classUnderTest.getInterval());
    classUnderTest.setInterval(4);
    assertEquals(4, classUnderTest.getInterval());
    classUnderTest.setInterval(6);
    assertEquals(6, classUnderTest.getInterval());
    classUnderTest.setInterval(8);
    assertEquals(8, classUnderTest.getInterval());
    classUnderTest.setInterval(12);
    assertEquals(12, classUnderTest.getInterval());
    classUnderTest.setInterval(24);
    assertEquals(24, classUnderTest.getInterval());
  }

  @Test
  public void testSetInterval_invalid() {
    try {
      classUnderTest.setInterval(5);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException iae) {
      // pass
    }
    assertEquals(12, classUnderTest.getInterval());
  }

  @Test
  public void testIntervalHoursSame() {
    assertEquals(12, classUnderTest.getHours());
    assertEquals(12, classUnderTest.getInterval());
    classUnderTest.setHours(6);
    assertEquals(6, classUnderTest.getHours());
    assertEquals(6, classUnderTest.getInterval());
    classUnderTest.setInterval(12);
    assertEquals(12, classUnderTest.getHours());
    assertEquals(12, classUnderTest.getInterval());
    try {
      classUnderTest.setInterval(5);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException ae) {
      // pass
    }
    try {
      classUnderTest.setHours(5);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException ae) {
      // pass
    }
    assertEquals(12, classUnderTest.getHours());
    assertEquals(12, classUnderTest.getInterval());
  }

  @Test
  public void handle() {
    final Methods methods = createMock(Methods.class);
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    List<String> uuids = new ArrayList<String>();
    uuids.add("abc-def-ghi");
    Date scheduledDate = new Date();
    DateTime nowDt = new DateTime();
    DateTime newDt = new DateTime();
    DateTime nominalDt = new DateTime(2016,2,3,10,15,0);
    BltTriggerJobMessage message = new BltTriggerJobMessage();
    message.setScheduledFireTime(scheduledDate);

    classUnderTest = new GraRule() {
      @Override
      protected DateTime getNominalTime(DateTime dt) {
        return methods.getNominalTime(dt);
      }

      @Override
      protected List<CatalogEntry> findFiles(DateTime now) {
        return methods.findFiles(now);
      }
    };
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setArea("myarea");
    classUnderTest.setZrA(1.1);
    classUnderTest.setZrB(2.2);
    classUnderTest.setFilesPerHour(4);
    classUnderTest.setHours(12);
    classUnderTest.setAcceptableLoss(2);
    expect(ruleUtil.nowDT()).andReturn(nowDt);
    expect(ruleUtil.createDateTime(scheduledDate)).andReturn(newDt);
    expect(methods.getNominalTime(newDt)).andReturn(nominalDt);
    expect(methods.findFiles(nominalDt)).andReturn(entries);
    expect(ruleUtil.getUuidStringsFromEntries(entries)).andReturn(uuids);
    
    replayAll();
    
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(message);
    
    verifyAll();
    assertEquals("eu.baltrad.beast.CreateGraCoefficient", result.getAlgorithm());
    assertEquals(10, result.getArguments().length);
    assertEquals("--area=myarea", result.getArguments()[0]);
    assertEquals("--date=20160203", result.getArguments()[1]);
    assertEquals("--time=101500", result.getArguments()[2]);
    assertEquals("--zra=1.1", result.getArguments()[3]);
    assertEquals("--zrb=2.2", result.getArguments()[4]);
    assertEquals("--interval=12", result.getArguments()[5]);
    assertEquals("--N=49", result.getArguments()[6]);
    assertEquals("--accept=2", result.getArguments()[7]);
    assertEquals("--quantity=DBZH", result.getArguments()[8]);
    assertEquals("--distancefield=eu.baltrad.composite.quality.distance.radar", result.getArguments()[9]);
    
    assertEquals(1,result.getFiles().length);
    assertEquals("abc-def-ghi", result.getFiles()[0]);
    
  }

  @Test
  public void testGetNominalTime() {
    classUnderTest.setInterval(12);
    classUnderTest.setFirstTermUTC(6);

    assertEquals(new DateTime(2013, 5, 3, 18, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 5, 5, 4)));
    assertEquals(new DateTime(2013, 5, 4, 6, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 6, 5, 4)));
    assertEquals(new DateTime(2013, 5, 4, 6, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 7, 5, 4)));
    assertEquals(new DateTime(2013, 5, 4, 6, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 8, 5, 4)));
    assertEquals(new DateTime(2013, 5, 4, 6, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 17, 5, 4)));
    assertEquals(new DateTime(2013, 5, 4, 18, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 19, 5, 4)));

    classUnderTest.setInterval(12);
    classUnderTest.setFirstTermUTC(18);
    assertEquals(new DateTime(2013, 5, 3, 18, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 5, 5, 4)));
    assertEquals(new DateTime(2013, 5, 4, 6, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 6, 5, 4)));
    assertEquals(new DateTime(2013, 5, 4, 6, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 7, 5, 4)));
    assertEquals(new DateTime(2013, 5, 4, 6, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 8, 5, 4)));
    assertEquals(new DateTime(2013, 5, 4, 6, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 17, 5, 4)));
    assertEquals(new DateTime(2013, 5, 4, 18, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 19, 5, 4)));

    classUnderTest.setInterval(6);
    classUnderTest.setFirstTermUTC(23);
    assertEquals(new DateTime(2013, 5, 3, 23, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 0, 5, 4)));
    assertEquals(new DateTime(2013, 5, 3, 23, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 1, 5, 4)));
    assertEquals(new DateTime(2013, 5, 3, 23, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 2, 5, 4)));
    assertEquals(new DateTime(2013, 5, 3, 23, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 3, 5, 4)));
    assertEquals(new DateTime(2013, 5, 3, 23, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 4, 5, 4)));
    assertEquals(new DateTime(2013, 5, 4, 5, 0, 0), classUnderTest.getNominalTime(new DateTime(2013, 5, 4, 5, 5, 4)));
  }

}
