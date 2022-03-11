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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.bdb.util.Time;

/**
 * Utility functions when accessing the database and querying for
 * various things.
 * @author Anders Henja
 */
public interface IRuleUtilities {
  /**
   * Fetch the lowest scan for all provided sources. The elevation angle is accessible
   * from the {@link eu.baltrad.beast.db.CatalogEntry#getAttribute(String)}.
   * @param startDT the start time of the interval to search in
   * @param stopDT the stop time of the interval to search in
   * @param sources the node id of the sources
   * @return a map of source : elevation angle
   */
  public Map<String, Double> fetchLowestSourceElevationAngle(DateTime startDT, DateTime stopDT, List<String> sources);

  /**
   * Fetch the lowest scan for all provided sources. The elevation angle is accessible
   * from the {@link eu.baltrad.beast.db.CatalogEntry#getAttribute(String)}.
   * @param startDT the start time of the interval to search in
   * @param stopDT the stop time of the interval to search in
   * @param sources the node id of the sources
   * @param quantity the quantity to look for in the files
   * @return a map of source : elevation angle
   */
  public Map<String, Double> fetchLowestSourceElevationAngle(DateTime startDT, DateTime stopDT, List<String> sources, String quantity);

  /**
   * Returns the catalog entry that was stored first among the provided entries
   * @param entries the entries
   * @return the first stored entry
   */
  public CatalogEntry findFirstStoredEntry(List<CatalogEntry> entries);

  /**
   * Returns the catalog entry that was stored last among the provided entries
   * @param entries the entries
   * @return the first stored entry
   */
  public CatalogEntry findLastStoredEntry(List<CatalogEntry> entries);
  
  /**
   * Removes all entries that have a storage time older than limit.
   * @param entries The entries to filter
   * @param limit the date time limit
   * @return the filtered list
   */
  public List<CatalogEntry> removeEntriesWithStorageTimeOlderThan(List<CatalogEntry> entries, DateTime limit);
  
  /**
   * Returns an entry with the specified name. Note, it will always be the first match so
   * if there are more than one entry with the specified source name you will not be aware
   * of that if using this method.
   * @param source the source name
   * @param entries the list of entries to traverse
   * @return a catalog entry
   */
  public CatalogEntry getEntryBySource(String source, List<CatalogEntry> entries);

  /**
   * Filter entries by time closest to a nominal time. If the list contains more than
   * one entry from the same source, the one nearest in time to the nominal time will
   * be used.
   * @param nominalDT the nominal time
   * @param entries a list of entries
   * @return a list of entries
   */
  public List<CatalogEntry> getEntriesByClosestTime(DateTime nominalDT, List<CatalogEntry> entries);
  
  /**
   * Filter entries by source. Pick entries that have a source present in sources.
   * @param sources list of sources to match against
   * @param entries a list of entries to filter
   * @return a list of entries
   */
  public List<CatalogEntry> getEntriesBySources(List<String> sources, List<CatalogEntry> entries);

  /**
   * Creates a list of files from the entries. 
   * @param entries a list of entries
   * @return a list of files
   */
  public List<String> getFilesFromEntries(List<CatalogEntry> entries);
  
  /**
   * Creates a list of uuids
   * @param entries a list of entries
   * @return a list of uuids
   */
  public List<String> getUuidStringsFromEntries(List<CatalogEntry> entries);
  
  /**
   * Creates a list of elevation angles. It is assumed that each entry contains an elangle at /dataset1/where/elangle, otherwise unexpected behaviour
   * @param entries a list of entries
   * @return a list of doubles.
   */
  public List<Double> getElanglesFromEntries(List<CatalogEntry> entries);
  
  /**
   * Returns a list of sources from the provided entries
   * @param entries the entries
   * @return a list of sources
   */
  public List<String> getSourcesFromEntries(List<CatalogEntry> entries);
  
  /**
   * Creates the storage DateTime from a catalog entry
   * @param ce the catalog entry
   * @return the date time
   */
  public DateTime createStorageDateTime(CatalogEntry ce);
  
  /**
   * Creates a gregorian calendar with the specified date/time
   * @param dt the date time
   * @return a gregorian calendar
   */
  public Calendar createCalendar(DateTime dt);
  
  /**
   * @return the current calendar. Same as calling GregorianCalendar.getInstance(). 
   */
  public Calendar now();
  
  /**
   * @return the current date time.
   */
  public DateTime nowDT();

  /**
   * Creates an date time instance from a calendar
   * @param c the calendar
   * @return the date time instance
   */
  public DateTime createDateTime(Calendar c);
  
  /**
   * Creates a date time instance
   * @param year the year
   * @param month the month between 1 - 12
   * @param dayOfMonth the day between 1 - 31
   * @param hourOfDay the hour of the day between 0 - 23
   * @param minute the minute of the hour between 0 - 59
   * @param second the second of the minute between 0 - 59
   * @return the date time instance
   */
  public DateTime createDateTime(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second);
  
  /**
   * Creates a date time instance from a util date.
   * @param date the date
   * @return the date time
   */
  public DateTime createDateTime(java.util.Date date);

  /**
   * Creates the nominal time from current time and an interval.
   * 
   * The interval must be evenly dividable by 60. E.g. 1,2,3,4,...,15,..30,60.
   * The nominal time will be adjusted so that it is within the nearest lowest
   * time interval. I.e. If interval is 10 and minutes is 9, then the minutes
   * will be set to 0.
   * @param now current time
   * @param interval the interval
   * @return the nominal time.
   * @throws InvalidArgumentException if interval not valid
   */
  public DateTime createNominalTime(DateTime now, int interval);
  
  /**
   * Same as {@link #createNominalTime(DateTime, int)} but takes
   * date and time separately instead.
   * @param d the date
   * @param t the time 
   * @param interval the interval
   * @return a nominal time
   */
  public DateTime createNominalTime(Date d, Time t, int interval);
  
  /**
   * Almost same as {@link #createNominalTime(DateTime, int)} but returns
   * the end time in the interval instead. I.e. if interval is 10 and
   * minutes is 9, then the minutes returned will be 10.
   * @param now current time
   * @param interval the interval
   * @return end nominal time
   */
  public DateTime createNextNominalTime(DateTime now, int interval);
  
  /**
   * Almost same as {@link #createNominalTime(DateTime, int)} but returns
   * the previous nominal time instead. I.e. if interval is 10 and
   * minutes is 9, then the minutes returned will be 0.
   * @param now current time
   * @param interval the interval
   * @return previous nominal time
   */
  public DateTime createPrevNominalTime(DateTime now, int interval);
  
  /**
   * Calculates the timeout in ms that should be used. If nominalTimeout = false, then the returned value
   * will be timeout regardless of nominal time. However, if nominalTimeout is specified then
   * the difference between now and nominal time will be used and the difference will either be substracted or added to
   * the timeout value
   * @param nominalTime the nominal time that we are aiming for
   * @param nominalTimeout if timeout should be based on nominal time or not
   * @param timeout the timeout in milliseconds
   * @return the timeout in ms
   */
  public long getTimeoutTime(DateTime nominalTime, boolean nominalTimeout, long timeout);
  
  /**
   * This is a way of indicating if a rule has been triggered or not
   * for a specific time period. This can be quite useful if you know
   * that a rule will be triggered more than once and only want to
   * generate it more than once. Note, that old entries will automatically
   * be removed. 
   * @param ruleid the rule id
   * @param now the time period for this rule
   */
  public void trigger(int ruleid, DateTime now);
  
  /**
   * Returns if the rule has been triggered for the specified date time.
   * @param ruleid the rule id
   * @param now the time period to check
   * @return true if the rule has been triggered, false otherwise
   */
  public boolean isTriggered(int ruleid, DateTime now);
  
  /**
   * Returns a list of all available radar source names, like
   * searl,...
   * @return the list of radar names
   */
  public List<String> getRadarSources();
  
  /**
   * Creates a diff between expected and actual list of strings. The returned mapping contains
   * the joined strings in expected and actual and the Integer value has the following meaning.
   * 0 = means that the string exists in both expected and actual
   * -1 = means that the string exists in expected but not in actual
   * +1 = means that the string exists in actual but not in expected
   * @param expected the list of expected strings
   * @param actual the list of actual strings
   * @return a mapping to identify the difference between the two lits
   */
  public Map<String,Integer> diff(List<String> expected, List<String> actual);
  
  /**
   * Useful for reporting to the system that there might be a deviation between
   * expected and actual sources. This is probably quite handy when reporting
   * alarms to an alarm handler.
   * @param expected the expected sources
   * @param actual the actual sources
   */
  public void reportRadarSourceUsage(List<String> expected, List<String> actual);
}
