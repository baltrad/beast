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

import java.util.Set;

import org.springframework.util.StringUtils;

import eu.baltrad.fc.Variant;
import eu.baltrad.fc.expr.Attribute;
import eu.baltrad.fc.expr.BinaryOperator;
import eu.baltrad.fc.expr.Expression;
import eu.baltrad.fc.expr.ExpressionList;
import eu.baltrad.fc.expr.Literal;


/**
 * Filter based on an attribute value
 */
public class AttributeFilter implements IFilter {
  private static final String TYPE = "attr";

  private Integer id;
  private String attr;
  private Operator op;
  private ValueType valueType;
  private String value;
  
  /**
   * value type
   */
  public enum ValueType {
    STRING() {
      @Override
      public Variant parseString(String str) {
        return new Variant(str);
      }
    },
    LONG() {
      @Override
      public Variant parseString(String str) {
        return new Variant(Long.parseLong(str));
      }
    },
    DOUBLE() {
      @Override
      public Variant parseString(String str) {
        return new Variant(Double.parseDouble(str));
      }
    },
    BOOL() {
      @Override
      public Variant parseString(String str) {
        return new Variant(Boolean.parseBoolean(str));
      }
    };
    
    /**
     * parse BDB Variant from string
     */
    public abstract Variant parseString(String str);
    
    /**
     * convert to BDB Attribute expression type
     */
    public Attribute.Type toBdbAttributeType() {
      return Attribute.Type.valueOf(this.toString());
    }
  }
  
  /**
   * operator type
   */
  public enum Operator {
    EQ,
    NE,
    LT,
    LE,
    GT,
    GE,
    IN() {
      @Override
      public boolean isMultiValued() { return true; }
    },
    NOT_IN() {
      @Override
      public boolean isMultiValued() { return true; }
    };
  
    /**
     * return true if this operator can handle a list of values
     */
    public boolean isMultiValued() { return false; }
    
    /**
     * convert to BDB BinaryOperator expression type
     */
    public BinaryOperator.Type toBdbBinaryOperatorType() {
      return BinaryOperator.Type.valueOf(this.toString());
    }
  }
  
  /**
   * @see IFilter#getType()
   */
  @Override
  public String getType() { return TYPE; }

  /**
   * @see IFilter#getId()
   */
  @Override
  public Integer getId() { return id; }

  /**
   * @see IFilter#setId()
   */
  @Override
  public void setId(Integer id) { this.id = id; }

  /**
   * @see IFilter#getExpression()
   */
  @Override
  public Expression getExpression() {
    Expression attrExpr = new Attribute(attr, valueType.toBdbAttributeType());
    Expression valueExpr = getValueExpression();
    return new BinaryOperator(op.toBdbBinaryOperatorType(),
                              attrExpr,
                              valueExpr);
  }

  public String getAttribute() { return attr; }
  public void setAttribute(String attr) { this.attr = attr; }

  public Operator getOperator() { return op; }
  public void setOperator(Operator op) { this.op = op; }

  public ValueType getValueType() { return valueType; }
  public void setValueType(ValueType type) { this.valueType = type; }

  public String getValue() { return value; }
  public void setValue(String value) { this.value = value; }
  
  protected Expression getValueExpression() {
    if (op.isMultiValued()) {
      ExpressionList exprList = new ExpressionList();
      String[] values = StringUtils.commaDelimitedListToStringArray(value);
      for (int i = 0; i < values.length; i++) {
        System.out.println(values[i]);
        exprList.append(new Literal(valueType.parseString(values[i].trim())));
      }
      return exprList;
    } else {
      return new Literal(valueType.parseString(value));
    }
  }
}
