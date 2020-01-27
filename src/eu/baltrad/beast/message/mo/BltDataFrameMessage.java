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
package eu.baltrad.beast.message.mo;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.IBltXmlMessage;

/**
 * @author Anders Henja
 */
public class BltDataFrameMessage implements IBltMessage  {
  /**
   * The headers to be added to the data frame
   */
  private Map<String, String> headers = new HashMap<String, String>();
  
  /**
   * Filename
   */
  private String filename = null;

  /**
   * @param filename the filename to set
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }
  
  /**
   * @return the filename that is stripped of the directory name
   */
  public String getFileBasename() {
    if (filename != null) {
      return filename.substring(filename.lastIndexOf(File.separator)+1);
    }
    return null;
  }

  /**
   * Add a header attribute to be used
   * @param name the header name
   * @param value the value
   */
  public void addHeader(String name, String value) {
    headers.put(name, value);
  }
  
  /**
   * Return the value of the header with specified name
   * @param name the name
   * @return the value
   */
  public String getHeader(String name) {
    return headers.get(name);
  }
  
  /**
   * @return an iterator of all header names
   */
  public Iterator<String> getHeaders() {
    return headers.keySet().iterator();
  }
  
  /**
   * @param name the header name
   * @return if this message contains the specified header attribute or not
   */
  public boolean hasHeader(String name) {
    return headers.containsKey(name);
  }
}
