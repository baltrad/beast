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
import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.List;

import eu.baltrad.beast.ManagerContext;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.filters.PolarScanAngleFilter;
import eu.baltrad.beast.db.filters.TimeIntervalFilter;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.message.mo.BltMultiRoutedMessage;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.timer.ITimeoutRule;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.timer.TimeoutTask;
import eu.baltrad.fc.Date;
import eu.baltrad.fc.DateTime;
import eu.baltrad.fc.Time;
import eu.baltrad.fc.oh5.File;

/**
 * @author Anders Henja
 *
 */
public class VolumeRule implements IRule, ITimeoutRule {
  /**
   * The name of this static composite type
   */
  public final static String TYPE = "blt_volume";
  
  /**
   * The catalog for database access
   */
  private Catalog catalog = null;

  /**
   * The timeout manager
   */
  private TimeoutManager timeoutManager = null;
  
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
   * The sources for which this rule applies
   */
  private List<String> sources = new ArrayList<String>();
  
  /**
   * The recipients that are affected by this rule. Used
   * for generating timeout message
   */
  private List<String> recipients = new ArrayList<String>();

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
   * @param e the min elevation angle to set in degrees
   */
  public void setElevationMin(double e) {
    this.eMin = e;
  }
  
  /**
   * returns the min elevation angle in degrees
   * @return
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
   * @return
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
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    IBltMessage result = null;
    initialize();
    VolumeTimerData data = createTimerData(message);
    
    if (data != null) {
      List<CatalogEntry> entries = fetchAllCurrentEntries(data.getDateTime(), data.getSource());
      TimeoutTask tt = timeoutManager.getRegisteredTask(data);
      
      if (areCriteriasMet(entries, data.getDateTime(), data.getSource())) {
        removeEntriesOutsideElevationRange(entries);
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
   * @param entries
   */
  protected void removeEntriesOutsideElevationRange(List<CatalogEntry> entries) {
    int index = entries.size() - 1;
    while (index >= 0) {
      Double elangle = (Double)entries.get(index).getAttribute("where/elangle");
      if (elangle < eMin || elangle > eMax) {
        entries.remove(index);
      }
      index --;
    }
  }

  /**
   * @see eu.baltrad.beast.rules.timer.ITimeoutRule#timeout(long, int, java.lang.Object)
   */
  @Override
  public IBltMessage timeout(long id, int why, Object data) {
    initialize();
    VolumeTimerData vtd = (VolumeTimerData)data;
    if (vtd != null) {
      List<CatalogEntry> entries = fetchAllCurrentEntries(vtd.getDateTime(), vtd.getSource());
      IBltMessage msgtosend = createMessage(vtd.getDateTime(), entries);
      BltMultiRoutedMessage mrmsg = new BltMultiRoutedMessage();
      mrmsg.setDestinations(recipients);
      mrmsg.setMessage(msgtosend);
      return mrmsg;
    }
    return null;
  }

  /**
   * Verifies if the criterias has been met so that we can create
   * the message.
   * @param entries a list of catalog entries
   * @return true if the criterias has been met.
   */
  protected boolean areCriteriasMet(List<CatalogEntry> entries, DateTime dt, String source) {
    DateTime previousDateTime = getPreviousDateTime(dt, source);
    List<Double> elevations = getPreviousElevationAngles(previousDateTime, source);
    
    for (CatalogEntry ce : entries) {
      Double elangle = (Double)ce.getAttribute("where/elangle");
      if (previousDateTime == null && 
          ((ascending == true && elangle >= eMax) ||
           (ascending == false && elangle <= eMin))) {
        return true;
      }
      elevations.remove(elangle);
    }
    
    return ((previousDateTime!=null)&&(elevations.size() == 0)); 
  }
  
  /**
   * If possible creates a CompositingTimerData.
   * @param message the message (that should be a BltDataMessage)
   * @return a volume timer data or null if not possible
   */
  protected VolumeTimerData createTimerData(IBltMessage message) {
    VolumeTimerData result = null;
    if (message instanceof BltDataMessage) {
      File file = ((BltDataMessage)message).getFile();
      if (file.what_object().equals("SCAN")) {
        Time t = file.what_time();
        Date d = file.what_date();
        String s = file.source().node_id();
        double rscale = file.group("/dataset1/where").attribute("rscale").value().double_();
        long nbins = file.group("/dataset1/where").attribute("nbins").value().int64_();
        
        DateTime nominalTime = getNominalTime(d, t);
        if (sources.size() == 0 || sources.contains(s)) {
          result = new VolumeTimerData(ruleid, nominalTime, s, nbins, rscale);
        }
      }
    }
    
    return result;
  }

  /**
   * Creates a message if all nessecary entries are there
   * @param date the date
   * @param time the time
   * @param entries the list of entries
   * @return a message if criterias are fullfilled, otherwise null
   */
  protected IBltMessage createMessage(DateTime nominalTime, List<CatalogEntry> entries) {
    BltGenerateMessage result = new BltGenerateMessage();
    Date date = nominalTime.date();
    Time time = nominalTime.time();

    if (entries == null || entries.size() == 0) {
      return null;
    }
    
    String source = entries.get(0).getSource();
    
    result.setAlgorithm("eu.baltrad.beast.GenerateVolume");

    result.setFiles(getFilesFromEntries(entries));

    String[] args = new String[3];
    args[0] = "--source="+source;
    args[1] = "--date="+new Formatter().format("%d%02d%02d",date.year(), date.month(), date.day()).toString(); 
    args[2] = "--time="+new Formatter().format("%02d%02d%02d",time.hour(), time.minute(), time.second()).toString();
    
    result.setArguments(args);
    
    return result;
  }
  
  /**
   * Returns the list of scans that should be used for generating
   * the volume
   * @param entries the entries
   * @return a list of paths
   */
  protected String[] getFilesFromEntries(List<CatalogEntry> entries) {
    int i = 0;
    String[] result = new String[entries.size()];
    for (CatalogEntry e: entries) {
      result[i++] = e.getPath();
    }
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
    PolarScanAngleFilter filter = createAngleFilter(nominalTime, source);
    List<CatalogEntry> entries = catalog.fetch(filter);
    return entries;
  }

  /**
   * Returns the previous datetime for the scans
   * @param now the current time
   * @param source the source node id
   * @return the previous time or null if none found
   */
  protected DateTime getPreviousDateTime(DateTime now, String source) {
    TimeIntervalFilter filter = createPreviousTimeFilter(now, source);
    List<CatalogEntry> entries = catalog.fetch(filter);
    DateTime result = null;
    if (entries.size() > 0) {
      result = entries.get(0).getDateTime();
    }
    return result;
  }
  
  /**
   * Returns the elevation angles for the previous time period
   * @param nominalTime the time we are currently working with
   * @param source the source
   * @return a list of elevation angles in degrees or a zero length list
   */
  protected List<Double> getPreviousElevationAngles(DateTime time, String source) {
    List<Double> result = new ArrayList<Double>();
    if (time != null) {
      PolarScanAngleFilter psafilter = new PolarScanAngleFilter();
      psafilter.setDateTime(time);
      psafilter.setSource(source);
      psafilter.setMinElevation(eMin);
      psafilter.setMaxElevation(eMax);
      psafilter.setSortOrder(PolarScanAngleFilter.ASCENDING);
      List<CatalogEntry> entries = catalog.fetch(psafilter);
      for (CatalogEntry ce : entries) {
        result.add((Double)ce.getAttribute("where/elangle"));
      }
    }
    
    return result;    
  }
  
  /**
   * Returns a filter used for inquirying what scans exists for the
   * current time.
   * @param nominalTime the nominal time
   * @param source the source node id
   * @return the filter
   */
  protected PolarScanAngleFilter createAngleFilter(DateTime nominalTime, String source) {
    PolarScanAngleFilter filter = new PolarScanAngleFilter();
    filter.setDateTime(nominalTime);
    filter.setSource(source);
    filter.setSortOrder(PolarScanAngleFilter.ASCENDING);
    return filter;
  }
  
  /**
   * Returns a filter that should be used to fetch the previous date time period
   * @param date the date
   * @param time the time
   * @returns a TimeIntervalFilter for scans  
   */
  protected TimeIntervalFilter createPreviousTimeFilter(DateTime nominalTime, String source) {
    TimeIntervalFilter filter = new TimeIntervalFilter();
    filter.setStopDateTime(nominalTime);
    filter.setSource(source);
    filter.setObject("SCAN");
    filter.setLimit(1);
    return filter;
  }
  
  /**
   * Returns the stop time
   * @param dt the date/time to determine stop dt from
   * @return the stop date/time
   */
  protected DateTime getStop(DateTime dt) {
    Date d = dt.date();
    Time t = dt.time();
    GregorianCalendar cal = new GregorianCalendar();
    cal.set(d.year(), d.month() - 1, d.day(), t.hour(), t.minute(), t.second());
    int period = t.minute() / interval;
    int minute = (period + 1) * interval;
    cal.set(Calendar.MINUTE, minute);
    Date date = new Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    Time time = new Time(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
    DateTime result = new DateTime(date, time);
    return result;
  }
  
  /**
   * Initializes the nessecary components like catalog and
   * timeout manager.
   * @throws RuntimeException if the nessecary components not could be aquired
   */
  protected synchronized void initialize() {
    if (catalog == null) {
      catalog = ManagerContext.getCatalog();
    }
    if (timeoutManager == null) {
      timeoutManager = ManagerContext.getTimeoutManager();
    }
    if (catalog == null || timeoutManager == null) {
      throw new RuntimeException();
    }
  }

  /**
   * @see eu.baltrad.beast.rules.IRuleRecipientAware#setRecipients(java.util.List)
   */
  @Override
  public void setRecipients(List<String> recipients) {
    this.recipients = recipients;
  }
}
