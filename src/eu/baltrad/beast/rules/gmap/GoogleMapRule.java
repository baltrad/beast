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

package eu.baltrad.beast.rules.gmap;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.Time;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.RuleUtils;

/**
 * The google map rule
 * @author Anders Henja
 * @date 2012-03-21
 */
public class GoogleMapRule implements IRule, InitializingBean {
  public final static String BEAST_GMAP_ALGORITHM = "eu.baltrad.beast.creategmap";
  
  /**
   * The name of this static google map type
   */
  public final static String TYPE = "blt_gmap";

  /**
   * The path that should point out where the result should be placed
   */
  private String path = null;
  
  /**
   * The area for this google map rule
   */
  private String area = null;

  /**
   * The catalog for database access
   */
  private Catalog catalog = null;
  
  /**
   * The unique rule id separating this compositing rule from the others.
   */
  private int ruleid = -1;
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(GoogleMapRule.class);

	/**
	 *  @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
    if (catalog == null) {
      throw new BeanInitializationException("catalog missing");
    }
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
	 * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
	 */
	@Override
	public IBltMessage handle(IBltMessage message) {
	  logger.debug("ENTER: handle(IBltMessage)");
	  BltGenerateMessage result = null;
	  
	  try {
	    if (isValid() && message != null && message instanceof BltDataMessage) {
	      FileEntry fe = ((BltDataMessage)message).getFileEntry();
	      logger.info("ENTER: execute ruleId: " + getRuleId() + ", thread: " + Thread.currentThread().getName() + 
            ", file: " + fe.getUuid());
	      String object = fe.getMetadata().getWhatObject();
	      if (object != null && object.equals("COMP")) {
	        String source = fe.getMetadata().getWhatSource();
	        Date d = fe.getMetadata().getWhatDate();
	        Time t = fe.getMetadata().getWhatTime();
	        if (isSupportedArea(source)) {
	          logger.info("Creating google map image message: " + area);
	          String oname = createOutputName(d, t);
	          result = new BltGenerateMessage();
	          result.setAlgorithm(BEAST_GMAP_ALGORITHM);
	          result.setFiles(new String[]{fe.getUuid().toString()});
	          List<String> args = new ArrayList<String>();
	          args.add("--outfile="+oname);
	          args.add("--date="+RuleUtils.getFormattedDate(d)); 
	          args.add("--time="+RuleUtils.getFormattedTime(t));
	          args.add("--algorithm_id="+getRuleId());
	          result.setArguments(args.toArray(new String[0]));
	        }
	      }
	      logger.info("EXIT: execute ruleId: " + getRuleId() + ", thread: " + Thread.currentThread().getName() + 
            ", file: " + fe.getUuid());
	    }
	  } finally {
	    logger.debug("EXIT: handle(IBltMessage)");
	  }
	  return result;
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
	  if (area != null && area.length() > 0) {
	    return true;
	  }
	  return false;
	}

  /**
   * @param path the path to set
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * @param area the area to set
   */
  public void setArea(String area) {
    this.area = area;
  }

  /**
   * @return the area
   */
  public String getArea() {
    return area;
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
    return catalog;
  }

  /**
   * Checks if a wanted area exists in this source and in that case
   * return the name of that area.
   * @param source the source to check
   * @return an area if it could be found in the source otherwise null
   */
  protected boolean isSupportedArea(String source) {
    if (source.matches(".*:"+area+"(,.*|$)")) {
      return true;
    }
    return false;
  }
  
  /**
   * Creates the absolute file path to be used for the generated file.
   * @param area the area
   * @param d the date
   * @param t the time
   * @return the absolute file path
   */
  protected String createOutputName(Date d, Time t) {
    String pp = "";
    if (path != null) {
      pp = path;
    }
    return String.format("%s/%s/%04d/%02d/%02d/%04d%02d%02d%02d%02d.png",
        pp, area, d.year(), d.month(), d.day(), d.year(), d.month(), d.day(), t.hour(), t.minute());
  }
}
