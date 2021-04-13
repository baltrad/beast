/* --------------------------------------------------------------------
Copyright (C) 2009-2010 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.rules.composite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.bdb.util.Time;
import eu.baltrad.bdb.util.TimeDelta;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.db.filters.CompositingRuleFilter;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.message.mo.BltMultiRoutedMessage;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.RuleUtils;
import eu.baltrad.beast.rules.timer.ITimeoutRule;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.timer.TimeoutTask;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * Compositing rule for beeing able to generate composites both from
 * scans and volumes. However, it is not possible to mix composites and scans
 * yet.
 * The composite by scan will always work on lowest elevation and composite
 * by volume is dependant on the receiving end. 
 * 
 * @author Anders Henja
 */
public class CompositingRule implements IRule, ITimeoutRule, InitializingBean {
  /**
   * The name of this static composite type
   */
  public final static String TYPE = "blt_composite";
  
  /** Performs the quality analysis and then applies the result to the original set */
  public final static int QualityControlMode_ANALYZE_AND_APPLY = 0;
  
  /** Only performs the quality analysis */
  public final static int QualityControlMode_ANALYZE = 1;  
  
  /**
   * If pixel should be determined by "closest to radar"
   */
  public final static int SelectionMethod_NEAREST_RADAR = 0;
  
  /**
   * If pixel should be determined by "nearest sea level"
   */
  public final static int SelectionMethod_HEIGHT_ABOVE_SEALEVEL = 1;
  
  /**
   * To set that method should be PPI compositing
   */
  public final static String PPI = "ppi";

  /**
   * To set that method should be CAPPPI compositing
   */
  public final static String CAPPI = "cappi";
  
  /**
   * To set that method should be PCAPPI compositing
   */
  public final static String PCAPPI = "pcappi";
  
  /**
   * Use Pseudo-Max compositing.
   */
  public final static String PMAX = "pmax";
  
  /**
   * Use Max compositing
   */
  public final static String MAX = "max";
  
  /**
   * The catalog for database access
   */
  private Catalog catalog = null;

  /**
   * The timeout manager
   */
  private TimeoutManager timeoutManager = null;
  
  /**
   * Utilities that simplifies database access
   */
  private IRuleUtilities ruleUtil = null;
  
  /**
   * The unique rule id separating this compositing rule from the others.
   */
  private int ruleid = -1;
  
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
  private boolean scanBased = false;
  
  /**
   * The selection method to use when determining the pixel
   */
  private int selectionMethod = SelectionMethod_NEAREST_RADAR; 
  
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
   * Incoming data exceeding this age threshold will not be handled 
   * by this rule. In minutes. -1 indicates that no max age limit 
   * will be applied.
   */
  private int maxAgeLimit = -1;
  
  /**
   * How the quality controls should be handled and used
   */
  private int qualityControlMode = QualityControlMode_ANALYZE_AND_APPLY;
  
  /**
   * Indicates quality controls shall always be reprocessed
   */
  private boolean reprocessQuality = false;
  
  /**
   * The recipients that are affected by this rule. Used
   * for generating timeout message
   */
  private List<String> recipients = new ArrayList<String>();
  
  /**
   * The filter used for matching files
   */
  private IFilter filter = null;
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(CompositingRule.class);

  /**
   * Default constructor,however, use manager for creation
   */
  protected CompositingRule() {
    //
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
   * @param mgr the timeout manager to set
   */
  protected void setTimeoutManager(TimeoutManager mgr) {
    this.timeoutManager = mgr;
  }
  
  /**
   * @return the timeout manager
   */
  protected TimeoutManager getTimeoutManager() {
    return this.timeoutManager;
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
   * @return the interval
   */
  public int getInterval() {
    return this.interval;
  }
  
  /**
   * @param timeout the timeout in seconds
   */
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }
  
  /*
   * @return the timeout in seconds
   */
  public int getTimeout() {
    return this.timeout;
  }
  
  /**
   * @param sources the list of sources to set
   */
  public void setSources(List<String> sources) {
    this.sources = sources;
  }
  
  /**
   * @return a list of sources
   */
  public List<String> getSources() {
    return this.sources;
  }
  
  /**
   * @param area the area to set
   */
  public void setArea(String area) {
    this.area = area;
  }
  
  /**
   * @return an area
   */
  public String getArea() {
    return this.area;
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
   * @param scanBased the scanBased to set
   */
  public void setScanBased(boolean scanBased) {
    this.scanBased = scanBased;
  }

  /**
   * @return the scanBased
   */
  public boolean isScanBased() {
    return scanBased;
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public synchronized IBltMessage handle(IBltMessage message) {
    logger.debug("ENTER: handle(IBltMessage)");
    try {
      IBltMessage generatedMessage = null;
      if (message instanceof BltDataMessage) {
        FileEntry file = ((BltDataMessage)message).getFileEntry();
        DateTime fileDateTime = getDateTimeFromFile(file);
        UUID fileUuid = file.getUuid();
        CompositingRuleFilter ruleFilter = createFilter(ruleUtil.createNominalTime(fileDateTime, getInterval()));
        if (dateTimeExceedsMaxAgeLimit(fileDateTime)) {
          logger.debug("CompositingRule - datetime in file " + fileUuid + " exceeds the maximum age limit of " + 
              getMaxAgeLimit() + " in rule. File not handled by rule.");
        } else if (ruleFilter.fileMatches(file)) {
          logger.info("ENTER: execute CompositingRule with ruleId: " + getRuleId() + ", thread: " + Thread.currentThread().getName() + 
              ", file: " + fileUuid);
          
          generatedMessage = createComposite(message, ruleFilter);
          
          logger.info("EXIT: execute CompositingRule with ruleId: " + getRuleId() + ", thread: " + Thread.currentThread().getName() + 
              ", file: " + fileUuid); 
        }
      }
      return generatedMessage;
    } finally {
      logger.debug("EXIT: handle(IBltMessage)");
    }
  }
  
  protected boolean dateTimeExceedsMaxAgeLimit(DateTime dateTime) {
    if (getMaxAgeLimit() == -1) {
      // -1 means disabled
      return false;
    }
    
    DateTime now = ruleUtil.nowDT();
    
    int secondsOffsetToLimit = -(60 * getMaxAgeLimit());
    TimeDelta timeDeltaToLimit = new TimeDelta().addSeconds(secondsOffsetToLimit);
    DateTime dateTimeLimit = now.add(timeDeltaToLimit);
    
    return dateTimeLimit.isAfter(dateTime);
  }
  
  protected IBltMessage createComposite(IBltMessage message, CompositingRuleFilter ruleFilter) {
    IBltMessage result = null;

    CompositeTimerData data = createTimerData(message);
    if (data == null) {
      return null;
    }
    
    TimeoutTask timeoutTask = timeoutManager.getRegisteredTask(data);
    if (timeoutTask == null) {
      Map<String, CatalogEntry> previousEntries = fetchPreviousEntriesMap(ruleFilter);
      if (previousEntries.size() > 0) {
        data.setPreviousEntries(previousEntries);        
      }
    } else {
      data = (CompositeTimerData)timeoutTask.getData();
    }
    
    Map<String,CatalogEntry> currentEntries = fetchEntriesMap(ruleFilter);
    
    boolean allSourcesPresent = true;
    for (String src : data.getPreviousSources()) {
      if (!currentEntries.containsKey(src)) {
        allSourcesPresent = false;
        break;
      } else if (isScanBased()) {
        // for scans, we check also that the lowest scan is received
        CatalogEntry currentEntry = currentEntries.get(src);
        Double currentElangle = (Double)currentEntry.getAttribute("/dataset1/where/elangle");
        Double previousElangle = data.getPreviousAngles().get(src);
        
        if (previousElangle == null || previousElangle.compareTo(currentElangle) < 0) {
          allSourcesPresent = false;
          break;
        }
      }
    }
    
    if (allSourcesPresent) {
      result = createMessage(data.getDateTime(), currentEntries);
      ruleUtil.trigger(ruleid, data.getDateTime());
      if (timeoutTask != null) {
        timeoutManager.unregister(timeoutTask.getId());
      }
    } else {
      if (timeoutTask == null && timeout > 0) {
        timeoutManager.register(this, ruleUtil.getTimeoutTime(data.getDateTime(), nominalTimeout, timeout*1000), data);
      }
    }

    return result;
  }
  
  /**
   * @see eu.baltrad.beast.rules.timer.ITimeoutRule#timeout(long, int, Object)
   */
  @Override
  public synchronized IBltMessage timeout(long id, int why, Object data) {
    logger.debug("ENTER: timeout("+id+","+why+"," + data + ")");
    IBltMessage result = null;
    CompositeTimerData ctd = (CompositeTimerData)data;
    if (ctd != null) {
      CompositingRuleFilter ruleFilter = createFilter(ctd.getDateTime());
      Map<String, CatalogEntry> entries = fetchEntriesMap(ruleFilter);

      if (!ruleUtil.isTriggered(ruleid, ctd.getDateTime())) {
        IBltMessage msgtosend = createMessage(ctd.getDateTime(), entries);
        BltMultiRoutedMessage mrmsg = new BltMultiRoutedMessage();
        mrmsg.setDestinations(recipients);
        mrmsg.setMessage(msgtosend);
        ruleUtil.trigger(ruleid, ctd.getDateTime());
        result = mrmsg;
      }
    }
    logger.debug("EXIT: timeout("+id+","+why+"," + data + ")");
    return result;
  }
  
  /**
   * If possible creates a CompositingTimerData.
   * @param message the message (that should be a BltDataMessage)
   * @return a compositing timer data or null if not possible
   */
  protected CompositeTimerData createTimerData(IBltMessage message) {
    CompositeTimerData result = null;
    if (message instanceof BltDataMessage) {
      FileEntry file = ((BltDataMessage)message).getFileEntry();
      DateTime nominalTime = getNominalTimeFromFile(file);

      if (!ruleUtil.isTriggered(ruleid, nominalTime)) {
        result = new CompositeTimerData(ruleid, nominalTime, isScanBased(), sources);
      }
    }
    
    return result;
  }
  
  protected DateTime getDateTimeFromFile(FileEntry file) {
    Metadata metaData = file.getMetadata();
    Time time = metaData.getWhatTime();
    Date date = metaData.getWhatDate();
    return new DateTime(date, time);
  }
  
  protected DateTime getNominalTimeFromFile(FileEntry file) {
    Metadata metaData = file.getMetadata();
    Time t = metaData.getWhatTime();
    Date d = metaData.getWhatDate();
    return ruleUtil.createNominalTime(d, t, interval);
  }

  /**
   * Creates a message if all nessecary entries are there
   * @param nominalDT the nominal time
   * @param entriesMap the list of entries
   * @return a message if criterias are fullfilled, otherwise null
   */
  protected IBltMessage createMessage(DateTime nominalDT, Map<String,CatalogEntry> entriesMap) {
    BltGenerateMessage result = new BltGenerateMessage();
    Date date = nominalDT.getDate();
    Time time = nominalDT.getTime();
    
    List<CatalogEntry> entries = new ArrayList<CatalogEntry>(entriesMap.values());
    
    result.setAlgorithm("eu.baltrad.beast.GenerateComposite");
    List<String> uuids = ruleUtil.getUuidStringsFromEntries(entries);
    List<String> usedSources = new ArrayList<String>(entriesMap.keySet());
    ruleUtil.reportRadarSourceUsage(sources, usedSources);
    
    result.setFiles(uuids.toArray(new String[0]));

    List<String> args = new ArrayList<String>();
    args.add("--area="+area);
    args.add("--date="+RuleUtils.getFormattedDate(date));
    args.add("--time="+RuleUtils.getFormattedTime(time));
    if (selectionMethod == SelectionMethod_NEAREST_RADAR) {
      args.add("--selection=NEAREST_RADAR");
    } else if (selectionMethod == SelectionMethod_HEIGHT_ABOVE_SEALEVEL) {
      args.add("--selection=HEIGHT_ABOVE_SEALEVEL");
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
      args.add("--qc-mode="+getQualityControlModeAsString());      
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
    if (getQitotalField() != null && !getQitotalField().equals("")) {
      args.add("--qitotal_field="+getQitotalField());
    }
    if (getQuantity() != null && !getQuantity().equals("")) {
      args.add("--quantity="+getQuantity());
    }
    if (isReprocessQuality()) {
      args.add("--reprocess_qfields=True");
    }
    args.add("--algorithm_id="+getRuleId());
    args.add("--merge=true");
    result.setArguments(args.toArray(new String[0]));

    logger.debug("CompositingRule createMessage - entries: " +
                 StringUtils.collectionToDelimitedString(uuids, " "));

    return result;
  }

  /**
   * Returns a filter
   * @param nominalDT the nominal time
   * @return a TimeIntervalFilter for polar volumes  
   */
  protected CompositingRuleFilter createFilter(DateTime nominalDT) {
    DateTime stopDT = ruleUtil.createNextNominalTime(nominalDT, interval);
    
    CompositingRuleFilter ruleFilter = 
        new CompositingRuleFilter(isScanBased(), quantity, sources, nominalDT, stopDT, filter);
    
    return ruleFilter;
  }
  
  protected Map<String,CatalogEntry> fetchPreviousEntriesMap(CompositingRuleFilter ruleFilter) {
    DateTime previousStartDT = ruleUtil.createPrevNominalTime(ruleFilter.getStartDateTime(), interval);
    CompositingRuleFilter previousFilter = createFilter(previousStartDT);
    
    return fetchEntriesMap(previousFilter);
  }
  
  protected Map<String,CatalogEntry> fetchEntriesMap(CompositingRuleFilter ruleFilter) {
    Map<String,CatalogEntry> result = new HashMap<String, CatalogEntry>();
    
    List<CatalogEntry> entries = catalog.fetch(ruleFilter);
    
    List<String> remainingSources = new ArrayList<String>(sources);
    
    for (CatalogEntry entry : entries) {
      String src = entry.getSource();
      if (remainingSources.contains(src)) {
        if (ruleFilter.fileMatches(entry.getFileEntry())) {
          result.put(src, entry);
          remainingSources.remove(src);          
        }
      }
    }

    return result;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRuleRecipientAware#setRecipients(java.util.List)
   */
  @Override
  public void setRecipients(List<String> recipients) {
    this.recipients = recipients;
  }

  /**
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() {
    if (catalog == null ||
        timeoutManager == null ||
        ruleUtil == null) {
      throw new BeanInitializationException("catalog, timeoutManager or ruleUtilities missing");
    }
  }

  /**
   * @param selectionMethod the selectionMethod to set
   */
  public void setSelectionMethod(int selectionMethod) {
    if (selectionMethod < SelectionMethod_NEAREST_RADAR || selectionMethod > SelectionMethod_HEIGHT_ABOVE_SEALEVEL) {
      throw new IllegalArgumentException();
    }
    this.selectionMethod = selectionMethod;
  }

  /**
   * Sets the selection method from a string
   * @param selectionMethod the selection method
   */
  public void setSelectionMethod(String selectionMethod) {
    if (selectionMethod.equalsIgnoreCase("NEAREST_RADAR")) {
      setSelectionMethod(SelectionMethod_NEAREST_RADAR);
    } else if (selectionMethod.equalsIgnoreCase("HEIGHT_ABOVE_SEALEVEL")) {
      setSelectionMethod(SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    } else {
      throw new IllegalArgumentException("Invalid selection method " + selectionMethod);
    }
  }
  
  /**
   * @return the selectionMethod
   */
  public int getSelectionMethod() {
    return selectionMethod;
  }
  
  public String getSelectionMethodAsString() {
    if (selectionMethod == SelectionMethod_NEAREST_RADAR) {
      return "NEAREST_RADAR";
    } else if (selectionMethod == SelectionMethod_HEIGHT_ABOVE_SEALEVEL) {
      return "HEIGHT_ABOVE_SEALEVEL";
    }
    return "UNKNOWN";
  }

  /**
   * @param detectors the detectors to set
   */
  public void setDetectors(List<String> detectors) {
    if (detectors == null) {
      this.detectors = new ArrayList<String>();
    } else {
      this.detectors = detectors;
    }
  }

  /**
   * @return the detectors
   */
  public List<String> getDetectors() {
    return detectors;
  }

  /**
   * @param method the method to set
   */
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * @return the method
   */
  public String getMethod() {
    return method;
  }

  /**
   * @param prodpar the prodpar to set
   */
  public void setProdpar(String prodpar) {
    this.prodpar = prodpar;
  }

  /**
   * @return the prodpar
   */
  public String getProdpar() {
    return prodpar;
  }

  /**
   * @return if GRA correction should be applied or not
   */
  public boolean isApplyGRA() {
    return applyGRA;
  }

  /**
   * @param applyGRA if GRA correction should be applied or not
   */
  public void setApplyGRA(boolean applyGRA) {
    this.applyGRA = applyGRA;
  }

  /**
   * @return the ZR A coefficient when converting from reflectivity to MM/H
   */
  public double getZR_A() {
    return ZR_A;
  }

  /**
   * @param zR_A the ZR A coefficient
   */
  public void setZR_A(double zR_A) {
    ZR_A = zR_A;
  }

  /**
   * @return the ZR b coefficient when converting from reflectivity to MM/H
   */
  public double getZR_b() {
    return ZR_b;
  }

  /**
   * @param zR_b the ZR b coefficient
   */
  public void setZR_b(double zR_b) {
    ZR_b = zR_b;
  }

  /**
   * @return if malfunc should be ignored or not
   */
  public boolean isIgnoreMalfunc() {
    return ignoreMalfunc;
  }

  /**
   * @param ignoreMalfunc if malfunc should be ignored or not
   */
  public void setIgnoreMalfunc(boolean ignoreMalfunc) {
    this.ignoreMalfunc = ignoreMalfunc;
  }

  /**
   * @return if ct filtering will be performed or not
   */
  public boolean isCtFilter() {
    return ctFilter;
  }

  /**
   * @param ctFilter if ct filtering should be performed or not
   */
  public void setCtFilter(boolean ctFilter) {
    this.ctFilter = ctFilter;
  }

  /**
   * @return the QI total field
   */
  public String getQitotalField() {
    return qitotalField;
  }

  /**
   * @param qitotalField the QI total field to set
   */
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
   * @return the maximum age limit in minutes
   */
  public int getMaxAgeLimit() {
    return maxAgeLimit;
  }

  /**
   * @param maxAgeLimit the maximum age limit in minutes
   */
  public void setMaxAgeLimit(int maxAgeLimit) {
    this.maxAgeLimit = maxAgeLimit;
  }

  public boolean isNominalTimeout() {
    return nominalTimeout;
  }

  public void setNominalTimeout(boolean nominalTimeout) {
    this.nominalTimeout = nominalTimeout;
  }

  public int getQualityControlMode() {
    return qualityControlMode;
  }
  
  public String getQualityControlModeAsString() {
    if (getQualityControlMode() == QualityControlMode_ANALYZE) {
      return "ANALYZE";
    } else if (getQualityControlMode() == QualityControlMode_ANALYZE_AND_APPLY) {
      return "ANALYZE_AND_APPLY";
    }
    return "UNKNOWN";
  }
  
  public void setQualityControlMode(String qualityControlMode) {
    if (qualityControlMode.equalsIgnoreCase("ANALYZE")) {
      this.qualityControlMode = QualityControlMode_ANALYZE;
    } else if (qualityControlMode.equalsIgnoreCase("ANALYZE_AND_APPLY")) {
      this.qualityControlMode = QualityControlMode_ANALYZE_AND_APPLY;
    } else {
      throw new IllegalArgumentException("Unknown quality control mode string " + qualityControlMode);
    }
  }
  
  public void setQualityControlMode(int qualityControlMode) {
    this.qualityControlMode = qualityControlMode;
  }
  
  public IFilter getFilter() {
    return filter;
  }

  public void setFilter(IFilter filter) {
    this.filter = filter;
  }

  public void setReprocessQuality(Boolean reprocessQuality) {
    this.reprocessQuality = reprocessQuality;
  }
  
  public boolean isReprocessQuality() {
    return this.reprocessQuality;
  }
}
