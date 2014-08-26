/* --------------------------------------------------------------------
Copyright (C) 2009-2013 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.rules.site2d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * Site 2D rule for beeing able to generate images from single scans & volumes.
 *
 * @author Anders Henja
 */
public class Site2DRule implements IRule, InitializingBean {
  /**
   * The name of this static composite type
   */
  public final static String TYPE = "blt_site2d";
  
  /**
   * To set that method should be PPI generation
   */
  public final static String PPI = "ppi";

  /**
   * To set that method should be CAPPPI generation
   */
  public final static String CAPPI = "cappi";
  
  /**
   * To set that method should be PCAPPI generation
   */
  public final static String PCAPPI = "pcappi";

  /**
   * Use Pseudo-Max generation.
   */
  public final static String PMAX = "pmax";
  
  /**
   * Use Max generation
   */
  public final static String MAX = "max";

  /**
   * The catalog for database access
   */
  private Catalog catalog = null;
  
  /**
   * Utilities that simplifies database access
   */
  private IRuleUtilities ruleUtil = null;
  
  /**
   * The interval in minutes for a composite.
   * Always starts at 0 and interval minutes ahead.
   * E.g.
   * Interval = 12 =>
   * 00 - 12
   * 12 - 24
   * 24 - 36
   * 36 - 48
   * 48 - 00
   */
  private int interval = 15;
  
  /**
   * A list of sources (e.g. seang, sekkr, ...)
   */
  private List<String> sources = new ArrayList<String>();
  
  /**
   * The area that this composite should cover. If not specified a best fit approach will be used.
   */
  private String area = null;
  
  /**
   * If it is scans or volumes that should trigger the site 2d product
   */
  private boolean scanBased = false;
  
  /**
   * Detectors that should be run for this composite rule
   */
  private List<String> detectors = new ArrayList<String>();
  
  /**
   * The algorithm to use. CAPPI and PCAPPI is really only meaningful for volumes but we are not discriminating anyone. 
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
   * Indicates if malfunc should be ignored or not when generating the product
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
   * the rule id
   */
  private int ruleid = -1;

  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(Site2DRule.class);
  
  /**
   * @param catalog the catalog to set
   */
  protected void setCatalog(Catalog catalog) {
    this.catalog = catalog;
  }

  /**
   * @return the catalog
   */
  protected Catalog getCatalog() {
    return this.catalog;
  }

  /**
   * @param ruleUtil the ruleUtil to set
   */
  protected void setRuleUtilities(IRuleUtilities ruleUtil) {
    this.ruleUtil = ruleUtil;
  }

  /**
   * @return the ruleUtil
   */
  protected IRuleUtilities getRuleUtilities() {
    return ruleUtil;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    logger.debug("ENTER: handle(IBltMessage)");
    if (message instanceof BltDataMessage) {
      FileEntry file = ((BltDataMessage)message).getFileEntry();
      String object = file.getMetadata().getWhatObject();
      String src = file.getSource().getName();
      if (object != null && 
          ((scanBased && object.equals("SCAN")) || (!scanBased && object.equals("PVOL"))) && 
          src != null && sources.contains(src)) {
        return createMessage(file.getUuid().toString());
      }
    }
    return null;
  }

  protected BltGenerateMessage createMessage(String filename) {
    BltGenerateMessage result = new BltGenerateMessage();
    result.setAlgorithm("eu.baltrad.beast.GenerateSite2D");
    result.setFiles(new String[]{filename});
    List<String> args = new ArrayList<String>();
    
    if (this.area != null) { 
      args.add("--area="+area);
    }
    
    if (detectors.size() > 0) {
      StringBuffer dstr = new StringBuffer();
      Iterator<String> iterator = detectors.iterator();
      while (iterator.hasNext()) {
        dstr.append(iterator.next());
        if (iterator.hasNext()) {
          dstr.append(",");
        }
      }
      args.add("--anomaly-qc="+dstr.toString());
    }
    args.add("--method="+this.method);
    args.add("--prodpar="+this.prodpar);
    if (isApplyGRA()) {
      args.add("--applygra=true");
      args.add("--zrA="+this.ZR_A);
      args.add("--zrb="+this.ZR_b);
    }
    
    if (isIgnoreMalfunc()) {
      args.add("--ignore-malfunc=true");
    }
    if (isCtFilter()) {
      args.add("--ctfilter=True");
    }
    
    result.setArguments(args.toArray(new String[0]));

    return result;
  }
  
  /**
   * @param ruleid the ruleid to set
   */
  public void setRuleId(int ruleid) {
    this.ruleid = ruleid;
  }

  /**
   * @return the ruleid
   */
  public int getRuleId() {
    return ruleid;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRule#getType()
   */
  @Override
  public String getType() {
    return TYPE;
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#isValid()
   */
  @Override
  public boolean isValid() {
    return true;
  }

  /**
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() {
    if (catalog == null ||
        ruleUtil == null) {
      throw new BeanInitializationException("catalog or ruleUtilities missing");
    }
  }

  /**
   * @return the interval
   */
  public int getInterval() {
    return interval;
  }

  /**
   * Sets the interval to be used. Must be a valid integer that
   * is greater than 0 and evenly dividable by 60. E.g.
   * 1,2,3,4,...15,....30,60 
   * @param interval
   * @throws IllegalArgumentException if interval not valid
   */
  public void setInterval(int interval) {
    if (interval != 0 && 60%interval == 0) {
      this.interval = interval;
    } else {
      throw new IllegalArgumentException("interval not valid");
    }
  }

  /**
   * @return the sources this rule affects
   */
  public List<String> getSources() {
    return sources;
  }

  /**
   * @param sources the sources this rule should affect
   */
  public void setSources(List<String> sources) {
    this.sources = sources;
  }

  /**
   * @return the area (can be null)
   */
  public String getArea() {
    return area;
  }

  /**
   * @param area the area to use for the generated product (may be null and in that case, best fit approach is used)
   */
  public void setArea(String area) {
    this.area = area;
  }

  /**
   * @return if this rule affects scans or not. If false, then volumes are handled.
   */
  public boolean isScanBased() {
    return scanBased;
  }

  /**
   * @param scanBased if this rule should affect scans or not. If false, the volumes are handled
   */
  public void setScanBased(boolean scanBased) {
    this.scanBased = scanBased;
  }

  /**
   * @return the detectors to be used during the site 2d generation
   */
  public List<String> getDetectors() {
    return detectors;
  }

  /**
   * param detectors the detectors to be used during the site 2d generation
   */
  public void setDetectors(List<String> detectors) {
    if (detectors == null) {
      this.detectors = new ArrayList<String>();
    } else {
      this.detectors = detectors;
    }
  }

  /**
   * @return the method to use for the generation
   */
  public String getMethod() {
    return method;
  }

  /**
   * @param method the method to use for the generation
   */
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * @return the product parameters to use in the method
   */
  public String getProdpar() {
    return prodpar;
  }

  /**
   * @param prodpar the product parameters to use in the method
   */
  public void setProdpar(String prodpar) {
    if (prodpar == null) {
      this.prodpar = "";
    } else {
      this.prodpar = prodpar;
    }
  }

  /**
   * @return if gra adjustment should be performed or not
   */
  public boolean isApplyGRA() {
    return applyGRA;
  }

  /**
   * @param applyGRA if gra adjustment should be performed or not
   */
  public void setApplyGRA(boolean applyGRA) {
    this.applyGRA = applyGRA;
  }

  /**
   * @return if malfunc scans should be ignored or not. 
   */
  public boolean isIgnoreMalfunc() {
    return ignoreMalfunc;
  }

  /**
   * @param ignoreMalfunc if malfunc scans should be ignored or not.
   */
  public void setIgnoreMalfunc(boolean ignoreMalfunc) {
    this.ignoreMalfunc = ignoreMalfunc;
  }

  /**
   * @return if ct filtering should be applied or not
   */
  public boolean isCtFilter() {
    return ctFilter;
  }

  /**
   * @param ctFilter  if ct filtering should be applied or not
   */
  public void setCtFilter(boolean ctFilter) {
    this.ctFilter = ctFilter;
  }

  /**
   * @return the ZR-A conversion factor
   */
  public double getZR_A() {
    return ZR_A;
  }

  /**
   * @param zR_A  the ZR-A conversion factor
   */
  public void setZR_A(double zR_A) {
    ZR_A = zR_A;
  }

  /**
   * @return  the ZR-b conversion factor
   */
  public double getZR_b() {
    return ZR_b;
  }

  /**
   * @param zR_b  the ZR-b conversion factor
   */
  public void setZR_b(double zR_b) {
    ZR_b = zR_b;
  }
}
