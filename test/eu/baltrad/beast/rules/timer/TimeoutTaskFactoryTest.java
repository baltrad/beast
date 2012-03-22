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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.message.IBltMessage;

/**
 * @author Anders Henja
 */
public class TimeoutTaskFactoryTest {
  private TimeoutTaskFactory classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new TimeoutTaskFactory();
  }

  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  @Test
  public void testCreate_noListener() throws Exception {
    ITimeoutRule rule = new ITimeoutRule() {
      public IBltMessage timeout(long id, int why, Object data) {return null;}
      public void setRecipients(List<String> recipients) {}
    };
    try {
      classUnderTest.create(rule, 1, null, null);
      fail("Expected TimeoutRuleException");
    } catch (TimeoutRuleException e) {
      // pass
    }
  }

  @Test
  public void testCreate_2() throws Exception {
    ITimeoutRule rule = new ITimeoutRule() {
      public IBltMessage timeout(long id, int why, Object data) {return null;}
      public void setRecipients(List<String> recipients) {}
    };
    ITimeoutTaskListener listener = new ITimeoutTaskListener() {
      public void timeoutNotification(long id, ITimeoutRule rule, Object data) {}
      public void cancelNotification(long id, ITimeoutRule rule, Object data) {}
    };
    
    TimeoutTask result = classUnderTest.create(rule, 2, null, listener);
    assertNotNull(result);
    assertSame(rule, result.getRule());
    assertEquals(2, result.getId());
    assertSame(listener, result.getListener());
  }

  @Test
  public void testCreate_noRule() throws Exception {
    try {
      classUnderTest.create(null, 1, null, null);
      fail("Expected TimeoutRuleException");
    } catch (TimeoutRuleException e) {
      //pass 
    }
  }
}
