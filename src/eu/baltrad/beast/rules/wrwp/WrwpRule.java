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

package eu.baltrad.beast.rules.wrwp;

import java.util.ArrayList;
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
 * @author Anders Henja
 *
 */
public class WrwpRule implements IRule, InitializingBean {
  /**
   * The name of this static wrwp type
   */
  public final static String TYPE = "blt_wrwp";
  
  /**
   * The unique rule id separating this wrwp rule from the others.
   */
  private int ruleid = -1;
  
  /**
   * The catalog for database access
   */
  private Catalog catalog = null;

  /**
   * Utilities that simplifies database access
   */
  private IRuleUtilities ruleUtil = null;

  /**
   * The polar volume sources this rule should be executed for
   */
  private List<String> sources = new ArrayList<String>();

  /**
   * Height interval for deriving a profile [m]
   */
  private int interval = 200;
  
  /**
   * Maximum height of the profile [m]
   */
  private int maxheight = 12000;

  /**
   * Minimum distance for deriving a profile [m]
   */
  private int mindistance = 4000;
  
  /**
   * Maximum distance for deriving a profile [m]
   */
  private int maxdistance = 40000;

  /**
   * Minimum elevation angle [deg]
   */
  private double minelevationangle = 2.5;

  /**
   * Radial velocity threshold [m/s]
   */
  private double velocitythreshold = 2.0;
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(WrwpRule.class);
  
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
   * @return Height interval for deriving a profile [m]
   */
  public int getInterval() {
    return interval;
  }

  /**
   * @param interval The height interval for deriving a profile [m]
   */
  public void setInterval(int interval) {
    this.interval = interval;
  }

  /**
   * @return Maximum height of the profile [m]
   */
  public int getMaxheight() {
    return maxheight;
  }

  /**
   * @param maxheight the maximum height of the profile [m]
   */
  public void setMaxheight(int maxheight) {
    this.maxheight = maxheight;
  }

  /**
   * @return Minimum distance for deriving a profile [m]
   */
  public int getMindistance() {
    return mindistance;
  }

  /**
   * @param mindistance the minimum distance for deriving a profile [m]
   */
  public void setMindistance(int mindistance) {
    this.mindistance = mindistance;
  }

  /**
   * @return Maximum distance for deriving a profile [m]
   */
  public int getMaxdistance() {
    return maxdistance;
  }

  /**
   * @param maxdistance the maximum distance for deriving a profile [m]
   */
  public void setMaxdistance(int maxdistance) {
    this.maxdistance = maxdistance;
  }

  /**
   * @return Minimum elevation angle [deg]
   */
  public double getMinelevationangle() {
    return minelevationangle;
  }

  /**
   * @param minelevationangle the minimum elevation angle [deg]
   */
  public void setMinelevationangle(double minelevationangle) {
    this.minelevationangle = minelevationangle;
  }

  /**
   * @return Radial velocity threshold [m/s]
   */
  public double getMinvelocitythreshold() {
    return velocitythreshold;
  }

  /**
   * @param velocitythreshold the radial velocity threshold [m/s]
   */
  public void setMinvelocitythreshold(double velocitythreshold) {
    this.velocitythreshold = velocitythreshold;
  }

  /**
   * @param sources the sources this rule should be used for
   */
  public void setSources(List<String> sources) {
    this.sources = sources;
  }
  
  /**
   * @return the sources this rule is used for
   */
  public List<String> getSources() {
    return this.sources;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    BltGenerateMessage result = null;
    
    if (message instanceof BltDataMessage) {
      FileEntry file = ((BltDataMessage)message).getFileEntry();
      if (file.getMetadata().getWhatObject().equals("PVOL")) {
        String s = file.getSource().getName();
        if (sources.size() > 0 && sources.contains(s)) {
          logger.debug("Creating a message to generate a wrwp product for '"+s+"'");
          result = new BltGenerateMessage();
          result.setAlgorithm("eu.baltrad.beast.GenerateWrwp");
          result.setFiles(new String[]{file.getUuid().toString()});
          List<String> args = new ArrayList<String>();
          args.add("--interval="+interval);
          args.add("--maxheight="+maxheight);
          args.add("--mindistance="+mindistance);
          args.add("--maxdistance="+maxdistance);
          args.add("--minelevationangle="+minelevationangle);
          args.add("--velocitythreshold="+velocitythreshold);
          result.setArguments(args.toArray(new String[0]));
        }
      }
    }

    return result;
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
  public void afterPropertiesSet() throws Exception {
    if (catalog == null ||
        ruleUtil == null) {
      throw new BeanInitializationException("catalog or ruleUtilities missing");
    }    
  }
}
