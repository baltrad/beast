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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonRootName;

import eu.baltrad.beast.db.IFilter;
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
   * If area name should be used in the path or not
   */
  private boolean useAreaInPath = true;
  
  
  /**
   * The area for this gmap rule
   */
  private String area = null;
  
  /**
   * The matching filter for doing more fine-grained selections
   */
  private IFilter filter = null;

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
   * @return the useAreaInPath
   */
  public boolean isUseAreaInPath() {
    return useAreaInPath;
  }

  /**
   * @param useAreaInPath the useAreaInPath to set
   */
  public void setUseAreaInPath(boolean useAreaInPath) {
    this.useAreaInPath = useAreaInPath;
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
   * @return the filter
   */
  public IFilter getFilter() {
    return filter;
  }

  /**
   * @param filter the filter to set
   */
  public void setFilter(IFilter filter) {
    this.filter = filter;
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
    this.setUseAreaInPath(rule.isUseAreaInPath());
    this.setPath(rule.getPath());
    this.setFilter(rule.getFilter());
  }

  /**
   * Creates a rule from self using the provided router manager
   * @routerManager - the router manager
   */
  @Override
  public IRule toRule(IRouterManager routerManager) {
    GoogleMapRule rule = (GoogleMapRule)routerManager.createRule(GoogleMapRule.TYPE);

    rule.setArea(this.getArea());
    rule.setUseAreaInPath(this.isUseAreaInPath());
    rule.setPath(this.getPath());
    rule.setFilter(this.getFilter());
    
    return rule;
  }

  @Override
  @JsonIgnore
  public boolean isValid() {
    if (getName() != null && !getName().isEmpty() &&
        getArea() != null && !getArea().isEmpty() &&
        getPath() != null && !getPath().isEmpty() &&
        getRecipients().size() > 0) {
      return true;
    }
    return false;
  }
}
