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

import java.util.HashMap;
import java.util.Map;

import eu.baltrad.fc.DateTime;

/**
 * @author Anders Henja
 */
public class CatalogEntry {
  private String object = null;
  private String path = null;
  private String src = null;
  private DateTime dt = null;
  private Map<String, Object> attributes = null;
  
  /**
   * Default constructor
   */
  public CatalogEntry() {
    attributes = new HashMap<String, Object>();
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
   * @param dt the date time to set
   */
  public void setDateTime(DateTime dt) {
    this.dt = dt;
  }
  
  /**
   * @return the date time
   */
  public DateTime getDateTime() {
    return this.dt;
  }
  
  /**
   * Adds a attribute to the result
   * @param name the name
   * @param value the value
   */
  public void addAttribute(String name, Object value) {
    attributes.put(name, value);
  }
  
  /**
   * @param name the attributes name
   * @return the value
   */
  public Object getAttribute(String name) {
    return attributes.get(name);
  }
}
