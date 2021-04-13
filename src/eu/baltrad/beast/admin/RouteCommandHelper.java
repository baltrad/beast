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
package eu.baltrad.beast.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;

import eu.baltrad.beast.admin.objects.routes.AcrrRoute;
import eu.baltrad.beast.admin.objects.routes.CompositeRoute;
import eu.baltrad.beast.admin.objects.routes.DBTrimAgeRoute;
import eu.baltrad.beast.admin.objects.routes.DBTrimCountRoute;
import eu.baltrad.beast.admin.objects.routes.DistributionRoute;
import eu.baltrad.beast.admin.objects.routes.GmapRoute;
import eu.baltrad.beast.admin.objects.routes.GraRoute;
import eu.baltrad.beast.admin.objects.routes.Route;
import eu.baltrad.beast.admin.objects.routes.ScansunRoute;
import eu.baltrad.beast.admin.objects.routes.Site2DRoute;
import eu.baltrad.beast.admin.objects.routes.VolumeRoute;
import eu.baltrad.beast.admin.objects.routes.WrwpRoute;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.acrr.AcrrRule;
import eu.baltrad.beast.rules.bdb.BdbTrimAgeRule;
import eu.baltrad.beast.rules.bdb.BdbTrimCountRule;
import eu.baltrad.beast.rules.composite.CompositingRule;
import eu.baltrad.beast.rules.dist.DistributionRule;
import eu.baltrad.beast.rules.gmap.GoogleMapRule;
import eu.baltrad.beast.rules.gra.GraRule;
import eu.baltrad.beast.rules.scansun.ScansunRule;
import eu.baltrad.beast.rules.site2d.Site2DRule;
import eu.baltrad.beast.rules.volume.VolumeRule;
import eu.baltrad.beast.rules.wrwp.WrwpRule;

/**
 * @author anders
 */
public class RouteCommandHelper implements InitializingBean{
  /**
   * Container class for mapping between routes and rules
   */
  public static class RouteMapping {
    private String routeName;
    private String ruleType;
    private Class<? extends Route> routeClass;
    public RouteMapping(String routeName, String ruleType, Class<? extends Route> routeClass) {
      this.routeName = routeName;
      this.ruleType = ruleType;
      this.routeClass = routeClass;
    }
    public String getRouteName() {
      return routeName;
    }
    public String getRuleType() {
      return ruleType;
    }
    public Class<? extends Route> getRouteClass() {
      return routeClass;
    }
  };

  /**
   * The mapping of routes and rules
   */
  private Map<String, RouteCommandHelper.RouteMapping> mappings = new HashMap<String, RouteCommandHelper.RouteMapping>();
  
  @SuppressWarnings("serial")
  private static Set<RouteMapping> ROUTE_SET = new HashSet<RouteMapping>() {{
    add(new RouteMapping("composite-route", CompositingRule.TYPE, CompositeRoute.class));
    add(new RouteMapping("site2d-route", Site2DRule.TYPE, Site2DRoute.class));
    add(new RouteMapping("gmap-route", GoogleMapRule.TYPE, GmapRoute.class));
    add(new RouteMapping("volume-route", VolumeRule.TYPE, VolumeRoute.class));
    add(new RouteMapping("db-trim-count-route", BdbTrimCountRule.TYPE, DBTrimCountRoute.class));
    add(new RouteMapping("db-trim-age-route", BdbTrimAgeRule.TYPE, DBTrimAgeRoute.class));
    add(new RouteMapping("distribution-route", DistributionRule.TYPE, DistributionRoute.class));
    add(new RouteMapping("acrr-route", AcrrRule.TYPE, AcrrRoute.class));
    add(new RouteMapping("gra-route", GraRule.TYPE, GraRoute.class));
    add(new RouteMapping("wrwp-route", WrwpRule.TYPE, WrwpRoute.class));
    add(new RouteMapping("scansun-route", ScansunRule.TYPE, ScansunRoute.class));
  }};
  
  /**
   * Constructor
   */
  public RouteCommandHelper() {
    setup(ROUTE_SET);
  }
  
  /**
   * Sets up a mapping
   * @param routeSet the route set
   */
  public void setup(Set<RouteMapping> routes) {
    mappings.clear();
    for (RouteMapping m : routes) {
      mappings.put(m.getRouteName(), m);
    }
  }
  
  /**
   * Returns a list of rule names from the route names
   * @param types the list of types
   * @return a list of rule types
   */
  public List<String> translateRouteTypesToRuleNames(List<String> types) {
    List<String> result = new ArrayList<String>();
    for (String t : types) {
      if (mappings.containsKey(t)) {
        result.add(mappings.get(t).getRuleType());
      }
    }
    return result;
  }
  
  /**
   * Creates a route from a route definition
   * @param def the route definition
   * @return the route
   */
  public Route createRouteFromDefinition(RouteDefinition def) {
    Route result = null;
    try {
      for (RouteMapping m : mappings.values()) {
        if (def.getRuleType().equals(m.getRuleType())) {
          result = (Route)m.getRouteClass().getConstructor().newInstance();
          result.fromRouteDefinition(def);
          break;
        }
      }
    } catch (Exception e) {
      throw new AdministratorException(e);
    }
    return result;
  }
  
  /**
   * Returns the actual class for the specified route name
   * @param key the route name
   * @return the class
   */
  @SuppressWarnings("rawtypes") // We cant mock behaviour unless doing this...
  public Class getRouteClass(String key) {
    return mappings.get(key).getRouteClass();
  }
  
  /**
   * Creates a new instance of provided route
   * @param key the route name
   * @return the route
   */
  @SuppressWarnings("unchecked")
  public Route newInstance(String key) {
    if (hasRouteMapping(key)) {
      try {
        return (Route)getRouteClass(key).getConstructor().newInstance();
      } catch (Exception e) {
        throw new AdministratorException(e);
      }
    }
    return null;
  }
  
  /**
   * Returns if the route mappings exists or not
   * @param key the key
   * @return if route exists or not
   */
  public boolean hasRouteMapping(String key) {
    return mappings.containsKey(key);
  }
  
  /**
   * The currently supported route names
   * @return the key set
   */
  public List<String> getRouteTypes() {
    List<String> result = new ArrayList<String>();
    result.addAll(mappings.keySet());
    Collections.sort(result);
    return result;
  }
  
  /**
   * After properties set
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    setup(ROUTE_SET);
  }
}
