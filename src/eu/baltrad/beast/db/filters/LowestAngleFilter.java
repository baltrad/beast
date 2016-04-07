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
 * @author Anders Henja
 */
public class LowestAngleFilter implements ICatalogFilter {
  /**
   * The start datetime we are interested in
   */
  private DateTime startDT = null;

  /**
   * The stop datetime we are interested in
   */
  private DateTime stopDT = null;

  /**
   * The source we are interested in.
   */
  private String source = null;
  
  /**
   * The quantity we are interested in
   */
  private String quantity = null;
  
  /**
   * If item located should be closed to the start date (ascending sort by date/time
   */
  private boolean closestToStartDate = false;
  
  /**
   * @see eu.baltrad.beast.db.ICatalogFilter#apply(eu.baltrad.bdb.db.FileQuery)
   */
  @Override
  public void apply(FileQuery query) {
    ExpressionFactory xpr = new ExpressionFactory();
    Expression dtAttr = xpr.combinedDateTime("what/date", "what/time");

    if (source == null) {
      throw new IllegalArgumentException("datetime and at source required");
    }

    List<Expression> filters = new ArrayList<Expression>();

    filters.add(xpr.eq(xpr.attribute("what/object"), xpr.literal("SCAN")));
    filters.add(xpr.eq(xpr.attribute("_bdb/source_name"), xpr.literal(source)));

    if (quantity != null) {
      filters.add(xpr.eq(xpr.attribute("what/quantity"), xpr.literal(quantity)));
    }
    
    if (startDT != null) {
      filters.add(xpr.ge(dtAttr, xpr.literal(this.startDT)));
    }
    
    if (stopDT != null) {
      filters.add(xpr.lt(dtAttr, xpr.literal(this.stopDT)));
    }

    query.setFilter(xpr.and(filters));
    
    query.appendOrderClause(xpr.asc(xpr.attribute("where/elangle")));
    if (closestToStartDate) {
      query.appendOrderClause(xpr.asc(dtAttr));
    }
    query.setLimit(1);
  }

  /**
   * @param datetime the datetime to set
   */
  public void setStart(DateTime datetime) {
    this.startDT = datetime;
  }

  /**
   * @return the datetime
   */
  public DateTime getStart() {
    return startDT;
  }

  /**
   * @param datetime the datetime to set
   */
  public void setStop(DateTime datetime) {
    this.stopDT = datetime;
  }

  /**
   * @return the datetime
   */
  public DateTime getStop() {
    return stopDT;
  }
  
  /**
   * @param source the source to set
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * @return the sources
   */
  public String getSource() {
    return source;
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
   * If ordering also should consider if closest to start time or not
   */
  public boolean isClosestToStartDate() {
    return closestToStartDate;
  }

  /**
   * If ordering also should consider if closest to start time or not
   */
  public void setClosestToStartDate(boolean closestToStartDate) {
    this.closestToStartDate = closestToStartDate;
  }
}
