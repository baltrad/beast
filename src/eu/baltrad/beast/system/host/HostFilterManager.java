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

package eu.baltrad.beast.system.host;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

/**
 * @author Anders Henja
 *
 */
public class HostFilterManager implements IHostFilterManager, InitializingBean {
  private static class FilterEntry {
    String str;
    Pattern pattern;
    FilterEntry(String s, Pattern p) {
      str = s;
      pattern = p;
    }
    boolean matches(String s) {
      return pattern.matcher(s).matches();
    }
    String getStringPattern() {
      return str;
    }
  };
  /**
   * The db access
   */
  private SimpleJdbcOperations template = null;

  /**
   * The patterns
   */
  private List<FilterEntry> patterns = null;
  
  /**
   * Only allow patterns that matches N.N.N.N where N can be either * or [0-9]{1,3} 
   */
  private static Pattern VALID_PATTERN = Pattern.compile("^(([0-9]{1,3})|\\*)\\.(([0-9]{1,3})|\\*)\\.(([0-9]{1,3})|\\*)\\.(([0-9]{1,3})|\\*)$"); 
  
  /**
   * Constructor
   */
  public HostFilterManager() {
    patterns = new ArrayList<FilterEntry>();
  }
  
  /**
   * @param template the template to use
   */
  public void setTemplate(SimpleJdbcOperations template) {
    this.template = template;
  }
  
  /**
   * @see eu.baltrad.beast.system.host.IHostFilterManager#add(java.lang.String)
   */
  @Override
  public synchronized void add(String ipfilter) {
    if (isValidPattern(ipfilter)) {
      template.update("INSERT INTO beast_host_filter (name) VALUES (?)", ipfilter);
      patterns.add(new FilterEntry(ipfilter, Pattern.compile(ipfilter.replaceAll("\\*", "[0-9]{1,3}"))));
    } else {
      throw new IllegalArgumentException("Illegal format for ipfilter: " + ipfilter);
    }
  }

  /**
   * @see eu.baltrad.beast.system.host.IHostFilterManager#remove(java.lang.String)
   */
  @Override
  public synchronized void remove(String ipfilter) {
    if (ipfilter != null) {
      template.update("DELETE FROM beast_host_filter WHERE name = ?", ipfilter);
      for (int i = 0; i < patterns.size(); i++) {
        if (patterns.get(i).getStringPattern().equals(ipfilter)) {
          patterns.remove(i);
          break;
        }
      }
    } else {
      throw new NullPointerException();
    }
  }

  /**
   * @see eu.baltrad.beast.system.host.IHostFilterManager#accepted(java.lang.String)
   */
  @Override
  public synchronized boolean accepted(String ip) {
    for (FilterEntry e : patterns) {
      if (e.matches(ip)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @see eu.baltrad.beast.system.host.IHostFilterManager#isRegistered(java.lang.String)
   */
  @Override
  public boolean isRegistered(String ipfilter) {
    for (FilterEntry e : patterns) {
      if (e.getStringPattern().equals(ipfilter)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * @see @see eu.baltrad.beast.system.host.IHostFilterManager##getPatterns()
   */
  @Override
  public synchronized List<String> getPatterns() {
    List<String> result = new ArrayList<String>();
    for (FilterEntry e : patterns) {
      result.add(e.getStringPattern());
    }
    return result;
  }

  /**
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    List<String> strs = template.query("SELECT name FROM beast_host_filter", getEntryMapper());
    for (String s : strs) {
      if (isValidPattern(s)) {
        patterns.add(new FilterEntry(s, Pattern.compile(s.replaceAll("\\*", "[0-9]{1,3}"))));
      }
    }
  }
  
  /**
   * Returns the row mapper for mapping entries
   * @return
   */
  protected ParameterizedRowMapper<String> getEntryMapper() {
    return new ParameterizedRowMapper<String>() {
      @Override
      public String mapRow(ResultSet rs, int rown) throws SQLException {
        return rs.getString("name");
      }
    };
  }
  
  /**
   * Checks if the pattern is valid.
   * @param pattern the pattern
   * @return if valid or not
   */
  protected boolean isValidPattern(String pattern) {
    Matcher m = VALID_PATTERN.matcher(pattern);
    return m.matches();
  }
}
