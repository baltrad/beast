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

import org.quartz.CronExpression;

/**
 * Factory for creating cron entries.
 * 
 * A cron expression is either 6 or 7 fields with a space between
 * the fields. This means that any special character in a field
 * should never be separated by a space since that will be counted
 * as the next field. The format of the expression is always.
 *  
 * &lt;second&gt; &lt;minute&gt; &lt;hour&gt; &lt;day of month&gt; &lt;month&gt; &lt;day of week&gt; (&lt;year&gt;)
 * 
 * field           allowed             special character
 * seconds         0 - 59              , - &#42; /
 * minute          0 - 59              , - &#42; /
 * hour            0 - 23              , - &#42; /
 * day of month    0 - 31              , - &#42; ? / L W
 * month           1 - 12              , - &#42; /
 * day of week     1 - 7 or SUN-SAT    , - &#42; ? / L #
 * year (optional) empty, 1970-2199    , - &#42; /
 * 
 * If you want to specify all allowed values you can use asterisk (*) which
 * will represent all values between first - last.
 * 
 * It is also possible to define both ranges and lists and a combination of
 * both. Ranges are represented by a hyphen (-) and lists by a comma (,).
 * This means that you could define all minutes between 0 and 10 with 0-10
 * and hour 0 - 6 and 18-23 with 0-6,18-23 but remember to not use spaces
 * when defining a field since that will flow over to the next field.
 * 
 * You can also define step values by using &lt;range&gt; or &#42; / &lt;value&gt; to 
 * indicate specific values. So if you want to execute a cron every second hour
 * between 0 - 6 in the morning you could write 0-6/2 or if you want to specify every
 * second hour the whole day you define it with &#42;/2.
 * 
 * L is defined as last which means that in day of month L would have the effect to specify
 * last day in the current month. You can also specify a specific day which means that
 * 6L would represent last friday of the month.
 * 
 * W represents weekdays (2-6) and can be used in conjunction with L which gives that LW
 * represents the last weekday of the month.
 * 
 * # is another special character that represents the n-th y-th day in the month, which
 * gives that if you for example specify 6#3 it would mean the 3rd friday of the month.
 * 
 * Cron entries can also have a seventh field that defines the year but that is optional.
 * 
 * It is not possible to specify both day of week and day of month simultaneously so one
 * of those two entries must be specified with a ? at any time.
 * 
 * For more information about the cron-scheduling, please refer to manual for 
 * <a href="http://www.quartz-scheduler.org/docs/tutorials/crontrigger.html">Quartz</a>.
 * 
 * @author Anders Henja
 * @date 2011-01-11
 */
public class CronEntryFactory {
  /**
   * Creates an entry from a cron expression and a job name
   * @param expression the expression
   * @param jobName the job name
   * @return the cron entry
   */
  public CronEntry create(String expression, String jobName) {
    validateExpression(expression);
    return new CronEntry(expression, jobName);
  }

  /**
   * Creates an entry from a cron expression, a job name and an id
   * @param expression the expression
   * @param jobName the job name
   * @param id the id
   * @return the cron entry
   */  
  public CronEntry create(String expression, String jobName, int id) {
    validateExpression(expression);
    return new CronEntry(id, expression, jobName);
  }
  
  /**
   * Creates a cron entry.
   * @param seconds - second pattern
   * @param minute  - minute pattern
   * @param hour    - hour pattern
   * @param dayOfMonth - day of month pattern
   * @param month   - month pattern
   * @param dayOfWeek  - day of week pattern
   * @param jobName the name of the job
   * @return a cron entry
   */
  public CronEntry create(String seconds, String minute, String hour, String dayOfMonth, String month, String dayOfWeek, String jobName) {
    StringBuffer buf = new StringBuffer();
    if (seconds != null && !seconds.equals("")) {
      buf.append(seconds);
    } else {
      buf.append("0");
    }
    buf.append(" ").append(minute);
    buf.append(" ").append(hour);
    buf.append(" ").append(dayOfMonth);
    buf.append(" ").append(month);
    buf.append(" ").append(dayOfWeek);
    
    return create(buf.toString(), jobName);
  }

  /**
   * Creates a cron entry.
   * @param seconds - second pattern
   * @param minute  - minute pattern
   * @param hour    - hour pattern
   * @param dayOfMonth - day of month pattern
   * @param month   - month pattern
   * @param dayOfWeek  - day of week pattern
   * @param jobName - the name of the job
   * @param id - the id
   * @return a cron entry
   */
  public CronEntry create(String seconds, String minute, String hour, String dayOfMonth, String month, String dayOfWeek, String jobName, int id) {
    StringBuffer buf = new StringBuffer();
    if (seconds != null && !seconds.equals("")) {
      buf.append(seconds);
    } else {
      buf.append("0");
    }
    buf.append(" ").append(minute);
    buf.append(" ").append(hour);
    buf.append(" ").append(dayOfMonth);
    buf.append(" ").append(month);
    buf.append(" ").append(dayOfWeek);
    
    return create(buf.toString(), jobName, id);
  }  
  
  /**
   * Creates a cron entry.
   * @param minute  - minute pattern
   * @param hour    - hour pattern
   * @param dayOfMonth - day of month pattern
   * @param month   - month pattern
   * @param dayOfWeek  - day of week pattern
   * @param jobName the name of the job
   * @return a cron entry
   */
  public CronEntry create(String minute, String hour, String dayOfMonth, String month, String dayOfWeek, String jobName) {
    return create("0", minute, hour, dayOfMonth, month, dayOfWeek, jobName);
  }

  /**
   * Creates a cron entry.
   * @param minute  - minute pattern
   * @param hour    - hour pattern
   * @param dayOfMonth - day of month pattern
   * @param month   - month pattern
   * @param dayOfWeek  - day of week pattern
   * @param jobName the name of the job
   * @param id - the id
   * @return a cron entry
   */
  public CronEntry create(String minute, String hour, String dayOfMonth, String month, String dayOfWeek, String jobName, int id) {
    return create("0", minute, hour, dayOfMonth, month, dayOfWeek, jobName, id);
  }
  
  /**
   * Creates a cron entry.
   * @param hour    - hour pattern
   * @param dayOfMonth - day of month pattern
   * @param month   - month pattern
   * @param dayOfWeek  - day of week pattern
   * @param jobName - the name of the job
   * @return a cron entry
   */
  public CronEntry create(String hour, String dayOfMonth, String month, String dayOfWeek, String jobName) {
    return create("0", hour, dayOfMonth, month, dayOfWeek, jobName);
  }

  /**
   * Creates a cron entry.
   * @param hour    - hour pattern
   * @param dayOfMonth - day of month pattern
   * @param month   - month pattern
   * @param dayOfWeek  - day of week pattern
   * @param jobName - the name of the job
   * @param id - the id
   * @return a cron entry
   */
  public CronEntry create(String hour, String dayOfMonth, String month, String dayOfWeek, String jobName, int id) {
    return create("0", hour, dayOfMonth, month, dayOfWeek, jobName, id);
  }  
  
  /**
   * Creates a cron entry.
   * @param dayOfMonth - day of month pattern
   * @param month   - month pattern
   * @param dayOfWeek  - day of week pattern
   * @param jobName the name of the job
   * @return a cron entry
   */
  public CronEntry create(String dayOfMonth, String month, String dayOfWeek, String jobName) {
    return create("0", dayOfMonth, month, dayOfWeek, jobName);
  }

  /**
   * Creates a cron entry.
   * @param dayOfMonth - day of month pattern
   * @param month   - month pattern
   * @param dayOfWeek  - day of week pattern
   * @param jobName - the name of the job
   * @param id - the id
   * @return a cron entry
   */
  public CronEntry create(String dayOfMonth, String month, String dayOfWeek, String jobName, int id) {
    return create("0", dayOfMonth, month, dayOfWeek, jobName, id);
  }
  
  /**
   * Validates that an expression is valid
   * @param expression the expression to validate
   * @throws SchedulerException if the expression isn't valid
   */
  protected void validateExpression(String expression) {
    try {
      new CronExpression(expression);
    } catch (Throwable t) {
      throw new SchedulerException(t.getMessage());
    }
  }
}
