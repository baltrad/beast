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
   * The time when the last call to begin() was performed
   */
  private long begintime = 0;
  
  /**
   * The time when the last call to end() was performed
   */
  private long endtime = 0;
  
  /**
   * The sum of all begin-end sequences;
   */
  private long totaltime = 0;
  
  /**
   * The number of times begin has been folloed by end
   */
  private long nrbeginend = 0;
  
  /**
   * @see eu.baltrad.beast.pgfwk.IGeneratorPlugin#generate(java.lang.String, java.lang.String[], java.lang.Object[])
   */
  @Override
  public void generate(String algorithm, String[] files, Object[] arguments) {
    // TODO Auto-generated method stub
    
  }
  
  /**
   * Resets the time counter
   */
  public void begin() {
    begintime = System.currentTimeMillis();
  }

  /**
   * Stops the time counter
   */
  public void end() {
    endtime = System.currentTimeMillis();
    totaltime += (endtime - begintime);
    nrbeginend++;
  }

  /**
   * Returns the number of times begin has been followed by end
   * @return
   */
  public long getNrTimes() {
    return nrbeginend;
  }
  
  /**
   * Returns the average time consumption
   * @return the average time consumption
   */
  public long getAverageTime() {
    if (nrbeginend > 0) {
      return (long)totaltime/nrbeginend;
    } else {
      return 0;
    }
  }
}
