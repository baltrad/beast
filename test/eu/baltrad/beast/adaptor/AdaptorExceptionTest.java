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
package eu.baltrad.beast.adaptor;

import static org.junit.Assert.*;

import org.junit.Test;
/**
 * @author Anders Henja
 */
public class AdaptorExceptionTest {
  @Test
  public void testConstructor() {
    AdaptorException classUnderTest = new AdaptorException();
    assertTrue(classUnderTest instanceof RuntimeException);
  }
  
  @Test
  public void testStringConstructor() {
    AdaptorException classUnderTest = new AdaptorException("something");
    assertEquals("something", classUnderTest.getMessage());
  }
  
  @Test
  public void testThrowableConstructor() {
    RuntimeException x = new RuntimeException("something");
    AdaptorException classUnderTest = new AdaptorException(x);
    assertEquals("something", classUnderTest.getCause().getMessage());
  }
  
  @Test
  public void testStringThrowableConstructor() {
    RuntimeException x = new RuntimeException("something");
    AdaptorException classUnderTest = new AdaptorException("else", x);
    assertEquals("else", classUnderTest.getMessage());
    assertEquals("something", classUnderTest.getCause().getMessage());
  }
}
