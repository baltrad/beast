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
package eu.baltrad.beast.net.scp;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

public class SCPFileUploadHandlerTest extends TestCase {
  private static interface MockMethods {
    void connect(URI u) throws IOException;
    void auth(URI u) throws IOException;
    void store(File s, URI d) throws IOException;
  };

  private MockControl clientControl;
  private SSHClient client;
  private MockControl xferControl;
  private SCPFileTransfer xfer;
  private MockControl methodsControl;
  private MockMethods methods;
  private SCPFileUploadHandler classUnderTest;

  protected void setUp() {
    clientControl = MockClassControl.createControl(SSHClient.class);
    client = (SSHClient)clientControl.getMock();
    xferControl = MockClassControl.createControl(SCPFileTransfer.class);
    xfer = (SCPFileTransfer)xferControl.getMock();
    methodsControl = MockControl.createControl(MockMethods.class);
    methods = (MockMethods)methodsControl.getMock();
    classUnderTest = new SCPFileUploadHandler(client);
  }

  protected void replay() {
    clientControl.replay();
    methodsControl.replay();
    xferControl.replay();
  }

  protected void verify() {
    clientControl.verify();
    methodsControl.verify();
    xferControl.verify();
  }
  
  public void testUpload() throws Exception {
    File src = new File("/path/to/file");
    URI dst = URI.create("scp://user:pass@host/path");

    classUnderTest = new SCPFileUploadHandler(client) {
      @Override
      public void connect(URI u) throws IOException { methods.connect(u); }
      @Override
      public void auth(URI u) throws IOException { methods.auth(u); }
      @Override
      public void store(File s, URI d) throws IOException { methods.store(s, d); }
    };

    methods.connect(dst);
    methods.auth(dst);
    methods.store(src, dst);
    client.disconnect();
    replay();

    classUnderTest.upload(src, dst);
    verify();
  }

  public void testUpload_connectFailure() throws Exception {
    File src = new File("/path/to/file");
    URI dst = URI.create("scp://user:pass@host/path");

    classUnderTest = new SCPFileUploadHandler(client) {
      @Override
      public void connect(URI u) throws IOException { methods.connect(u); }
    };

    methods.connect(dst);
    methodsControl.setThrowable(new IOException());
    replay();
    
    try {
      classUnderTest.upload(src, dst);
      fail("expected IOException");
    } catch (IOException e) {}
    verify();
  }

  public void testUpload_authFailure() throws Exception {
    File src = new File("/path/to/file");
    URI dst = URI.create("scp://user:pass@host/path");

    classUnderTest = new SCPFileUploadHandler(client) {
      @Override
      public void connect(URI u) throws IOException { methods.connect(u); }
      @Override
      public void auth(URI u) throws IOException { methods.auth(u); }
    };

    methods.connect(dst);
    methods.auth(dst);
    methodsControl.setThrowable(new IOException());
    client.disconnect();
    replay();

    try {
      classUnderTest.upload(src, dst);
      fail("expected IOException");
    } catch (IOException e) {}
    verify();
  }

  public void testUpload_storeFailure() throws Exception {
    File src = new File("/path/to/file");
    URI dst = URI.create("scp://user:pass@host/path");

    classUnderTest = new SCPFileUploadHandler(client) {
      @Override
      public void connect(URI u) throws IOException { methods.connect(u); }
      @Override
      public void auth(URI u) throws IOException { methods.auth(u); }
      @Override
      public void store(File s, URI d) throws IOException { methods.store(s, d); }
    };

    methods.connect(dst);
    methods.auth(dst);
    methods.store(src, dst);
    methodsControl.setThrowable(new IOException());
    client.disconnect();
    replay();

    try {
      classUnderTest.upload(src, dst);
      fail("expected IOException");
    } catch (IOException e) {}
    verify();
  }

  public void testConnect() throws Exception {
    client.setConnectTimeout(classUnderTest.DEFAULT_CONNECT_TIMEOUT);
    client.setTimeout(classUnderTest.DEFAULT_SOCKET_TIMEOUT);
    client.loadKnownHosts();
    client.connect("host", 1234);
    replay();
    
    classUnderTest.connect(URI.create("scp://user:pass@host:1234/path"));
    verify();
  }

  public void testConnect_defaultPort() throws Exception {
    client.setConnectTimeout(classUnderTest.DEFAULT_CONNECT_TIMEOUT);
    client.setTimeout(classUnderTest.DEFAULT_SOCKET_TIMEOUT);
    client.loadKnownHosts();
    client.connect("host", 22);
    replay();
    
    classUnderTest.connect(URI.create("scp://user:pass@host/path"));
    verify();
  }

  public void testAuth_pubkey() throws Exception {
    client.authPublickey("user");
    replay();

    classUnderTest.auth(URI.create("scp://user@host/path"));
    verify();
  }

  public void testAuth_password() throws Exception {
    client.authPassword("user", "pass");
    replay();

    classUnderTest.auth(URI.create("scp://user:pass@host/path"));
    verify();
  }

  public void testStore() throws Exception {
    File src = new File("/path/to/file");

    client.newSCPFileTransfer();
    clientControl.setReturnValue(xfer);
    xfer.upload("/path/to/file", "/path");
  }

  public void testStore_missingPath() throws Exception {
    File src = new File("/path/to/file");

    client.newSCPFileTransfer();
    clientControl.setReturnValue(xfer);
    xfer.upload("/path/to/file", "/");
  }
}
