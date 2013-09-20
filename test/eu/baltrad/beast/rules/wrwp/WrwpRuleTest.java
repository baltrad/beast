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

package eu.baltrad.beast.rules.wrwp;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
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
import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.util.IRuleUtilities;

/**
 * @author Anders Henja
 *
 */
public class WrwpRuleTest extends EasyMockSupport {
  private Catalog catalog = null;
  private IRuleUtilities ruleUtil = null;
  private WrwpRule classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    catalog = createMock(Catalog.class);
    ruleUtil = createMock(IRuleUtilities.class);
    classUnderTest = new WrwpRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtil);
  }
  
  @After
  public void tearDown() throws Exception {
    catalog = null;
    ruleUtil = null;
    classUnderTest = null;
  }
  
  @Test
  public void test_interval() {
    assertEquals(200, classUnderTest.getInterval());
    classUnderTest.setInterval(300);
    assertEquals(300, classUnderTest.getInterval());
  }
  
  @Test
  public void test_maxheight() {
    assertEquals(12000, classUnderTest.getMaxheight());
    classUnderTest.setMaxheight(3000);
    assertEquals(3000, classUnderTest.getMaxheight());
  }
  
  @Test
  public void test_mindistance() {
    assertEquals(4000, classUnderTest.getMindistance());
    classUnderTest.setMindistance(3000);
    assertEquals(3000, classUnderTest.getMindistance());
  }

  @Test
  public void test_maxdistance() {
    assertEquals(40000, classUnderTest.getMaxdistance());
    classUnderTest.setMaxdistance(3000);
    assertEquals(3000, classUnderTest.getMaxdistance());
  }

  @Test
  public void test_minelevationangle() {
    assertEquals(2.5, classUnderTest.getMinelevationangle(), 4);
    classUnderTest.setMinelevationangle(3.5);
    assertEquals(3.5, classUnderTest.getMinelevationangle(), 4);
  }

  @Test
  public void test_minvelocitythreshold() {
    assertEquals(2.0, classUnderTest.getMinvelocitythreshold(), 4);
    classUnderTest.setMinvelocitythreshold(5.0);
    assertEquals(5.0, classUnderTest.getMinvelocitythreshold(), 4);
  }
  
  @Test
  public void test_handle() {
    FileEntry fe = createMock(FileEntry.class);
    Metadata meta = createMock(Metadata.class);
    Source src = new Source("seang");
    List<String> sources = new ArrayList<String>();
    UUID uuid = UUID.randomUUID();
    
    sources.add("searl");
    sources.add("seang");
    
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(fe);
    
    classUnderTest.setInterval(500);
    classUnderTest.setMaxdistance(10000);
    classUnderTest.setMindistance(1000);
    classUnderTest.setMaxheight(10000);
    classUnderTest.setMinelevationangle(3.5);
    classUnderTest.setMinvelocitythreshold(4.5);
    classUnderTest.setSources(sources);
    
    expect(fe.getMetadata()).andReturn(meta);
    expect(meta.getWhatObject()).andReturn("PVOL");
    expect(fe.getSource()).andReturn(src);
    expect(fe.getUuid()).andReturn(uuid);
    
    replayAll();
    
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(msg);
    
    verifyAll();
    assertEquals("eu.baltrad.beast.GenerateWrwp", result.getAlgorithm());
    assertEquals(1, result.getFiles().length);
    assertEquals(uuid.toString(), result.getFiles()[0]);
    assertEquals(6, result.getArguments().length);
    assertEquals("--interval=500", result.getArguments()[0]);
    assertEquals("--maxheight=10000", result.getArguments()[1]);
    assertEquals("--mindistance=1000", result.getArguments()[2]);
    assertEquals("--maxdistance=10000", result.getArguments()[3]);
    assertEquals("--minelevationangle=3.5", result.getArguments()[4]);
    assertEquals("--velocitythreshold=4.5", result.getArguments()[5]);
  }

  @Test
  public void test_handle_notSupportedSource() {
    FileEntry fe = createMock(FileEntry.class);
    Metadata meta = createMock(Metadata.class);
    Source src = new Source("sekkr");
    List<String> sources = new ArrayList<String>();
    
    sources.add("searl");
    sources.add("seang");
    
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(fe);
    
    classUnderTest.setSources(sources);
    
    expect(fe.getMetadata()).andReturn(meta);
    expect(meta.getWhatObject()).andReturn("PVOL");
    expect(fe.getSource()).andReturn(src);
    
    replayAll();
    
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(msg);
    
    verifyAll();
    assertEquals(null, result);
  }

  @Test
  public void test_handle_notDataMessage() {
    BltAlertMessage msg = new BltAlertMessage();
    
    replayAll();
    
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(msg);
    
    verifyAll();
    assertEquals(null, result);
  }

  @Test
  public void test_getType() {
    assertEquals("blt_wrwp", classUnderTest.getType());
  }
  
  @Test
  public void test_isValid() {
    assertEquals(true, classUnderTest.isValid());
  }
  
  @Test
  public void testAfterPropertiesSet() throws Exception {
    classUnderTest = new WrwpRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.afterPropertiesSet();
  }
  
  @Test
  public void testAfterPropertiesSet_noCatalog() throws Exception {
    classUnderTest = new WrwpRule();
    classUnderTest.setRuleUtilities(ruleUtil);
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected BeanInitializationException");
    } catch (BeanInitializationException bie) {
      // pass
    }
  }
  
  @Test
  public void testAfterPropertiesSet_nothingSet() throws Exception
  {
    classUnderTest = new WrwpRule();
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected BeanInitializationException");
    } catch (BeanInitializationException bie) {
      // pass
    }
  }
  
}
