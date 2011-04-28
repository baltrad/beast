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

import junit.framework.TestCase;

import org.easymock.MockControl;

import eu.baltrad.fc.expr.Expression;
import eu.baltrad.fc.expr.ExpressionFactory;

public class CombinedFilterTest extends TestCase {
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
    filter1Control.setReturnValue(xpr.long_(1));
    filter2.getExpression();
    filter2Control.setReturnValue(xpr.long_(2));
    replay();

    Expression expected = xpr.or_(xpr.long_(1), xpr.long_(2));
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
    filter1Control.setReturnValue(xpr.long_(1));
    filter2.getExpression();
    filter2Control.setReturnValue(xpr.long_(2));
    replay();

    Expression expected = xpr.and_(xpr.long_(1), xpr.long_(2));
    Expression e = classUnderTest.getExpression();
    verify();
    assertTrue(e.equals(expected));
  }

  public void testIsValid() {
    List<IFilter> children = new ArrayList<IFilter>();
    children.add(filter1);
    children.add(filter2);
    classUnderTest.setChildFilters(children);
    classUnderTest.setMatchType(CombinedFilter.MatchType.ALL);

    filter1.getExpression();
    filter1Control.setReturnValue(xpr.long_(1));
    filter2.getExpression();
    filter2Control.setReturnValue(xpr.long_(2));
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
    filter1Control.setReturnValue(xpr.long_(1), MockControl.ZERO_OR_MORE);
    filter2.getExpression();
    filter2Control.setReturnValue(xpr.long_(2), MockControl.ZERO_OR_MORE);
    replay();

    assertFalse(classUnderTest.isValid());
  } 
}
