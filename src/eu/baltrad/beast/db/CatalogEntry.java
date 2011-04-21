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

import eu.baltrad.fc.DateTime;
import eu.baltrad.fc.db.FileEntry;
import eu.baltrad.fc.oh5.Attribute;
import eu.baltrad.fc.oh5.Scalar;

/**
 * @author Anders Henja
 */
public class CatalogEntry {
  private FileEntry entry = null;
  
  /**
   * @param entry the file entry to set
   */
  public void setFileEntry(FileEntry entry) {
    this.entry = entry;
  }
  
  /**
   * @return the associated file entry
   */
  public FileEntry getFileEntry() {
    return this.entry;
  }
  
  /**
   * @return the object
   */
  public String getObject() {
    if (entry != null)
      return entry.what_object();
    else
      return null;
  }
  
  /**
   * @return the node
   */
  public String getSource() {
    if (entry != null)
      return entry.source().get("_name");
    else
      return null;
  }
  
  /**
   * @return the date time
   */
  public DateTime getDateTime() {
    if (entry != null)
      return new DateTime(entry.what_date(), entry.what_time());
    else
      return null;
  }
  
  public Object getAttribute(String name) {
    if (entry == null)
      return null;
    Attribute attr = entry.root().attribute(name);
    if (attr == null)
      return null;
    Scalar value = attr.value();
    if (value.type() == Scalar.Type.STRING) {
      return value.string();
    } else if (value.type() == Scalar.Type.INT64) {
      return new Long(value.int64_());
    } else if (value.type() == Scalar.Type.DOUBLE) {
      return new Double(value.double_());
    } else {
      throw new RuntimeException("unhandled oh5.Scalar type: " + value.type().toString());
    }
  }
}
