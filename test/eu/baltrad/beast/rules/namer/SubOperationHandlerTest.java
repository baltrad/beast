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

package eu.baltrad.beast.rules.namer;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Anders Henja
 * @date 2014-11-28
 */
public class SubOperationHandlerTest {
  private SubOperationHandler classUnderTest;
  
  @Before
  public void setUp() {
    classUnderTest = new SubOperationHandler();
  }
  
  @After
  public void tearDown() {
    classUnderTest = null;
  }
  
  @Test
  public void toupper() {
    assertEquals("abc", classUnderTest.handle("", "abc"));
    assertEquals("ABC", classUnderTest.handle(".toupper()", "abc"));
    assertEquals("Abc", classUnderTest.handle(".toupper(0)", "abc"));
    assertEquals("aBc", classUnderTest.handle(".toupper(1)", "abc"));
    assertEquals("abC", classUnderTest.handle(".toupper(2)", "abc"));
    assertEquals("aBC", classUnderTest.handle(".toupper(1,3)", "abc"));
    assertEquals("abc", classUnderTest.handle(".toupper(-1)", "abc")); // Out of bounds
    assertEquals("abc", classUnderTest.handle(".toupper(3)", "abc")); // Out of bounds
    assertEquals("abc", classUnderTest.handle(".toupper(1,4)", "abc")); // Out of bounds
  }

  @Test
  public void tolower() {
    assertEquals("ABC", classUnderTest.handle("", "ABC"));
    assertEquals("abc", classUnderTest.handle(".tolower()", "ABC"));
    assertEquals("aBC", classUnderTest.handle(".tolower(0)", "ABC"));
    assertEquals("AbC", classUnderTest.handle(".tolower(1)", "ABC"));
    assertEquals("ABc", classUnderTest.handle(".tolower(2)", "ABC"));
    assertEquals("Abc", classUnderTest.handle(".tolower(1,3)", "ABC"));
    assertEquals("ABC", classUnderTest.handle(".tolower(-1)", "ABC")); // Out of bounds
    assertEquals("ABC", classUnderTest.handle(".tolower(3)", "ABC")); // Out of bounds
    assertEquals("ABC", classUnderTest.handle(".tolower(1,4)", "ABC")); // Out of bounds
  }
  
  @Test
  public void substring() {
    assertEquals("abcdefghi", classUnderTest.handle("", "abcdefghi"));
    assertEquals("", classUnderTest.handle(".substring(0)", "abcdefghi"));
    assertEquals("abc", classUnderTest.handle(".substring(3)", "abcdefghi"));
    assertEquals("abcdefghi", classUnderTest.handle(".substring(10)", "abcdefghi")); // Out of bounds
    assertEquals("bc", classUnderTest.handle(".substring(1,3)", "abcdefghi"));
    assertEquals("hi", classUnderTest.handle(".substring(7,9)", "abcdefghi"));
    assertEquals("abcdefghi", classUnderTest.handle(".substring(7,11)", "abcdefghi")); // Out of bounds
  }
  
  @Test
  public void trim() {
    assertEquals("abcdefghi", classUnderTest.handle(".trim()", "  abcdefghi  "));
    assertEquals("abcdefghi", classUnderTest.handle(".trim()", "abcdefghi  "));
    assertEquals("abcdefghi", classUnderTest.handle(".trim()", "  abcdefghi"));
  }

  @Test
  public void ltrim() {
    assertEquals("abcdefghi  ", classUnderTest.handle(".ltrim()", "  abcdefghi  "));
    assertEquals("abcdefghi  ", classUnderTest.handle(".ltrim()", "abcdefghi  "));
    assertEquals("abcdefghi", classUnderTest.handle(".ltrim()", "  abcdefghi"));
  }

  @Test
  public void rtrim() {
    assertEquals("  abcdefghi", classUnderTest.handle(".rtrim()", "  abcdefghi  "));
    assertEquals("abcdefghi", classUnderTest.handle(".rtrim()", "abcdefghi  "));
    assertEquals("  abcdefghi", classUnderTest.handle(".rtrim()", "  abcdefghi"));
  }
  
  @Test
  public void interval_l() {
    assertEquals("00", classUnderTest.handle(".interval_l(15)", "10")); 
  }
  
  @Test
  public void interval_u() {
    assertEquals("15", classUnderTest.handle(".interval_u(15)", "10")); 
  }
  
  @Test
  public void combined_suboperations() {
    assertEquals("ABCdefgh", classUnderTest.handle(".tolower().toupper(0,4).tolower(3)", "ABCDEFGH"));
    assertEquals("Cde", classUnderTest.handle(".tolower().toupper(0,4).tolower(3).substring(2,5)", "ABCDEFGH"));
    assertEquals("abc def", classUnderTest.handle(".tolower().trim()", "ABC DEF "));
  }
}
