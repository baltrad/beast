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
package eu.baltrad.beast.db.filters;

import java.util.ArrayList;
import java.util.List;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.db.FileQuery;
import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.expr.ExpressionFactory;
import eu.baltrad.bdb.oh5.MetadataMatcher;
import eu.baltrad.bdb.util.DateTime;

import eu.baltrad.beast.db.ICatalogFilter;
import eu.baltrad.beast.db.IFilter;

/**
 * A filter for collecting and matching files for a specific compositing 
 * rule, within a time period. 
 * 
 * The filter can either be setup to be scan-based or volume-based, just 
 * as the compositing rule. If scan-based, it will match files with "what/object" 
 * set to "SCAN". Otherwise it will match files with "what/object" set to 
 * "PVOL".
 * 
 * The filter can both be used to check if specific files match the filter 
 * (by using method fileMatches(FileEntry)) and for getting a FileQuery 
 * to use towards the database, to get all files matching the parameters of
 * the filter.
 * 
 * When the filter is use to query the database, the returned files will be 
 * sorted on time ("what/date" + "what/time"), with the oldest file first.
 * 
 * If the filter is set to be scan-based, the files will also be sorted on angle,  
 * with the lowest angle ("where/elangle") first, when applied in a query for the 
 * database, if there are several matches for the other parameters of the 
 * filter.
 * 
 * The filter can also be provided with a list of sources. The filter will 
 * then only match/return files that that have a source id matching any 
 * of the sources in the list.
 * 
 * It is also possible to define a start - stop date/time interval. If so, 
 * it will only fetch files within this time period, if used for querying the 
 * database. You can also ignore specifying start and/or
 * stop date/time but if you ignore both you probably want to use
 * a different filter.
 * 
 * 
 * @author Mats Vernersson
 */
public class CompositingRuleFilter implements ICatalogFilter {
  /**
   * The object type
   */
  private String objectType = null;
  
  /**
   * The source node ids
   */
  private List<String> sources = null;
  
  /**
   * The start date time
   */
  private DateTime startDateTime = null;
  
  /**
   * The stop date time
   */
  private DateTime stopDateTime = null;
  
  /**
   * The quantity to look for
   */
  private String quantity = null;
  
  /**
   * Indicates if the filter is to be used for a scanBased compositing rule or not. If true, 
   * filter will match objects with what/object=SCAN, otherwise what/object=PVOL
   */
  private boolean scanBased = false;
  
  /**
   * Expression factory to use for generating expressions in the filter
   */
  private ExpressionFactory xprFactory = null;
  
  /**
   * Matcher to use for comparing metadata in files against the filter settings
   */
  private MetadataMatcher matcher = null;
  
  /**
   * Additional filter (in addition to the default filtering created for the compositing rule) 
   * that will be applied to check in file matching.
   * 
   */
  private IFilter additionalFileFilter = null;
  
  /**
   * Constructor for the filter
   * 
   * @param scanBased             Indicates if the filter is to be used for a scanBased compositing rule or not.
   * @param quantity              The quantity to look for in files
   * @param sources               The source node ids
   * @param startDateTime         A datetime object describing the start of the period in which to look for files
   * @param stopDateTime          A datetime object describing the end of the period in which to look for files
   * @param additionalFileFilter  Rule specific filter, to be applied when matching files. Can be set to null if 
   *                              no additional filtering shall be applied.
   */
  public CompositingRuleFilter(boolean scanBased, String quantity, List<String> sources, DateTime startDateTime, DateTime stopDateTime, IFilter additionalFileFilter) {

    this.scanBased = scanBased;
    if (scanBased) {
      this.objectType = "SCAN";      
    } else {
      this.objectType = "PVOL";
    }
    
    this.quantity = quantity;
    this.sources = sources;
    this.startDateTime = startDateTime;
    this.stopDateTime = stopDateTime;
    
    this.matcher = new MetadataMatcher();
    this.xprFactory = new ExpressionFactory();
    
    this.additionalFileFilter = additionalFileFilter;
  }
  
  /**
   * @see eu.baltrad.beast.db.ICatalogFilter#apply(eu.baltrad.bdb.db.FileQuery)
   */
  @Override
  public void apply(FileQuery query) {
    Expression filterExpression = getFilterExpression(true);
    query.setFilter(filterExpression);
    
    if (scanBased) {
      query.appendOrderClause(xprFactory.asc(xprFactory.attribute("where/elangle")));
    }
    
    query.appendOrderClause(xprFactory.asc(getDateTimeAttribute()));
  }
  
  /**
   * Checks if the metadata of a file matches the settings of the filter. 
   * 
   * @param file to check against the filter
   * @return True if the file matches the filter. False otherwise.
   */
  public boolean fileMatches(FileEntry file) {
    Expression filterExpression = getFilterExpression(false);
    return matcher.match(file.getMetadata(), filterExpression);
  }
  
  protected Expression getFilterExpression(boolean dbQuery) {
    Expression datetimeAttr = getDateTimeAttribute();

    List<Expression> filters = new ArrayList<Expression>();
    filters.add(xprFactory.eq(xprFactory.attribute("what/object"), xprFactory.literal(objectType)));
    
    if (sources != null) {
      List<Expression> sourceList = new ArrayList<Expression>();
      for (String source : sources) {
        sourceList.add(xprFactory.literal(source));
      }
      filters.add(xprFactory.in(getSourceAttribute(), xprFactory.list(sourceList)));
    }

    if (dbQuery) {
      if (startDateTime != null) {
        filters.add(xprFactory.ge(datetimeAttr, xprFactory.literal(startDateTime)));
      }
      if (stopDateTime != null) {
        filters.add(xprFactory.lt(datetimeAttr, xprFactory.literal(stopDateTime)));
      }
    } else {    
      if (additionalFileFilter != null) {
        filters.add(additionalFileFilter.getExpression());
      }
    }
    
    if (quantity != null) {
      filters.add(xprFactory.eq(xprFactory.attribute("what/quantity"), xprFactory.literal(quantity)));
    }
    
    return xprFactory.and(filters);
  }
  
  protected Expression getDateTimeAttribute() {
    return xprFactory.combinedDateTime("what/date", "what/time");
  }
  
  protected Expression getSourceAttribute() {
    return xprFactory.attribute("_bdb/source_name");
  }
  
  /**
   * Sets the start date time
   * @param dt the date time to set
   */
  public void setStartDateTime(DateTime dt) {
    this.startDateTime = dt;
  }
  
  /**
   * Sets the stop date time
   * @param dt the date time to set
   */
  public void setStopDateTime(DateTime dt) {
    this.stopDateTime = dt;
  }
  
  /**
   * @return the start date
   */
  public DateTime getStartDateTime() {
    return this.startDateTime;
  }
  
  /**
   * @return the stop date
   */
  public DateTime getStopDateTime() {
    return this.stopDateTime;
  }

  /**
   * Sets the object to search for
   * @param object the object to set
   */
  public void setObject(String object) {
    this.objectType = object;
  }
  
  /**
   * @return the object type
   */
  public String getObject() {
    return this.objectType;
  }
  
  /**
   * Sets a list of source names that the filter will match against
   * @param object the object to set
   */
  public void setSources(List<String> sources) {
    this.sources = new ArrayList<String>(sources);
  }
  
  /**
   * @return the list of source names
   */
  public List<String> getSources() {
    return this.sources;
  }

  /**
   * @return the quantity
   */
  public String getQuantity() {
    return quantity;
  }

  /**
   * @param quantity the quantity to set
   */
  public void setQuantity(String quantity) {
    this.quantity = quantity;
  }

  /**
   * @return expression factory
   */
  public ExpressionFactory getXprFactory() {
    return xprFactory;
  }

  /**
   * Sets the expression factory to use
   * 
   * @param xprFactory
   */
  public void setXprFactory(ExpressionFactory xprFactory) {
    this.xprFactory = xprFactory;
  }

  /**
   * @return matcher used in the filter
   */
  public MetadataMatcher getMatcher() {
    return matcher;
  }

  /**
   * Sets the matcher to use for matching files against the filter.
   * 
   * @param matcher
   */
  public void setMatcher(MetadataMatcher matcher) {
    this.matcher = matcher;
  }
}
