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
package eu.baltrad.beast.rules.timer;

import java.util.List;
import java.util.Timer;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import eu.baltrad.beast.manager.IBltMessageManager;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.rules.IRule;
import junit.framework.TestCase;


/**
 * @author Anders Henja
 */
public class TimeoutManagerTest extends TestCase {
  private MockControl factoryControl = null;
  private ITimeoutTaskFactory factory = null;
  private MockControl timerControl = null;
  private Timer timer = null;
  private MockControl managerControl = null;
  private IBltMessageManager manager = null;
  
  private TimeoutManager classUnderTest = null;
  
  public void setUp() throws Exception {
    super.setUp();
    factoryControl = MockControl.createControl(ITimeoutTaskFactory.class);
    factory = (ITimeoutTaskFactory)factoryControl.getMock();
    timerControl = MockClassControl.createControl(Timer.class);
    timer = (Timer)timerControl.getMock();
    managerControl = MockControl.createControl(IBltMessageManager.class);
    manager = (IBltMessageManager)managerControl.getMock();
    
    classUnderTest = new TimeoutManager() {
      protected synchronized long newID() {
        return 0;
      }
    };
    classUnderTest.setTimer(timer);
    classUnderTest.setFactory(factory);
    classUnderTest.setMessageManager(manager);
  }
  
  public void tearDown() throws Exception {
    classUnderTest = null;
    super.tearDown();
  }
  
  protected void replay() {
    factoryControl.replay();
    timerControl.replay();
    managerControl.replay();
  }
  
  protected void verify() {
    factoryControl.verify();
    timerControl.verify();
    managerControl.verify();
  }
  
  public void testIsRegistered() throws Exception {
    TimeoutTask t1 = new TimeoutTask();
    t1.setData(new String("ABC"));  // By using new String().. We know that == wont say equal since string caching will not be used
    TimeoutTask t2 = new TimeoutTask();
    t2.setData(new String("DEF"));
    TimeoutTask t3 = new TimeoutTask();
    t3.setData(new String("GHI"));
    classUnderTest.tasks.put(new Long(1), t1);
    classUnderTest.tasks.put(new Long(2), t2);
    classUnderTest.tasks.put(new Long(3), t3);
    
    assertEquals(false, classUnderTest.isRegistered(new String("ADE")));
    assertEquals(true, classUnderTest.isRegistered(new String("ABC")));
    assertEquals(true, classUnderTest.isRegistered(new String("GHI")));
  }

  public void testIsRegistered_nullData() throws Exception {
    TimeoutTask t1 = new TimeoutTask();
    t1.setData(new String("ABC"));  // By using new String().. We know that == wont say equal since string caching will not be used
    TimeoutTask t2 = new TimeoutTask();
    t2.setData(null);
    TimeoutTask t3 = new TimeoutTask();
    t3.setData(new String("GHI"));
    classUnderTest.tasks.put(new Long(1), t1);
    classUnderTest.tasks.put(new Long(2), t2);
    classUnderTest.tasks.put(new Long(3), t3);
    
    assertEquals(false, classUnderTest.isRegistered(new String("ADE")));
  }

  public void testIsRegistered_nullTest() throws Exception {
    TimeoutTask t1 = new TimeoutTask();
    t1.setData(new String("ABC"));  // By using new String().. We know that == wont say equal since string caching will not be used
    TimeoutTask t2 = new TimeoutTask();
    t2.setData(new String("DEF"));
    TimeoutTask t3 = new TimeoutTask();
    t3.setData(new String("GHI"));
    classUnderTest.tasks.put(new Long(1), t1);
    classUnderTest.tasks.put(new Long(2), t2);
    classUnderTest.tasks.put(new Long(3), t3);
    
    assertEquals(false, classUnderTest.isRegistered(null));
  }
  
  public void testGetRegisteredTask() throws Exception {
    TimeoutTask t1 = new TimeoutTask();
    t1.setData(new String("ABC"));  // By using new String().. We know that == wont say equal since string caching will not be used
    TimeoutTask t2 = new TimeoutTask();
    t2.setData(new String("DEF"));
    TimeoutTask t3 = new TimeoutTask();
    t3.setData(new String("GHI"));
    classUnderTest.tasks.put(new Long(1), t1);
    classUnderTest.tasks.put(new Long(2), t2);
    classUnderTest.tasks.put(new Long(3), t3);

    TimeoutTask r1 = classUnderTest.getRegisteredTask("ABC");
    TimeoutTask r2 = classUnderTest.getRegisteredTask("DEF");
    TimeoutTask r3 = classUnderTest.getRegisteredTask("GHI");
    TimeoutTask r4 = classUnderTest.getRegisteredTask("HIJ");

    assertSame(t1, r1);
    assertSame(t2, r2);
    assertSame(t3, r3);
    assertEquals(null, r4);
  }

  public void testGetRegisteredTask_nullData() throws Exception {
    TimeoutTask t1 = new TimeoutTask();
    t1.setData(new String("ABC"));  // By using new String().. We know that == wont say equal since string caching will not be used
    TimeoutTask t2 = new TimeoutTask();
    TimeoutTask t3 = new TimeoutTask();
    t3.setData(new String("GHI"));
    classUnderTest.tasks.put(new Long(1), t1);
    classUnderTest.tasks.put(new Long(2), t2);
    classUnderTest.tasks.put(new Long(3), t3);

    TimeoutTask r1 = classUnderTest.getRegisteredTask("ABC");
    TimeoutTask r2 = classUnderTest.getRegisteredTask("DEF");
    TimeoutTask r3 = classUnderTest.getRegisteredTask("GHI");
    TimeoutTask r4 = classUnderTest.getRegisteredTask("HIJ");

    assertSame(t1, r1);
    assertEquals(null, r2);
    assertSame(t3, r3);
    assertEquals(null, r4);
  }

  public void testGetRegisteredTask_null() throws Exception {
    TimeoutTask t1 = new TimeoutTask();
    t1.setData(new String("ABC"));  // By using new String().. We know that == wont say equal since string caching will not be used
    TimeoutTask t2 = new TimeoutTask();
    t2.setData(new String("DEF"));
    TimeoutTask t3 = new TimeoutTask();
    t3.setData(new String("GHI"));
    classUnderTest.tasks.put(new Long(1), t1);
    classUnderTest.tasks.put(new Long(2), t2);
    classUnderTest.tasks.put(new Long(3), t3);
    TimeoutTask r1 = classUnderTest.getRegisteredTask(null);
    assertSame(null, r1);
  }
  
  public void testRegister() throws Exception {
    ITimeoutRule rule = new ITimeoutRule() {
      public IBltMessage timeout(long id, int why, Object data) {return null;}
      public void setRecipients(List<String> recipients) {}
    };
    TimeoutTask task = new TimeoutTask();
    
    factory.create(rule, 0, null, classUnderTest);
    factoryControl.setReturnValue(task);
    timer.schedule(task, 1000);
    
    replay();
    
    long result = classUnderTest.register(rule, 1000, null);
    
    verify();
    assertSame(task, classUnderTest.tasks.get(result));
  }
  
  public void testCancel() throws Exception {
    MockControl taskControl = MockClassControl.createControl(TimeoutTask.class);
    TimeoutTask task = (TimeoutTask)taskControl.getMock();

    task.cancel();
    taskControl.setReturnValue(false);
    
    classUnderTest.tasks.put((long)0, task);
    
    replay();
    taskControl.replay();
    
    classUnderTest.cancel(0);
    
    verify();
    taskControl.verify();
  }

  public void testCancel_noSuchId() throws Exception {
    MockControl taskControl = MockClassControl.createControl(TimeoutTask.class);
    TimeoutTask task = (TimeoutTask)taskControl.getMock();

    classUnderTest.tasks.put((long)0, task);
    
    replay();
    taskControl.replay();
    
    classUnderTest.cancel(1);
    
    verify();
    taskControl.verify();
  }
 
  public void testUnregister() throws Exception {
    MockControl taskControl = MockClassControl.createControl(TimeoutTask.class);
    TimeoutTask task = (TimeoutTask)taskControl.getMock();

    classUnderTest.tasks.put((long)0, task);
    
    task.stop();
    taskControl.setReturnValue(true);
    
    replay();
    taskControl.replay();

    classUnderTest.unregister(0);
    
    verify();
    taskControl.verify();
  }
  
  public void testNewId() {
    classUnderTest = new TimeoutManager();
    classUnderTest.setStartID(10);
    assertEquals((long)10, classUnderTest.newID());
    assertEquals((long)11, classUnderTest.newID());
    assertEquals((long)12, classUnderTest.newID());
    assertEquals((long)13, classUnderTest.newID());
  }
  
  public void testCancelNotification() {
    MockControl ruleControl = MockControl.createControl(ITimeoutRule.class);
    ITimeoutRule rule = (ITimeoutRule)ruleControl.getMock();
    
    IBltMessage msg = new IBltMessage() {
    };
    
    TimeoutTask task = new TimeoutTask();
    classUnderTest.tasks.put((long)1, task);
    
    rule.timeout(1, ITimeoutRule.CANCELLED, null);
    ruleControl.setReturnValue(msg);
    
    manager.manage(msg);
    
    replay();
    ruleControl.replay();
    
    classUnderTest.cancelNotification(1, rule, null);
   
    verify();
    ruleControl.verify();
    assertEquals(null, classUnderTest.tasks.get((long)1));
  }

  public void testTimeoutNotification() {
    MockControl ruleControl = MockControl.createControl(ITimeoutRule.class);
    ITimeoutRule rule = (ITimeoutRule)ruleControl.getMock();

    IBltMessage msg = new IBltMessage() {
    };

    TimeoutTask task = new TimeoutTask();
    classUnderTest.tasks.put((long)1, task);
    
    rule.timeout(1, ITimeoutRule.TIMEOUT, null);
    ruleControl.setReturnValue(msg);
    
    manager.manage(msg);
    
    replay();
    ruleControl.replay();
    
    classUnderTest.timeoutNotification(1, rule, null);
   
    verify();
    ruleControl.verify();
    assertEquals(null, classUnderTest.tasks.get((long)1));
  }
  
  public static class SimpleRule implements IRule, ITimeoutRule {
    private TimeoutManager mgr = null;
    private Object timerdata = new Object();
    private long id = -1;
    private boolean registered = false;
    private long timeoutId = -1;
    private Object timeoutData = null;
    private int why = -1;
    
    @Override
    public String getType() {
      return null;
    }
    @Override
    public synchronized IBltMessage handle(IBltMessage message) {
      id = mgr.register(this, 500, timerdata);
      try {
        Thread.sleep(2000);
      } catch (Throwable t) {
      }
      registered = mgr.isRegistered(timerdata);
      return null;
    }

    @Override
    public synchronized IBltMessage timeout(long id, int why, Object data) {
      this.why = why;
      this.timeoutId = id;
      this.timeoutData = data;
      return null;
    }

    @Override
    public void setRecipients(List<String> recipients) {
    }
    
    public void setTimeoutManager(TimeoutManager mgr) {
      this.mgr = mgr;
    }
    
    public boolean isRegistered() {
      return registered;
    }
    public long getId() {
      return id;
    }
    public Object getTimerdata() {
      return timerdata;
    }
    public long getTimeoutId() {
      return timeoutId;
    }
    public Object getTimeoutData() {
      return timeoutData;
    }
    public int getWhy() {
      return why;
    }
  };
  
  public static class DeadlockRuleRunner extends Thread {
    private Object lock = new Object();
    private SimpleRule rule = null;
    private volatile boolean finished = false;
    
    public DeadlockRuleRunner(SimpleRule rule) {
      this.rule = rule;
    }
    public void run() {
      rule.handle(null);
      finished = true;
      try {
        Thread.sleep(500);
      } catch (Throwable t) {
      }
      synchronized (lock) {
        lock.notify();
      }
    }

    public boolean waitForLock(long timeout) {
      long now = System.currentTimeMillis();
      long endtime = now + timeout;
      synchronized (lock) {
        try {
          while(!finished && now < endtime) {
            lock.wait((endtime-now));
            now = System.currentTimeMillis();
          }
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
      return finished;
    }
  }
  
  public void testDeadlock() throws Exception {
    // Setup
    TimeoutManager mgr = new TimeoutManager();
    TimeoutTaskFactory factory = new TimeoutTaskFactory();
    mgr.setFactory(factory);
    SimpleRule rule = new SimpleRule();
    rule.setTimeoutManager(mgr);
    
    DeadlockRuleRunner runner = new DeadlockRuleRunner(rule);
    runner.start();
    // Execute test
    boolean finished = runner.waitForLock(3000);

    // Verify
    assertEquals(true, finished);
    assertSame(rule.getTimerdata(), rule.getTimeoutData());
    assertEquals(rule.getId(), rule.getTimeoutId());
    assertFalse(rule.isRegistered());
  }
}
