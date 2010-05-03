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
import java.util.List;

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
   * @param sources the list of sources to set
   */
  public void setSources(List<String> sources) {
    this.sources = sources;
  }
  
  /**
   * @param area the area to set
   */
  public void setArea(String area) {
    this.area = area;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRule#getDefinition()
   */
  @Override
  public String getDefinition() {
    return "Compositing rule";
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#getType()
   */
  @Override
  public String getType() {
    return "static";
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
        TimeIntervalFilter filter = createFilter(d, t);
        List<CatalogEntry> entries = catalog.fetch(filter);
        if (areCriteriasMet(entries)) {
          return createMessage(d, t, entries);
        }
      }
    }
    return null;
  }

  /**
   * @see eu.baltrad.beast.rules.timer.ITimeoutRule#timeout(long, int)
   */
  @Override
  public IBltMessage timeout(long id, int why) {
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
   * Creates a list of files from the entries
   * @param entries a list of entries
   * @return a list of files
   */
  protected List<String> getFilesFromEntries(List<CatalogEntry> entries) {
    List<String> result = new ArrayList<String>();
    for (CatalogEntry entry : entries) {
      result.add(entry.getPath());
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
  protected IBltMessage createMessage(Date date, Time time, List<CatalogEntry> entries) {
    BltGenerateMessage result = new BltGenerateMessage();
    result.setAlgorithm("eu.baltrad.beast.GenerateComposite");

    result.setFiles(getFilesFromEntries(entries).toArray(new String[0]));

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
  protected TimeIntervalFilter createFilter(Date date, Time time) {
    TimeIntervalFilter filter = new TimeIntervalFilter();
    DateTime dt = new DateTime(date, time);
    filter.setObject("PVOL");
    DateTime startDT = getStart(dt);
    DateTime stopDT = getStop(dt);
    filter.setStartDateTime(startDT.getDate(), startDT.getTime());
    filter.setStopDateTime(stopDT.getDate(), stopDT.getTime());
    return filter;
  }
  
  /**
   * Returns the start date/time
   * @param dt the date/time to determine start dt from
   * @return the start date/time
   */
  protected DateTime getStart(DateTime dt) {
    DateTime result = new DateTime();
    Time t = dt.getTime();
    int period = t.minute() / interval;
    result.setTime(new Time(t.hour(), period*interval, 0));
    result.setDate(dt.getDate());
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
