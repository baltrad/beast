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

package eu.baltrad.beast.net.sftp;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.fail;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;

/**
 * @author Anders Henja
 *
 */
public class SFTPFileUploadHandlerTest extends EasyMockSupport {
  private SSHClient client = null;
  private SFTPFileUploadHandler classUnderTest = null;
  
  @Before
  public void setUp() {
    client = createMock(SSHClient.class);
    classUnderTest = new SFTPFileUploadHandler();
  }
  
  @After
  public void tearDown() {
    classUnderTest = null;
    client = null;
  }
  
  @Test
  public void store() throws Exception {
    SSHClient client = createMock(SSHClient.class);
    File source = new File("/tmp/slask.h5");
    URI destination = URI.create("/tmp/something.h5");
    
    SFTPClient sftp = createMock(SFTPClient.class);
    expect(client.newSFTPClient()).andReturn(sftp);
    sftp.put("/tmp/slask.h5", "/tmp/something.h5");
    sftp.close();
    
    replayAll();
    classUnderTest.store(client, source, destination);
    verifyAll();
  }

  @Test
  public void store_failedConnect() throws Exception {
    SSHClient client = createMock(SSHClient.class);
    File source = new File("/tmp/slask.h5");
    URI destination = URI.create("/tmp/something.h5");
    
    expect(client.newSFTPClient()).andThrow(new IOException("HOO"));
    
    replayAll();
    try {
      classUnderTest.store(client, source, destination);
      fail("Expected exception");
    } catch (IOException e) {
      // pass
    }
    verifyAll();
  }  
  
  @Test
  public void store_failedPut() throws Exception {
    SSHClient client = createMock(SSHClient.class);
    File source = new File("/tmp/slask.h5");
    URI destination = URI.create("/tmp/something.h5");
    
    SFTPClient sftp = createMock(SFTPClient.class);
    expect(client.newSFTPClient()).andReturn(sftp);
    sftp.put("/tmp/slask.h5", "/tmp/something.h5");
    expectLastCall().andThrow(new IOException("HOO"));
    sftp.close();
    
    replayAll();
    try {
      classUnderTest.store(client, source, destination);
      fail("Expected exception");
    } catch (IOException e) {
      // pass
    }
    verifyAll();
  }  
}
