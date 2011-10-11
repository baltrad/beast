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

import org.codehaus.jackson.annotate.JsonAutoDetect;

import eu.baltrad.fc.Expression;
import eu.baltrad.fc.ExpressionFactory;

/**
 * Always matching filter
 */
@JsonAutoDetect
public class AlwaysMatchFilter implements IFilter {
  private static final String TYPE = "always";

  private Integer id;

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
    return new Expression(true);
  }

  /**
   * @see IFilter#isValid()
   */
  @Override
  public boolean isValid() {
    return true;
  }
}
