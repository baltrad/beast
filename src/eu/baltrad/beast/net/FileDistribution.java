/* --------------------------------------------------------------------
Copyright (C) 2009-2010 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.net;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownServiceException;
import java.util.HashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import eu.baltrad.beast.net.ftp.FTPFileUploadHandler;
import eu.baltrad.beast.net.scp.SCPFileUploadHandler;
import eu.baltrad.beast.net.scp.SCPOnlyFileUploadHandler;
import eu.baltrad.beast.net.sftp.SFTPFileUploadHandler;

/**
 * Runnable that executes file distributions. The type of file distribution is 
 * determined by the scheme indicated by the destination URI, provided as input to 
 * the constructor. Valid schemes are "copy", "ftp", "scp", "scponly", and "sftp". 
 * 
 * The class provides protection against parallel uploads toward the same destination. 
 * If a distribution is started towards a destination that is already targeted by another 
 * active distribution, it will aborted and a warning will be output to the log.
 *
 */
public class FileDistribution implements Runnable {
  
  private FileUploadHandler uploadHandler;
  private URI fullDestination;
  private File sourceFile;
  
  private static HashMap<URI, String> currentUploads = new HashMap<URI, String>();
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(FileDistribution.class);

  /**
   * @param src
   * @param destination
   * @param entryName
   * 
   * @throws UnknownServiceException thrown if the scheme in the destination URI 
   *                                 is invalid/unknown 
   */
  public FileDistribution(File src, URI destination, String entryName) 
      throws UnknownServiceException {
    uploadHandler = getHandlerByScheme(destination.getScheme());
    sourceFile = src;
    fullDestination = appendPath(destination, entryName);
    logger.info("HANDLER: " + uploadHandler.getClass().getName() + ", sourceFile = " + sourceFile.getName() + ", DEST="+fullDestination.toString());
  }
  
  public FileUploadHandler getUploadHandler() {
    return uploadHandler;
  }

  public void setUploadHandler(FileUploadHandler uploadHandler) {
    this.uploadHandler = uploadHandler;
  }
  
  public static HashMap<URI, String> getCurrentUploads() {
    return currentUploads;
  }

  public static void setCurrentUploads(HashMap<URI, String> currentUploads) {
    FileDistribution.currentUploads = currentUploads;
  }

  public URI getFullDestination() {
    return fullDestination;
  }

  public void setFullDestination(URI fullDestination) {
    this.fullDestination = fullDestination;
  }

  public File getSourceFile() {
    return sourceFile;
  }

  public void setSourceFile(File sourceFile) {
    this.sourceFile = sourceFile;
  }

  @Override
  public void run() {
    if (!sourceFile.isAbsolute()) {
      throw new IllegalArgumentException("Source path must be absolute");      
    }
    
    boolean dstAlreadyLocked = lockUpload(sourceFile, fullDestination);
    if (dstAlreadyLocked) {
      warnAboutOngoingUpload(sourceFile, fullDestination);
    } else {
      try {
        logger.info("UPLOADING " + sourceFile.getName() + ", DEST="+fullDestination.toString());

        uploadHandler.upload(sourceFile, fullDestination);
        logger.info("File " + sourceFile.getName() + " distributed with " + fullDestination.getScheme() + " to: " + fullDestination.getPath());
      } catch (IOException e) {
        logger.error("File distribution failed!", e);
      } finally {
        unlockUpload(fullDestination);          
      }
    }
  }
  
  protected FileUploadHandler getHandlerByScheme(String scheme) throws UnknownServiceException {
    FileUploadHandler uploadHandler = null;
      
    if (scheme == null) {
      throw new UnknownServiceException("No scheme found");
    }
    
    if (scheme.equalsIgnoreCase("ftp")) {
      uploadHandler = new FTPFileUploadHandler();
    } else if (scheme.equalsIgnoreCase("scp")) {
      uploadHandler = new SCPFileUploadHandler();      
    } else if (scheme.equalsIgnoreCase("scponly")) {
      uploadHandler = new SCPOnlyFileUploadHandler();      
    } else if (scheme.equalsIgnoreCase("copy")) {
      uploadHandler = new CopyFileUploadHandler();      
    } else if (scheme.equalsIgnoreCase("sftp")) {
      uploadHandler = new SFTPFileUploadHandler();      
    } else {
      throw new UnknownServiceException(scheme);      
    }
    
    return uploadHandler;
  }
  
  /**
   * Append a path to an URI.
   * 
   * @param uri the URI to append to
   * @param path the path to append
   * @return a new URI with the path appended
   */
  protected URI appendPath(URI uri, String path) {
    return uploadHandler.appendPath(uri, path);
  }
  
  protected boolean lockUpload(File src, URI destination) {
    boolean alreadyLocked = false;
    
    synchronized(getCurrentUploads()) {
      if (getCurrentUploads().containsKey(destination)) {
        alreadyLocked = true;
      } else {
        getCurrentUploads().put(destination, src.getName());
      }
    }
    
    return alreadyLocked;
  }
  
  protected void unlockUpload(URI destination) {   
    synchronized(getCurrentUploads()) {
      getCurrentUploads().remove(destination);
    }
  }
  
  protected void warnAboutOngoingUpload(File src, URI fullDestination) {
    String ongoingSrc = getCurrentUploads().get(fullDestination);
    logger.warn("Upload already ongoing to " + fullDestination.toString() + 
                ". Aborting! Src for ongoing upload: " + ongoingSrc + 
                ", New src: " + src.getName());
  }

}
