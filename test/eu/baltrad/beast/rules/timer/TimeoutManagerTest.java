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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.List;
import java.util.Timer;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.manager.IBltMessageManager;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.rules.IRule;

/**
 * @author Anders Henja
 */
public class TimeoutManagerTest extends EasyMockSupport {
  private ITimeoutTaskFactory factory = null;
  private Timer timer = null;
  private IBltMessageManager manager = null;
  
  private TimeoutManager classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    factory = createMock(ITimeoutTaskFactory.class);
    timer = createMock(Timer.class);
    manager = createMock(IBltMessageManager.class);
    
    classUnderTest = new TimeoutManager() {
      protected synchronized long newID() {
        return 0;
      }
    };
    classUnderTest.setTimer(timer);
    classUnderTest.setFactory(factory);
    classUnderTest.setMessageManager(manager);
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
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

  @Test
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

  @Test
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
  
  @Test
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

  @Test
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

  @Test
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
  
  @Test
  public void testRegister() throws Exception {
    ITimeoutRule rule = new ITimeoutRule() {
      public IBltMessage timeout(long id, int why, Object data) {return null;}
      public void setRecipients(List<String> recipients) {}
    };
    TimeoutTask task = new TimeoutTask();
    
    expect(factory.create(rule, 0, null, classUnderTest)).andReturn(task);
    timer.schedule(task, 1000);
    
    replayAll();
    
    long result = classUnderTest.register(rule, 1000, null);
    
    verifyAll();
    assertSame(task, classUnderTest.tasks.get(result));
  }
  
  @Test
  public void testCancel() throws Exception {
    TimeoutTask task = createMock(TimeoutTask.class);

    expect(task.cancel()).andReturn(false);
    
    classUnderTest.tasks.put((long)0, task);
    
    replayAll();
    
    classUnderTest.cancel(0);
    
    verifyAll();
  }

  @Test
  public void testCancel_noSuchId() throws Exception {
    TimeoutTask task = createMock(TimeoutTask.class);

    classUnderTest.tasks.put((long)0, task);
    
    replayAll();
    
    classUnderTest.cancel(1);
    
    verifyAll();
  }
 
  @Test
  public void testUnregister() throws Exception {
    TimeoutTask task = createMock(TimeoutTask.class);

    classUnderTest.tasks.put((long)0, task);
    
    expect(task.stop()).andReturn(true);
    
    replayAll();

    classUnderTest.unregister(0);
    
    verifyAll();
  }
  
  @Test
  public void testNewId() {
    classUnderTest = new TimeoutManager();
    classUnderTest.setStartID(10);
    assertEquals((long)10, classUnderTest.newID());
    assertEquals((long)11, classUnderTest.newID());
    assertEquals((long)12, classUnderTest.newID());
    assertEquals((long)13, classUnderTest.newID());
  }
  
  @Test
  public void testCancelNotification() {
    ITimeoutRule rule = createMock(ITimeoutRule.class);
    
    IBltMessage msg = new IBltMessage() {
    };
    
    TimeoutTask task = new TimeoutTask();
    classUnderTest.tasks.put((long)1, task);
    
    expect(rule.timeout(1, ITimeoutRule.CANCELLED, null)).andReturn(msg);
    
    manager.manage(msg);
    
    replayAll();
    
    classUnderTest.cancelNotification(1, rule, null);
   
    verifyAll();
    assertEquals(null, classUnderTest.tasks.get((long)1));
  }

  @Test
  public void testTimeoutNotification() {
    ITimeoutRule rule = createMock(ITimeoutRule.class);

    IBltMessage msg = new IBltMessage() {
    };

    TimeoutTask task = new TimeoutTask();
    classUnderTest.tasks.put((long)1, task);
    
    expect(rule.timeout(1, ITimeoutRule.TIMEOUT, null)).andReturn(msg);
    
    manager.manage(msg);
    
    replayAll();
    
    classUnderTest.timeoutNotification(1, rule, null);
   
    verifyAll();
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
    public boolean isValid() {
      return true;
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
  
  @Test
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
