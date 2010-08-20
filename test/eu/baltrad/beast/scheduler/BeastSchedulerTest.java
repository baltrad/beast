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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
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
public class BeastSchedulerTest extends TestCase {
  private static interface MockMethods {
    public CronEntry createCronEntry(String expression, String name);
    public NamedParameterJdbcTemplate getNamedParameterTemplate();
    public GeneratedKeyHolder createKeyHolder();
    public void scheduleJob(CronEntry entry) throws SchedulerException;
    public void registerJob(String jobName) throws org.quartz.SchedulerException;
    public BeanPropertySqlParameterSource getParameterSource(CronEntry entry);
    public CronTriggerBean createTrigger(CronEntry entry);
    public JobDetailBean createJob(String name);
    public ParameterizedRowMapper<CronEntry> getScheduleMapper();
  };
  
  private MockControl sfControl = null;
  private SchedulerFactoryBean sfBean = null;
  private MockControl methodsControl = null;
  private MockMethods methods = null;
  private MockControl jdbcControl = null;
  private SimpleJdbcOperations jdbc = null;
  private BeastScheduler classUnderTest = null;
  
  public void setUp() throws Exception {
    super.setUp();
    sfControl = MockClassControl.createControl(SchedulerFactoryBean.class);
    sfBean = (SchedulerFactoryBean)sfControl.getMock();
    methodsControl = MockControl.createControl(MockMethods.class);
    methods = (MockMethods)methodsControl.getMock();
    jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    
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
  
  public void tearDown() throws Exception {
    super.tearDown();
    sfBean = null;
    sfControl = null;
    methods = null;
    methodsControl = null;
    jdbc = null;
    jdbcControl = null;
    classUnderTest = null;
  }
  
  protected void replay() {
    sfControl.replay();
    methodsControl.replay();
    jdbcControl.replay();
  }
  
  public void verify() {
    sfControl.verify();
    methodsControl.verify();
    jdbcControl.verify();
  }
  
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
    
    methods.getScheduleMapper();
    methodsControl.setReturnValue(mapper);
    
    jdbc.query("select id, expression, name from beast_scheduled_jobs order by id",
        mapper, new Object[]{});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(entries);

    methods.scheduleJob(e1);
    methods.scheduleJob(e2);
    methods.scheduleJob(e3);
    
    replay();
    
    classUnderTest.afterPropertiesSet();
    
    verify();
  }
  
  @SuppressWarnings("unchecked")
  public void testRegister() throws Exception {
    MockControl nptControl = MockClassControl.createControl(NamedParameterJdbcTemplate.class);
    NamedParameterJdbcTemplate namedParameterTemplate = (NamedParameterJdbcTemplate)nptControl.getMock();
    CronEntry entry = new CronEntry();
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    keyHolder.getKeyList().add(new HashMap());
    keyHolder.getKeys().put("id", new Integer(10));
    BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(entry);
    
    methods.createCronEntry("0 * * * * ?", "ABC");
    methodsControl.setReturnValue(entry);
    methods.createKeyHolder();
    methodsControl.setReturnValue(keyHolder);
    methods.getParameterSource(entry);
    methodsControl.setReturnValue(parameterSource);
    methods.getNamedParameterTemplate();
    methodsControl.setReturnValue(namedParameterTemplate);
    namedParameterTemplate.update("insert into beast_scheduled_jobs (expression, name) values (:expression,:name)",
        parameterSource,
        keyHolder);
    nptControl.setReturnValue(0);
    
    methods.scheduleJob(entry);
    
    replay();
    nptControl.replay();
    
    classUnderTest.register("0 * * * * ?", "ABC");
    
    verify();
    nptControl.verify();
  }

  @SuppressWarnings("unchecked")
  public void testRegister_updateThrowsException() throws Exception {
    MockControl nptControl = MockClassControl.createControl(NamedParameterJdbcTemplate.class);
    NamedParameterJdbcTemplate namedParameterTemplate = (NamedParameterJdbcTemplate)nptControl.getMock();
    CronEntry entry = new CronEntry();
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    keyHolder.getKeyList().add(new HashMap());
    keyHolder.getKeys().put("id", new Integer(10));
    BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(entry);
    
    methods.createCronEntry("0 * * * * ?", "ABC");
    methodsControl.setReturnValue(entry);
    methods.createKeyHolder();
    methodsControl.setReturnValue(keyHolder);
    methods.getParameterSource(entry);
    methodsControl.setReturnValue(parameterSource);
    methods.getNamedParameterTemplate();
    methodsControl.setReturnValue(namedParameterTemplate);
    namedParameterTemplate.update("insert into beast_scheduled_jobs (expression, name) values (:expression,:name)",
        parameterSource,
        keyHolder);
    nptControl.setThrowable(new DataRetrievalFailureException("x"));
    
    replay();
    nptControl.replay();

    try {
      classUnderTest.register("0 * * * * ?", "ABC");
      fail("Expected SchedulerException");
    } catch (SchedulerException se) {
      // pass
    }
    
    verify();
    nptControl.verify();
  }
  
  @SuppressWarnings("unchecked")
  public void testRegister_scheduleJobThrowsException() throws Exception {
    MockControl nptControl = MockClassControl.createControl(NamedParameterJdbcTemplate.class);
    NamedParameterJdbcTemplate namedParameterTemplate = (NamedParameterJdbcTemplate)nptControl.getMock();
    CronEntry entry = new CronEntry();
    SchedulerException schedulerException = new SchedulerException();
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    keyHolder.getKeyList().add(new HashMap());
    keyHolder.getKeys().put("id", new Integer(10));
    BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(entry);
    
    methods.createCronEntry("0 * * * * ?", "ABC");
    methodsControl.setReturnValue(entry);
    methods.createKeyHolder();
    methodsControl.setReturnValue(keyHolder);
    methods.getParameterSource(entry);
    methodsControl.setReturnValue(parameterSource);
    methods.getNamedParameterTemplate();
    methodsControl.setReturnValue(namedParameterTemplate);

    namedParameterTemplate.update("insert into beast_scheduled_jobs (expression, name) values (:expression,:name)",
        parameterSource,
        keyHolder);
    nptControl.setReturnValue(0);
    methods.scheduleJob(entry);
    methodsControl.setThrowable(schedulerException);
    replay();
    nptControl.replay();

    try {
      classUnderTest.register("0 * * * * ?", "ABC");
      fail("Expected SchedulerException");
    } catch (SchedulerException se) {
      assertSame(schedulerException, se);
    }
    
    verify();
    nptControl.verify();    
  }
  
  public void testUnregister() throws Exception {
    MockControl schedControl = MockControl.createControl(Scheduler.class);
    Scheduler scheduler = (Scheduler)schedControl.getMock();
    
    sfBean.getScheduler();
    sfControl.setReturnValue(scheduler);
    jdbc.update("delete from beast_scheduled_jobs where id=?",
        new Object[]{2});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    scheduler.unscheduleJob("2", "beast");
    schedControl.setReturnValue(true);
    
    replay();
    schedControl.replay();
    
    classUnderTest.unregister(2);
    
    verify();
    schedControl.verify();
  }

  public void testUnregister_jdbcUpdateThrowsException() throws Exception {
    MockControl schedControl = MockControl.createControl(Scheduler.class);
    Scheduler scheduler = (Scheduler)schedControl.getMock();
    sfBean.getScheduler();
    sfControl.setReturnValue(scheduler);
    
    jdbc.update("delete from beast_scheduled_jobs where id=?",
        new Object[]{2});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setThrowable(new DataRetrievalFailureException("x"));
    
    replay();
    schedControl.replay();
    
    try {
      classUnderTest.unregister(2);
      fail("Expected SchedulerException");
    } catch (SchedulerException e) {
      // pass
    }
    
    verify();
    schedControl.verify();
  }
  
  public void testUnregister_unscheduleThrowsException() throws Exception {
    MockControl schedControl = MockControl.createControl(Scheduler.class);
    Scheduler scheduler = (Scheduler)schedControl.getMock();
    org.quartz.SchedulerException schedulerException = new org.quartz.SchedulerException();

    sfBean.getScheduler();
    sfControl.setReturnValue(scheduler);
    
    jdbc.update("delete from beast_scheduled_jobs where id=?",
        new Object[]{2});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    scheduler.unscheduleJob("2", "beast");
    schedControl.setThrowable(schedulerException);
    
    replay();
    schedControl.replay();
    
    try {
      classUnderTest.unregister(2);
      fail("Expected SchedulerException");
    } catch (SchedulerException e) {
      // pass
    }
    
    verify();
    schedControl.verify();
  }

  public void testGetSchedule() throws Exception {
    MockControl schedControl = MockControl.createControl(Scheduler.class);
    Scheduler scheduler = (Scheduler)schedControl.getMock();
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
    
    sfBean.getScheduler();
    sfControl.setReturnValue(scheduler);

    scheduler.getJobNames("beast");
    schedControl.setReturnValue(new String[]{"A", "B", "C"});
    scheduler.getTriggersOfJob("A", "beast");
    schedControl.setReturnValue(new Trigger[]{});
    scheduler.getTriggersOfJob("B", "beast");
    schedControl.setReturnValue(tg1);
    scheduler.getTriggersOfJob("C", "beast");
    schedControl.setReturnValue(tg2);
    
    replay();
    schedControl.replay();
    
    List<CronEntry> entries = classUnderTest.getSchedule();
    
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
    
    verify();
    schedControl.verify();
  }
  
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
  
  public void testScheduleJob() throws Exception {
    MockControl schedControl = MockControl.createControl(Scheduler.class);
    Scheduler scheduler = (Scheduler)schedControl.getMock();
    CronTriggerBean triggerBean = new CronTriggerBean();
    CronEntry entry = new CronEntry(1, "0 * * * * ?", "A");
    
    methods.createTrigger(entry);
    methodsControl.setReturnValue(triggerBean);
    methods.registerJob("A");
    sfBean.getScheduler();
    sfControl.setReturnValue(scheduler);
    scheduler.scheduleJob(triggerBean);
    schedControl.setReturnValue(new Date(0));
    
    classUnderTest = new BeastScheduler() {
      protected CronTriggerBean createTrigger(CronEntry entry) {
        return methods.createTrigger(entry);
      }
      protected void registerJob(String jobName) throws org.quartz.SchedulerException {
        methods.registerJob(jobName);
      }      
    };
    classUnderTest.setSchedulerFactoryBean(sfBean);
    
    
    replay();
    schedControl.replay();
    
    classUnderTest.scheduleJob(entry);
    
    verify();
    schedControl.verify();
  }
  
  public void testRegisterJob() throws Exception {
    MockControl schedControl = MockControl.createControl(Scheduler.class);
    Scheduler scheduler = (Scheduler)schedControl.getMock();

    JobDetailBean detail = new JobDetailBean();
    classUnderTest = new BeastScheduler() {
      protected JobDetailBean createJob(String name) {
        return methods.createJob(name);
      }      
    };
    classUnderTest.setSchedulerFactoryBean(sfBean);
    
    methods.createJob("ABC");
    methodsControl.setReturnValue(detail);
    sfBean.getScheduler();
    sfControl.setReturnValue(scheduler);
    scheduler.addJob(detail, true);
    
    replay();
    schedControl.replay();

    classUnderTest.registerJob("ABC");
    
    verify();
    schedControl.verify();
  }

  public void testRegisterJob_addJobThrowsException() throws Exception {
    MockControl schedControl = MockControl.createControl(Scheduler.class);
    Scheduler scheduler = (Scheduler)schedControl.getMock();

    JobDetailBean detail = new JobDetailBean();
    org.quartz.SchedulerException exc = new org.quartz.SchedulerException();
    classUnderTest = new BeastScheduler() {
      protected JobDetailBean createJob(String name) {
        return methods.createJob(name);
      }      
    };
    classUnderTest.setSchedulerFactoryBean(sfBean);
    
    methods.createJob("ABC");
    methodsControl.setReturnValue(detail);
    sfBean.getScheduler();
    sfControl.setReturnValue(scheduler);
    scheduler.addJob(detail, true);
    schedControl.setThrowable(exc);
    replay();
    schedControl.replay();

    try {
      classUnderTest.registerJob("ABC");
      fail("Expected org.quartz.SchedulerException");
    } catch (org.quartz.SchedulerException e) {
      assertSame(exc, e);
    }
    
    verify();
    schedControl.verify();
  }

  public void testCreateCronEntry() throws Exception {
    classUnderTest = new BeastScheduler();
    
    CronEntry result = classUnderTest.createCronEntry("a b c", "abc");
    assertEquals("abc", result.getName());
    assertEquals("a b c", result.getExpression());
  }
  
  public void testGetNamedParameterTemplate() throws Exception {
    MockControl templateControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations template = (SimpleJdbcOperations)templateControl.getMock();
    MockControl operationsControl = MockControl.createControl(JdbcOperations.class);
    JdbcOperations operations = (JdbcOperations)operationsControl.getMock();
    
    template.getJdbcOperations();
    templateControl.setReturnValue(operations);
    
    classUnderTest = new BeastScheduler();
    classUnderTest.setJdbcTemplate(template);
    
    templateControl.replay();
    operationsControl.replay();
    
    NamedParameterJdbcTemplate result = classUnderTest.getNamedParameterTemplate();
    assertSame(operations, result.getJdbcOperations());

    templateControl.verify();
    operationsControl.verify();
  }
  
  public void testCreateJob() throws Exception {
    MockControl mgrControl = MockControl.createControl(IBltMessageManager.class);
    IBltMessageManager mgr = (IBltMessageManager)mgrControl.getMock();
    
    classUnderTest = new BeastScheduler();
    classUnderTest.setMessageManager(mgr);
    
    JobDetailBean result = classUnderTest.createJob("abc");
    
    assertEquals("abc", result.getName());
    assertEquals("beast", result.getGroup());
    assertSame(BeastJobInvoker.class, result.getJobClass());
    assertSame(mgr, result.getJobDataMap().get("messageManager"));
  }
  
  public void testCreateJob_nullName() throws Exception {
    MockControl mgrControl = MockControl.createControl(IBltMessageManager.class);
    IBltMessageManager mgr = (IBltMessageManager)mgrControl.getMock();
    
    classUnderTest = new BeastScheduler();
    classUnderTest.setMessageManager(mgr);

    try {
      classUnderTest.createJob(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException npe) {
      // pass
    }
  }
  
  public void testCreateJob_nullId() throws Exception {
    MockControl mgrControl = MockControl.createControl(IBltMessageManager.class);
    IBltMessageManager mgr = (IBltMessageManager)mgrControl.getMock();
    
    classUnderTest = new BeastScheduler();
    classUnderTest.setMessageManager(mgr);

    try {
      classUnderTest.createJob(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException npe) {
      // pass
    }
  }
}
