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

import junit.framework.TestCase;

/**
 *
 * @author Anders Henja
 * @date Jan 11, 2011
 */
public class CronEntryFactoryTest extends TestCase {
  private CronEntryFactory classUnderTest = null;
  
  public void setUp() throws Exception {
    classUnderTest = new CronEntryFactory();
  }
  
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  public void testCreateExpressionAndJob() throws Exception {
    String[] expressions = {"* * * * * ?", 
        "0 */10 * ? * *",
        "0 1 1,7,13,20 * * ? *",
        "0 * * 1,2,3 * ?",
        "0 */2,*/3 * 1 * ?",
        "0 1,2,*/3 * 1 * ?"};
    for (String expr : expressions) {
      CronEntry entry = classUnderTest.create(expr, "A JOB");
      assertNotNull(entry);
      assertEquals(expr, entry.getExpression());
      assertEquals("A JOB", entry.getName());
    }
  }

  public void testCreateExpressionAndJob_badExpressions() throws Exception {
    String[] expressions = {"* * * * * *", 
        "60 * * * * ?",
        "1 1 24 * * ?",
        "* * * ? * 8",
        "* * * ? * ?"};
    for (String expr : expressions) {
      try {
        classUnderTest.create(expr, "A JOB");
        fail("Expected SchedulerException for " + expr);
      } catch (SchedulerException e) {
        // pass
      }
    }
  }
  
  public void testCreate() throws Exception {
    //classUnderTest.create(CronEntry.EVERY_MINUTE, CronEntry.EVERY_SECOND_HOUR, CronEntry.EVERY_DAY, 
  }
  
  /* Seconds:
   * NA, there is no reason to specify a specific second.
   * 
   * Minute:
   * Every minute:             0 * * * * ?
   * Every second minute:      0 &#42;/2 * * * ?
   * Every fifth minute:       0 &#42;/5 * * * ?
   * Every tenth minute:       0 &#42;/10 * * * ?
   * Every fifteenth minute:   0 &#42;/15 * * * ?
   * Every twentieth minute:   0 &#42;/20 * * * ?
   * Every thirtieth minute:   0 &#42;/30 * * * ?
   * 
   * Hour:
   * Every hour:               0 0 * * * ?
   * Every second hour:        0 0 &#42;/2 * * ?
   * Every third hour:         0 0 &#42;/3 * * ?
   * Every fourth hour:        0 0 &#42;/4 * * ?
   * Every sixth hour:         0 0 &#42;/6 * * ?
   * Every eight hour:         0 0 &#42;/8 * * ?
   * Every twelfth hour:       0 0 &#42;/12 * * ?
   * 
   * Day of month:
   * Every day:                0 0 0 * * ?
   * Every weekday:            0 0 0 W * ?
   * Last day of month:        0 0 0 L * ?
   * Last weekday of month:    0 0 0 LW * ?
   * 
   * Month:
   * Every month:              0 0 0 1 * ?
   * Every second month:       0 0 0 1 &#42;/2 ?
   * Every third month:        0 0 0 1 &#42;/3 ?
   * Every fourth month:       0 0 0 1 &#42;/4 ?
   * Every sixth month:        0 0 0 1 &#42;/6 ?
   * 
   * Day of week:
   * Last day of week:         0 0 0 ? * L
   * X:th Y:day of the month   0 0 0 ? * Y#X
   *   (e.g. 6#3 means 3:rd friday of the month)
   */
}
