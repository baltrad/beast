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
package eu.baltrad.beast.rules.volume;

import junit.framework.TestCase;
import eu.baltrad.beast.db.DateTime;

/**
 * @author Anders Henja
 */
public class VolumeTimerDataTest extends TestCase {
  public void testConstructor() {
    DateTime dt = new DateTime(2010,1,1,1,10,10);
    VolumeTimerData o = new VolumeTimerData(1,dt,"seang", 21, 2000.0);
    assertEquals(dt, o.getDateTime());
    assertEquals("seang", o.getSource());
    assertEquals(21, o.getNBins());
    assertEquals(2000.0, o.getRScale());
  }

  public void testConstructor_null() {
    try {
      new VolumeTimerData(1, null, "seang", 21, 2000.0);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }

  public void testConstructor_nullSource() {
    try {
      new VolumeTimerData(1, new DateTime(2010,1,1,1,10,10),null, 21, 2000.0);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }

  public void testConstructor_zeroOrNegativeBins() {
    try {
      new VolumeTimerData(1, new DateTime(2010,1,1,1,10,10),"seang", 0, 2000.0);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
    try {
      new VolumeTimerData(1, new DateTime(2010,1,1,1,10,10),"seang", -1, 2000.0);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }    
  }  

  public void testConstructor_zeroOrNegativeRange() {
    try {
      new VolumeTimerData(1, new DateTime(2010,1,1,1,10,10),"seang", 21, 0.0);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
    try {
      new VolumeTimerData(1, new DateTime(2010,1,1,1,10,10),"seang", 21, -2000.0);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }  
  
  public void testEquals() throws Exception {
    VolumeTimerData o1 = new VolumeTimerData(1, new DateTime(2010,1,1,1,10,10), "seang", 21, 2000.0);
    VolumeTimerData o2 = new VolumeTimerData(1, new DateTime(2010,1,1,1,10,10), "seang", 21, 2000.0);
    assertTrue(o1.equals(o2));
    assertTrue(o2.equals(o1));
    assertTrue(o1.equals(o1));
    assertTrue(o2.equals(o2));
  }

  public void testEquals_differentRuleId() throws Exception {
    VolumeTimerData o1 = new VolumeTimerData(1, new DateTime(2010,1,1,1,10,10), "seang", 21, 2000.0);
    VolumeTimerData o2 = new VolumeTimerData(2, new DateTime(2010,1,1,1,10,10), "seang", 21, 2000.0);
    assertFalse(o1.equals(o2));
    assertFalse(o2.equals(o1));
  }

  public void testEquals_differentDateId() throws Exception {
    VolumeTimerData o1 = new VolumeTimerData(1, new DateTime(2010,1,1,1,10,10), "seang", 21, 2000.0);
    VolumeTimerData o2 = new VolumeTimerData(1, new DateTime(2010,2,1,1,10,10), "seang", 21, 2000.0);
    assertFalse(o1.equals(o2));
    assertFalse(o2.equals(o1));
  }
  
  public void testEquals_notSameClass() throws Exception {
    VolumeTimerData o1 = new VolumeTimerData(1, new DateTime(2010,1,1,1,10,10), "seang", 21, 2000.0);
    Object o2 = new Object();
    assertFalse(o1.equals(o2));
  }

  public void testEquals_null() throws Exception {
    VolumeTimerData o1 = new VolumeTimerData(1, new DateTime(2010,1,1,1,10,10), "seang", 21, 2000.0);
    assertFalse(o1.equals(null));
  }
}
