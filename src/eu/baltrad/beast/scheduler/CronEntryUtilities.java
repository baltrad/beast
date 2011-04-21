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

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.quartz.CronExpression;

/**
 * Utilities when working with cron entry expressions
 * @author Anders Henja
 * @date 2011-01-12
 */
public class CronEntryUtilities {
  /**
   * Base pattern, used by seconds, minutes, hours, months
   */
  private Pattern BASE_PATTERN=Pattern.compile("^[0-9\\-*/]+$");
  
  /**
   * Day of month pattern
   */
  private Pattern DAYOFMONTH_PATTERN=Pattern.compile("^[0-9\\-*/LW?]+$");

  /**
   * Day of month pattern
   */
  private Pattern DAYOFWEEK_PATTERN=Pattern.compile("^[0-9\\-*/L#?]+$");

  /**
   * Index of seconds
   */
  public final static int SECONDS_INDEX = 0;
  
  /**
   * Index of minutes
   */
  public final static int MINUTES_INDEX = 1;

  /**
   * Index of hours
   */
  public final static int HOURS_INDEX = 2;

  /**
   * Index of days of month
   */
  public final static int DAYSOFMONTH_INDEX = 3;
  
  /**
   * Index of months
   */
  public final static int MONTHS_INDEX = 4;
  
  /**
   * Index of days of week
   */
  public final static int DAYSOFWEEK_INDEX = 5;
  
  /**
   * Index of years
   */
  public final static int YEARS_INDEX = 6;
  
  /**
   * Constructor
   */
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
  public String createBasePattern(List<String> values) {
    StringBuffer b = new StringBuffer();
    int len = values.size();
    for (int i = 0; i < len; i++) {
      String value = values.get(i);
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
  public List<String> parseBasePattern(String value) {
    String[] result = value.split(",");
    return Arrays.asList(result);
  }

  /**
   * Creates a second pattern
   * @param values the second values
   * @return the second pattern
   */
  public String createSecondPattern(List<String> values) {
    return createBasePattern(values);
  }
  
  /**
   * Parses a second pattern
   * @param value the second pattern
   * @return an array of individual second patterns
   */
  public List<String> parseSecondPattern(String value) {
    return parseBasePattern(value);
  }
  
  /**
   * Creates a minute pattern
   * @param values the minute values
   * @return the minute pattern
   */
  public String createMinutePattern(List<String> values) {
    return createBasePattern(values);
  }
  
  /**
   * Parses a minute pattern
   * @param value the minute pattern
   * @return an array of individual minute patterns
   */
  public List<String> parseMinutePattern(String value) {
    return parseBasePattern(value);
  }

  /**
   * Creates a hour pattern
   * @param values the hour values
   * @return the hour pattern
   */
  public String createHourPattern(List<String> values) {
    return createBasePattern(values);
  }
  
  /**
   * Parses a hour pattern
   * @param value the hour pattern
   * @return an array of individual hour patterns
   */
  public List<String> parseHourPattern(String value) {
    return parseBasePattern(value);
  }

  /**
   * Creates a day of month pattern
   * @param values the day of month patterns
   * @return the day of month pattern
   */
  public String createDayOfMonthPattern(List<String> values) {
    StringBuffer b = new StringBuffer();
    int len = values.size();
    for (int i = 0; i < len; i++) {
      String value = values.get(i);
      if (DAYOFMONTH_PATTERN.matcher(value) == null) {
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
   * Parses a day of month pattern
   * @param value the day of month pattern
   * @return an array of individual day of month patterns
   */
  public List<String> parseDayOfMonthPattern(String value) {
    return parseBasePattern(value);
  }
  
  /**
   * Creates a month pattern
   * @param values the month values
   * @return the month pattern
   */
  public String createMonthPattern(List<String> values) {
    return createBasePattern(values);
  }
  
  /**
   * Parses a month pattern
   * @param value the month pattern
   * @return an array of individual month patterns
   */
  public List<String> parseMonthPattern(String value) {
    return parseBasePattern(value);
  }
  
  /**
   * Creates a day of week pattern
   * @param values the day of week patterns
   * @return the day of week pattern
   */
  public String createDayOfWeekPattern(List<String> values) {
    StringBuffer b = new StringBuffer();
    int len = values.size();
    for (int i = 0; i < len; i++) {
      String value = values.get(i);
      if (DAYOFWEEK_PATTERN.matcher(value) == null) {
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
   * Parses a day of week pattern
   * @param value the day of week pattern
   * @return an array of individual day of week patterns
   */
  public List<String> parseDayOfWeekPattern(String value) {
    return parseBasePattern(value);
  }
  
  /**
   * Creates a year pattern
   * @param values the year values
   * @return the year pattern
   */
  public String createYearPattern(List<String> values) {
    return createBasePattern(values);
  }
  
  /**
   * Parses a year pattern
   * @param value the year pattern
   * @return an array of individual year patterns
   */
  public List<String> parseYearPattern(String value) {
    return parseBasePattern(value);
  }
  
  /**
   * Extracts all individual entries for all fields in a cron
   * expression
   * @param expression the cron expression
   * @return an array of entries
   */
  @SuppressWarnings("unchecked")
  public List<String>[] parseAllInExpression(String expression) {
    validateExpression(expression);
    List<String>[] result = null;
    String[] tokens = expression.split(" ");
    if (tokens.length < 6 || tokens.length > 7) {
      throw new SchedulerException("A cron expression must contain either 6 or 7 fields");
    }
    result = new List[tokens.length];
    result[SECONDS_INDEX] = parseSecondPattern(tokens[0]);
    result[MINUTES_INDEX] = parseMinutePattern(tokens[1]);
    result[HOURS_INDEX] = parseHourPattern(tokens[2]);
    result[DAYSOFMONTH_INDEX] = parseDayOfMonthPattern(tokens[3]);
    result[MONTHS_INDEX] = parseMonthPattern(tokens[4]);
    result[DAYSOFWEEK_INDEX] = parseDayOfWeekPattern(tokens[5]);
    if (tokens.length == 7) {
      result[YEARS_INDEX] = parseYearPattern(tokens[6]);
    }
    
    return result;
  }
  
  /**
   * Creates a cron expression from provided entries
   * @param seconds the second patterns
   * @param minutes the minute patterns
   * @param hours the hour patterns
   * @param daysOfMonth the day of month patterns
   * @param months the month patterns
   * @param daysOfWeek the day of week patterns
   * @return a cron expression
   */
  public String createExpression(
      List<String> seconds, 
      List<String> minutes, 
      List<String> hours, 
      List<String> daysOfMonth, 
      List<String> months,
      List<String> daysOfWeek) {
    StringBuffer b = new StringBuffer();
    b.append(createSecondPattern(seconds));
    b.append(" ").append(createMinutePattern(minutes));
    b.append(" ").append(createHourPattern(hours));
    b.append(" ").append(createDayOfMonthPattern(daysOfMonth));
    b.append(" ").append(createMonthPattern(months));
    b.append(" ").append(createDayOfWeekPattern(daysOfWeek));
    return b.toString();
  }
  
  /**
   * Validates that an expression is valid
   * @param expression the expression to validate
   * @throws SchedulerException if the expression isn't valid
   */
  public static void validateExpression(String expression) {
    try {
      new CronExpression(expression);
    } catch (ParseException t) {
      throw new SchedulerException(t.getMessage(), t);
    }
  }
}
