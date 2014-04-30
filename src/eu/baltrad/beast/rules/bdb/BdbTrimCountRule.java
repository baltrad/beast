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
package eu.baltrad.beast.rules.bdb;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRulePropertyAccess;

import eu.baltrad.bdb.FileCatalog;

/**
 * Rule to keep the number of files in BDB below a limit.
 */
public class BdbTrimCountRule implements IRule, IRulePropertyAccess, InitializingBean {
  /**
   * The typename of this rule
   */
  public final static String TYPE = "bdb_trim_count";

  /**
   * number of files to remove between logging removal progress
   */
  final static int LOG_PROGRESS_FREQUENCY = 100;

  private FileCatalog catalog;

  /**
   * limit on the number of files in the database
   */
  private int fileCountLimit = 0;

  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(BdbTrimCountRule.class);

  /**
   * Default constructor, however, use manager for creation
   */
  protected BdbTrimCountRule() {
  }
  
  /**
   * @return the file count limit, 0 if unlimited
   */
  public int getFileCountLimit() {
    return fileCountLimit;
  }
  
  /**
   * @param limit file count limit, 0 if unlimited
   */
  public void setFileCountLimit(int limit) {
    fileCountLimit = limit;
  }
  
  /**
   * @param catalog the catalog to set
   */
  protected void setFileCatalog(FileCatalog catalog) {
    this.catalog = catalog;
  }
  
  /**
   * @return the catalog
   */
  protected FileCatalog getFileCatalog() {
    return this.catalog;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRulePropertyAccess#getProperties()
   */
  public Map<String, String> getProperties() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("fileCountLimit", Integer.toString(fileCountLimit));
    return props;
  }

  /**
   * @see eu.baltrad.beast.rules.IRulePropertyAccess#setProperties(Map)
   */
  public void setProperties(Map<String, String> props) {
    fileCountLimit = 0;

    if (props.containsKey("fileCountLimit")) {
      try {
        fileCountLimit = Integer.parseInt(props.get("fileCountLimit"));
      } catch (NumberFormatException e) { }
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
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    if (message instanceof BltTriggerJobMessage && fileCountLimit > 0) {
      execute();
    }
    return null;
  }
  
  /**
   * execute this rule
   */
  protected void execute() {
    int fileCount = getFileCount();
    int ctr = fileCount - fileCountLimit;
    int totalNumberOfFilesToRemove = ctr;
    
    if (fileCount <= fileCountLimit) {
      return;
    }
    
    int numRemoved = 0;
    while (ctr > 0) {
      logger.info("Removing files from " + numRemoved + " onwards of " + totalNumberOfFilesToRemove);
      int nrRemoved = catalog.getDatabase().removeFilesByCount(fileCountLimit, LOG_PROGRESS_FREQUENCY);
      ctr -= nrRemoved;
      numRemoved += nrRemoved;
      if (nrRemoved == 0) {
        break;
      }
    }
  }
  
  /**
   * get the number of files in the database
   */
  protected int getFileCount() {
    return (int)catalog.getDatabase().getFileCount();
  }

  /**
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() {
    if (catalog == null) {
      throw new BeanInitializationException("catalog missing");
    }
  }
}
