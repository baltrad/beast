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

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

public class FTPFileUploadHandlerTest extends TestCase {
  private static interface MockMethods {
    void connect(URI u) throws IOException;
    void store(File s, URI d) throws IOException;
    boolean isDirectory(File path) throws IOException;
    InputStream openStream(File f) throws IOException;
  };

  private MockControl clientControl;
  private FTPClient client;
  private MockControl methodsControl;
  private MockMethods methods;
  private FTPFileUploadHandler classUnderTest;
  
  protected void setUp() {
    // client call order is important
    clientControl = MockClassControl.createStrictControl(FTPClient.class);
    client = (FTPClient)clientControl.getMock();
    methodsControl = MockControl.createControl(MockMethods.class);
    methods = (MockMethods)methodsControl.getMock();
    classUnderTest = new FTPFileUploadHandler(client);
  }

  protected void replay() {
    clientControl.replay();
    methodsControl.replay();
  }

  protected void verify() {
    clientControl.verify();
    methodsControl.verify();
  }

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
    client.login("user", "passwd");
    clientControl.setReturnValue(true);
    methods.store(src, dst);
    client.disconnect();
    replay();

    classUnderTest.upload(src, dst);
    verify();
  }

  public void testUpload_connectFailure() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host/remote/path");
    File src = new File("/path/to/file");

    classUnderTest = new FTPFileUploadHandler(client) {
      protected void connect(URI u) throws IOException {
        methods.connect(u);
      }
    };
    
    methods.connect(dst);
    methodsControl.setThrowable(new IOException());
    replay();
    
    try {
      classUnderTest.upload(src, dst);
      fail("expected IOException");
    } catch (IOException e) { }
    verify();
  }

  public void testUpload_loginFailure() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host/remote/path");
    File src = new File("/path/to/file");

    classUnderTest = new FTPFileUploadHandler(client) {
      protected void connect(URI u) throws IOException {
        methods.connect(u);
      }
    };
    
    methods.connect(dst);
    client.login("user", "passwd");
    clientControl.setReturnValue(false);
    client.disconnect();
    replay();
    
    try {
      classUnderTest.upload(src, dst);
      fail("expected IOException");
    } catch (IOException e) { }
    verify();
  }

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
    client.login("user", "passwd");
    clientControl.setReturnValue(true);
    methods.store(src, dst);
    methodsControl.setThrowable(new IOException());
    client.disconnect();
    replay();

    try {
      classUnderTest.upload(src, dst);
      fail("expected IOException");
    } catch (IOException e) { }
    verify();
  }

  public void testConnect() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host");
    
    client.setConnectTimeout(classUnderTest.DEFAULT_CONNECT_TIMEOUT);
    client.connect("host", 21);
    client.getReplyCode();
    clientControl.setReturnValue(200);
    client.setSoTimeout(classUnderTest.DEFAULT_SOCKET_TIMEOUT);
    client.enterLocalPassiveMode();
    replay();

    classUnderTest.connect(dst);
    verify();
  }

  public void testConnect_negativeReplyCode() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host");
    
    client.setConnectTimeout(classUnderTest.DEFAULT_CONNECT_TIMEOUT);
    client.connect("host", 21);
    client.getReplyCode();
    clientControl.setReturnValue(500);
    replay();

    try {
      classUnderTest.connect(dst);
      fail("expected IOException");
    } catch (IOException e) { }
    verify();    
  }

  public void testConnect_Failure() throws Exception {
     URI dst = URI.create("ftp://user:passwd@host");
    
    client.setConnectTimeout(classUnderTest.DEFAULT_CONNECT_TIMEOUT);
    client.connect("host", 21);
    client.getReplyCode();
    clientControl.setReturnValue(500);
    replay();

    try {
      classUnderTest.connect(dst);
      fail("expected IOException");
    } catch (IOException e) { }
    verify();
  }

  public void testConnect_customPort() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host:1234");
    
    client.setConnectTimeout(classUnderTest.DEFAULT_CONNECT_TIMEOUT);
    client.connect("host", 1234);
    client.getReplyCode();
    clientControl.setReturnValue(200);
    client.setSoTimeout(classUnderTest.DEFAULT_SOCKET_TIMEOUT);
    client.enterLocalPassiveMode();
    replay();

    classUnderTest.connect(dst);
    verify();
  }

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

    methods.isDirectory(new File("/remote/path"));
    methodsControl.setReturnValue(true);
    client.changeWorkingDirectory("/remote/path");
    clientControl.setReturnValue(true);
    client.setFileType(client.BINARY_FILE_TYPE);
    clientControl.setReturnValue(true);
    methods.openStream(src);
    methodsControl.setReturnValue(stream);
    client.storeFile("file", stream);
    clientControl.setReturnValue(true);
    replay();

    classUnderTest.store(src, dst);
    verify();
  }

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

    methods.isDirectory(new File("/remote/path/newfilename"));
    methodsControl.setReturnValue(false);
    client.changeWorkingDirectory("/remote/path");
    clientControl.setReturnValue(true);
    client.setFileType(client.BINARY_FILE_TYPE);
    clientControl.setReturnValue(true);
    methods.openStream(src);
    methodsControl.setReturnValue(stream);
    client.storeFile("newfilename", stream);
    clientControl.setReturnValue(true);
    replay();

    classUnderTest.store(src, dst);
    verify();
  }

  public void testStore_cwdFailure() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host/remote/path");
    File src = new File("/path/to/file");

    classUnderTest = new FTPFileUploadHandler(client) {
      protected boolean isDirectory(File path) throws IOException {
        return methods.isDirectory(path);
      }
    };

    methods.isDirectory(new File("/remote/path"));
    methodsControl.setReturnValue(true);
    client.changeWorkingDirectory("/remote/path");
    clientControl.setReturnValue(false);
    replay();
  
    try {
      classUnderTest.store(src, dst);
      fail("expected IOException");
    } catch (IOException e) { }
    verify();
  }

  public void testStore_setBinaryFailure() throws Exception {
    URI dst = URI.create("ftp://user:passwd@host/remote/path");
    File src = new File("/path/to/file");

    classUnderTest = new FTPFileUploadHandler(client) {
      protected boolean isDirectory(File path) throws IOException {
        return methods.isDirectory(path);
      }
    };
    
    methods.isDirectory(new File("/remote/path"));
    methodsControl.setReturnValue(true);
    client.changeWorkingDirectory("/remote/path");
    clientControl.setReturnValue(true);
    client.setFileType(client.BINARY_FILE_TYPE);
    clientControl.setReturnValue(false);
    replay();

    try {
      classUnderTest.store(src, dst);
      fail("expected IOException");
    } catch (IOException e) { }
    verify();
  }

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

    methods.isDirectory(new File("/remote/path"));
    methodsControl.setReturnValue(true);
    client.changeWorkingDirectory("/remote/path");
    clientControl.setReturnValue(true);
    client.setFileType(client.BINARY_FILE_TYPE);
    clientControl.setReturnValue(true);
    methods.openStream(src);
    methodsControl.setReturnValue(stream);
    client.storeFile("file", stream);
    clientControl.setReturnValue(false);
    replay();

    try {
      classUnderTest.store(src, dst);
      fail("expected IOException");
    } catch (IOException e) { }
    verify();
  }

  public void testIsDirectory() throws Exception {
    FTPFile[] listResult = new FTPFile[]{
      new FTPFile()
    };
    listResult[0].setType(FTPFile.DIRECTORY_TYPE);
    listResult[0].setName("dir");

    client.listFiles("/path/to");
    clientControl.setReturnValue(listResult);
    replay();

    assertTrue(classUnderTest.isDirectory(new File("/path/to/dir")));
    verify();
  }

  public void testIsDirectory_file() throws Exception {
    FTPFile[] listResult = new FTPFile[]{
      new FTPFile()
    };
    listResult[0].setType(FTPFile.FILE_TYPE);
    listResult[0].setName("dir");

    client.listFiles("/path/to");
    clientControl.setReturnValue(listResult);
    replay();

    assertFalse(classUnderTest.isDirectory(new File("/path/to/dir")));
    verify();
  }


  public void testIsDirectory_root() throws Exception {
    replay();
    assertTrue(classUnderTest.isDirectory(new File("/")));
    verify();
  }

  public void testGetUser() {
    URI uri = URI.create("ftp://user:passwd@host");
    assertEquals("user", classUnderTest.getUser(uri));
  }

  public void testGetPassword() {
    URI uri = URI.create("ftp://user:passwd@host");
    assertEquals("passwd", classUnderTest.getPassword(uri));
  }

  public void testGetPath() {
    URI uri = URI.create("ftp://host/path/to/dir");
    assertEquals(new File("/path/to/dir"), classUnderTest.getPath(uri));
  }

  public void testGetPath_missing() {
    URI uri = URI.create("ftp://host");
    try {
      classUnderTest.getPath(uri);
      fail("excpected IllegalArgumentException");
    } catch (IllegalArgumentException e) { }
  }

  public void testSetClient_null() {
    try {
      classUnderTest.setClient(null);
      fail("expected NullPointerException");
    } catch (NullPointerException e) { }
  }
}
