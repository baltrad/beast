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
package eu.baltrad.beast.db.filters;

import eu.baltrad.beast.db.ICatalogFilter;
import eu.baltrad.fc.DateTime;
import eu.baltrad.fc.db.FileQuery;
import eu.baltrad.fc.expr.ExpressionFactory;

/**
 * @author Anders Henja
 */
public class PolarScanAngleFilter implements ICatalogFilter {
  /**
   * If elevation angles should be sorted in ascending order
   */
  public static final int ASCENDING = 0;
  
  /**
   * If elevation angles should be sorted in descending order
   */
  public static final int DESCENDING = 1;
  
  /**
   * The date time
   */
  private DateTime dt = null;
  
  /**
   * The source
   */
  private String source = null;
  
  /**
   * The sort order
   */
  private int order = ASCENDING;
  
  /**
   * The min elevation
   */
  private double minElevation = -90.0;
  
  /**
   * The max elevation
   */
  private double maxElevation = 90.0;
  
  /**
   * @see eu.baltrad.beast.db.ICatalogFilter#apply(eu.baltrad.fc.db.FileQuery)
   */
  @Override
  public void apply(FileQuery query) {
    ExpressionFactory xpr = new ExpressionFactory();

    if (dt == null || source == null) {
      throw new IllegalArgumentException("datetime and source id required");
    }

    query.filter(xpr.eq(xpr.attribute("what/object"), xpr.string("SCAN")));
    query.filter(xpr.eq(xpr.attribute("what/source:_name"), xpr.string(source)));

    query.filter(xpr.eq(xpr.attribute("what/date"), xpr.date(dt)));
    query.filter(xpr.eq(xpr.attribute("what/time"), xpr.time(dt)));

    query.filter(xpr.ge(xpr.attribute("where/elangle"), xpr.double_(minElevation)));
    query.filter(xpr.le(xpr.attribute("where/elangle"), xpr.double_(maxElevation)));
    
    if (this.order == ASCENDING) {
      query.order_by(xpr.attribute("where/elangle"), FileQuery.SortDir.ASC);
    } else {
      query.order_by(xpr.attribute("where/elangle"), FileQuery.SortDir.DESC);
    }
  }

  /**
   * @param dt the date time
   */
  public void setDateTime(DateTime dt) {
    this.dt = dt;
  }

  /**
   * @return the date time
   */
  public DateTime getDateTime() {
    return dt;
  }
  
  /**
   * @param source sets the source, should be source id
   */
  public void setSource(String source) {
    this.source = source;
  }
  
  /**
   * @return the source id
   */
  public String getSource() {
    return this.source;
  }

  /**
   * @param order the sort order
   */
  public void setSortOrder(int order) {
    this.order = order;
  }

  /**
   * @return the sort order
   */
  public int getSortOrder() {
    return order;
  }

  /**
   * @param minElevation the minElevation to set
   */
  public void setMinElevation(double minElevation) {
    this.minElevation = minElevation;
  }

  /**
   * @return the minElevation
   */
  public double getMinElevation() {
    return minElevation;
  }

  /**
   * @param maxElevation the maxElevation to set
   */
  public void setMaxElevation(double maxElevation) {
    this.maxElevation = maxElevation;
  }

  /**
   * @return the maxElevation
   */
  public double getMaxElevation() {
    return maxElevation;
  }
}
