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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonRootName;

import eu.baltrad.beast.router.IRouterManager;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.scansun.ScansunRule;

/**
 * Object used when manipulating {@link ScansunRule}
 * @author anders
 */
@JsonRootName("scansun-route")
public class ScansunRoute extends Route {
  /**
   * Sources
   */
  private List<String> sources = new ArrayList<String>();

  /**
   * @return the sources
   */
  public List<String> getSources() {
    return sources;
  }

  /**
   * @param sources the sources to set
   */
  public void setSources(List<String> sources) {
    this.sources = sources;
  }
  
  @Override
  public void fromRouteDefinition(RouteDefinition def) {
    ScansunRule rule = (ScansunRule)def.getRule();
    this.setAuthor(def.getAuthor());
    this.setDescription(def.getDescription());
    this.setName(def.getName());
    this.setRecipients(def.getRecipients());
    this.setActive(def.isActive());
    
    this.setSources(rule.getSources());
  }
  
  @Override
  public IRule toRule(IRouterManager routerManager) {
    ScansunRule rule = (ScansunRule)routerManager.createRule(ScansunRule.TYPE);
    
    rule.setSources(this.getSources());
    
    return rule;
  }
}
