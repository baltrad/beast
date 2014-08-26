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

package eu.baltrad.beast.rules.site2d;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanInitializationException;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.oh5.Source;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class Site2DRuleTest extends EasyMockSupport {
  private Catalog catalog = null;
  private IRuleUtilities ruleUtil = null;
  
  private Site2DRule classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    catalog = createMock(Catalog.class);
    ruleUtil = createMock(IRuleUtilities.class);
    classUnderTest = new Site2DRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtil);
  }
   
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  @Test
  public void handle() {
    BltDataMessage msg = createMock(BltDataMessage.class);
    FileEntry fe = createMock(FileEntry.class);
    Metadata md = createMock(Metadata.class);
    Source source = createMock(Source.class);
    UUID ruid = UUID.randomUUID();
    
    List<String> sources = new ArrayList<String>();
    sources.add("seses");
    sources.add("nisse");
    List<String> detectors = new ArrayList<String>();
    detectors.add("piff");
    detectors.add("puff");
    
    classUnderTest.setArea("gnom_area");
    classUnderTest.setSources(sources);
    classUnderTest.setApplyGRA(true);
    classUnderTest.setDetectors(detectors);
    
    expect(msg.getFileEntry()).andReturn(fe).anyTimes();
    expect(fe.getMetadata()).andReturn(md).anyTimes();
    expect(md.getWhatObject()).andReturn("PVOL");
    expect(fe.getSource()).andReturn(source);
    expect(source.getName()).andReturn("seses");
    expect(fe.getUuid()).andReturn(ruid);
    
    replayAll();
    
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(msg);
    
    verifyAll();
    assertNotNull(result);
    assertEquals(1, result.getFiles().length);
    assertEquals(ruid.toString(), result.getFiles()[0]);
    assertEquals(7, result.getArguments().length);
    assertEquals("--area=gnom_area", result.getArguments()[0]);
    assertEquals("--anomaly-qc=piff,puff", result.getArguments()[1]);
    assertEquals("--method=pcappi", result.getArguments()[2]);
    assertEquals("--prodpar=1000.0", result.getArguments()[3]);
    assertEquals("--applygra=true", result.getArguments()[4]);
    assertEquals("--zrA=200.0", result.getArguments()[5]);
    assertEquals("--zrb=1.6", result.getArguments()[6]);
  }

  @Test
  public void handle_scanBased() {
    BltDataMessage msg = createMock(BltDataMessage.class);
    FileEntry fe = createMock(FileEntry.class);
    Metadata md = createMock(Metadata.class);
    Source source = createMock(Source.class);
    UUID ruid = UUID.randomUUID();
    
    List<String> sources = new ArrayList<String>();
    sources.add("seses");
    sources.add("nisse");
    List<String> detectors = new ArrayList<String>();
    detectors.add("piff");
    detectors.add("puff");
    
    classUnderTest.setArea("gnom_area");
    classUnderTest.setSources(sources);
    classUnderTest.setApplyGRA(true);
    classUnderTest.setDetectors(detectors);
    classUnderTest.setScanBased(true);
    
    expect(msg.getFileEntry()).andReturn(fe).anyTimes();
    expect(fe.getMetadata()).andReturn(md).anyTimes();
    expect(md.getWhatObject()).andReturn("SCAN");
    expect(fe.getSource()).andReturn(source);
    expect(source.getName()).andReturn("seses");
    expect(fe.getUuid()).andReturn(ruid);
    
    replayAll();
    
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(msg);
    
    verifyAll();
    assertNotNull(result);
    assertEquals(1, result.getFiles().length);
    assertEquals(ruid.toString(), result.getFiles()[0]);
    assertEquals(7, result.getArguments().length);
    assertEquals("--area=gnom_area", result.getArguments()[0]);
    assertEquals("--anomaly-qc=piff,puff", result.getArguments()[1]);
    assertEquals("--method=pcappi", result.getArguments()[2]);
    assertEquals("--prodpar=1000.0", result.getArguments()[3]);
    assertEquals("--applygra=true", result.getArguments()[4]);
    assertEquals("--zrA=200.0", result.getArguments()[5]);
    assertEquals("--zrb=1.6", result.getArguments()[6]);
  }

  @Test
  public void handle_scanBased_withPVOL() {
    BltDataMessage msg = createMock(BltDataMessage.class);
    FileEntry fe = createMock(FileEntry.class);
    Metadata md = createMock(Metadata.class);
    Source source = createMock(Source.class);
    UUID ruid = UUID.randomUUID();
    
    List<String> sources = new ArrayList<String>();
    sources.add("seses");
    sources.add("nisse");
    List<String> detectors = new ArrayList<String>();
    detectors.add("piff");
    detectors.add("puff");
    
    classUnderTest.setArea("gnom_area");
    classUnderTest.setSources(sources);
    classUnderTest.setApplyGRA(true);
    classUnderTest.setDetectors(detectors);
    classUnderTest.setScanBased(true);
    
    expect(msg.getFileEntry()).andReturn(fe).anyTimes();
    expect(fe.getMetadata()).andReturn(md).anyTimes();
    expect(fe.getSource()).andReturn(source);
    expect(source.getName()).andReturn("seses");
    expect(md.getWhatObject()).andReturn("PVOL");
    
    replayAll();
    
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(msg);
    
    verifyAll();
    assertNull(result);
  }

  //@Test
  public void type() {
    assertEquals("blt_site2d", classUnderTest.getType());
  }

  //@Test
  public void isValid() {
    assertEquals(true, classUnderTest.isValid());
  }
  
  //@Test
  public void afterPropertiesSet() throws Exception {
    Site2DRule classUnderTest = new Site2DRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.afterPropertiesSet();
  }
  
  //@Test
  public void afterPropertiesSet_missingCatalog() throws Exception {
    try {
      Site2DRule classUnderTest = new Site2DRule();
      classUnderTest.setRuleUtilities(ruleUtil);
      classUnderTest.afterPropertiesSet();
      fail("Expected BeanInitializationException");
    } catch (BeanInitializationException bie) {
      // pass
    }
  }
  
  //@Test
  public void afterPropertiesSet_missingRuleUtils() throws Exception {
    try {
      Site2DRule classUnderTest = new Site2DRule();
      classUnderTest.setCatalog(catalog);
      classUnderTest.afterPropertiesSet();
      fail("Expected BeanInitializationException");
    } catch (BeanInitializationException bie) {
      // pass
    }
  }

  //@Test
  public void testSetInterval() throws Exception {
    int[] valid = {1,2,3,4,5,6,10,12,15,20,30,60};
    for (int v : valid) {
      classUnderTest.setInterval(v);
    }
  }

  //@Test
  public void testSetInterval_invalid() throws Exception {
    int[] invalid = {0,7,8,9,11,13,14,16,17,18,19,21,22,23,24,25,26,27,28,29,35,40,61,62};
    for (int v : invalid) {
      try {
        classUnderTest.setInterval(v);
        fail("Expected IllegalArgumentException");
      } catch (IllegalArgumentException e) {
        // pass
      }
    }
  }
  
  //@Test
  public void sources() {
    List<String> sources = new ArrayList<String>();
    sources.add("nisse");
    classUnderTest.setSources(sources);
    assertSame(sources, classUnderTest.getSources());
  }
  
  //@Test
  public void area() {
    assertEquals(null, classUnderTest.getArea());
    classUnderTest.setArea("nisse");
    assertEquals("nisse", classUnderTest.getArea());
    classUnderTest.setArea(null);
    assertEquals(null, classUnderTest.getArea());
  }
  
  //@Test
  public void scanBased() {
    assertEquals(false, classUnderTest.isScanBased());
    classUnderTest.setScanBased(true);
    assertEquals(true, classUnderTest.isScanBased());
  }
  
  //@Test
  public void detectors() {
    List<String> detectors = new ArrayList<String>();
    assertEquals(0, classUnderTest.getDetectors().size());
    classUnderTest.setDetectors(detectors);
    assertSame(detectors, classUnderTest.getDetectors());
    classUnderTest.setDetectors(null);
    assertEquals(0, classUnderTest.getDetectors().size());
  }
  
  //@Test
  public void method() {
    assertEquals(Site2DRule.PCAPPI, classUnderTest.getMethod());
    classUnderTest.setMethod(Site2DRule.PPI);
    assertEquals(Site2DRule.PPI, classUnderTest.getMethod());
  }
  
  //@Test
  public void prodpar() {
    assertEquals("1000.0", classUnderTest.getProdpar());
    classUnderTest.setProdpar("1,1");
    assertEquals("1,1", classUnderTest.getProdpar());
    classUnderTest.setProdpar(null);
    assertEquals("", classUnderTest.getProdpar());
  }
  
  //@Test
  public void applyGRA() {
    assertEquals(false, classUnderTest.isApplyGRA());
    classUnderTest.setApplyGRA(true);
    assertEquals(true, classUnderTest.isApplyGRA());
  }
  
  //@Test
  public void ignoreMalfunc() {
    assertEquals(false, classUnderTest.isIgnoreMalfunc());
    classUnderTest.setIgnoreMalfunc(true);
    assertEquals(true, classUnderTest.isIgnoreMalfunc());
  }
  
  //@Test
  public void ctFilter() {
    assertEquals(false, classUnderTest.isCtFilter());
    classUnderTest.setCtFilter(true);
    assertEquals(true, classUnderTest.isCtFilter());
  }
  
  //@Test
  public void zr_A() {
    assertEquals(200.0, classUnderTest.getZR_A(), 4);
    classUnderTest.setZR_A(1.1);
    assertEquals(1.1, classUnderTest.getZR_A(), 4);
  }
  
  @Test
  public void zr_b() {
    assertEquals(1.6, classUnderTest.getZR_b(), 4);
    classUnderTest.setZR_b(1.2);
    assertEquals(1.2, classUnderTest.getZR_b(), 4);
  }
}
