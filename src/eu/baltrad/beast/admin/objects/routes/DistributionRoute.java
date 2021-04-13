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

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.router.IRouterManager;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.dist.DistributionRule;

/**
 * Object used to manipulate {@link DistributionRule}
 * @author anders
 * 
 * ftp
 * scp
 * scponly
 * copy
 * sftp
 *
 */
@JsonRootName("distribution-route")
public class DistributionRoute extends Route {
  /**
   * Destination of call
   * Currently supported protocols are: ftp, scp, scponly, copy and sftp
   */
  private String destination = null;
  
  /**
   * The naming template, see user guide for format
   */
  private String nameTemplate = null;
  
  /**
   * The filter deciding what should be distributed
   */
  private IFilter filter = null;

  /**
   * @return the destination
   */
  public String getDestination() {
    return destination;
  }

  /**
   * @param destination the destination to set
   */
  public void setDestination(String destination) {
    this.destination = destination;
  }

  /**
   * @return the nameTemplate
   */
  @JsonProperty(value="name_template")
  public String getNameTemplate() {
    return nameTemplate;
  }

  /**
   * @param nameTemplate the nameTemplate to set
   */
  @JsonProperty(value="name_template")
  public void setNameTemplate(String nameTemplate) {
    this.nameTemplate = nameTemplate;
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
  
  @Override
  public void fromRouteDefinition(RouteDefinition def) {
    DistributionRule rule = (DistributionRule)def.getRule();
    this.setAuthor(def.getAuthor());
    this.setDescription(def.getDescription());
    this.setName(def.getName());
    //this.setRecipients(def.getRecipients());
    this.setActive(def.isActive());
    
    this.setDestination(rule.getDestination().toASCIIString());
    this.setFilter(rule.getFilter());
    this.setNameTemplate(rule.getMetadataNamingTemplate());
  }
  
  @Override
  public IRule toRule(IRouterManager routerManager) {
    DistributionRule rule = (DistributionRule)routerManager.createRule(DistributionRule.TYPE);
    rule.setDestination(this.getDestination());
    rule.setMetadataNamingTemplate(this.getNameTemplate());
    return rule;
  }
}
