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
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.JobDetailBean;
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
    public void registerJob(String jobName) throws org.quartz.SchedulerException;
    public BeanPropertySqlParameterSource getParameterSource(CronEntry entry);
    public CronTriggerBean createTrigger(CronEntry entry);
    public JobDetailBean createJob(String name);
    public ParameterizedRowMapper<CronEntry> getScheduleMapper();
  };
  
  private SchedulerFactoryBean sfBean = null;
  private MockMethods methods = null;
  private SimpleJdbcOperations jdbc = null;
  private BeastScheduler classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    sfBean = createMock(SchedulerFactoryBean.class);
    methods = createMock(MockMethods.class);
    jdbc = createMock(SimpleJdbcOperations.class);
    
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
      protected void registerJob(String jobName) throws org.quartz.SchedulerException {
        methods.registerJob(jobName);
      }
      protected BeanPropertySqlParameterSource getParameterSource(CronEntry entry) {
        return methods.getParameterSource(entry);
      }
      protected CronTriggerBean createTrigger(CronEntry entry) {
        return methods.createTrigger(entry);
      }
      protected ParameterizedRowMapper<CronEntry> getScheduleMapper() {
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
    
    ParameterizedRowMapper<CronEntry> mapper = new ParameterizedRowMapper<CronEntry>() {
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
    expect(scheduler.unscheduleJob("2", "beast")).andReturn(true);
    
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
    expect(scheduler.unscheduleJob("2", "beast")).andThrow(schedulerException);
    
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
    CronTrigger t1 = new CronTrigger();
    t1.setName("1");
    t1.setJobName("A");
    t1.setCronExpression("1 * * * * ?");
    CronTrigger t2 = new CronTrigger();
    t2.setName("2");
    t2.setJobName("B");
    t2.setCronExpression("2 * * * * ?");
    CronTrigger t3 = new CronTrigger();
    t3.setName("3");
    t3.setJobName("C");
    t3.setCronExpression("3 * * * * ?");
    SimpleTrigger t4 = new SimpleTrigger();
    Trigger[] tg1 = new Trigger[]{t1, t2};
    Trigger[] tg2 = new Trigger[]{t3, t4};
    
    expect(sfBean.getScheduler()).andReturn(scheduler);
    expect(scheduler.getJobNames("beast")).andReturn(new String[]{"A", "B", "C"});
    expect(scheduler.getTriggersOfJob("A", "beast")).andReturn(new Trigger[]{});
    expect(scheduler.getTriggersOfJob("B", "beast")).andReturn(tg1);
    expect(scheduler.getTriggersOfJob("C", "beast")).andReturn(tg2);
    
    replayAll();
    
    List<CronEntry> entries = classUnderTest.getSchedule();

    verifyAll();
    assertEquals(3, entries.size());
    assertEquals(1, entries.get(0).getId());
    assertEquals("1 * * * * ?", entries.get(0).getExpression());
    assertEquals("A", entries.get(0).getName());
    assertEquals(2, entries.get(1).getId());
    assertEquals("2 * * * * ?", entries.get(1).getExpression());
    assertEquals("B", entries.get(1).getName());
    assertEquals(3, entries.get(2).getId());
    assertEquals("3 * * * * ?", entries.get(2).getExpression());
    assertEquals("C", entries.get(2).getName());
    
  }
  
  @Test
  public void testCreateTrigger() throws Exception {
    CronEntry entry = new CronEntry();
    entry.setId(10);
    entry.setExpression("0 * * * * ?");
    entry.setName("job");
    
    classUnderTest = new BeastScheduler();
    
    CronTriggerBean result = classUnderTest.createTrigger(entry);
    
    assertEquals("10", result.getName());
    assertEquals("beast", result.getGroup());
    assertEquals("job", result.getJobName());
    assertEquals("beast", result.getJobGroup());
    assertEquals("0 * * * * ?", result.getCronExpression());
  }
 
  @Test
  public void testCreateTrigger_badExpression() throws Exception {
    CronEntry entry = new CronEntry();
    entry.setId(10);
    entry.setExpression("0 * * ?");
    entry.setName("job");
    
    classUnderTest = new BeastScheduler();
    
    try {
      classUnderTest.createTrigger(entry);
      fail("Expected SchedulerException");
    } catch (SchedulerException e) {
      // pass
    }
  }
  
  @Test
  public void testScheduleJob() throws Exception {
    Scheduler scheduler = createMock(Scheduler.class);
    CronTriggerBean triggerBean = new CronTriggerBean();
    CronEntry entry = new CronEntry(1, "0 * * * * ?", "A");
    
    expect(methods.createTrigger(entry)).andReturn(triggerBean);
    methods.registerJob("A");
    expect(sfBean.getScheduler()).andReturn(scheduler);
    expect(scheduler.scheduleJob(triggerBean)).andReturn(new Date(0));
    
    classUnderTest = new BeastScheduler() {
      protected CronTriggerBean createTrigger(CronEntry entry) {
        return methods.createTrigger(entry);
      }
      protected void registerJob(String jobName) throws org.quartz.SchedulerException {
        methods.registerJob(jobName);
      }      
    };
    classUnderTest.setSchedulerFactoryBean(sfBean);
    
    
    replayAll();
    
    classUnderTest.scheduleJob(entry);
    
    verifyAll();
  }

  @Test
  public void testRescheduleJob() throws Exception {
    Scheduler scheduler = createMock(Scheduler.class);
    CronTriggerBean triggerBean = new CronTriggerBean();
    CronEntry entry = new CronEntry(1, "0 * * * * ?", "A");
    
    expect(methods.createTrigger(entry)).andReturn(triggerBean);
    methods.registerJob("A");
    expect(sfBean.getScheduler()).andReturn(scheduler);
    
    expect(scheduler.unscheduleJob("1", "beast")).andReturn(true);
    expect(scheduler.scheduleJob(triggerBean)).andReturn(new Date());
    
    classUnderTest = new BeastScheduler() {
      protected CronTriggerBean createTrigger(CronEntry entry) {
        return methods.createTrigger(entry);
      }
      protected void registerJob(String jobName) throws org.quartz.SchedulerException {
        methods.registerJob(jobName);
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

    JobDetailBean detail = new JobDetailBean();
    classUnderTest = new BeastScheduler() {
      protected JobDetailBean createJob(String name) {
        return methods.createJob(name);
      }      
    };
    classUnderTest.setSchedulerFactoryBean(sfBean);
    
    expect(methods.createJob("ABC")).andReturn(detail);
    expect(sfBean.getScheduler()).andReturn(scheduler);
    scheduler.addJob(detail, true);
    
    replayAll();

    classUnderTest.registerJob("ABC");
    
    verifyAll();
  }

  @Test
  public void testRegisterJob_addJobThrowsException() throws Exception {
    Scheduler scheduler = createMock(Scheduler.class);

    JobDetailBean detail = new JobDetailBean();
    org.quartz.SchedulerException exc = new org.quartz.SchedulerException();
    classUnderTest = new BeastScheduler() {
      protected JobDetailBean createJob(String name) {
        return methods.createJob(name);
      }      
    };
    classUnderTest.setSchedulerFactoryBean(sfBean);
    
    expect(methods.createJob("ABC")).andReturn(detail);
    expect(sfBean.getScheduler()).andReturn(scheduler);
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
    SimpleJdbcOperations template = createMock(SimpleJdbcOperations.class);
    JdbcOperations operations = createMock(JdbcOperations.class);
    
    expect(template.getJdbcOperations()).andReturn(operations);
    
    classUnderTest = new BeastScheduler();
    classUnderTest.setJdbcTemplate(template);
    
    replayAll();
    
    NamedParameterJdbcTemplate result = classUnderTest.getNamedParameterTemplate();
    assertSame(operations, result.getJdbcOperations());

    verifyAll();
  }

  @Test
  public void testCreateJob() throws Exception {
    IBltMessageManager mgr = createMock(IBltMessageManager.class);
    
    classUnderTest = new BeastScheduler();
    classUnderTest.setMessageManager(mgr);
    
    replayAll();
    
    JobDetailBean result = classUnderTest.createJob("abc");
    
    verifyAll();
    assertEquals("abc", result.getName());
    assertEquals("beast", result.getGroup());
    assertSame(BeastJobInvoker.class, result.getJobClass());
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
