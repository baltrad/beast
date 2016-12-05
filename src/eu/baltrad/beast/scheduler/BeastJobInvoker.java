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

import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import eu.baltrad.beast.manager.IBltMessageManager;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;

/**
 * Will trigger the IBeastJob that has been stored in the jobdetail.
 * @author Anders Henja
 */
public class BeastJobInvoker implements Job {
  private static Logger logger = LogManager.getLogger(BeastJobInvoker.class);
  
  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @Override
  public void execute(JobExecutionContext ctx) throws JobExecutionException {
    JobDetail detail = ctx.getJobDetail() ;
    IBltMessageManager mgr = (IBltMessageManager)detail.getJobDataMap().get("messageManager");
    String id = ctx.getTrigger().getKey().getName();
    String name = detail.getKey().getName();
    logger.debug("Running triggered job message with id="+id+" and name="+name);
    Date scheduledFireTime = ctx.getScheduledFireTime();
    Date fireTime = ctx.getFireTime();
    Date prevFireTime = ctx.getPreviousFireTime();
    Date nextFireTime = ctx.getNextFireTime();
    
    BltTriggerJobMessage msg = createMessage(id, name, scheduledFireTime, fireTime, prevFireTime, nextFireTime);
    mgr.manage(msg);
  }
  
  /**
   * Creates a scheduled job message
   * @param id the id
   * @param name the name
   * @return the message
   */
  protected BltTriggerJobMessage createMessage(String id, String name, Date scheduledFireTime, Date fireTime, Date prevFireTime, Date nextFireTime) {
    BltTriggerJobMessage result = new BltTriggerJobMessage();
    result.setId(id);
    result.setName(name);
    result.setScheduledFireTime(scheduledFireTime);
    result.setFireTime(fireTime);
    result.setPrevFireTime(prevFireTime);
    result.setNextFireTime(nextFireTime);
    return result;
  }
}
