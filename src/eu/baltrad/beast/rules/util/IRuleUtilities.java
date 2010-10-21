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

import java.util.GregorianCalendar;
import java.util.List;

import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.fc.Date;
import eu.baltrad.fc.DateTime;
import eu.baltrad.fc.Time;

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
   * @return a list of found entries.
   */
  public List<CatalogEntry> fetchLowestSourceElevationAngle(DateTime startDT, DateTime stopDT, List<String> sources);

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
   * Creates a list of files from the entries. If the list contains more than
   * one entry from the same source, the one nearest in time to the nominal time 
   * will be used.
   * @param nominalDT the nominal time
   * @param sources the sources that should be returned (acts as a filter)
   * @param entries a list of entries
   * @return a list of files
   */
  public List<String> getFilesFromEntries(DateTime nominalDT, List<String> sources, List<CatalogEntry> entries);
  
  /**
   * Returns a list of sources from the provided entries
   * @param entries the entries
   * @return a list of sources
   */
  public List<String> getSourcesFromEntries(List<CatalogEntry> entries);
  
  /**
   * Creates a gregorian calendar with the specified date/time
   * @param date the date
   * @param time the time
   * @return a gregorian calendar
   */
  public GregorianCalendar createCalendar(DateTime dt);
  
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
}
