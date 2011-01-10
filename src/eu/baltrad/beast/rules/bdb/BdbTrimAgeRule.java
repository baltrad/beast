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

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;

import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.IRulePropertyAccess;


import eu.baltrad.fc.DateTime;
import eu.baltrad.fc.FileCatalog;
import eu.baltrad.fc.TimeDelta;
import eu.baltrad.fc.db.FileQuery;
import eu.baltrad.fc.db.FileResult;
import eu.baltrad.fc.expr.ExpressionFactory;

/**
 * Rule to keep the age of files in BDB above a limit.
 *
 * @note the nominal datetime (what/date + what/time) is used for file "age"
 */
public class BdbTrimAgeRule implements IRule, IRulePropertyAccess {
  /**
   * The typename of this rule
   */
  public final static String TYPE = "bdb_trim_age";

  private FileCatalog catalog;

  /**
   * limit on file age, in seconds
   */
  private int fileAgeLimit = 0;
  
  /**
   * @return the file count limit, 0 if unlimited
   */
  int getFileAgeLimit() {
    return fileAgeLimit;
  }
  
  /**
   * @param limit file count limit, 0 if unlimited
   */
  void setFileAgeLimit(int limit) {
    fileAgeLimit = limit;
  }
  
  /**
   * @param catalog the catalog to set
   */
  protected void setFileCatalog(FileCatalog catalog) {
    this.catalog = catalog;
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
   * @see eu.baltrad.beast.rules.IRulePropertyAccess#setProperties()
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
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    if (message instanceof BltTriggerJobMessage && fileAgeLimit > 0) {
      execute();
    }
    return null;
  }
  
  /**
   * execute this rule
   */
  protected void execute() {
    FileQuery qry = getExcessiveFileQuery();
    FileResult rset = qry.execute();
    try {
      while (rset.next()) {
        catalog.remove(rset.entry());
      }
    } finally {
      rset.delete();
    }
  }
    
  /**
   * get query for files older than fileAgeLimit
   */
  protected FileQuery getExcessiveFileQuery() {
    ExpressionFactory xpr = new ExpressionFactory();
    FileQuery qry = catalog.query_file();
    qry.filter(xpr.combined_datetime("what/date", "what/time").lt(xpr.datetime(getAgeLimitDateTime())));
    return qry;
  }

  /**
   * get age limit as a DateTime
   */
  protected DateTime getAgeLimitDateTime() {
    TimeDelta dt = new TimeDelta();
    dt.add_seconds(-fileAgeLimit);
    return getCurrentDateTime().add(dt);
  }
  
  /**
   * get current UTC DateTime
   */
  protected DateTime getCurrentDateTime() {
    return DateTime.utc_now();
  }
}
