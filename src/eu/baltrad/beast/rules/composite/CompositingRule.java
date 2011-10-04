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
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.filters.LowestAngleFilter;
import eu.baltrad.beast.db.filters.TimeIntervalFilter;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.message.mo.BltMultiRoutedMessage;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.timer.ITimeoutRule;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.timer.TimeoutTask;
import eu.baltrad.beast.rules.util.IRuleUtilities;
import eu.baltrad.fc.Date;
import eu.baltrad.fc.DateTime;
import eu.baltrad.fc.Time;
import eu.baltrad.fc.FileEntry;

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
   * The recipients that are affected by this rule. Used
   * for generating timeout message
   */
  private List<String> recipients = new ArrayList<String>();
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(CompositingRule.class);

  /**
   * Default constructor,however, use manager for creation
   */
  protected CompositingRule() {
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
      if (message instanceof BltDataMessage) {
        FileEntry file = ((BltDataMessage)message).getFileEntry();
        String object = file.metadata().what_object();
        if (object != null && object.equals("SCAN") && isScanBased()) {
          return handleCompositeFromScans(message);
        } else if (object != null && object.equals("PVOL") && !isScanBased()) {
          return handleCompositeFromVolume(message);
        }
      }
      return null;
    } finally {
      logger.debug("EXIT: handle(IBltMessage)");
    }
  }

  /**
   * Handles generation of a composite from a number of scans
   * @param message the @ref eu.baltrad.beast.message.mo.BltDataMessage containing a file scan 
   * @return a message or null if criterias not have been met.
   */
  protected IBltMessage handleCompositeFromScans(IBltMessage message) {
    logger.debug("ENTER: handleCompositeFromScans(IBltMessage)");

    IBltMessage result = null;
    Map<String, Double> prevAngles = null;
    CompositeTimerData data = createTimerData(message);
    if (data != null) {
      TimeoutTask tt = timeoutManager.getRegisteredTask(data);
      if (tt == null) {
        DateTime prevDateTime = ruleUtil.createPrevNominalTime(data.getDateTime(), interval);
        prevAngles = ruleUtil.fetchLowestSourceElevationAngle(prevDateTime, data.getDateTime(), sources);
        data.setPreviousAngles(prevAngles);
      } else {
        data = (CompositeTimerData)tt.getData();
      }
    
      result = createCompositeScanMessage(data);
      if (result != null) {
        ruleUtil.trigger(ruleid, data.getDateTime());
        if (tt != null) {
          timeoutManager.unregister(tt.getId());
        }
      } else {
        if (tt == null && timeout > 0) {
          timeoutManager.register(this, timeout*1000, data);
        }
      }
    }
    logger.debug("EXIT: handleCompositeFromScans(IBltMessage)");
    return result;
  }


  /**
   * Creates a composite scan message if criterias are met.
   * @param data the composite timer data
   * @return a message if criterias are met, otherwise null
   */
  protected IBltMessage createCompositeScanMessage(CompositeTimerData data) {
    logger.debug("ENTER: createCompositeScanMessage(CompositeTimerData)");
    try {
      DateTime nextTime = ruleUtil.createNextNominalTime(data.getDateTime(), interval);
      Map<String, Double> currAngles = ruleUtil.fetchLowestSourceElevationAngle(data.getDateTime(), nextTime, sources);
      Map<String, Double> prevAngles = data.getPreviousAngles();
    
      for (String src : sources) {
        Double pelangle = prevAngles.get(src);
        Double elangle = currAngles.get(src);
        if (pelangle != null && elangle != null) {
          if (pelangle.compareTo(elangle) < 0) {
            return null;
          }
        } else {
          return null;
        }
      }
      List<CatalogEntry> entries = fetchScanEntries(data.getDateTime());
      return createMessage(data.getDateTime(), entries);
    } finally {
      logger.debug("EXIT: createCompositeScanMessage(CompositeTimerData)");
    }
  }

  /**
   * Determines if a composite should be generated from a number
   * of volumes or not.
   * @param message the @ref eu.baltrad.beast.message.mo.BltDataMessage containing a file volume
   * @return the message or null if criterias not have been met.
   */
  protected IBltMessage handleCompositeFromVolume(IBltMessage message) {
    logger.debug("ENTER: handleCompositeFromVolume(IBltMessage)");

    IBltMessage result = null;
    CompositeTimerData data = createTimerData(message);
    if (data != null) {
      List<CatalogEntry> entries = fetchEntries(data.getDateTime());
      TimeoutTask tt = timeoutManager.getRegisteredTask(data);
      if (areCriteriasMet(entries)) {
        result = createMessage(data.getDateTime(), entries);
        ruleUtil.trigger(ruleid, data.getDateTime());
        if (tt != null) {
          timeoutManager.unregister(tt.getId());
        }
      } else {
        if (tt == null) {
          if (timeout > 0) {
            timeoutManager.register(this, timeout*1000, data);
          }
        }
      }
    }
    logger.debug("EXIT: handleCompositeFromVolume(IBltMessage)");
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
      List<CatalogEntry> entries = null;
      
      if (ctd.isScanBased()) {
        entries = fetchScanEntries(ctd.getDateTime());
      } else {
        entries = fetchEntries(ctd.getDateTime());
      }
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
      String object = file.metadata().what_object();
      Time t = file.metadata().what_time();
      Date d = file.metadata().what_date();
      DateTime nominalTime = ruleUtil.createNominalTime(d, t, interval);
      if (!ruleUtil.isTriggered(ruleid, nominalTime)) {
        if (!isScanBased() && object.equals("PVOL")) {
          result = new CompositeTimerData(ruleid, nominalTime);
        } else if (isScanBased() && object.equals("SCAN")) {
          result = new CompositeTimerData(ruleid, nominalTime, true);
        }
      }
    }
    
    return result;
  }
  
  /**
   * Verifies if the criterias has been met so that we can create
   * the message.
   * @param entries a list of catalog entries
   * @return true if the criterias has been met.
   */
  protected boolean areCriteriasMet(List<CatalogEntry> entries) {
    List<String> es = ruleUtil.getSourcesFromEntries(entries);
    for (String s : sources) {
      if (!es.contains(s)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Creates a message if all nessecary entries are there
   * @param nominalDT the nominal time
   * @param entries the list of entries
   * @return a message if criterias are fullfilled, otherwise null
   */
  protected IBltMessage createMessage(DateTime nominalDT, List<CatalogEntry> entries) {
    BltGenerateMessage result = new BltGenerateMessage();
    Date date = nominalDT.date();
    Time time = nominalDT.time();
    
    result.setAlgorithm("eu.baltrad.beast.GenerateComposite");
    logger.debug("createMessage: entries: " +
                 StringUtils.collectionToDelimitedString(getUuidsFromEntries(entries), " "));
    logger.debug("createMessage: filtering for nominal time: " + nominalDT.to_iso_string());
    entries = ruleUtil.getEntriesByClosestTime(nominalDT, entries);
    logger.debug("createMessage: entries: " +
                 StringUtils.collectionToDelimitedString(getUuidsFromEntries(entries), " "));
    logger.debug("createMessage: filtering for sources: " +
                 StringUtils.collectionToDelimitedString(sources, " "));
    entries = ruleUtil.getEntriesBySources(sources, entries);
    logger.debug("createMessage: entries: " +
                 StringUtils.collectionToDelimitedString(getUuidsFromEntries(entries), " "));
    
    List<String> files = ruleUtil.getFilesFromEntries(entries);
    result.setFiles(files.toArray(new String[0]));

    List<String> args = new ArrayList<String>();
    args.add("--area="+area);
    args.add("--date="+new Formatter().format("%d%02d%02d",date.year(), date.month(), date.day()).toString()); 
    args.add("--time="+new Formatter().format("%02d%02d%02d",time.hour(), time.minute(), time.second()).toString());
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
    }
    args.add("--method="+this.method);
    args.add("--prodpar="+this.prodpar);
    
    result.setArguments(args.toArray(new String[0]));

    logger.debug("createMessage: Returning algorithm " + result.getAlgorithm());

    return result;
  }
  
  /**
   * Fetches the entries that are from the nominal time up until 
   * the stop time defined by the interval.
   * @param nominalTime the nominal time
   * @return a list of entries
   */
  protected List<CatalogEntry> fetchEntries(DateTime nominalTime) {
    TimeIntervalFilter filter = createFilter(nominalTime);
    List<CatalogEntry> entries = catalog.fetch(filter);
    return entries;
  }

  /**
   * Returns a filter
   * @param nominalDT the nominal time
   * @return a TimeIntervalFilter for polar volumes  
   */
  protected TimeIntervalFilter createFilter(DateTime nominalDT) {
    TimeIntervalFilter filter = new TimeIntervalFilter();
    filter.setObject("PVOL");
    DateTime stopDT = ruleUtil.createNextNominalTime(nominalDT, interval);
    filter.setStartDateTime(nominalDT);
    filter.setStopDateTime(stopDT);
    return filter;
  }
  
  /**
   * Fetches lowest sweep scan for sources between nomialDT and
   * nominalDT + interval
   * @param nominalDT the nominal time
   * @return list of entries
   */
  protected List<CatalogEntry> fetchScanEntries(DateTime nominalDT) {
    List<CatalogEntry> result = new ArrayList<CatalogEntry>();
    LowestAngleFilter filter = createScanFilter(nominalDT);
    for (String src : sources) {
      filter.setSource(src);
      List<CatalogEntry> entries = catalog.fetch(filter);
      result.addAll(entries);
    }
    return result;
  }

  protected LowestAngleFilter createScanFilter(DateTime nominalDT) {
    LowestAngleFilter filter = new LowestAngleFilter();
    filter.setStart(nominalDT);
    DateTime stopDT = ruleUtil.createNextNominalTime(nominalDT, interval);
    filter.setStop(stopDT);
    return filter;
  }

  protected List<String> getUuidsFromEntries(List<CatalogEntry> entries) {
    List<String> uuids = new ArrayList<String>(entries.size());
    for (CatalogEntry e: entries) {
      uuids.add(e.getUuid());
    }
    return uuids;
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
   * @return the selectionMethod
   */
  public int getSelectionMethod() {
    return selectionMethod;
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
   * @param algorithm the algorithm to set
   */
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * @return the algorithm
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
}
