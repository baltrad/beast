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
import eu.baltrad.fc.expr.Expression;
import eu.baltrad.fc.expr.ExpressionFactory;

/**
 * @author Anders Henja
 */
public class LowestAngleFilter implements ICatalogFilter {
  /**
   * The start datetime we are interested in
   */
  private DateTime startDT = null;

  /**
   * The stop datetime we are interested in
   */
  private DateTime stopDT = null;

  /**
   * The source we are interested in.
   */
  private String source = null;
  
  /**
   * @see eu.baltrad.beast.db.ICatalogFilter#apply(eu.baltrad.fc.db.FileQuery)
   */
  @Override
  public void apply(FileQuery query) {
    ExpressionFactory xpr = new ExpressionFactory();
    Expression dtAttr = xpr.combined_datetime("what/date", "what/time");

    if (source == null) {
      throw new IllegalArgumentException("datetime and at source required");
    }

    query.filter(xpr.eq(xpr.attribute("what/object"), xpr.string("SCAN")));
    query.filter(xpr.eq(xpr.attribute("what/source:_name"), xpr.string(source)));
    
    if (startDT != null) {
      query.filter(xpr.ge(dtAttr, xpr.datetime(this.startDT)));
    }
    
    if (stopDT != null) {
      query.filter(xpr.lt(dtAttr, xpr.datetime(this.stopDT)));
    }
    
    query.order_by(xpr.attribute("where/elangle"), FileQuery.SortDir.ASC);
    query.limit(1);
  }

  /**
   * @param datetime the datetime to set
   */
  public void setStart(DateTime datetime) {
    this.startDT = datetime;
  }

  /**
   * @return the datetime
   */
  public DateTime getStart() {
    return startDT;
  }

  /**
   * @param datetime the datetime to set
   */
  public void setStop(DateTime datetime) {
    this.stopDT = datetime;
  }

  /**
   * @return the datetime
   */
  public DateTime getStop() {
    return stopDT;
  }
  
  /**
   * @param source the source to set
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * @return the sources
   */
  public String getSource() {
    return source;
  }
}
