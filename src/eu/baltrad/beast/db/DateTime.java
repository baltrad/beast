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
package eu.baltrad.beast.db;

import java.util.Formatter;

import eu.baltrad.fc.Date;
import eu.baltrad.fc.Time;

/**
 * Utility class so that it is possible to work with both dates and times
 * at the same time.
 * @author Anders Henja
 */
public class DateTime {
  /**
   * The date
   */
  private Date date = null;
  
  /**
   * The time
   */
  private Time time = null;
  
  /**
   * Default constructor
   */
  public DateTime() {
  }
  
  /**
   * Constructor
   * @param date the date
   * @param time the time
   */
  public DateTime(Date date, Time time) {
    this.date = date;
    this.time = time;
  }
  
  /**
   * @param year the year
   * @param month the month
   * @param day the day
   * @param hour the hour
   * @param minute the minute
   * @param second the second
   */
  public DateTime(int year, int month, int day, int hour, int minute, int second) {
    setDate(year, month, day);
    setTime(hour, minute, second);
  }
  
  /**
   * @param date the date to set
   */
  public void setDate(Date date) {
    this.date = date;
  }
  
  /**
   * @param year the year to set
   * @param month the month to set
   * @param day the day to set
   */
  public void setDate(int year, int month, int day) {
    this.date = new Date(year,month,day);
  }
  
  /**
   * @param time the time to set
   */
  public void setTime(Time time) {
    this.time = time;
  }
  
  /**
   * @param hour the hour to set
   * @param minute the minute to set
   * @param second the second to set
   */
  public void setTime(int hour, int minute, int second) {
    setTime(new Time(hour,minute,second));
  }
  
  /**
   * @param hour the hour to set
   * @param minute the minute to set
   * @param second the second to set
   * @param ms the millisecond to set
   */
  public void setTime(int hour, int minute, int second, int ms) {
    setTime(new Time(hour,minute,second,ms));
  }
  
  /**
   * @return the date
   */
  public Date getDate() {
    return this.date;
  }
  
  /**
   * @return the time
   */
  public Time getTime() {
    return this.time;
  }
  
  /**
   * Compares equality between this instance and the dt
   * @param odt the date time to compare with
   * @return true if equality
   */
  public boolean equals(Object odt) {
    boolean result = false;
    if (odt instanceof DateTime && odt.getClass() == DateTime.class) { 
      DateTime dt = (DateTime)odt;
      Date d = dt.getDate();
      Time t = dt.getTime();
      if (d != null && t != null && date != null && time != null) {
        if (d.year() == date.year() && d.month() == date.month() && d.day() == date.day() &&
            t.hour() == time.hour() && t.minute() == time.minute() && t.second() == time.second()) {
          result = true;
        }
      }
    }
    return result;
  }
  
  public String toString() {
    StringBuffer b = new StringBuffer();
    Formatter formatter = new Formatter(b);
    if (date != null && time != null) {
      formatter.format("%d-%02d-%02d %02d:%02d:%02d", date.year(), date.month(), date.day(), time.hour(), time.minute(), time.second());
    } else {
      formatter.format("unknown");
    }
    return b.toString();
  }
}
