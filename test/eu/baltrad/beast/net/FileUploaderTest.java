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

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.MockControl;

public class FileUploaderTest extends TestCase {
  private Map<String, FileUploadHandler> handlers;
  private MockControl handlerControl;
  private FileUploadHandler handler;
  private FileUploader classUnderTest;

  protected void setUp() {
    handlers = new HashMap<String, FileUploadHandler>();
    handlerControl = MockControl.createControl(FileUploadHandler.class);
    handler = (FileUploadHandler)handlerControl.getMock();
    handlers.put("test", handler);

    classUnderTest = new FileUploader(handlers);
  }

  protected void replay() {
    handlerControl.replay();
  }

  protected void verify() {
    handlerControl.verify();
  }

  public void testCreateDefault() {
    FileUploader u = FileUploader.createDefault();
    assertNotNull(u);
    assertNotNull(u.getHandlerByScheme("ftp"));
    assertNotNull(u.getHandlerByScheme("scp"));
    assertNotNull(u.getHandlerByScheme("copy"));
  }

  public void testUpload() throws Exception {
    File src = new File("/input");
    URI dst = URI.create("test:///");

    handler.upload(src, dst);
    replay();

    classUnderTest.upload(src, dst);
    verify();
  }

  public void testUpload_unknownScheme() throws Exception {
    try {
      classUnderTest.upload("/input", "unknown:///");
      fail("expected UnknownServiceException");
    } catch (java.net.UnknownServiceException e) {
      // pass
    }
  }

  public void testUpload_relativeSrc() throws Exception {
    try {
      classUnderTest.upload("input", "test:///");
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }

  public void testSetHandlers_null() {
   try {
      classUnderTest.setHandlers(null);
      fail("expected NullPointerException");
    } catch (NullPointerException e) {
      // pass
    }
  }
}
