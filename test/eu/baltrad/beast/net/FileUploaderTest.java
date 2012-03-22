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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

public class FileUploaderTest extends EasyMockSupport {
  private Map<String, FileUploadHandler> handlers;
  private FileUploadHandler handler;
  private FileUploader classUnderTest;

  @Before
  public void setUp() {
    handlers = new HashMap<String, FileUploadHandler>();
    handler = createMock(FileUploadHandler.class);
    handlers.put("test", handler);

    classUnderTest = new FileUploader(handlers);
  }

  @Test
  public void testCreateDefault() {
    FileUploader u = FileUploader.createDefault();
    assertNotNull(u);
    assertNotNull(u.getHandlerByScheme("ftp"));
    assertNotNull(u.getHandlerByScheme("scp"));
    assertNotNull(u.getHandlerByScheme("copy"));
  }

  @Test
  public void testUpload() throws Exception {
    File src = new File("/input");
    URI dst = URI.create("test:///");

    handler.upload(src, dst);
    
    replayAll();

    classUnderTest.upload(src, dst);
    
    verifyAll();
  }

  @Test
  public void testUpload_unknownScheme() throws Exception {
    try {
      classUnderTest.upload("/input", "unknown:///");
      fail("expected UnknownServiceException");
    } catch (java.net.UnknownServiceException e) {
      // pass
    }
  }

  @Test
  public void testUpload_relativeSrc() throws Exception {
    try {
      classUnderTest.upload("input", "test:///");
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) { }
  }

  @Test
  public void testAppendPath() throws Exception {
    URI uri = URI.create("test:///");
    URI uriAppended = URI.create("test:///path");

    expect(handler.appendPath(uri, "path")).andReturn(uriAppended);
    
    replayAll();

    URI result = classUnderTest.appendPath(uri, "path");    
    assertSame(uriAppended, result);
    
    verifyAll();
  }

  @Test
  public void testAppendPath_unknownScheme() throws Exception {
    URI uri = URI.create("unknown:///");
    try {
      classUnderTest.appendPath(uri, "path");
      fail("expected UnknownServiceException");
    } catch (java.net.UnknownServiceException e) { }
  }

  @Test
  public void testSetHandlers_null() {
   try {
      classUnderTest.setHandlers(null);
      fail("expected NullPointerException");
    } catch (NullPointerException e) {
      // pass
    }
  }
}
