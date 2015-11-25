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

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.oh5.Attribute;
import eu.baltrad.bdb.util.DateTime;

/**
 * @author Anders Henja
 */
public class CatalogEntry {
  private FileEntry entry = null;
  
  /**
   * Default constructor
   */
  public CatalogEntry() {
  }

  /**
   * Constructor
   * @param entry the file entry to wrap
   */
  public CatalogEntry(FileEntry entry) {
    setFileEntry(entry);
  }
  
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
   * @return the unique identifier
   */
  public String getUuid() {
    if (entry != null)
      return entry.getUuid().toString();
    else
      return null;
  }
  
  /**
   * @return the object
   */
  public String getObject() {
    if (entry != null)
      return entry.getMetadata().getWhatObject();
    else
      return null;
  }
  
  /**
   * @return the node
   */
  public String getSource() {
    if (entry != null)
      return entry.getSource().getName();
    else
      return null;
  }
  
  /**
   * @return the date time
   */
  public DateTime getDateTime() {
    if (entry != null) {
      return new DateTime(
        entry.getMetadata().getWhatDate(),
        entry.getMetadata().getWhatTime()
      );
    } else {
      return null;
    }
  }
  
  public Object getAttribute(String name) {
    if (entry == null)
      return null;
    Attribute attr = entry.getMetadata().getAttribute(name);
    if (attr == null)
      return null;

    switch (attr.getType()) {
      case STRING:
        return attr.toString();
      case LONG:
        return attr.toLong();
      case DOUBLE:
        return attr.toDouble();
      default:
        throw new RuntimeException(
          "unhandled attribute type: " + attr.getType()
        );
    }
  }
}
