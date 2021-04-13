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

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.router.IRouterManager;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.composite.CompositingRule;
import eu.baltrad.beast.rules.site2d.Site2DRule;

/**
 * Object used when manipulating {@link Site2DRule}
 * @author anders
 */
@JsonRootName("site2d-route")
public class Site2DRoute extends Route {
  /**
   * If composite should be generated from scans or volumes.
   */
  private boolean scan_based = false;

  /**
   * The algorithm to use
   */
  private String method = CompositingRule.PCAPPI;

  /**
   * The product parameter that should be used in conjunction with the algorithm.
   * E.g. for PCAPPI, specify height in meters. For PPI, specify elevation angle
   * in degrees. Etc. See ODIM specification.
   */
  private String prodpar = "1000.0";

  /**
   * The are that this composite should cover
   */
  private String area = null;

  /**
   * The pcs id to use
   */
  private String pcs_id = null;
  
  /**
   * The xscale to use if specifying pcs_id
   */
  private double xscale = 2000.0;
  
  /**
   * The yscale to use if specifying pcs id
   */
  private double yscale = 2000.0;
  
  /**
   * Interval
   */
  private int interval = 15;

  /**
   * Indicates if GRA coefficients should be applied or not
   */
  private boolean applyGRA = false;
  
  /**
   * The ZR A coefficient when translating from reflectivity to MM/H
   */
  private double ZR_A = 200.0;
  
  /**
   * The ZR b coefficient when translating from reflectivity to MM/H
   */
  private double ZR_b = 1.6;

  /**
   * Indicates if malfunc should be ignored or not when generating the composites
   */
  private boolean ignoreMalfunc = false;

  /**
   * Indicates if cloudtype filter should be applied or not
   */
  private boolean ctFilter = false;
  
  /**
   * A list of sources (e.g. seang, sekkr, ...)
   */
  private List<String> sources = new ArrayList<String>();
  
  /**
   * Detectors that should be run for this composite rule
   */
  private List<String> detectors = new ArrayList<String>();
  
  /**
   * How the quality controls should be handled and used
   */
  private String qualityControlMode = "ANALYZE_AND_APPLY";

  /**
   * The quantity to use
   */
  private String quantity = "DBZH";
  
  /**
   * The filter used for matching files
   */
  private IFilter filter = null;
  
  /**
   * Constructor
   */
  public Site2DRoute() {
  }
  
  /**
   * Constructor
   * @param name name of the site2d route
   */
  public Site2DRoute(String name) {
    setName(name);
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
   * @return the scanBased
   */
  @JsonProperty(value="scan_based")
  public boolean isScanBased() {
    return scan_based;
  }

  /**
   * @param scanBased the scanBased to set
   */
  @JsonProperty(value="scan_based")
  public void setScanBased(boolean scanBased) {
    this.scan_based = scanBased;
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
   * @return the method
   */
  public String getMethod() {
    return method;
  }

  /**
   * @param method the method to set
   */
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * @return the prodpar
   */
  public String getProdpar() {
    return prodpar;
  }

  /**
   * @param prodpar the prodpar to set
   */
  public void setProdpar(String prodpar) {
    this.prodpar = prodpar;
  }

  /**
   * @return the applyGRA
   */
  @JsonProperty(value="apply_gra")
  public boolean isApplyGRA() {
    return applyGRA;
  }

  /**
   * @param applyGRA the applyGRA to set
   */
  @JsonProperty(value="apply_gra")
  public void setApplyGRA(boolean applyGRA) {
    this.applyGRA = applyGRA;
  }

  /**
   * @return the ignoreMalfunc
   */
  @JsonProperty(value="ignore_malfunc")
  public boolean isIgnoreMalfunc() {
    return ignoreMalfunc;
  }

  /**
   * @param ignoreMalfunc the ignoreMalfunc to set
   */
  @JsonProperty(value="ignore_malfunc")
  public void setIgnoreMalfunc(boolean ignoreMalfunc) {
    this.ignoreMalfunc = ignoreMalfunc;
  }

  /**
   * @return the ctFilter
   */
  @JsonProperty(value="ct_filter")
  public boolean isCtFilter() {
    return ctFilter;
  }

  /**
   * @param ctFilter the ctFilter to set
   */
  @JsonProperty(value="ct_filter")
  public void setCtFilter(boolean ctFilter) {
    this.ctFilter = ctFilter;
  }

  /**
   * @return the zR_A
   */
  @JsonProperty(value="zr_A")
  public double getZR_A() {
    return ZR_A;
  }

  /**
   * @param zR_A the zR_A to set
   */
  @JsonProperty(value="zr_A")
  public void setZR_A(double zR_A) {
    ZR_A = zR_A;
  }

  /**
   * @return the zR_b
   */
  @JsonProperty(value="zr_b")
  public double getZR_b() {
    return ZR_b;
  }

  /**
   * @param zR_b the zR_b to set
   */
  @JsonProperty(value="zr_b")
  public void setZR_b(double zR_b) {
    ZR_b = zR_b;
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
   * @return the pcs_id
   */
  @JsonProperty(value="pcs_id")
  public String getPcsId() {
    return pcs_id;
  }

  /**
   * @param pcs_id the pcs_id to set
   */
  @JsonProperty(value="pcs_id")
  public void setPcsId(String pcs_id) {
    this.pcs_id = pcs_id;
  }

  /**
   * @return the xscale
   */
  public double getXscale() {
    return xscale;
  }

  /**
   * @param xscale the xscale to set
   */
  public void setXscale(double xscale) {
    this.xscale = xscale;
  }

  /**
   * @return the yscale
   */
  public double getYscale() {
    return yscale;
  }

  /**
   * @param yscale the yscale to set
   */
  public void setYscale(double yscale) {
    this.yscale = yscale;
  }
  
  @Override
  public void fromRouteDefinition(RouteDefinition def) {
    Site2DRule rule = (Site2DRule)def.getRule();
    this.setAuthor(def.getAuthor());
    this.setDescription(def.getDescription());
    this.setName(def.getName());
    this.setRecipients(def.getRecipients());
    this.setActive(def.isActive());

    this.setApplyGRA(rule.isApplyGRA());
    this.setArea(rule.getArea());
    this.setCtFilter(rule.isCtFilter());
    this.setDetectors(rule.getDetectors());
    this.setFilter(rule.getFilter());
    this.setIgnoreMalfunc(rule.isIgnoreMalfunc());
    this.setInterval(rule.getInterval());
    this.setMethod(rule.getMethod());
    this.setPcsId(rule.getPcsid());
    this.setProdpar(rule.getProdpar());
    this.setQualityControlMode(rule.getQualityControlModeAsString());
    this.setScanBased(rule.isScanBased());
    this.setSources(rule.getSources());
    this.setXscale(rule.getXscale());
    this.setYscale(rule.getYscale());
    this.setZR_A(rule.getZR_A());
    this.setZR_b(rule.getZR_b());
  }
  
  /**
   * Creates a rule from self using the provided router manager
   * @routerManager - the router manager
   */
  @Override
  public IRule toRule(IRouterManager routerManager) {
    Site2DRule rule = (Site2DRule)routerManager.createRule(Site2DRule.TYPE);
    
    rule.setApplyGRA(this.isApplyGRA());
    rule.setArea(this.getArea());
    rule.setCtFilter(this.isCtFilter());
    rule.setDetectors(this.getDetectors());
    rule.setFilter(this.getFilter());
    rule.setIgnoreMalfunc(this.isIgnoreMalfunc());
    rule.setInterval(this.getInterval());
    rule.setMethod(this.getMethod());
    rule.setPcsid(this.getPcsId());
    rule.setProdpar(this.getProdpar());
    rule.setQualityControlMode(this.getQualityControlMode());
    rule.setScanBased(this.isScanBased());
    rule.setSources(this.getSources());
    rule.setXscale(this.getXscale());
    rule.setYscale(this.getYscale());
    rule.setZR_A(this.getZR_A());
    rule.setZR_b(this.getZR_b());
    
    return rule;
  }

}
