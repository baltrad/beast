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

import eu.baltrad.bdb.db.AttributeQuery;
import eu.baltrad.bdb.db.AttributeResult;
import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.expr.ExpressionFactory;
import eu.baltrad.bdb.oh5.Source;
import eu.baltrad.bdb.storage.LocalStorage;
import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.bdb.util.Time;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.system.RadarConnectionStatusReporter;
import eu.baltrad.beast.system.SystemStatus;

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
   * The radar connection status reporter
   */
  private RadarConnectionStatusReporter reporter = null;
  
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
   * @param reporter the reporter to set
   */
  public void setRadarReporter(RadarConnectionStatusReporter reporter) {
    this.reporter = reporter;
  }
  
  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#fetchLowestSourceElevationAngle(eu.baltrad.bdb.util.DateTime, eu.baltrad.bdb.util.DateTime, java.util.List)
   */
  @Override
  public Map<String, Double> fetchLowestSourceElevationAngle(DateTime startDT, DateTime stopDT, List<String> sources) {
    return fetchLowestSourceElevationAngle(startDT, stopDT, sources, null);
  }

  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#fetchLowestSourceElevationAngle(eu.baltrad.bdb.util.DateTime, eu.baltrad.bdb.util.DateTime, java.util.List, java.lang.String)
   */
  public Map<String, Double> fetchLowestSourceElevationAngle(DateTime startDT, DateTime stopDT, List<String> sources, String quantity) {
    Map<String, Double> result = new HashMap<String, Double>();
    if (sources.isEmpty()) {
      return result;
    }

    ExpressionFactory xpr = new ExpressionFactory();
    Expression dtAttr = xpr.combinedDateTime("what/date", "what/time");
    Expression srcAttr = xpr.attribute("_bdb/source_name");
    
    List<Expression> srcExpr = new ArrayList<Expression>();
    for (String srcStr : sources) {
      srcExpr.add(xpr.literal(srcStr));
    }

    List<Expression> filter = new ArrayList<Expression>();
    filter.add(xpr.eq(xpr.attribute("what/object"), xpr.literal("SCAN")));
    if (quantity != null && !quantity.equals("")) {
      filter.add(xpr.eq(xpr.attribute("what/quantity"), xpr.literal(quantity)));
    }
    filter.add(xpr.ge(dtAttr, xpr.literal(startDT)));
    filter.add(xpr.lt(dtAttr, xpr.literal(stopDT)));
    filter.add(xpr.in(srcAttr, xpr.list(srcExpr)));

    AttributeQuery qry = new AttributeQuery();
    qry.fetch("source", srcAttr);
    qry.fetch("min_elangle", xpr.min(xpr.attribute("where/elangle")));
    qry.setFilter(xpr.and(filter));
    qry.appendGroupClause(srcAttr);

    AttributeResult rset = catalog.getCatalog().getDatabase().execute(qry);
    try {
      while (rset.next()) {
        if (!rset.isNull("min_elangle")) {
          result.put(rset.getString("source"), rset.getDouble("min_elangle"));
        }
      }
    } finally {
      rset.close();
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
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#getEntriesByClosestTime(eu.baltrad.bdb.util.DateTime, java.util.List)
   */
  @Override
  public List<CatalogEntry> getEntriesByClosestTime(DateTime nominalDT, List<CatalogEntry> entries) {
    Map<String, CatalogEntry> entryMap = new HashMap<String, CatalogEntry>();
    Calendar nominalTimeCalendar = createCalendar(nominalDT);
    
    for (CatalogEntry entry: entries) {
      String src = entry.getSource();
      if (!entryMap.containsKey(src)) {
        entryMap.put(src, entry);
      } else {
        Calendar entryCalendar = createCalendar(entry.getDateTime());
        CatalogEntry mapEntry = entryMap.get(src);
        Calendar mapEntryCalendar = createCalendar(mapEntry.getDateTime());
      
        // If the entrys time is closer to the nominal time than the existing one, replace it
        if (Math.abs(entryCalendar.compareTo(nominalTimeCalendar)) < Math.abs(mapEntryCalendar.compareTo(nominalTimeCalendar))) {
          entryMap.put(src, entry);
        }
      }
    }
    return new ArrayList<CatalogEntry>(entryMap.values());
  }

  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#getEntriesBySources(java.util.List, java.util.List)
   */
  public List<CatalogEntry> getEntriesBySources(List<String> sources, List<CatalogEntry> entries) {
    List<CatalogEntry> result = new ArrayList<CatalogEntry>();
    for (CatalogEntry entry : entries) {
      if (sources.contains(entry.getSource())) {
        result.add(entry);
      }
    }
    return result;
  }

  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#getFilesFromEntries(java.util.List)
   */
  @Override
  public List<String> getFilesFromEntries(List<CatalogEntry> entries) {
    List<String> result = new ArrayList<String>();
    LocalStorage storage = catalog.getCatalog().getLocalStorage();
    for (CatalogEntry entry : entries) {
      result.add(storage.store(entry.getFileEntry()).toString());
    }
    return result;
  }

  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#getUuidStringsFromEntries(java.util.List)
   */
  public List<String> getUuidStringsFromEntries(List<CatalogEntry> entries) {
    List<String> result = new ArrayList<String>();
    for (CatalogEntry entry: entries) {
      result.add(entry.getUuid());
    }
    return result;
  }
  
  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#createCalendar(eu.baltrad.bdb.util.DateTime)
   */
  @Override
  public Calendar createCalendar(DateTime dt) {
    GregorianCalendar c = new GregorianCalendar();
    Date date = dt.getDate();
    Time time = dt.getTime();
    c.set(date.year(), date.month()-1, date.day(), time.hour(), time.minute(), time.second());
    return c;
  }

  
  @Override
  public Calendar now() {
    return GregorianCalendar.getInstance();
  }
  
  @Override
  public DateTime nowDT() {
    return createDateTime(now());
  }
  
  @Override
  public DateTime createDateTime(Calendar c) {
    return createDateTime(c.get(Calendar.YEAR), 
        c.get(Calendar.MONTH)+1, 
        c.get(Calendar.DAY_OF_MONTH), 
        c.get(Calendar.HOUR_OF_DAY), 
        c.get(Calendar.MINUTE), 
        c.get(Calendar.SECOND));
  }

  @Override
  public DateTime createDateTime(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
    return new DateTime(year, month, dayOfMonth, hourOfDay, minute, second);
  }
  
  @Override
  public DateTime createDateTime(java.util.Date date) {
    GregorianCalendar c = new GregorianCalendar();
    c.setTime(date);
    return createDateTime(c);
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
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#createNominalTime(eu.baltrad.bdb.util.DateTime, int)
   */
  @Override
  public DateTime createNominalTime(DateTime now, int interval) {
    if (interval == 0 || 60%interval != 0) {
      throw new IllegalArgumentException("Interval must be evenly dividable by 60");
    }
    Time t = now.getTime();
    Date d = now.getDate();
    int period = t.minute() / interval;
    return new DateTime(d, new Time(t.hour(), period*interval, 0));
  }

  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#createNominalTime(eu.baltrad.bdb.util.Date, eu.baltrad.bdb.util.Time, int)
   */
  @Override
  public DateTime createNominalTime(Date d, Time t, int interval) {
    return createNominalTime(new DateTime(d, t), interval);
  }

  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#createNextNominalTime(eu.baltrad.bdb.util.DateTime, int)
   */
  @Override
  public DateTime createNextNominalTime(DateTime now, int interval) {
    if (interval == 0 || 60%interval != 0) {
      throw new IllegalArgumentException("Interval must be evenly dividable by 60");
    }
    Calendar cal = createCalendar(now);
    Time t = now.getTime();
    int period = t.minute() / interval;
    int minute = (period + 1) * interval;
    cal.set(Calendar.MINUTE, minute);
    Date nd = new Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    Time nt = new Time(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
    return new DateTime(nd, nt);
  }

  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#createPrevNominalTime(eu.baltrad.bdb.util.DateTime, int)
   */
  @Override
  public DateTime createPrevNominalTime(DateTime now, int interval) {
    if (interval == 0 || 60%interval != 0) {
      throw new IllegalArgumentException("Interval must be evenly dividable by 60");
    }
    Calendar cal = createCalendar(now);
    Time t = now.getTime();
    int period = t.minute() / interval;
    int minute = (period - 1) * interval;
    cal.set(Calendar.MINUTE, minute);
    Date nd = new Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    Time nt = new Time(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
    return new DateTime(nd, nt);
  }
  
  /**
   * @return The current UTC date time
   */
  protected DateTime getCurrentDateTimeUTC() {
    return new DateTime();
  }
  
  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#getTimeoutTime(eu.baltrad.bdb.util.DateTime, boolean, long)
   */
  @Override
  public long getTimeoutTime(DateTime nominalTime, boolean nominalTimeout, long timeout) {
    if (nominalTimeout) {
      Calendar nominalCal = createCalendar(nominalTime);
      Calendar currentCal = createCalendar(getCurrentDateTimeUTC());
      long to = currentCal.getTimeInMillis() - nominalCal.getTimeInMillis();
      if (to >= timeout) {
        return 0;
      } else {
        return timeout - to;
      }
    }
    return timeout;
  }
  
  /**
   * This function will keep a backlog of 100 entries.
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#trigger(int, eu.baltrad.bdb.util.DateTime)
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
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#isTriggered(int, eu.baltrad.bdb.util.DateTime)
   */
  @Override
  public synchronized boolean isTriggered(int ruleid, DateTime now) {
    IdDateMapping m = new IdDateMapping(ruleid, now);
    return registeredTriggers.contains(m);
  }
  
  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#getRadarSources()
   */
  @Override
  public synchronized List<String> getRadarSources() {
    List<Source> sources = catalog.getCatalog().getDatabase().getSourceManager().getSources();
    List<String> radarNames = new ArrayList<String>(sources.size());

    for (Source src : sources) {
      if (src.has("RAD")) {
         radarNames.add(src.getName());
      }
    }
    return radarNames;
  }
  

  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#diff(java.util.List,java.util.List)
   */
  @Override
  public Map<String,Integer> diff(List<String> expected, List<String> actual) {
    Map<String,Integer> result = new HashMap<String, Integer>();
    if (expected != null) {
      for (String s : expected) {
        result.put(s, -1);
      }
    }
    
    if (actual != null) {
      for (String s : actual) {
        if (result.containsKey(s)) {
          result.put(s, 0);
        } else {
          result.put(s, 1);
        }
      }
    }
    
    return result;
  }
  
  /**
   * @see eu.baltrad.beast.rules.util.IRuleUtilities#reportRadarSourceUsage(java.util.List,java.util.List)
   */
  @Override
  public void reportRadarSourceUsage(List<String> expected, List<String> actual) {
    if (reporter != null) {
      Map<String,Integer> difference = diff(expected, actual);
      for (String s : expected) {
        if (difference.get(s) == 0) {
          reporter.setStatus(s, SystemStatus.OK);
        } else if (difference.get(s) == -1) {
          reporter.setStatus(s, SystemStatus.EXCHANGE_PROBLEM);
        }
      }
    }
  }

}
