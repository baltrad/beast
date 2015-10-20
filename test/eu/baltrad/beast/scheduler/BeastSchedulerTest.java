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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import eu.baltrad.beast.manager.IBltMessageManager;

/**
 * @author Anders Henja
 *
 */
public class BeastSchedulerTest extends EasyMockSupport {
  private static interface MockMethods {
    public CronEntry createCronEntry(String expression, String name);
    public NamedParameterJdbcTemplate getNamedParameterTemplate();
    public GeneratedKeyHolder createKeyHolder();
    public void scheduleJob(CronEntry entry) throws SchedulerException;
    public void rescheduleJob(int id, CronEntry entry) throws SchedulerException;
    public JobDetail registerJob(String jobName) throws org.quartz.SchedulerException;
    public BeanPropertySqlParameterSource getParameterSource(CronEntry entry);
    public CronTrigger createTrigger(CronEntry entry, JobDetail jobDetail);
    public JobDetailFactoryBean createJob(String name);
    public RowMapper<CronEntry> getScheduleMapper();
  };
  
  private SchedulerFactoryBean sfBean = null;
  private MockMethods methods = null;
  private JdbcOperations jdbc = null;
  private BeastScheduler classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    sfBean = createMock(SchedulerFactoryBean.class);
    methods = createMock(MockMethods.class);
    jdbc = createMock(JdbcOperations.class);
    
    classUnderTest = new BeastScheduler() {
      protected CronEntry createCronEntry(String expression, String name) {
        return methods.createCronEntry(expression, name);
      }
      protected NamedParameterJdbcTemplate getNamedParameterTemplate() {
        return methods.getNamedParameterTemplate();
      }
      protected GeneratedKeyHolder createKeyHolder() {
        return methods.createKeyHolder();
      }
      protected void scheduleJob(CronEntry entry) throws SchedulerException {
        methods.scheduleJob(entry);
      }
      protected void rescheduleJob(int id, CronEntry entry) throws SchedulerException {
        methods.rescheduleJob(id, entry);
      }
      protected JobDetail registerJob(String jobName) throws org.quartz.SchedulerException {
        return methods.registerJob(jobName);
      }
      protected BeanPropertySqlParameterSource getParameterSource(CronEntry entry) {
        return methods.getParameterSource(entry);
      }
      protected CronTrigger createTrigger(CronEntry entry, JobDetail jobDetail) {
        return methods.createTrigger(entry, jobDetail);
      }
      protected RowMapper<CronEntry> getScheduleMapper() {
        return methods.getScheduleMapper();
      }
    };
    classUnderTest.setSchedulerFactoryBean(sfBean);
    classUnderTest.setJdbcTemplate(jdbc);
  }

  @After
  public void tearDown() throws Exception {
    sfBean = null;
    methods = null;
    jdbc = null;
    classUnderTest = null;
  }

  @Test
  public void testAfterPropertiesSet() throws Exception {
    List<CronEntry> entries = new ArrayList<CronEntry>();
    CronEntry e1 = new CronEntry(1, "1 * * * * ?", "A");
    CronEntry e2 = new CronEntry(2, "2 * * * * ?", "B");
    CronEntry e3 = new CronEntry(3, "3 * * * * ?", "A");
    entries.add(e1);
    entries.add(e2);
    entries.add(e3);
    
    RowMapper<CronEntry> mapper = new RowMapper<CronEntry>() {
      public CronEntry mapRow(ResultSet arg0, int arg1) throws SQLException {
        return null;
      }
    };
    
    expect(methods.getScheduleMapper()).andReturn(mapper);
    
    expect(jdbc.query("select id, expression, name from beast_scheduled_jobs order by id",
        mapper, new Object[]{})).andReturn(entries);

    methods.scheduleJob(e1);
    methods.scheduleJob(e2);
    methods.scheduleJob(e3);
    
    replayAll();
    
    classUnderTest.afterPropertiesSet();
    
    verifyAll();
  }
  
  @SuppressWarnings({ "unchecked"})
  @Test
  public void testRegister() throws Exception {
    NamedParameterJdbcTemplate namedParameterTemplate = createMock(NamedParameterJdbcTemplate.class);
    CronEntry entry = new CronEntry();
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    keyHolder.getKeyList().add(new HashMap());
    keyHolder.getKeys().put("id", new Integer(10));
    BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(entry);
    
    expect(methods.createCronEntry("0 * * * * ?", "ABC")).andReturn(entry);
    expect(methods.createKeyHolder()).andReturn(keyHolder);
    expect(methods.getParameterSource(entry)).andReturn(parameterSource);
    expect(methods.getNamedParameterTemplate()).andReturn(namedParameterTemplate);
    expect(namedParameterTemplate.update("insert into beast_scheduled_jobs (expression, name) values (:expression,:name)",
        parameterSource,
        keyHolder)).andReturn(1);
    
    methods.scheduleJob(entry);
    
    replayAll();
    
    classUnderTest.register("0 * * * * ?", "ABC");
    
    verifyAll();
  }

  @SuppressWarnings({ "unchecked"})
  @Test
  public void testRegister_updateThrowsException() throws Exception {
    NamedParameterJdbcTemplate namedParameterTemplate = createMock(NamedParameterJdbcTemplate.class);
    CronEntry entry = new CronEntry();
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    keyHolder.getKeyList().add(new HashMap());
    keyHolder.getKeys().put("id", new Integer(10));
    BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(entry);
    
    expect(methods.createCronEntry("0 * * * * ?", "ABC")).andReturn(entry);
    expect(methods.createKeyHolder()).andReturn(keyHolder);
    expect(methods.getParameterSource(entry)).andReturn(parameterSource);
    expect(methods.getNamedParameterTemplate()).andReturn(namedParameterTemplate);
    expect(namedParameterTemplate.update("insert into beast_scheduled_jobs (expression, name) values (:expression,:name)",
        parameterSource,
        keyHolder)).andThrow(new DataRetrievalFailureException("x"));
    
    replayAll();

    try {
      classUnderTest.register("0 * * * * ?", "ABC");
      fail("Expected SchedulerException");
    } catch (SchedulerException se) {
      // pass
    }
    
    verifyAll();
  }
  
  @SuppressWarnings({ "unchecked"})
  @Test
  public void testRegister_scheduleJobThrowsException() throws Exception {
    NamedParameterJdbcTemplate namedParameterTemplate = createMock(NamedParameterJdbcTemplate.class);
    CronEntry entry = new CronEntry();
    SchedulerException schedulerException = new SchedulerException();
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    keyHolder.getKeyList().add(new HashMap());
    keyHolder.getKeys().put("id", new Integer(10));
    BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(entry);
    
    expect(methods.createCronEntry("0 * * * * ?", "ABC")).andReturn(entry);
    expect(methods.createKeyHolder()).andReturn(keyHolder);
    expect(methods.getParameterSource(entry)).andReturn(parameterSource);
    expect(methods.getNamedParameterTemplate()).andReturn(namedParameterTemplate);

    expect(namedParameterTemplate.update("insert into beast_scheduled_jobs (expression, name) values (:expression,:name)",
        parameterSource,
        keyHolder)).andReturn(1);

    methods.scheduleJob(entry);
    expectLastCall().andThrow(schedulerException);

    replayAll();

    try {
      classUnderTest.register("0 * * * * ?", "ABC");
      fail("Expected SchedulerException");
    } catch (SchedulerException se) {
      assertSame(schedulerException, se);
    }
    
    verifyAll();
  }

  @Test
  public void testReregister() throws Exception {
    CronEntry entry = new CronEntry();
    
    expect(methods.createCronEntry("0 * * * * ?", "ABC")).andReturn(entry);
    expect(jdbc.update("update beast_scheduled_jobs set expression=?, name=? where id=?",
        new Object[]{"0 * * * * ?", "ABC", 10})).andReturn(1);
    
    methods.rescheduleJob(10, entry);
    
    replayAll();
    
    classUnderTest.reregister(10, "0 * * * * ?", "ABC");
    
    verifyAll();
  }
  
  @Test
  public void testUnregister() throws Exception {
    Scheduler scheduler = createMock(Scheduler.class);
    
    expect(sfBean.getScheduler()).andReturn(scheduler);
    expect(jdbc.update("delete from beast_scheduled_jobs where id=?",
        new Object[]{2})).andReturn(1);
    expect(scheduler.unscheduleJob(new TriggerKey("2", "beast"))).andReturn(true);
    
    replayAll();
    
    classUnderTest.unregister(2);
    
    verifyAll();
  }

  @Test
  public void testUnregister_jdbcUpdateThrowsException() throws Exception {
    Scheduler scheduler = createMock(Scheduler.class);
    expect(sfBean.getScheduler()).andReturn(scheduler);
    
    expect(jdbc.update("delete from beast_scheduled_jobs where id=?",
        new Object[]{2})).andThrow(new DataRetrievalFailureException("x"));
    
    replayAll();
    
    try {
      classUnderTest.unregister(2);
      fail("Expected SchedulerException");
    } catch (SchedulerException e) {
      // pass
    }
    
    verifyAll();
  }

  @Test
  public void testUnregister_unscheduleThrowsException() throws Exception {
    Scheduler scheduler = createMock(Scheduler.class);
    org.quartz.SchedulerException schedulerException = new org.quartz.SchedulerException();

    expect(sfBean.getScheduler()).andReturn(scheduler);
    
    expect(jdbc.update("delete from beast_scheduled_jobs where id=?",
        new Object[]{2})).andReturn(1);
    expect(scheduler.unscheduleJob(new TriggerKey("2", "beast"))).andThrow(schedulerException);
    
    replayAll();
    
    try {
      classUnderTest.unregister(2);
      fail("Expected SchedulerException");
    } catch (SchedulerException e) {
      // pass
    }
    
    verifyAll();
  }

  @Test
  public void testGetSchedule() throws Exception {
    Scheduler scheduler = createMock(Scheduler.class);
    CronTrigger t1 = createMock(CronTrigger.class);
    CronTrigger t2 = createMock(CronTrigger.class);
    TriggerKey t1k = new TriggerKey("1","beast");
    TriggerKey t2k = new TriggerKey("2","beast");
    Set<JobKey> keys = new HashSet<JobKey>();
    JobKey key1 = new JobKey("A", "beast");
    JobKey key2 = new JobKey("B", "beast");
    keys.add(key1);
    keys.add(key2);
    
    List<Trigger> triggers1 = new ArrayList<Trigger>();
    triggers1.add(t1);
    
    List<Trigger> triggers2 = new ArrayList<Trigger>();
    triggers2.add(t2);
    
    expect(sfBean.getScheduler()).andReturn(scheduler);
    expect(scheduler.getJobKeys(GroupMatcher.jobGroupEquals("beast"))).andReturn(keys);
    expect(scheduler.getTriggersOfJob(key1)).andReturn((ArrayList)triggers1);
    expect(t1.getKey()).andReturn(t1k);
    expect(t1.getCronExpression()).andReturn("1 * * * * ?");
    expect(t1.getJobKey()).andReturn(key1);
    
    expect(scheduler.getTriggersOfJob(key2)).andReturn((ArrayList)triggers2);
    expect(t2.getKey()).andReturn(t2k);
    expect(t2.getCronExpression()).andReturn("3 * * * * ?");
    expect(t2.getJobKey()).andReturn(key2);
    
    replayAll();
    
    List<CronEntry> entries = classUnderTest.getSchedule();

    verifyAll();
    assertEquals(2, entries.size());
    assertEquals(1, entries.get(0).getId());
    assertEquals("1 * * * * ?", entries.get(0).getExpression());
    assertEquals("A", entries.get(0).getName());
    assertEquals(2, entries.get(1).getId());
    assertEquals("3 * * * * ?", entries.get(1).getExpression());
    assertEquals("B", entries.get(1).getName());
  }
  
  @Test
  public void testCreateTrigger() throws Exception {
    JobDetail jobDetail = createMock(JobDetail.class);
    CronEntry entry = new CronEntry();
    entry.setId(10);
    entry.setExpression("0 * * * * ?");
    entry.setName("job");

    expect(jobDetail.getKey()).andReturn(new JobKey("job", "beast"));
    replayAll();
    
    classUnderTest = new BeastScheduler();
    CronTrigger result = classUnderTest.createTrigger(entry, jobDetail);
    
    verifyAll();
    assertEquals("10", result.getKey().getName());
    assertEquals("beast", result.getKey().getGroup());
    assertEquals("job", result.getJobKey().getName());
    assertEquals("beast", result.getJobKey().getGroup());
    assertEquals("0 * * * * ?", result.getCronExpression());
  }
 
  @Test
  public void testCreateTrigger_badExpression() throws Exception {
    JobDetail jobDetail = createMock(JobDetail.class);
    CronEntry entry = new CronEntry();
    entry.setId(10);
    entry.setExpression("0 * * ?");
    entry.setName("job");
    
    expect(jobDetail.getKey()).andReturn(new JobKey("job", "beast"));
    
    replayAll();
    classUnderTest = new BeastScheduler();
    
    try {
      classUnderTest.createTrigger(entry, jobDetail);
      fail("Expected SchedulerException");
    } catch (SchedulerException e) {
      // pass
    }
    verifyAll();
  }
  
  @Test
  public void testScheduleJob() throws Exception {
    Scheduler scheduler = createMock(Scheduler.class);
    JobDetail jobDetail = createMock(JobDetail.class);
    CronTrigger triggerBean = createMock(CronTrigger.class);
    CronEntry entry = new CronEntry(1, "0 * * * * ?", "A");
    
    expect(methods.registerJob("A")).andReturn(jobDetail);
    expect(methods.createTrigger(entry, jobDetail)).andReturn(triggerBean);
    expect(sfBean.getScheduler()).andReturn(scheduler);
    expect(scheduler.scheduleJob(triggerBean)).andReturn(new Date(0));
    
    classUnderTest = new BeastScheduler() {
      protected CronTrigger createTrigger(CronEntry entry, JobDetail jobDetail) {
        return methods.createTrigger(entry, jobDetail);
      }
      protected JobDetail registerJob(String jobName) throws org.quartz.SchedulerException {
        return methods.registerJob(jobName);
      }      
    };
    classUnderTest.setSchedulerFactoryBean(sfBean);
    
    
    replayAll();
    
    classUnderTest.scheduleJob(entry);
    
    verifyAll();
  }

  @Test
  public void testRescheduleJob() throws Exception {
    JobDetail jobDetail = createMock(JobDetail.class);
    Scheduler scheduler = createMock(Scheduler.class);
    CronTrigger triggerBean = createMock(CronTrigger.class);
    CronEntry entry = new CronEntry(1, "0 * * * * ?", "A");
    
    expect(methods.createTrigger(entry, jobDetail)).andReturn(triggerBean);
    expect(methods.registerJob("A")).andReturn(jobDetail);
    expect(sfBean.getScheduler()).andReturn(scheduler);
    
    expect(scheduler.unscheduleJob(new TriggerKey("1", "beast"))).andReturn(true);
    expect(scheduler.scheduleJob(triggerBean)).andReturn(new Date());
    
    classUnderTest = new BeastScheduler() {
      protected CronTrigger createTrigger(CronEntry entry, JobDetail jobDetail) {
        return methods.createTrigger(entry, jobDetail);
      }
      protected JobDetail registerJob(String jobName) throws org.quartz.SchedulerException {
        return methods.registerJob(jobName);
      }      
    };
    classUnderTest.setSchedulerFactoryBean(sfBean);
    
    
    replayAll();
    
    classUnderTest.rescheduleJob(1, entry);
    
    verifyAll();
  }
  
  @Test
  public void testRegisterJob() throws Exception {
    Scheduler scheduler = createMock(Scheduler.class);

    JobDetailFactoryBean detailFactory = createMock(JobDetailFactoryBean.class);
    JobDetail detail = createMock(JobDetail.class);
    classUnderTest = new BeastScheduler() {
      protected JobDetailFactoryBean createJob(String name) {
        return methods.createJob(name);
      }      
    };
    classUnderTest.setSchedulerFactoryBean(sfBean);
    
    expect(methods.createJob("ABC")).andReturn(detailFactory);
    expect(sfBean.getScheduler()).andReturn(scheduler);
    expect(detailFactory.getObject()).andReturn(detail);
    scheduler.addJob(detail, true);
    
    replayAll();

    classUnderTest.registerJob("ABC");
    
    verifyAll();
  }

  @Test
  public void testRegisterJob_addJobThrowsException() throws Exception {
    Scheduler scheduler = createMock(Scheduler.class);

    JobDetailFactoryBean detailFactory = createMock(JobDetailFactoryBean.class);
    JobDetail detail = createMock(JobDetail.class);
    org.quartz.SchedulerException exc = new org.quartz.SchedulerException();
    classUnderTest = new BeastScheduler() {
      protected JobDetailFactoryBean createJob(String name) {
        return methods.createJob(name);
      }      
    };
    classUnderTest.setSchedulerFactoryBean(sfBean);
    
    expect(methods.createJob("ABC")).andReturn(detailFactory);
    expect(sfBean.getScheduler()).andReturn(scheduler);
    expect(detailFactory.getObject()).andReturn(detail);
    scheduler.addJob(detail, true);
    expectLastCall().andThrow(exc);

    replayAll();

    try {
      classUnderTest.registerJob("ABC");
      fail("Expected org.quartz.SchedulerException");
    } catch (org.quartz.SchedulerException e) {
      assertSame(exc, e);
    }
    
    verifyAll();
  }

  @Test
  public void testCreateCronEntry() throws Exception {
    classUnderTest = new BeastScheduler();
    
    CronEntry result = classUnderTest.createCronEntry("a b c", "abc");
    assertEquals("abc", result.getName());
    assertEquals("a b c", result.getExpression());
  }
  
  @Test
  public void testGetNamedParameterTemplate() throws Exception {
    JdbcOperations template = createMock(JdbcOperations.class);
    
    classUnderTest = new BeastScheduler();
    classUnderTest.setJdbcTemplate(template);
    
    replayAll();
    
    NamedParameterJdbcTemplate result = classUnderTest.getNamedParameterTemplate();
    assertSame(template, result.getJdbcOperations());

    verifyAll();
  }

  @Test
  public void testCreateJob() throws Exception {
    IBltMessageManager mgr = createMock(IBltMessageManager.class);
    
    classUnderTest = new BeastScheduler();
    classUnderTest.setMessageManager(mgr);
    
    replayAll();
    
    JobDetailFactoryBean result = classUnderTest.createJob("abc");
    
    verifyAll();
    assertEquals("abc", result.getObject().getKey().getName());
    assertEquals("beast", result.getObject().getKey().getGroup());
    assertSame(BeastJobInvoker.class, result.getObject().getJobClass());
    assertSame(mgr, result.getJobDataMap().get("messageManager"));
  }
  
  @Test
  public void testCreateJob_nullName() throws Exception {
    IBltMessageManager mgr = createMock(IBltMessageManager.class);
    
    classUnderTest = new BeastScheduler();
    classUnderTest.setMessageManager(mgr);

    replayAll();
    
    try {
      classUnderTest.createJob(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException npe) {
      // pass
    }
    verifyAll();
  }
  
  @Test
  public void testCreateJob_nullId() throws Exception {
    IBltMessageManager mgr = createMock(IBltMessageManager.class);
    
    classUnderTest = new BeastScheduler();
    classUnderTest.setMessageManager(mgr);

    replayAll();
    
    try {
      classUnderTest.createJob(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException npe) {
      // pass
    }
    verifyAll();
  }
}
