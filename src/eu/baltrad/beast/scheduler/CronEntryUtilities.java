/* --------------------------------------------------------------------
Copyright (C) 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.scheduler;

import java.util.regex.Pattern;

/**
 *
 * @author Anders Henja
 * @date Jan 12, 2011
 */
public class CronEntryUtilities {
  private Pattern BASE_PATTERN=Pattern.compile("[0-9\\-*/]+");
  
  public CronEntryUtilities() {
  }
  
  /**
   * Creates a base expression from a list of values or intervals.
   * I.e. it is allowed to specify a sequence like 3,5,&#42;/2 which would
   * mean every second value and also value 3 and 5.
   * Only allowed characters are 0-9 - * and /. I.e. separator character ,
   * is not allowed since it will be used to concatenate the strings.
   * @param values a list of values
   * @return the string pattern 
   */
  public String createBasePattern(String[] values) {
    StringBuffer b = new StringBuffer();
    int len = values.length;
    for (int i = 0; i < len; i++) {
      String value = values[i];
      if (BASE_PATTERN.matcher(value) == null) {
        throw new SchedulerException("Bad character");
      } else {
        b.append(value);
        if (i < len-1) {
          b.append(",");
        }
      }
    }
    return b.toString();
  }
  
  /**
   * Parses a base pattern into it's separate parts, i.e. splits by comma.
   * @param value the pattern
   * @return an array of individual values
   */
  public String[] parseBasePattern(String value) {
    String[] result = value.split(",");
    return result;
  }

  /**
   * Creates a second pattern
   * @param values the second values
   * @return the second pattern
   */
  public String createSecondPattern(String[] values) {
    return createBasePattern(values);
  }
  
  /**
   * Parses a second pattern
   * @param value the second pattern
   * @return an array of individual second patterns
   */
  public String[] parseSecondPattern(String value) {
    return parseBasePattern(value);
  }
  
  /**
   * Creates a minute pattern
   * @param values the minute values
   * @return the minute pattern
   */
  public String createMinutePattern(String[] values) {
    return createBasePattern(values);
  }
  
  /**
   * Parses a minute pattern
   * @param value the minute pattern
   * @return an array of individual minute patterns
   */
  public String[] parseMinutePattern(String value) {
    return parseBasePattern(value);
  }

  /**
   * Creates a hour pattern
   * @param values the hour values
   * @return the hour pattern
   */
  public String createHourPattern(String[] values) {
    return createBasePattern(values);
  }
  
  /**
   * Parses a hour pattern
   * @param value the hour pattern
   * @return an array of individual hour patterns
   */
  public String[] parseHourPattern(String value) {
    return parseBasePattern(value);
  }
}
