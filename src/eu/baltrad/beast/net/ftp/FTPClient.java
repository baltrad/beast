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
import java.io.IOException;
import java.net.SocketException;

/**
 * FTP client interface.
 */
public interface FTPClient {
  /**
   * Open connection to a server.
   *
   * @param url server to connect to
   * @throws IOException if the connection can't be established
   */
  boolean connect(String host, int port) throws IOException;
  
  /**
   * Close the connection.
   * Multiple calls are allowed (that is calling on an already closed
   * connection doesn't fail).
   */
  void disconnect() throws IOException;
  
  /**
   * Log in to the server.
   *
   * @param user username to use 
   * @param password password to user
   *
   * @return true if the login attempt was successful
   * @throws IOException if an I/O error occurs while communicating with the server
   */
  boolean login(String user, String password) throws IOException;
  
  /**
   * Log out of the server.
   *
   * @return true if the logout was successful
   * @throws IOException if an I/O error occurs while communicating with the server
   */
  boolean logout() throws IOException;
  
  /**
   * Get current working directory on the server.
   *
   * @return the path on the server
   * @throws IOException if an I/O error occurs while communicating with the server
   */
  String getWorkingDirectory() throws IOException;
  
  /**
   * Set working directory on the server.
   *
   * @param path the path to change to on the server
   * @return true if path successfully changed
   * @throws IOException if an I/O error occurs while communicating with the server
   */
  boolean setWorkingDirectory(String path) throws IOException;
  
  /**
   * store a file on the server.
   *
   * @param remote the file name on the server
   * @param local local input stream
   * @return true if successfully completed, false if not
   * @throws IOException if an I/O error occurs while communicating with the server
   */
  boolean store(String remote, InputStream local) throws IOException;

  /**
   * Get timeout on establishing a connection.
   * 
   * @return the timeout in milliseconds
   */
  int getConnectTimeout();

  /**
   * Set timeout on establishing a connection.
   * 
   * @param timeout the timeout to set in milliseconds
   */
  void setConnectTimeout(int timeout);
  
  /**
   * Get timeout of currently open connection.
   * Only call after connect() has been called
   *
   * @return the socket timeout in milliseconds
   */
  int getSoTimeout() throws SocketException;

  /**
   * Set timeout of currently open connection.
   * Only call after connect() has been called
   *
   * @param timeout the timeout to set in milliseconds
   */
  void setSoTimeout(int timeout) throws SocketException;
      
  /**
   * Switch between binary and ascii file modes.
   *
   * @param binary true sets binary mode, false sets ascii mode
   */
  boolean setBinary(boolean binary) throws IOException;

  /**
   * Switch between active and passive modes.
   * If in passive mode, causes a PASV (or EPSV) command to be issued to
   * the server before the opening of every data connection.
   *
   * @param passive true sets passive mode, false sets active mode
   */
  void setPassive(boolean passive);
}
