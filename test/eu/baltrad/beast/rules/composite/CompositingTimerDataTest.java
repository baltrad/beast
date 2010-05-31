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
package eu.baltrad.beast.rules.composite;

import eu.baltrad.beast.db.DateTime;
import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class CompositingTimerDataTest extends TestCase {
  public void testConstructor() {
    DateTime dt = new DateTime(2010,1,1,1,10,10);
    CompositingTimerData o = new CompositingTimerData(1,dt);
    assertEquals(dt, o.getDateTime());
  }

  public void testConstructor_null() {
    try {
      new CompositingTimerData(1, null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }
  
  public void testEquals() throws Exception {
    CompositingTimerData o1 = new CompositingTimerData(1, new DateTime(2010,1,1,1,10,10));
    CompositingTimerData o2 = new CompositingTimerData(1, new DateTime(2010,1,1,1,10,10));
    assertTrue(o1.equals(o2));
    assertTrue(o2.equals(o1));
    assertTrue(o1.equals(o1));
    assertTrue(o2.equals(o2));
  }

  public void testEquals_differentRuleId() throws Exception {
    CompositingTimerData o1 = new CompositingTimerData(1, new DateTime(2010,1,1,1,10,10));
    CompositingTimerData o2 = new CompositingTimerData(2, new DateTime(2010,1,1,1,10,10));
    assertFalse(o1.equals(o2));
    assertFalse(o2.equals(o1));
  }
  
  public void testEquals_notSameClass() throws Exception {
    CompositingTimerData o1 = new CompositingTimerData(1, new DateTime(2010,1,1,1,10,10));
    Object o2 = new Object();
    assertFalse(o1.equals(o2));
  }

  public void testEquals_null() throws Exception {
    CompositingTimerData o1 = new CompositingTimerData(1, new DateTime(2010,1,1,1,10,10));
    assertFalse(o1.equals(null));
  }
  
}
