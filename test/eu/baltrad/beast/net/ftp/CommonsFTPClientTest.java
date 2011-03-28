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

import java.io.InputStream;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

public class CommonsFTPClientTest extends TestCase {
  private MockControl clientControl;
  private org.apache.commons.net.ftp.FTPClient client;
  private CommonsFTPClient classUnderTest;

  protected void setUp() throws Exception {
    clientControl = MockClassControl.createControl(org.apache.commons.net.ftp.FTPClient.class);
    client = (org.apache.commons.net.ftp.FTPClient)clientControl.getMock();
    classUnderTest = new CommonsFTPClient();
    classUnderTest.setCommonsClient(client);
  }

  protected void replay() {
    clientControl.replay();
  }

  protected void verify() {
    clientControl.verify();
  }

  public void testConnect() throws Exception {
    client.connect("host", 21);
    client.getReplyCode();
    clientControl.setReturnValue(200);
    replay();

    assertTrue(classUnderTest.connect("host", 21));
    verify();
  }

  public void testConnect_failure() throws Exception {
    client.connect("host", 21);
    client.getReplyCode();
    clientControl.setReturnValue(500);
    replay();

    assertFalse(classUnderTest.connect("host", 21));
    verify();
  }

  public void testDisconnect() throws Exception {
    client.disconnect();
    replay();
    
    classUnderTest.disconnect();
    verify();
  }

  public void testDisconnect_notConnected() throws Exception {
    CommonsFTPClient c = new CommonsFTPClient();
    c.disconnect();
  }

  public void testLogin() throws Exception {
    client.login("user", "pass");
    clientControl.setReturnValue(true);
    replay();

    assertTrue(classUnderTest.login("user", "pass"));
    verify();
  }

  public void testLogout() throws Exception {
    client.logout();
    clientControl.setReturnValue(true);
    replay();

    assertTrue(classUnderTest.logout());
    verify();
  }

  public void testGetWorkingDirectory() throws Exception {
    client.printWorkingDirectory();
    clientControl.setReturnValue("path");
    replay();

    assertEquals("path", classUnderTest.getWorkingDirectory());
    verify();
  }

  public void testSetWorkingDirectory() throws Exception {
    client.changeWorkingDirectory("path");
    clientControl.setReturnValue(true);
    replay();
    
    assertTrue(classUnderTest.setWorkingDirectory("path"));
    verify();
  }

  public void testStore() throws Exception {
    InputStream s = new InputStream() {
      @Override
      public int read() { return -1; }
    };

    client.storeFile("name", s);
    clientControl.setReturnValue(true);
    replay();

    assertTrue(classUnderTest.store("name", s));
    verify();
  }

  public void testGetConnectTimeout() {
    client.getConnectTimeout();
    clientControl.setReturnValue(1);
    replay();

    assertEquals(1, classUnderTest.getConnectTimeout());
    verify();
  }

  public void testSetConnectTimeout() {
    client.setConnectTimeout(1);
    replay();

    classUnderTest.setConnectTimeout(1);
    verify();
  }

  public void testGetSoTimeout() throws Exception {
    client.getSoTimeout();
    clientControl.setReturnValue(1);
    replay();

    assertEquals(1, classUnderTest.getSoTimeout());
    verify();
  }

  public void testSetSoTimeout() throws Exception {
    client.setSoTimeout(1);
    replay();

    classUnderTest.setSoTimeout(1);
    verify();
  }
  
  public void testSetBinary() throws Exception {
    client.setFileType(client.BINARY_FILE_TYPE);
    clientControl.setReturnValue(true);
    replay();

    assertTrue(classUnderTest.setBinary(true));
    verify();
  }

  public void testSetBinary_false() throws Exception {
    client.setFileType(client.ASCII_FILE_TYPE);
    clientControl.setReturnValue(true);
    replay();

    assertTrue(classUnderTest.setBinary(false));
    verify();
  }

  public void setPassive() {
    client.enterLocalPassiveMode();
    replay();
    
    classUnderTest.setPassive(true);
    verify();
  }

  public void setPassive_false() {
    client.enterLocalActiveMode();
    replay();
    
    classUnderTest.setPassive(false);
    verify();
  }
}
