/* --------------------------------------------------------------------
Copyright (C) 2009-2010 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.manager;

import eu.baltrad.beast.pgfwk.IGeneratorPlugin;

/**
 * Used for testing
 * @author Anders Henja
 */
public class TimingGeneratorPlugin implements IGeneratorPlugin {
  /**
   * The sum of all begin-end sequences;
   */
  private long totaltime = 0;
  
  /**
   * The number of times totaltime has been increased
   */
  private long nrtimes = 0;
  
  /**
   * @see eu.baltrad.beast.pgfwk.IGeneratorPlugin#generate(java.lang.String, java.lang.String[], java.lang.Object[])
   */
  @Override
  public synchronized void generate(String algorithm, String[] files, Object[] arguments) {
    Long btime = Long.parseLong((String)arguments[0]);
    long difftime = System.currentTimeMillis() - btime;
    totaltime += difftime;
    nrtimes++;
    notify();
  }

  /**
   * Waits the specified timeout period for the generate call to be called
   * nrtimes or until the timeout occur
   * @param nrtimes the nr of times the function should be called
   * @param timeout the timeout in ms
   * @return the nr of times
   */
  public synchronized long waitForResponse(long nrtimes, long timeout) {
    long currtime = System.currentTimeMillis();
    long endtime = currtime + timeout;
    while (this.nrtimes != nrtimes && (currtime < endtime)) {
      try {
        wait(endtime - currtime);
      } catch (Throwable t) {
      }
      currtime = System.currentTimeMillis();
    }
    return this.nrtimes;
  }
  
  /**
   * Resets the settings
   */
  public synchronized void reset() {
    totaltime = 0;
    nrtimes = 0;
    notify();
  }
  
  /**
   * @return the total time
   */
  public long getTotaltime() {
    return this.totaltime;
  }
}
