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
package eu.baltrad.beast.net;

import java.io.File;
import java.io.IOException;
import java.net.UnknownServiceException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import eu.baltrad.beast.net.ftp.FTPFileUploadHandler;
import eu.baltrad.beast.net.scp.SCPFileUploadHandler;

public class FileUploader {
  /**
   * Default constructor. Constructs with no handlers and DefaultFileNamer.
   */
  FileUploader() {
    this(new HashMap<String, FileUploadHandler>());
  }

  /**
   * Constructor.
   * @param handlers handler map to use
   */
  public FileUploader(Map<String, FileUploadHandler> handlers) {
    setHandlers(handlers);
  }
    
  /**
   * Create FileUploader with default configuration. The instance has handlers
   * for 'ftp' and 'scp' schemes.
   *
   * @return a new FileUploader instance
   */
  public static FileUploader createDefault() {
    Map<String, FileUploadHandler> handlers = new HashMap<String, FileUploadHandler>();
    handlers.put("ftp", new FTPFileUploadHandler());
    handlers.put("scp", new SCPFileUploadHandler());
    return new FileUploader(handlers);
  }
  
  /**
   * Upload a File. Forwards to upload(java.io.File, java.net.URI);
   */
  public void upload(String src, String dst)
      throws IOException, UnknownServiceException {
    upload(new File(src), URI.create(dst));
  }
  
  /**
   * Upload a File.
   *
   * @param src source file to upload
   * @param dst handler specific destination URI. Handler is determined by
   *        the uri scheme.
   *
   * @throws IOException if the upload fails for any reason
   * @throws UnknownServiceException if no handler is found for the scheme
   */
  public void upload(File src, URI dst)
      throws IOException, UnknownServiceException {
    FileUploadHandler h = getHandlerByScheme(dst.getScheme());
    if (!src.isAbsolute())
      throw new IllegalArgumentException("source path must be absolute");
    if (h == null)
      throw new UnknownServiceException(dst.getScheme());
    h.upload(src, dst);
  }

  protected FileUploadHandler getHandlerByScheme(String scheme) {
    return handlers.get(scheme);
  }

  public void setHandlers(Map<String, FileUploadHandler> handlers) {
    if (handlers == null)
      throw new NullPointerException("handlers == null");
    this.handlers = handlers;
  }

  private Map<String, FileUploadHandler> handlers;
}
