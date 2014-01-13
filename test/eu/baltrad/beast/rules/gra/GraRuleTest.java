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

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class GraRuleTest extends EasyMockSupport {
  private Catalog catalog = null;
  private IRuleUtilities ruleUtil = null;
  private GraRule classUnderTest = null;

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
    classUnderTest.setFirstTermUTC(2);
    classUnderTest.setFirstTermUTC(-1);
    assertEquals(2, classUnderTest.getFirstTermUTC());
    classUnderTest.setFirstTermUTC(24);
    assertEquals(2, classUnderTest.getFirstTermUTC());
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
    classUnderTest.setInterval(-1);
    assertEquals(24, classUnderTest.getInterval());
    classUnderTest.setInterval(25);
    assertEquals(24, classUnderTest.getInterval());
  }
  
  @Test
  public void testGetNominalTime() {
    classUnderTest.setInterval(12);
    classUnderTest.setFirstTermUTC(6);
    
    assertEquals(new DateTime(2013,5,3,18,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,5,5,4)));
    assertEquals(new DateTime(2013,5,4,6,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,6,5,4)));
    assertEquals(new DateTime(2013,5,4,6,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,7,5,4)));
    assertEquals(new DateTime(2013,5,4,6,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,8,5,4)));
    assertEquals(new DateTime(2013,5,4,6,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,17,5,4)));
    assertEquals(new DateTime(2013,5,4,18,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,19,5,4)));

    classUnderTest.setInterval(12);
    classUnderTest.setFirstTermUTC(18);
    assertEquals(new DateTime(2013,5,3,18,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,5,5,4)));
    assertEquals(new DateTime(2013,5,4,6,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,6,5,4)));
    assertEquals(new DateTime(2013,5,4,6,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,7,5,4)));
    assertEquals(new DateTime(2013,5,4,6,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,8,5,4)));
    assertEquals(new DateTime(2013,5,4,6,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,17,5,4)));
    assertEquals(new DateTime(2013,5,4,18,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,19,5,4)));

    classUnderTest.setInterval(6);
    classUnderTest.setFirstTermUTC(23);
    assertEquals(new DateTime(2013,5,3,23,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,0,5,4)));
    assertEquals(new DateTime(2013,5,3,23,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,1,5,4)));
    assertEquals(new DateTime(2013,5,3,23,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,2,5,4)));
    assertEquals(new DateTime(2013,5,3,23,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,3,5,4)));
    assertEquals(new DateTime(2013,5,3,23,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,4,5,4)));
    assertEquals(new DateTime(2013,5,4,5,0,0), classUnderTest.getNominalTime(new DateTime(2013,5,4,5,5,4)));
  }
  
}
