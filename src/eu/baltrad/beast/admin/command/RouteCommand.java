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

import java.util.ArrayList;
import java.util.List;

import eu.baltrad.beast.admin.Command;
import eu.baltrad.beast.admin.objects.routes.Route;

/**
 * @author anders
 *
 */
public class RouteCommand extends Command {
  public final static String ADD = "add_route";
  public final static String UPDATE = "update_route";
  public final static String REMOVE = "remove_route";
  public final static String GET = "get_route";
  public final static String LIST = "list_routes";
  public final static String LIST_TYPES = "list_route_types";
  public final static String CREATE_ROUTE_TEMPLATE = "create_route_template";
  /**
   * operation
   */
  private String operation;

  /**
   * The route
   */
  private Route route;
  
  /**
   * If list_routes command is the operation it is possible to filter by type
   */
  private List<String> listRoutesTypes = new ArrayList<String>();
  
  /**
   * If active filter should be used or not
   */
  private boolean useActiveFilter = false;

  /**
   * The active filter to be used.
   */
  private boolean activeFilter = false;
  
  /**
   * If a specific route
   */
  private String templateRouteType = null;
  
  /**
   * Constructor
   * @param operation the operation
   */
  public RouteCommand(String operation) {
    setOperation(operation);
  }

  public RouteCommand(String operation, Route route) {
    setOperation(operation);
    setRoute(route);
  }
  
  /**
   * @return the operation
   */
  @Override
  public String getOperation() {
    return operation;
  }

  /**
   * @param operation the operation to set
   */
  public void setOperation(String operation) {
    this.operation = operation;
  }

  /**
   * @return the route
   */
  public Route getRoute() {
    return route;
  }

  /**
   * @param route the route to set
   */
  public void setRoute(Route route) {
    this.route = route;
  }

  /**
   * @return the listRoutesTypes
   */
  public List<String> getListRoutesTypes() {
    return listRoutesTypes;
  }

  /**
   * @param listRoutesTypes the listRoutesTypes to set
   */
  public void setListRoutesTypes(List<String> listRoutesTypes) {
    if (listRoutesTypes == null) {
      this.listRoutesTypes = new ArrayList<String>();
    } else {
      this.listRoutesTypes = listRoutesTypes;
    }
  }

  /**
   * @return the useActiveFilter
   */
  public boolean useActiveFilter() {
    return useActiveFilter;
  }

  /**
   * @param useActiveFilter the useActiveFilter to set
   */
  public void setUseActiveFilter(boolean useActiveFilter) {
    this.useActiveFilter = useActiveFilter;
  }

  /**
   * @return the activeFilter
   */
  public boolean isActiveFilter() {
    return activeFilter;
  }

  /**
   * @param activeFilter the activeFilter to set
   */
  public void setActiveFilter(boolean activeFilter) {
    this.activeFilter = activeFilter;
  }

  /**
   * @return the templateRouteType
   */
  public String getTemplateRouteType() {
    return templateRouteType;
  }

  /**
   * @param templateRouteType the templateRouteType to set
   */
  public void setTemplateRouteType(String templateRouteType) {
    this.templateRouteType = templateRouteType;
  }
}
