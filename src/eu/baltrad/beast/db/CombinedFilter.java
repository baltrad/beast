/*
Copyright 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

This file is part of Beast library.

Beast library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Beast library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Beast library.  If not, see <http://www.gnu.org/licenses/>.
*/

package eu.baltrad.beast.db;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;

import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.expr.ExpressionFactory;

/**
 * Combine different filters.
 *
 * Persistance of child filters is managed through this filter.
 * Child filters are explicitly owned by this filter and should not be shared.
 */
@JsonAutoDetect
public class CombinedFilter implements IFilter {
  private static final String TYPE = "combined";

  private ExpressionFactory xpr = new ExpressionFactory();
  
  private Integer id;
  private MatchType matchType;
  private List<IFilter> childFilters = new ArrayList<IFilter>();

  public enum MatchType {
    ANY,
    ALL
  }

  /**
   * @see IFilter#getType()
   */
  @Override
  public String getType() { return TYPE; }

  /**
   * @see IFilter#getId()
   */
  @Override
  public Integer getId() { return id; }

  /**
   * @see IFilter#setId()
   */
  @Override
  public void setId(Integer id) { this.id = id; }

  /**
   * @see IFilter#getExpression()
   */
  @Override
  public Expression getExpression() {
    List<Expression> exprList = new ArrayList<Expression>(childFilters.size());
    for (IFilter filter : childFilters) {
      exprList.add(filter.getExpression());
    }

    switch (matchType) {
      case ALL:
        return xpr.and(exprList);
      case ANY:
        return xpr.or(exprList);
      default:
        throw new RuntimeException("unhandled matchType: " + matchType);
    }
  }

  /**
   * @see IFilter#isValid()
   */
  public boolean isValid() {
    try {
      return getExpression() != null;
    } catch(Exception e) {
      return false;
    }
  }

  public MatchType getMatchType() { return matchType; }
  public void setMatchType(MatchType type) { this.matchType = type; }

  public List<IFilter> getChildFilters() { return childFilters; }
  public void addChildFilter(IFilter filter) { this.childFilters.add(filter); }
  public void setChildFilters(List<IFilter> filters) { this.childFilters = filters; }
}
