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
import eu.baltrad.beast.rules.gra.GraRule;

/**
 * Object used to manipulate {@link GraRule}
 * @author anders
 */
@JsonRootName("gra-route")
public class GraRoute extends Route {
  /**
   * The area for which this route should be triggered
   */
  private String area = null;
  
  /**
   * The object type, can be either IMAGE or COMP
   */
  private String objectType = "IMAGE";
  
  /**
   * The quantity that should be accumulated
   */
  private String quantity = "DBZH";

  /**
   * The number of files per hour to be used. E.g if 4, then files at 00,15,30 and 45 will be used.
   */
  private int filesPerHour = 4;
  
  /**
   * The acceptable loss in percent (0-100).
   */
  private int acceptableLoss = 0;
  
  /**
   * The quality field for distance. If not specified, eu.baltrad.composite.quality.distance.radar will be used.
   */
  private String distanceField = "eu.baltrad.composite.quality.distance.radar";

  /**
   * The ZR-A constant.
   */
  private double zrA = 200.0;

  /**
   * The ZR-b constant.
   */
  private double zrb = 1.6;
  
  /**
   * The offset in hours to the first observation term. This is the time when the term ends.
   */
  private int firstTermUtc = 6;
  
  /**
   * The number of hours of each term.
   */
  private int interval = 12;
  
  /**
   * The matching filter for doing more fine-grained selections
   */
  private IFilter filter = null;

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
   * @return the objectType
   */
  @JsonProperty(value="object_type")
  public String getObjectType() {
    return objectType;
  }

  /**
   * @param objectType the objectType to set
   */
  @JsonProperty(value="object_type")
  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  /**
   * @return the quantity
   */
  public String getQuantity() {
    return quantity;
  }

  /**
   * @param quantity the quantity to set
   */
  public void setQuantity(String quantity) {
    this.quantity = quantity;
  }

  /**
   * @return the filesPerHour
   */
  @JsonProperty(value="files_per_hour")
  public int getFilesPerHour() {
    return filesPerHour;
  }

  /**
   * @param filesPerHour the filesPerHour to set
   */
  @JsonProperty(value="files_per_hour")
  public void setFilesPerHour(int filesPerHour) {
    this.filesPerHour = filesPerHour;
  }

  /**
   * @return the acceptableLoss
   */
  @JsonProperty(value="acceptable_loss")
  public int getAcceptableLoss() {
    return acceptableLoss;
  }

  /**
   * @param acceptableLoss the acceptableLoss to set
   */
  @JsonProperty(value="acceptable_loss")
  public void setAcceptableLoss(int acceptableLoss) {
    this.acceptableLoss = acceptableLoss;
  }

  /**
   * @return the distanceField
   */
  @JsonProperty(value="distance_field")
  public String getDistanceField() {
    return distanceField;
  }

  /**
   * @param distanceField the distanceField to set
   */
  @JsonProperty(value="distance_field")
  public void setDistanceField(String distanceField) {
    this.distanceField = distanceField;
  }

  /**
   * @return the zrA
   */
  @JsonProperty(value="zr_A")
  public double getZrA() {
    return zrA;
  }

  /**
   * @param zrA the zrA to set
   */
  @JsonProperty(value="zr_A")
  public void setZrA(double zrA) {
    this.zrA = zrA;
  }

  /**
   * @return the zrb
   */
  @JsonProperty(value="zr_b")
  public double getZrb() {
    return zrb;
  }

  /**
   * @param zrb the zrb to set
   */
  @JsonProperty(value="zr_b")
  public void setZrb(double zrb) {
    this.zrb = zrb;
  }

  /**
   * @return the firstTermUtc
   */
  @JsonProperty(value="first_term_utc")
  public int getFirstTermUtc() {
    return firstTermUtc;
  }

  /**
   * @param firstTermUtc the firstTermUtc to set
   */
  @JsonProperty(value="first_term_utc")
  public void setFirstTermUtc(int firstTermUtc) {
    this.firstTermUtc = firstTermUtc;
  }

  /**
   * @return the interval
   */
  public int getInterval() {
    return interval;
  }

  /**
   * @param interval the interval to set
   */
  public void setInterval(int interval) {
    this.interval = interval;
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
    GraRule rule = (GraRule)def.getRule();
    this.setAuthor(def.getAuthor());
    this.setDescription(def.getDescription());
    this.setName(def.getName());
    this.setRecipients(def.getRecipients());
    this.setActive(def.isActive());

    this.setAcceptableLoss(rule.getAcceptableLoss());
    this.setArea(rule.getArea());
    this.setDistanceField(rule.getDistancefield());
    this.setFilesPerHour(rule.getFilesPerHour());
    this.setFilter(rule.getFilter());
    this.setFirstTermUtc(rule.getFirstTermUTC());
    this.setInterval(rule.getInterval());
    this.setObjectType(rule.getObjectType());
    this.setQuantity(rule.getQuantity());
    this.setZrA(rule.getZrA());
    this.setZrb(rule.getZrB());
  }
  
  @Override
  public IRule toRule(IRouterManager routerManager) {
    GraRule rule = (GraRule)routerManager.createRule(GraRule.TYPE);

    rule.setAcceptableLoss(this.getAcceptableLoss());
    rule.setArea(this.getArea());
    rule.setDistancefield(this.getDistanceField());
    rule.setFilesPerHour(this.getFilesPerHour());
    rule.setFilter(this.getFilter());
    rule.setFirstTermUTC(this.getFirstTermUtc());
    rule.setInterval(this.getInterval());
    rule.setObjectType(this.getObjectType());
    rule.setQuantity(this.getQuantity());
    rule.setZrA(this.getZrA());
    rule.setZrB(this.getZrb());

    return rule;
  }
}
