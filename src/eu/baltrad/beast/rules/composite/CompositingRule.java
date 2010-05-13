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
import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.DateTime;
import eu.baltrad.beast.db.filters.TimeIntervalFilter;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.timer.ITimeoutRule;
import eu.baltrad.fc.Date;
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
  private Catalog catalog = null;

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
   * The are that this composite should cover
   */
  private String area = null;
  
  /**
   * @param catalog the catalog to set
   */
  public void setCatalog(Catalog catalog) {
    this.catalog = catalog;
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
    return "blt_composite";
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    if (message instanceof BltDataMessage) {
      File file = ((BltDataMessage)message).getFile();
      if (file.what_object().equals("PVOL")) {
        Time t = file.what_time();
        Date d = file.what_date();
        DateTime nominalTime = getNominalTime(d, t);
        TimeIntervalFilter filter = createFilter(nominalTime);
        List<CatalogEntry> entries = catalog.fetch(filter);
        if (areCriteriasMet(entries)) {
          return createMessage(nominalTime, entries);
        }
      }
    }
    return null;
  }

  /**
   * @see eu.baltrad.beast.rules.timer.ITimeoutRule#timeout(long, int, Object)
   */
  @Override
  public IBltMessage timeout(long id, int why, Object data) {
    return null;
  }
  
  /**
   * Verifies if the criterias has been met so that we can create
   * the message.
   * @param entries a list of catalog entries
   * @return true if the criterias has been met.
   */
  protected boolean areCriteriasMet(List<CatalogEntry> entries) {
    List<String> es = getSourcesFromEntries(entries);
    for (String s : sources) {
      if (!es.contains(s)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Creates a list of sources from the entries
   * @param entries a list of entries
   * @return a list of sources
   */
  protected List<String> getSourcesFromEntries(List<CatalogEntry> entries) {
    List<String> result = new ArrayList<String>();
    for (CatalogEntry entry : entries) {
      result.add(entry.getSource());
    }
    return result;
  }
  
  /**
   * Creates a list of files from the entries. If the list contains more than
   * one entry from the same source, the one nearest in time to the nominal time 
   * will be used.
   * @param entries a list of entries
   * @return a list of files
   */
  protected List<String> getFilesFromEntries(DateTime nominalTime, List<CatalogEntry> entries) {
    Map<String, CatalogEntry> entryMap = new HashMap<String, CatalogEntry>();
    GregorianCalendar nominalTimeCalendar = createCalendar(nominalTime.getDate(), nominalTime.getTime());
    List<String> result = new ArrayList<String>();
    
    for (CatalogEntry entry: entries) {
      String src = entry.getSource();
      if (sources.contains(src)) {
        if (!entryMap.containsKey(src)) {
          entryMap.put(src, entry);
        } else {
          GregorianCalendar entryCalendar = createCalendar(entry.getDate(), entry.getTime());
          CatalogEntry mapEntry = entryMap.get(src);
          GregorianCalendar mapEntryCalendar = createCalendar(mapEntry.getDate(), mapEntry.getTime());
        
          // If the entrys time is closer to the nominal time than the existing one, replace it
          if (Math.abs(entryCalendar.compareTo(nominalTimeCalendar)) < Math.abs(mapEntryCalendar.compareTo(nominalTimeCalendar))) {
            entryMap.put(src, entry);
          }
        }
      }
    }
    
    for (CatalogEntry entry : entryMap.values()) {
      result.add(entry.getPath());
    }

    return result;
  }
  
  /**
   * Creates a gregorian calendar with the specified date/time
   * @param date the date
   * @param time the time
   * @return a gregorian calendar
   */
  protected GregorianCalendar createCalendar(Date date, Time time) {
    GregorianCalendar c = new GregorianCalendar();
    c.set(date.year(), date.month()-1, date.day(), time.hour(), time.minute(), time.second());
    return c;
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
    
    result.setAlgorithm("eu.baltrad.beast.GenerateComposite");

    result.setFiles(getFilesFromEntries(nominalTime, entries).toArray(new String[0]));

    String[] args = new String[3];
    args[0] = "--area="+area;
    args[1] = "--date="+new Formatter().format("%d%02d%02d",date.year(), date.month(), date.day()).toString(); 
    args[2] = "--time="+new Formatter().format("%02d%02d%02d",time.hour(), time.minute(), time.second()).toString();
    
    result.setArguments(args);
    
    return result;
  }
  
  /**
   * Returns a filter
   * @param date the date
   * @param time the time
   * @returns a TimeIntervalFilter for polar volumes  
   */
  protected TimeIntervalFilter createFilter(DateTime nominalTime) {
    TimeIntervalFilter filter = new TimeIntervalFilter();
    filter.setObject("PVOL");
    DateTime stopDT = getStop(nominalTime);
    filter.setStartDateTime(nominalTime.getDate(), nominalTime.getTime());
    filter.setStopDateTime(stopDT.getDate(), stopDT.getTime());
    return filter;
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
}
