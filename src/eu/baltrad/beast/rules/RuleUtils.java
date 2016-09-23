package eu.baltrad.beast.rules;

import java.util.Formatter;

import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.Time;

/**
 * A class containing common utility functions for rules.
 *
 * @author Mats Vernersson
 */
public class RuleUtils {
  
  /**
   * Converts a date object into a string that is formatted according to the common format that 
   * is expected by the message recipients/adaptors. The format is: 'YYYYmmdd'.
   * 
   * @param date The date to format
   * @return a string containing the formatted date
   */
  @SuppressWarnings("resource")
  public static String getFormattedDate(Date date) {
    return new Formatter().format("%d%02d%02d",date.year(), date.month(), date.day()).toString();
  }
  
  /**
   * Converts a time object into a string that is formatted according to the common format that 
   * is expected by the message recipients/adaptors. The format is: 'HHMMSS'.
   * 
   * @param time The time to format
   * @return a string containing the formatted time
   */
  @SuppressWarnings("resource")
  public static String getFormattedTime(Time time) {
    return new Formatter().format("%02d%02d%02d",time.hour(), time.minute(), time.second()).toString();
  }

}
