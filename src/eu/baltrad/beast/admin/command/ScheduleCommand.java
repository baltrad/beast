/* --------------------------------------------------------------------
Copyright (C) 2009-2021 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.admin.command;

import eu.baltrad.beast.admin.Command;

/**
 * @author anders
 */
public class ScheduleCommand extends Command {
  public final static String ADD = "add_schedule";
  public final static String UPDATE = "update_schedule";
  public final static String REMOVE = "remove_schedule";
  public final static String GET = "get_schedule";
  public final static String LIST = "list_schedule";
  
  /**
   * The operation
   */
  private String operation = null;
  
  /**
   * The unique identifier
   */
  private int identfier = 0;
  
  /**
   * The route name affected by this schedule 
   */
  private String routeName = null;
  
  /**
   * The expression
   */
  private String expression = null;
  
  /**
   * Constructor
   */
  public ScheduleCommand() {
  }
  
  /**
   * Constructor
   * @param operation -the operation
   */
  public ScheduleCommand(String operation) {
    setOperation(operation);
  }

  /**
   * Constructor
   */
  public ScheduleCommand(String operation, String expression) {
    setOperation(operation);
    setExpression(expression);
  }
  
  /**
   * @return the operation
   */
  @Override
  public String getOperation() {
    return operation;
  }
  
  /**
   * @param operation the operation
   */
  public void setOperation(String operation) {
    this.operation = operation;
  }

  /**
   * @return the expression
   */
  public String getExpression() {
    return expression;
  }

  /**
   * @param expression the expression to set
   */
  public void setExpression(String expression) {
    this.expression = expression;
  }

  /**
   * @return the identfier
   */
  public int getIdentfier() {
    return identfier;
  }

  /**
   * @param identfier the identfier to set
   */
  public void setIdentfier(int identfier) {
    this.identfier = identfier;
  }

  /**
   * @return the routeName
   */
  public String getRouteName() {
    return routeName;
  }

  /**
   * @param routeName the routeName to set
   */
  public void setRouteName(String routeName) {
    this.routeName = routeName;
  }
}
