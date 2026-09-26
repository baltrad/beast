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
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.oh5.MetadataMatcher;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.IFilter;
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
   * Maximum elevation angle [deg]
   */
  private double maxelevationangle = 45.0;
  
  /**
   * KNMI method: conditional minimum elevation angle [deg]
   */
  private double conditionalminelevationangle = 9.5;
  
  /**
   * KNMI method: height threshold below which conditional minimum elevation is employed [m]
   */
  private double heightthreshold = 2000.0;
  
  /**
   * KNMI method: minimum Nyquist interval for use of scan [m/s]
   */
  private double minnyquistinterval = 10.0;
  
  /**
   * KNMI method: number of azimuth sector bins for detecting gaps
   */
  private int numbergapbins = 8;
  
  /**
   * KNMI method: minimum number of samples within an azimuth sector bin
   */
  private int minnumbergapsamples = 5;
  
  /**
   * KNMI method: maximum number standard deviations of residuals to include samples
   */
  private int maxnumberstandarddeviations = 0;
  
  /**
   * KNMI method: maximum deviation of a samples to the fit [m/s]
   */
  private double maxvelocitydeviation = 10.0;
  
  /**
   * Radial velocity threshold [m/s]
   */
  private double velocitythreshold = 2.0;
  
  /**
   * Maximum allowed layer velocity [m/s]
   */
  private double maxvelocitythreshold = 60.0; 
  
  /**
   * Minimum sample size for reflectivity
   */
  private int minsamplesizereflectivity = 40;
  
  /**
   * Minimum sample size for wind
   */
  private int minsamplesizewind = 40;

  /**
   * Processing method (SMHI or KNMI)
   */
  private string wrwpprocessingmethod = "SMHI";
  
  /**
   * A list of fields to generate
   */
  private List<String> fields = new ArrayList<String>();
  
  /**
   * The filter used for matching files
   */
  private IFilter filter = null;

  /**
   * The matcher used for verifying the filter
   */
  private MetadataMatcher matcher;
  
  private Pattern FIELDS_PATTERN = Pattern.compile("^[A-Za-z_0-9\\-]+$");
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(WrwpRule.class);
  
  public WrwpRule() {
    matcher = new MetadataMatcher();
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
   * @return Conditional minimum elevation angle [deg]
   */
  public double getConditionalminelevationangle() {
    return conditionalminelevationangle;
  }

  /**
   * @param conditionalminelevationangle the conditional minimum elevation angle [deg]
   */
  public void setConditionalminelevationangle(double conditionalminelevationangle) {
    this.conditionalminelevationangle = conditionalminelevationangle;
  }

  /**
   * @return Height threshold [m]
   */
  public double getHeightthreshold() {
    return heightthreshold;
  }

  /**
   * @param heightthreshold the height threshold [m]
   */
  public void setHeightthreshold(double heightthreshold) {
    this.heightthreshold = heightthreshold;
  }

  /**
   * @return Minimum Nyquist interval [m/s]
   */
  public double getMinnyquistinterval() {
    return minnyquistinterval;
  }

  /**
   * @param minnyquistinterval the minimum Nyquist interval [m/s]
   */
  public void setMinnyquistinterval(double minnyquistinterval) {
    this.minnyquistinterval = minnyquistinterval;
  }

  /**
   * @return Number of azimuth sector bins for detecting gaps
   */
  public int getNumbergapbins() {
    return numbergapbins;
  }

  /**
   * @param numbergapbins the number of azimuth sector bins for detecting gaps
   */
  public void setNumbergapbins(int numbergapbins) {
    this.numbergapbins = numbergapbins;
  }

  /**
   * @return Minimum number of samples within an azimuth sector bin
   */
  public int getMinnumbergapsamples() {
    return minnumbergapsamples;
  }

  /**
   * @param minnumbergapsamples the minimum number of samples within an azimuth sector bin
   */
  public void setMinnumbergapsamples(int minnumbergapsamples) {
    this.minnumbergapsamples = minnumbergapsamples;
  }

  /**
   * @return Maximum number standard deviations of residuals to include samples
   */
  public int getMaxnumberstandarddeviations() {
    return maxnumberstandarddeviations;
  }

  /**
   * @param maxnumberstandarddeviations the maximum number standard deviations of residuals to include samples
   */
  public void setMaxnumberstandarddeviations(int maxnumberstandarddeviations) {
    this.maxnumberstandarddeviations = maxnumberstandarddeviations;
  }

  /**
   * @return Maximum deviation of a samples to the fit [m/s]
   */
  public double getMaxvelocitydeviation() {
    return maxvelocitydeviation;
  }

  /**
   * @param maxvelocitydeviation the maximum deviation of a samples to the fit [m/s]
   */
  public void setMaxvelocitydeviation(double maxvelocitydeviation) {
    this.maxvelocitydeviation = maxvelocitydeviation;
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
   * @return WRWP extraction method (SMHI/KNMI)
   */
  public String getWrwpprocessingmethod() {
    return wrwpprocessingmethod;
  }

  /**
   * @param wrwpprocessingmethod the WRWP extraction method (SMHI/KNMI)
   */
  public void setWrwpprocessingmethod(String wrwpprocessingmethod) {
    this.wrwpprocessingmethod = wrwpprocessingmethod;
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
   * @return a comma separated list of fields to generate
   */
  public String getFieldsAsStr() {
    String result = "";
    if (fields.size()==0)
      return "";
    
    for (String s : fields) {
      result = result + "," + s;
    }
    
    return result.substring(1);
  }

  /**
   * @return a list of fields
   */
  public List<String> getFields() {
    return fields;
  }
  
  /**
   * @param fields a comma separated list of fields to generate
   */
  public boolean setFields(String fields) {
    List<String> newFields = new ArrayList<String>();
    
    if (fields==null)
      return false;
    
    if (fields.trim().length()==0) {
      this.fields = newFields;
      return true;
    }
    
    StringTokenizer tokenizer=new StringTokenizer(fields, ",");
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken().trim();
      Matcher m = FIELDS_PATTERN.matcher(token);
      if (!m.matches())
        return false;
      newFields.add(token);
    }
    this.fields = newFields;
    return true;
  }
  
  /**
   * Sets the fields to generate 
   * @param fields the fields to generate
   * @return if field names are allowed
   */
  public boolean setFields(List<String> fields) {
    List<String> newFields = new ArrayList<String>();
    for (String s : fields) {
      String fname = s.trim();
      Matcher m = FIELDS_PATTERN.matcher(fname);
      if (!m.matches())
        return false;
      newFields.add(fname);
    }
    this.fields = newFields;
    return true;
  }
  
  public IFilter getFilter() {
    return filter;
  }

  public void setFilter(IFilter filter) {
    this.filter = filter;
  }

  public MetadataMatcher getMatcher() {
    return matcher;
  }

  public void setMatcher(MetadataMatcher matcher) {
    this.matcher = matcher;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    BltGenerateMessage result = null;
    
    if (message instanceof BltDataMessage) {
      FileEntry file = ((BltDataMessage)message).getFileEntry();
      if (handleFile(file)) {
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
          args.add("--maxelevationangle="+maxelevationangle);
          args.add("--conditionalminelevationangle="+conditionalminelevationangle);
          args.add("--heightthreshold="+heightthreshold);
          args.add("--minnyquistinterval="+minnyquistinterval);
          args.add("--numbergapbins="+numbergapbins);
          args.add("--minnumbergapsamples="+minnumbergapsamples);
          args.add("--maxnumberstandarddeviations="+maxnumberstandarddeviations);
          args.add("--maxvelocitydeviation="+maxvelocitydeviation);
          args.add("--velocitythreshold="+velocitythreshold);
          args.add("--maxvelocitythreshold="+maxvelocitythreshold);
          args.add("--minsamplesizereflectivity="+minsamplesizereflectivity);
          args.add("--minsamplesizewind="+minsamplesizewind);
          args.add("--wrwpprocessingmethod="+wrwpprocessingmethod);
          if (fields.size() > 0)
            args.add("--fields="+getFieldsAsStr());
          result.setArguments(args.toArray(new String[0]));
        }
      }
    }

    return result;
  }
  
  private boolean handleFile(FileEntry file) {
    boolean handleFile = false;
    if (file.getMetadata().getWhatObject().equals("PVOL")) {
      if (filter == null || matcher.match(file.getMetadata(), filter.getExpression())) {
        handleFile = true;
      }
    }
    
    return handleFile;
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

  public double getMaxelevationangle() {
    return maxelevationangle;
  }

  public void setMaxelevationangle(double maxelevationangle) {
    this.maxelevationangle = maxelevationangle;
  }

  public double getMaxvelocitythreshold() {
    return maxvelocitythreshold;
  }

  public void setMaxvelocitythreshold(double maxvelocitythreshold) {
    this.maxvelocitythreshold = maxvelocitythreshold;
  }

  public int getMinsamplesizereflectivity() {
    return minsamplesizereflectivity;
  }

  public void setMinsamplesizereflectivity(int minsamplesizereflectivity) {
    this.minsamplesizereflectivity = minsamplesizereflectivity;
  }

  public int getMinsamplesizewind() {
    return minsamplesizewind;
  }

  public void setMinsamplesizewind(int minsamplesizewind) {
    this.minsamplesizewind = minsamplesizewind;
  }
}
