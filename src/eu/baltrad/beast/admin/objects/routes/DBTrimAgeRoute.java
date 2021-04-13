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
import eu.baltrad.beast.rules.bdb.BdbTrimAgeRule;

/**
 * Object used to manipulate {@link BdbTrimAgeRule}.
 * @author anders
 */
@JsonRootName("db-trim-age-route")
public class DBTrimAgeRoute extends Route {
  private int age = 0;

  /**
   * @return the age
   */
  public int getAge() {
    return age;
  }

  /**
   * @param age the age to set
   */
  public void setAge(int age) {
    this.age = age;
  }
  
  @Override
  public void fromRouteDefinition(RouteDefinition def) {
    BdbTrimAgeRule rule = (BdbTrimAgeRule)def.getRule();
    this.setAuthor(def.getAuthor());
    this.setDescription(def.getDescription());
    this.setName(def.getName());
    this.setRecipients(def.getRecipients());
    this.setActive(def.isActive());
    this.setAge(rule.getFileAgeLimit());
  }
  
  @Override
  public IRule toRule(IRouterManager routerManager) {
    BdbTrimAgeRule rule = (BdbTrimAgeRule)routerManager.createRule(BdbTrimAgeRule.TYPE);
    rule.setFileAgeLimit(this.getAge());
    return rule;
  }
}
