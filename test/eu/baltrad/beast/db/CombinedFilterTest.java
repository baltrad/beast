/*
Copyright 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

This file is part of Beast library.

Beast library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Beast library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Beast library.  If not, see <http://www.gnu.org/licenses/>.
*/

package eu.baltrad.beast.db;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.expr.ExpressionFactory;

public class CombinedFilterTest extends EasyMockSupport {
  private ObjectMapper jsonMapper;
  private IFilter filter1;
  private IFilter filter2;
  private ExpressionFactory xpr;
  private CombinedFilter classUnderTest;

  @Before
  public void setUp() {
    filter1 = createMock(IFilter.class);
    filter2 = createMock(IFilter.class);
    xpr = new ExpressionFactory();
    jsonMapper = new ObjectMapper();
    classUnderTest = new CombinedFilter();
  }

  @Test
  public void testGetExpression_ANY() {
    List<IFilter> children = new ArrayList<IFilter>();
    children.add(filter1);
    children.add(filter2);
    classUnderTest.setChildFilters(children);
    classUnderTest.setMatchType(CombinedFilter.MatchType.ANY);

    expect(filter1.getExpression()).andReturn(xpr.literal(1));
    expect(filter2.getExpression()).andReturn(xpr.literal(2));

    replayAll();

    Expression expected = xpr.or(xpr.literal(1), xpr.literal(2));
    Expression e = classUnderTest.getExpression();
    
    verifyAll();
    assertTrue(e.equals(expected));
  }

  @Test
  public void testGetExpression_ALL() {
    List<IFilter> children = new ArrayList<IFilter>();
    children.add(filter1);
    children.add(filter2);
    classUnderTest.setChildFilters(children);
    classUnderTest.setMatchType(CombinedFilter.MatchType.ALL);

    expect(filter1.getExpression()).andReturn(xpr.literal(1));
    expect(filter2.getExpression()).andReturn(xpr.literal(2));

    replayAll();

    Expression expected = xpr.and(xpr.literal(1), xpr.literal(2));
    Expression e = classUnderTest.getExpression();
    
    verifyAll();
    assertEquals(expected, e);
  }

  @Test
  public void testAddChildFilter() {
    classUnderTest.addChildFilter(filter1);
    assertTrue(classUnderTest.getChildFilters().contains(filter1));
  }

  @Test
  public void testIsValid() {
    List<IFilter> children = new ArrayList<IFilter>();
    children.add(filter1);
    children.add(filter2);
    classUnderTest.setChildFilters(children);
    classUnderTest.setMatchType(CombinedFilter.MatchType.ALL);

    expect(filter1.getExpression()).andReturn(xpr.literal(1));
    expect(filter2.getExpression()).andReturn(xpr.literal(2));

    replayAll();
    
    assertTrue(classUnderTest.isValid());
    
    verifyAll();
  }

  @Test
  public void testIsValid_noChild() {
    classUnderTest.setMatchType(CombinedFilter.MatchType.ALL);
    
    replayAll();
    
    assertFalse(classUnderTest.isValid());
    
    verifyAll();
  }

  @Test
  public void testIsValid_noMatchType() {
    List<IFilter> children = new ArrayList<IFilter>();
    children.add(filter1);
    children.add(filter2);
    classUnderTest.setChildFilters(children);

    expect(filter1.getExpression()).andReturn(xpr.literal(1));
    expect(filter2.getExpression()).andReturn(xpr.literal(2));

    replayAll();

    assertFalse(classUnderTest.isValid());
    
    verifyAll();
  } 

  @Test
  public void testJackson_toJson() {
    CombinedFilter child1 = new CombinedFilter();
    child1.setId(1);
    child1.setMatchType(CombinedFilter.MatchType.ALL);
    CombinedFilter child2 = new CombinedFilter();
    child2.setId(2);
    child2.setMatchType(CombinedFilter.MatchType.ANY);
    classUnderTest.setId(3);
    classUnderTest.addChildFilter(child1);
    classUnderTest.addChildFilter(child2);
    classUnderTest.setMatchType(CombinedFilter.MatchType.ALL);

    JsonNode json = jsonMapper.valueToTree(classUnderTest);
    assertEquals(4, json.size());
    assertEquals("combined", json.get("type").asText());
    assertEquals(3, json.get("id").asInt());
    assertEquals("ALL", json.get("matchType").asText());
    JsonNode children = json.get("childFilters");
    assertEquals(2, children.size());
    json = children.get(0);
    assertEquals(4, json.size());
    assertEquals("combined", json.get("type").asText());
    assertEquals(1, json.get("id").asInt());
    assertEquals("ALL", json.get("matchType").asText());
    assertEquals(0, json.get("childFilters").size());
    json = children.get(1);
    assertEquals(4, json.size());
    assertEquals("combined", json.get("type").asText());
    assertEquals(2, json.get("id").asInt());
    assertEquals("ANY", json.get("matchType").asText());
    assertEquals(0, json.get("childFilters").size());
  }

  @Test
  public void testJackson_fromJson() throws java.io.IOException {
    ObjectNode child = JsonNodeFactory.instance.objectNode();
    child.put("type", "combined");
    child.put("id", 2);
    child.put("matchType", "ALL");
    child.put("childFilters", JsonNodeFactory.instance.arrayNode());
    ObjectNode json = JsonNodeFactory.instance.objectNode();
    json.put("type", "combined");
    json.put("id", 1);
    json.put("matchType", "ANY");
    ArrayNode children = JsonNodeFactory.instance.arrayNode();
    children.add(child);
    json.put("childFilters", children);

    classUnderTest = (CombinedFilter)jsonMapper.treeToValue(json, IFilter.class);

    assertEquals(new Integer(1), classUnderTest.getId());
    assertEquals(CombinedFilter.MatchType.ANY, classUnderTest.getMatchType());
    assertEquals(1, classUnderTest.getChildFilters().size());
  }
}
