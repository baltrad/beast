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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.JsonNodeFactory;

import junit.framework.TestCase;

import org.easymock.MockControl;

import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.expr.ExpressionFactory;

public class CombinedFilterTest extends TestCase {
  private ObjectMapper jsonMapper;
  private MockControl filter1Control;
  private IFilter filter1;
  private MockControl filter2Control;
  private IFilter filter2;
  private ExpressionFactory xpr;
  private CombinedFilter classUnderTest;

  public void setUp() {
    filter1Control = MockControl.createControl(IFilter.class);
    filter1 = (IFilter)filter1Control.getMock();
    filter2Control = MockControl.createControl(IFilter.class);
    filter2 = (IFilter)filter2Control.getMock();
    xpr = new ExpressionFactory();
    jsonMapper = new ObjectMapper();
    classUnderTest = new CombinedFilter();
  }

  private void replay() {
    filter1Control.replay();
    filter2Control.replay();
  }
  
  private void verify() {
    filter1Control.verify();
    filter2Control.verify();
  }

  public void testGetExpression_ANY() {
    List<IFilter> children = new ArrayList<IFilter>();
    children.add(filter1);
    children.add(filter2);
    classUnderTest.setChildFilters(children);
    classUnderTest.setMatchType(CombinedFilter.MatchType.ANY);

    filter1.getExpression();
    filter1Control.setReturnValue(xpr.literal(1));
    filter2.getExpression();
    filter2Control.setReturnValue(xpr.literal(2));
    replay();

    Expression expected = xpr.or(xpr.literal(1), xpr.literal(2));
    Expression e = classUnderTest.getExpression();
    verify();
    assertTrue(e.equals(expected));
  }

  public void testGetExpression_ALL() {
    List<IFilter> children = new ArrayList<IFilter>();
    children.add(filter1);
    children.add(filter2);
    classUnderTest.setChildFilters(children);
    classUnderTest.setMatchType(CombinedFilter.MatchType.ALL);

    filter1.getExpression();
    filter1Control.setReturnValue(xpr.literal(1));
    filter2.getExpression();
    filter2Control.setReturnValue(xpr.literal(2));
    replay();

    Expression expected = xpr.and(xpr.literal(1), xpr.literal(2));
    Expression e = classUnderTest.getExpression();
    verify();
    assertEquals(expected, e);
  }

  public void testAddChildFilter() {
    classUnderTest.addChildFilter(filter1);
    assertTrue(classUnderTest.getChildFilters().contains(filter1));
  }

  public void testIsValid() {
    List<IFilter> children = new ArrayList<IFilter>();
    children.add(filter1);
    children.add(filter2);
    classUnderTest.setChildFilters(children);
    classUnderTest.setMatchType(CombinedFilter.MatchType.ALL);

    filter1.getExpression();
    filter1Control.setReturnValue(xpr.literal(1));
    filter2.getExpression();
    filter2Control.setReturnValue(xpr.literal(2));
    replay();
    
    assertTrue(classUnderTest.isValid());
  }

  public void testIsValid_noChild() {
    classUnderTest.setMatchType(CombinedFilter.MatchType.ALL);

    assertFalse(classUnderTest.isValid()); 
  }

  public void testIsValid_noMatchType() {
    List<IFilter> children = new ArrayList<IFilter>();
    children.add(filter1);
    children.add(filter2);
    classUnderTest.setChildFilters(children);

    filter1.getExpression();
    filter1Control.setReturnValue(xpr.literal(1), MockControl.ZERO_OR_MORE);
    filter2.getExpression();
    filter2Control.setReturnValue(xpr.literal(2), MockControl.ZERO_OR_MORE);
    replay();

    assertFalse(classUnderTest.isValid());
  } 

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
    assertEquals("combined", json.get("type").getValueAsText());
    assertEquals(3, json.get("id").getValueAsInt());
    assertEquals("ALL", json.get("matchType").getValueAsText());
    JsonNode children = json.get("childFilters");
    assertEquals(2, children.size());
    json = children.get(0);
    assertEquals(4, json.size());
    assertEquals("combined", json.get("type").getValueAsText());
    assertEquals(1, json.get("id").getValueAsInt());
    assertEquals("ALL", json.get("matchType").getValueAsText());
    assertEquals(0, json.get("childFilters").size());
    json = children.get(1);
    assertEquals(4, json.size());
    assertEquals("combined", json.get("type").getValueAsText());
    assertEquals(2, json.get("id").getValueAsInt());
    assertEquals("ANY", json.get("matchType").getValueAsText());
    assertEquals(0, json.get("childFilters").size());
  }

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
