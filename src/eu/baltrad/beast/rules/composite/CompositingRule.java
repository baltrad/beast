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
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import eu.baltrad.beast.ManagerContext;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
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
import eu.baltrad.fc.oh5.File;

/**
 * @author Anders Henja
 *
 */
public class CompositingRule implements IRule, ITimeoutRule {
  /**
   * The name of this static composite type
   */
  public final static String TYPE = "blt_composite";
  
  /**
   * The catalog for database access
   */
  Catalog catalog = null;

  /**
   * The timeout manager
   */
  TimeoutManager timeoutManager = null;
  
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
   * The recipients that are affected by this rule. Used
   * for generating timeout message
   */
  private List<String> recipients = new ArrayList<String>();
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(CompositingRule.class);

  /**
   * @param catalog the catalog to set
   */
  protected void setCatalog(Catalog catalog) {
    this.catalog = catalog;
  }

  /**
   * @param mgr the timeout manager to set
   */
  protected void setTimeoutManager(TimeoutManager mgr) {
    this.timeoutManager = mgr;
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
   * @throws InvalidArgumentException if interval not valid
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
   * @param ruleUtil the ruleUtil to set
   */
  public void setRuleUtilities(IRuleUtilities ruleUtil) {
    this.ruleUtil = ruleUtil;
  }

  /**
   * @return the ruleUtil
   */
  public IRuleUtilities getRuleUtilities() {
    return ruleUtil;
  }
  /**
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public synchronized IBltMessage handle(IBltMessage message) {
    logger.debug("handle(IBltMessage)");
    initialize();
    
    if (message instanceof BltDataMessage) {
      File file = ((BltDataMessage)message).getFile();
      String object = file.what_object();
      if (object != null && object.equals("SCAN") && isScanBased()) {
        return handleCompositeFromScans(message);
      } else if (object != null && object.equals("PVOL") && !isScanBased()) {
        return handleCompositeFromVolume(message);
      }
    }
    return null;
  }

  /**
   * Handles generation of a composite from a number of scans
   * @param data the composite timer data
   * @return a message or null if criterias not have been met.
   */
  protected IBltMessage handleCompositeFromScans(IBltMessage message) {
    logger.debug("handleCompositeFromScans(IBltMessage)");

    IBltMessage result = null;
    List<CatalogEntry> prevAngles = null;
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
        if (tt != null) {
          timeoutManager.unregister(tt.getId());
        }
      } else {
        if (tt == null && timeout > 0) {
          timeoutManager.register(this, timeout*1000, data);
        }
      }
    }
    
    return result;
  }


  /**
   * Creates a composite scan message if criterias are met.
   * @param data the composite timer data
   * @return a message if criterias are met, otherwise null
   */
  protected IBltMessage createCompositeScanMessage(CompositeTimerData data) {
    logger.debug("createCompositeScanMessage(CompositeTimerData)");

    DateTime nextTime = ruleUtil.createNextNominalTime(data.getDateTime(), interval);
    List<CatalogEntry> currAngles = ruleUtil.fetchLowestSourceElevationAngle(data.getDateTime(), nextTime, sources);
    List<CatalogEntry> prevAngles = data.getPreviousAngles();
    
    for (String src : sources) {
      CatalogEntry pentry = ruleUtil.getEntryBySource(src, prevAngles);
      CatalogEntry entry = ruleUtil.getEntryBySource(src, currAngles);
      if (pentry != null && entry != null) {
        double pelangle = (Double)pentry.getAttribute("where/elangle");
        double elangle = (Double)entry.getAttribute("where/elangle");
        if (pelangle < elangle) {
          return null;
        }
      } else {
        return null;
      }
    }

    return createMessage(data.getDateTime(), currAngles);
  }

  /**
   * Determines if a composite should be generated from a number
   * of volumes or not.
   * @param data the composite timer data
   * @return the message or null if criterias not have been met.
   */
  protected IBltMessage handleCompositeFromVolume(IBltMessage message) {
    logger.debug("handleCompositeFromVolume(IBltMessage)");

    IBltMessage result = null;
    CompositeTimerData data = createTimerData(message);
    if (data != null) {
      List<CatalogEntry> entries = fetchEntries(data.getDateTime());
      TimeoutTask tt = timeoutManager.getRegisteredTask(data);
      if (areCriteriasMet(entries)) {
        result = createMessage(data.getDateTime(), entries);
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
    return result;
  }
  
  /**
   * @see eu.baltrad.beast.rules.timer.ITimeoutRule#timeout(long, int, Object)
   */
  @Override
  public synchronized IBltMessage timeout(long id, int why, Object data) {
    logger.debug("timeout("+id+","+why+"," + data + ")");
    initialize();
    CompositeTimerData ctd = (CompositeTimerData)data;
    if (ctd != null) {
      List<CatalogEntry> entries = null;
      
      if (ctd.isScanBased()) {
        DateTime nextTime = ruleUtil.createNextNominalTime(ctd.getDateTime(), interval);
        entries = ruleUtil.fetchLowestSourceElevationAngle(ctd.getDateTime(), nextTime, sources);
      } else {
        entries = fetchEntries(ctd.getDateTime());
      }
      IBltMessage msgtosend = createMessage(ctd.getDateTime(), entries);
      BltMultiRoutedMessage mrmsg = new BltMultiRoutedMessage();
      mrmsg.setDestinations(recipients);
      mrmsg.setMessage(msgtosend);
      return mrmsg;
    }
    return null;
  }
  
  /**
   * Initializes the nessecary components like catalog and
   * timeout manager.
   * @throws RuntimeException if the nessecary components not could be aquired
   */
  protected synchronized void initialize() {
    logger.debug("initialize()");
    
    if (catalog == null) {
      catalog = ManagerContext.getCatalog();
    }
    if (timeoutManager == null) {
      timeoutManager = ManagerContext.getTimeoutManager();
    }
    if (ruleUtil == null) {
      ruleUtil = ManagerContext.getUtilities();
    }
    if (catalog == null || timeoutManager == null || ruleUtil == null) {
      throw new RuntimeException();
    }
  }
  
  /**
   * If possible creates a CompositingTimerData.
   * @param message the message (that should be a BltDataMessage)
   * @return a compositing timer data or null if not possible
   */
  protected CompositeTimerData createTimerData(IBltMessage message) {
    logger.debug("createTimerData(IBltMessage)");
    CompositeTimerData result = null;
    if (message instanceof BltDataMessage) {
      File file = ((BltDataMessage)message).getFile();
      String object = file.what_object();
      Time t = file.what_time();
      Date d = file.what_date();
      DateTime nominalTime = ruleUtil.createNominalTime(d, t, interval);
      if (!isScanBased() && object.equals("PVOL")) {
        result = new CompositeTimerData(ruleid, nominalTime);
      } else if (isScanBased() && object.equals("SCAN")) {
        result = new CompositeTimerData(ruleid, nominalTime, true);
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
    logger.debug("areCriteriasMet(List<CatalogEntry>)");
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
   * @param date the date
   * @param time the time
   * @param entries the list of entries
   * @return a message if criterias are fullfilled, otherwise null
   */
  protected IBltMessage createMessage(DateTime nominalDT, List<CatalogEntry> entries) {
    logger.debug("createMessage(DateTime, List<CatalogEntry>)");

    BltGenerateMessage result = new BltGenerateMessage();
    Date date = nominalDT.date();
    Time time = nominalDT.time();
    
    result.setAlgorithm("eu.baltrad.beast.GenerateComposite");

    result.setFiles(ruleUtil.getFilesFromEntries(nominalDT, sources, entries).toArray(new String[0]));

    String[] args = new String[3];
    args[0] = "--area="+area;
    args[1] = "--date="+new Formatter().format("%d%02d%02d",date.year(), date.month(), date.day()).toString(); 
    args[2] = "--time="+new Formatter().format("%02d%02d%02d",time.hour(), time.minute(), time.second()).toString();
    
    result.setArguments(args);

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
    logger.debug("fetchEntries(DateTime)");
    TimeIntervalFilter filter = createFilter(nominalTime);
    List<CatalogEntry> entries = catalog.fetch(filter);
    return entries;
  }
  
  /**
   * Returns a filter
   * @param date the date
   * @param time the time
   * @returns a TimeIntervalFilter for polar volumes  
   */
  protected TimeIntervalFilter createFilter(DateTime nominalDT) {
    logger.debug("createFilter(DateTime)");
    TimeIntervalFilter filter = new TimeIntervalFilter();
    filter.setObject("PVOL");
    DateTime stopDT = ruleUtil.createNextNominalTime(nominalDT, interval);
    filter.setStartDateTime(nominalDT);
    filter.setStopDateTime(stopDT);
    return filter;
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleRecipientAware#setRecipients(java.util.List)
   */
  @Override
  public void setRecipients(List<String> recipients) {
    this.recipients = recipients;
  }
}
