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
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
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
 *
 */
public class BdbProductStatusReporter implements ISystemStatusReporter{
  /**
   * minutes=xx
   */
  private final static Pattern MINUTE_PATTERN=Pattern.compile("^minutes=([0-9]+)$");
  
  /**
   * products pattern, products=(SCAN|PVOL|COMP)(,(SCAN|PVOL|COMP))*
   */
  private final static Pattern PRODUCTS_PATTERN=Pattern.compile("^products=(.*)$");

  /**
   * products pattern, products=(src)(,(src))*
   */
  private final static Pattern SOURCES_PATTERN=Pattern.compile("^sources=(.*)$");

  /**
   * The expression factory
   */
  private ExpressionFactory xpr = new ExpressionFactory();

  /**
   * The file catalog
   */
  private FileCatalog fc = null;
  
  /**
   * @see eu.baltrad.beast.system.ISystemStatusReporter#getName()
   */
  @Override
  public String getName() {
    return "bdb_product_status";
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
   * @see eu.baltrad.beast.system.ISystemStatusReporter#getStatus(java.lang.String[])
   */
  @Override
  public Set<SystemStatus> getStatus(String... args) {
    Set<SystemStatus> result = EnumSet.of(SystemStatus.UNDEFINED);
    FileQuery query = createFileQuery();
    Expression expr = createSearchFilter(args);
    
    query.setLimit(1);
    query.setFilter(expr);

    FileResult set = fc.getDatabase().execute(query);
    if (set.next()) {
      result = EnumSet.of(SystemStatus.OK);
    } else {
      result = EnumSet.of(SystemStatus.COMMUNICATION_PROBLEM);
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
  protected Expression createSearchFilter(String... args) {
    int minute = 5;
    String tlimit = extractStringValue(MINUTE_PATTERN, 1, args);
    String srcstr = extractStringValue(SOURCES_PATTERN, 1, args);
    String prodstr = extractStringValue(PRODUCTS_PATTERN, 1, args);
    Expression dtfilter = null;
    List<Expression> srcfilter = null;
    List<Expression> prodfilter = null;
    List<Expression> qfilter = new ArrayList<Expression>();
    
    if (tlimit != null) {
      try {
        minute = Integer.parseInt(tlimit);
      } catch (Exception e) {
        // nothing to do... we use default minutes
        e.printStackTrace();
      }
    }
    DateTime dt = createFromDateTime(minute);
    dtfilter = createDateTimeFilter(dt);
    if (srcstr != null) {
      srcfilter = createSourceList(srcstr);
    }
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
    return new GregorianCalendar();
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
  protected List<Expression> createSourceList(String src) {
    List<Expression> filters = new ArrayList<Expression>();
    
    String[] products = tokenizeString(src);
    for (String s : products) {
      // We try both source name and the CMT field.
      filters.add(xpr.or(xpr.eq(xpr.attribute("_bdb/source_name"), xpr.literal(s)),
                         xpr.eq(xpr.attribute("what/source:CMT"), xpr.literal(s))));
      //filters.add(xpr.eq(xpr.attribute("_bdb/source_name"), xpr.literal(s)));
      //filters.add(xpr.eq(xpr.attribute("what/source:CMT"), xpr.literal(s)));
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
