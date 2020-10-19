/**
 * 
 */
package eu.baltrad.beast.rules.namer;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.bdb.oh5.Attribute;
import eu.baltrad.bdb.oh5.Group;
import eu.baltrad.bdb.oh5.Metadata;

/**
 * @author anders
 *
 */
public class DoubleToStringNameCreatorTest extends EasyMockSupport {
  private DoubleToStringNameCreator classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    Map<Double, String> mapping = new HashMap<Double, String>();
    mapping.put(0.5, "A");
    mapping.put(1.0, "B");
    mapping.put(1.5, "C");
    mapping.put(2.5, "E");
    classUnderTest = new DoubleToStringNameCreator();
    classUnderTest.setAttributePattern("_beast/elanglenamer(:/dataset[0-9]+/where/elangle)?");
    classUnderTest.setDefaultAttribute("/dataset1/where/elangle");
    classUnderTest.setDefaultValue("UNKNOWN");
    classUnderTest.setMapping(mapping);
    classUnderTest.afterPropertiesSet();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void testSupports() {
    assertEquals(true, classUnderTest.supports("_beast/elanglenamer"));
    assertEquals(true, classUnderTest.supports("_beast/elanglenamer:/dataset1/where/elangle"));
    assertEquals(true, classUnderTest.supports("_beast/elanglenamer:/dataset10/where/elangle"));
    assertEquals(false, classUnderTest.supports("_beast/elanglenamers"));
    assertEquals(false, classUnderTest.supports("_beast/elanglenamer:/where/elangle"));
    assertEquals(false, classUnderTest.supports("_beast/elanglenamer:/dataset10/data1/where/elangle"));
  }
  
  @Test
  public void createName_1() {
    assertEquals("A", classUnderTest.createName("_beast/elanglenamer", createBaseMeta(0.5, 2.5)));
  }
  
  @Test
  public void createName_2() {
    assertEquals("A", classUnderTest.createName("_beast/elanglenamer:/dataset1/where/elangle", createBaseMeta(0.5, 2.5)));
  }

  @Test
  public void createName_3() {
    assertEquals("E", classUnderTest.createName("_beast/elanglenamer:/dataset2/where/elangle", createBaseMeta(0.5, 2.5)));
  }

  @Test
  public void createName_4() {
    assertEquals("UNKNOWN", classUnderTest.createName("_beast/elanglenamer:/dataset3/where/elangle", createBaseMeta(0.5, 2.5)));
  }

  @Test
  public void createName_5() {
    assertEquals("UNKNOWN", classUnderTest.createName("_beast/elanglenamer:/dataset4/where/elangle", createBaseMeta(0.5, 2.5)));
  }
  
  @Test
  public void createName_6_with_other_mapping() throws Exception {
    Map<Double, String> mapping = new HashMap<Double, String>();
    mapping.put(0.5, "ARG");
    mapping.put(2.5, "Y");
    mapping.put(3.5, "X");
    classUnderTest = new DoubleToStringNameCreator();
    classUnderTest.setAttributePattern("_beast/elanglenamer(:/dataset[0-9]+/where/elangle)?");
    classUnderTest.setDefaultAttribute("/dataset1/where/elangle");
    classUnderTest.setDefaultValue("UNKNOWN");
    classUnderTest.setMapping(mapping);
    classUnderTest.afterPropertiesSet();
    
    assertEquals("ARG", classUnderTest.createName("_beast/elanglenamer", createBaseMeta(0.5, 2.5)));
    assertEquals("Y", classUnderTest.createName("_beast/elanglenamer:/dataset2/where/elangle", createBaseMeta(0.5, 2.5)));
    assertEquals("UNKNOWN", classUnderTest.createName("_beast/elanglenamer:/dataset3/where/elangle", createBaseMeta(0.5, 4.5)));
  }
  
  protected Metadata createBaseMeta(double elangle1, double elangle2) {
    Metadata metadata = new Metadata();
    metadata.addNode("/", new Group("dataset1"));
    metadata.addNode("/", new Group("dataset2"));
    metadata.addNode("/", new Group("what"));
    metadata.addNode("/what", new Attribute("date", "20141011"));
    metadata.addNode("/what", new Attribute("time", "060708"));
    metadata.addNode("/dataset1", new Group("where"));
    metadata.addNode("/dataset1", new Group("data1"));
    metadata.addNode("/dataset1/data1", new Group("where"));
    metadata.addNode("/dataset1/where",  new Attribute("elangle", elangle1));
    metadata.addNode("/dataset2", new Group("where"));
    metadata.addNode("/dataset2/where",  new Attribute("elangle", elangle2));
    return metadata;
  }
}
