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
import eu.baltrad.beast.rules.composite.CompositingRule;

/**
 * Object for manipulating {@link CompositingRule}.
 * @author anders
 */
@JsonRootName("composite-route")
public class CompositeRoute extends Route {
  
  /**
   * To set that method should be PPI compositing
   */
  public final static String PPI = CompositingRule.PPI;

  /**
   * To set that method should be CAPPPI compositing
   */
  public final static String CAPPI = CompositingRule.CAPPI;
  
  /**
   * To set that method should be PCAPPI compositing
   */
  public final static String PCAPPI = CompositingRule.PCAPPI;
  
  /**
   * Use Pseudo-Max compositing.
   */
  public final static String PMAX = CompositingRule.PMAX;
  
  /**
   * Use Max compositing
   */
  public final static String MAX = CompositingRule.MAX;
  
  /**
   * Nearest radar selection
   */
  public final static String Selection_NEAREST_RADAR = "NEAREST_RADAR";

  /**
   * Nearest radar selection
   */
  public final static String Selection_HEIGHT_ABOVE_SEALEVEL = "HEIGHT_ABOVE_SEALEVEL";

  /**
   * Performs the analyze and also applies the result
   */
  public final static String QualityControlMode_ANALYZE_AND_APPLY = "ANALYZE_AND_APPLY";
  
  /** Only performs the quality analysis */
  public final static String QualityControlMode_ANALYZE = "ANALYZE";  
  
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
   * The are that this composite should cover
   */
  private String area = null;
  
  /**
   * If composite should be generated from scans or volumes.
   */
  private boolean scan_based = false;
  
  /**
   * The selection method to use when determining the pixel
   */
  private String selectionMethod = Selection_NEAREST_RADAR; 
  
  /**
   * Detectors that should be run for this composite rule
   */
  private List<String> detectors = new ArrayList<String>();
  
  /**
   * The algorithm to use
   */
  private String method = PCAPPI;
  
  /**
   * The product parameter that should be used in conjunction with the algorithm.
   * E.g. for PCAPPI, specify height in meters. For PPI, specify elevation angle
   * in degrees. Etc. See ODIM specification.
   */
  private String prodpar = "1000.0";

  /**
   * Indicates if GRA coefficients should be applied or not
   */
  private boolean applyGRA = false;
  
  /**
   * Indicates if malfunc should be ignored or not when generating the composites
   */
  private boolean ignoreMalfunc = false;
  
  /**
   * Indicates if cloudtype filter should be applied or not
   */
  private boolean ctFilter = false;
  
  /**
   * The ZR A coefficient when translating from reflectivity to MM/H
   */
  private double ZR_A = 200.0;
  
  /**
   * The ZR b coefficient when translating from reflectivity to MM/H
   */
  private double ZR_b = 1.6;
  
  /**
   * The qi total field to use if QI-compositing should be performed.
   */
  private String qitotalField = null;
  
  /**
   * The quantity to use
   */
  private String quantity = "DBZH";
  
  /**
   * The options
   */
  private String options = null;
  
  /**
   * Incoming data exceeding this age threshold will not be handled 
   * by this rule. In minutes. -1 indicates that no max age limit 
   * will be applied.
   */
  private int maxAgeLimit = -1;
  
  /**
   * How the quality controls should be handled and used
   */
  private String qualityControlMode = QualityControlMode_ANALYZE_AND_APPLY;
  
  /**
   * Indicates quality controls shall always be reprocessed
   */
  private boolean reprocessQuality = false;
  
  /**
   * The filter used for matching files
   */
  private IFilter filter = null;

  /**
   * Constructor
   */
  public CompositeRoute() {
  }

  /**
   * Constructor 
   * @param name name of route
   */
  public CompositeRoute(String name) {
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
    if (sources == null) {
      this.sources = new ArrayList<String>();
    } else {
      this.sources = sources;
    }
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
   * @return the selectionMethod
   */
  @JsonProperty(value="selection_method")
  public String getSelectionMethod() {
    return selectionMethod;
  }

  /**
   * @param selectionMethod the selectionMethod to set
   */
  @JsonProperty(value="selection_method")
  public void setSelectionMethod(String selectionMethod) {
    this.selectionMethod = selectionMethod;
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
   * @return the qitotalField
   */
  @JsonProperty(value="qi_total_field")
  public String getQitotalField() {
    return qitotalField;
  }

  /**
   * @param qitotalField the qitotalField to set
   */
  @JsonProperty(value="qi_total_field")
  public void setQitotalField(String qitotalField) {
    this.qitotalField = qitotalField;
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
   * @return the maxAgeLimit
   */
  @JsonProperty(value="max_age_limit")
  public int getMaxAgeLimit() {
    return maxAgeLimit;
  }

  /**
   * @param maxAgeLimit the maxAgeLimit to set
   */
  @JsonProperty(value="max_age_limit")
  public void setMaxAgeLimit(int maxAgeLimit) {
    this.maxAgeLimit = maxAgeLimit;
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
   * @return the reprocessQuality
   */
  @JsonProperty(value="reprocess_quality")
  public boolean isReprocessQuality() {
    return reprocessQuality;
  }

  /**
   * @param reprocessQuality the reprocessQuality to set
   */
  @JsonProperty(value="reprocess_quality")
  public void setReprocessQuality(boolean reprocessQuality) {
    this.reprocessQuality = reprocessQuality;
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
    CompositingRule rule = (CompositingRule)def.getRule();
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
    this.setMaxAgeLimit(rule.getMaxAgeLimit());
    this.setMethod(rule.getMethod());
    this.setNominalTimeout(rule.isNominalTimeout());
    this.setProdpar(rule.getProdpar());
    this.setQitotalField(rule.getQitotalField());
    this.setQualityControlMode(rule.getQualityControlModeAsString());
    this.setQuantity(rule.getQuantity());
    this.setOptions(rule.getOptions());
    this.setReprocessQuality(rule.isReprocessQuality());
    this.setScanBased(rule.isScanBased());
    this.setSelectionMethod(rule.getSelectionMethodAsString());
    this.setSources(rule.getSources());
    this.setTimeout(rule.getTimeout());
    this.setZR_A(rule.getZR_A());
    this.setZR_b(rule.getZR_b());
  }
  
  @Override
  public IRule toRule(IRouterManager routerManager) {
    CompositingRule rule = (CompositingRule)routerManager.createRule(CompositingRule.TYPE);

    rule.setApplyGRA(this.isApplyGRA());
    rule.setArea(this.getArea());
    rule.setCtFilter(this.isCtFilter());
    rule.setDetectors(this.getDetectors());
    rule.setFilter(this.getFilter());
    rule.setIgnoreMalfunc(this.isIgnoreMalfunc());
    rule.setInterval(this.getInterval());
    rule.setMaxAgeLimit(this.getMaxAgeLimit());
    rule.setMethod(this.getMethod());
    rule.setNominalTimeout(this.isNominalTimeout());
    rule.setProdpar(this.getProdpar());
    rule.setQitotalField(this.getQitotalField());
    rule.setQualityControlMode(this.getQualityControlMode());
    rule.setQuantity(this.getQuantity());
    rule.setOptions(this.getOptions());
    rule.setReprocessQuality(this.isReprocessQuality());
    rule.setScanBased(this.isScanBased());
    rule.setSelectionMethod(this.getSelectionMethod());
    rule.setSources(this.getSources());
    rule.setTimeout(this.getTimeout());
    rule.setZR_A(this.getZR_A());
    rule.setZR_b(this.getZR_b());
    
    return rule;
  }

  @Override
  @JsonIgnore
  public boolean isValid() {
    boolean result = false; 
    if (this.getName() != null && !this.getName().isEmpty() &&
        this.getArea() != null && !this.getArea().isEmpty() &&
        this.getSources().size() > 0 &&
        this.getRecipients().size() > 0) {
      result = true;
    }
    
    if (result && this.getMethod() != null && this.getMethod().equalsIgnoreCase(CompositeRoute.PMAX)) {
      result = false;
      if (this.getProdpar() != null) {
        String[] values = this.getProdpar().split(",");
        if (values.length >= 1) {
          try {
            Double.parseDouble(values[0].trim());
            if (values.length > 1) {
              Double.parseDouble(values[1].trim());
            }
            result = true;
          } catch (NumberFormatException e) {
          }
        }
      }
    } else if (result && this.getProdpar() != null && !this.getProdpar().isEmpty()) {
      result = false;
      try {
        Double.parseDouble(this.getProdpar());
        result = true;
      } catch (NumberFormatException e) {
        // pass
      }
    }
    return result;
  }

  /**
   * @return the options
   */
  public String getOptions() {
    return options;
  }

  /**
   * @param options the options to set
   */
  public void setOptions(String options) {
    this.options = options;
  }
}
