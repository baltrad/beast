/* --------------------------------------------------------------------
Copyright (C) 2009-2013 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.pgf;

/**
 * @author Anders Henja
 *        result[k] = {"id":a.id, "xsize":a.xsize, "ysize":a.ysize, "xscale":a.xscale, "yscale":a.yscale, "extent":a.extent, "pcs":a.projection.definition}
 */
public class AreaInformation {
  private String id;
  private int xsize;
  private int ysize;
  private double xscale;
  private double yscale;
  private double extent[];
  private String pcs;
  
  /**
   * Constructor
   * @param id the id of this area
   */
  public AreaInformation(String id) {
    this.id = id;
  }
  
  /**
   * @return this areas id
   */
  public String getId() {
    return id;
  }
  
  /**
   * @return the xsize of the area
   */
  public int getXsize() {
    return xsize;
  }
  
  /**
   * @param xsize the xsize of the area
   */
  public void setXsize(int xsize) {
    this.xsize = xsize;
  }
  
  /**
   * @return the ysize of the area
   */
  public int getYsize() {
    return ysize;
  }
  
  /**
   * @param ysize the ysize of the area
   */
  public void setYsize(int ysize) {
    this.ysize = ysize;
  }
  
  /**
   * @return the xscale
   */
  public double getXscale() {
    return xscale;
  }
  
  /**
   * @param xscale the xscale
   */
  public void setXscale(double xscale) {
    this.xscale = xscale;
  }
  
  /**
   * @return the yscale
   */
  public double getYscale() {
    return yscale;
  }
  
  /**
   * @param yscale the yscale
   */
  public void setYscale(double yscale) {
    this.yscale = yscale;
  }
  
  /**
   * @return the extent, consists of 4 doubles
   */
  public double[] getExtent() {
    return extent;
  }
  
  /**
   * @param extent the extent, consists of 4 doubles
   */
  public void setExtent(double extent[]) {
    this.extent = extent;
  }
  
  /**
   * @return the projection definition in PROJ.4 style
   */
  public String getPcs() {
    return pcs;
  }
  
  /**
   * @param pcs the projection definition in PROJ.4 style
   */
  public void setPcs(String pcs) {
    this.pcs = pcs;
  }
  
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("Area=").append(id).append(", size=").append(xsize).append("x").append(ysize).append(", scale=").append(xscale).append("x").append(yscale);
    buf.append(", extent=(").append(extent[0]).append(",").append(extent[1]).append(",").append(extent[2]).append(",").append(extent[3]).append("), pcs=").append(pcs);
    return buf.toString();
  }
}
