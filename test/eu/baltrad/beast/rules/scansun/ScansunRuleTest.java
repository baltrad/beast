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

package eu.baltrad.beast.rules.scansun;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.oh5.Source;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;

/**
 * @author Anders Henja
 */
public class ScansunRuleTest extends EasyMockSupport {
  private ScansunRule classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new ScansunRule();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void handle() throws Exception {
    BltDataMessage msg = createMock(BltDataMessage.class);
    FileEntry fe = createMock(FileEntry.class);
    Metadata md = createMock(Metadata.class);
    Source source = createMock(Source.class);
    UUID ruid = UUID.randomUUID();
    
    List<String> sources = new ArrayList<String>();
    sources.add("seses");
    sources.add("nisse");
    classUnderTest.setSources(sources);
    
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
    assertEquals(0, result.getArguments().length);
  }

  @Test
  public void handle_2() throws Exception {
    BltDataMessage msg = createMock(BltDataMessage.class);
    FileEntry fe = createMock(FileEntry.class);
    Metadata md = createMock(Metadata.class);
    Source source = createMock(Source.class);
    UUID ruid = UUID.randomUUID();
    
    List<String> sources = new ArrayList<String>();
    sources.add("seses");
    sources.add("nisse");
    classUnderTest.setSources(sources);
    
    expect(msg.getFileEntry()).andReturn(fe).anyTimes();
    expect(fe.getMetadata()).andReturn(md).anyTimes();
    expect(md.getWhatObject()).andReturn("PVOL");
    expect(fe.getSource()).andReturn(source);
    expect(source.getName()).andReturn("nisse");
    expect(fe.getUuid()).andReturn(ruid);
    
    replayAll();
    
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(msg);
    
    verifyAll();
    assertNotNull(result);
    assertEquals(1, result.getFiles().length);
    assertEquals(ruid.toString(), result.getFiles()[0]);
    assertEquals(0, result.getArguments().length);
  }

  @Test
  public void handle_3() throws Exception {
    BltDataMessage msg = createMock(BltDataMessage.class);
    FileEntry fe = createMock(FileEntry.class);
    Metadata md = createMock(Metadata.class);
    Source source = createMock(Source.class);
    
    List<String> sources = new ArrayList<String>();
    sources.add("seses");
    sources.add("nisse");
    classUnderTest.setSources(sources);
    
    expect(msg.getFileEntry()).andReturn(fe).anyTimes();
    expect(fe.getMetadata()).andReturn(md).anyTimes();
    expect(md.getWhatObject()).andReturn("PVOL");
    expect(fe.getSource()).andReturn(source);
    expect(source.getName()).andReturn(null);
    
    replayAll();
    
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(msg);
    
    verifyAll();
    assertNull(result);
  }
  
  @Test
  public void ruleId() {
    assertEquals(-1, classUnderTest.getRuleId());
    classUnderTest.setRuleId(10);
    assertEquals(10, classUnderTest.getRuleId());
  }
  
  @Test
  public void getType() {
    assertEquals("blt_scansun", classUnderTest.getType());
  }

  @Test
  public void isValid() {
    assertEquals(true, classUnderTest.isValid());
  }

  @Test
  public void sources() {
    List<String> sources = new ArrayList<String>();
    assertEquals(0, classUnderTest.getSources().size());
    classUnderTest.setSources(sources);
    assertSame(sources, classUnderTest.getSources());
  }
}
