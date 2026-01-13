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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.oh5.MetadataMatcher;
import eu.baltrad.bdb.oh5.Source;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.db.IFilter;
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
  private MetadataMatcher matcher = null;
  
  @Before
  public void setUp() throws Exception {
    catalog = createMock(Catalog.class);
    ruleUtil = createMock(IRuleUtilities.class);
    matcher = createMock(MetadataMatcher.class);
    classUnderTest = new WrwpRule();
    classUnderTest.setCatalog(catalog);
    classUnderTest.setRuleUtilities(ruleUtil);
    classUnderTest.setMatcher(matcher);
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
  public void test_maxelevationangle() {
    assertEquals(45.0, classUnderTest.getMaxelevationangle(), 4);
    classUnderTest.setMaxelevationangle(35.0);
    assertEquals(35.0, classUnderTest.getMaxelevationangle(), 4);
  }
  
  @Test
  public void test_conditionalminelevationangle() {
    assertEquals(9.5, classUnderTest.getConditionalminelevationangle(), 4);
    classUnderTest.setConditionalminelevationangle(12.5);
    assertEquals(12.5, classUnderTest.getConditionalminelevationangle(), 4);
  }
  
  @Test
  public void test_heightthreshold() {
    assertEquals(2000.0, classUnderTest.getHeightthreshold(), 4);
    classUnderTest.setHeightthreshold(3000.0);
    assertEquals(3000.0, classUnderTest.getHeightthreshold(), 4);
  }
  
  @Test
  public void test_minnyquistinterval() {
    assertEquals(10.0, classUnderTest.getMinnyquistinterval(), 4);
    classUnderTest.setMinnyquistinterval(11.0);
    assertEquals(11.0, classUnderTest.getMinnyquistinterval(), 4);
  }
  
  @Test
  public void test_numbergapbins() {
    assertEquals(8, classUnderTest.getNumbergapbins());
    classUnderTest.setNumbergapbins(9);
    assertEquals(9, classUnderTest.getNumbergapbins());
  }
  
  @Test
  public void test_minnumbergapsamples() {
    assertEquals(5, classUnderTest.getMinnumbergapsamples());
    classUnderTest.setMinnumbergapsamples(7);
    assertEquals(7, classUnderTest.getMinnumbergapsamples());
  }
  
  @Test
  public void test_maxnumberstandarddeviations() {
    assertEquals(0, classUnderTest.getMaxnumberstandarddeviations());
    classUnderTest.setMaxnumberstandarddeviations(2);
    assertEquals(2, classUnderTest.getMaxnumberstandarddeviations());
  }
  
  @Test
  public void test_maxvelocitydeviation() {
    assertEquals(10.0, classUnderTest.getMaxvelocitydeviation(), 4);
    classUnderTest.setMaxvelocitydeviation(12.0);
    assertEquals(12.0, classUnderTest.getMaxvelocitydeviation(), 4);
  }
  
  @Test
  public void test_minvelocitythreshold() {
    assertEquals(2.0, classUnderTest.getMinvelocitythreshold(), 4);
    classUnderTest.setMinvelocitythreshold(5.0);
    assertEquals(5.0, classUnderTest.getMinvelocitythreshold(), 4);
  }
  
  @Test
  public void test_maxvelocitythreshold() {
    assertEquals(60.0, classUnderTest.getMaxvelocitythreshold(), 4);
    classUnderTest.setMaxvelocitythreshold(50.0);
    assertEquals(50.0, classUnderTest.getMaxvelocitythreshold(), 4);
  }  
  
  @Test
  public void test_minsamplesizereflectivity()  {
    assertEquals(40, classUnderTest.getMinsamplesizereflectivity());
    classUnderTest.setMinsamplesizereflectivity(30);
    assertEquals(30, classUnderTest.getMinsamplesizereflectivity());
  }

  @Test
  public void test_minsamplesizewind()  {
    assertEquals(40, classUnderTest.getMinsamplesizewind());
    classUnderTest.setMinsamplesizewind(30);
    assertEquals(30, classUnderTest.getMinsamplesizewind());
  }
    
  @Test
  public void test_wrwpprocessingmethod()  {
    assertEquals("SMHI", classUnderTest.getWrwpprocessingmethod());
    classUnderTest.setWrwpprocessingmethod("KNMI");
    assertEquals("KNMI", classUnderTest.getWrwpprocessingmethod());
  }

  @Test
  public void test_fields() {
    assertEquals(0, classUnderTest.getFields().size());
    List<String> newFields = new ArrayList<String>();
    newFields.add("ff");
    newFields.add(" fg");
    newFields.add("fh ");

    assertEquals(true, classUnderTest.setFields(newFields));
    
    assertEquals(3, classUnderTest.getFields().size());
    assertEquals("ff", classUnderTest.getFields().get(0));
    assertEquals("fg", classUnderTest.getFields().get(1));
    assertEquals("fh", classUnderTest.getFields().get(2));
  }

  @Test
  public void test_invalid_field() {
    assertEquals(0, classUnderTest.getFields().size());
    List<String> newFields = new ArrayList<String>();
    newFields.add("f f");

    assertEquals(false, classUnderTest.setFields(newFields));
    
    assertEquals(0, classUnderTest.getFields().size());
  }
  
  @Test
  public void test_fields_str() {
    assertEquals("", classUnderTest.getFieldsAsStr());
    String[][] valid_values={
        {"ff,ff_dev,dd ","ff,ff_dev,dd"},
        {" ff, ff_dev, dd", "ff,ff_dev,dd"},
        {",ff,ff_dev,dd", "ff,ff_dev,dd"},
        {"   ", ""}}; 
    
    for (String[] x : valid_values) {
      assertEquals(true, classUnderTest.setFields(x[0]));
      assertEquals(x[1], classUnderTest.getFieldsAsStr());
    }
  }
  
  @Test
  public void test_fields_nok_str() {
    assertEquals("", classUnderTest.getFieldsAsStr());
    String[] valid_values={
        " ff, ff _dev, dd",
        " f a  "}; 
    
    for (String x : valid_values) {
      assertEquals(false, classUnderTest.setFields(x));
      assertEquals("", classUnderTest.getFieldsAsStr());
    }
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
    classUnderTest.setMaxelevationangle(43.5);
    classUnderTest.setConditionalminelevationangle(8.5);
    classUnderTest.setHeightthreshold(2500.0);
    classUnderTest.setMinnyquistinterval(8.7);
    classUnderTest.setNumbergapbins(11);
    classUnderTest.setMinnumbergapsamples(8);
    classUnderTest.setMaxnumberstandarddeviations(5);
    classUnderTest.setMaxvelocitydeviation(11.5);
    classUnderTest.setWrwpprocessingmethod("KNMI");
    classUnderTest.setMinvelocitythreshold(4.5);
    classUnderTest.setMaxvelocitythreshold(44.5);
    classUnderTest.setMinsamplesizereflectivity(30);
    classUnderTest.setMinsamplesizewind(29);
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
    assertEquals(10, result.getArguments().length);
    assertEquals("--interval=500", result.getArguments()[0]);
    assertEquals("--maxheight=10000", result.getArguments()[1]);
    assertEquals("--mindistance=1000", result.getArguments()[2]);
    assertEquals("--maxdistance=10000", result.getArguments()[3]);
    assertEquals("--minelevationangle=3.5", result.getArguments()[4]);
    assertEquals("--maxelevationangle=43.5", result.getArguments()[5]);
    assertEquals("--conditionalminelevationangle=8.5", result.getArguments()[6]);
    assertEquals("--heightthreshold=2500.0", result.getArguments()[7]);
    assertEquals("--minnyquistinterval=8.7", result.getArguments()[8]);
    assertEquals("--numbergapbins=11", result.getArguments()[9]);
    assertEquals("--minnumbergapsamples=8", result.getArguments()[10]);
    assertEquals("--maxnumberstandarddeviations=5", result.getArguments()[11]);
    assertEquals("--maxvelocitydeviation=11.5", result.getArguments()[12]);
    assertEquals("--wrwpprocessingmethod=KNMI", result.getArguments()[13]);
    assertEquals("--velocitythreshold=4.5", result.getArguments()[14]);
    assertEquals("--maxvelocitythreshold=44.5", result.getArguments()[15]);
    assertEquals("--minsamplesizereflectivity=30", result.getArguments()[16]);
    assertEquals("--minsamplesizewind=29", result.getArguments()[17]);
  }

  @Test
  public void test_handle_with_fields() {
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
    classUnderTest.setMaxelevationangle(43.5);
    classUnderTest.setConditionalminelevationangle(8.5);
    classUnderTest.setHeightthreshold(2500.0);
    classUnderTest.setMinnyquistinterval(8.7);
    classUnderTest.setNumbergapbins(11);
    classUnderTest.setMinnumbergapsamples(8);
    classUnderTest.setMaxnumberstandarddeviations(5);
    classUnderTest.setMaxvelocitydeviation(11.5);
    classUnderTest.setWrwpprocessingmethod("KNMI");
    classUnderTest.setMinvelocitythreshold(4.5);
    classUnderTest.setMaxvelocitythreshold(44.5);
    classUnderTest.setMinsamplesizereflectivity(30);
    classUnderTest.setMinsamplesizewind(29);
    classUnderTest.setSources(sources);
    classUnderTest.setFields("ff,ff_dev,dd");
    
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
    assertEquals(11, result.getArguments().length);
    assertEquals("--interval=500", result.getArguments()[0]);
    assertEquals("--maxheight=10000", result.getArguments()[1]);
    assertEquals("--mindistance=1000", result.getArguments()[2]);
    assertEquals("--maxdistance=10000", result.getArguments()[3]);
    assertEquals("--minelevationangle=3.5", result.getArguments()[4]);
    assertEquals("--maxelevationangle=43.5", result.getArguments()[5]);
    assertEquals("--conditionalminelevationangle=8.5", result.getArguments()[6]);
    assertEquals("--heightthreshold=2500.0", result.getArguments()[7]);
    assertEquals("--minnyquistinterval=8.7", result.getArguments()[8]);
    assertEquals("--numbergapbins=11", result.getArguments()[9]);
    assertEquals("--minnumbergapsamples=8", result.getArguments()[10]);
    assertEquals("--maxnumberstandarddeviations=5", result.getArguments()[11]);
    assertEquals("--maxvelocitydeviation=11.5", result.getArguments()[12]);
    assertEquals("--wrwpprocessingmethod=KNMI", result.getArguments()[13]);
    assertEquals("--velocitythreshold=4.5", result.getArguments()[14]);
    assertEquals("--maxvelocitythreshold=44.5", result.getArguments()[15]);
    assertEquals("--minsamplesizereflectivity=30", result.getArguments()[16]);
    assertEquals("--minsamplesizewind=29", result.getArguments()[17]);
    assertEquals("--fields=ff,ff_dev,dd", result.getArguments()[18]);
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
  
  @Test
  public void handle_filterNotMatching() throws Exception {

    FileEntry fe = createMock(FileEntry.class);
    Metadata m = createMock(Metadata.class);
    IFilter filter = createMock(IFilter.class);
    Expression xpr = createMock(Expression.class);
    
    List<String> sources = new ArrayList<String>();
    sources.add("searl");
    sources.add("seang");
    classUnderTest.setSources(sources);
    
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(fe);
    
    expect(fe.getMetadata()).andReturn(m).anyTimes();
    expect(m.getWhatObject()).andReturn("PVOL");
    expect(filter.getExpression()).andReturn(xpr);
    expect(matcher.match(m, xpr)).andReturn(false);

    classUnderTest.setFilter(filter);
    classUnderTest.setMatcher(matcher);
    
    replayAll();
    
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(msg);
    
    verifyAll();
    assertNull(result);
  }
  
  @Test
  public void handle_filterMatching() throws Exception {

    Source fileSource = new Source("searl");
    FileEntry fe = createMock(FileEntry.class);
    Metadata m = createMock(Metadata.class);
    IFilter filter = createMock(IFilter.class);
    Expression xpr = createMock(Expression.class);
    
    List<String> sources = new ArrayList<String>();
    sources.add("searl");
    sources.add("seang");
    classUnderTest.setSources(sources);
    
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(fe);
    
    expect(fe.getMetadata()).andReturn(m).anyTimes();
    expect(fe.getUuid()).andReturn(new UUID(10, 99)).anyTimes();
    expect(m.getWhatObject()).andReturn("PVOL");
    expect(fe.getSource()).andReturn(fileSource);
    expect(filter.getExpression()).andReturn(xpr);
    expect(matcher.match(m, xpr)).andReturn(true);

    classUnderTest.setFilter(filter);
    classUnderTest.setMatcher(matcher);
    
    replayAll();
    
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(msg);
    
    verifyAll();
    assertNotNull(result);
    assertEquals(result.getAlgorithm(), "eu.baltrad.beast.GenerateWrwp");
    assertEquals(result.getFiles().length, 1);
    assertEquals(result.getFiles()[0], "00000000-0000-000a-0000-000000000063"); // UUID(10, 99) should give this
  } 
  
}
