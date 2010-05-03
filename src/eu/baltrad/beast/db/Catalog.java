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

import eu.baltrad.fc.FileCatalog;
import eu.baltrad.fc.Query;
import eu.baltrad.fc.ResultSet;
import eu.baltrad.fc.expr.ExpressionFactory;

/**
 * Helper API for simplifying certain tasks related to the FileCatalog
 * @author Anders Henja
 */
public class Catalog {
  /**
   * The file catalog
   */
  private FileCatalog fc = null;

  /**
   * The expression factory
   */
  private ExpressionFactory xpr = null;
  
  /**
   * Default constructor
   */
  public Catalog() {
    xpr = new ExpressionFactory();
  }
  
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
   * @param xpr the expression factory to set
   */
  public void setExpressionFactory(ExpressionFactory xpr) {
    this.xpr = xpr;
  }
  
  /**
   * Returns a list of entries by applying the provided filter
   * @param filter
   * @return
   */
  public List<CatalogEntry> fetch(ICatalogFilter filter) {
    List<CatalogEntry> result = new ArrayList<CatalogEntry>();
    
    Query q = fc.query();
    
    q.fetch(xpr.attribute("path"));
    q.fetch(xpr.attribute("src_node"));
    q.fetch(xpr.attribute("what/date"));
    q.fetch(xpr.attribute("what/time"));
    q.fetch(xpr.attribute("what/object"));
    
    filter.apply(q);
    
    ResultSet set = q.execute();
    try {
      while (set.next()) {
        CatalogEntry entry = new CatalogEntry();
        entry.setPath(set.string(0));
        entry.setSource(set.string(1));
        entry.setDate(set.date(2));
        entry.setTime(set.time(3));
        entry.setObject(set.string(4));
        result.add(entry);
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
}
