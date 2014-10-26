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

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.bdb.oh5.Attribute;
import eu.baltrad.bdb.oh5.Group;
import eu.baltrad.bdb.oh5.Metadata;

/**
 * @author Anders Henja
 *
 */
public class FormattableDateTimeNameCreatorTest extends EasyMockSupport {
  private FormattableDateTimeNameCreator classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    classUnderTest = new FormattableDateTimeNameCreator();
  }

  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void supports() {
    assertEquals(true, classUnderTest.supports("_beast/datetime"));
    assertEquals(true, classUnderTest.supports("_beast/datetime:YYY-mm-DD HH-MM-ss"));
    assertEquals(true, classUnderTest.supports("_beast/datetime:YYY-mm-DD / HH:MM:ss"));
    assertEquals(false, classUnderTest.supports("hexcode"));
    assertEquals(false, classUnderTest.supports("${klsdak}"));
    assertEquals(false, classUnderTest.supports(null));
  }

  @Test
  public void createName_1() {
    Metadata metadata = createBaseMetadata();
    
    String result = classUnderTest.createName("_beast/datetime:yyyy-MM-dd HH:mm:ss", metadata);

    assertEquals("2014-10-11 06:07:08", result);
  }

  @Test
  public void createName_2() {
    Metadata metadata = createBaseMetadata();
    
    String result = classUnderTest.createName("_beast/datetime", metadata);

    assertEquals("20141011060708", result);
  }

  @Test
  public void createName_3() {
    Metadata metadata = createBaseMetadata();
    
    String result = classUnderTest.createName("_beast/datetime:yyyy/MM/dd/HH/mm/ss", metadata);

    assertEquals("2014/10/11/06/07/08", result);
  }

  private Metadata createBaseMetadata() {
    Metadata metadata = new Metadata();
    metadata.addNode("/", new Group("dataset1"));
    metadata.addNode("/", new Group("dataset2"));
    metadata.addNode("/", new Group("what"));
    metadata.addNode("/what", new Attribute("date", "20141011"));
    metadata.addNode("/what", new Attribute("time", "060708"));
    metadata.addNode("/dataset1", new Group("data1"));
    metadata.addNode("/dataset1", new Group("data2"));
    metadata.addNode("/dataset1", new Group("what"));
    metadata.addNode("/dataset1", new Group("where"));
    metadata.addNode("/dataset1/what", new Attribute("date", "20141111"));
    metadata.addNode("/dataset1/what", new Attribute("time", "101112"));
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
