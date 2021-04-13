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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.expr.ExpressionFactory;

public class AttributeFilterTest {
  private AttributeFilter classUnderTest;
  private ExpressionFactory xpr;
  private ObjectMapper jsonMapper;

  @Before
  public void setUp() {
    xpr = new ExpressionFactory();
    jsonMapper = new ObjectMapper();
    classUnderTest = new AttributeFilter();
  }

  @After
  public void tearDown() {
    xpr = null;
    jsonMapper = null;
    classUnderTest = null;
  }
  
  @Test
  public void testGetExpression_singleValuedDouble() {
    Expression expected = xpr.lt(
      xpr.attribute("where/elangle"),
      xpr.literal(7.5)
    );
    classUnderTest.setAttribute("where/elangle");
    classUnderTest.setOperator(AttributeFilter.Operator.LT);
    classUnderTest.setValueType(AttributeFilter.ValueType.DOUBLE);
    classUnderTest.setValue("7.5");
    assertEquals(expected, classUnderTest.getExpression());
  }

  @Test
  public void testGetExpression_multiValuedString() {
    Expression expected = xpr.in(
      xpr.attribute("what/object"),
      xpr.list(xpr.literal("PVOL"), xpr.literal("SCAN"))
    );
    classUnderTest.setAttribute("what/object");
    classUnderTest.setOperator(AttributeFilter.Operator.IN);
    classUnderTest.setValueType(AttributeFilter.ValueType.STRING);
    classUnderTest.setValue("PVOL, SCAN");
    assertEquals(expected, classUnderTest.getExpression());
  }

  @Test
  public void testGetExpression_negated() {
    Expression expected = xpr.not(
      xpr.eq(
        xpr.attribute("what/object"),
        xpr.literal("PVOL")
      )
    );
    classUnderTest.setAttribute("what/object");
    classUnderTest.setOperator(AttributeFilter.Operator.EQ);
    classUnderTest.setValueType(AttributeFilter.ValueType.STRING);
    classUnderTest.setValue("PVOL");
    classUnderTest.setNegated(true);
    assertTrue(classUnderTest.getExpression().equals(expected));
  }

  @Test
  public void testGetValueExpression_multiValuedString() {
    Expression expected = xpr.list(xpr.literal("PVOL"), xpr.literal("SCAN"));

    classUnderTest.setOperator(AttributeFilter.Operator.IN);
    classUnderTest.setValueType(AttributeFilter.ValueType.STRING);
    classUnderTest.setValue("PVOL, SCAN");

    assertEquals(expected, classUnderTest.getValueExpression());
  }

  @Test
  public void testIsValid() {
    classUnderTest.setAttribute("where/elangle");
    classUnderTest.setOperator(AttributeFilter.Operator.IN);
    classUnderTest.setValueType(AttributeFilter.ValueType.STRING);
    classUnderTest.setValue("PVOL, SCAN");

    assertTrue(classUnderTest.isValid());
  }

  @Test
  public void testIsValid_noAttribute() {
    classUnderTest.setOperator(AttributeFilter.Operator.IN);
    classUnderTest.setValueType(AttributeFilter.ValueType.STRING);
    classUnderTest.setValue("PVOL, SCAN");

    assertFalse(classUnderTest.isValid());
  }

  @Test
  public void testIsValid_noOperator() {
    classUnderTest.setAttribute("where/elangle");
    classUnderTest.setValueType(AttributeFilter.ValueType.STRING);
    classUnderTest.setValue("PVOL, SCAN");

    assertFalse(classUnderTest.isValid());
  }

  @Test
  public void testIsValid_noValueType() {
    classUnderTest.setAttribute("where/elangle");
    classUnderTest.setOperator(AttributeFilter.Operator.IN);
    classUnderTest.setValue("PVOL, SCAN");

    assertFalse(classUnderTest.isValid());
  }

  @Test
  public void testIsValid_noValue() {
    classUnderTest.setAttribute("where/elangle");
    classUnderTest.setOperator(AttributeFilter.Operator.IN);
    classUnderTest.setValueType(AttributeFilter.ValueType.STRING);

    assertFalse(classUnderTest.isValid());
  }
  
  @Test
  public void testIsValid_invalidValue() {
    classUnderTest.setAttribute("where/elangle");
    classUnderTest.setOperator(AttributeFilter.Operator.IN);
    classUnderTest.setValueType(AttributeFilter.ValueType.DOUBLE);
    classUnderTest.setValue("PVOL, SCAN");

    assertFalse(classUnderTest.isValid());
  }

  @Test
  public void testJackson_toJson() {
    classUnderTest.setId(5);
    classUnderTest.setAttribute("where/object");
    classUnderTest.setOperator(AttributeFilter.Operator.IN);
    classUnderTest.setValueType(AttributeFilter.ValueType.STRING);
    classUnderTest.setValue("PVOL, SCAN");
    
    JsonNode json = jsonMapper.valueToTree(classUnderTest);
    
    assertEquals(7, json.size());
    assertEquals("attr", json.get("type").asText());
    assertEquals(5, json.get("id").asInt());
    assertEquals("where/object", json.get("attribute").asText());
    assertEquals("IN", json.get("operator").asText());
    assertEquals("STRING", json.get("valueType").asText());
    assertEquals("PVOL, SCAN", json.get("value").asText());
    assertFalse(json.get("negated").asBoolean());
    assertFalse(json.has("xpr"));
  }

  @Test
  public void testJackson_fromJson() throws java.io.IOException {
    ObjectNode json = JsonNodeFactory.instance.objectNode();
    json.put("type", "attr");
    json.put("id", 5);
    json.put("attribute", "where/object");
    json.put("operator", "IN");
    json.put("valueType", "STRING");
    json.put("value", "PVOL, SCAN");
    json.put("negated", true);
    
    classUnderTest = (AttributeFilter)jsonMapper.treeToValue(json, IFilter.class);

    assertEquals(new Integer(5), classUnderTest.getId());
    assertEquals("where/object", classUnderTest.getAttribute());
    assertEquals(AttributeFilter.Operator.IN, classUnderTest.getOperator());
    assertEquals(AttributeFilter.ValueType.STRING, classUnderTest.getValueType());
    assertEquals("PVOL, SCAN", classUnderTest.getValue());
    assertTrue(classUnderTest.isNegated());
  }
  
  @Test
  public void testGetExpression_like_string() {
    Expression expected = xpr.like(
      xpr.attribute("how/task"), xpr.literal("*_hdr")
    );
    classUnderTest.setAttribute("how/task");
    classUnderTest.setOperator(AttributeFilter.Operator.LIKE);
    classUnderTest.setValueType(AttributeFilter.ValueType.STRING);
    classUnderTest.setValue("*_hdr");
    assertEquals(expected, classUnderTest.getExpression());
  }
}
