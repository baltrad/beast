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
package eu.baltrad.beast.scheduler;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Anders Henja
 *
 */
public class CronEntryTest {
  private CronEntry classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    classUnderTest = new CronEntry();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void testId() {
    assertEquals(0, classUnderTest.getId());
    classUnderTest.setId(1);
    assertEquals(1, classUnderTest.getId());
  }
  
  @Test
  public void testExpression() {
    assertEquals(null, classUnderTest.getExpression());
    classUnderTest.setExpression("a");
    assertEquals("a", classUnderTest.getExpression());
  }
}
