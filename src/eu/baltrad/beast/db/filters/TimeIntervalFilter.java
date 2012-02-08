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

import eu.baltrad.bdb.db.FileQuery;
import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.expr.ExpressionFactory;
import eu.baltrad.bdb.util.DateTime;

import eu.baltrad.beast.db.ICatalogFilter;

/**
 * Fetches all objects that are within a specified start - stop
 * date/time interval. You can also ignore specifying start and/or
 * stop date/time but if you ignore both you probably want to use
 * a different filter.
 * 
 * The filter compares:
 *   object equality (REQUIRED)
 *   source node id equality (OPTIONAL)
 *   start date-time <= requested < stop date-time
 *
 * It is also possible to limit the number of returned values by using
 * the limit. Default is unlimited (0). The limiting function is ordered
 * by descending date/time which means that if you specify limit 1 it will
 * be the most recent time within the time interval.
 * 
 * @author Anders Henja
 */
public class TimeIntervalFilter implements ICatalogFilter {
  /**
   * The object type
   */
  private String object = null;
  
  /**
   * The source node id
   */
  private String source = null;
  
  /**
   * The start date time
   */
  private DateTime startDT = null;
  
  /**
   * The stop date time
   */
  private DateTime stopDT = null;
  
  /**
   * The number of returned matches
   */
  private int limit = 0;
  
  /**
   * @see eu.baltrad.beast.db.ICatalogFilter#apply(eu.baltrad.bdb.db.FileQuery)
   */
  @Override
  public void apply(FileQuery query) {
    ExpressionFactory xpr = new ExpressionFactory();
    Expression dtAttr = xpr.combinedDateTime("what/date", "what/time");

    if (object == null) {
      throw new IllegalArgumentException("Must specify object type");
    }

    List<Expression> filters = new ArrayList<Expression>();
    filters.add(xpr.eq(xpr.attribute("what/object"), xpr.literal(object)));
    if (source != null) {
      filters.add(xpr.eq(xpr.attribute("_bdb/source_name"), xpr.literal(source)));
    }

    if (startDT != null) {
      filters.add(xpr.ge(dtAttr, xpr.literal(this.startDT)));
    }
    if (stopDT != null) {
      filters.add(xpr.lt(dtAttr, xpr.literal(this.stopDT)));
    }

    query.setFilter(xpr.and(filters));
    
    if (this.limit > 0) {
      query.appendOrderClause(xpr.desc(dtAttr));
      query.setLimit(this.limit);
    }
  }
  
  /**
   * Sets the start date time
   * @param dt the date time to set
   */
  public void setStartDateTime(DateTime dt) {
    this.startDT = dt;
  }
  
  /**
   * Sets the stop date time
   * @param dt the date time to set
   */
  public void setStopDateTime(DateTime dt) {
    this.stopDT = dt;
  }
  
  /**
   * Sets the object to search for
   * @param object the object to set
   */
  public void setObject(String object) {
    this.object = object;
  }
  
  /**
   * @return the start date
   */
  public DateTime getStartDateTime() {
    return this.startDT;
  }
  
  /**
   * @return the stop date
   */
  public DateTime getStopDateTime() {
    return this.stopDT;
  }
  
  /**
   * @return the object type
   */
  public String getObject() {
    return this.object;
  }

  /**
   * @param source the source to set
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * @return the source
   */
  public String getSource() {
    return source;
  }

  /**
   * @param limit the limit to set
   */
  public void setLimit(int limit) {
    this.limit = limit;
  }

  /**
   * @return the limit
   */
  public int getLimit() {
    return limit;
  }
}
