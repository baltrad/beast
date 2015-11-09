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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

public class SCPFileUploadHandlerTest extends EasyMockSupport {
  private static interface MockMethods {
    SSHClient acquireSSHClient();
    SSHClient connect(URI u) throws IOException;
    void auth(SSHClient client, URI u) throws IOException;
    void store(SSHClient client, File s, URI d) throws IOException;
    void disconnect(SSHClient client) throws IOException;
  };

  private SSHClient client;
  private SCPFileTransfer xfer;
  private MockMethods methods;
  private SCPFileUploadHandler classUnderTest;

  @Before
  public void setUp() {
    client = createMock(SSHClient.class);
    xfer = createMock(SCPFileTransfer.class);
    methods = createMock(MockMethods.class);
    classUnderTest = new SCPFileUploadHandler();
  }

  @Test
  public void testUpload() throws Exception {
    File src = new File("/path/to/file");
    URI dst = URI.create("scp://user:pass@host/path");

    classUnderTest = new SCPFileUploadHandler() {
      @Override
      public SSHClient connect(URI u) throws IOException { return methods.connect(u); }
      @Override
      public void auth(SSHClient client, URI u) throws IOException { methods.auth(client, u); }
      @Override
      public void store(SSHClient client, File s, URI d) throws IOException { methods.store(client, s, d); }
      @Override
      public void disconnect(SSHClient client) throws IOException { methods.disconnect(client); }
    };
  
    expect(methods.connect(dst)).andReturn(client);
    methods.auth(client, dst);
    methods.store(client, src, dst);
    methods.disconnect(client);
    
    replayAll();

    classUnderTest.upload(src, dst);
    
    verifyAll();
  }

  @Test
  public void testUpload_connectFailure() throws Exception {
    File src = new File("/path/to/file");
    URI dst = URI.create("scp://user:pass@host/path");

    classUnderTest = new SCPFileUploadHandler() {
      @Override
      public SSHClient connect(URI u) throws IOException { return methods.connect(u); }
    };

    methods.connect(dst);
    expectLastCall().andThrow(new IOException());
    
    replayAll();
    
    try {
      classUnderTest.upload(src, dst);
      fail("expected IOException");
    } catch (IOException e) {}
    
    verifyAll();
  }

  @Test
  public void testUpload_authFailure() throws Exception {
    File src = new File("/path/to/file");
    URI dst = URI.create("scp://user:pass@host/path");

    classUnderTest = new SCPFileUploadHandler() {
      @Override
      public SSHClient connect(URI u) throws IOException { return methods.connect(u); }
      @Override
      public void auth(SSHClient client, URI u) throws IOException { methods.auth(client, u); }
      @Override
      public void disconnect(SSHClient client) throws IOException { methods.disconnect(client); }
    };

    expect(methods.connect(dst)).andReturn(client);
    methods.auth(client, dst);
    expectLastCall().andThrow(new IOException());
    methods.disconnect(client);
    
    replayAll();

    try {
      classUnderTest.upload(src, dst);
      fail("expected IOException");
    } catch (IOException e) {}
    
    verifyAll();
  }

  @Test
  public void testUpload_storeFailure() throws Exception {
    File src = new File("/path/to/file");
    URI dst = URI.create("scp://user:pass@host/path");

    classUnderTest = new SCPFileUploadHandler() {
      @Override
      public SSHClient connect(URI u) throws IOException { return methods.connect(u); }
      @Override
      public void auth(SSHClient client, URI u) throws IOException { methods.auth(client, u); }
      @Override
      public void store(SSHClient client, File s, URI d) throws IOException { methods.store(client, s, d); }
      @Override
      public void disconnect(SSHClient client) throws IOException { methods.disconnect(client); }
    };

    expect(methods.connect(dst)).andReturn(client);
    methods.auth(client, dst);
    methods.store(client, src, dst);
    expectLastCall().andThrow(new IOException());
    methods.disconnect(client);
    
    replayAll();

    try {
      classUnderTest.upload(src, dst);
      fail("expected IOException");
    } catch (IOException e) {}
    
    verifyAll();
  }

  @Test
  public void testConnect() throws Exception {
    classUnderTest = new SCPFileUploadHandler() {
      @Override
      public SSHClient acquireSSHClient() { return client; };
    };
    client.addHostKeyVerifier(isA(PromiscuousVerifier.class));
    client.setConnectTimeout(SCPFileUploadHandler.DEFAULT_CONNECT_TIMEOUT);
    client.setTimeout(SCPFileUploadHandler.DEFAULT_SOCKET_TIMEOUT);
    client.loadKnownHosts();
    client.connect("host", 1234);
    
    replayAll();
    
    classUnderTest.connect(URI.create("scp://user:pass@host:1234/path"));
    
    verifyAll();
  }

  @Test
  public void testConnect_defaultPort() throws Exception {
    classUnderTest = new SCPFileUploadHandler() {
      @Override
      public SSHClient acquireSSHClient() { return client; };
    };
    client.addHostKeyVerifier(isA(PromiscuousVerifier.class));
    client.setConnectTimeout(SCPFileUploadHandler.DEFAULT_CONNECT_TIMEOUT);
    client.setTimeout(SCPFileUploadHandler.DEFAULT_SOCKET_TIMEOUT);
    client.loadKnownHosts();
    client.connect("host", 22);
    
    replayAll();
    
    classUnderTest.connect(URI.create("scp://user:pass@host/path"));
    
    verifyAll();
  }

  @Test
  public void testAuth_pubkey() throws Exception {
    client.authPublickey("user");
    
    replayAll();

    classUnderTest.auth(client, URI.create("scp://user@host/path"));
    
    verifyAll();
  }

  @Test
  public void testAuth_password() throws Exception {
    client.authPassword("user", "pass");
    
    replayAll();

    classUnderTest.auth(client, URI.create("scp://user:pass@host/path"));
    
    verifyAll();
  }

  @Test
  public void testStore() throws Exception {
    expect(client.newSCPFileTransfer()).andReturn(xfer);
    xfer.upload("/path/to/file", "/path");
    replayAll();
    
    classUnderTest.store(client, new File("/path/to/file"), URI.create("/path"));
    
    verifyAll();
    
  }

  @Test
  public void testStore_missingPath() throws Exception {
    expect(client.newSCPFileTransfer()).andReturn(xfer);
    xfer.upload("/path/to/file", "/");
    
    replayAll();

    classUnderTest.store(client, new File("/path/to/file"), URI.create("/"));
    
    verifyAll();
  }
}
