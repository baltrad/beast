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
package eu.baltrad.beast.router;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.rules.IRule;

/**
 * @author Anders Henja
 */
public class RouteDefinitionTest extends EasyMockSupport {
  @Test
  public void testSetGetRule() {
    RouteDefinition classUnderTest = new RouteDefinition();
    IRule rule = new IRule() {
      public String getType() {return null;}
      public IBltMessage handle(IBltMessage message) {return null;}
      public boolean isValid() {return true;}
    };
    
    assertNull(classUnderTest.getRule());
    classUnderTest.setRule(rule);
    IRule result = classUnderTest.getRule();
    assertSame(rule, result);
  }
  
  @Test
  public void testSetGetAuthor() {
    RouteDefinition classUnderTest = new RouteDefinition();
    assertNull(classUnderTest.getAuthor());
    classUnderTest.setAuthor("nisse");
    assertEquals("nisse", classUnderTest.getAuthor());
  }
  
  @Test
  public void testSetIsActive() {
    RouteDefinition classUnderTest = new RouteDefinition();
    assertEquals(true, classUnderTest.isActive());
    classUnderTest.setActive(false);
    assertEquals(false, classUnderTest.isActive());
  }
  
  @Test
  public void testSetGetRecipients() {
    RouteDefinition classUnderTest = new RouteDefinition();
    List<String> recipients = new ArrayList<String>();
    assertNotNull(classUnderTest.getRecipients());
    classUnderTest.setRecipients(recipients);
    List<String> result = classUnderTest.getRecipients();
    assertSame(recipients, result);
  }

  @Test
  public void testSetGetRecipients_null() {
    RouteDefinition classUnderTest = new RouteDefinition();
    classUnderTest.setRecipients(null);
    assertNotNull(classUnderTest.getRecipients());
  }
  
  @Test
  public void testSetGetName() {
    RouteDefinition classUnderTest = new RouteDefinition();
    assertNull(classUnderTest.getName());
    classUnderTest.setName("nisse");
    assertEquals("nisse", classUnderTest.getName());
  }
  
  @Test
  public void testHandle() {
    IRule irule = createMock(IRule.class);
    
    IBltMessage msg = new IBltMessage() {};
    IBltMessage resmsg = new IBltMessage() {};
    
    expect(irule.handle(msg)).andReturn(resmsg);
    RouteDefinition classUnderTest = new RouteDefinition();
    classUnderTest.setRule(irule);
    
    replayAll();
    
    // Execute test
    IBltMessage result = classUnderTest.handle(msg);
    
    // Verify
    verifyAll();
    assertSame(resmsg, result);
  }

  @Test
  public void testHandle_inactive() {
    IRule irule = createMock(IRule.class);
    
    IBltMessage msg = new IBltMessage() {};
    RouteDefinition classUnderTest = new RouteDefinition();
    classUnderTest.setRule(irule);
    classUnderTest.setActive(false);
    
    replayAll();
    
    // Execute test
    IBltMessage result = classUnderTest.handle(msg);
    
    // Verify
    verifyAll();
    assertNull(result);
  }

  @Test
  public void testHandle_nullMessage() {
    IRule irule = createMock(IRule.class);
    
    RouteDefinition classUnderTest = new RouteDefinition();
    classUnderTest.setRule(irule);
    
    replayAll();
    
    // Execute test
    try {
      classUnderTest.handle(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException npe) {
      // pass
    }
    
    // Verify
    verifyAll();
  }
  
  @Test
  public void testGetRuleType() throws Exception {
    IRule irule = createMock(IRule.class);
    
    expect(irule.getType()).andReturn("atype");
    
    RouteDefinition classUnderTest = new RouteDefinition();
    classUnderTest.setRule(irule);
    
    replayAll();

    String result = classUnderTest.getRuleType();
    
    verifyAll();
    assertEquals("atype", result);
  }

  @Test
  public void testGetRuleType_noRule() throws Exception {
    
    RouteDefinition classUnderTest = new RouteDefinition();
    
    String result = classUnderTest.getRuleType();
    
    assertEquals(null, result);
  }
}
