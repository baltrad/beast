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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import eu.baltrad.bdb.util.DateTime;

/**
 * @author Anders Henja
 */
public class CompositeTimerDataTest {
  @Test
  public void testConstructor() {
    DateTime dt = new DateTime(2010,1,1,1,10,10);
    CompositeTimerData o = new CompositeTimerData(1,dt);
    assertEquals(dt, o.getDateTime());
  }

  @Test
  public void testConstructor_null() {
    try {
      new CompositeTimerData(1, null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }
  
  @Test
  public void testEquals() throws Exception {
    CompositeTimerData o1 = new CompositeTimerData(1, new DateTime(2010,1,1,1,10,10));
    CompositeTimerData o2 = new CompositeTimerData(1, new DateTime(2010,1,1,1,10,10));
    assertTrue(o1.equals(o2));
    assertTrue(o2.equals(o1));
    assertTrue(o1.equals(o1));
    assertTrue(o2.equals(o2));
  }

  @Test
  public void testEquals_differentRuleId() throws Exception {
    CompositeTimerData o1 = new CompositeTimerData(1, new DateTime(2010,1,1,1,10,10));
    CompositeTimerData o2 = new CompositeTimerData(2, new DateTime(2010,1,1,1,10,10));
    assertFalse(o1.equals(o2));
    assertFalse(o2.equals(o1));
  }
  
  @Test
  public void testEquals_notSameClass() throws Exception {
    CompositeTimerData o1 = new CompositeTimerData(1, new DateTime(2010,1,1,1,10,10));
    Object o2 = new Object();
    assertFalse(o1.equals(o2));
  }

  @Test
  public void testEquals_null() throws Exception {
    CompositeTimerData o1 = new CompositeTimerData(1, new DateTime(2010,1,1,1,10,10));
    assertFalse(o1.equals(null));
  }
}
