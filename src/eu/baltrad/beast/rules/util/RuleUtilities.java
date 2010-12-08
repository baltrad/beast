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
package eu.baltrad.beast.rules.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.filters.LowestAngleFilter;
import eu.baltrad.fc.Date;
import eu.baltrad.fc.DateTime;
import eu.baltrad.fc.Time;
import eu.baltrad.fc.oh5.Source;

/**
 * @author Anders Henja
 */
public class RuleUtilities implements IRuleUtilities {
  /**
   * The catalog
   */
  private Catalog catalog = null;

  private static class IdDateMapping {
    private int ruleid = 0;
    private DateTime dt = null;
    public IdDateMapping(int ruleid, DateTime dt) {
      if (dt == null) {
        throw new NullPointerException();
      }
      this.ruleid = ruleid;
      this.dt = dt;
    }
    public boolean equals(Object o) {
      if (o != null && o.getClass() == IdDateMapping.class) {
        IdDateMapping o2 = (IdDateMapping)o;
        if (o2.ruleid == this.ruleid &&
            o2.dt.equals(this.dt)) {
          return true;
        }
      }
      return false;
    }
  };
  
  /**
   * The list of registered triggers.
   */
  private List<IdDateMapping> registeredTriggers = new ArrayList<IdDateMapping>();
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(RuleUtilities.class);

  /**
   * @param catalog the catalog to set
   */
  public void setCatalog(Catalog catalog) {
    this.catalog = catalog;
  }

  /**
   * @return the catalog
   */
  public Catalog getCatalog() {
    return catalog;
  }

  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#fetchLowestSourceElevationAngle(eu.baltrad.fc.DateTime, eu.baltrad.fc.DateTime, java.util.List)
   */
  @Override
  public List<CatalogEntry> fetchLowestSourceElevationAngle(DateTime startDT, DateTime stopDT, List<String> sources) {
    // What I really want to achieve is to fetch each source's lowest angle for the specified
    // datetime but since the db-api is somewhat limited I will have to fetch lowest angle
    // for each source.
    List<CatalogEntry> result = new ArrayList<CatalogEntry>();
    LowestAngleFilter filter = new LowestAngleFilter();
    filter.setStart(startDT);
    filter.setStop(stopDT);

    for (String src: sources) {
      filter.setSource(src);
      List<CatalogEntry> entries = catalog.fetch(filter);
      result.addAll(entries);
    }
    
    return result;
  }

  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#getEntryBySource(java.lang.String, java.util.List)
   */
  @Override
  public CatalogEntry getEntryBySource(String source, List<CatalogEntry> entries) {
    for (CatalogEntry e : entries) {
      String src = e.getSource();
      if (src != null && src.equals(source)) {
        return e;
      }
    }
    return null;
  }

  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#getFilesFromEntries(eu.baltrad.fc.DateTime, java.util.List, java.util.List)
   */
  @Override
  public List<String> getFilesFromEntries(DateTime nominalDT, List<String> sources, List<CatalogEntry> entries) {
    Map<String, CatalogEntry> entryMap = new HashMap<String, CatalogEntry>();
    GregorianCalendar nominalTimeCalendar = createCalendar(nominalDT);
    List<String> result = new ArrayList<String>();
    
    for (CatalogEntry entry: entries) {
      String src = entry.getSource();
      if (sources.contains(src)) {
        if (!entryMap.containsKey(src)) {
          entryMap.put(src, entry);
        } else {
          GregorianCalendar entryCalendar = createCalendar(entry.getDateTime());
          CatalogEntry mapEntry = entryMap.get(src);
          GregorianCalendar mapEntryCalendar = createCalendar(mapEntry.getDateTime());
        
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
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#createCalendar(eu.baltrad.fc.DateTime)
   */
  @Override
  public GregorianCalendar createCalendar(DateTime dt) {
    GregorianCalendar c = new GregorianCalendar();
    Date date = dt.date();
    Time time = dt.time();
    c.set(date.year(), date.month()-1, date.day(), time.hour(), time.minute(), time.second());
    return c;
  }

  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#getSourcesFromEntries(java.util.List)
   */
  @Override
  public List<String> getSourcesFromEntries(List<CatalogEntry> entries) {
    List<String> result = new ArrayList<String>();
    for (CatalogEntry entry : entries) {
      String src = entry.getSource();
      if (!result.contains(src)) {
        result.add(entry.getSource());
      }
    }
    return result;
  }

  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#createNominalTime(eu.baltrad.fc.DateTime, int)
   */
  @Override
  public DateTime createNominalTime(DateTime now, int interval) {
    if (interval == 0 || 60%interval != 0) {
      throw new IllegalArgumentException("Interval must be evenly dividable by 60");
    }
    Time t = now.time();
    Date d = now.date();
    int period = t.minute() / interval;
    return new DateTime(d, new Time(t.hour(), period*interval, 0));
  }

  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#createNominalTime(eu.baltrad.fc.Date, eu.baltrad.fc.Time, int)
   */
  @Override
  public DateTime createNominalTime(Date d, Time t, int interval) {
    return createNominalTime(new DateTime(d, t), interval);
  }

  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#createNextNominalTime(eu.baltrad.fc.DateTime, int)
   */
  @Override
  public DateTime createNextNominalTime(DateTime now, int interval) {
    if (interval == 0 || 60%interval != 0) {
      throw new IllegalArgumentException("Interval must be evenly dividable by 60");
    }
    GregorianCalendar cal = createCalendar(now);
    Time t = now.time();
    int period = t.minute() / interval;
    int minute = (period + 1) * interval;
    cal.set(Calendar.MINUTE, minute);
    Date nd = new Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    Time nt = new Time(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
    return new DateTime(nd, nt);
  }

  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#createPrevNominalTime(eu.baltrad.fc.DateTime, int)
   */
  @Override
  public DateTime createPrevNominalTime(DateTime now, int interval) {
    if (interval == 0 || 60%interval != 0) {
      throw new IllegalArgumentException("Interval must be evenly dividable by 60");
    }
    GregorianCalendar cal = createCalendar(now);
    Time t = now.time();
    int period = t.minute() / interval;
    int minute = (period - 1) * interval;
    cal.set(Calendar.MINUTE, minute);
    Date nd = new Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    Time nt = new Time(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
    return new DateTime(nd, nt);
  }
  
  /**
   * This function will keep a backlog of 100 entries.
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#trigger(int, eu.baltrad.fc.DateTime)
   */
  @Override
  public synchronized void trigger(int ruleid, DateTime now) {
    logger.debug("trigger("+ruleid+", DateTime)");
    IdDateMapping m = new IdDateMapping(ruleid, now);
    
    if (!registeredTriggers.contains(m)) {
      registeredTriggers.add(m);
    }
    
    // Keep a backlog of 100 entries until there is need for more clever solution
    if (registeredTriggers.size() > 100) {
      registeredTriggers.remove(0);
    }
  }
  
  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#isTriggered(int, eu.baltrad.fc.DateTime)
   */
  @Override
  public synchronized boolean isTriggered(int ruleid, DateTime now) {
    IdDateMapping m = new IdDateMapping(ruleid, now);
    return registeredTriggers.contains(m);
  }
  
  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#getRadarSources()
   */
  public synchronized List<String> getRadarSources() {
    List<Source> sources = catalog.getCatalog().database().sources();
    List<String> radarNames = new ArrayList<String>(sources.size());

    for (Source src : sources) {
      if (src.has("RAD")) {
         radarNames.add(src.get("_name"));
      }
    }
    return radarNames;
  }
}
