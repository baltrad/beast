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

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.quartz.Scheduler;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.JobDetailBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import eu.baltrad.beast.manager.IBltMessageManager;
import eu.baltrad.beast.message.IBltMessage;

/**
 * @author Anders Henja
 *
 */
public class BeastSchedulerTest extends TestCase {
  static interface MockMethods {
    public CronTriggerBean createTrigger(String id, String expression);
    public JobDetailBean createJob(String id, IBeastJob job);
  };
  
  private MockControl sfControl = null;
  private SchedulerFactoryBean sfBean = null;
  private MockControl methodsControl = null;
  private MockMethods methods = null;
  
  private BeastScheduler classUnderTest = null;
  
  public void setUp() throws Exception {
    super.setUp();
    sfControl = MockClassControl.createControl(SchedulerFactoryBean.class);
    sfBean = (SchedulerFactoryBean)sfControl.getMock();
    methodsControl = MockControl.createControl(MockMethods.class);
    methods = (MockMethods)methodsControl.getMock();
    
    classUnderTest = new BeastScheduler() {
      protected CronTriggerBean createTrigger(String id, String expression) {
        return methods.createTrigger(id, expression);
      }
      protected JobDetailBean createJob(String id, IBeastJob job) {
        return methods.createJob(id, job);
      }
    };
    classUnderTest.setSchedulerFactoryBean(sfBean);
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
    sfBean = null;
    sfControl = null;
    methods = null;
    methodsControl = null;
    classUnderTest = null;
  }
  
  protected void replay() {
    sfControl.replay();
    methodsControl.replay();
  }
  
  public void verify() {
    sfControl.verify();
    methodsControl.verify();
  }
  
  public void testRegister() throws Exception {
    MockControl schedControl = MockControl.createControl(Scheduler.class);
    Scheduler scheduler = (Scheduler)schedControl.getMock();
    
    CronTriggerBean trigger = new CronTriggerBean();
    JobDetailBean detail = new JobDetailBean();
    IBeastJob job = new IBeastJob() {
      public IBltMessage trigger() {
        return null;
      }
    };
    methods.createTrigger("abc", "0 * * * * ?");
    methodsControl.setReturnValue(trigger);
    methods.createJob("abc", job);
    methodsControl.setReturnValue(detail);
    sfBean.getScheduler();
    sfControl.setReturnValue(scheduler);
    scheduler.scheduleJob(detail, trigger);
    schedControl.setReturnValue(null);
    
    replay();
    schedControl.replay();
    
    classUnderTest.register("abc", "0 * * * * ?", job);
    
    verify();
    schedControl.verify();
  }
  
  public void testRegister_createTriggerThrowsException() throws Exception {
    IBeastJob job = new IBeastJob() {
      public IBltMessage trigger() {
        return null;
      }
    };
    SchedulerException schedulerException = new SchedulerException();
    methods.createTrigger("abc", "0 a * * * ?");
    methodsControl.setThrowable(schedulerException);
    
    replay();

    try {
      classUnderTest.register("abc", "0 a * * * ?", job);
      fail("Expected SchedulerException");
    } catch (SchedulerException se) {
      // pass
    }
    
    verify();
  }

  public void testRegister_scheduleJobThrowsException() throws Exception {
    MockControl schedControl = MockControl.createControl(Scheduler.class);
    Scheduler scheduler = (Scheduler)schedControl.getMock();
    
    CronTriggerBean trigger = new CronTriggerBean();
    JobDetailBean detail = new JobDetailBean();
    IBeastJob job = new IBeastJob() {
      public IBltMessage trigger() {
        return null;
      }
    };
    org.quartz.SchedulerException scheduleException = new org.quartz.SchedulerException();
    methods.createTrigger("abc", "0 * * * * ?");
    methodsControl.setReturnValue(trigger);
    methods.createJob("abc", job);
    methodsControl.setReturnValue(detail);
    sfBean.getScheduler();
    sfControl.setReturnValue(scheduler);
    scheduler.scheduleJob(detail, trigger);
    schedControl.setThrowable(scheduleException);
    
    replay();
    schedControl.replay();
    
    try {
      classUnderTest.register("abc", "0 * * * * ?", job);
      fail("Expected SchedulerException");
    } catch (SchedulerException se) {
      // pass
    }
    
    verify();
    schedControl.verify();
  }

  public void testUnegister() throws Exception {
    MockControl schedControl = MockControl.createControl(Scheduler.class);
    Scheduler scheduler = (Scheduler)schedControl.getMock();
    
    sfBean.getScheduler();
    sfControl.setReturnValue(scheduler);
    scheduler.deleteJob("abc", "beast");
    schedControl.setReturnValue(true);
    
    replay();
    schedControl.replay();
    
    classUnderTest.unregister("abc");
    
    verify();
    schedControl.verify();
  }
  
  public void testUnegister_deleteThrowsException() throws Exception {
    MockControl schedControl = MockControl.createControl(Scheduler.class);
    Scheduler scheduler = (Scheduler)schedControl.getMock();
    org.quartz.SchedulerException schedulerException = new org.quartz.SchedulerException();
    sfBean.getScheduler();
    sfControl.setReturnValue(scheduler);
    scheduler.deleteJob("abc", "beast");
    schedControl.setThrowable(schedulerException);
    
    replay();
    schedControl.replay();
    
    try {
      classUnderTest.unregister("abc");
      fail("Expected SchedulerException");
    } catch (SchedulerException e) {
      // pass
    }
    
    verify();
    schedControl.verify();
  }
  public void testCreateTrigger() throws Exception {
    classUnderTest = new BeastScheduler();
    
    CronTriggerBean result = classUnderTest.createTrigger("a", "0 * * * * ?");
    assertEquals("a", result.getName());
    assertEquals("0 * * * * ?", result.getCronExpression());
  }
 
  public void testCreateTrigger_badExpression() throws Exception {
    classUnderTest = new BeastScheduler();
    
    try {
      classUnderTest.createTrigger("a", "0 * * ?");
      fail("Expected SchedulerException");
    } catch (SchedulerException e) {
      // pass
    }
  }
  
  public void testCreateJob() throws Exception {
    MockControl mgrControl = MockControl.createControl(IBltMessageManager.class);
    IBltMessageManager mgr = (IBltMessageManager)mgrControl.getMock();
    
    IBeastJob job = new IBeastJob() {
      public IBltMessage trigger() {
        return null;
      }
    };
    
    classUnderTest = new BeastScheduler();
    classUnderTest.setMessageManager(mgr);
    
    JobDetailBean result = classUnderTest.createJob("abc", job);
    
    assertEquals("abc", result.getName());
    assertEquals("beast", result.getGroup());
    assertSame(BeastJobInvoker.class, result.getJobClass());
    assertSame(job, result.getJobDataMap().get("job"));
    assertSame(mgr, result.getJobDataMap().get("messageManager"));
  }
}
