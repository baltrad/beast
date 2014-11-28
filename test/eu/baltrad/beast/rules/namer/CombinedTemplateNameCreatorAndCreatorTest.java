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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.bdb.oh5.Attribute;
import eu.baltrad.bdb.oh5.Group;
import eu.baltrad.bdb.oh5.Metadata;

/**
 * @author Anders Henja
 * @date 2014-11-28
 */
public class CombinedTemplateNameCreatorAndCreatorTest {
  private TemplateNameCreatorMetadataNamer classUnderTest;
  private MetadataNameCreatorFactory factory;
  private QuantityHexNameCreator quantityHexNameCreator;
  private FormattableDateTimeNameCreator dateTimeNameCreator;
  
  @Before
  public void setUp() {
    factory = new MetadataNameCreatorFactory();
    quantityHexNameCreator = new QuantityHexNameCreator(new File(getClass().getResource("quantities_fixture.xml").getFile()));
    dateTimeNameCreator = new FormattableDateTimeNameCreator();
    List<MetadataNameCreator> creators = new ArrayList<MetadataNameCreator>();
    creators.add(quantityHexNameCreator);
    creators.add(dateTimeNameCreator);
    factory.setCreators(creators);
  }
  
  @After
  public void tearDown() {
    classUnderTest = null;
    factory = null;
    quantityHexNameCreator = null;
    dateTimeNameCreator = null;
  }
  
  @Test
  public void standard_attributes() {
    Metadata metadata = createBaseMetadata();
    
    classUnderTest = new TemplateNameCreatorMetadataNamer("${what/source:CMT}.toupper().substring(9)/${_beast/datetime:yyyy/MM/dd/HH/mm/ss}/${what/object}.tolower()_${_beast/hexcode}.h5");
    
    classUnderTest.setFactory(factory);
    
    assertEquals("TESTGMAPS/2014/01/01/10/00/00/image_0x3.h5", classUnderTest.name(metadata)); 
  }

  private Metadata createBaseMetadata() {
    Metadata metadata = new Metadata();
    metadata.addNode("/", new Group("dataset1"));
    metadata.addNode("/", new Group("dataset2"));
    metadata.addNode("/", new Group("what"));
    metadata.addNode("/", new Group("where"));
    metadata.addNode("/", new Group("how"));
    metadata.addNode("/what", new Attribute("date", "20140101"));
    metadata.addNode("/what", new Attribute("time", "100000"));
    metadata.addNode("/what", new Attribute("source", "ORG:82,CMT:testgmaps_2000"));
    metadata.addNode("/what", new Attribute("object", "IMAGE"));
    metadata.addNode("/how", new Attribute("task", "se.some.qc"));
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
    
    metadata.addNode("/dataset1/data1/what", new Attribute("quantity", "DBZH"));
    metadata.addNode("/dataset2/data1/what", new Attribute("quantity", "DBZH"));
    metadata.addNode("/dataset1/data2/what", new Attribute("quantity", "TH"));
    metadata.addNode("/dataset2/data2/what", new Attribute("quantity", "ABCD"));
    metadata.addNode("/dataset1/what", new Attribute("quantity", "LSB"));
    metadata.addNode("/dataset2/what", new Attribute("quantity", "NLSB"));
    
    return metadata;
  }
}
