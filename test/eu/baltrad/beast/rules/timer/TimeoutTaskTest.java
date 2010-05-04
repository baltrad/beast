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

import eu.baltrad.beast.message.IBltMessage;

/**
 * @author Anders Henja
 */
public class TimeoutTaskTest extends TestCase {
  private MockControl listenerControl = null;
  private ITimeoutTaskListener listener = null;
  
  private TimeoutTask classUnderTest = null;
  
  public void setUp() throws Exception {
    super.setUp();
    listenerControl = MockControl.createControl(ITimeoutTaskListener.class);
    listener = (ITimeoutTaskListener)listenerControl.getMock();
    classUnderTest = new TimeoutTask();
    classUnderTest.setListener(listener);
  }
  
  public void tearDown() throws Exception {
    listenerControl = null;
    listener = null;
    classUnderTest = null;
    super.tearDown();
  }
  
  protected void replay() {
    listenerControl.replay();
  }
  
  protected void verify() {
    listenerControl.verify();
  }
  
  public void testRun() {
    ITimeoutRule rule = new ITimeoutRule() {
      public IBltMessage timeout(long id, int why, Object data) {return null;}
    };
    classUnderTest.setId(10);
    classUnderTest.setRule(rule);
    listener.timeoutNotification(10, rule, null);
    
    replay();
    classUnderTest.run();
    verify();
  }

  public void testCancel() {
    ITimeoutRule rule = new ITimeoutRule() {
      public IBltMessage timeout(long id, int why, Object data) {return null;}
    };
    classUnderTest.setId(10);
    classUnderTest.setRule(rule);
    listener.cancelNotification(10, rule, null);
    replay();
    classUnderTest.cancel();
    verify();
  }  
}
