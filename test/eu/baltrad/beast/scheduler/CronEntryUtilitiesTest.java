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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Anders Henja
 * @date Jan 12, 2011
 */
public class CronEntryUtilitiesTest extends EasyMockSupport {
  private static interface MockMethods {
    public String createBasePattern(List<String> values);
    public List<String> parseBasePattern(String value);
  };
  
  private CronEntryUtilities classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    classUnderTest = new CronEntryUtilities();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void testCreateBasePattern() throws Exception {
    List<String> values = Arrays.asList(new String[]{"1","2","3","10"});
    String result = classUnderTest.createBasePattern(values);
    assertEquals("1,2,3,10", result);
  }

  @Test
  public void testCreateBasePattern_2() throws Exception {
    List<String> values = Arrays.asList(new String[]{"*/2"});
    String result = classUnderTest.createBasePattern(values);
    assertEquals("*/2", result);
  }

  @Test
  public void testCreateBasePattern_3() throws Exception {
    List<String> values = Arrays.asList(new String[]{"*/2","*/3"});
    String result = classUnderTest.createBasePattern(values);
    assertEquals("*/2,*/3", result);
  }

  @Test
  public void testCreateBasePattern_4() throws Exception {
    List<String> values = Arrays.asList(new String[]{"*/2","1","31"});
    String result = classUnderTest.createBasePattern(values);
    assertEquals("*/2,1,31", result);
  }
  
  @Test
  public void testParseBasePattern() throws Exception {
    List<String> result = classUnderTest.parseBasePattern("1,2,3,10");
    assertTrue(validateList(Arrays.asList(new String[]{"1","2","3","10"}), result));
  }

  @Test
  public void testParseBasePattern_2() throws Exception {
    List<String> result = classUnderTest.parseBasePattern("*/2");
    assertTrue(validateList(Arrays.asList(new String[]{"*/2"}), result));
  }  

  @Test
  public void testParseBasePattern_3() throws Exception {
    List<String> result = classUnderTest.parseBasePattern("*/2,*/3");
    assertTrue(validateList(Arrays.asList(new String[]{"*/2","*/3"}), result));
  }  

  @Test
  public void testParseBasePattern_4() throws Exception {
    List<String> result = classUnderTest.parseBasePattern("*/2,1,31");
    assertTrue(validateList(Arrays.asList(new String[]{"*/2","1","31"}), result));
  }    
  
  @Test
  public void testCreateSecondPattern() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    List<String> strings = new ArrayList<String>();
    
    classUnderTest = new CronEntryUtilities() {
      public String createBasePattern(List<String> values) {
        return methods.createBasePattern(values);
      }
    };
    
    expect(methods.createBasePattern(strings)).andReturn("nisse");
    
    replayAll();
    
    String result = classUnderTest.createSecondPattern(strings);
    
    verifyAll();
    assertEquals("nisse", result);
  }

  @Test
  public void testParseSecondPattern() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    List<String> strings = new ArrayList<String>();
    
    classUnderTest = new CronEntryUtilities() {
      public List<String> parseBasePattern(String value) {
        return methods.parseBasePattern(value);
      }
    };
    
    expect(methods.parseBasePattern("nisse")).andReturn(strings);
    
    replayAll();
    
    List<String> result = classUnderTest.parseSecondPattern("nisse");
    
    verifyAll();
    assertSame(strings, result);
  }
  
  @Test
  public void testCreateMinutePattern() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    List<String> strings = new ArrayList<String>();
    
    classUnderTest = new CronEntryUtilities() {
      public String createBasePattern(List<String> values) {
        return methods.createBasePattern(values);
      }
    };
    
    expect(methods.createBasePattern(strings)).andReturn("nisse");
    
    replayAll();
    
    String result = classUnderTest.createMinutePattern(strings);
    
    verifyAll();
    assertEquals("nisse", result);
  }

  @Test
  public void testParseMinutePattern() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    List<String> strings = new ArrayList<String>();
    
    classUnderTest = new CronEntryUtilities() {
      public List<String> parseBasePattern(String value) {
        return methods.parseBasePattern(value);
      }
    };
    
    expect(methods.parseBasePattern("nisse")).andReturn(strings);
    
    replayAll();
    
    List<String> result = classUnderTest.parseMinutePattern("nisse");
    
    verifyAll();
    assertSame(strings, result);
  }
  
  @Test
  public void testCreateHourPattern() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    List<String> strings = new ArrayList<String>();
    
    classUnderTest = new CronEntryUtilities() {
      public String createBasePattern(List<String> values) {
        return methods.createBasePattern(values);
      }
    };
    
    expect(methods.createBasePattern(strings)).andReturn("nisse");
    
    replayAll();
    
    String result = classUnderTest.createHourPattern(strings);
    
    verifyAll();
    assertEquals("nisse", result);
  }

  @Test
  public void testParseHourPattern() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    List<String> strings = new ArrayList<String>();
    
    classUnderTest = new CronEntryUtilities() {
      public List<String> parseBasePattern(String value) {
        return methods.parseBasePattern(value);
      }
    };
    
    expect(methods.parseBasePattern("nisse")).andReturn(strings);
    
    replayAll();
    
    List<String> result = classUnderTest.parseHourPattern("nisse");
    
    verifyAll();
    assertSame(strings, result);
  }

  @Test
  public void testCreateDayOfMonthPattern() throws Exception {
    String[][][] values = { 
     {{"1,2,3,10"},{"1","2","3","10"}},
     {{"*/2"},{"*/2"}},
     {{"*/2,*/3"},{"*/2","*/3"}},
     {{"*/2,1,31"},{"*/2","1","31"}},
     {{"*/2,LW"},{"*/2","LW"}},
     {{"?"},{"?"}},
    };
    for (int i = 0; i < values.length;i++) {
      String result = classUnderTest.createDayOfMonthPattern(Arrays.asList(values[i][1]));
      assertEquals(values[i][0][0], result);
    }
  }
  
  @Test
  public void testParseDayOfMonthPattern() throws Exception {
    String[][][] values = { 
        {{"1,2,3,10"},{"1","2","3","10"}},
        {{"*/2"},{"*/2"}},
        {{"*/2,*/3"},{"*/2","*/3"}},
        {{"*/2,1,31"},{"*/2","1","31"}},
        {{"*/2,LW"},{"*/2","LW"}},
        {{"?"},{"?"}},
       };
    for (int i = 0; i < values.length;i++) {
      List<String> result = classUnderTest.parseDayOfMonthPattern(values[i][0][0]);
      assertTrue(validateList(Arrays.asList(values[i][1]), result));
    }    
  }
  
  @Test
  public void testCreateMonthPattern() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    List<String> strings = new ArrayList<String>();
    
    classUnderTest = new CronEntryUtilities() {
      public String createBasePattern(List<String> values) {
        return methods.createBasePattern(values);
      }
    };
    
    expect(methods.createBasePattern(strings)).andReturn("nisse");
    
    replayAll();
    
    String result = classUnderTest.createMonthPattern(strings);
    
    verifyAll();
    assertEquals("nisse", result);
  }

  @Test
  public void testParseMonthPattern() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    List<String> strings = new ArrayList<String>();
    
    classUnderTest = new CronEntryUtilities() {
      public List<String> parseBasePattern(String value) {
        return methods.parseBasePattern(value);
      }
    };
    
    expect(methods.parseBasePattern("nisse")).andReturn(strings);
    
    replayAll();
    
    List<String> result = classUnderTest.parseMonthPattern("nisse");
    
    verifyAll();
    assertSame(strings, result);
  }

  @Test
  public void testCreateDayOfWeekPattern() throws Exception {
    String[][][] values = { 
     {{"1,2,3,10"},{"1","2","3","10"}},
     {{"*/2"},{"*/2"}},
     {{"*/2,*/3"},{"*/2","*/3"}},
     {{"*/2,1,31"},{"*/2","1","31"}},
     {{"*/2,LW"},{"*/2","LW"}},
     {{"?"},{"?"}},
    };
    for (int i = 0; i < values.length;i++) {
      String result = classUnderTest.createDayOfWeekPattern(Arrays.asList(values[i][1]));
      assertEquals(values[i][0][0], result);
    }
  }
  
  @Test
  public void testParseDayOfWeekPattern() throws Exception {
    String[][][] values = { 
        {{"1,2,3,10"},{"1","2","3","10"}},
        {{"*/2"},{"*/2"}},
        {{"*/2,*/3"},{"*/2","*/3"}},
        {{"*/2,1,31"},{"*/2","1","31"}},
        {{"*/2,LW"},{"*/2","LW"}},
        {{"?"},{"?"}},
       };
    for (int i = 0; i < values.length;i++) {
      List<String> result = classUnderTest.parseDayOfWeekPattern(values[i][0][0]);
      assertTrue(validateList(Arrays.asList(values[i][1]), result));
    }    
  }

  @Test
  public void testCreateYearPattern() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    List<String> strings = new ArrayList<String>();
    
    classUnderTest = new CronEntryUtilities() {
      public String createBasePattern(List<String> values) {
        return methods.createBasePattern(values);
      }
    };
    
    expect(methods.createBasePattern(strings)).andReturn("nisse");
    
    replayAll();
    
    String result = classUnderTest.createYearPattern(strings);
    
    verifyAll();
    assertEquals("nisse", result);
  }

  @Test
  public void testParseYearPattern() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    List<String> strings = new ArrayList<String>();
    
    classUnderTest = new CronEntryUtilities() {
      public List<String> parseBasePattern(String value) {
        return methods.parseBasePattern(value);
      }
    };
    
    expect(methods.parseBasePattern("nisse")).andReturn(strings);
    
    replayAll();
    
    List<String> result = classUnderTest.parseYearPattern("nisse");
    
    verifyAll();
    assertSame(strings, result);
  }
  
  
  @Test
  public void testParseAllInExpression() throws Exception {
    String expression = "1,*/2 0 23 LW 1,5,9 ?";
    List<String>[] result = classUnderTest.parseAllInExpression(expression);
    assertEquals(6, result.length);
    assertEquals(2, result[CronEntryUtilities.SECONDS_INDEX].size());
    assertEquals("1", result[CronEntryUtilities.SECONDS_INDEX].get(0));
    assertEquals("*/2", result[CronEntryUtilities.SECONDS_INDEX].get(1));
    
    assertEquals(1, result[CronEntryUtilities.MINUTES_INDEX].size());
    assertEquals("0", result[CronEntryUtilities.MINUTES_INDEX].get(0));

    assertEquals(1, result[CronEntryUtilities.HOURS_INDEX].size());
    assertEquals("23", result[CronEntryUtilities.HOURS_INDEX].get(0));

    assertEquals(1, result[CronEntryUtilities.DAYSOFMONTH_INDEX].size());
    assertEquals("LW", result[CronEntryUtilities.DAYSOFMONTH_INDEX].get(0));

    assertEquals(3, result[CronEntryUtilities.MONTHS_INDEX].size());
    assertEquals("1", result[CronEntryUtilities.MONTHS_INDEX].get(0));
    assertEquals("5", result[CronEntryUtilities.MONTHS_INDEX].get(1));
    assertEquals("9", result[CronEntryUtilities.MONTHS_INDEX].get(2));
    
    assertEquals(1, result[CronEntryUtilities.DAYSOFWEEK_INDEX].size());
    assertEquals("?", result[CronEntryUtilities.DAYSOFWEEK_INDEX].get(0));
  }

  @Test
  public void testParseAllInExpression_withYear() throws Exception {
    String expression = "1,*/2 0 23 LW 1,5,9 ? 1979,1980,1981";
    List<String>[] result = classUnderTest.parseAllInExpression(expression);
    assertEquals(7, result.length);
    assertEquals(2, result[CronEntryUtilities.SECONDS_INDEX].size());
    assertEquals("1", result[CronEntryUtilities.SECONDS_INDEX].get(0));
    assertEquals("*/2", result[CronEntryUtilities.SECONDS_INDEX].get(1));
    
    assertEquals(1, result[CronEntryUtilities.MINUTES_INDEX].size());
    assertEquals("0", result[CronEntryUtilities.MINUTES_INDEX].get(0));

    assertEquals(1, result[CronEntryUtilities.HOURS_INDEX].size());
    assertEquals("23", result[CronEntryUtilities.HOURS_INDEX].get(0));

    assertEquals(1, result[CronEntryUtilities.DAYSOFMONTH_INDEX].size());
    assertEquals("LW", result[CronEntryUtilities.DAYSOFMONTH_INDEX].get(0));

    assertEquals(3, result[CronEntryUtilities.MONTHS_INDEX].size());
    assertEquals("1", result[CronEntryUtilities.MONTHS_INDEX].get(0));
    assertEquals("5", result[CronEntryUtilities.MONTHS_INDEX].get(1));
    assertEquals("9", result[CronEntryUtilities.MONTHS_INDEX].get(2));
    
    assertEquals(1, result[CronEntryUtilities.DAYSOFWEEK_INDEX].size());
    assertEquals("?", result[CronEntryUtilities.DAYSOFWEEK_INDEX].get(0));

    assertEquals(3, result[CronEntryUtilities.YEARS_INDEX].size());
    assertEquals("1979", result[CronEntryUtilities.YEARS_INDEX].get(0));
    assertEquals("1980", result[CronEntryUtilities.YEARS_INDEX].get(1));
    assertEquals("1981", result[CronEntryUtilities.YEARS_INDEX].get(2));
  }

  @Test
  public void testParseAllInExpression_badExpressions() throws Exception {
    String[] expressions = {
      "0 * * ? * ?",
      "90 1 1 * * ?",
      "10 * * ? *",
      "10 * * ? * * * *"
    };
    for (String expr: expressions) {
      try {
        classUnderTest.parseAllInExpression(expr);
        fail("Expected SchedulerException");
      } catch (SchedulerException e) {
        // pass
      }
    }
  }
  
  private boolean validateList(List<String> expected, List<String> arr) {
    if (arr.size() != expected.size()) {
      return false;
    }
    for (int i = 0; i < arr.size(); i++) {
      if (!arr.get(i).equals(expected.get(i))) {
        return false;
      }
    }
    return true;
  }
}
