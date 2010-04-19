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

import java.util.Timer;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import eu.baltrad.beast.message.IBltMessage;
import junit.framework.TestCase;


/**
 * @author Anders Henja
 */
public class TimeoutManagerTest extends TestCase {
  private MockControl factoryControl = null;
  private ITimeoutTaskFactory factory = null;
  private MockControl timerControl = null;
  private Timer timer = null;
  
  private TimeoutManager classUnderTest = null;
  
  public void setUp() throws Exception {
    super.setUp();
    factoryControl = MockControl.createControl(ITimeoutTaskFactory.class);
    factory = (ITimeoutTaskFactory)factoryControl.getMock();
    timerControl = MockClassControl.createControl(Timer.class);
    timer = (Timer)timerControl.getMock();
    
    classUnderTest = new TimeoutManager() {
      protected synchronized long newID() {
        return 0;
      }
    };
    classUnderTest.setTimer(timer);
    classUnderTest.setFactory(factory);
  }
  
  public void tearDown() throws Exception {
    classUnderTest = null;
    super.tearDown();
  }
  
  protected void replay() {
    factoryControl.replay();
  }
  
  protected void verify() {
    factoryControl.verify();
  }
  
  public void testRegister() throws Exception {
    ITimeoutRule rule = new ITimeoutRule() {
      public IBltMessage timeout(long id, int why) {return null;}
    };
    TimeoutTask task = new TimeoutTask();
    
    factory.create(rule, 0, classUnderTest);
    factoryControl.setReturnValue(task);
    timer.schedule(task, 1000);
    
    replay();
    
    long result = classUnderTest.register(rule, 1000);
    
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
    
    rule.timeout(1, ITimeoutRule.CANCELLED);
    ruleControl.setReturnValue(msg);
    
    replay();
    ruleControl.replay();
    
    classUnderTest.cancelNotification(1, rule);
   
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
    
    rule.timeout(1, ITimeoutRule.TIMEOUT);
    ruleControl.setReturnValue(msg);
    
    replay();
    ruleControl.replay();
    
    classUnderTest.timeoutNotification(1, rule);
   
    verify();
    ruleControl.verify();
    assertEquals(null, classUnderTest.tasks.get((long)1));
  }
  
}
