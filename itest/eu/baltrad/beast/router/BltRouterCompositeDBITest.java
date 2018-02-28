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
package eu.baltrad.beast.router;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.springframework.context.support.AbstractApplicationContext;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.qc.AnomalyException;
import eu.baltrad.beast.qc.IAnomalyDetectorManager;
import eu.baltrad.beast.router.impl.BltRouter;
import eu.baltrad.beast.rules.RuleException;
import eu.baltrad.beast.rules.composite.CompositingRule;
import eu.baltrad.beast.scheduler.IBeastScheduler;

/**
 * @author Anders Henja
 */
public class BltRouterCompositeDBITest extends TestCase {
  private BltRouter classUnderTest = null;
  private AbstractApplicationContext context = null;
  private BeastDBTestHelper helper = null;
  private IBeastScheduler scheduler = null;
  private IAnomalyDetectorManager anomalymanager = null;
  
  public void setUp() throws Exception {
    context = BeastDBTestHelper.loadContext(this);
    helper = (BeastDBTestHelper)context.getBean("testHelper");
    helper.purgeBaltradDB();
    helper.tearDown();
    helper.cleanInsert(this);
    context = BeastDBTestHelper.loadContext(this); // Reread context to work with fresh db
    classUnderTest = (BltRouter)context.getBean("router");
    classUnderTest.afterPropertiesSet();
    scheduler = (IBeastScheduler)context.getBean("beastscheduler");
    anomalymanager = (IAnomalyDetectorManager)context.getBean("anomalymanager");
    
  }
  
  public void tearDown() throws Exception {
    helper = null;
    classUnderTest = null;
    scheduler = null;
    anomalymanager=null;
    context.close();
  }

  /**
   * Verifies the database table with an excel sheet.
   * @param extras
   * @throws Exception
   */
  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_router_rules");
    ITable actual = helper.getDatabaseTable("beast_router_rules");
    Assertion.assertEquals(expected, actual);

    expected = helper.getXlsTable(this, extras, "beast_adaptors");
    actual = helper.getDatabaseTable("beast_adaptors");
    Assertion.assertEqualsIgnoreCols(expected, actual, new String[]{"adaptor_id"});

    expected = helper.getXlsTable(this, extras, "beast_router_dest");
    actual = helper.getDatabaseTable("beast_router_dest");
    Assertion.assertEquals(expected, actual);
    
    expected = helper.getXlsTable(this, extras, "beast_scheduled_jobs");
    actual = helper.getDatabaseTable("beast_scheduled_jobs");
    Assertion.assertEquals(expected, actual);
    
    expected = helper.getXlsTable(this, extras, "beast_composite_rules");
    expected = DefaultColumnFilter.excludedColumnsTable(expected, new String[]{"ZR_A","ZR_b"}); // ZRA & ZR_b causes some presicionproblems
    actual = helper.getDatabaseTable("beast_composite_rules");
    actual = DefaultColumnFilter.excludedColumnsTable(actual, new String[]{"ZR_A","ZR_b"});
    Assertion.assertEquals(expected, actual);
  }
  
  protected void verifyDetectorTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_anomaly_detectors");
    ITable actual = helper.getDatabaseTable("beast_anomaly_detectors");
    Assertion.assertEquals(expected, actual);

    expected = helper.getXlsTable(this, extras, "beast_composite_detectors");
    actual = helper.getDatabaseTable("beast_composite_detectors", new String[]{"rule_id","name"});
    Assertion.assertEquals(expected, actual);
  }

  
  public void testLoadCompositingDef() {
    RouteDefinition def = classUnderTest.getDefinition("admin");
    assertNotNull(def);
    assertEquals("admin", def.getName());
    assertEquals("Karl", def.getAuthor());
    assertEquals("blt_composite", def.getRuleType());
    assertEquals(20, ((CompositingRule)def.getRule()).getTimeout());
    assertEquals(true, ((CompositingRule)def.getRule()).isScanBased());
    assertEquals(CompositingRule.SelectionMethod_NEAREST_RADAR, ((CompositingRule)def.getRule()).getSelectionMethod());
    assertEquals(CompositingRule.PPI, ((CompositingRule)def.getRule()).getMethod());
    assertEquals("0.5", ((CompositingRule)def.getRule()).getProdpar());
    assertEquals(-1, ((CompositingRule)def.getRule()).getMaxAgeLimit());
    assertEquals(false, ((CompositingRule)def.getRule()).isApplyGRA());
    assertEquals(100.0, ((CompositingRule)def.getRule()).getZR_A(), 4);
    assertEquals(1.5, ((CompositingRule)def.getRule()).getZR_b(), 4);
    assertEquals(true, ((CompositingRule)def.getRule()).isIgnoreMalfunc());
    assertEquals(true, ((CompositingRule)def.getRule()).isCtFilter());
    assertEquals("se.baltrad.something", ((CompositingRule)def.getRule()).getQitotalField());
    assertEquals("VRAD", ((CompositingRule)def.getRule()).getQuantity());
    assertEquals(true, ((CompositingRule)def.getRule()).isNominalTimeout());
    assertEquals(CompositingRule.QualityControlMode_ANALYZE, ((CompositingRule)def.getRule()).getQualityControlMode());
    assertEquals(1, ((CompositingRule)def.getRule()).getDetectors().size());
    assertEquals("ropo", ((CompositingRule)def.getRule()).getDetectors().get(0));
  }

  public void testUpdateCompositingDef() throws Exception {
    List<String> recipients = new ArrayList<String>();
    recipients.add("A2");
    List<String> sources = new ArrayList<String>();
    sources.add("src1");
    List<String> detectors = new ArrayList<String>();
    detectors.add("dmi");
    detectors.add("beamb");
    
    RouteDefinition def = classUnderTest.getDefinition("admin");
    assertNotNull(def);
    def.setAuthor("Per");
    
    ((CompositingRule)def.getRule()).setInterval(20);
    ((CompositingRule)def.getRule()).setTimeout(15);
    ((CompositingRule)def.getRule()).setScanBased(false);
    ((CompositingRule)def.getRule()).setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    ((CompositingRule)def.getRule()).setMethod(CompositingRule.CAPPI);
    ((CompositingRule)def.getRule()).setProdpar("500.0");
    ((CompositingRule)def.getRule()).setMaxAgeLimit(60);
    ((CompositingRule)def.getRule()).setApplyGRA(true);
    ((CompositingRule)def.getRule()).setZR_A(200.0);
    ((CompositingRule)def.getRule()).setZR_b(1.6);
    ((CompositingRule)def.getRule()).setIgnoreMalfunc(false);
    ((CompositingRule)def.getRule()).setCtFilter(false);
    ((CompositingRule)def.getRule()).setQitotalField("se.smhi.something");
    ((CompositingRule)def.getRule()).setQuantity("TH");
    ((CompositingRule)def.getRule()).setNominalTimeout(false);
    ((CompositingRule)def.getRule()).setQualityControlMode(CompositingRule.QualityControlMode_ANALYZE_AND_APPLY);
    ((CompositingRule)def.getRule()).setDetectors(detectors);
    
    classUnderTest.updateDefinition(def);
    
    verifyDatabaseTables("updatecomposite");
  }
  
  public void testRemoveCompositeWithScheduledJob() throws Exception {
    scheduler.register("0 0 0 * 1 ?", "admin");
    try {
      classUnderTest.deleteDefinition("admin");
      fail("Expected RuleException");
    } catch (RuleException e) {
    }
    verifyDatabaseTables("removeCompositeWithScheduledJob");
    RouteDefinition def = classUnderTest.getDefinition("admin");
    assertNotNull(def);
    assertEquals("admin", def.getName());
  }
  
  public void testRemoveCompositeWithScheduledJobRemoved() throws Exception {
    int id = scheduler.register("0 0 0 * 1 ?", "admin");
    try {
      classUnderTest.deleteDefinition("admin");
      fail("Expected RuleException");
    } catch (RuleException e) {
    }
    scheduler.unregister(id);
    classUnderTest.deleteDefinition("admin");
    verifyDatabaseTables("removeCompositeWithScheduledJobRemoved");
    RouteDefinition def = classUnderTest.getDefinition("admin");
    assertNull(def);
  }

  public void testCreateAndStoreComposite() throws Exception {
    List<String> recipients = new ArrayList<String>();
    recipients.add("A2");
    List<String> sources = new ArrayList<String>();
    sources.add("src1");
    
    List<String> detectors = new ArrayList<String>();
    detectors.add("ropo");
    detectors.add("dmi");

    CompositingRule rule = (CompositingRule)classUnderTest.createRule("blt_composite");
    rule.setArea("sweet");
    rule.setInterval(20);
    rule.setTimeout(30);
    rule.setScanBased(false);
    rule.setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    rule.setMethod(CompositingRule.CAPPI);
    rule.setProdpar("500.0");
    rule.setMaxAgeLimit(30);
    rule.setApplyGRA(true);
    rule.setZR_A(210.0);
    rule.setZR_b(1.7);
    rule.setIgnoreMalfunc(false);
    rule.setCtFilter(false);
    rule.setQitotalField("se.someone.somewhere");
    rule.setQuantity("NOOP");
    rule.setNominalTimeout(false);
    rule.setQualityControlMode(CompositingRule.QualityControlMode_ANALYZE);
    rule.setDetectors(detectors);
    rule.setSources(sources);

    RouteDefinition def = classUnderTest.create("ugly", "anders", true, "test", recipients, rule);
    classUnderTest.storeDefinition(def);
    
    verifyDatabaseTables("createcomposite");
  }
  
  public void testCreateAndStoreCompositeWithBadDetector() throws Exception {
    List<String> recipients = new ArrayList<String>();
    recipients.add("A2");
    List<String> sources = new ArrayList<String>();
    sources.add("src1");
    
    List<String> detectors = new ArrayList<String>();
    detectors.add("sigge");

    CompositingRule rule = (CompositingRule)classUnderTest.createRule("blt_composite");
    rule.setArea("sweet");
    rule.setInterval(20);
    rule.setTimeout(30);
    rule.setScanBased(false);
    rule.setSelectionMethod(CompositingRule.SelectionMethod_HEIGHT_ABOVE_SEALEVEL);
    rule.setDetectors(detectors);
    rule.setSources(sources);

    RouteDefinition def = classUnderTest.create("ugly", "anders", true, "test", recipients, rule);
    try {
      classUnderTest.storeDefinition(def);
      fail("Expected RuleException");
    } catch (RuleException e) {
      // pass
    }
    
    verifyDatabaseTables(null);
  }
  
  public void testRemoveDetectorWhenCompositeDepends() throws Exception {
    try {
      anomalymanager.remove("ropo");
      fail("Expected AnomalyException");
    } catch (AnomalyException e) {
      // pass
    }
  }
}
