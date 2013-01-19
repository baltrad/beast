/* --------------------------------------------------------------------
Copyright (C) 2009-2013 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.log.message;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Anders Henja
 *
 */
public class LogMessageTest {
  private LogMessage classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = null;
  }

  @After
  public void tearDown() throws Exception {
    classUnderTest = null;    
  }

  @Test
  public void testSetSeverity() {
    classUnderTest = new LogMessage();
    for (MessageSeverity s : new MessageSeverity[]{MessageSeverity.UNDEFINED, MessageSeverity.INFO, MessageSeverity.WARNING, MessageSeverity.ERROR, MessageSeverity.FATAL}) {
      classUnderTest.setSeverity(s);
      Assert.assertEquals(s, classUnderTest.getSeverity());
    }
  }
  
  @Test
  public void testSetSeverity_null() {
    classUnderTest = new LogMessage();
    try {
      classUnderTest.setSeverity(null);
      Assert.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }
  
  
  @Test
  public void testGetFormattedMessage() {
    classUnderTest = new LogMessage("BEAST", "00001", "This is a %s with %s to say");
    Assert.assertEquals("This is a test with nothing to say", classUnderTest.getFormattedMessage("test", "nothing"));
  }
}
