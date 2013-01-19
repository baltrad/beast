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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Used for grouping a number of reporters into a combined version. For example
 * a system health check can be performed with system=bdb,db and there are two
 * different reporters, one for bdb and the other for db. Then you can create a
 * grouped status reporter that will check all reporters for their status.
 * @author Anders Henja
 */
public class GroupedStatusReporter implements ISystemStatusReporter {
  /**
   * The name this group has
   */
  private String name;
  
  /**
   * The grouped reporters.
   */
  private Map<String, ISystemStatusReporter> reporters;
  
  /**
   * Constructor
   * @param name the name this group should be known as
   */
  public GroupedStatusReporter(String name) {
    this.name = name;
  }

  /**
   * The reporters that are set must be able to handle a null argument since the
   * provided args supported by the reporter is name={reporter 1 name}, {reporter 2 name}, and so on
   * @param reporters
   */
  public void setReporters(Map<String, ISystemStatusReporter> reporters) {
    this.reporters = reporters;
  }
  
  /**
   * @see eu.baltrad.beast.system.ISystemStatusReporter#getName()
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * @see eu.baltrad.beast.system.ISystemStatusReporter#getStatus(java.lang.String[])
   */
  @Override
  public Set<SystemStatus> getStatus(String... args) {
    Set<SystemStatus> result = new HashSet<SystemStatus>();
    for (String str : args) {
      String[] tokens = tokenizeString(str);
      for (String str2 : tokens) {
        if (reporters.containsKey(str2)) {
          result.addAll(reporters.get(str2).getStatus());
        } else {
          result.add(SystemStatus.UNDEFINED);
        }
      }
    }
    if (result.size() == 0) {
      result.add(SystemStatus.UNDEFINED);
    }
    return result;
  }
  
  /**
   * Returns an array of tokens (comma separated)
   * @param str the string to tokenize
   * @return an array of tokens (always != null)
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
