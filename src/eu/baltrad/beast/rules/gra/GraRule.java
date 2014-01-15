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
import java.util.Formatter;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.bdb.util.Time;
import eu.baltrad.bdb.util.TimeDelta;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;
import eu.baltrad.beast.rules.acrr.AcrrRule;

/**
 * @author Anders Henja
 *
 */
public class GraRule extends AcrrRule {
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
  private int interval = 12;
  
  /**
   * First term of the calculation. For example, if interval is 12 and firstTermUTC = 6 it means
   * that the first gra calculation will be performed between 18:00 - 06:00 and the next one between 06:00 and 18:00.
   */
  private int firstTermUTC = 6;
  
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
        DateTime nt = getNominalTime(getNowDT());
        List<CatalogEntry> entries = findFiles(nt);
        List<String> uuids = getRuleUtilities().getUuidStringsFromEntries(entries);
        
        BltGenerateMessage result = new BltGenerateMessage();
        Date date = nt.getDate();
        Time time = nt.getTime();
        
        result.setAlgorithm("eu.baltrad.beast.CreateGraCoefficient");
        result.setFiles(uuids.toArray(new String[0]));
        List<String> args = new ArrayList<String>();
        args.add("--area="+getArea());
        args.add("--date="+new Formatter().format("%d%02d%02d",date.year(), date.month(), date.day()).toString()); 
        args.add("--time="+new Formatter().format("%02d%02d%02d",time.hour(), time.minute(), time.second()).toString());
        args.add("--zra="+getZrA());
        args.add("--zrb="+getZrB());
        args.add("--hours="+getHours());
        args.add("--N="+(getFilesPerHour() * getHours() + 1));
        args.add("--accept="+ getAcceptableLoss());
        args.add("--quantity="+getQuantity());
        args.add("--distancefield=" + getDistancefield());
        
        result.setArguments(args.toArray(new String[0]));
        
        return result;
      }
    } finally {
      logger.debug("EXIT: handle(IBltMessage)");
    }
    return null;
  }

  public int getFirstTermUTC() {
    return firstTermUTC;
  }

  public void setFirstTermUTC(int firstTermUTC) {
    if (firstTermUTC >= 0 && firstTermUTC < 24) {
      this.firstTermUTC = firstTermUTC;
    } else {
      throw new IllegalArgumentException("First term UTC not valid (should be between 0 and 23)");
    }
  }

  public int getInterval() {
    return interval;
  }

  /**
   * The interval of which we can use as periods
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
