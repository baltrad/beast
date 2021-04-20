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
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.router.IRouterManager;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.wrwp.WrwpRule;

/**
 * Object used when manipulating {@link WrwpRule}
 * @author anders
 */
@JsonRootName("wrwp-route")
public class WrwpRoute extends Route {
  /**
   * Specify height interval
   */
  private int heightInterval = 200;

  /**
   * Specify maximum profile height
   */
  private int maxProfileHeight = 12000;
  
  /**
   * Minimum distance for deriving a profile
   */
  private int minDistance = 4000;
  
  /**
   * Maximum distance for deriving a profile
   */
  private int maxDistance = 40000;
  
  /**
   * Minimum elevation angle [deg]
   */
  private double minElevationAngle = 2.5;
  
  /**
   * Maximum elevation angle [deg]
   */
  private double maxElevationAngle = 45.0;
  
  /**
   * Radial velocity threshold [m/s]
   */
  private double radialVelocityThreshold = 2.0;
  
  /**
   * Upper threshold for calculated velocity [m/s]
   */
  private double upperThresholdForCalculatedVelocity = 60.0;
  
  /**
   * Min sample size for reflectivity
   */
  private int minSampleSizeForReflectivity = 40;

  /**
   * Min sample size for wind
   */
  private int minSampleSizeForWind = 40;
  
  /**
   * Fields to be processed
   */
  private List<String> fields = new ArrayList<String>();
  
  /**
   * Sources to be processed
   */
  private List<String> sources = new ArrayList<String>();
  
  /**
   * The filter
   */
  private IFilter filter = null;

  /**
   * @return the heightInterval
   */
  @JsonProperty(value="height_interval")
  public int getHeightInterval() {
    return heightInterval;
  }

  /**
   * @param heightInterval the heightInterval to set
   */
  @JsonProperty(value="height_interval")
  public void setHeightInterval(int heightInterval) {
    this.heightInterval = heightInterval;
  }

  /**
   * @return the maxProfileHeight
   */
  @JsonProperty(value="max_profile_height")
  public int getMaxProfileHeight() {
    return maxProfileHeight;
  }

  /**
   * @param maxProfileHeight the maxProfileHeight to set
   */
  @JsonProperty(value="max_profile_height")
  public void setMaxProfileHeight(int maxProfileHeight) {
    this.maxProfileHeight = maxProfileHeight;
  }

  /**
   * @return the minDistance
   */
  @JsonProperty(value="min_distance")
  public int getMinDistance() {
    return minDistance;
  }

  /**
   * @param minDistance the minDistance to set
   */
  @JsonProperty(value="min_distance")
  public void setMinDistance(int minDistance) {
    this.minDistance = minDistance;
  }

  /**
   * @return the maxDistance
   */
  @JsonProperty(value="max_distance")
  public int getMaxDistance() {
    return maxDistance;
  }

  /**
   * @param maxDistance the maxDistance to set
   */
  @JsonProperty(value="max_distance")
  public void setMaxDistance(int maxDistance) {
    this.maxDistance = maxDistance;
  }

  /**
   * @return the minElevationAngle
   */
  @JsonProperty(value="min_elevation_angle")
  public double getMinElevationAngle() {
    return minElevationAngle;
  }

  /**
   * @param minElevationAngle the minElevationAngle to set
   */
  @JsonProperty(value="min_elevation_angle")
  public void setMinElevationAngle(double minElevationAngle) {
    this.minElevationAngle = minElevationAngle;
  }

  /**
   * @return the maxElevationAngle
   */
  @JsonProperty(value="max_elevation_angle")
  public double getMaxElevationAngle() {
    return maxElevationAngle;
  }

  /**
   * @param maxElevationAngle the maxElevationAngle to set
   */
  @JsonProperty(value="max_elevation_angle")
  public void setMaxElevationAngle(double maxElevationAngle) {
    this.maxElevationAngle = maxElevationAngle;
  }

  /**
   * @return the radialVelocityThreshold
   */
  @JsonProperty(value="radial_velocity_threshold")
  public double getRadialVelocityThreshold() {
    return radialVelocityThreshold;
  }

  /**
   * @param radialVelocityThreshold the radialVelocityThreshold to set
   */
  @JsonProperty(value="radial_velocity_threshold")
  public void setRadialVelocityThreshold(double radialVelocityThreshold) {
    this.radialVelocityThreshold = radialVelocityThreshold;
  }

  /**
   * @return the upperThresholdForCalculatedVelocity
   */
  @JsonProperty(value="upper_threshold_for_calculated_velocity")
  public double getUpperThresholdForCalculatedVelocity() {
    return upperThresholdForCalculatedVelocity;
  }

  /**
   * @param upperThresholdForCalculatedVelocity the upperThresholdForCalculatedVelocity to set
   */
  @JsonProperty(value="upper_threshold_for_calculated_velocity")
  public void setUpperThresholdForCalculatedVelocity(double upperThresholdForCalculatedVelocity) {
    this.upperThresholdForCalculatedVelocity = upperThresholdForCalculatedVelocity;
  }

  /**
   * @return the minSampleSizeForReflectivity
   */
  @JsonProperty(value="min_sample_size_for_reflectivity")
  public int getMinSampleSizeForReflectivity() {
    return minSampleSizeForReflectivity;
  }

  /**
   * @param minSampleSizeForReflectivity the minSampleSizeForReflectivity to set
   */
  @JsonProperty(value="min_sample_size_for_reflectivity")
  public void setMinSampleSizeForReflectivity(int minSampleSizeForReflectivity) {
    this.minSampleSizeForReflectivity = minSampleSizeForReflectivity;
  }

  /**
   * @return the minSampleSizeForWind
   */
  @JsonProperty(value="min_sample_size_for_wind")
  public int getMinSampleSizeForWind() {
    return minSampleSizeForWind;
  }

  /**
   * @param minSampleSizeForWind the minSampleSizeForWind to set
   */
  @JsonProperty(value="min_sample_size_for_wind")
  public void setMinSampleSizeForWind(int minSampleSizeForWind) {
    this.minSampleSizeForWind = minSampleSizeForWind;
  }

  /**
   * @return the fields
   */
  public List<String> getFields() {
    return fields;
  }

  /**
   * @param fields the fields to set
   */
  public void setFields(List<String> fields) {
    this.fields = fields;
  }

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
    WrwpRule rule = (WrwpRule)def.getRule();
    this.setAuthor(def.getAuthor());
    this.setDescription(def.getDescription());
    this.setName(def.getName());
    this.setRecipients(def.getRecipients());
    this.setActive(def.isActive());

    this.setFields(rule.getFields());
    this.setFilter(rule.getFilter());
    this.setHeightInterval(rule.getInterval());
    this.setMaxDistance(rule.getMaxdistance());
    this.setMaxElevationAngle(rule.getMaxelevationangle());
    this.setMaxProfileHeight(rule.getMaxheight());
    this.setMinDistance(rule.getMindistance());
    this.setMinElevationAngle(rule.getMinelevationangle());
    this.setMinSampleSizeForReflectivity(rule.getMinsamplesizereflectivity());
    this.setMinSampleSizeForWind(rule.getMinsamplesizewind());
    this.setRadialVelocityThreshold(rule.getMinvelocitythreshold());
    this.setSources(rule.getSources());
    this.setUpperThresholdForCalculatedVelocity(rule.getMaxvelocitythreshold());
  }
  
  @Override
  public IRule toRule(IRouterManager routerManager) {
    WrwpRule rule = (WrwpRule)routerManager.createRule(WrwpRule.TYPE);
    
    rule.setFields(this.getFields());
    rule.setFilter(this.getFilter());
    rule.setInterval(this.getHeightInterval());
    rule.setMaxdistance(this.getMaxDistance());
    rule.setMaxelevationangle(this.getMaxElevationAngle());
    rule.setMaxheight(this.getMaxProfileHeight());
    rule.setMindistance(this.getMinDistance());
    rule.setMinelevationangle(this.getMinElevationAngle());
    rule.setMinsamplesizereflectivity(this.getMinSampleSizeForReflectivity());
    rule.setMinsamplesizewind(this.getMinSampleSizeForWind());
    rule.setMinvelocitythreshold(this.getRadialVelocityThreshold());
    rule.setSources(this.getSources());
    rule.setMaxvelocitythreshold(this.getUpperThresholdForCalculatedVelocity());
    
    return rule;
  }

  @Override
  @JsonIgnore
  public boolean isValid() {
    if (getName() != null && !getName().isEmpty() &&
        getSources().size() > 0 &&
        getRecipients().size() > 0 &&
        getFields().size() > 0) {
      return true;
    }
    return false;
  }
}
