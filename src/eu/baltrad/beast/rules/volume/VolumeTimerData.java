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
package eu.baltrad.beast.rules.volume;

import eu.baltrad.beast.db.DateTime;

/**
 * @author Anders Henja
 *
 */
public class VolumeTimerData {
  /**
   * The date time 
   */
  private DateTime dt = null;
  
  /**
   * The source of the data
   */
  private String source = null;
  
  /**
   * The number of bins for each ray
   */
  private long nbins = 0;
  
  /**
   * The scale of each bin (in meters)
   */
  private double rscale = 0.0;
  
  /**
   * The unique rule id
   */
  private int ruleid = -1;
  
  /**
   * Constructor
   * @param ruleid the unique id
   * @param dt the date time
   * @param source the source id
   * @param nbins the number of bins for a ray
   * @param rscale the length (in meters) of each bin
   * @throws IllegalArgumentException if dt == null
   */
  public VolumeTimerData(int ruleid, DateTime dt, String source, long nbins, double rscale) {
    if (dt == null || source == null || nbins <= 0 || rscale <= 0.0) {
      throw new IllegalArgumentException();
    }
    this.ruleid = ruleid;
    this.dt = dt;
    this.source = source;
    this.nbins = nbins;
    this.rscale = rscale;
  }
  
  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object data) {
    boolean result = false;

    if (data instanceof VolumeTimerData && data.getClass() == VolumeTimerData.class) {
      VolumeTimerData vtd = (VolumeTimerData)data;
      result = this.dt.equals(vtd.getDateTime()) && vtd.getRuleId() == this.ruleid && 
      vtd.getSource().equals(this.source) && this.nbins == vtd.getNBins() && this.rscale == vtd.getRScale();
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
   * @return the source id
   */
  public String getSource() {
    return this.source;
  }
  
  /**
   * @return the number of bins
   */
  public long getNBins() {
    return this.nbins;
  }
  
  /**
   * @return the length of each bin in meters.
   */
  public double getRScale() {
    return this.rscale;
  }
}
