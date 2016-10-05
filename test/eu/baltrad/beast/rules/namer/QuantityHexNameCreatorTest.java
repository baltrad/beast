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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.bdb.oh5.Attribute;
import eu.baltrad.bdb.oh5.Group;
import eu.baltrad.bdb.oh5.Metadata;

/**
 * @author Anders Henja
 */
public class QuantityHexNameCreatorTest extends EasyMockSupport {
  private QuantityHexNameCreator classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    classUnderTest = new QuantityHexNameCreator(createDefaultMap());
  }

  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  @Test
  public void supports() {
    assertEquals(true, classUnderTest.supports("_beast/hexcode"));
    assertEquals(false, classUnderTest.supports("/hexcode"));
    assertEquals(false, classUnderTest.supports("${klsdak}"));
    assertEquals(false, classUnderTest.supports(null));
  }
  
  @Test
  public void testEightQuantities() {
    Metadata metadata = createBaseMetadata();
    
    metadata.addNode("/dataset1", new Group("data3"));
    metadata.addNode("/dataset1", new Group("data4"));
    metadata.addNode("/dataset1", new Group("data5"));
    metadata.addNode("/dataset1", new Group("data6"));
    metadata.addNode("/dataset1", new Group("data7"));
    metadata.addNode("/dataset1", new Group("data8"));
    
    metadata.addNode("/dataset1/data1", new Group("what"));
    metadata.addNode("/dataset1/data2", new Group("what"));
    metadata.addNode("/dataset1/data3", new Group("what"));
    metadata.addNode("/dataset1/data4", new Group("what"));
    metadata.addNode("/dataset1/data5", new Group("what"));
    metadata.addNode("/dataset1/data6", new Group("what"));
    metadata.addNode("/dataset1/data7", new Group("what"));
    metadata.addNode("/dataset1/data8", new Group("what"));
    
    metadata.addNode("/dataset1/data1/what", new Attribute("quantity", "DBZH")); // 63
    metadata.addNode("/dataset1/data2/what", new Attribute("quantity", "LDR")); // 40
    metadata.addNode("/dataset1/data3/what", new Attribute("quantity", "WRADH")); // 53
    metadata.addNode("/dataset1/data4/what", new Attribute("quantity", "SNRH")); // 49
    metadata.addNode("/dataset1/data5/what", new Attribute("quantity", "SQIH"));  // 51
    metadata.addNode("/dataset1/data6/what", new Attribute("quantity", "VRADH")); // 60
    metadata.addNode("/dataset1/data7/what", new Attribute("quantity", "CCORH")); // 47
    metadata.addNode("/dataset1/data8/what", new Attribute("quantity", "TH"));  // 62

    classUnderTest = new QuantityHexNameCreator(new File(getClass().getResource("quantities_fixture.xml").getFile()));
    
    String result = classUnderTest.createName("_beast/hexcode", metadata);
    
    assertEquals("0x81540b", result);
  }
  
  @Test
  public void testCreate_1_shiftRight() {
    Metadata metadata = createBaseMetadata();
    metadata.addNode("/dataset1/data1/what", new Attribute("quantity", "DBZH"));
    metadata.addNode("/dataset2/data1/what", new Attribute("quantity", "DBZH"));
    metadata.addNode("/dataset2/data2/what", new Attribute("quantity", "TH"));

    String result = classUnderTest.createName("_beast/hexcode", metadata);
    
    assertEquals("0x3", result);
  }

  @Test
  public void testCreate_shiftRight_invalidTag() {
    Metadata metadata = createBaseMetadata();

    try{
      classUnderTest.createName("/hexdata", metadata);
      Assert.fail("Expected MetadataNameCreatorException");
    } catch (MetadataNameCreatorException e) {
      // pass
    }
  }

  
  @Test
  public void testCreate_2_shiftRight() {
    Metadata metadata = createBaseMetadata();
    metadata.addNode("/dataset1/data1/what", new Attribute("quantity", "DBZH"));
    metadata.addNode("/dataset2/data1/what", new Attribute("quantity", "DBZH"));
    metadata.addNode("/dataset2/data2/what", new Attribute("quantity", "NONAME"));

    String result = classUnderTest.createName("_beast/hexcode", metadata);
    
    assertEquals("0x1", result);
  }

  @Test
  public void testCreate_3_shiftRight() {
    Metadata metadata = createBaseMetadata();
    metadata.addNode("/dataset1/data1/what", new Attribute("quantity", "DBZH"));
    metadata.addNode("/dataset2/data1/what", new Attribute("quantity", "DBZH"));
    metadata.addNode("/dataset1/data2/what", new Attribute("quantity", "TH"));
    metadata.addNode("/dataset2/data2/what", new Attribute("quantity", "ABCD"));
    metadata.addNode("/dataset1/what", new Attribute("quantity", "LSB"));
    metadata.addNode("/dataset2/what", new Attribute("quantity", "NLSB"));
    

    String result = classUnderTest.createName("_beast/hexcode", metadata);
    
    assertEquals("0xd000000000000003", result);
  }

  @Test
  public void testCreate_1_shiftLeft() {
    classUnderTest = new QuantityHexNameCreator(createDefaultMap(), true);
    
    Metadata metadata = createBaseMetadata();
    metadata.addNode("/dataset1/data1/what", new Attribute("quantity", "DBZH"));
    metadata.addNode("/dataset2/data1/what", new Attribute("quantity", "DBZH"));
    metadata.addNode("/dataset2/data2/what", new Attribute("quantity", "TH"));

    String result = classUnderTest.createName("_beast/hexcode", metadata);
    
    assertEquals("0xc000000000000000", result);
  }

  @Test
  public void testCreate_2_shiftLeft() {
    classUnderTest = new QuantityHexNameCreator(createDefaultMap(), true);

    Metadata metadata = createBaseMetadata();
    metadata.addNode("/dataset1/data1/what", new Attribute("quantity", "DBZH"));
    metadata.addNode("/dataset2/data1/what", new Attribute("quantity", "DBZH"));
    metadata.addNode("/dataset2/data2/what", new Attribute("quantity", "NONAME"));

    String result = classUnderTest.createName("_beast/hexcode", metadata);
    
    assertEquals("0x8000000000000000", result);
  }

  @Test
  public void testConstructor_filePath() throws Exception {
    Metadata metadata = createBaseMetadata();
    metadata.addNode("/dataset1/data1/what", new Attribute("quantity", "DBZH"));
    metadata.addNode("/dataset2/data1/what", new Attribute("quantity", "DBZH"));
    metadata.addNode("/dataset1/data2/what", new Attribute("quantity", "TH"));
    metadata.addNode("/dataset2/data2/what", new Attribute("quantity", "ABCD"));
    metadata.addNode("/dataset1/what", new Attribute("quantity", "LSB"));
    metadata.addNode("/dataset2/what", new Attribute("quantity", "NLSB"));
    
    classUnderTest = new QuantityHexNameCreator(new File(getClass().getResource("quantities_fixture.xml").getFile()));

    String result = classUnderTest.createName("_beast/hexcode", metadata);
    
    assertEquals("0x3", result);
  }

  @Test
  public void testConstructor_filePath_shiftLeft() throws Exception {
    Metadata metadata = createBaseMetadata();
    metadata.addNode("/dataset1/data1/what", new Attribute("quantity", "DBZH"));
    metadata.addNode("/dataset2/data1/what", new Attribute("quantity", "DBZH"));
    metadata.addNode("/dataset1/data2/what", new Attribute("quantity", "TH"));
    metadata.addNode("/dataset2/data2/what", new Attribute("quantity", "ABCD"));
    metadata.addNode("/dataset1/what", new Attribute("quantity", "LSB"));
    metadata.addNode("/dataset2/what", new Attribute("quantity", "NLSB"));
    
    classUnderTest = new QuantityHexNameCreator(new File(getClass().getResource("quantities_fixture_shift_left.xml").getFile()));

    String result = classUnderTest.createName("_beast/hexcode", metadata);
    
    assertEquals("0xc000000000000000", result);
  }

  private Metadata createBaseMetadata() {
    Metadata metadata = new Metadata();
    metadata.addNode("/", new Group("dataset1"));
    metadata.addNode("/", new Group("dataset2"));
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

  private Map<String, Integer> createDefaultMap() {
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("LSB", 0);
    result.put("NLSB", 1);
    result.put("ABCD", 3);
    result.put("BRDR", 21);
    result.put("TH", 62);
    result.put("DBZH", 63);
    
    return result;
  }
}
