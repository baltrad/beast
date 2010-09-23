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

import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.List;

import eu.baltrad.beast.ManagerContext;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.DateTime;
import eu.baltrad.beast.db.filters.TimeIntervalFilter;
import eu.baltrad.beast.db.filters.VolumeScanFilter;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.timer.ITimeoutRule;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.timer.TimeoutTask;
import eu.baltrad.fc.Date;
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
   * Should only be called by this package.
   * @param ruleid the ruleid to set
   */
  void setRuleid(int ruleid) {
    this.ruleid = ruleid;
  }

  /**
   * @return the ruleid
   */
  public int getRuleid() {
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
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    IBltMessage result = null;
    initialize();
    VolumeTimerData data = createTimerData(message);
    if (data != null) {
      List<CatalogEntry> entries = fetchEntries(data.getDateTime(), data.getSource());
      TimeoutTask tt = timeoutManager.getRegisteredTask(data);
      if (areCriteriasMet(entries)) {
        result = createMessage(data.getDateTime(), entries);
        if (tt != null) {
          timeoutManager.unregister(tt.getId());
        }
      }
      //probably useful when implementing the elev-check
      //double elangle = file.group("/dataset1/where").attribute("elangle").value().double_();
      /**
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
      */
    }
    return result;
  }

  /**
   * @see eu.baltrad.beast.rules.timer.ITimeoutRule#timeout(long, int, java.lang.Object)
   */
  @Override
  public IBltMessage timeout(long id, int why, Object data) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Verifies if the criterias has been met so that we can create
   * the message.
   * @param entries a list of catalog entries
   * @return true if the criterias has been met.
   */
  protected boolean areCriteriasMet(List<CatalogEntry> entries) {
    /*List<String> es = getSourcesFromEntries(entries);
    for (String s : sources) {
      if (!es.contains(s)) {
        return false;
      }
    }*/
    return true;
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
        String s = file.what_source();
        double rscale = file.group("/dataset1/where").attribute("rscale").value().double_();
        long nbins = file.group("/dataset1/where").attribute("nbins").value().int64_();
        
        DateTime nominalTime = getNominalTime(d, t);
        result = new VolumeTimerData(ruleid, nominalTime, s, nbins, rscale);
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
    Date date = nominalTime.getDate();
    Time time = nominalTime.getTime();
    
    String source = entries.get(0).getSource();
    
    result.setAlgorithm("eu.baltrad.beast.GenerateVolume");

    //result.setFiles(getFilesFromEntries(nominalTime, entries).toArray(new String[0]));

    String[] args = new String[3];
    args[0] = "--source="+source;
    args[1] = "--date="+new Formatter().format("%d%02d%02d",date.year(), date.month(), date.day()).toString(); 
    args[2] = "--time="+new Formatter().format("%02d%02d%02d",time.hour(), time.minute(), time.second()).toString();
    
    result.setArguments(args);
    
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
    DateTime result = new DateTime();
    int period = t.minute() / interval;
    result.setTime(new Time(t.hour(), period*interval, 0));
    result.setDate(d);
    return result;
  }
  
  /**
   * Fetches the entries that are from the nominal time up until 
   * the stop time defined by the interval.
   * @param nominalTime the nominal time
   * @return a list of entries
   */
  protected List<CatalogEntry> fetchEntries(DateTime nominalTime, String source) {
    VolumeScanFilter filter = createFilter(nominalTime, source);
    List<CatalogEntry> entries = catalog.fetch(filter);
    return entries;
  }
  
  /**
   * Returns a filter
   * @param date the date
   * @param time the time
   * @returns a TimeIntervalFilter for polar volumes  
   */
  protected VolumeScanFilter createFilter(DateTime nominalTime, String source) {
    VolumeScanFilter filter = new VolumeScanFilter();
    filter.setSource(source);
    DateTime stopDT = getStop(nominalTime);
    filter.setStartDateTime(nominalTime.getDate(), nominalTime.getTime());
    filter.setStopDateTime(stopDT.getDate(), stopDT.getTime());
    return filter;
  }
  
  /**
   * Returns the stop time
   * @param dt the date/time to determine stop dt from
   * @return the stop date/time
   */
  protected DateTime getStop(DateTime dt) {
    Date d = dt.getDate();
    Time t = dt.getTime();
    GregorianCalendar cal = new GregorianCalendar();
    cal.set(d.year(), d.month() - 1, d.day(), t.hour(), t.minute(), t.second());
    int period = t.minute() / interval;
    int minute = (period + 1) * interval;
    cal.set(Calendar.MINUTE, minute);
    DateTime result = new DateTime();
    result.setDate(new Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));
    result.setTime(new Time(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND)));
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
}
