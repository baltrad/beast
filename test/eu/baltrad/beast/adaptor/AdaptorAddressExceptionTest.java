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

import junit.framework.TestCase;

/**
 * @author Anders Henja
 *
 */
public class AdaptorAddressExceptionTest extends TestCase {
  public void testConstructor() {
    AdaptorAddressException classUnderTest = new AdaptorAddressException();
    assertTrue(classUnderTest instanceof AdaptorException);
  }
  
  public void testStringConstructor() {
    AdaptorAddressException classUnderTest = new AdaptorAddressException("something");
    assertEquals("something", classUnderTest.getMessage());
  }
  
  public void testThrowableConstructor() {
    RuntimeException x = new RuntimeException("something");
    AdaptorAddressException classUnderTest = new AdaptorAddressException(x);
    assertEquals("something", classUnderTest.getCause().getMessage());
  }
  
  public void testStringThrowableConstructor() {
    RuntimeException x = new RuntimeException("something");
    AdaptorAddressException classUnderTest = new AdaptorAddressException("else", x);
    assertEquals("else", classUnderTest.getMessage());
    assertEquals("something", classUnderTest.getCause().getMessage());
  }
}
