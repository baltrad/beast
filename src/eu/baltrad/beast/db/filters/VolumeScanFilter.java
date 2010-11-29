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

import eu.baltrad.beast.db.ICatalogFilter;
import eu.baltrad.fc.Date;
import eu.baltrad.fc.DateTime;
import eu.baltrad.fc.Time;
import eu.baltrad.fc.db.AttributeQuery;
import eu.baltrad.fc.expr.Expression;
import eu.baltrad.fc.expr.ExpressionFactory;

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
   * @see eu.baltrad.beast.db.ICatalogFilter#apply(eu.baltrad.fc.db.AttributeQuery)
   */
  @Override
  public void apply(AttributeQuery query) {
    ExpressionFactory xpr = new ExpressionFactory();
    Expression dtAttr = xpr.combined_datetime("what/date", "what/time");
    DateTime startDT = new DateTime(startDate, startTime);
    DateTime stopDT = new DateTime(stopDate, stopTime);

    query.filter(xpr.eq(xpr.attribute("what/object"), xpr.string("SCAN")));
    query.filter(xpr.eq(xpr.attribute("what/source"), xpr.string(source)));
    query.filter(xpr.ge(dtAttr, xpr.datetime(startDT)));
    query.filter(xpr.lt(dtAttr, xpr.datetime(stopDT)));
  }
  
  /**
   * @see eu.baltrad.beast.db.ICatalogFilter#getExtraAttributes()
   */
  @Override
  public String[] getExtraAttributes() {
    return null;
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
