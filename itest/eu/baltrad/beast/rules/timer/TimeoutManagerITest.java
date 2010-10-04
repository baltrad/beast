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

import junit.framework.TestCase;

import eu.baltrad.beast.ManagerContext;
import eu.baltrad.beast.manager.IBltMessageManager;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.rules.IScriptableRule;

/**
 * @author Anders Henja
 */
public class TimeoutManagerITest extends TestCase {
  private TimeoutManager classUnderTest = null;
  private TimeoutTaskFactory factory = null;
  
  private TestMsgManager manager = null;
  
  public TimeoutManagerITest(String name) {
    super(name);
  }
  
  private static class TestMsgManager implements IBltMessageManager {
    private IBltMessage msg = null;
    public synchronized IBltMessage getMessage() {
      return this.msg;
    }
    
    @Override
    public synchronized void manage(IBltMessage message) {
      this.msg = message;
    }
    
    @Override
    public void shutdown() {
    }
  }
  
  /**
   * Setup of test
   */
  public void setUp() throws Exception {
    manager = new TestMsgManager();
    factory = new TimeoutTaskFactory();
    classUnderTest = new TimeoutManager();
    classUnderTest.setFactory(factory);
    classUnderTest.setMessageManager(manager);
    
    // Initialize the Manager context
    new ManagerContext().setTimeoutManager(classUnderTest);
  }
  
  /**
   * Teardown of test
   */
  public void tearDown() throws Exception {
    manager = null;
    factory = null;
    classUnderTest.destroy();
    classUnderTest = null;
  }  
  
  /**
   * Test message used for passing in some relevant information
   * to the test rule.
   */
  private static class TimeoutMessage implements IBltMessage {
    private final static int REGISTER = 0;
    private final static int CANCEL = 1;
    private int operation = REGISTER;
    private Object trigger = null;
    public TimeoutMessage(int operation, Object trigger) {
      this.operation = operation;
      this.trigger = trigger;
    }
    public int getOperation() {
      return this.operation;
    }
    public Object getTrigger() {
      return this.trigger;
    }
  }
  
  /**
   * Test rule that implements both the timeout rule and the scriptable rule
   */
  private static class SimpleTimeoutRule implements ITimeoutRule, IScriptableRule {
    long id = 0;
    long ruleid = 0;
    int why = 0;
    volatile Object data = null;
    IBltMessage result = null;
    
    public SimpleTimeoutRule(IBltMessage result) {
      this.result = result;
    }
    
    @Override
    public synchronized IBltMessage timeout(long id, int why, Object data) {
      this.id = id;
      this.why = why;
      this.data = data;
      notifyAll();
      return result;
    }
    
    public synchronized int waitForTimeout(long timeout, Object data) {
      long currtime = System.currentTimeMillis();
      long endtime = currtime + timeout;
      while (data != this.data && (currtime < endtime)) {
        try {
          wait((endtime - currtime));
        } catch (Throwable t) {
        }
        currtime = System.currentTimeMillis();
      }
      notifyAll();
      return this.why;
    }

    @Override
    public IBltMessage handle(IBltMessage message) {
      TimeoutMessage msg = (TimeoutMessage)message;
      if (msg.getOperation() == TimeoutMessage.REGISTER) {
        ruleid = ManagerContext.getTimeoutManager().register(this, 1000, msg.getTrigger());
      } else {
        ManagerContext.getTimeoutManager().cancel(ruleid);
      }
      return null;
    }
  };
  
  public void testTimeout() {
    IBltMessage message = new IBltMessage() {};
    Object trigger = new Object();
    SimpleTimeoutRule rule = new SimpleTimeoutRule(message);
    
    long id = classUnderTest.register(rule, 1000, trigger);
    int why = rule.waitForTimeout(1500, trigger);
    
    assertEquals(ITimeoutRule.TIMEOUT, why);
    assertEquals(id, rule.id);
    assertSame(trigger, rule.data);
    assertSame(message, manager.getMessage());
  }

  public void testTimeout_noReturnedMessage() {
    Object trigger = new Object();
    SimpleTimeoutRule rule = new SimpleTimeoutRule(null);
    
    long id = classUnderTest.register(rule, 1000, trigger);
    int why = rule.waitForTimeout(1500, trigger);
    
    assertEquals(ITimeoutRule.TIMEOUT, why);
    assertEquals(id, rule.id);
    assertSame(trigger, rule.data);
    assertEquals(null, manager.getMessage());
  }
  
  public void testCancel() {
    IBltMessage message = new IBltMessage() {};
    Object trigger = new Object();
    SimpleTimeoutRule rule = new SimpleTimeoutRule(message);
    
    long id = classUnderTest.register(rule, 1000, trigger);
    classUnderTest.cancel(id);
    int why = rule.waitForTimeout(1500, trigger);
    
    assertEquals(ITimeoutRule.CANCELLED, why);
    assertEquals(id, rule.id);
    assertSame(trigger, rule.data);
    assertSame(message, manager.getMessage());
  }

  public void testCancel_noReturnedMessage() {
    Object trigger = new Object();
    SimpleTimeoutRule rule = new SimpleTimeoutRule(null);
    
    long id = classUnderTest.register(rule, 1000, trigger);
    classUnderTest.cancel(id);
    int why = rule.waitForTimeout(1500, trigger);

    assertEquals(ITimeoutRule.CANCELLED, why);
    assertEquals(id, rule.id);
    assertSame(trigger, rule.data);
    assertEquals(null, manager.getMessage());
  }

  public void testRule_timeout() {
    Object trigger = new Object();
    IBltMessage result = new IBltMessage() {};
    TimeoutMessage msg = new TimeoutMessage(TimeoutMessage.REGISTER, trigger);
    
    SimpleTimeoutRule rule = new SimpleTimeoutRule(result);
    rule.handle(msg);
    
    rule.waitForTimeout(1500, trigger);
    assertEquals(rule.id, rule.ruleid);
    assertEquals(ITimeoutRule.TIMEOUT, rule.why);
    assertSame(trigger, rule.data);
    assertSame(result, manager.getMessage());
  }

  public void testRule_timeoutNoMessage() {
    Object trigger = new Object();
    TimeoutMessage msg = new TimeoutMessage(TimeoutMessage.REGISTER, trigger);

    SimpleTimeoutRule rule = new SimpleTimeoutRule(null);
    rule.handle(msg);
    
    rule.waitForTimeout(1500, trigger);
    
    assertEquals(rule.id, rule.ruleid);
    assertEquals(ITimeoutRule.TIMEOUT, rule.why);
    assertSame(trigger, rule.data);
    assertEquals(null, manager.getMessage());
  }

  
  public void testRule_cancel() {
    Object trigger = new Object();
    IBltMessage result = new IBltMessage() {};
    TimeoutMessage regmsg = new TimeoutMessage(TimeoutMessage.REGISTER, trigger);
    TimeoutMessage cancelmsg = new TimeoutMessage(TimeoutMessage.CANCEL, trigger);
    
    SimpleTimeoutRule rule = new SimpleTimeoutRule(result);
    rule.handle(regmsg);
    rule.handle(cancelmsg);
    
    rule.waitForTimeout(1500, trigger);
    
    assertEquals(rule.id, rule.ruleid);
    assertEquals(ITimeoutRule.CANCELLED, rule.why);
    assertSame(trigger, rule.data);
    assertSame(result, manager.getMessage());
  }

  public void testRule_cancelNoMessage() {
    Object trigger = new Object();
    TimeoutMessage regmsg = new TimeoutMessage(TimeoutMessage.REGISTER, trigger);
    TimeoutMessage cancelmsg = new TimeoutMessage(TimeoutMessage.CANCEL, trigger);
    
    SimpleTimeoutRule rule = new SimpleTimeoutRule(null);
    rule.handle(regmsg);
    rule.handle(cancelmsg);
    
    rule.waitForTimeout(1500, trigger);
    
    assertEquals(rule.id, rule.ruleid);
    assertEquals(ITimeoutRule.CANCELLED, rule.why);
    assertSame(trigger, rule.data);
    assertEquals(null, manager.getMessage());
  }
}
