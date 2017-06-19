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

package eu.baltrad.beast.system;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import eu.baltrad.bdb.FileCatalog;
import eu.baltrad.bdb.db.FileQuery;
import eu.baltrad.bdb.db.FileResult;
import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.expr.ExpressionFactory;
import eu.baltrad.bdb.util.DateTime;

/**
 * @author Anders Henja
 */
public class BdbObjectStatusReporter implements ISystemStatusReporter{
  /**
   * The expression factory
   */
  private ExpressionFactory xpr = new ExpressionFactory();

  /**
   * The file catalog
   */
  private FileCatalog fc = null;
  
  /**
   * The supported attributes
   */
  private static Set<String> SUPPORTED_ATTRIBUTES=new HashSet<String>();
  static {
    SUPPORTED_ATTRIBUTES.add("minutes");
    SUPPORTED_ATTRIBUTES.add("objects");
    SUPPORTED_ATTRIBUTES.add("sources");
    SUPPORTED_ATTRIBUTES.add("areas");
    SUPPORTED_ATTRIBUTES.add("what/*");
    SUPPORTED_ATTRIBUTES.add("where/*");
    SUPPORTED_ATTRIBUTES.add("how/*");
  }
  
  /**
   * @see eu.baltrad.beast.system.ISystemStatusReporter#getName()
   */
  @Override
  public String getName() {
    return "bdb.object.status";
  }

  /**
   * Sets the file catalog to use for querying
   * @param fc the file catalog
   */
  @Autowired
  public void setFileCatalog(FileCatalog fc) {
    this.fc = fc;
  }
  
  /**
   * @see eu.baltrad.beast.system.ISystemStatusReporter#getSupportedAttributes()
   */
  @Override
  public Set<String> getSupportedAttributes() {
    return SUPPORTED_ATTRIBUTES;
  }
  
  /**
   * Supports the value mapping. {
   * "objects",<string list>; "sources",<string list>; "areas",<string list>; "minutes",<integer,long or string>
   * @see eu.baltrad.beast.system.ISystemStatusReporter#getStatus()
   */
  @Override
  public Set<SystemStatus> getStatus(Map<String,Object> values) {
    Set<SystemStatus> result = EnumSet.of(SystemStatus.UNDEFINED);
    FileQuery query = createFileQuery();
    Expression expr = createSearchFilter(values);
    query.setLimit(1);
    query.setFilter(expr);
    FileResult set = fc.getDatabase().execute(query);
    try {
      if (set.next()) {
        result = EnumSet.of(SystemStatus.OK);
      } else {
        result = EnumSet.of(SystemStatus.COMMUNICATION_PROBLEM);
      }
    } finally {
      if (set != null) {
        set.close();
      }
    }
    
    return result;
  }

  /**
   * @return a new file query instance
   */
  protected FileQuery createFileQuery() {
    return new FileQuery();
  }
  
  /**
   * Creates the search filter to be used for generation
   * @param args the arguments
   * @return the expression
   */
  protected Expression createSearchFilter(Map<String,Object> values) {
    int minute = 5;
    Object tlimit = values.get("minutes");
    String srcstr = (String)values.get("sources");
    String areastr = (String)values.get("areas");
    String prodstr = (String)values.get("objects");
    Expression dtfilter = null;
    List<Expression> srcfilter = null;
    List<Expression> prodfilter = null;
    List<Expression> qfilter = new ArrayList<Expression>();
    
    if (tlimit != null) {
      if (tlimit instanceof String) {
        try {
          minute = Integer.parseInt((String)tlimit);
        } catch (Exception e) {
          // nothing to do... we use default minutes
          e.printStackTrace();
        }
      } else if (tlimit instanceof Long) {
        // downcast ok
        minute = ((Long)tlimit).intValue();
      } else if (tlimit instanceof Integer) {
        minute = (Integer)tlimit;
      } // else use default value
    }
    DateTime dt = createFromDateTime(minute);
    dtfilter = createDateTimeFilter(dt);
    srcfilter = createSourceList(srcstr, areastr);
    if (prodstr != null) {
      prodfilter = createProductsList(prodstr);
    }

    qfilter.add(dtfilter);
    if (srcfilter != null) {
      qfilter.add(xpr.or(srcfilter));
    }
    if (prodfilter != null) {
      qfilter.add(xpr.or(prodfilter));
    }
    
    // Handle optional attributes
    for (String k : values.keySet()) {
      String lowerCaseK = k.toLowerCase();
      if (lowerCaseK.startsWith("what/") || lowerCaseK.startsWith("where/") || lowerCaseK.startsWith("how/")) {
        //List<Expression> filters = new ArrayList<Expression>();
        qfilter.add(xpr.eq(xpr.attribute(lowerCaseK), xpr.literal((String)values.get(k))));
      }
    }
    
    return xpr.and(qfilter);
  }
  
  /**
   * Extracts group with index grp from the first argument that matches the pattern
   * @param pattern the pattern
   * @param grp the group index
   * @param args the list of arguments
   * @return the extracted string value
   */
  protected String extractStringValue(Pattern pattern, int grp, String... args) {
    for (String s : args) {
      Matcher m = pattern.matcher(s);
      if (m.matches()) {
        return m.group(1);
      }
    }
    return null;
  }
  
  /**
   * Creates a date time that is minutes back in time.
   * @param minutes the minutes back in time
   * @return the date time
   */
  protected DateTime createFromDateTime(int minutes) {
    GregorianCalendar c = createCalendar();
    c.add(Calendar.MINUTE, 0 - minutes);
    return new DateTime(c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH) + 1,
                        c.get(Calendar.DAY_OF_MONTH),
                        c.get(Calendar.HOUR_OF_DAY),
                        c.get(Calendar.MINUTE),
                        0);
  }
  
  /**
   * Creates a gregorian calendar instance
   * @return the gregorian calendar
   */
  protected GregorianCalendar createCalendar() {
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
    return calendar;
  }
  
  /**
   * Creates the >= date time filter
   * @param dt the from date time 
   * @return the expression
   */
  protected Expression createDateTimeFilter(DateTime dt) {
    Expression x1 = xpr.combinedDateTime("what/date", "what/time");
    Expression x2 = xpr.literal(dt);
    return xpr.ge(x1, x2);
  }
  
  /**
   * Creates a list of source expressions from a comma-separated list of sources
   * @param src the list of sources
   * @return the list
   */
  protected List<Expression> createSourceList(String sources, String areas) {
    List<Expression> filters = new ArrayList<Expression>();
    
    if (sources != null) {
      String[] asources = tokenizeString(sources);
      for (String s : asources) {
        // We try both source name and the CMT field.
        filters.add(xpr.or(xpr.eq(xpr.attribute("_bdb/source_name"), xpr.literal(s)),
                           xpr.eq(xpr.attribute("what/source:CMT"), xpr.literal(s))));
      }
    }
    if (areas != null) {
      String[] aareas = tokenizeString(areas);
      for (String s : aareas) {
        // We try both source name and the CMT field.
        filters.add(xpr.or(xpr.eq(xpr.attribute("_bdb/source_name"), xpr.literal(s)),
                           xpr.eq(xpr.attribute("what/source:CMT"), xpr.literal(s))));
      }
      
    }

    return filters;
  }
  
  /**
   * Creates a list of product expressions from a comma-separated list of products
   * @param prod the list of products
   * @return the list
   */
  protected List<Expression> createProductsList(String prod) {
    List<Expression> filters = new ArrayList<Expression>();
    
    String[] products = tokenizeString(prod);
    for (String s : products) {
      filters.add(xpr.eq(xpr.attribute("what/object"), xpr.literal(s)));
    }

    return filters;
  }

  /**
   * Tokenizes a comma separated string.
   * @param str the string to be tokenized
   * @return the tokens
   */
  protected String[] tokenizeString(String str) {
    StringTokenizer tok = new StringTokenizer(str, ",");
    int ntoks = tok.countTokens();
    String[] result = new String[ntoks];
    int idx = 0;
    while (tok.hasMoreTokens()) {
      result[idx++] = tok.nextToken();
    }
    return result;
  }
}
