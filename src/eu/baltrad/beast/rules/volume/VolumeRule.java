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
package eu.baltrad.beast.rules.volume;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.Period;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.oh5.MetadataMatcher;
import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.bdb.util.Time;
import eu.baltrad.bdb.util.TimeDelta;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.db.filters.TimeIntervalFilter;
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
 * @author Anders Henja
 *
 */
public class VolumeRule implements IRule, ITimeoutRule, InitializingBean {
  /**
   * The name of this static composite type
   */
  public final static String TYPE = "blt_volume";

  /** Performs the quality analysis and then applies the result to the original set */
  public final static int QualityControlMode_ANALYZE_AND_APPLY = 0;
  
  /** Only performs the quality analysis */
  public final static int QualityControlMode_ANALYZE = 1;  
  
  /**
   * The catalog for database access
   */
  private Catalog catalog = null;

  /**
   * The timeout manager
   */
  private TimeoutManager timeoutManager = null;
  
  /**
   * If the timeout should be calculated from the nominal time or from the arrival of the first file
   * belonging to this product.
   */
  private boolean nominalTimeout = false;
  
  /**
   * The rule utilities
   */
  private IRuleUtilities ruleUtilities = null;
  
  /**
   * The unique rule id separating this volume rule from the others.
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
   * Min elevation
   */
  private double eMin = 0.0;
  
  /**
   * Max elevation
   */
  private double eMax = 45.0;
  
  /**
   * If the scans are done in ascending order or not
   */
  private boolean ascending = true;
  
  /**
   * If elevation angle usage should be determined from the previous nominal times data set
   */
  private boolean adaptiveElevationAngles = false;
  
  /**
   * How the quality controls should be handled and used
   */
  private int qualityControlMode = QualityControlMode_ANALYZE_AND_APPLY;
  
  /**
   * The sources for which this rule applies
   */
  private List<String> sources = new ArrayList<String>();
  
  /**
   * The recipients that are affected by this rule. Used
   * for generating timeout message
   */
  private List<String> recipients = new ArrayList<String>();

  /**
   * A mapping of previously handled jobs for respective source
   */
  private Map<String,VolumeTimerData> handledData = new HashMap<String, VolumeTimerData>();

  /**
   * Detectors that should be run for this composite rule
   */
  private List<String> detectors = new ArrayList<String>();

  /**
   * The elevation angles
   */
  private List<Double> elevationAngles = new ArrayList<Double>();
  
  /**
   * The filter used for matching files
   */
  private IFilter filter = null;

  /**
   * The matcher used for verifying the filter
   */
  private MetadataMatcher matcher;
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(VolumeRule.class);
  
  /**
   * Default constructor, however use manager for creation.
   */
  protected VolumeRule() {
    matcher = new MetadataMatcher();
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
   * @param utilities the rule utilities
   */
  protected void setRuleUtilities(IRuleUtilities utilities) {
    this.ruleUtilities = utilities;
  }
  
  /**
   * @return the rule utilities
   */
  protected IRuleUtilities getRuleUtilities() {
    return this.ruleUtilities;
  }
  
  /**
   * @param ruleid the ruleid to set
   */
  protected void setRuleId(int ruleid) {
    this.ruleid = ruleid;
  }

  /**
   * @return the ruleid
   */
  public int getRuleId() {
    return ruleid;
  }

  /**
   * @param timeout the timeout to set
   */
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }
  
  /**
   * @return the timeout
   */
  public int getTimeout() {
    return this.timeout;
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
   * @param e the min elevation angle to set in degrees
   */
  public void setElevationMin(double e) {
    this.eMin = e;
  }
  
  /**
   * returns the min elevation angle in degrees
   * @return the min elevation
   */
  public double getElevationMin() {
    return this.eMin;
  }

  /**
   * @param e the min elevation angle to set in degrees
   */
  public void setElevationMax(double e) {
    this.eMax = e;
  }
  
  /**
   * returns the min elevation angle in degrees
   * @return the max elevation
   */
  public double getElevationMax() {
    return this.eMax;
  }

  /**
   * @param ascending sets if the scans are performed in ascending order or not, default = true
   */
  public void setAscending(boolean ascending) {
    this.ascending = ascending;
  }

  /**
   * @return if the scans are performed in ascending order or not
   */
  public boolean isAscending() {
    return ascending;
  }
  
  /**
   * @param sources the sources for which this rule applies
   */
  public void setSources(List<String> sources) {
    this.sources = sources;
  }
  
  /**
   * @return the sources for which this rule applies.
   */
  public List<String> getSources() {
    return this.sources;
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

  public String toStrFromDoubleList(List<Double> doubles) {
    StringBuffer b = new StringBuffer();
    for (Double d : doubles) {
      b.append(d.toString());
      b.append(",");
    }
    String s = b.toString();
    if (s.length() > 0) {
      s = s.substring(0, s.length() - 1);
    }
    return s;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public synchronized IBltMessage handle(IBltMessage message) {
    IBltMessage result = null;
    VolumeTimerData data = createTimerData(message);
    
    if (data != null && !isHandled(data)) {
      // if data != null, message is an instance of BltDataMessage - therefore safe to cast
      FileEntry file = ((BltDataMessage)message).getFileEntry();
      logger.info("ENTER: execute VolumeRule with ruleId: " + getRuleId() + ", thread: " + Thread.currentThread().getName() + 
          ", file: " + file.getUuid());
      List<CatalogEntry> entries = fetchAllCurrentEntries(data.getDateTime(), data.getSource());
      TimeoutTask tt = timeoutManager.getRegisteredTask(data);
      
      boolean adaptiveCriteriasMet = false;
      if (isAdaptiveElevationAngles() && areAdaptiveElevationAnglesMatching(entries, data.getDateTime(), data.getSource())) {
        adaptiveCriteriasMet = true;
      }
      boolean criteriasMet = areCriteriasMet(entries, data.getDateTime(), data.getSource());
      
      if (adaptiveCriteriasMet || criteriasMet) {
        List<CatalogEntry> newentries = filterEntries(entries, data.getDateTime().getTime());
        result = createMessage(data.getDateTime(), newentries);
        if (tt != null) {
          timeoutManager.unregister(tt.getId());
        }
        
        if (isAdaptiveElevationAngles()) {
          // We update handled with a the previous periods elevation angles so that we know what to expect next
          // period. The reason for using previous interval is that it is the only way we can recover from
          // lost scans since we otherwise always assume that we expect the current number of scans to be the
          // correct.
          //
          // This is not true if timeout occurs though, then it's all entries that exists at timeout
          // that should be used instead.
          //
          // The major flaw with this approach is that if user has specified a too long timeout
          // the timeout will result in an list of elevation angles that are not possible to complete.
          //
          if (!criteriasMet) {
            DateTime prevDateTime = ruleUtilities.createPrevNominalTime(data.getDateTime(), interval);
            List<CatalogEntry> prevEntries = fetchAllCurrentEntries(prevDateTime, data.getSource());
            List<CatalogEntry> filteredPrevEntries = filterEntries(prevEntries, prevDateTime.getTime());
            if (nominalTimeout) {
              // If we have nominal timeout we use prevDateTime + timeout on stored times as filter
              TimeDelta td = new TimeDelta().addSeconds(timeout);
              DateTime dtLimit = prevDateTime.add(td);
              filteredPrevEntries = ruleUtilities.removeEntriesWithStorageTimeOlderThan(filteredPrevEntries, dtLimit);
            } else {
              // Otherwise we use first entries storage time + timeout as filter 
              CatalogEntry firstEntry = ruleUtilities.findFirstStoredEntry(filteredPrevEntries);
              DateTime dt = ruleUtilities.createStorageDateTime(firstEntry);
              TimeDelta td = new TimeDelta().addSeconds(timeout);
              DateTime dtLimit = dt.add(td);
              filteredPrevEntries = ruleUtilities.removeEntriesWithStorageTimeOlderThan(filteredPrevEntries, dtLimit);
            }
            List<Double> newElangles = ruleUtilities.getElanglesFromEntries(filteredPrevEntries);
            logger.info("Adaptive elevation angles for '" + data.getSource() + "' set to " + toStrFromDoubleList(newElangles));
            data.setAdaptiveElevationAngles(newElangles);
          } else {
            logger.info("Adaptive elevation angles reset for '" + data.getSource() + "'");
          }
        }
      } else {
        if (tt == null) {
          if (timeout > 0) {
            timeoutManager.register(this, ruleUtilities.getTimeoutTime(data.getDateTime(), nominalTimeout, timeout*1000), data);
          }
        }
      }
      logger.info("EXIT: execute VolumeRule with ruleId: " + getRuleId() + ", thread: " + Thread.currentThread().getName() + 
          ", file: " + file.getUuid());
    }
    
    if (result != null) {
      setHandled(data);
    }
    
    return result;
  }

  /**
   * If entries contains a scan with same elevation angle as provided entry, then the
   * entry in entries will be replaced with provided one if it is closer to the nominal time.
   * 
   * @param entries a list of entries
   * @param entry the entry that eventually should replace an existing one
   * @param nominalTime the nominal time
   * @return if any entry is found with same elevation angle as provided one.
   */
  protected boolean replaceScanElevation(List<CatalogEntry> entries, CatalogEntry entry, Time nominalTime) {
    boolean duplicate = false;
    int sz = entries.size();
    try {
      Time t = Time.fromIsoString((String)entry.getAttribute("/dataset1/what/starttime"));
      long diffTime = Math.abs(nominalTime.getCumulativeMsecs() - t.getCumulativeMsecs());
      Double elangle = (Double)entry.getAttribute("/dataset1/where/elangle");
      for (int i = 0; i < sz; i++) {
        Double relangle = (Double)entries.get(i).getAttribute("/dataset1/where/elangle");
        if (relangle.equals(elangle)) {
          duplicate = true;
          Time rt = Time.fromIsoString((String)entries.get(i).getAttribute("/dataset1/what/starttime"));
          long rsDiffTime = Math.abs(nominalTime.getCumulativeMsecs() - rt.getCumulativeMsecs());
          if (diffTime < rsDiffTime) {
            entries.remove(i);
            entries.add(i, entry);
          }
          break;
        }
      }
    } catch (RuntimeException t) {
      t.printStackTrace();
    }
    
    return duplicate;
  }
  
  /**
   * Removes all unwanted catalog entries.
   * @param entries the entries to be filtered
   * @param nominalTime the expected nominal time
   * @return a list of filtered entries
   */
  protected List<CatalogEntry> filterEntries(List<CatalogEntry> entries, Time nominalTime) {
    List<CatalogEntry> result = createCatalogEntryList();

    for (CatalogEntry entry : entries) {
      Double elangle = (Double)entry.getAttribute("/dataset1/where/elangle");
      if (elangle >= eMin && elangle <= eMax) {
        if (!replaceScanElevation(result, entry, nominalTime)) {
          result.add(entry);
        }
      }
    }
    
    return result;
  }
  
  /**
   * Creates an array list for catalog entries
   * @return a list for catalog entries
   */
  protected List<CatalogEntry> createCatalogEntryList() {
    return new ArrayList<CatalogEntry>();
  }

  /**
   * @see eu.baltrad.beast.rules.timer.ITimeoutRule#timeout(long, int, java.lang.Object)
   */
  @Override
  public synchronized IBltMessage timeout(long id, int why, Object data) {
    VolumeTimerData vtd = (VolumeTimerData)data;
    if (vtd != null) {
      List<CatalogEntry> entries = fetchAllCurrentEntries(vtd.getDateTime(), vtd.getSource());
      List<CatalogEntry> newentries = filterEntries(entries, vtd.getDateTime().getTime());
      IBltMessage msgtosend = createMessage(vtd.getDateTime(), newentries);
      BltMultiRoutedMessage mrmsg = new BltMultiRoutedMessage();
      mrmsg.setDestinations(recipients);
      mrmsg.setMessage(msgtosend);
      
      if (isAdaptiveElevationAngles()) {
        // When timeout is triggered we assume that the current data is what we want to wait for.
        List<Double> newElangles = ruleUtilities.getElanglesFromEntries(newentries);        
        logger.info("Timeout caused Adaptive elevation angles to be set for '" + vtd.getSource() + "' angles are: " + toStrFromDoubleList(newElangles));
        vtd.setAdaptiveElevationAngles(ruleUtilities.getElanglesFromEntries(newentries));
      }
      
      setHandled(vtd);
      return mrmsg;
    }
    return null;
  }

  /**
   * Checks if the adaptive elevation angles are matching or not
   * @param entries the entries with current elevation angles
   * @param dt the nominal time (Currently Not Used)
   * @param source the radar source
   * @return true if we have a matching situation, otherwise false
   */
  protected boolean areAdaptiveElevationAnglesMatching(List<CatalogEntry> entries, DateTime dt, String source) {
    if (handledData.containsKey(source)) {
      List<Double> adaptiveAngles = handledData.get(source).getAdaptiveElevationAngles();
      List<Double> entriesAngles = ruleUtilities.getElanglesFromEntries(entries);
      
      if (adaptiveAngles.size() > 0) {
        for (Double d: adaptiveAngles) {
          if (!entriesAngles.contains(d)) {
            return false;
          }
        }
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * Verifies if the criterias has been met so that we can create the message.
   * @param entries a list of catalog entries
   * @return true if the criterias has been met.
   */
  protected boolean areCriteriasMet(List<CatalogEntry> entries, DateTime dt, String source) {
    if (this.elevationAngles.size() == 0) {
      for (CatalogEntry ce : entries) {
        Double elangle = (Double)ce.getAttribute("/dataset1/where/elangle");
        if ((ascending == true && elangle >= eMax) ||
            (ascending == false && elangle <= eMin)) {
          return true;
        }
      }
    } else {
      ArrayList<Double> currentAngles = new ArrayList<Double>(); 
      for (CatalogEntry ce : entries) {
        Double elangle = (Double)ce.getAttribute("/dataset1/where/elangle");
        currentAngles.add(elangle);
      }
      for (Double d : elevationAngles) {
        if (!currentAngles.contains(d)) {
          return false;
        }
      }
      return true;
    }
    
    return false; 
  }
  
  /**
   * If possible creates a CompositingTimerData.
   * @param message the message (that should be a BltDataMessage)
   * @return a volume timer data or null if not possible
   */
  protected VolumeTimerData createTimerData(IBltMessage message) {
    VolumeTimerData result = null;
    if (message instanceof BltDataMessage) {
      FileEntry file = ((BltDataMessage)message).getFileEntry();
      if (file.getMetadata().getWhatObject().equals("SCAN")) {
        Time t = file.getMetadata().getWhatTime();
        Date d = file.getMetadata().getWhatDate();
        String s = file.getSource().getName();
        DateTime nominalTime = ruleUtilities.createNominalTime(d, t, interval);
        if (sources.size() == 0 || sources.contains(s)) {
          if (filter == null || matcher.match(file.getMetadata(), filter.getExpression())) {
            result = new VolumeTimerData(ruleid, nominalTime, s);
          }
        }
      }
    }
    
    return result;
  }

  /**
   * Creates a message if all nessecary entries are there
   * @param nominalTime the nominal time
   * @param entries the list of entries
   * @return a message if criterias are fullfilled, otherwise null
   */
  protected IBltMessage createMessage(DateTime nominalTime, List<CatalogEntry> entries) {
    BltGenerateMessage result = new BltGenerateMessage();
    Date date = nominalTime.getDate();
    Time time = nominalTime.getTime();

    if (entries == null || entries.size() == 0) {
      return null;
    }
    
    String source = entries.get(0).getSource();
    
    result.setAlgorithm("eu.baltrad.beast.GenerateVolume");
    
    List<String> uuids = ruleUtilities.getUuidStringsFromEntries(entries);
    result.setFiles(uuids.toArray(new String[0]));

    List<String> args = new ArrayList<String>();
    args.add("--source="+source);
    args.add("--date="+RuleUtils.getFormattedDate(date));
    args.add("--time="+RuleUtils.getFormattedTime(time));

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
    args.add("--algorithm_id="+getRuleId()+"-"+source);
    args.add("--merge=true");

    result.setArguments(args.toArray(new String[0]));
    
    logger.debug("VolumeRule createMessage - entries: " +
        StringUtils.join(uuids, " "));
    
    return result;
  }
  
  /**
   * Returns the nominal time for the specified time, i.e.
   * the start time period
   * @param d the date
   * @param t the time
   * @return a nominal time
   */
  protected DateTime getNominalTime(Date d, Time t) {
    int period = t.minute() / interval;
    return new DateTime(d, new Time(t.hour(), period*interval, 0));
  }
  
  /**
   * Fetches the entries that are from the nominal time up until 
   * the stop time defined by the interval.
   * @param nominalTime the nominal time
   * @return a list of entries
   */
  protected List<CatalogEntry> fetchAllCurrentEntries(DateTime nominalTime, String source) {
    TimeIntervalFilter filter = createFilter(nominalTime, source);
    List<CatalogEntry> entries = catalog.fetch(filter);
    if (this.filter != null) {
      List<CatalogEntry> nentries = new ArrayList<CatalogEntry>(); 
      for (CatalogEntry ce : entries) {
        if (matcher.match(ce.getFileEntry().getMetadata(), this.filter.getExpression())) {
          nentries.add(ce);
        }
      }
      entries = nentries;
    }
    return entries;
  }
  
  /**
   * Returns a filter used for inquirying what scans exists for the
   * current time.
   * @param nominalTime the nominal time
   * @param source the source node id
   * @return the filter
   */
  protected TimeIntervalFilter createFilter(DateTime nominalTime, String source) {
    DateTime nextNt = ruleUtilities.createNextNominalTime(nominalTime, interval);
    TimeIntervalFilter filter = new TimeIntervalFilter();
    filter.setObject("SCAN");
    filter.setSource(source);
    filter.setStartDateTime(nominalTime);
    filter.setStopDateTime(nextNt);
    return filter;
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleRecipientAware#setRecipients(java.util.List)
   */
  @Override
  public void setRecipients(List<String> recipients) {
    this.recipients = recipients;
  }
  
  /**
   * Returns if the given data already has been processed or not.
   * 
   * @param data the data to check
   * @return true if it already has been processed
   */
  synchronized boolean isHandled(VolumeTimerData data) {
    VolumeTimerData handled = handledData.get(data.getSource());
    if (handled != null) {
      return data.equals(handled);
    }
    return false;
  }
  
  /**
   * Sets the data to have been handled. Will replace
   * previous handled job for the provided source.
   * @param data
   */
  synchronized void setHandled(VolumeTimerData data) {
    handledData.put(data.getSource(), data);
  }

  /**
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() {
    if (catalog == null ||
        timeoutManager == null ||
        ruleUtilities == null) {
      throw new BeanInitializationException("catalog, timeoutManager or ruleUtilities missing");
    }
  }

  public String getElevationAngles() {
    if (elevationAngles.size() > 0) {
      return StringUtils.join(elevationAngles.toArray(), ",");
    }
    return null;
  }

  public void setElevationAngles(String elevationAngles) throws IllegalArgumentException {
    ArrayList<Double> newValues = new ArrayList<Double>();
    if (elevationAngles != null && elevationAngles.trim().length() > 0) {
      String[] tokens = elevationAngles.split("\\s*,\\s*");
      for (String t : tokens) {
        try {
          newValues.add(Double.parseDouble(t));
        } catch (Exception e) {
          throw new IllegalArgumentException(e);
        }
      }
      for (Double d : newValues) {
        this.elevationAngles.add(d);
      }
    } else {
      this.elevationAngles.clear();
    }
  }
  
  public void setElevationAngles(List<Double> elevationAngles) throws IllegalArgumentException {
    this.elevationAngles.clear();
    this.elevationAngles.addAll(elevationAngles);
  }
  
  public List<Double> getElevationAnglesAsDoubles() {
    return elevationAngles;
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

  public boolean isNominalTimeout() {
    return nominalTimeout;
  }

  public void setNominalTimeout(boolean nominalTimeout) {
    this.nominalTimeout = nominalTimeout;
  }


  /**
   * @return the adaptiveElevationAngles
   */
  public boolean isAdaptiveElevationAngles() {
    return adaptiveElevationAngles;
  }

  /**
   * @param adaptiveElevationAngles the adaptiveElevationAngles to set
   */
  public void setAdaptiveElevationAngles(boolean adaptiveElevationAngles) {
    this.adaptiveElevationAngles = adaptiveElevationAngles;
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
  
  public void setQualityControlMode(int qualityControlMode) {
    this.qualityControlMode = qualityControlMode;
  }

  public void setQualityControlMode(String qualityControlMode) {
    if (qualityControlMode.equals("ANALYZE")) {
      this.qualityControlMode = QualityControlMode_ANALYZE;
    } else if (qualityControlMode.equals("ANALYZE_AND_APPLY")) {
      this.qualityControlMode = QualityControlMode_ANALYZE_AND_APPLY;
    } else {
      throw new IllegalArgumentException("Unknown quality control mode string " + qualityControlMode);
    }
  }
}
