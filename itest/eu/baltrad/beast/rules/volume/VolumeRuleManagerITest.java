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
package eu.baltrad.beast.rules.volume;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.timer.TimeoutManager;
import eu.baltrad.beast.rules.util.IRuleUtilities;
import eu.baltrad.fc.db.FileEntry;

/**
 * @author Anders Henja
 *
 */
public class VolumeRuleManagerITest extends TestCase {
  private ApplicationContext dbcontext = null;
  private ApplicationContext context = null;
  
  private BeastDBTestHelper helper = null;
  private VolumeRuleManager classUnderTest = null;
  private SimpleJdbcOperations template = null;
  private Catalog catalog = null;
  private TimeoutManager timeoutManager = null;
  private IRuleUtilities utilities = null;
  
  private static String FIXTURE = "fixtures/scan_sehud_0.5_20110126T184500Z.h5";
  
  public void setUp() throws Exception {
    dbcontext = BeastDBTestHelper.loadDbContext(this);
    helper = (BeastDBTestHelper)dbcontext.getBean("helper");
    helper.tearDown();
    helper.purgeBaltradDB();
    
    helper.cleanInsert(this);
    template = (SimpleJdbcOperations)dbcontext.getBean("jdbcTemplate");
    
    context = BeastDBTestHelper.loadContext(this);
    catalog = (Catalog)context.getBean("catalog");
    timeoutManager = (TimeoutManager)context.getBean("timeoutmanager");
    utilities = (IRuleUtilities)context.getBean("ruleutil");
    
    classUnderTest = new VolumeRuleManager();
    classUnderTest.setJdbcTemplate(template);
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(utilities);
    classUnderTest.setTimeoutManager(timeoutManager);
  }
  
  public void tearDown() throws Exception {
    classUnderTest = null;
    dbcontext = null;
    context = null;
    helper = null;
    template = null;
    catalog = null;
    utilities = null;
    timeoutManager = null;
  }
 
  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_volume_rules");
    ITable actual = helper.getDatabaseTable("beast_volume_rules");
    Assertion.assertEquals(expected, actual);

    expected = helper.getXlsTable(this, extras, "beast_volume_sources");
    actual = helper.getDatabaseTable("beast_volume_sources");
    Assertion.assertEquals(expected, actual);
  }
  
  private String getFilePath(String resource) throws Exception {
    java.io.File f = new java.io.File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }
  
  protected BltDataMessage createDataMessage(FileEntry f) {
    BltDataMessage result = new BltDataMessage();
    result.setFileEntry(f);
    return result;
  }
  
  public void testLoadAndHandle() throws Exception {
    VolumeRule rule = (VolumeRule)classUnderTest.load(1);
  
    assertNotNull(rule.getRuleUtilities());
    assertNotNull(rule.getCatalog());
    assertNotNull(rule.getTimeoutManager());
    
    FileEntry f = catalog.getCatalog().store(getFilePath(FIXTURE));
    catalog.getCatalog().storage().store(f);

    BltGenerateMessage msg = (BltGenerateMessage)rule.handle(createDataMessage(f));

    assertNull(msg);
  }
  
  public void testLoad_1() throws Exception  {
    VolumeRule rule = (VolumeRule)classUnderTest.load(1);
    assertEquals(6, rule.getInterval());
    assertEquals(10, rule.getTimeout());
    assertEquals(true, rule.isAscending());
    assertEquals(0.1, rule.getElevationMin());
    assertEquals(45.0, rule.getElevationMax());
    List<String> sources = rule.getSources();
    assertEquals(2, sources.size());
    assertTrue(sources.contains("S1"));
    assertTrue(sources.contains("S2"));
  }
  
  public void testLoad_2() throws Exception  {
    VolumeRule rule = (VolumeRule)classUnderTest.load(2);
    assertEquals(12, rule.getInterval());
    assertEquals(20, rule.getTimeout());
    assertEquals(true, rule.isAscending());
    assertEquals(1.0, rule.getElevationMin());
    assertEquals(10.0, rule.getElevationMax());
    List<String> sources = rule.getSources();
    assertEquals(3, sources.size());
    assertTrue(sources.contains("S3"));
    assertTrue(sources.contains("S4"));
    assertTrue(sources.contains("S5"));
  }
  
  public void testLoad_3() throws Exception  {
    VolumeRule rule = (VolumeRule)classUnderTest.load(3);
    assertEquals(10, rule.getInterval());
    assertEquals(30, rule.getTimeout());
    assertEquals(false, rule.isAscending());
    assertEquals(2.5, rule.getElevationMin());
    assertEquals(24.1, rule.getElevationMax());
    List<String> sources = rule.getSources();
    assertEquals(0, sources.size());
  }
  
  public void testStore() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("S10");
    sources.add("S12");
    VolumeRule rule = new VolumeRule();
    rule.setInterval(10);
    rule.setTimeout(20);
    rule.setAscending(false);
    rule.setElevationMin(2.0);
    rule.setElevationMax(20.0);
    rule.setSources(sources);
    
    classUnderTest.store(4, rule);
    
    verifyDatabaseTables("store");
  }
  
  public void testDelete() throws Exception {
    classUnderTest.delete(2);
    verifyDatabaseTables("delete");
  }
  
  public void testUpdate() throws Exception {
    List<String> sources = new ArrayList<String>();
    sources.add("S27");
    sources.add("S4");
    VolumeRule rule = (VolumeRule)classUnderTest.load(2);
    rule.setInterval(15);
    rule.setTimeout(10);
    rule.setAscending(false);
    rule.setElevationMin(2.0);
    rule.setElevationMax(24.0);
    rule.setSources(sources);
    
    classUnderTest.update(2, rule);
    
    verifyDatabaseTables("update");
  }
}
