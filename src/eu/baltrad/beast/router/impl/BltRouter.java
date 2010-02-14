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
package eu.baltrad.beast.router.impl;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.router.IRoute;
import eu.baltrad.beast.router.IRouter;
import eu.baltrad.beast.router.IRouterManager;
import eu.baltrad.beast.router.RouteDefinition;

/**
 * The Baltrad router that determines all routes.
 * @author Anders Henja
 */
public class BltRouter implements IRouter, IRouterManager {
  /**
   * The JDBC template managing the database connectivity.
   */
  private SimpleJdbcTemplate template = null;

  /**
   * Sets the data source that should be used by the SimpleJdbcTemplate instance
   * @param source the data source
   */
  public void setDataSource(DataSource source) {
    template = new SimpleJdbcTemplate(source);
  }

	/**
	 * Creates a list of zero or more routes.
	 * @param msg - the message that should result in the route(s).
	 * @return a list of zero or more routes.
	 */
	@Override
	public List<IRoute> getRoutes(IBltMessage msg) {
		// TODO Auto-generated method stub
		return null;
	}

  /**
   * @see eu.baltrad.beast.router.IRouterManager#deleteDefinition(java.lang.String)
   */
  @Override
  public void deleteDefinition(String name) {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see eu.baltrad.beast.router.IRouterManager#getDefinition(java.lang.String)
   */
  @Override
  public RouteDefinition getDefinition(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see eu.baltrad.beast.router.IRouterManager#getDefinitions()
   */
  @Override
  public List<RouteDefinition> getDefinitions() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see eu.baltrad.beast.router.IRouterManager#storeDefinition(eu.baltrad.beast.router.RouteDefinition)
   */
  @Override
  public void storeDefinition(RouteDefinition def) {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see eu.baltrad.beast.router.IRouterManager#updateDefinition(eu.baltrad.beast.router.RouteDefinition)
   */
  @Override
  public void updateDefinition(RouteDefinition def) {
    // TODO Auto-generated method stub
    
  }
}
