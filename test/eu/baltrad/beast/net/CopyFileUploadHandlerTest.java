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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

public class CopyFileUploadHandlerTest extends EasyMockSupport {
  private static interface CopyFileUploadHandlerMethods {
    boolean isDirectory(File path);
    void copyFileToDirectory(File src, File dst) throws IOException;
    void copyFile(File src, File dst) throws IOException;
  };

  private CopyFileUploadHandlerMethods methods;
  private CopyFileUploadHandler classUnderTest;

  @Before
  public void setUp() {
    methods = createMock(CopyFileUploadHandlerMethods.class);
    classUnderTest = new CopyFileUploadHandler() {
      @Override
      protected boolean isDirectory(File path) {
        return methods.isDirectory(path);
      }

      @Override
      protected void copyFileToDirectory(File src, File dst)
          throws IOException {
        methods.copyFileToDirectory(src, dst);
      }
      
      @Override
      protected void copyFile(File src, File dst) throws IOException {
        methods.copyFile(src, dst);
      }
    };
  }

  @Test
  public void testUpload_dstDirectory() throws Exception {
    File src = new File("/path/to/src");
    File dst = new File("/path/to/dst");
    URI dstUri = URI.create("copy:///path/to/dst");

    expect(methods.isDirectory(new File("/path/to/dst"))).andReturn(true);
    methods.copyFileToDirectory(src, dst);
    
    replayAll();

    classUnderTest.upload(src, dstUri);
    
    verifyAll();
  }

  @Test
  public void testUpload_dstFile() throws Exception {
    File src = new File("/path/to/src");
    File dst = new File("/path/to/dst");
    URI dstUri = URI.create("copy:///path/to/dst");

    expect(methods.isDirectory(new File("/path/to/dst"))).andReturn(false);
    methods.copyFile(src, dst);
    
    replayAll();

    classUnderTest.upload(src, dstUri);
    
    verifyAll();
  }

  @Test
  public void testUpload_invalidURI() throws Exception {
    File src = new File("/path/to/src");
    URI dstUri = URI.create("copy://host");
    
    try {
      classUnderTest.upload(src, dstUri);
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) { }
  }
}
