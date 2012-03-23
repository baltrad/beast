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

package eu.baltrad.beast.rules.gmap;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanInitializationException;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.Time;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;

/**
 * Tests the google map rule
 * @author Anders Henja
 * @date 2012-03-21
 */
public class GoogleMapRuleTest extends EasyMockSupport {
  private GoogleMapRule classUnderTest = null;
  private Catalog catalog = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new GoogleMapRule();
    catalog = createMock(Catalog.class);
    classUnderTest.setCatalog(catalog);
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void testAfterPropertiesSet() throws Exception {
    replayAll();

    classUnderTest.afterPropertiesSet();

    verifyAll();
  }
  
  @Test
  public void testAfterPropertiesSet_noCatalog() throws Exception {
    classUnderTest.setCatalog(null);

    replayAll();

    try {
      classUnderTest.afterPropertiesSet();
      Assert.fail("Expected BeanInitializationException");
    } catch (BeanInitializationException e) {
      // pass
    }
    
    verifyAll();
  }
  
  @Test
  public void testIsSupportedArea() throws Exception {
    classUnderTest.setArea("swegmaps");
    
    assertEquals(true, classUnderTest.isSupportedArea("ORG:82,CMT:swegmaps"));
    assertEquals(true, classUnderTest.isSupportedArea("CMT:swegmaps,ORG:82"));
    assertEquals(true, classUnderTest.isSupportedArea("PLC:ang,CMT:swegmaps,ORG:82"));

    assertEquals(false, classUnderTest.isSupportedArea("ORG:82,CMT:swegmaps_2000"));
    assertEquals(false, classUnderTest.isSupportedArea("CMT:swegmaps_2000, ORG:82"));
    assertEquals(false, classUnderTest.isSupportedArea("PLC:ang,CMT:swegmaps_2000, ORG:82"));

    classUnderTest.setArea("swegmaps_2000");
    assertEquals(false, classUnderTest.isSupportedArea("ORG:82,CMT:swegmaps"));
    assertEquals(false, classUnderTest.isSupportedArea("CMT:swegmaps,ORG:82"));
    assertEquals(false, classUnderTest.isSupportedArea("PLC:ang,CMT:swegmaps,ORG:82"));

    assertEquals(true, classUnderTest.isSupportedArea("ORG:82,CMT:swegmaps_2000"));
    assertEquals(true, classUnderTest.isSupportedArea("CMT:swegmaps_2000, ORG:82"));
    assertEquals(true, classUnderTest.isSupportedArea("PLC:ang,CMT:swegmaps_2000, ORG:82"));
  }
  
  @Test
  public void testCreateOutputName() throws Exception {
    classUnderTest.setPath("/tmp/path");
    classUnderTest.setArea("ar");
    assertEquals("/tmp/path/ar/2010/01/01/201001010215.png",
        classUnderTest.createOutputName(new Date(2010,1,1), new Time(2,15,0)));

    classUnderTest.setArea("arar");
    assertEquals("/tmp/path/arar/2011/01/01/201101010215.png",
        classUnderTest.createOutputName(new Date(2011,1,1), new Time(2,15,0)));
  }  
  
  @Test
  public void testCreateOutputName_nullPath() throws Exception {
    classUnderTest.setPath(null);
    classUnderTest.setArea("ar");

    assertEquals("/ar/2010/01/01/201001010215.png",
        classUnderTest.createOutputName(new Date(2010,1,1), new Time(2,15,0)));

    classUnderTest.setArea("arar");
    assertEquals("/arar/2011/01/01/201101010215.png",
        classUnderTest.createOutputName(new Date(2011,1,1), new Time(2,15,0)));
  }  

  @Test
  public void testType() throws Exception {
    assertEquals("blt_gmap", GoogleMapRule.TYPE);
    assertEquals(GoogleMapRule.TYPE, classUnderTest.getType());
  }
  
  @Test
  public void isValid() throws Exception {
    classUnderTest.setArea(null);
    assertEquals(false, classUnderTest.isValid());
    classUnderTest.setArea("somedummyarea");
    assertEquals(true, classUnderTest.isValid());
    classUnderTest.setArea("");
    assertEquals(false, classUnderTest.isValid());
  }
  
  @Test
  public void testHandle() throws Exception {
    Date date = new Date(2010, 1, 1);
    Time time = new Time(10, 0, 0);
    UUID uuid = UUID.randomUUID();
    BltDataMessage message = createMock(BltDataMessage.class);
    Metadata metadata = createMock(Metadata.class);
    FileEntry fe = createMock(FileEntry.class);

    classUnderTest.setArea("swegmaps");
    classUnderTest.setPath("/tmp/data");

    expect(message.getFileEntry()).andReturn(fe);
    expect(fe.getMetadata()).andReturn(metadata).times(4);
    expect(metadata.getWhatObject()).andReturn("COMP");
    expect(metadata.getWhatSource()).andReturn("ORG:82,CMT:swegmaps");
    expect(metadata.getWhatDate()).andReturn(date);
    expect(metadata.getWhatTime()).andReturn(time);
    expect(fe.getUuid()).andReturn(uuid);
    expect(catalog.getFileCatalogPath(uuid.toString())).andReturn("/nisse/1.h5");
    
    replayAll();
    
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(message);
    
    verifyAll();
    
    assertEquals("eu.baltrad.beast.creategmap", result.getAlgorithm());
    assertEquals(1, result.getFiles().length);
    assertEquals("/nisse/1.h5", result.getFiles()[0]);
    assertEquals(1, result.getArguments().length);
    assertEquals("--outfile=/tmp/data/swegmaps/2010/01/01/201001011000.png", result.getArguments()[0]);
  }
  
  @Test
  public void testHandle_isInvalidArea() throws Exception {
    BltDataMessage message = createMock(BltDataMessage.class);

    classUnderTest.setArea(null);

    replayAll();
    
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(message);
    
    verifyAll();
    assertNull(result);
  }

  @Test
  public void testHandle_isUnsupportedMessage() throws Exception {
    IBltMessage message = new IBltMessage() { };

    classUnderTest.setArea("swegmaps");

    replayAll();
    
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(message);
    
    verifyAll();
    assertNull(result);
  }
  
  @Test
  public void testHandle_notAComposite() throws Exception {
    BltDataMessage message = createMock(BltDataMessage.class);
    FileEntry fe = createMock(FileEntry.class);
    Metadata metadata = createMock(Metadata.class);

    classUnderTest.setArea("swegmaps");

    expect(message.getFileEntry()).andReturn(fe);
    expect(fe.getMetadata()).andReturn(metadata).times(1);
    expect(metadata.getWhatObject()).andReturn("PVOL");
    
    replayAll();
    
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(message);
    
    verifyAll();
    assertNull(result);
  }
  
  @Test
  public void testHandle_notSupportedArea() throws Exception {
    Date date = new Date(2010, 1, 1);
    Time time = new Time(10, 0, 0);
    BltDataMessage message = createMock(BltDataMessage.class);
    Metadata metadata = createMock(Metadata.class);
    FileEntry fe = createMock(FileEntry.class);

    classUnderTest.setArea("swegmaps");
    classUnderTest.setPath("/tmp/data");

    expect(message.getFileEntry()).andReturn(fe);
    expect(fe.getMetadata()).andReturn(metadata).times(4);
    expect(metadata.getWhatObject()).andReturn("COMP");
    expect(metadata.getWhatSource()).andReturn("ORG:82,CMT:swegmaps_2000");
    expect(metadata.getWhatDate()).andReturn(date);
    expect(metadata.getWhatTime()).andReturn(time);
    
    replayAll();
    
    BltGenerateMessage result = (BltGenerateMessage)classUnderTest.handle(message);
    
    verifyAll();
    assertNull(result);
  }
}
