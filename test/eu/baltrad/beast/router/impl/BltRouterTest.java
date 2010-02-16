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
package eu.baltrad.beast.router.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.router.Route;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.RuleException;


/**
 * @author Anders Henja
 */
public class BltRouterTest extends TestCase {
  public void testGetRoutes() throws Exception {
    MockControl ruleControl = MockControl.createControl(IRule.class);
    IRule rule = (IRule)ruleControl.getMock();
    MockControl rule2Control = MockControl.createControl(IRule.class);
    IRule rule2 = (IRule)rule2Control.getMock();
    MockControl rule3Control = MockControl.createControl(IRule.class);
    IRule rule3 = (IRule)rule3Control.getMock();
    
    IBltMessage message = new IBltMessage() { };
    IBltMessage rule2message = new IBltMessage() { };
    rule.handle(message);
    ruleControl.setReturnValue(null);
    rule2.handle(message);
    rule2Control.setReturnValue(rule2message);
    rule3.handle(message);
    rule3Control.setReturnValue(null);
    
    List<RouteDefinition> defs = new ArrayList<RouteDefinition>();
    RouteDefinition d1 = new RouteDefinition();
    d1.setRule(rule);
    RouteDefinition d2 = new RouteDefinition();
    d2.setRule(rule2);
    List<String> d2recipients = new ArrayList<String>();
    d2recipients.add("Adaptor1");
    d2.setRecipients(d2recipients);
    RouteDefinition d3 = new RouteDefinition();
    d3.setRule(rule3);
    defs.add(d1);
    defs.add(d2);
    defs.add(d3);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(defs);

    ruleControl.replay();
    rule2Control.replay();
    rule3Control.replay();
    
    // Execute test
    List<Route> result = classUnderTest.getRoutes(message);
    
    // Verify result
    ruleControl.verify();
    rule2Control.verify();
    rule3Control.verify();
    assertEquals(1, result.size());
    Route routeResult = result.get(0);
    assertEquals("Adaptor1", routeResult.getDestination());
    assertSame(rule2message, routeResult.getMessage());
  }
  
  public void testDeleteDefinition() {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    
    jdbc.update(null, new Object[]{});
    jdbcControl.setMatcher(MockControl.ALWAYS_MATCHER);
    jdbcControl.setReturnValue(0);

    jdbc.update(null, new Object[]{});
    jdbcControl.setMatcher(MockControl.ALWAYS_MATCHER);
    jdbcControl.setReturnValue(0);

    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setJdbcTemplate(jdbc);
    classUnderTest.setDefinitions(new ArrayList<RouteDefinition>());
    jdbcControl.replay();

    // execute test
    classUnderTest.deleteDefinition("X");
    
    // verify
    jdbcControl.verify();
  }
  
  public void testDeleteDefinition_throwsException() {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    
    jdbc.update(null, new Object[]{});
    jdbcControl.setMatcher(MockControl.ALWAYS_MATCHER);
    jdbcControl.setReturnValue(0);

    jdbc.update(null, new Object[]{});
    jdbcControl.setMatcher(MockControl.ALWAYS_MATCHER);
    jdbcControl.setThrowable(new RuntimeException());

    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setJdbcTemplate(jdbc);
    jdbcControl.replay();

    // execute test
    try {
      classUnderTest.deleteDefinition("X");
      fail("Expected RuleException");
    } catch (RuleException e) {
      // pass
    }
    
    // verify
    jdbcControl.verify();
  }  
  
  public void testGetDefinitions() throws Exception {
    List<RouteDefinition> defs = new ArrayList<RouteDefinition>();
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(defs);
    
    assertSame(defs, classUnderTest.getDefinitions());
  }
  
  public void testGetDefinition() throws Exception {
    List<RouteDefinition> defs = new ArrayList<RouteDefinition>();
    RouteDefinition d1 = new RouteDefinition();
    d1.setName("D1");
    RouteDefinition d2 = new RouteDefinition();
    d2.setName("D2");
    defs.add(d1);
    defs.add(d2);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setDefinitions(defs);
    
    RouteDefinition result = classUnderTest.getDefinition("D2");
    assertSame(d2, result);
  }
  
  
  
  public void testStoreRecipients() throws Exception {
    MockControl jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    SimpleJdbcOperations jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    
    List<String> recipients = new ArrayList<String>();
    recipients.add("X1");
    recipients.add("X2");
    
    jdbc.update(null, new Object[]{});
    jdbcControl.setMatcher(MockControl.ALWAYS_MATCHER);
    jdbcControl.setReturnValue(0);
    jdbc.update(null, new Object[]{});
    jdbcControl.setMatcher(MockControl.ALWAYS_MATCHER);
    jdbcControl.setReturnValue(0);
    
    BltRouter classUnderTest = new BltRouter();
    classUnderTest.setJdbcTemplate(jdbc);

    jdbcControl.replay();
    
    // execute
    classUnderTest.storeRecipients("D1", recipients);
    
    // verify
    jdbcControl.verify();
  }
}
