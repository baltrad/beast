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

import java.util.HashMap;
import java.util.Map;

import eu.baltrad.bdb.util.DateTime;

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
   * If composite should be generated from scans or volumes.
   */
  private boolean scanBased = false;
  
  /**
   * Contains the previous time periods angles.
   */
  private Map<String, Double> prevAngles = new HashMap<String, Double>();
  
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
   * Constructor
   * @param ruleid the rule id
   * @param dt the date time
   * @param scanBased if scan based composite or not
   */
  public CompositeTimerData(int ruleid, DateTime dt, boolean scanBased) {
    this(ruleid, dt);
    this.scanBased = scanBased;
  }
  
  /**
   * @see Object#equals(Object)
   * Compares equality on date/time/ruleid and class
   */
  public boolean equals(Object data) {
    boolean result = false;

    if (data instanceof CompositeTimerData && data.getClass() == CompositeTimerData.class) {
      CompositeTimerData ctd = (CompositeTimerData)data;
      DateTime odt = ctd.getDateTime();
      if (dt.equals(odt) && 
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

  /**
   * @param scanBased the scanBased to set
   */
  public void setScanBased(boolean scanBased) {
    this.scanBased = scanBased;
  }

  /**
   * @return the scanBased
   */
  public boolean isScanBased() {
    return scanBased;
  }

  /**
   * @param prevAngles the prevAngles to set
   */
  public void setPreviousAngles(Map<String, Double> prevAngles) {
    if (prevAngles == null) {
      throw new NullPointerException("angles must not be null");
    }
    this.prevAngles = prevAngles;
  }

  /**
   * @return the prevAngles
   */
  public Map<String, Double> getPreviousAngles() {
    return prevAngles;
  }
}
