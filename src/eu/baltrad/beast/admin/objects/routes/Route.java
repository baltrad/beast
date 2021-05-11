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

import org.codehaus.jackson.annotate.JsonIgnore;

import eu.baltrad.beast.router.IRouterManager;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.IRule;

/**
 * Base object used when manipulating different routes. See {@link RouteDefinition}
 * @author anders
 */
public abstract class Route {
  /**
   * The unique name of this definition.
   */
  private String name = null;

  /**
   * The list of recipients.
   */
  private List<String> recipients = new ArrayList<String>();
  
  /**
   * If this definition is active or not.
   */
  private boolean active = true;

  /**
   * The author of this definition.
   */
  private String author = null;
  
  
  /**
   * Description of this route definition.
   */
  private String description = null;


  /**
   * Constructor
   */
  public Route() {
  }

  /**
   * Constructor
   */
  public Route(String name) {
    setName(name);
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }


  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }


  /**
   * @return the recipients
   */
  public List<String> getRecipients() {
    return recipients;
  }


  /**
   * @param recipients the recipients to set
   */
  public void setRecipients(List<String> recipients) {
    if (recipients == null) {
      this.recipients = new ArrayList<String>();
    } else {
      this.recipients = recipients;
    }
  }


  /**
   * @return the active
   */
  public boolean isActive() {
    return active;
  }


  /**
   * @param active the active to set
   */
  public void setActive(boolean active) {
    this.active = active;
  }


  /**
   * @return the author
   */
  public String getAuthor() {
    return author;
  }


  /**
   * @param author the author to set
   */
  public void setAuthor(String author) {
    this.author = author;
  }


  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }


  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }
  
  /**
   * Validates this route so that it follows the conventions.
   * @return true if valid, false otherwise
   */
  public boolean validate() {
    if (name != null && !name.isEmpty() && author != null && description != null) {
      return isValid();
    }
    return false;
  }
  
  @JsonIgnore
  public abstract boolean isValid();
  
  /**
   * Fills a route from the route definition
   * @param def the route definition
   */
  public void fromRouteDefinition(RouteDefinition def) {
    throw new RuntimeException("Not implemented");
  }
  
  /**
   * Creates a rule from self using the provided router manager
   * @param routerManager the router manager
   * @return the rule
   */
  public IRule toRule(IRouterManager routerManager) {
    throw new RuntimeException("Not implemented");
  }
}
