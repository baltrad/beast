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

import junit.framework.TestCase;

import eu.baltrad.fc.expr.Expression;
import eu.baltrad.fc.expr.ExpressionFactory;
import eu.baltrad.fc.expr.ExpressionList;
import eu.baltrad.fc.expr.Literal;

public class AttributeFilterTest extends TestCase {
  private AttributeFilter classUnderTest;
  private ExpressionFactory xpr;

  public void setUp() {
    xpr = new ExpressionFactory();
    classUnderTest = new AttributeFilter();
  }

  public void testGetExpression_singleValuedDouble() {
    Expression expected = xpr.attribute("where/elangle").lt(xpr.double_(7.5));
    classUnderTest.setAttribute("where/elangle");
    classUnderTest.setOperator(AttributeFilter.Operator.LT);
    classUnderTest.setValueType(AttributeFilter.ValueType.DOUBLE);
    classUnderTest.setValue("7.5");
    assertTrue(classUnderTest.getExpression().equals(expected));
  }

  public void testGetExpression_multiValuedString() {
    Expression expected = xpr.attribute("what/object").in(new Expression[]{xpr.string("PVOL"), xpr.string("SCAN")});
    classUnderTest.setAttribute("what/object");
    classUnderTest.setOperator(AttributeFilter.Operator.IN);
    classUnderTest.setValueType(AttributeFilter.ValueType.STRING);
    classUnderTest.setValue("PVOL, SCAN");
    assertTrue(classUnderTest.getExpression().equals(expected));
  }

  public void testGetValueExpression_multiValuedString() {
    Expression expected = new ExpressionList(
                            new Expression[]{
                              xpr.string("PVOL"),
                              xpr.string("SCAN")
                            });

    classUnderTest.setOperator(AttributeFilter.Operator.IN);
    classUnderTest.setValueType(AttributeFilter.ValueType.STRING);
    classUnderTest.setValue("PVOL, SCAN");

    assertTrue(classUnderTest.getValueExpression().equals(expected));
  }

  public void testIsValid() {
    classUnderTest.setAttribute("where/elangle");
    classUnderTest.setOperator(AttributeFilter.Operator.IN);
    classUnderTest.setValueType(AttributeFilter.ValueType.STRING);
    classUnderTest.setValue("PVOL, SCAN");

    assertTrue(classUnderTest.isValid());
  }

  public void testIsValid_noAttribute() {
    classUnderTest.setOperator(AttributeFilter.Operator.IN);
    classUnderTest.setValueType(AttributeFilter.ValueType.STRING);
    classUnderTest.setValue("PVOL, SCAN");

    assertFalse(classUnderTest.isValid());
  }

  public void testIsValid_noOperator() {
    classUnderTest.setAttribute("where/elangle");
    classUnderTest.setValueType(AttributeFilter.ValueType.STRING);
    classUnderTest.setValue("PVOL, SCAN");

    assertFalse(classUnderTest.isValid());
  }

  public void testIsValid_noValueType() {
    classUnderTest.setAttribute("where/elangle");
    classUnderTest.setOperator(AttributeFilter.Operator.IN);
    classUnderTest.setValue("PVOL, SCAN");

    assertFalse(classUnderTest.isValid());
  }

  public void testIsValid_noValue() {
    classUnderTest.setAttribute("where/elangle");
    classUnderTest.setOperator(AttributeFilter.Operator.IN);
    classUnderTest.setValueType(AttributeFilter.ValueType.STRING);

    assertFalse(classUnderTest.isValid());
  }
  
  public void testIsValid_invalidValue() {
    classUnderTest.setAttribute("where/elangle");
    classUnderTest.setOperator(AttributeFilter.Operator.IN);
    classUnderTest.setValueType(AttributeFilter.ValueType.DOUBLE);
    classUnderTest.setValue("PVOL, SCAN");

    assertFalse(classUnderTest.isValid());
  }
}
