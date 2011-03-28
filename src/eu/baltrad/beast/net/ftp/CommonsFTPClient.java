/*
Copyright 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

This file is part of HLHDF.

HLHDF is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

HLHDF is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with HLHDF.  If not, see <http://www.gnu.org/licenses/>.
*/

package eu.baltrad.beast.net.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

/**
 * FTPClient implementation using org.apache.commons.net.ftp.FTPClient.
 */
public class CommonsFTPClient implements FTPClient {
  private org.apache.commons.net.ftp.FTPClient client;
  
  /**
   * Default constructor.
   */
  public CommonsFTPClient() {
    client = new org.apache.commons.net.ftp.FTPClient();
  }

  public boolean connect(String host, int port) throws IOException {
    client.connect(host, port);
    int reply = client.getReplyCode();
    return org.apache.commons.net.ftp.FTPReply.isPositiveCompletion(reply);
  }

  public void disconnect() throws IOException {
    client.disconnect();
  }

  public boolean login(String user, String password) throws IOException {
    return client.login(user, password);
  }

  public boolean logout() throws IOException {
    return client.logout();
  }

  public String getWorkingDirectory() throws IOException {
    return client.printWorkingDirectory();
  }

  public boolean setWorkingDirectory(String path) throws IOException {
    return client.changeWorkingDirectory(path);
  }

  public boolean store(String remote, InputStream local) throws IOException {
    return client.storeFile(remote, local);
  }

  public int getConnectTimeout() {
    return client.getConnectTimeout();
  }

  public void setConnectTimeout(int timeout) {
    client.setConnectTimeout(timeout);
  }

  public int getSoTimeout() throws SocketException {
    return client.getSoTimeout();
  }

  public void setSoTimeout(int timeout) throws SocketException {
    client.setSoTimeout(timeout);
  }

  public boolean setBinary(boolean binary) throws IOException {
    int fileType = client.ASCII_FILE_TYPE;
    if (binary)
      fileType = client.BINARY_FILE_TYPE;
    return client.setFileType(fileType);
  }

  public void setPassive(boolean passive) {
    if (passive)
      client.enterLocalPassiveMode();
    else
      client.enterLocalActiveMode();
  }
  
  /**
   * Set the underlying client to use. For testing purposes.
   */
  protected void setCommonsClient(org.apache.commons.net.ftp.FTPClient client) {
    this.client = client;
  }
}
