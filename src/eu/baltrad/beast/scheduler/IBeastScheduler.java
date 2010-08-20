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
package eu.baltrad.beast.scheduler;

import java.util.List;

/**
 * @author Anders Henja
 */
public interface IBeastScheduler {
  /**
   * Registers a job
   * @param cron - the cron expression
   * @param jobName - the name of the job to be executed
   * @returns the id for this scheduled job
   */
  public int register(String cron, String jobName);
  
  /**
   * Unregisters the specified job
   * @param id the jobid
   */
  public void unregister(int id);
  
  /**
   * @return the schedule
   */
  public List<CronEntry> getSchedule();
  
  /**
   * Returns the schedule for the specified job
   * @param job the job
   * @return the schedule
   */
  public List<CronEntry> getSchedule(String job);
  
  /**
   * Returns the entry for the specified id
   * @param id the id
   * @return the entry or null if not found
   */
  public CronEntry getEntry(int id);
}
