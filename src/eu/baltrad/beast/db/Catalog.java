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

import eu.baltrad.fc.DateTime;
import eu.baltrad.fc.FileCatalog;
import eu.baltrad.fc.Variant;
import eu.baltrad.fc.db.AttributeQuery;
import eu.baltrad.fc.db.AttributeResult;
import eu.baltrad.fc.expr.ExpressionFactory;

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
   * Returns a list of entries by applying the provided filter.
   * @param filter the filter to use
   * @return a list of entries
   */
  public List<CatalogEntry> fetch(ICatalogFilter filter) {
    List<CatalogEntry> result = new ArrayList<CatalogEntry>();
    String[] attributes = filter.getExtraAttributes();
    AttributeQuery q = fc.query_attribute();
    
    q.fetch(xpr.attribute("file:uuid"));
    q.fetch(xpr.attribute("what/source:node"));
    q.fetch(xpr.attribute("what/date"));
    q.fetch(xpr.attribute("what/time"));
    q.fetch(xpr.attribute("what/object"));
    
    filter.apply(q);
    
    AttributeResult set = q.execute();
    try {
      while (set.next()) {
        CatalogEntry entry = new CatalogEntry();
        String uuid = set.string(0);
	String path = fc.storage().store(fc.database().entry_by_uuid(uuid));
	entry.setPath(path);
        entry.setSource(set.string(1));
        entry.setDateTime(new DateTime(set.date(2), set.time(3)));
        entry.setObject(set.string(4));
        if (attributes != null) {
          int alen = 5 + attributes.length;
          int idx = 0;
          for (int ctr = 5; ctr < alen; ctr++, idx++) {
            Object obj = null;
            Variant v = set.value_at(ctr);
            if (v.is_bool()) {
              obj = new Boolean(v.to_bool());
            } else if (v.is_date()) {
              obj = v.to_date();
            } else if (v.is_double()) {
              obj = new Double(v.to_double());
            } else if (v.is_int64()) {
              obj = new Long(v.to_int64());
            } else if (v.is_string()) {
              obj = v.to_string();
            } else if (v.is_time()) {
              obj = v.to_time();
            }
            entry.addAttribute(attributes[idx], obj);
          }
        }
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
