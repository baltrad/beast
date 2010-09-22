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
   * Number of times generate has been called since last reset
   */
  private volatile long ntimes = 0;
  
  /**
   * The total time it took from client to server provided that arguments contains
   * the clients send time.
   */
  private long totaltime = 0;
  
  /**
   * Indicator that we should reset
   */
  private volatile boolean reset = false;
  
  /**
   * @see eu.baltrad.beast.pgfwk.IGeneratorPlugin#generate(java.lang.String, java.lang.String[], java.lang.Object[])
   */
  @Override
  public synchronized void generate(String algorithm, String[] files, Object[] arguments) {
    try {
      Long btime = Long.parseLong((String)arguments[0]);
      long difftime = System.currentTimeMillis() - btime;
      totaltime += difftime;
    } catch (Throwable t) {
      t.printStackTrace();
    }
    ntimes = ntimes + 1;
    notifyAll();
  }
  
  /**
   * Waits the specified time or until generate has been called
   * nrtimes since last reset
   * @param nrtimes the number of times generate should have been called
   * @param timeout the time to wait in ms.
   * @return the number of times
   */
  public synchronized long waitForResponse(long nrtimes, long timeout) {
    long currtime = System.currentTimeMillis();
    long endtime = currtime + timeout;
    reset = false;
    
    while (nrtimes != this.ntimes && currtime < endtime && !reset) {
      try {
        wait(endtime - currtime);
      } catch (Throwable t) {
        t.printStackTrace();
      }
      currtime = System.currentTimeMillis();
    }
    if (nrtimes != this.ntimes) {
      System.out.println("generate called " + ntimes + " / " + nrtimes);
    }
    if (currtime >= endtime) {
      System.out.println("timeout occured");
    }
    return this.ntimes;
  }
  
  public synchronized void reset() {
    ntimes = 0;
    reset = true;
    totaltime = 0;
  }
  
  public synchronized long getTotalTime() {
    return totaltime;
  }
  
//  /**
//   * The sum of all begin-end sequences;
//   */
//  private volatile long totaltime = 0;
//  
//  /**
//   * The number of times totaltime has been increased
//   */
//  private volatile long nrtimes = 0;
//  
//  /**
//   * @see eu.baltrad.beast.pgfwk.IGeneratorPlugin#generate(java.lang.String, java.lang.String[], java.lang.Object[])
//   */
//  @Override
//  public synchronized void generate(String algorithm, String[] files, Object[] arguments) {
//    Long btime = Long.parseLong((String)arguments[0]);
//    long difftime = System.currentTimeMillis() - btime;
//    totaltime += difftime;
//    nrtimes++;
//    System.out.println("Generate (nrtimes="+nrtimes+"): " + Thread.currentThread().getName());
//    notifyAll();
//  }
//
//  /**
//   * Waits the specified timeout period for the generate call to be called
//   * nrtimes or until the timeout occur
//   * @param nrtimes the nr of times the function should be called
//   * @param timeout the timeout in ms
//   * @return the nr of times
//   */
//  public synchronized long waitForResponse(long nrtimes, long timeout) {
//    long currtime = System.currentTimeMillis();
//    long endtime = currtime + timeout;
//    while (this.nrtimes != nrtimes && (currtime < endtime)) {
//      System.out.println("Waiting for " + (endtime - currtime) + " ms");
//      try {
//        wait(endtime - currtime);
//      } catch (Throwable t) {
//      }
//      currtime = System.currentTimeMillis();
//      System.out.println("new time, currtime = "+currtime + "endtime = " + endtime);
//    }
//    System.out.println("Returning nrtimes: " + this.nrtimes + " when expected="+nrtimes);
//    return this.nrtimes;
//  }
//  
//  /**
//   * Resets the settings
//   */
//  public synchronized void reset() {
//    totaltime = 0;
//    nrtimes = 0;
//    notifyAll();
//  }
//  
//  /**
//   * @return the total time
//   */
//  public synchronized long getTotaltime() {
//    return this.totaltime;
//  }
}
