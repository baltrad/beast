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
package eu.baltrad.beast.scheduler;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * @author Anders Henja
 *
 */
@JsonRootName("schedule")
public class CronEntry {
  private int id = 0;
  private String expression = null;
  private String name = null;

  /**
   * Default constructor
   */
  public CronEntry() {
    
  }
  
  /**
   * Constructor
   * @param expression
   * @param name the name of the job to be executed
   */
  public CronEntry(String expression, String name) {
    setExpression(expression);
    setName(name);
  }
  
  /**
   * Constructor used by the scheduler.
   * @param id the unique id
   * @param expression the cron expression
   * @param name the name of the job to be executed
   */
  public CronEntry(int id, String expression, String name) {
    setId(id);
    setExpression(expression);
    setName(name);
  }
  
  /**
   * @param id the id to set
   */
  @JsonProperty(value="identifier")
  public void setId(int id) {
    this.id = id;
  }
  
  /**
   * @return the id
   */
  @JsonProperty(value="identifier")
  public int getId() {
    return id;
  }

  /**
   * @param expression the expression to set
   */
  public void setExpression(String expression) {
    this.expression = expression;
  }

  /**
   * @return the expression
   */
  public String getExpression() {
    return expression;
  }

  /**
   * @param name the name to set
   */
  @JsonProperty(value="route-name")
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the name
   */
  @JsonProperty(value="route-name")
  public String getName() {
    return name;
  }
}
