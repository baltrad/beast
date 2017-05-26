/* --------------------------------------------------------------------
Copyright (C) 2009-2013 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.rules.acrr;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.oh5.MetadataMatcher;
import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.bdb.util.Time;
import eu.baltrad.bdb.util.TimeDelta;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.CatalogEntry;
import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.db.filters.TimeSelectionFilter;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.RuleUtils;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class AcrrRule implements IRule, InitializingBean {
  /**
   * The name of this static acrr type
   */
  public final static String TYPE = "blt_acrr";
  
  /**
   * The catalog for database access
   */
  private Catalog catalog = null;

  /**
   * Utilities that simplifies database access
   */
  private IRuleUtilities ruleUtil = null;
  
  /**
   * The unique rule id separating this acrr rule from the others.
   */
  private int ruleid = -1;
  
  /**
   * The object type included in the product
   */
  private String objectType = null; // The object type, either IMAGE or COMP
  
  /**
   * The area that this acrr accumulation should cover
   */
  private String area = null;

  /**
   * The numbers of hours we are accumulating over
   */
  private int hours = 1;
  
  /**
   * The ZR-A relation. Default is 200.0
   */
  private double zrA = 200.0;
  
  /**
   * The ZR-B relation. Default is 1.6
   */
  private double zrB = 1.6;
  
  /**
   * The number of files per hour. This value is specified as the number of files included in the product
   * excluding the last file. This means that if filesPerHour = 4, the actual number of files in the product
   * will be 5 (e.g. 00:00,00:15,00:30,00:45,01:00). Note, that both 00:00 and 01:00 is included.
   */
  private int filesPerHour = 4;
  
  /**
   * The acceptable loss in percent.
   */
  private int acceptableLoss = 0;

  /**
   * The distancefield to use in the product generation. When specifying this, it will use the
   * product generators default distance field.
   */
  private String distancefield = "eu.baltrad.composite.quality.distance.radar";
  
  /**
   * The parameter to use in the accumulation
   */
  private String quantity = "DBZH";
  
  /**
   * If GRA coefficients should be applied or not
   */
  private boolean applyGRA = false;
  
  /**
   * The filter used for matching files
   */
  private IFilter filter = null;

  /**
   * The matcher used for verifying the filter
   */
  private MetadataMatcher matcher;
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(AcrrRule.class);
  
  /**
   * Constructor
   */
  protected AcrrRule() {
    matcher = new MetadataMatcher();
  }
  
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
    return this.catalog;
  }
  
  /**
   * @param ruleUtil the ruleUtil to set
   */
  public void setRuleUtilities(IRuleUtilities ruleUtil) {
    this.ruleUtil = ruleUtil;
  }

  /**
   * @return the ruleUtil
   */
  public IRuleUtilities getRuleUtilities() {
    return ruleUtil;
  }
  
  /**
   * @param ruleid the ruleid to set
   */
  public void setRuleId(int ruleid) {
    this.ruleid = ruleid;
  }

  /**
   * @return the ruleid
   */
  public int getRuleId() {
    return ruleid;
  }
  
  /**
   * @param area the area to set
   */
  public void setArea(String area) {
    this.area = area;
  }
  
  /**
   * @return an area
   */
  public String getArea() {
    return this.area;
  }

  /**
   * @return the number of hours in the product
   */
  public int getHours() {
    return hours;
  }

  /**
   * @param hours the number of hours in the product
   */
  public void setHours(int hours) {
    this.hours = hours;
  }
  
  /**
   * @return the ZR-A relation
   */
  public double getZrA() {
    return zrA;
  }

  /**
   * @param zrA the ZR-A relation
   */
  public void setZrA(double zrA) {
    this.zrA = zrA;
  }

  /**
   * @return the ZR-B relation
   */
  public double getZrB() {
    return zrB;
  }

  /**
   * @param zrB the ZR-B relation
   */
  public void setZrB(double zrB) {
    this.zrB = zrB;
  }

  /**
   * The files included / hour. The actual number of in-products are actually one more.
   * This means that if filesPerHour = 4, the actual number of files in the product
   * will be 5 (e.g. 00:00,00:15,00:30,00:45,01:00). Note, that both 00:00 and 01:00 are included.   
   * @return the files included / hour (-1)
   */
  public int getFilesPerHour() {
    return filesPerHour;
  }

  /**
   * Sets the number of in-products / hour. Be aware that the actual number of used files
   * will be number_of_files_per_hour * hours + 1. For example, if filesPerHour = 4 and hours = 1,
   * the number of files will be 4 * 1 + 1. E.g. 00:00,00:15,00:30,00:45,01:00.
   * The number of files / hours will affect
   * Valid values are [1,2,3,4,6,12]  (=> 2,3,4,7,13 files)   
   * @param filesPerHour the number of in-products / hour (-1).
   * 
   */
  public void setFilesPerHour(int filesPerHour) {
    if (filesPerHour == 1 ||
        filesPerHour == 2 ||
        filesPerHour == 3 ||
        filesPerHour == 4 ||
        filesPerHour == 6 ||
        filesPerHour == 12) {
      this.filesPerHour = filesPerHour;
    } else {
      throw new IllegalArgumentException("Only valid values are 1,2,3,4,6,12");
    }
  }

  /**
   * The object type, either COMP or IMAGE
   * @return the object type
   */
  public String getObjectType() {
    return objectType;
  }

  /**
   * @param objectType the object type to use, either IMAGE or COMP
   */
  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }
  
  /**
   * The acceptable loss / pixel when generating the acrr product.
   * @return acceptable loss in percent (0-100)
   */
  public int getAcceptableLoss() {
    return acceptableLoss;
  }

  /**
   * The acceptable loss when generating a product. If for example, there are 10 files to be used in the
   * generation and acceptableLoss = 20, it means that the observations at that position can be used if
   * not more than 2 (20%) observations with nodata exists in that set.
   * @param acceptableLoss acceptable loss in percent (0-100)
   */
  public void setAcceptableLoss(int acceptableLoss) {
    if (acceptableLoss >= 0 && acceptableLoss <= 100) {
      this.acceptableLoss = acceptableLoss;
    } else {
      throw new IllegalArgumentException("Valid range is 0 - 100");
    }
  }

  /**
   * The distance field to use in the product generation. Default is eu.baltrad.composite.quality.distance.radar
   * @return the distance quality field
   */
  public String getDistancefield() {
    return distancefield;
  }

  /**
   * Sets the distance quality field
   * @param distancefield
   */
  public void setDistancefield(String distancefield) {
    this.distancefield = distancefield;
  }

  /**
   * @return the parameter (quantity) to use for the ACRR accumulation
   */
  public String getQuantity() {
    return quantity;
  }

  /**
   * @param quantity the parameter (quantity) to use in the accumulation
   */
  public void setQuantity(String quantity) {
    this.quantity = quantity;
  }
  
  /**
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (catalog == null ||
        ruleUtil == null) {
      throw new BeanInitializationException("catalog or ruleUtilities missing");
    }
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#getType()
   */
  @Override
  public String getType() {
    return TYPE;
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#isValid()
   */
  @Override
  public boolean isValid() {
    return true;
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    logger.debug("ENTER: handle(IBltMessage)");
    try {
      if (message instanceof BltTriggerJobMessage) {
        logger.info("ENTER: execute AcrrRule with ruleId: " + getRuleId() + ", thread: " + Thread.currentThread().getName());
        DateTime nowdt = ruleUtil.nowDT();
        if (((BltTriggerJobMessage)message).getScheduledFireTime() != null) {
          nowdt = ruleUtil.createDateTime(((BltTriggerJobMessage)message).getScheduledFireTime());
        }
        DateTime nt = ruleUtil.createNominalTime(nowdt, getFilesPerHourInterval());
        List<CatalogEntry> entries = findFiles(nowdt);
        List<String> uuids = ruleUtil.getUuidStringsFromEntries(entries);
        
        BltGenerateMessage result = new BltGenerateMessage();
        Date date = nt.getDate();
        Time time = nt.getTime();
        
        result.setAlgorithm("eu.baltrad.beast.GenerateAcrr");
        result.setFiles(uuids.toArray(new String[0]));
        List<String> args = new ArrayList<String>();
        args.add("--area="+area);
        args.add("--date="+RuleUtils.getFormattedDate(date));
        args.add("--time="+RuleUtils.getFormattedTime(time));
        args.add("--zra="+zrA);
        args.add("--zrb="+zrB);
        args.add("--hours="+hours);
        args.add("--N="+(filesPerHour * hours + 1));
        args.add("--accept="+ acceptableLoss);
        args.add("--quantity="+quantity);
        args.add("--distancefield=" + distancefield);
        if (isApplyGRA()) {
          args.add("--applygra=true");
        }
        result.setArguments(args.toArray(new String[0]));
        
        logger.debug("AcrrRule createMessage - entries: " +
            StringUtils.collectionToDelimitedString(uuids, " "));
        
        logger.info("EXIT: execute AcrrRule with ruleId: " + getRuleId() + ", thread: " + Thread.currentThread().getName());
        
        return result;
      }
    } 
    catch (Exception e) {
      logger.error("FAIL: execute AcrrRule with ruleId: " + getRuleId() + ", thread: " + Thread.currentThread().getName(), e);
    }
    finally {
      logger.debug("EXIT: handle(IBltMessage)");
    }
    return null;
  }
  
  /**
   * Returns a filtered list of all included files from now and hours backward.
   * The filtered list will only contain one file / what_time, what_date and the
   * files will be based on latest stored_time.
   * @param now the current time
   * @return the found unique entries.
   */
  protected List<CatalogEntry> findFiles(DateTime now) {
    DateTime endDt = ruleUtil.createNominalTime(now, getFilesPerHourInterval());
    Calendar c = ruleUtil.createCalendar(endDt);
    c.add(Calendar.HOUR, -hours);
    DateTime startDt = ruleUtil.createDateTime(c);
    TimeSelectionFilter filter = createFilter(startDt, endDt, getFilesPerHourInterval());
    return filterEntries(catalog.fetch(filter));
  }
  
  /**
   * Filters the entries so that only one date-time / slot occurs
   * @param entries the entries to filter
   * @return the filtered entries
   */
  protected List<CatalogEntry> filterEntries(List<CatalogEntry> entries) {
    Map<DateTime, CatalogEntry> remembered = new HashMap<DateTime, CatalogEntry>();
    for (CatalogEntry e : entries) {
      if (this.filter != null) {
        if (!matcher.match(e.getFileEntry().getMetadata(), this.filter.getExpression())) {
          continue;
        }
      }
      DateTime dt = e.getDateTime();
      if (remembered.containsKey(dt)) {
        CatalogEntry r = remembered.get(dt);
        if (compareStoredDateTime(e, r) <= 0) {
          remembered.put(dt, e);
        }
      } else {
        remembered.put(dt, e);
      }
    }
    
    return filterEntries(entries, remembered);
  }
  
  /**
   * Creates a new list of entries where the entry must exist in both entries and validEntries
   * @param entries the list of entries that should be filtered
   * @param validEntries the map of valid entries
   * @return a filtered list
   */
  protected List<CatalogEntry> filterEntries(List<CatalogEntry> entries, Map<DateTime,CatalogEntry> validEntries) {
    List<CatalogEntry> filtered = new ArrayList<CatalogEntry>();
    for (CatalogEntry e : entries) {
      CatalogEntry re = validEntries.get(e.getDateTime());
      if (re != null && re.getUuid().equals(e.getUuid())) {
        filtered.add(e);
      }
    }
    return filtered;
  }
  
  /**
   * Compares the stored date time between catalog entry 1 and 2
   * @param e1 entry 1
   * @param e2 entry 2
   * @return a negative integer, zero, or a positive integer if the stored time for e1 is less than, equal to, or greater than e2.
   */
  protected int compareStoredDateTime(CatalogEntry e1, CatalogEntry e2) {
    FileEntry e1fe = e1.getFileEntry();
    FileEntry e2fe = e2.getFileEntry();
    DateTime e1dt = new DateTime(e1fe.getStoredDate(),e1fe.getStoredTime());
    DateTime e2dt = new DateTime(e2fe.getStoredDate(),e2fe.getStoredTime());
    Calendar c1 = ruleUtil.createCalendar(e1dt);
    Calendar c2 = ruleUtil.createCalendar(e2dt);
    return c1.compareTo(c2);
  }
  
  /**
   * Returns true if d1 is <= d2, otherwise false
   * @param d1 date time 1
   * @param d2 date time 2
   * @return true if d1 is <= d2, otherwise false
   */
  protected boolean isLessOrEqual(DateTime d1, DateTime d2) {
    Calendar c1 = ruleUtil.createCalendar(d1);
    Calendar c2 = ruleUtil.createCalendar(d2);
    return c1.compareTo(c2) <= 0;
  }

  /**
   * Creates the time selection filter to be used in this generation.
   * @param sdt The first date time in the interval
   * @param edt the last date time in the interval
   * @param interval the interval
   * @return the filter
   */
  protected TimeSelectionFilter createFilter(DateTime sdt, DateTime edt, int interval) {
    TimeSelectionFilter f = new TimeSelectionFilter();
    f.setObjectType(objectType);
    f.setSource(area);
    List<String> quants = new ArrayList<String>();
    quants.add("ACRR");
    f.exclude("what/quantity", quants);
    DateTime nxt = new DateTime(sdt.getDate(), sdt.getTime());
    TimeDelta delta = new TimeDelta().addSeconds(60 * interval);
    while (isLessOrEqual(nxt,edt)) {
      f.addDateTime(nxt);
      nxt = nxt.add(delta);
    }
    return f;
  }
  
  /**
   * Returns the interval corresponding to the number of files / hour. E.g.
   * 1 means 60 minute interval, 2 means 30 minute, 3 means 20 and so on 
   * @return the interval to use depending on the filesPerHour setting
   */
  protected int getFilesPerHourInterval() {
    // 1,2,3,4,6,12
    if (filesPerHour == 1) {
      return 60;
    } else if (filesPerHour == 2) {
      return 30;
    } else if (filesPerHour == 3) {
      return 20;
    } else if (filesPerHour == 4) {
      return 15;
    } else if (filesPerHour == 6) {
      return 10;
    } else if (filesPerHour == 12) {
      return 5;
    }
    throw new IllegalArgumentException("Should not be possible to call getInterval with bad filesPerHour setting");
  }

  /**
   * @return If GRA coefficients should be applied or not 
   */
  public boolean isApplyGRA() {
    return applyGRA;
  }

  /**
   * @param applyGRA If GRA coefficients should be applied or not
   */
  public void setApplyGRA(boolean applyGRA) {
    this.applyGRA = applyGRA;
  }
  

  /**
   * @returns the filter to use when trying out if files are matching
   */
  public IFilter getFilter() {
    return filter;
  }

  /**
   * @param filter the filter to use
   */
  public void setFilter(IFilter filter) {
    this.filter = filter;
  }

  /**
   * @return the metadata matcher
   */
  public MetadataMatcher getMatcher() {
    return matcher;
  }

  /**
   * @param matcher the metadata matcher
   */
  public void setMatcher(MetadataMatcher matcher) {
    this.matcher = matcher;
  }
}
