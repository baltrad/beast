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
package eu.baltrad.beast.admin.objects.routes;

import org.codehaus.jackson.map.annotate.JsonRootName;

import eu.baltrad.beast.router.IRouterManager;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.gmap.GoogleMapRule;

/**
 * Object used to manipulate {@link GoogleMapRule}
 * @author anders
 */
@JsonRootName("gmap-route")
public class GmapRoute extends Route {
  /**
   * The path where files should be stored
   */
  private String path = null;
  
  /**
   * The area for this gmap rule
   */
  private String area = null;
  
  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * @param path the path to set
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * @return the area
   */
  public String getArea() {
    return area;
  }

  /**
   * @param area the area to set
   */
  public void setArea(String area) {
    this.area = area;
  }
  
  /**
   * Fills self with information from route definition
   * @param def - the route definition
   */
  @Override
  public void fromRouteDefinition(RouteDefinition def) {
    GoogleMapRule rule = (GoogleMapRule)def.getRule();
    this.setAuthor(def.getAuthor());
    this.setDescription(def.getDescription());
    this.setName(def.getName());
    this.setRecipients(def.getRecipients());
    this.setActive(def.isActive());
    
    this.setArea(rule.getArea());
    this.setPath(rule.getPath());
  }

  /**
   * Creates a rule from self using the provided router manager
   * @routerManager - the router manager
   */
  @Override
  public IRule toRule(IRouterManager routerManager) {
    GoogleMapRule rule = (GoogleMapRule)routerManager.createRule(GoogleMapRule.TYPE);

    rule.setArea(this.getArea());
    rule.setPath(this.getPath());
    
    return rule;
  }
}
