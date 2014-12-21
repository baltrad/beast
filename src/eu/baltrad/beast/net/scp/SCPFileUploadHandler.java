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

import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

import eu.baltrad.beast.net.FileUploadHandlerBase;

/**
 * Upload files using SCP.
 *
 * Uses ~/.ssh/known_hosts and ~/.ssh/known_hosts2 for host verification and
 * ~/.ssh/id_rsa and ~/.ssh/id_dsa for keys.
 *
 * More thorough configuration could be provided through query arguments in
 * the future.
 */
public class SCPFileUploadHandler extends FileUploadHandlerBase {
  /**
   * Default constructor.
   */
  public SCPFileUploadHandler() {
  }
    
  public void upload(File src, URI dst) throws IOException {
    SSHClient client = connect(dst);
    try {
      auth(client, dst);
      store(client, src, dst);
    } finally {
      disconnect(client);
    }
  }

  protected SSHClient connect(URI uri) throws IOException {
    SSHClient client = acquireSSHClient();
    client.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
    client.setTimeout(DEFAULT_SOCKET_TIMEOUT);
    client.loadKnownHosts();
    client.connect(uri.getHost(), getPort(uri));
    return client;
  }

  protected void disconnect(SSHClient client) throws IOException {
    client.disconnect();
  }

  protected void auth(SSHClient client, URI uri) throws IOException {
    String pass = getPassword(uri);
    String user = getUser(uri);
    if (pass == null || pass.isEmpty())
      client.authPublickey(user);
    else
      client.authPassword(user, pass);
  }

  protected void store(SSHClient client, File src, URI dst) throws IOException {
    SCPFileTransfer xfer = client.newSCPFileTransfer();
    xfer.upload(src.toString(), getPath(dst));
  }

  protected int getPort(URI uri) {
    int result = uri.getPort();
    if (result == -1)
      result = 22;
    return result;
  }

  protected String getUser(URI uri) {
    String result = new String();
    String userInfo = uri.getUserInfo();
    if (userInfo != null) {
      if (userInfo.indexOf(":") != -1)
        result = userInfo.substring(0, userInfo.indexOf(":"));
      else
        result = userInfo;
    }
    return result;
  }
  
  protected String getPassword(URI uri) {
    String result = new String();
    String userInfo = uri.getUserInfo();
    if (userInfo != null && userInfo.indexOf(":") >= 0)
      result = userInfo.substring(userInfo.indexOf(":") + 1);
    return result;
  }

  protected String getPath(URI uri) {
    String result = uri.getPath();
    if (result == null || result.isEmpty())
      result = "/";
    return result;
  }

  protected SSHClient acquireSSHClient() {
    return new SSHClient(createDefaultConfig());
  }

  protected DefaultConfig createDefaultConfig() {
    if (sshDefaultConfig == null) {
      synchronized (sshDefaultConfigLock) {
        if (sshDefaultConfig == null) {
          sshDefaultConfig = new DefaultConfig();
        }
      }
    }
    return sshDefaultConfig;
  }
  
  protected static final int DEFAULT_CONNECT_TIMEOUT = 10000;
  protected static final int DEFAULT_SOCKET_TIMEOUT = 60000;
  private DefaultConfig sshDefaultConfig = null;
  private Object sshDefaultConfigLock = new Object();
}
