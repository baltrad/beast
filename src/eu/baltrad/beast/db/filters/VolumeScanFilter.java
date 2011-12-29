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
import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.bdb.util.Time;

import eu.baltrad.beast.db.ICatalogFilter;

/**
 * Filter to be used when searching for a number of scans that
 * together will form a volume
 * @author Anders Henja
 */
public class VolumeScanFilter implements ICatalogFilter {
  private String source = null;
  private Date startDate = null;
  private Time startTime = null;
  private Date stopDate = null;
  private Time stopTime = null;
  
  /**
   * @see eu.baltrad.beast.db.ICatalogFilter#apply(eu.baltrad.bdb.db.FileQuery)
   */
  @Override
  public void apply(FileQuery query) {
    ExpressionFactory xpr = new ExpressionFactory();
    Expression dtAttr = xpr.combinedDateTime("what/date", "what/time");
    DateTime startDT = new DateTime(startDate, startTime);
    DateTime stopDT = new DateTime(stopDate, stopTime);

    List<Expression> filters = new ArrayList<Expression>();
    filters.add(xpr.eq(xpr.attribute("what/object"), xpr.literal("SCAN")));
    filters.add(xpr.eq(xpr.attribute("what/source"), xpr.literal(source)));
    filters.add(xpr.ge(dtAttr, xpr.literal(startDT)));
    filters.add(xpr.lt(dtAttr, xpr.literal(stopDT)));
    query.setFilter(xpr.and(filters));
  }
  
  /**
   * Sets the start date time
   * @param date the date to set
   * @param time the time to set
   */
  public void setStartDateTime(Date date, Time time) {
    this.startDate = date;
    this.startTime = time;
  }
  
  /**
   * Sets the stop date time
   * @param date the date to set
   * @param time the time to set
   */
  public void setStopDateTime(Date date, Time time) {
    this.stopDate = date;
    this.stopTime = time;
  }
  
  /**
   * Sets the source
   * @param source the source
   */
  public void setSource(String source) {
    this.source = source;
  }
  
  /**
   * @return the start date
   */
  public Date getStartDate() {
    return this.startDate;
  }
  
  /**
   * @return the start time
   */
  public Time getStartTime() {
    return this.startTime;
  }

  /**
   * @return the stop date
   */
  public Date getStopDate() {
    return this.stopDate;
  }
  
  /**
   * @return the stop time
   */
  public Time getStopTime() {
    return this.stopTime;
  }  
  
  /**
   * @return the object type
   */
  public String getSource() {
    return this.source;
  }
}
