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
import eu.baltrad.beast.rules.volume.VolumeRule;

/**
 * Object when manipulating {@link VolumeRule}
 * @author anders
 */
@JsonRootName("volume-route")
public class VolumeRoute  extends Route {
  /**
   * Ascending or not
   */
  private boolean ascending = true;
  
  /**
   * Min elevation
   */
  private double minElevation = 0.0;
  
  /**
   * Max elevation
   */
  private double maxElevation = 45.0;

  /**
   * Detectors that should be run for this composite rule
   */
  private List<String> detectors = new ArrayList<String>();

  /**
   * The elevation angles
   */
  private List<Double> elevationAngles = new ArrayList<Double>();
  
  /**
   * If adaptive elevation angle handling should be used or not
   */
  private boolean adaptiveElevationAngles = false;
  
  /**
   * How the quality controls should be handled and used
   */
  private String qualityControlMode = "ANALYZE_AND_APPLY";

  /**
   * The filter used for matching files
   */
  private IFilter filter = null;

  /**
   * Interval
   */
  private int interval = 15;
  
  /**
   * The timeout, if 0, no timeout
   */
  private int timeout = 0;
  
  /**
   * If the timeout should be calculated from the nominal time or from the arrival of the first file
   * belonging to this product.
   */
  private boolean nominalTimeout = false;
  
  /**
   * A list of sources (e.g. seang, sekkr, ...)
   */
  private List<String> sources = new ArrayList<String>();

  /**
   * @return the ascending
   */
  public boolean isAscending() {
    return ascending;
  }

  /**
   * @param ascending the ascending to set
   */
  public void setAscending(boolean ascending) {
    this.ascending = ascending;
  }

  /**
   * @return the minElevation
   */
  @JsonProperty(value="min_elevation")
  public double getMinElevation() {
    return minElevation;
  }

  /**
   * @param minElevation the minElevation to set
   */
  @JsonProperty(value="min_elevation")
  public void setMinElevation(double minElevation) {
    this.minElevation = minElevation;
  }

  /**
   * @return the maxElevation
   */
  @JsonProperty(value="max_elevation")
  public double getMaxElevation() {
    return maxElevation;
  }

  /**
   * @param maxElevation the maxElevation to set
   */
  @JsonProperty(value="max_elevation")
  public void setMaxElevation(double maxElevation) {
    this.maxElevation = maxElevation;
  }

  /**
   * @return the detectors
   */
  public List<String> getDetectors() {
    return detectors;
  }

  /**
   * @param detectors the detectors to set
   */
  public void setDetectors(List<String> detectors) {
    this.detectors = detectors;
  }

  /**
   * @return the elevationAngles
   */
  @JsonProperty(value="elevation_angles")
  public List<Double> getElevationAngles() {
    return elevationAngles;
  }

  /**
   * @param elevationAngles the elevationAngles to set
   */
  @JsonProperty(value="elevation_angles")
  public void setElevationAngles(List<Double> elevationAngles) {
    this.elevationAngles = elevationAngles;
  }


  /**
   * @return the adaptiveElevationAngles
   */
  @JsonProperty(value="adaptive_elevation_angles")
  public boolean isAdaptiveElevationAngles() {
    return adaptiveElevationAngles;
  }

  /**
   * @param adaptiveElevationAngles the adaptiveElevationAngles to set
   */
  @JsonProperty(value="adaptive_elevation_angles")
  public void setAdaptiveElevationAngles(boolean adaptiveElevationAngles) {
    this.adaptiveElevationAngles = adaptiveElevationAngles;
  }
  
  /**
   * @return the qualityControlMode
   */
  @JsonProperty(value="quality_control_mode")
  public String getQualityControlMode() {
    return qualityControlMode;
  }

  /**
   * @param qualityControlMode the qualityControlMode to set
   */
  @JsonProperty(value="quality_control_mode")
  public void setQualityControlMode(String qualityControlMode) {
    this.qualityControlMode = qualityControlMode;
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
   * @return the timeout
   */
  public int getTimeout() {
    return timeout;
  }

  /**
   * @param timeout the timeout to set
   */
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  /**
   * @return the nominalTimeout
   */
  @JsonProperty(value="nominal_timeout")
  public boolean isNominalTimeout() {
    return nominalTimeout;
  }

  /**
   * @param nominalTimeout the nominalTimeout to set
   */
  @JsonProperty(value="nominal_timeout")
  public void setNominalTimeout(boolean nominalTimeout) {
    this.nominalTimeout = nominalTimeout;
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
  
  @Override
  public void fromRouteDefinition(RouteDefinition def) {
    VolumeRule rule = (VolumeRule)def.getRule();
    this.setAuthor(def.getAuthor());
    this.setDescription(def.getDescription());
    this.setName(def.getName());
    this.setRecipients(def.getRecipients());
    this.setActive(def.isActive());

    this.setAscending(rule.isAscending());
    this.setDetectors(rule.getDetectors());
    this.setElevationAngles(rule.getElevationAnglesAsDoubles());
    this.setAdaptiveElevationAngles(rule.isAdaptiveElevationAngles());
    this.setFilter(rule.getFilter());
    this.setInterval(rule.getInterval());
    this.setMaxElevation(rule.getElevationMax());
    this.setMinElevation(rule.getElevationMin());
    this.setNominalTimeout(rule.isNominalTimeout());
    this.setQualityControlMode(rule.getQualityControlModeAsString());
    this.setSources(rule.getSources());
    this.setTimeout(rule.getTimeout());
  }
  
  @Override
  public IRule toRule(IRouterManager routerManager) {
    VolumeRule rule = (VolumeRule)routerManager.createRule(VolumeRule.TYPE);
    rule.setAscending(this.isAscending());
    rule.setDetectors(this.getDetectors());
    rule.setElevationAngles(this.getElevationAngles());
    rule.setAdaptiveElevationAngles(this.isAdaptiveElevationAngles());
    rule.setFilter(this.getFilter());
    rule.setInterval(this.getInterval());
    rule.setElevationMax(this.getMaxElevation());
    rule.setElevationMin(this.getMinElevation());
    rule.setNominalTimeout(this.isNominalTimeout());
    rule.setQualityControlMode(this.getQualityControlMode());
    rule.setSources(this.getSources());
    rule.setTimeout(this.getTimeout());   
    return rule;
  }

  @Override
  @JsonIgnore
  public boolean isValid() {
    if (getName() != null && !getName().isEmpty() &&
        getRecipients().size() > 0 &&
        getSources().size() > 0) {
      return true;
    }
    return false;
  }
}
