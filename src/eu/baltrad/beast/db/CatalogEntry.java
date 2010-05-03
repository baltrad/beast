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

import eu.baltrad.fc.Date;
import eu.baltrad.fc.Time;

/**
 * @author Anders Henja
 */
public class CatalogEntry {
  private String object = null;
  private String path = null;
  private String src = null;
  private Date date = null;
  private Time time = null;
  
  /**
   * Default constructor
   */
  public CatalogEntry() {
    
  }
  
  /**
   * @param object the object to set
   */
  public void setObject(String object) {
    this.object = object;
  }
  
  /**
   * @return the object
   */
  public String getObject() {
    return object;
  }
  
  /**
   * @param path the path to set
   */
  public void setPath(String path) {
    this.path = path;
  }
  
  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }
  
  /**
   * @param src the source to set
   */
  public void setSource(String src) {
    this.src = src;
  }
  
  /**
   * @return the node
   */
  public String getSource() {
    return src;
  }
  
  /**
   * @param date the date to set
   */
  public void setDate(Date date) {
    this.date = date;
  }
  
  /**
   * @return the date
   */
  public Date getDate() {
    return date;
  }
  
  /**
   * @param time the time to set
   */
  public void setTime(Time time) {
    this.time = time;
  }
  
  /**
   * @return the time
   */
  public Time getTime() {
    return time;
  }
}
