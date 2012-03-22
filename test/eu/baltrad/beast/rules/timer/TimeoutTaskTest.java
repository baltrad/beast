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

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.message.IBltMessage;

/**
 * @author Anders Henja
 */
public class TimeoutTaskTest extends EasyMockSupport {
  private ITimeoutTaskListener listener = null;
  private TimeoutTask classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    listener = createMock(ITimeoutTaskListener.class);
    classUnderTest = new TimeoutTask();
    classUnderTest.setListener(listener);
  }

  @After
  public void tearDown() throws Exception {
    listener = null;
    classUnderTest = null;
  }

  @Test
  public void testRun() {
    ITimeoutRule rule = new ITimeoutRule() {
      public IBltMessage timeout(long id, int why, Object data) {return null;}
      public void setRecipients(List<String> recipients) {}
    };
    classUnderTest.setId(10);
    classUnderTest.setRule(rule);
    listener.timeoutNotification(10, rule, null);
    
    replayAll();
    
    classUnderTest.run();
    
    verifyAll();
  }

  @Test
  public void testCancel() {
    ITimeoutRule rule = new ITimeoutRule() {
      public IBltMessage timeout(long id, int why, Object data) {return null;}
      public void setRecipients(List<String> recipients) {}
    };
    classUnderTest.setId(10);
    classUnderTest.setRule(rule);
    listener.cancelNotification(10, rule, null);
    
    replayAll();
    
    classUnderTest.cancel();
    
    verifyAll();
  }  
}
