/*
Copyright 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

This file is part of Beast library.

Beast library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Beast library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Beast library.  If not, see <http://www.gnu.org/licenses/>.
*/
package eu.baltrad.beast.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;

public class FileUploadHandlerBaseTest {
  private FileUploadHandlerBase classUnderTest;

  @Before
  public void setUp() {
    classUnderTest = new FileUploadHandlerBase() {
      @Override
      public void upload(File src, URI dst) { }
    };
  }

  @Test
  public void testAppendPath() {
    URI uri = URI.create(
      "scheme://user:pass@host:123/path?query#fragment"
    );
    URI expected = URI.create(
      "scheme://user:pass@host:123/path/file?query#fragment"
    );

    URI result = classUnderTest.appendPath(uri, "file");
    assertEquals(expected, result);
    assertNotSame(uri, result);
  }

  @Test
  public void testAppendPath_nonHierarchical() {
    URI uri = URI.create("scheme:path");

    try {
      classUnderTest.appendPath(uri, "file");
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) { }
  }

}
