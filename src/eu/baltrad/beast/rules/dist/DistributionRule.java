/* --------------------------------------------------------------------
Copyright (C) 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.rules.dist;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.net.FileUploader;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRulePropertyAccess;
import eu.baltrad.beast.rules.namer.MetadataNameCreatorFactory;
import eu.baltrad.beast.rules.namer.TemplateNameCreatorMetadataNamer;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.oh5.MetadataMatcher;
import eu.baltrad.bdb.storage.LocalStorage;
import eu.baltrad.bdb.util.FileEntryNamer;
import eu.baltrad.bdb.util.MetadataFileEntryNamer;
import eu.baltrad.bdb.util.UuidFileEntryNamer;

/**
 * Distribute incoming data to remote destinations. Incoming files that match
 * the associated filter are uploaded using
 * {@link eu.baltrad.beast.net.FileUploader FileUploader}.
 */
public class DistributionRule implements IRule, IRulePropertyAccess {
  public final static String TYPE = "distribution";
  
  private static HashMap<URI, String> currentUploads = new HashMap<URI, String>();

  /**
   * The unique rule id separating this rule from the others
   */
  private int ruleid = -1;
  
  private IFilter filter;
  private MetadataMatcher matcher;
  private URI destination;
  private FileUploader uploader;
  private LocalStorage localStorage;
  private FileEntryNamer namer;
  private MetadataNameCreatorFactory nameCreatorFactory;
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(DistributionRule.class);
  
  /**
   * Constructor.
   */
  protected DistributionRule(LocalStorage localStorage) {
    this.matcher = new MetadataMatcher();
    this.uploader = FileUploader.createDefault();
    this.localStorage = localStorage;
    this.setNameCreatorFactory(new MetadataNameCreatorFactory());
  }

  protected void setMatcher(MetadataMatcher matcher) {
    this.matcher = matcher;
  }

  protected void setUploader(FileUploader uploader) {
    this.uploader = uploader;
  }

  public MetadataNameCreatorFactory getNameCreatorFactory() {
    return nameCreatorFactory;
  }

  public void setNameCreatorFactory(MetadataNameCreatorFactory nameCreatorFactory) {
    this.nameCreatorFactory = nameCreatorFactory;
  }
  
  public IFilter getFilter() {
    return filter;
  }

  public void setFilter(IFilter filter) {
    this.filter = filter;
  }

  public URI getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = URI.create(destination);
  }

  public void setNamer(FileEntryNamer namer) {
    this.namer = namer;
  }

  /**
   * Set metadata naming template.
   */
  public void setMetadataNamingTemplate(String tmpl) {
    TemplateNameCreatorMetadataNamer mnamer = new TemplateNameCreatorMetadataNamer(tmpl);
    mnamer.setFactory(nameCreatorFactory);
    mnamer.afterPropertiesSet();
    this.namer = new MetadataFileEntryNamer(mnamer);
  }

  /**
   * Get metadata naming template.
   *
   * @return the template or null of some other namer is used.
   */
  public String getMetadataNamingTemplate() {
    if (namer == null)
      return null;

    try {
      MetadataFileEntryNamer metadataNamer = (MetadataFileEntryNamer)namer;
      TemplateNameCreatorMetadataNamer templateNamer = (TemplateNameCreatorMetadataNamer)metadataNamer.getMetadataNamer();
      return templateNamer.getTemplate();
    } catch (ClassCastException e) {
      return null;
    }
  }

  public void setUuidNamer() {
    this.namer = new UuidFileEntryNamer();
  }

  protected LocalStorage getLocalStorage() {
    return this.localStorage;
  }

  /**
   * @see eu.baltrad.beast.rules.IRulePropertyAccess#getProperties()
   */
  @Override
  public Map<String, String> getProperties() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("destination", destination.toString());
    String metadataNamingTemplate = getMetadataNamingTemplate();
    if (metadataNamingTemplate != null) {
      props.put("metadataNamingTemplate", metadataNamingTemplate);
    }
    return props;
  }

  /**
   * @see eu.baltrad.beast.rules.IRulePropertyAccess#setProperties(Map)
   */
  @Override
  public void setProperties(Map<String, String> props) {
    if (props.containsKey("destination")) {
      this.destination = URI.create(props.get("destination"));
    }
    if (props.containsKey("metadataNamingTemplate")) {
      setMetadataNamingTemplate(props.get("metadataNamingTemplate"));
    } else {
      setUuidNamer();
    }
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#getType()
   */
  @Override
  public String getType() {
    return TYPE;
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#isValid()
   */
  @Override
  public boolean isValid() {
    return true;
  }
  
  /**
   * @param ruleid the ruleid to set
   */
  public void setRuleId(int ruleid) {
    this.ruleid = ruleid;
  }

  /**
   * @return the ruleid
   */
  public int getRuleId() {
    return ruleid;
  }
  
  public static HashMap<URI, String> getCurrentUploads() {
    return currentUploads;
  }

  public static void setCurrentUploads(HashMap<URI, String> currentUploads) {
    DistributionRule.currentUploads = currentUploads;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    logger.debug("ENTER: handle(IBltMessage)");
    if (message instanceof BltDataMessage) {
      FileEntry entry = ((BltDataMessage)message).getFileEntry();
      if (match(entry)) {
        logger.info("ENTER: execute ruleId: " + getRuleId() + ", thread: " + Thread.currentThread().getName());
        upload(entry);
        logger.info("EXIT: execute ruleId: " + getRuleId() + ", thread: " + Thread.currentThread().getName());
      }
    }
    logger.debug("EXIT: handle(IBltMessage)");
    return null;
  }

  public boolean match(FileEntry entry) {
    return matcher.match(entry.getMetadata(), filter.getExpression());
  }

  public void upload(FileEntry entry) {
    File src = localStorage.store(entry);
    try {
      upload(src, entry);
    } catch (IOException e) {
      logger.error("upload failed", e);
    }
  }
  
  public void upload(File src, FileEntry entry) throws IOException {
    String entryName = namer.name(entry);
    URI fullDestination = uploader.appendPath(destination, entryName);
    
    boolean dstAlreadyLocked = lockUpload(src, fullDestination);
    if (dstAlreadyLocked) {
      warnAboutOngoingUpload(src, fullDestination);
    } else {
      try {
        uploader.upload(src, fullDestination);
      } finally {
        unlockUpload(fullDestination);          
      }
    }
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
                "! Src for ongoing upload: " + ongoingSrc + 
                ", New src: " + src.getName());
  }

}
