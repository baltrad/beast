/* --------------------------------------------------------------------
Copyright (C) 2009-2014 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.rules.gra;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.bdb.util.Time;
import eu.baltrad.bdb.util.TimeDelta;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.filters.TimeSelectionFilter;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;
import eu.baltrad.beast.rules.RuleUtils;
import eu.baltrad.beast.rules.acrr.AcrrRule;

/**
 * @author Anders Henja
 *
 */
public class GraRule extends AcrrRule {
  /**
   * The default interval
   */
  private static int DEFAULT_INTERVAL = 12;
  
  /**
   * The default first term UTC
   */
  private static int DEFAULT_FIRST_TERM_UTC = 6;
  
  /**
   * The name of this static acrr type
   */
  public final static String TYPE = "blt_gra";

  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(GraRule.class);
  
  /**
   * Number of hours / calculation.
   */
  private int interval = DEFAULT_INTERVAL;

  /**
   * First term of the calculation. For example, if interval is 12 and firstTermUTC = 6 it means
   * that the first gra calculation will be performed between 18:00 - 06:00 and the next one between 06:00 and 18:00.
   */
  private int firstTermUTC = DEFAULT_FIRST_TERM_UTC;
  
  public GraRule() {
    setHours(DEFAULT_INTERVAL);
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRule#getType()
   */
  @Override
  public String getType() {
    return TYPE;
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    logger.debug("ENTER: handle(IBltMessage)");
    try {
      if (message instanceof BltTriggerJobMessage) {
        logger.info("ENTER: execute GraRule with ruleId: " + getRuleId() + ", thread: " + Thread.currentThread().getName());
        DateTime nowdt = getNowDT();
        if (((BltTriggerJobMessage)message).getScheduledFireTime() != null) {
          nowdt = getRuleUtilities().createDateTime(((BltTriggerJobMessage)message).getScheduledFireTime());
        }        
        DateTime nt = getNominalTime(nowdt);
        List<CatalogEntry> entries = findFiles(nt);
        List<String> uuids = getRuleUtilities().getUuidStringsFromEntries(entries);
        
        BltGenerateMessage result = new BltGenerateMessage();
        Date date = nt.getDate();
        Time time = nt.getTime();
        
        result.setAlgorithm("eu.baltrad.beast.CreateGraCoefficient");
        result.setFiles(uuids.toArray(new String[0]));
        List<String> args = new ArrayList<String>();
        args.add("--area="+getArea());
        args.add("--date="+RuleUtils.getFormattedDate(date));
        args.add("--time="+RuleUtils.getFormattedTime(time));
        args.add("--zra="+getZrA());
        args.add("--zrb="+getZrB());
        args.add("--interval="+getHours());
        args.add("--N="+(getFilesPerHour() * getHours() + 1));
        args.add("--accept="+ getAcceptableLoss());
        args.add("--quantity="+getQuantity());
        args.add("--distancefield=" + getDistancefield());

        if (getOptions() != null && !getOptions().equals("")) {
          args.add("--options="+getOptions());
        }    

        result.setArguments(args.toArray(new String[0]));
        
        logger.debug("GraRule createMessage - entries: " +
            StringUtils.collectionToDelimitedString(uuids, " "));
        
        logger.info("EXIT: execute GraRule with ruleId: " + getRuleId() + ", thread: " + Thread.currentThread().getName());
        
        return result;
      }
    } finally {
      logger.debug("EXIT: handle(IBltMessage)");
    }
    return null;
  }

  @Override
  protected List<CatalogEntry> findFiles(DateTime now) {
    DateTime endDt = getRuleUtilities().createNominalTime(now, getFilesPerHourInterval());
    Calendar c = getRuleUtilities().createCalendar(endDt);
    c.add(Calendar.HOUR, -interval);
    DateTime startDt = getRuleUtilities().createDateTime(c);
    TimeSelectionFilter filter = createFilter(startDt, endDt, getFilesPerHourInterval());
    return filterEntries(getCatalog().fetch(filter));
  }
  
  /**
   * Returns the offset in hours to the first observation term. This is the time when the term ends.
   * @return the offset in hours
   */
  public int getFirstTermUTC() {
    return firstTermUTC;
  }

  /**
   * Sets the  offset in hours to the first observation term. This is the time when the term ends.
   * @param firstTermUTC the offset in hours
   */
  public void setFirstTermUTC(int firstTermUTC) {
    if (firstTermUTC >= 0 && firstTermUTC < 24) {
      this.firstTermUTC = firstTermUTC;
    } else {
      throw new IllegalArgumentException("First term UTC not valid (should be between 0 and 23)");
    }
  }

  /**
   * The interval for each term. This will always be the same as hours but it is only allowed to have the same values
   * as the interval.
   * @return the interval
   */
  public int getInterval() {
    return interval;
  }

  /**
   * The interval of which we can use as periods. This overrides the hours setting.
   * @param interval the interval, must be evenly dividable by 24. I.e. 1,2,3,4,6,8,12 and 24
   */
  public void setInterval(int interval) {
    if (interval == 1 || interval == 2 || interval == 3 || interval == 4 || interval == 6 || interval == 8 || interval == 12 || interval == 24) {
      this.interval = interval;
    } else {
      throw new IllegalArgumentException("Interval not valid (should be 1,2,3,4,6,8,12 or 24)"); 
    }
  }

  /**
   * Same definition as {@link #setInterval(int)}
   */
  @Override
  public void setHours(int hours) {
    setInterval(hours);
  }
  
  /**
   * Same definition as {@link #getHours()}
   */
  @Override
  public int getHours() {
    return getInterval();
  }
  
  /**
   * Returns the nearest lowest start of period from now. For example, if interval = 6, firstTermUTC = 6 and now.hour() = 7, then
   * returned date will be one hour lower than now.
   * @param now now
   * @return first previous start of period from now
   */
  protected DateTime getNominalTime(DateTime now) {
    int hour = now.getTime().hour();
    int pl = 0;
    
    if (hour >= firstTermUTC) {
      pl = (hour - firstTermUTC) % interval;
    } else {
      pl = ((hour - firstTermUTC) + 24) % interval;
    }

    DateTime nt = new DateTime(now.getDate(), new Time(now.getTime().hour(), 0, 0));
    TimeDelta td = new TimeDelta().addSeconds(- (pl * 3600));
    nt = nt.add(td);

    return nt;
  }
  
  /**
   * Basically here for testing purposes
   * @return now as date time object
   */
  protected DateTime getNowDT() {
    return getRuleUtilities().nowDT();
  }
}
