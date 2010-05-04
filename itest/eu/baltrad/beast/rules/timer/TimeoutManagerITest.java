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

import org.easymock.MockControl;
import org.springframework.context.ApplicationContext;

import eu.baltrad.beast.ManagerContext;
import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.manager.IBltMessageManager;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.rules.IScriptableRule;

/**
 * @author Anders Henja
 *
 */
public class TimeoutManagerITest extends TestCase {
  private ApplicationContext context = null;
  private TimeoutManager classUnderTest = null;
  
  private MockControl managerControl = null;
  private IBltMessageManager manager = null;
  
  public TimeoutManagerITest(String name) {
    super(name);
    context = BeastDBTestHelper.loadContext(this);
  }
  
  /**
   * Setup of test
   */
  public void setUp() throws Exception {
    managerControl = MockControl.createControl(IBltMessageManager.class);
    manager = (IBltMessageManager)managerControl.getMock();
    classUnderTest = (TimeoutManager)context.getBean("timeoutmanager");
    classUnderTest.setMessageManager(manager);
  }
  
  /**
   * Teardown of test
   */
  public void tearDown() throws Exception {
    managerControl = null;
    manager = null;
    classUnderTest = null;
  }  
  
  protected void replay() {
    managerControl.replay();
  }
  
  protected void verify() {
    managerControl.verify();
  }
  
  private static class TimeoutValues {
    public long id;
    public int why;
    public TimeoutValues() {
      id = 0;
      why = 0;
    }
  }
  
  public void testTimeout() {
    final TimeoutValues result = new TimeoutValues();
    final IBltMessage message = new IBltMessage() {};
    
    ITimeoutRule rule = new ITimeoutRule() {
      public synchronized IBltMessage timeout(long id, int why, Object data) {
        result.id = id;
        result.why = why;
        notify();
        return message;
      }
    };
    
    manager.manage(message);
    
    replay();
    
    long id = classUnderTest.register(rule, 1000, null);
    synchronized(rule) {
      try {
        rule.wait(1500);
      } catch (Throwable t) {
      }
    }
    verify();
    assertEquals(ITimeoutRule.TIMEOUT, result.why);
    assertEquals(id, result.id);
  }

  public void testTimeout_noReturnedMessage() {
    final TimeoutValues result = new TimeoutValues();
    
    ITimeoutRule rule = new ITimeoutRule() {
      public synchronized IBltMessage timeout(long id, int why, Object data) {
        result.id = id;
        result.why = why;
        notify();
        return null;
      }
    };
    
    replay();
    
    long id = classUnderTest.register(rule, 1000, null);
    synchronized(rule) {
      try {
        rule.wait(1500);
      } catch (Throwable t) {
      }
    }
    verify();
    assertEquals(ITimeoutRule.TIMEOUT, result.why);
    assertEquals(id, result.id);
  }
  
  public void testCancel() {
    final TimeoutValues result = new TimeoutValues();
    final IBltMessage message = new IBltMessage() {};
    
    ITimeoutRule rule = new ITimeoutRule() {
      public IBltMessage timeout(long id, int why, Object data) {
        result.id = id;
        result.why = why;
        return message;
      }
    };
    
    manager.manage(message);

    replay();
    
    long id = classUnderTest.register(rule, 1000, null);
    classUnderTest.cancel(id);
    synchronized(rule) {
      try {
        rule.wait(1500);
      } catch (Throwable t) {
      }
    }
    verify();
    
    assertEquals(ITimeoutRule.CANCELLED, result.why);
    assertEquals(id, result.id);
  }

  public void testCancel_noReturnedMessage() {
    final TimeoutValues result = new TimeoutValues();
    
    ITimeoutRule rule = new ITimeoutRule() {
      public IBltMessage timeout(long id, int why, Object data) {
        result.id = id;
        result.why = why;
        return null;
      }
    };
    
    replay();
    
    long id = classUnderTest.register(rule, 1000, null);
    classUnderTest.cancel(id);
    synchronized(rule) {
      try {
        rule.wait(1500);
      } catch (Throwable t) {
      }
    }
    verify();
    assertEquals(ITimeoutRule.CANCELLED, result.why);
    assertEquals(id, result.id);
  }
  
  private static class ExampleRule implements IScriptableRule, ITimeoutRule {
    private long id = -1;
    private long timeoutId = -1;
    private int why = -1;
    private IBltMessage resultMessage = null;
    
    public IBltMessage handle(IBltMessage message) {
      if (id < 0) {
        id = ManagerContext.getTimeoutManager().register(this, 1000, null);
      } else {
        ManagerContext.getTimeoutManager().cancel(id);
        id = -1;
      }
      return null;
    }
    
    public IBltMessage timeout(long id, int why, Object data) {
      this.timeoutId = id;
      this.why = why;
      return resultMessage;
    }
    
    public long getId() {
      return this.id;
    }
    
    public long getTimeoutId() {
      return this.timeoutId;
    }
    
    public int getWhy() {
      return this.why;
    }
  };
  
  public void testRule_timeout() {
    IBltMessage message = new IBltMessage() {};
    ExampleRule rule = new ExampleRule();
    rule.handle(message);
    synchronized (rule) {
      try {
        rule.wait(1500);
      } catch (Throwable t) {
        
      }
    }
    assertEquals(rule.getId(), rule.getTimeoutId());
    assertEquals(ITimeoutRule.TIMEOUT, rule.getWhy());
  }
  
  public void testRule_cancel() {
    long id = -1;
    IBltMessage message = new IBltMessage() {};
    IBltMessage message2 = new IBltMessage() {};
    ExampleRule rule = new ExampleRule();
    rule.handle(message);
    id = rule.getId();
    rule.handle(message2);
    
    synchronized (rule) {
      try {
        rule.wait(500);
      } catch (Throwable t) {
        
      }
    }
    assertEquals(id, rule.getTimeoutId());
    assertEquals(ITimeoutRule.CANCELLED, rule.getWhy());
  }
}
