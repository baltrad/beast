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
import eu.baltrad.bdb.db.FileQuery;
import eu.baltrad.bdb.db.FileResult;
import eu.baltrad.bdb.expr.ExpressionFactory;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.bdb.util.TimeDelta;

/**
 * Rule to keep the age of files in BDB above a limit.
 *
 * NOTE! the nominal datetime (what/date + what/time) is used for file "age"
 */
public class BdbTrimAgeRule implements IRule, IRulePropertyAccess, InitializingBean {
  /**
   * The typename of this rule
   */
  public final static String TYPE = "bdb_trim_age";
  
  /**
   * number of files to remove between logging removal progress
   */
  final static int LOG_PROGRESS_FREQUENCY = 100;

  final static int SECONDS_IN_DAY = 86400;

  private FileCatalog catalog;

  /**
   * limit on file age, in seconds
   */
  private int fileAgeLimit = 0;

  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(BdbTrimAgeRule.class);
  
  /**
   * Default constructor, however, use manager for creation
   */
  protected BdbTrimAgeRule() {
  }
  
  /**
   * @return the file count limit, 0 if unlimited
   */
  public int getFileAgeLimit() {
    return fileAgeLimit;
  }
  
  /**
   * @param limit file count limit, 0 if unlimited
   */
  public void setFileAgeLimit(int limit) {
    fileAgeLimit = limit;
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
    props.put("fileAgeLimit", Integer.toString(fileAgeLimit));
    return props;
  }

  /**
   * @see eu.baltrad.beast.rules.IRulePropertyAccess#setProperties(Map)
   */
  public void setProperties(Map<String, String> props) {
    fileAgeLimit = 0;

    if (props.containsKey("fileAgeLimit")) {
      try {
        fileAgeLimit = Integer.parseInt(props.get("fileAgeLimit"));
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
    logger.debug("handle(IBltMessage)");
    if (message instanceof BltTriggerJobMessage && fileAgeLimit > 0) {
      logger.debug("handle(IBltMessage): EXECUTING");
      execute();
    }
    return null;
  }
  
  /**
   * execute this rule
   */
  protected void execute() {
    FileQuery qry = getExcessiveFileQuery();
    FileResult rset = catalog.getDatabase().execute(qry);
    try {
      int numFiles = rset.size();
      int numRemoved = 0;
      while (rset.next()) {
        if ((numRemoved % LOG_PROGRESS_FREQUENCY) == 0) {
          logger.debug("removing files from " + numRemoved +
                       " onwards of " + numFiles);
        }
        catalog.remove(rset.getFileEntry());
        numRemoved += 1;
      }
    } finally {
      rset.close();
    }
  }
    
  /**
   * get query for files older than fileAgeLimit
   */
  protected FileQuery getExcessiveFileQuery() {
    ExpressionFactory xpr = new ExpressionFactory();
    FileQuery qry = new FileQuery();
    qry.setFilter(xpr.lt(
      xpr.combinedDateTime("what/date", "what/time"),
      xpr.literal(getAgeLimitDateTime())
    ));
    return qry;
  }

  /**
   * get age limit as a DateTime
   */
  protected DateTime getAgeLimitDateTime() {
    TimeDelta dt = new TimeDelta();
    dt = dt.addDays(-(fileAgeLimit / SECONDS_IN_DAY));
    dt = dt.addSeconds(-(fileAgeLimit % SECONDS_IN_DAY));
    return getCurrentDateTime().add(dt);
  }
  
  /**
   * get current UTC DateTime
   */
  protected DateTime getCurrentDateTime() {
    return DateTime.utcNow();
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
