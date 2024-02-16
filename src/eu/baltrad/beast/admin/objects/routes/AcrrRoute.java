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
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.router.IRouterManager;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.acrr.AcrrRule;

/**
 * Object for manipulating {@link AcrrRule}.
 * @see AcrrRule
 * @author anders
 */
@JsonRootName("acrr-route")
public class AcrrRoute extends Route {
  public static final String ObjectType_IMAGE = "IMAGE";
  
  public static final String ObjectType_COMP = "COMP";

  /**
   * The area for which this route should be triggered
   */
  private String area = null;
  
  /**
   * The object type, can be either IMAGE or COMP
   */
  private String objectType = ObjectType_IMAGE;
  
  /**
   * The quantity that should be accumulated
   */
  private String quantity = "DBZH";
  
  /**
   * The number of hours that should be accumulated over
   */
  private int hours = 24;
  
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
   * If GRA correction should be applied or not
   */
  private boolean applyGra = false;
  
  /**
   * The ZR-A constant.
   */
  private double zrA = 200.0;

  /**
   * The ZR-b constant.
   */
  private double zrb = 1.6;
  
  /**
   * The product id
   */
  private String productId = null;
  
  /**
   * The matching filter for doing more fine-grained selections
   */
  private IFilter filter = null;

  /**
   * Constructor
   */
  public AcrrRoute() {
  }

  /**
   * Constructor
   * @param name name of route
   */
  public AcrrRoute(String name)  {
    setName(name);
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
   * @return the hour
   */
  public int getHours() {
    return hours;
  }

  /**
   * @param hour the hour to set
   */
  public void setHours(int hours) {
    this.hours = hours;
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
   * @return the applyGra
   */
  @JsonProperty(value="apply_gra")
  public boolean isApplyGra() {
    return applyGra;
  }

  /**
   * @param applyGra the applyGra to set
   */
  @JsonProperty(value="apply_gra")
  public void setApplyGra(boolean applyGra) {
    this.applyGra = applyGra;
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
   * @return the product id. Default is null
   */
  public String getProductId() {
    return productId;
  }

  /**
   * @param productId the product id
   */
  public void setProductId(String productId) {
    this.productId = productId;
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
   * @see Route#fromRouteDefinition(RouteDefinition)
   */
  @Override
  public void fromRouteDefinition(RouteDefinition def) {
    AcrrRule rule = (AcrrRule)def.getRule();

    this.setAuthor(def.getAuthor());
    this.setDescription(def.getDescription());
    this.setName(def.getName());
    this.setRecipients(def.getRecipients());
    this.setActive(def.isActive());

    this.setAcceptableLoss(rule.getAcceptableLoss());
    this.setApplyGra(rule.isApplyGRA());
    this.setArea(rule.getArea());
    this.setDistanceField(rule.getDistancefield());
    this.setFilesPerHour(rule.getFilesPerHour());
    this.setFilter(rule.getFilter());
    this.setHours(rule.getHours());
    this.setObjectType(rule.getObjectType());
    this.setQuantity(rule.getQuantity());
    this.setZrA(rule.getZrA());
    this.setZrb(rule.getZrB());
    this.setProductId(rule.getProductId());
  }
  
  /**
   * @see Route#toRule(IRouterManager)
   */
  @Override
  public IRule toRule(IRouterManager routerManager) {
    AcrrRule rule = (AcrrRule)routerManager.createRule(AcrrRule.TYPE);

    rule.setAcceptableLoss(this.getAcceptableLoss());
    rule.setApplyGRA(this.isApplyGra());
    rule.setArea(this.getArea());
    rule.setDistancefield(this.getDistanceField());
    rule.setFilesPerHour(this.getFilesPerHour());
    rule.setFilter(this.getFilter());
    rule.setHours(this.getHours());
    rule.setObjectType(this.getObjectType());
    rule.setQuantity(this.getQuantity());
    rule.setZrA(this.getZrA());
    rule.setZrB(this.getZrb());
    rule.setProductId(this.getProductId());

    return rule;
  }

  @Override
  @JsonIgnore
  public boolean isValid() {
    if (getName() != null && !getName().isEmpty() && area != null && 
        !area.isEmpty() && getRecipients() != null && getRecipients().size() > 0 &&
        getAcceptableLoss() >= 0 && getAcceptableLoss() <= 100 &&
        getFilesPerHour() > 0) {
      return true;
    }
    return false;
  }
}
