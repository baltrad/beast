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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.baltrad.beast.manager.IBltMessageManager;

/**
 * The scheduler within beast. The scheduler works like any other
 * IBltMessage producer and sends BltScheduledJobMessage's. This
 * means that in order to handle scheduled jobs the appropriate
 * rule must be supporting this.
 * 
 * You ought to specify jobName (rule name) in order to get
 * the correct rule executed instead of the ordinary broadcast
 * but that is up to you.
 * 
 * @author Anders Henja
 */
public class BeastScheduler implements IBeastScheduler, InitializingBean, DisposableBean {
//  static
//  {
//      Logger rootLogger = Logger.getRootLogger();
//      rootLogger.setLevel(Level.DEBUG);
//      rootLogger.addAppender(new ConsoleAppender(
//                 new PatternLayout("%-6r [%p] %c - %m%n")));
//  }
  
  /**
   * The group to use for all scheduled jobs
   */
  private final static String GROUP_NAME = "beast";
  
  /**
   * Injected factory bean...
   */
  private SchedulerFactoryBean sf = null;

  /**
   * The message manager.
   */
  private IBltMessageManager messageManager = null;
 
  /**
   * The database access
   */
  private JdbcOperations template = null;
  
  /**
   * If the scheduler factory bean has been created internally or comes from
   * outside
   */
  private volatile boolean internalSfb = false;
  
  /**
   * Constructor
   */
  public BeastScheduler() {
    this.sf = null;
  }
  
  /**
   * Sets the scheduler factory bean
   * @param sf the factory bean
   */
  public synchronized void setSchedulerFactoryBean(SchedulerFactoryBean sf) {
    this.sf = sf;
    internalSfb = false;
  }

  /**
   * @param mgr the message manager to set
   */
  public void setMessageManager(IBltMessageManager mgr) {
    this.messageManager = mgr;
  }
  
  /**
   * @param template the template
   */
  public void setJdbcTemplate(JdbcOperations template) {
    this.template = template;
  }
  
  /**
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public synchronized void afterPropertiesSet() throws Exception {
    if (sf == null) {
      sf = createScheduleFactory();
      sf.afterPropertiesSet();
      internalSfb = true;
      sf.start();
    }
    List<CronEntry> entries = template.query("select id, expression, name from beast_scheduled_jobs order by id",
        getScheduleMapper(),
        new Object[]{});
    
    for (CronEntry entry : entries) {
      scheduleJob(entry);
    }
  }

  /**
   * Creates a SchedulerFactoryBean. Remember to call
   * afterPropertiesSet on it.
   * @return the scheduler factory bean
   */
  protected SchedulerFactoryBean createScheduleFactory() {
    return new SchedulerFactoryBean();
  }

  /**
   * @see org.springframework.beans.factory.DisposableBean#destroy()
   */
  @Override
  public synchronized void destroy() throws Exception {
    if (internalSfb == true && sf != null) {
      sf.destroy();
    }
  }
  /**
   * @see eu.baltrad.beast.scheduler.IBeastScheduler#register(java.lang.String, java.lang.String)
   */
  @Override
  @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
  public int register(String expression, String name) {
    try {
      CronEntry entry = createCronEntry(expression, name);
      KeyHolder keyHolder = createKeyHolder();
      BeanPropertySqlParameterSource parameterSource = getParameterSource(entry);
      
      getNamedParameterTemplate().update("insert into beast_scheduled_jobs (expression, name) values (:expression,:name)",
          parameterSource, keyHolder);

      entry.setId((Integer)keyHolder.getKeys().get("id"));
      
      scheduleJob(entry);
      
      return entry.getId();
    } catch (DataAccessException e) {
      throw new SchedulerException(e);
    }
  }

  /**
   * @see eu.baltrad.beast.scheduler.IBeastScheduler#reregister(int, java.lang.String, java.lang.String)
   */
  @Override
  @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
  public void reregister(int id, String expression, String name) {
    try {
      CronEntry entry = createCronEntry(expression, name);
      entry.setId(id);
      
      template.update("update beast_scheduled_jobs set expression=?, name=? where id=?",
          new Object[]{expression, name, id});

      rescheduleJob(id, entry);
    } catch (DataAccessException e) {
      throw new SchedulerException(e);
    }
  }

  /**
   * @see eu.baltrad.beast.scheduler.IBeastScheduler#unregister(int)
   */
  @Override
  @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
  public void unregister(int id) {
    try {
      Scheduler scheduler = sf.getScheduler();
      template.update("delete from beast_scheduled_jobs where id=?",
          new Object[]{id});
      scheduler.unscheduleJob(new TriggerKey(""+id, GROUP_NAME));
    } catch (org.quartz.SchedulerException e) {
      throw new SchedulerException(e);
    } catch (DataAccessException e) {
      throw new SchedulerException(e);
    }
  }
  
  /**
   * @see eu.baltrad.beast.scheduler.IBeastScheduler#getSchedule()
   */
  @Override
  public List<CronEntry> getSchedule() {
    List<CronEntry> result = new ArrayList<CronEntry>();
    try {
      Scheduler scheduler = sf.getScheduler();
      Set<JobKey> keys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(GROUP_NAME));
      for (JobKey k: keys) {
        @SuppressWarnings("unchecked")
        List<Trigger> triggers = (List<Trigger>)scheduler.getTriggersOfJob(k);
        for (Trigger trigger : triggers) {
          if (trigger instanceof CronTrigger) {
            CronEntry entry = new CronEntry();
            CronTrigger ct = (CronTrigger)trigger;
            try {
              entry.setId(Integer.parseInt(ct.getKey().getName()));
              entry.setExpression(ct.getCronExpression());
              entry.setName(ct.getJobKey().getName());
              result.add(entry);
            } catch (NumberFormatException e) {
              e.printStackTrace();
            }
          }
        }
      }
    } catch (org.quartz.SchedulerException t) {
      t.printStackTrace();
    } catch (RuntimeException t) {
      t.printStackTrace();
    }
    
    Collections.sort(result,
        new Comparator<CronEntry>() {
          @Override
          public int compare(CronEntry o1, CronEntry o2) {
            if (o1.getId() > o2.getId()) {
              return 1;
            } else if (o1.getId() < o2.getId()) {
              return -1;
            }
            return 0;
          }
        }
    );
    return result;
  }
  
  /**
   * @see eu.baltrad.beast.scheduler.IBeastScheduler#getSchedule(java.lang.String)
   */
  public List<CronEntry> getSchedule(String job) {
    List<CronEntry> result = new ArrayList<CronEntry>();
    List<CronEntry> entries = getSchedule();
    for (CronEntry entry: entries) {
      if (entry.getName().equals(job)) {
        result.add(entry);
      }
    }
    return result;
  }
  
  /**
   * @see eu.baltrad.beast.scheduler.IBeastScheduler#getEntry(int)
   */
  public CronEntry getEntry(int id) {
    CronEntry result = null;
    List<CronEntry> entries = getSchedule();
    for (CronEntry entry: entries) {
      if (id == entry.getId()) {
        result= entry;
        break;
      }
    }
    return result;
  }
  
  /**
   * Creates the cron trigger
   * @param entry the entry to create a cron trigger from
   * @return a cron trigger
   */
  protected CronTrigger createTrigger(CronEntry entry, JobDetail jobDetail) {
    try {
      CronTriggerFactoryBean result = new CronTriggerFactoryBean();
      result.setName(""+entry.getId());
      result.setGroup(GROUP_NAME);
      result.setCronExpression(entry.getExpression());
      result.setStartDelay(0);
      result.setJobDetail(jobDetail);
      result.afterPropertiesSet();
      return result.getObject();
    } catch (ParseException e) {
      throw new SchedulerException(e);
    }    
  }
  
  /***
   * Schedules a job
   * @param entry the entry (must be valid)
   * @throws SchedulerException on error
   */
  protected void scheduleJob(CronEntry entry) {
    try {
      JobDetail jobDetail = registerJob(entry.getName());
      CronTrigger trigger = createTrigger(entry, jobDetail);
      sf.getScheduler().scheduleJob(trigger);
    } catch (org.quartz.SchedulerException e) {
      throw new SchedulerException(e);
    }
  }

  /**
   * Reschedules the job
   * @param id the id of the trigger to be rescheduled
   * @param entry the job
   * @throws SchedulerException
   */
  protected void rescheduleJob(int id, CronEntry entry) {
    try {
      Scheduler scheduler = sf.getScheduler();
      scheduler.unscheduleJob(new TriggerKey(""+id, GROUP_NAME));
      
      JobDetail jobDetail = registerJob(entry.getName());
      CronTrigger trigger = createTrigger(entry, jobDetail);
      scheduler.scheduleJob(trigger);
    } catch (org.quartz.SchedulerException e) {
      throw new SchedulerException(e);
    }
  }
  
  /**
   * Creates a job detail
   * @param jobName the beast job name
   * @throws SchedulerException 
   */
  protected JobDetail registerJob(String jobName) throws org.quartz.SchedulerException {
    JobDetailFactoryBean bean = createJob(jobName);
    JobDetail detail = bean.getObject();
    sf.getScheduler().addJob(detail, true);
    return detail;
  }

  /**
   * Returns a new instance of cron entry
   * @param expression the cron expression
   * @param name the name
   * @return the instance
   */
  protected CronEntry createCronEntry(String expression, String name) {
    return new CronEntry(expression, name);
  }

  /**
   * @return the named parameter template
   */
  protected NamedParameterJdbcTemplate getNamedParameterTemplate() {
    return new NamedParameterJdbcTemplate(template);
  }

  /**
   * Creates a parameter source from an entry
   * @param entry the entry
   * @return the parameter source
   */
  protected BeanPropertySqlParameterSource getParameterSource(CronEntry entry) {
    return new BeanPropertySqlParameterSource(entry);
  }
  
  /**
   * @return the key holder
   */
  protected GeneratedKeyHolder createKeyHolder() {
    return new GeneratedKeyHolder();
  }

  /**
   * Creates a job detail bean
   * @param jobName the job name
   * @return the bean
   */
  protected JobDetailFactoryBean createJob(String jobName) {
    JobDetailFactoryBean result = new JobDetailFactoryBean();
    
    if (jobName == null) {
      throw new NullPointerException();
    }
    
    result.setName(jobName);
    result.setGroup("beast");
    result.setJobClass(BeastJobInvoker.class);
    result.getJobDataMap().put("messageManager", messageManager);
    result.setDurability(true);
    result.setBeanName(jobName);
    
    result.afterPropertiesSet();
    
    return result;    
  }
  
  /**
   * @return the schedule mapper to use
   */
  protected RowMapper<CronEntry> getScheduleMapper() {
    return new RowMapper<CronEntry>() {
      @Override
      public CronEntry mapRow(ResultSet rs, int rownum) throws SQLException {
        return doMapRow(rs, rownum);
      }
    };
  }
  
  /**
   * Maps one resultset into a cron entry
   * @param rs the result set
   * @param rownum the row number
   * @return the cron entry
   * @throws SQLException on error
   */
  protected CronEntry doMapRow(ResultSet rs, int rownum) throws SQLException {
    int id = rs.getInt("id");
    String name = rs.getString("name");
    String expression = rs.getString("expression");
    CronEntry result = new CronEntry(expression, name);
    result.setId(id);
    return result;
  }
  
  public static void main(String[] args) {
    BeastScheduler scheduler = new BeastScheduler();
    try {
      scheduler.afterPropertiesSet();
    } catch (Exception t) {
      t.printStackTrace();
    }

    scheduler.register("0 * * * * ?", "ABC");
    scheduler.register("0 * * * * ?", "ABC");
    scheduler.register("5 * * * * ?", "DEF");
    
    List<CronEntry> result = scheduler.getSchedule();
    for (CronEntry entry : result) {
      System.out.println("JOB " + entry.getId() + " => " + entry.getName());
    }
  }
}
