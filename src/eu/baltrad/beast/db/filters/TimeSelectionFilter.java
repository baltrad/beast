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

package eu.baltrad.beast.db.filters;

import java.util.ArrayList;
import java.util.List;

import eu.baltrad.bdb.db.FileQuery;
import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.expr.ExpressionFactory;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.beast.db.ICatalogFilter;

/**
 * @author Anders Henja
 *
 */
public class TimeSelectionFilter implements ICatalogFilter {
  /**
   * The individual date times
   */
  private List<DateTime> dateTimes = new ArrayList<DateTime>();
  
  /**
   * The object type we are after
   */
  private String objectType = null;
  
  /**
   * The source node id
   */
  private String source = null;
  
  /**
   * @return the source
   */
  public String getSource() {
    return source;
  }

  /**
   * @param source the source to set
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * @return the objectType
   */
  public String getObjectType() {
    return objectType;
  }

  /**
   * @param objectType the objectType to set
   */
  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  /**
   * @param dt a datetime to use in the filter
   */
  public void addDateTime(DateTime dt) {
    dateTimes.add(dt);
  }
  
  /**
   * @return the list of date times to use
   */
  public List<DateTime> getDateTimes() {
    return dateTimes;
  }
  
  /**
   * @see eu.baltrad.beast.db.ICatalogFilter#apply(eu.baltrad.bdb.db.FileQuery)
   */
  @Override
  public void apply(FileQuery query) {
    ExpressionFactory xpr = new ExpressionFactory();
    Expression dtAttr = xpr.combinedDateTime("what/date", "what/time");

    if (source == null || objectType == null || dateTimes.size() <= 0) {
      throw new IllegalArgumentException("Must specify source, objectType and at least one datetime");
    }
    
    List<Expression> dtFilter = new ArrayList<Expression>();
    for (DateTime dt : dateTimes) {
      dtFilter.add(xpr.eq(dtAttr, xpr.literal(dt)));
    }
    
    List<Expression> andFilter = new ArrayList<Expression>();
    
    andFilter.add(xpr.eq(xpr.attribute("what/object"), xpr.literal(objectType)));
    andFilter.add(xpr.or(dtFilter));
    andFilter.add(xpr.eq(xpr.attribute("what/source:CMT"), xpr.literal(source)));

    query.appendOrderClause(xpr.asc(xpr.attribute("what/date", "string")));
    query.appendOrderClause(xpr.asc(xpr.attribute("what/time", "string")));
    
    query.setFilter(xpr.and(andFilter));
  }
}
