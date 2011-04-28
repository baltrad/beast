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

import org.springframework.util.StringUtils;

import eu.baltrad.fc.expr.Expression;
import eu.baltrad.fc.expr.ExpressionFactory;


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

  private ExpressionFactory xpr;
  
  /**
   * Default constructor.
   */
  public AttributeFilter() {
    xpr = new ExpressionFactory();
  }

  /**
   * value type
   */
  public enum ValueType {
    STRING() {
      @Override
      public Expression parseString(String str) {
        return new Expression(str);
      }
    },
    LONG() {
      @Override
      public Expression parseString(String str) {
        return new Expression(Long.parseLong(str));
      }

      @Override
      public String toBdbAttributeType() {
        return "int64";
      }
    },
    DOUBLE() {
      @Override
      public Expression parseString(String str) {
        return new Expression(Double.parseDouble(str));
      }
    },
    BOOL() {
      @Override
      public Expression parseString(String str) {
        return new Expression(Boolean.parseBoolean(str));
      }
    };
    
    /**
     * parse BDB Expression from string
     */
    public abstract Expression parseString(String str);
    
    /**
     * convert to BDB Attribute expression type
     */
    public String toBdbAttributeType() {
      return this.toString().toLowerCase();
    }
  }
  
  /**
   * operator type
   */
  public enum Operator {
    EQ() {
      @Override public String toBdbBinaryOperatorType() { return "="; }
    },
    NE() {
      @Override public String toBdbBinaryOperatorType() { return "!="; }
    },
    LT() {
      @Override public String toBdbBinaryOperatorType() { return "<"; }
    },
    LE() {
      @Override public String toBdbBinaryOperatorType() { return "<="; }
    },
    GT() {
      @Override public String toBdbBinaryOperatorType() { return ">"; }
    },
    GE() {
      @Override public String toBdbBinaryOperatorType() { return ">="; }
    },
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
    public String toBdbBinaryOperatorType() {
      return this.toString().toLowerCase();
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
    Expression attrExpr = xpr.attribute(attr, valueType.toBdbAttributeType());
    Expression valueExpr = getValueExpression();
    return xpr.binary_op(op.toBdbBinaryOperatorType(), attrExpr, valueExpr);
  }

  /**
   * @see IFilter#isValid()
   */
  @Override
  public boolean isValid() {
    try {
      getExpression();
    } catch (Exception e) {
      return false;
    }
    return true;
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
      Expression exprList = new Expression();
      String[] values = StringUtils.commaDelimitedListToStringArray(value);
      if (values.length == 0)
        throw new RuntimeException("no value associated with AttributeFilter");
      for (int i = 0; i < values.length; i++) {
        exprList.push_back(valueType.parseString(values[i].trim()));
      }
      return exprList;
    } else {
      return valueType.parseString(value);
    }
  }
}
