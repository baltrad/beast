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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import eu.baltrad.beast.net.FileUploadHandler;

public class FTPFileUploadHandler implements FileUploadHandler {
  protected static final int DEFAULT_CONNECT_TIMEOUT = 10000;
  protected static final int DEFAULT_SOCKET_TIMEOUT = 60000;

  private FTPClient client;

  public FTPFileUploadHandler() {
    this(new FTPClient());
  }

  public FTPFileUploadHandler(FTPClient client) {
    setClient(client);
  }
  
  @Override
  public void upload(File src, URI dst) throws IOException {
    connect(dst);
    try {
      if (!client.login(getUser(dst), getPassword(dst)))
        throw new IOException("Failed to log in to " + dst.getHost());
      store(src, dst);
    } finally {
      client.disconnect();
    }
  }

  public void setClient(FTPClient client) {
    if (client == null)
      throw new NullPointerException("client == null");
    this.client = client;
  }

  protected void connect(URI uri) throws IOException {
    client.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
    client.connect(uri.getHost(), getPort(uri));
    int reply = client.getReplyCode();
    if (!FTPReply.isPositiveCompletion(reply))
      throw new IOException("Failed to connect to " + uri.getHost());
    client.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);
    client.enterLocalPassiveMode();
  }

  protected void store(File src, URI dst) throws IOException {
    File dstPath = getPath(dst);
    String dstFilename = src.getName();
    if (!isDirectory(dstPath)) {
      dstFilename = dstPath.getName();
      dstPath = dstPath.getParentFile();
    }
    if (!client.changeWorkingDirectory(dstPath.toString()))
      throw new IOException("Failed to cwd: " + dstPath);
    if (!client.setFileType(client.BINARY_FILE_TYPE))
      throw new IOException("Failed to set binary transfer mode");
    if (!client.storeFile(dstFilename, openStream(src)))
      throw new IOException("Failed to store " + src.toString());
  }

  protected boolean isDirectory(File path) throws IOException {
    if ("/".equals(path.toString()))
      return true;

    FTPFile[] dirs = client.listFiles(path.getParent());
    for (FTPFile dir: dirs) {
      if (dir.isDirectory() && dir.getName().equals(path.getName()))
        return true;
    }
    return false;
  }

  protected InputStream openStream(File f) throws IOException {
    return new FileInputStream(f);
  }

  protected String getHost(URI uri) {
    return uri.getHost();
  }

  protected int getPort(URI uri) {
    int result = uri.getPort();
    if (result == -1)
      result = 21;
    return result;
  }
  
  protected String getUser(URI uri) {
    String result = new String();
    String userInfo = uri.getUserInfo();
    if (userInfo != null)
      result = userInfo.substring(0, userInfo.indexOf(":"));
    return result;
  }
  
  protected String getPassword(URI uri) {
    String result = new String();
    String userInfo = uri.getUserInfo();
    if (userInfo != null && userInfo.indexOf(":") >= 0)
      result = userInfo.substring(userInfo.indexOf(":") + 1);
    return result;
  }

  protected File getPath(URI uri) {
    String result = uri.getPath();
    if (result == null || result.isEmpty())
      throw new IllegalArgumentException("no path in URI: " + uri);
    return new File(result);
  }
}
