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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.JobDetailBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import eu.baltrad.beast.manager.IBltMessageManager;

/**
 * @author Anders Henja
 *
 */
public class BeastScheduler implements IBeastScheduler, InitializingBean {
  /**
   * Injected factory bean...
   */
  private SchedulerFactoryBean sf = null;

  /**
   * The message manager.
   */
  private IBltMessageManager messageManager = null;
  
  /**
   * Constructor
   */
  public BeastScheduler() {
    this.sf = null;
  }
  
  /**
   * Sets the scheduler factory bean
   * @param sfb
   */
  public void setSchedulerFactoryBean(SchedulerFactoryBean sf) {
    this.sf = sf;
  }

  /**
   * @param mgr the message manager to set
   */
  public void setMessageManager(IBltMessageManager mgr) {
    this.messageManager = mgr;
  }
  
  /**
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (sf == null) {
      sf = new SchedulerFactoryBean();
      sf.afterPropertiesSet();
    }
  }

  /**
   * @see eu.baltrad.beast.scheduler.IBeastScheduler#register(java.lang.String, java.lang.String, eu.baltrad.beast.scheduler.IBeastJob)
   */
  @Override
  public void register(String id, String cron, IBeastJob job) {
    try {
      CronTriggerBean trigger = createTrigger(id, cron);
      JobDetailBean jobDetail = createJob(id, job);
      sf.getScheduler().scheduleJob(jobDetail, trigger);
    } catch (org.quartz.SchedulerException t) {
      throw new SchedulerException(t);
    }
  }
  

  /**
   * @see eu.baltrad.beast.scheduler.IBeastScheduler#unregister(java.lang.String)
   */
  @Override
  public void unregister(String id) {
    try {
      sf.getScheduler().deleteJob(id, "beast");
    } catch (org.quartz.SchedulerException e) {
      throw new SchedulerException(e);
    }
  }
  
  /**
   * Creates the cron trigger
   * @param id the id
   * @param expression the cron expression
   * @return a cron trigger
   */
  protected CronTriggerBean createTrigger(String id, String expression) {
    try {
      CronTriggerBean result = new CronTriggerBean();
      result.setName(id);
      result.setCronExpression(expression);
      return result;
    } catch (Throwable t) {
      throw new SchedulerException(t);
    }    
  }
  
  /**
   * Creates a job detail
   * @param id the id for this job
   * @param job the job that should be triggered
   * @return
   */
  protected JobDetailBean createJob(String id, IBeastJob job) {
    JobDetailBean result = new JobDetailBean();
    result.setName(id);
    result.setGroup("beast");
    result.setJobClass(BeastJobInvoker.class);
    result.getJobDataMap().put("job", job);
    result.getJobDataMap().put("messageManager", messageManager);
    return result;    
  }
}
