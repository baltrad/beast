/* --------------------------------------------------------------------
Copyright (C) 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

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

import org.easymock.MockControl;

import junit.framework.TestCase;

/**
 *
 * @author Anders Henja
 * @date Jan 12, 2011
 */
public class CronEntryUtilitiesTest extends TestCase {
  private static interface MockMethods {
    public String createBasePattern(String[] values);
    public String[] parseBasePattern(String value);
  };
  
  private CronEntryUtilities classUnderTest = null;
  
  public void setUp() throws Exception {
    classUnderTest = new CronEntryUtilities();
  }
  
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  public void testCreateBasePattern() throws Exception {
    String[] values = {"1","2","3","10"};
    String result = classUnderTest.createBasePattern(values);
    assertEquals("1,2,3,10", result);
  }

  public void testCreateBasePattern_2() throws Exception {
    String[] values = {"*/2"};
    String result = classUnderTest.createBasePattern(values);
    assertEquals("*/2", result);
  }

  public void testCreateBasePattern_3() throws Exception {
    String[] values = {"*/2","*/3"};
    String result = classUnderTest.createBasePattern(values);
    assertEquals("*/2,*/3", result);
  }

  public void testCreateBasePattern_4() throws Exception {
    String[] values = {"*/2","1","31"};
    String result = classUnderTest.createBasePattern(values);
    assertEquals("*/2,1,31", result);
  }
  
  public void testParseBasePattern() throws Exception {
    String[] result = classUnderTest.parseBasePattern("1,2,3,10");
    assertTrue(validateArray(new String[]{"1","2","3","10"}, result));
  }

  public void testParseBasePattern_2() throws Exception {
    String[] result = classUnderTest.parseBasePattern("*/2");
    assertTrue(validateArray(new String[]{"*/2"}, result));
  }  

  public void testParseBasePattern_3() throws Exception {
    String[] result = classUnderTest.parseBasePattern("*/2,*/3");
    assertTrue(validateArray(new String[]{"*/2","*/3"}, result));
  }  

  public void testParseBasePattern_4() throws Exception {
    String[] result = classUnderTest.parseBasePattern("*/2,1,31");
    assertTrue(validateArray(new String[]{"*/2","1","31"}, result));
  }    
  
  public void testCreateSecondPattern() throws Exception {
    MockControl methodsControl = MockControl.createControl(MockMethods.class);
    final MockMethods methods = (MockMethods)methodsControl.getMock();
    String[] strings = new String[0];
    
    classUnderTest = new CronEntryUtilities() {
      public String createBasePattern(String[] values) {
        return methods.createBasePattern(values);
      }
    };
    
    methods.createBasePattern(strings);
    methodsControl.setReturnValue("nisse");
    
    methodsControl.replay();
    
    String result = classUnderTest.createSecondPattern(strings);
    
    methodsControl.verify();
    assertEquals("nisse", result);
  }

  public void testParseSecondPattern() throws Exception {
    MockControl methodsControl = MockControl.createControl(MockMethods.class);
    final MockMethods methods = (MockMethods)methodsControl.getMock();
    String[] strings = new String[0];
    
    classUnderTest = new CronEntryUtilities() {
      public String[] parseBasePattern(String value) {
        return methods.parseBasePattern(value);
      }
    };
    
    methods.parseBasePattern("nisse");
    methodsControl.setReturnValue(strings);
    
    methodsControl.replay();
    
    String[] result = classUnderTest.parseSecondPattern("nisse");
    
    methodsControl.verify();
    assertSame(strings, result);
  }

  
  public void testCreateMinutePattern() throws Exception {
    MockControl methodsControl = MockControl.createControl(MockMethods.class);
    final MockMethods methods = (MockMethods)methodsControl.getMock();
    String[] strings = new String[0];
    
    classUnderTest = new CronEntryUtilities() {
      public String createBasePattern(String[] values) {
        return methods.createBasePattern(values);
      }
    };
    
    methods.createBasePattern(strings);
    methodsControl.setReturnValue("nisse");
    
    methodsControl.replay();
    
    String result = classUnderTest.createMinutePattern(strings);
    
    methodsControl.verify();
    assertEquals("nisse", result);
  }

  public void testParseMinutePattern() throws Exception {
    MockControl methodsControl = MockControl.createControl(MockMethods.class);
    final MockMethods methods = (MockMethods)methodsControl.getMock();
    String[] strings = new String[0];
    
    classUnderTest = new CronEntryUtilities() {
      public String[] parseBasePattern(String value) {
        return methods.parseBasePattern(value);
      }
    };
    
    methods.parseBasePattern("nisse");
    methodsControl.setReturnValue(strings);
    
    methodsControl.replay();
    
    String[] result = classUnderTest.parseMinutePattern("nisse");
    
    methodsControl.verify();
    assertSame(strings, result);
  }
  
  public void testCreateHourPattern() throws Exception {
    MockControl methodsControl = MockControl.createControl(MockMethods.class);
    final MockMethods methods = (MockMethods)methodsControl.getMock();
    String[] strings = new String[0];
    
    classUnderTest = new CronEntryUtilities() {
      public String createBasePattern(String[] values) {
        return methods.createBasePattern(values);
      }
    };
    
    methods.createBasePattern(strings);
    methodsControl.setReturnValue("nisse");
    
    methodsControl.replay();
    
    String result = classUnderTest.createHourPattern(strings);
    
    methodsControl.verify();
    assertEquals("nisse", result);
  }

  public void testParseHourPattern() throws Exception {
    MockControl methodsControl = MockControl.createControl(MockMethods.class);
    final MockMethods methods = (MockMethods)methodsControl.getMock();
    String[] strings = new String[0];
    
    classUnderTest = new CronEntryUtilities() {
      public String[] parseBasePattern(String value) {
        return methods.parseBasePattern(value);
      }
    };
    
    methods.parseBasePattern("nisse");
    methodsControl.setReturnValue(strings);
    
    methodsControl.replay();
    
    String[] result = classUnderTest.parseHourPattern("nisse");
    
    methodsControl.verify();
    assertSame(strings, result);
  }

  
  private boolean validateArray(String[] expected, String[] arr) {
    if (arr.length != expected.length) {
      return false;
    }
    for (int i = 0; i < arr.length; i++) {
      if (!arr[i].equals(expected[i])) {
        return false;
      }
    }
    return true;
  }
}
