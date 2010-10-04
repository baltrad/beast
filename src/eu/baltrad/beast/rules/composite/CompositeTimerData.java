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
package eu.baltrad.beast.rules.composite;

import eu.baltrad.fc.DateTime;

/**
 * Used for keeping track on registered tasks in the timeout manager.
 * @author Anders Henja
 */
public class CompositeTimerData {
  /**
   * The date time 
   */
  private DateTime dt = null;
  
  /**
   * The unique rule id
   */
  private int ruleid = -1;
  
  /**
   * Constructor
   * @param dt the date time
   * @throws IllegalArgumentException if dt == null
   */
  public CompositeTimerData(int ruleid, DateTime dt) {
    if (dt == null) {
      throw new IllegalArgumentException();
    }
    this.ruleid = ruleid;
    this.dt = dt;
  }
  
  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object data) {
    boolean result = false;

    if (data instanceof CompositeTimerData && data.getClass() == CompositeTimerData.class) {
      CompositeTimerData ctd = (CompositeTimerData)data;
      DateTime odt = ctd.getDateTime();
      if (dt.date().year() == odt.date().year() &&
          dt.date().month() == odt.date().month() &&
          dt.date().day() == odt.date().day() &&
          dt.time().hour() == odt.time().hour() &&
          dt.time().minute() == odt.time().minute() &&
          dt.time().second() == odt.time().second() &&
          ctd.getRuleId() == this.ruleid) {
          result = true;
      }
    }
    return result;
  }
  
  /**
   * @return the date time
   */
  public DateTime getDateTime() {
    return dt;
  } 
  
  /**
   * @return the rule id
   */
  public int getRuleId() {
    return this.ruleid;
  }
}
