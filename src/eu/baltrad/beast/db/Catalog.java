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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;

import eu.baltrad.fc.FileCatalog;
import eu.baltrad.fc.db.FileEntry;
import eu.baltrad.fc.db.FileQuery;
import eu.baltrad.fc.db.FileResult;

/**
 * Helper API for simplifying certain tasks related to the FileCatalog
 * @author Anders Henja
 */
public class Catalog implements InitializingBean {
  /**
   * The file catalog
   */
  private FileCatalog fc = null;

  /**
   * @param fc the catalog to set
   */
  public void setCatalog(FileCatalog fc) {
    this.fc = fc;
  }
  
  /**
   * @return the catalog
   */
  public FileCatalog getCatalog() {
    return this.fc;
  }
  
  /**
   * Returns a list of entries by applying the provided filter.
   * @param filter the filter to use
   * @return a list of entries
   */
  public List<CatalogEntry> fetch(ICatalogFilter filter) {
    List<CatalogEntry> result = new ArrayList<CatalogEntry>();
    FileQuery q = new FileQuery();
    
    filter.apply(q);
    
    FileResult set = fc.database().execute(q);
    try {
      while (set.next()) {
        FileEntry fEntry = set.entry();
        CatalogEntry cEntry = new CatalogEntry();
        cEntry.setFileEntry(fEntry);
        result.add(cEntry);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    } finally {
      if (set != null) {
        set.delete();
      }
    }
    return result;
  }

  /**
   * Returns the location of the file associated with the
   * uuid.
   * @param uuid the uuid
   * @return the file location
   */
  public String getFileCatalogPath(String uuid) {
    return fc.local_path_for_uuid(uuid);
  }
  
  /**
   * Requires that a file catalog instance has been set, otherwise a BeanInitializationException
   * will be thrown.
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (fc == null) {
      throw new BeanInitializationException("FileCatalog missing");
    }
  }
}
