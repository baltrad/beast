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

package eu.baltrad.beast.rules.namer;

import static org.junit.Assert.*;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.bdb.oh5.Attribute;
import eu.baltrad.bdb.oh5.Group;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.beast.InitializationException;
import static org.easymock.EasyMock.*;

/**
 * @author Anders Henja
 *
 */
public class TemplateNameCreatorMetadataNamerTest extends EasyMockSupport {
  private TemplateNameCreatorMetadataNamer classUnderTest = null;
  private MetadataNameCreatorFactory factory = null;
  private MetadataNameCreator nameCreator = null;
  
  @Before
  public void setUp() throws Exception {
    nameCreator = createMock(MetadataNameCreator.class);
    factory = createMock(MetadataNameCreatorFactory.class);
    classUnderTest = new TemplateNameCreatorMetadataNamer("${what/date}_${what/source:CMT}_${supportedTag}_${unsupportedTag}.h5");
    classUnderTest.setFactory(factory);
    classUnderTest.afterPropertiesSet();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
    factory = null;
    nameCreator = null;
  }
  
  @Test
  public void name_ok() {
    Metadata metadata = createBaseMetadata();
    
    expect(factory.supports("what/date")).andReturn(false);
    expect(factory.supports("supportedTag")).andReturn(true);
    expect(factory.get("supportedTag")).andReturn(nameCreator);
    expect(nameCreator.createName("supportedTag", metadata)).andReturn("createdname");
    expect(factory.supports("unsupportedTag")).andReturn(false);
    
    replayAll();
    
    String result = classUnderTest.name(metadata);
    
    verifyAll();
    assertEquals("20140101_testgmaps_2000_createdname_null.h5", result);
  }

  @Test
  public void name_inconsistent_namecreator() {
    Metadata metadata = createBaseMetadata();
    
    expect(factory.supports("what/date")).andReturn(false);
    expect(factory.supports("supportedTag")).andReturn(true);
    expect(factory.get("supportedTag")).andReturn(null);
    expect(factory.supports("unsupportedTag")).andReturn(false);
    
    replayAll();
    
    String result = classUnderTest.name(metadata);
    
    verifyAll();
    assertEquals("20140101_testgmaps_2000_null_null.h5", result);
  }

  @Test
  public void name_namecreator_returns_null() {
    Metadata metadata = createBaseMetadata();
    
    expect(factory.supports("what/date")).andReturn(false);
    expect(factory.supports("supportedTag")).andReturn(true);
    expect(factory.get("supportedTag")).andReturn(nameCreator);
    expect(nameCreator.createName("supportedTag", metadata)).andReturn(null);
    expect(factory.supports("unsupportedTag")).andReturn(false);
    
    replayAll();
    
    String result = classUnderTest.name(metadata);
    
    verifyAll();
    assertEquals("20140101_testgmaps_2000_null_null.h5", result);
  }

  @Test
  public void name_many_odd_characters_in_template() {
    Metadata metadata = createBaseMetadata();

    classUnderTest = new TemplateNameCreatorMetadataNamer("${_beast/name:ab cd ef}_${a/b/c}_${a:/#@+_-.}");
    classUnderTest.setFactory(factory);
    
    expect(factory.supports("_beast/name:ab cd ef")).andReturn(true);
    expect(factory.get("_beast/name:ab cd ef")).andReturn(nameCreator);
    expect(nameCreator.createName("_beast/name:ab cd ef", metadata)).andReturn("a");

    expect(factory.supports("a/b/c")).andReturn(true);
    expect(factory.get("a/b/c")).andReturn(nameCreator);
    expect(nameCreator.createName("a/b/c", metadata)).andReturn("b");

    expect(factory.supports("a:/#@+_-.")).andReturn(true);
    expect(factory.get("a:/#@+_-.")).andReturn(nameCreator);
    expect(nameCreator.createName("a:/#@+_-.", metadata)).andReturn("c");
    
    replayAll();
    
    String result = classUnderTest.name(metadata);
    
    verifyAll();
    assertEquals("a_b_c", result);
  }
  
  @Test
  public void afterPropertiesSet_invalid() {
    classUnderTest = new TemplateNameCreatorMetadataNamer("${what/date}_${what/source:CMT}_${supportedTag}_${unsupportedTag}.h5");
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected InitializationException");
    } catch (InitializationException e) {
      // pass
    }
  }
  
  private Metadata createBaseMetadata() {
    Metadata metadata = new Metadata();
    metadata.addNode("/", new Group("dataset1"));
    metadata.addNode("/", new Group("dataset2"));
    metadata.addNode("/", new Group("what"));
    metadata.addNode("/", new Group("where"));
    metadata.addNode("/what", new Attribute("date", "20140101"));
    metadata.addNode("/what", new Attribute("time", "100000"));
    metadata.addNode("/what", new Attribute("source", "ORG:82,CMT:testgmaps_2000"));
    metadata.addNode("/what", new Attribute("object", "IMAGE"));
    metadata.addNode("/dataset1", new Group("data1"));
    metadata.addNode("/dataset1", new Group("data2"));
    metadata.addNode("/dataset1", new Group("what"));
    metadata.addNode("/dataset1", new Group("where"));
    metadata.addNode("/dataset1/data1", new Group("what"));
    metadata.addNode("/dataset1/data1", new Group("where"));
    metadata.addNode("/dataset1/data2", new Group("what"));
    metadata.addNode("/dataset1/data2", new Group("where"));

    metadata.addNode("/dataset2", new Group("data1"));
    metadata.addNode("/dataset2", new Group("data2"));
    metadata.addNode("/dataset2", new Group("what"));
    metadata.addNode("/dataset2", new Group("where"));
    metadata.addNode("/dataset2/data1", new Group("what"));
    metadata.addNode("/dataset2/data1", new Group("where"));
    metadata.addNode("/dataset2/data2", new Group("what"));
    metadata.addNode("/dataset2/data2", new Group("where"));
    
    return metadata;
  }
}
