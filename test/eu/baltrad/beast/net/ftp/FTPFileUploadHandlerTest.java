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
package eu.baltrad.beast.net.ftp;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

public class FTPFileUploadHandlerTest extends EasyMockSupport {
  private static interface MockMethods {
    void connect(URI u) throws IOException;
    void store(File s, URI d) throws IOException;
    boolean isDirectory(File path) throws IOException;
    InputStream openStream(File f) throws IOException;
  };

  private FTPClient client;
  private MockMethods methods;
  private FTPFileUploadHandler classUnderTest;
  
  @Before
  public void setUp() {
    // client call order is important
    client = createMock(FTPClient.class);
    methods = createMock(MockMethods.class);
    classUnderTest = new FTPFileUploadHandler(client);
  }

  @Test
  public void testUpload() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host/remote/path");
    File src = new File("/path/to/file");

    classUnderTest = new FTPFileUploadHandler(client) {
      protected void connect(URI u) throws IOException {
        methods.connect(u);
      }

      protected void store(File s, URI d) throws IOException {
        methods.store(s, d);
      }
    };
    
    methods.connect(dst);
    expect(client.login("user", "passwd")).andReturn(true);
    methods.store(src, dst);
    client.disconnect();
    replayAll();

    classUnderTest.upload(src, dst);
    
    verifyAll();
  }

  @Test
  public void testUpload_connectFailure() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host/remote/path");
    File src = new File("/path/to/file");

    classUnderTest = new FTPFileUploadHandler(client) {
      protected void connect(URI u) throws IOException {
        methods.connect(u);
      }
    };
    
    methods.connect(dst);
    expectLastCall().andThrow(new IOException());

    replayAll();
    
    try {
      classUnderTest.upload(src, dst);
      fail("expected IOException");
    } catch (IOException e) { }
    
    verifyAll();
  }

  @Test
  public void testUpload_loginFailure() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host/remote/path");
    File src = new File("/path/to/file");

    classUnderTest = new FTPFileUploadHandler(client) {
      protected void connect(URI u) throws IOException {
        methods.connect(u);
      }
    };
    
    methods.connect(dst);
    expect(client.login("user", "passwd")).andReturn(false);
    client.disconnect();

    replayAll();
    
    try {
      classUnderTest.upload(src, dst);
      fail("expected IOException");
    } catch (IOException e) { }
    
    verifyAll();
  }

  @Test
  public void testUpload_storeFailure() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host/remote/path");
    File src = new File("/path/to/file");

    classUnderTest = new FTPFileUploadHandler(client) {
      protected void connect(URI u) throws IOException {
        methods.connect(u);
      }

      protected void store(File s, URI d) throws IOException {
        methods.store(s, d);
      }
    };
    
    methods.connect(dst);
    expect(client.login("user", "passwd")).andReturn(true);
    methods.store(src, dst);
    expectLastCall().andThrow(new IOException());
    client.disconnect();

    replayAll();

    try {
      classUnderTest.upload(src, dst);
      fail("expected IOException");
    } catch (IOException e) { }
    
    verifyAll();
  }

  @Test
  public void testConnect() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host");
    
    client.setConnectTimeout(FTPFileUploadHandler.DEFAULT_CONNECT_TIMEOUT);
    client.connect("host", 21);
    expect(client.getReplyCode()).andReturn(200);
    client.setSoTimeout(FTPFileUploadHandler.DEFAULT_SOCKET_TIMEOUT);
    client.enterLocalPassiveMode();
    
    replayAll();

    classUnderTest.connect(dst);
    
    verifyAll();
  }

  @Test
  public void testConnect_negativeReplyCode() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host");
    
    client.setConnectTimeout(FTPFileUploadHandler.DEFAULT_CONNECT_TIMEOUT);
    client.connect("host", 21);
    expect(client.getReplyCode()).andReturn(500);

    replayAll();

    try {
      classUnderTest.connect(dst);
      fail("expected IOException");
    } catch (IOException e) { }

    verifyAll();    
  }

  @Test
  public void testConnect_Failure() throws Exception {
     URI dst = URI.create("ftp://user:passwd@host");
    
    client.setConnectTimeout(FTPFileUploadHandler.DEFAULT_CONNECT_TIMEOUT);
    client.connect("host", 21);
    expect(client.getReplyCode()).andReturn(500);

    replayAll();

    try {
      classUnderTest.connect(dst);
      fail("expected IOException");
    } catch (IOException e) { }
    
    verifyAll();
  }

  @Test
  public void testConnect_customPort() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host:1234");
    
    client.setConnectTimeout(FTPFileUploadHandler.DEFAULT_CONNECT_TIMEOUT);
    client.connect("host", 1234);
    expect(client.getReplyCode()).andReturn(200);
    client.setSoTimeout(FTPFileUploadHandler.DEFAULT_SOCKET_TIMEOUT);
    client.enterLocalPassiveMode();
    
    replayAll();

    classUnderTest.connect(dst);
    
    verifyAll();
  }

  @Test
  public void testStore_dstDirectory() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host/remote/path");
    File src = new File("/path/to/file");
    InputStream stream = new InputStream() {
      @Override public int read() { return -1; } 
    };

    classUnderTest = new FTPFileUploadHandler(client) {
      protected InputStream openStream(File f) throws IOException {
        return methods.openStream(f);
      }

      protected boolean isDirectory(File path) throws IOException {
        return methods.isDirectory(path);
      }
    };

    expect(methods.isDirectory(new File("/remote/path"))).andReturn(true);
    expect(client.changeWorkingDirectory("/remote/path")).andReturn(true);
    expect(client.setFileType(FTPClient.BINARY_FILE_TYPE)).andReturn(true);
    expect(methods.openStream(src)).andReturn(stream);
    expect(client.storeFile(".file", stream)).andReturn(true);
    expect(client.rename(".file", "file")).andReturn(true);

    replayAll();

    classUnderTest.store(src, dst);
    
    verifyAll();
  }

  @Test
  public void testStore_dstFile() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host/remote/path/newfilename");
    File src = new File("/path/to/file");
    InputStream stream = new InputStream() {
      @Override public int read() { return -1; } 
    };

    classUnderTest = new FTPFileUploadHandler(client) {
      protected InputStream openStream(File f) throws IOException {
        return methods.openStream(f);
      }

      protected boolean isDirectory(File path) throws IOException {
        return methods.isDirectory(path);
      }
    };

    expect(methods.isDirectory(new File("/remote/path/newfilename"))).andReturn(false);
    expect(client.changeWorkingDirectory("/remote/path")).andReturn(true);
    expect(client.setFileType(FTPClient.BINARY_FILE_TYPE)).andReturn(true);
    expect(methods.openStream(src)).andReturn(stream);
    expect(client.storeFile(".newfilename", stream)).andReturn(true);
    expect(client.rename(".newfilename", "newfilename")).andReturn(true);

    replayAll();

    classUnderTest.store(src, dst);
    
    verifyAll();
  }

  @Test
  public void testStore_cwdFailure() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host/remote/path");
    File src = new File("/path/to/file");

    classUnderTest = new FTPFileUploadHandler(client) {
      protected boolean isDirectory(File path) throws IOException {
        return methods.isDirectory(path);
      }
    };

    expect(methods.isDirectory(new File("/remote/path"))).andReturn(true);
    expect(client.changeWorkingDirectory("/remote/path")).andReturn(false);

    replayAll();
  
    try {
      classUnderTest.store(src, dst);
      fail("expected IOException");
    } catch (IOException e) { }
    
    verifyAll();
  }

  @Test
  public void testStore_setBinaryFailure() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host/remote/path");
    File src = new File("/path/to/file");

    classUnderTest = new FTPFileUploadHandler(client) {
      protected boolean isDirectory(File path) throws IOException {
        return methods.isDirectory(path);
      }
    };
    
    expect(methods.isDirectory(new File("/remote/path"))).andReturn(true);
    expect(client.changeWorkingDirectory("/remote/path")).andReturn(true);
    expect(client.setFileType(FTPClient.BINARY_FILE_TYPE)).andReturn(false);
    
    replayAll();

    try {
      classUnderTest.store(src, dst);
      fail("expected IOException");
    } catch (IOException e) { }
    
    verifyAll();
  }

  @Test
  public void testStore_storeFailure() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host/remote/path");
    File src = new File("/path/to/file");
    InputStream stream = new InputStream() {
      @Override public int read() { return -1; } 
    };

    classUnderTest = new FTPFileUploadHandler(client) {
      protected InputStream openStream(File f) throws IOException {
        return methods.openStream(f);
      }

      protected boolean isDirectory(File path) throws IOException {
        return methods.isDirectory(path);
      }
    };

    expect(methods.isDirectory(new File("/remote/path"))).andReturn(true);
    expect(client.changeWorkingDirectory("/remote/path")).andReturn(true);
    expect(client.setFileType(FTPClient.BINARY_FILE_TYPE)).andReturn(true);
    expect(methods.openStream(src)).andReturn(stream);
    expect(client.storeFile(".file", stream)).andReturn(false);

    replayAll();

    try {
      classUnderTest.store(src, dst);
      fail("expected IOException");
    } catch (IOException e) { }
    
    verifyAll();
  }
  
  @Test
  public void testStore_renameFailure() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host/remote/path");
    File src = new File("/path/to/target.tst");
    InputStream stream = new InputStream() {
      @Override public int read() { return -1; } 
    };

    classUnderTest = new FTPFileUploadHandler(client) {
      protected InputStream openStream(File f) throws IOException {
        return methods.openStream(f);
      }

      protected boolean isDirectory(File path) throws IOException {
        return methods.isDirectory(path);
      }
    };

    expect(methods.isDirectory(new File("/remote/path"))).andReturn(true);
    expect(client.changeWorkingDirectory("/remote/path")).andReturn(true);
    expect(client.setFileType(FTPClient.BINARY_FILE_TYPE)).andReturn(true);
    expect(methods.openStream(src)).andReturn(stream);
    expect(client.storeFile(".target.tst", stream)).andReturn(true);
    expect(client.rename(".target.tst", "target.tst")).andReturn(false);

    replayAll();

    try {
      classUnderTest.store(src, dst);
      fail("expected IOException");
    } catch (IOException e) { 
      assertTrue(e.getMessage().equals("Failed to rename temporary file .target.tst to target.tst"));
    }
    
    verifyAll();
  }

  @Test
  public void testIsDirectory() throws Exception {
    FTPFile[] listResult = new FTPFile[]{
      new FTPFile()
    };
    listResult[0].setType(FTPFile.DIRECTORY_TYPE);
    listResult[0].setName("dir");

    expect(client.listFiles("/path/to")).andReturn(listResult);

    replayAll();

    assertTrue(classUnderTest.isDirectory(new File("/path/to/dir")));
    
    verifyAll();
  }

  @Test
  public void testIsDirectory_file() throws Exception {
    FTPFile[] listResult = new FTPFile[]{
      new FTPFile()
    };
    listResult[0].setType(FTPFile.FILE_TYPE);
    listResult[0].setName("dir");

    expect(client.listFiles("/path/to")).andReturn(listResult);

    replayAll();

    assertFalse(classUnderTest.isDirectory(new File("/path/to/dir")));
    
    verifyAll();
  }

  @Test
  public void testIsDirectory_root() throws Exception {
    replayAll();
    assertTrue(classUnderTest.isDirectory(new File("/")));
    verifyAll();
  }

  @Test
  public void testGetUser() {
    URI uri = URI.create("ftp://user:passwd@host");
    assertEquals("user", classUnderTest.getUser(uri));
  }

  @Test
  public void testGetPassword() {
    URI uri = URI.create("ftp://user:passwd@host");
    assertEquals("passwd", classUnderTest.getPassword(uri));
  }

  @Test
  public void testGetPath() {
    URI uri = URI.create("ftp://host/path/to/dir");
    assertEquals(new File("/path/to/dir"), classUnderTest.getPath(uri));
  }

  @Test
  public void testGetPath_missing() {
    URI uri = URI.create("ftp://host");
    try {
      classUnderTest.getPath(uri);
      fail("excpected IllegalArgumentException");
    } catch (IllegalArgumentException e) { }
  }

  @Test
  public void testSetClient_null() {
    try {
      classUnderTest.setClient(null);
      fail("expected NullPointerException");
    } catch (NullPointerException e) { }
  }
}
