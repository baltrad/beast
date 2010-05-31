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
package eu.baltrad.beast.db;

import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class DateTimeTest extends TestCase {
  public void testEquals() throws Exception {
    DateTime classUnderTest = new DateTime(2010,1,1,10,0,0);
    DateTime o2 = new DateTime(2010,1,1,10,0,0);
    assertTrue(o2.equals(classUnderTest));
    assertTrue(classUnderTest.equals(classUnderTest));
  }
  
  public void testEquals_false() throws Exception {
    DateTime classUnderTest = new DateTime(2010,1,1,10,0,0);
    DateTime o2 = new DateTime(2010,1,2,10,0,0);
    assertFalse(o2.equals(classUnderTest));
  }

  public void testEquals_false2() throws Exception {
    DateTime classUnderTest = new DateTime(2010,1,1,10,0,0);
    Object o2 = new Object();
    assertFalse(o2.equals(classUnderTest));
    assertFalse(classUnderTest.equals(o2));
  }

  public void testEquals_false3() throws Exception {
    DateTime classUnderTest = new DateTime(2010,1,1,10,0,0);
    assertFalse(classUnderTest.equals(null));
  }
}
