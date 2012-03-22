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
package eu.baltrad.beast.router;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import eu.baltrad.beast.message.IBltMessage;

/**
 * @author Anders Henja
 */
public class RouteTest {
  @Test
  public void testConstructor() {
    IBltMessage msg = new IBltMessage(){};
    Route classUnderTest = new Route("ABC", msg);
    assertEquals("ABC", classUnderTest.getDestination());
    assertSame(msg, classUnderTest.getMessage());
  }
  
  @Test
  public void testDestination() {
    Route classUnderTest = new Route();
    classUnderTest.setDestination("ABC");
    assertEquals("ABC", classUnderTest.getDestination());
  }
  
  @Test
  public void testMessage() {
    IBltMessage msg = new IBltMessage(){};
    Route classUnderTest = new Route("ABC", msg);
    assertSame(msg, classUnderTest.getMessage());
  }
}
