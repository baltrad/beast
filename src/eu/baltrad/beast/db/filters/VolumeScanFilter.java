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
import eu.baltrad.fc.Time;
import eu.baltrad.fc.db.AttributeQuery;
import eu.baltrad.fc.expr.ExpressionFactory;
import eu.baltrad.fc.expr.Literal;

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
    Literal xprStartDate = xpr.date(startDate.year(), startDate.month(), startDate.day());
    Literal xprStartTime = xpr.time(startTime.hour(), startTime.minute(), startTime.second());
    Literal xprStopDate = xpr.date(stopDate.year(), stopDate.month(), stopDate.day());
    Literal xprStopTime = xpr.time(stopTime.hour(), stopTime.minute(), stopTime.second());

    query.filter(xpr.eq(xpr.attribute("what/object"), xpr.string("SCAN")));
    query.filter(xpr.eq(xpr.attribute("what/source"), xpr.string(source)));
    query.filter(xpr.ge(xpr.attribute("what/date"), xprStartDate));
    query.filter(xpr.ge(xpr.attribute("what/time"), xprStartTime));
    query.filter(xpr.le(xpr.attribute("what/date"), xprStopDate));
    query.filter(xpr.lt(xpr.attribute("what/time"), xprStopTime));    
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
