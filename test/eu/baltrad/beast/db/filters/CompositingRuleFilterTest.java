/* --------------------------------------------------------------------
Copyright (C) 2009-2010 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.db.filters;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.db.FileQuery;
import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.expr.ExpressionFactory;
import eu.baltrad.bdb.expr.StringExpression;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.oh5.MetadataMatcher;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.beast.db.IFilter;


/**
 * @author Mats Vernersson, SMHI
 */
public class CompositingRuleFilterTest extends EasyMockSupport{
  
  private String quantity;
  private ArrayList<String> sources;
  private DateTime startDateTime;
  private DateTime stopDateTime;
  
  private static interface ICompositingRuleFilterMethods {
    public Expression getFilterExpression(boolean dbQuery);
  };
  
  @Before
  public void setUp() throws Exception {
    quantity = "DBZH";
    sources = new ArrayList<String>();
    sources.add("source1");
    startDateTime = new DateTime(2016, 4, 15, 10, 15, 0);
    stopDateTime = new DateTime(2016, 4, 15, 10, 30, 0);
  }
  
  private CompositingRuleFilter createDefaultFilter(boolean isScanBased) {
    return new CompositingRuleFilter(isScanBased, quantity, sources, startDateTime, stopDateTime, null);
  }

  @Test
  public void testConstructorScanBased() {
    testConstructor(true);
  }
  
  @Test
  public void testConstructorVolBased() {
    testConstructor(false);
  }
  
  @Test
  public void testApplyScanBased() {
    testApply(true);   
  }
  
  @Test
  public void testApplyPvolBased() {
    testApply(false);   
  }
  
  @Test
  public void testScanFileMatches() {
    testFileMatches(true, true, false);
  }
  
  @Test
  public void testScanFileNotMatches() {
    testFileMatches(true, false, false);
  }
  
  @Test
  public void testScanFileMatchAdditionalFilter() {
    testFileMatches(true, true, true);
  }
  
  @Test
  public void testPvolFileMatches() {
    testFileMatches(false, true, false);
  }
  
  @Test
  public void testPvolFileNotMatches() {
    testFileMatches(false, false, false);
  }
  
  @Test
  public void testPvolFileMatchAdditionalFilter() {
    testFileMatches(false, true, true);
  }
  
  private void testConstructor(boolean isScanBased) {
    CompositingRuleFilter filter = createDefaultFilter(isScanBased);
    
    String objectType = "PVOL";
    if (isScanBased) {
      objectType = "SCAN";
    }
    
    assertEquals(objectType, filter.getObject());
    assertEquals(quantity, filter.getQuantity());
    assertEquals(sources, filter.getSources());
    assertEquals(startDateTime, filter.getStartDateTime());
    assertEquals(stopDateTime, filter.getStopDateTime());
  }
  
  private void testApply(boolean isScanBased) {

    final ICompositingRuleFilterMethods methods = createMock(ICompositingRuleFilterMethods.class);
    
    FileQuery query = createMock(FileQuery.class);
    Expression filterExpression = createMock(Expression.class);
    
    CompositingRuleFilter classUnderTest = new CompositingRuleFilter(isScanBased, quantity, sources, startDateTime, startDateTime, null) {
      protected Expression getFilterExpression(boolean dbQuery) {
        return methods.getFilterExpression(dbQuery);
      }
    };

    expect(methods.getFilterExpression(true)).andReturn(filterExpression);
    
    Capture<Expression> capturedElangleOrderXpr = new Capture<Expression>();
    if (isScanBased) {
      query.appendOrderClause(EasyMock.capture(capturedElangleOrderXpr));      
    }
    
    Capture<Expression> capturedDateOrderXpr = new Capture<Expression>();
    query.appendOrderClause(EasyMock.capture(capturedDateOrderXpr));
    
    query.setFilter(filterExpression);

    replayAll();
    
    classUnderTest.apply(query);
    
    verifyAll();
    
    ExpressionFactory xprFactory = new ExpressionFactory();
    if (isScanBased) {
      assertEquals(xprFactory.asc(xprFactory.attribute("where/elangle")), capturedElangleOrderXpr.getValue());      
    }
    assertEquals(xprFactory.asc(xprFactory.combinedDateTime("what/date", "what/time")), capturedDateOrderXpr.getValue());

  }
  
  private void testFileMatches(boolean isScanBased, boolean fileMatching, boolean useAdditionalFilter) {
    FileEntry file = createMock(FileEntry.class);
    Metadata metadata = createMock(Metadata.class);
    MetadataMatcher matcher = createMock(MetadataMatcher.class);
    
    IFilter additionalFilter = null;
    Expression additionalFilterExpression = null;
    if (useAdditionalFilter) {
      additionalFilter = createMock(IFilter.class);
      additionalFilterExpression = new StringExpression("AdditionalFilterTest");
    }

    CompositingRuleFilter classUnderTest = new CompositingRuleFilter(isScanBased, quantity, sources, startDateTime, startDateTime, additionalFilter);
    classUnderTest.setMatcher(matcher);

    expect(file.getMetadata()).andReturn(metadata);
    
    Capture<Expression> capturedExpression = new Capture<Expression>();
    Capture<Metadata> capturedMeta = new Capture<Metadata>();
    expect(matcher.match(EasyMock.capture(capturedMeta), EasyMock.capture(capturedExpression))).andReturn(fileMatching);

    if (useAdditionalFilter) {
      expect(additionalFilter.getExpression()).andReturn(additionalFilterExpression);            
    }
    
    replayAll();
    
    boolean result = classUnderTest.fileMatches(file);
    
    verifyAll();
    
    assertEquals(result, fileMatching);
    assertEquals(capturedMeta.getValue(), metadata);
    Expression resultExpression = capturedExpression.getValue();
    assertEquals(resultExpression.getType(), Expression.Type.LIST);
    assertEquals(resultExpression.get(0).toString(), "and");
    assertEquals(resultExpression.get(1).getType(), Expression.Type.LIST);
    assertEquals(resultExpression.get(2).getType(), Expression.Type.LIST);
    
    String objectType = "PVOL";
    if (isScanBased) {
      objectType = "SCAN";
    }

    String[] whatObjectStrings = {"what/object", "string", objectType};
    assertTrue(expressionContainStrings(resultExpression, whatObjectStrings));
    String[] whatQuantityStrings = {"what/quantity", "string", "DBZH"};
    assertTrue(expressionContainStrings(resultExpression, whatQuantityStrings));
    String[] sourceStrings = {"_bdb/source_name", "string", "source1"};
    assertTrue(expressionContainStrings(resultExpression, sourceStrings));
    
    if (useAdditionalFilter) {
      String[] additionalFilterStrings = {"AdditionalFilterTest"};
      assertTrue(expressionContainStrings(resultExpression, additionalFilterStrings));
    }

  }
  
  private boolean expressionContainStrings(Expression expressionList, String[] stringsToFind) {
    int noOfConsequtiveStringsFound = searchExpressionForStrings(expressionList, stringsToFind, 0);
    return noOfConsequtiveStringsFound == stringsToFind.length;
  }
  
  private int searchExpressionForStrings(Expression expressionList, String[] stringsToFind, int currentStringIndex) {
    for (Expression expr : expressionList) {
      if (expr.getType() == Expression.Type.LIST) {
        currentStringIndex = searchExpressionForStrings(expr, stringsToFind, currentStringIndex);
      } else if (currentStringIndex > 0) {
        if (expr.toString().equals(stringsToFind[currentStringIndex])) {
          currentStringIndex++;
        } else {
          return 0;
        }
      } else {
        if (expr.toString().equals(stringsToFind[currentStringIndex])) {
          currentStringIndex++;
        }
      }
      
      if (currentStringIndex == stringsToFind.length) {
        return currentStringIndex;
      }
      
    }
    return currentStringIndex;
  }

}
