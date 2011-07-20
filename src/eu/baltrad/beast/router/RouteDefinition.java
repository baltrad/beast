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
package eu.baltrad.beast.router;

import java.util.ArrayList;
import java.util.List;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.rules.IRule;

/**
 * Defines a routing definition, i.e. if a rule evaulates to true, what
 * route(s) that should be affected.
 * @author Anders Henja
 */
public class RouteDefinition {
  /**
   * The rule that defines this route
   */
  private IRule rule = null;
  
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
   * The unique name of this definition.
   */
  private String name = null;
  
  /**
   * Description of this route definition.
   */
  private String description = null;
  
  public IBltMessage handle(IBltMessage msg) {
    if (msg == null) {
      throw new NullPointerException("msg == null");
    }
    if (active) {
      return rule.handle(msg);
    }
    return null;
  }
  
  /**
   * @param rule the rule to set
   */
  public void setRule(IRule rule) {
    this.rule = rule;
  }

  /**
   * @return the rule
   */
  public IRule getRule() {
    return rule;
  }

  /**
   * @param recipients the recipients to set
   */
  public void setRecipients(List<String> recipients) {
    if (recipients == null)
      recipients = new ArrayList<String>();
    this.recipients = recipients;
  }

  /**
   * @return the recipients
   */
  public List<String> getRecipients() {
    return recipients;
  }

  /**
   * @param active the active to set
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * @return the active
   */
  public boolean isActive() {
    return active;
  }

  /**
   * @param author the author to set
   */
  public void setAuthor(String author) {
    this.author = author;
  }

  /**
   * @return the author
   */
  public String getAuthor() {
    return author;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }
  
  /**
   * Returns the type of the rule.
   * @return the rule type (or null if no rule has been set)
   */
  public String getRuleType() {
    if (rule != null) {
      return rule.getType();
    }
    return null;
  }
}
