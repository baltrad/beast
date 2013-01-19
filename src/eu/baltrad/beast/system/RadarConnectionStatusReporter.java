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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Anders Henja
 *
 */
public class RadarConnectionStatusReporter implements ISystemStatusReporter {
  /**
   * The remembered status for the radars
   */
  private Map<String, SystemStatus> radarStatus = new HashMap<String, SystemStatus>();
  
  /**
   * The logger
   */
  private final static Logger logger = LogManager.getLogger(RadarConnectionStatusReporter.class);
  
  /**
   * @see eu.baltrad.beast.system.ISystemStatusReporter#getName()
   */
  @Override
  public String getName() {
    return "radar";
  }

  /**
   * @see eu.baltrad.beast.system.ISystemStatusReporter#getStatus(java.lang.String[])
   */
  @Override
  public Set<SystemStatus> getStatus(String... args) {
    logger.info("getStatus(String...)");
    Set<SystemStatus> result = new HashSet<SystemStatus>();
    for (String str : args) {
      String[] tokens = tokenizeString(str);
      for (String str2 : tokens) {
        if (radarStatus.containsKey(str2)) {
          result.add(radarStatus.get(str2));
        } else {
          result.add(SystemStatus.UNDEFINED);
        }
      }
    }
    if (result.size() == 0) {
      result.add(SystemStatus.UNDEFINED);
    }
    return EnumSet.copyOf(result);
  }
  
  /**
   * Sets the status for the node (radar)
   * @param node the radar
   * @param status the status
   */
  public void setStatus(String node, SystemStatus status) {
    radarStatus.put(node, status);
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
