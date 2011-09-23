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
package eu.baltrad.beast.qc;

import java.util.List;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.springframework.context.support.AbstractApplicationContext;

import eu.baltrad.beast.itest.BeastDBTestHelper;

/**
 * @author Anders Henja
 */
public class AnomalyDetectorManagerDBTest extends TestCase {
  private AbstractApplicationContext dbcontext = null;
  private AbstractApplicationContext context = null;
  private AnomalyDetectorManager classUnderTest = null;
  private BeastDBTestHelper helper = null;

  /**
   * Setup of test
   */
  public void setUp() throws Exception {
    dbcontext = BeastDBTestHelper.loadDbContext(this);
    helper = (BeastDBTestHelper)dbcontext.getBean("helper");
    helper.cleanInsert(this);
    context = BeastDBTestHelper.loadContext(this);
    classUnderTest = (AnomalyDetectorManager)context.getBean("detectorManager");
  }
  
  /**
   * Teardown of test
   */
  public void tearDown() throws Exception {
    helper.tearDown();
    classUnderTest = null;
    context.close();
    dbcontext.close();
  }
  
  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_anomaly_detectors");
    ITable actual = helper.getDatabaseTable("beast_anomaly_detectors");
    Assertion.assertEquals(expected, actual);
  }
  
  public void testAdd() throws Exception {
    AnomalyDetector detector = new AnomalyDetector();
    detector.setName("nisse");
    detector.setDescription("nisses description");
    classUnderTest.add(detector);
    verifyDatabaseTables("add");
  }
  
  public void testAdd_duplicate() throws Exception {
    AnomalyDetector detector = new AnomalyDetector();
    detector.setName("ropo");
    detector.setDescription("ropos description");    
    try {
      classUnderTest.add(detector);
      fail("Expected AnomalyException");
    } catch (AnomalyException e) {
      // pass
    }
    verifyDatabaseTables(null);
  }
  
  public void testUpdate() throws Exception {
    AnomalyDetector detector = new AnomalyDetector();
    detector.setName("ropo");
    detector.setDescription("nisses description");
    classUnderTest.update(detector);
    verifyDatabaseTables("update");
  }

  public void testUpdate_exception() throws Exception {
    AnomalyDetector detector = new AnomalyDetector();
    detector.setName("nisse");
    detector.setDescription("nisses description");
    try {
      classUnderTest.update(detector);
      fail("Expected AnomalyException");
    } catch (AnomalyException e) {
      // pass
    }
    verifyDatabaseTables(null);
  }

  
  public void testRemove() throws Exception {
    classUnderTest.remove("dmi");
    verifyDatabaseTables("remove");
  }

  public void testList() throws Exception {
    List<AnomalyDetector> result = classUnderTest.list();
    assertEquals(2, result.size());
    assertEquals("dmi", result.get(0).getName());
    assertEquals("dmis description", result.get(0).getDescription());
    assertEquals("ropo", result.get(1).getName());
    assertEquals("ropos description", result.get(1).getDescription());
  }
}
