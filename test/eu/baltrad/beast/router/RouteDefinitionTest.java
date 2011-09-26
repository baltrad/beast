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

import java.util.ArrayList;
import java.util.List;

import org.easymock.MockControl;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.rules.IRule;
import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class RouteDefinitionTest extends TestCase {
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
  
  public void testSetGetAuthor() {
    RouteDefinition classUnderTest = new RouteDefinition();
    assertNull(classUnderTest.getAuthor());
    classUnderTest.setAuthor("nisse");
    assertEquals("nisse", classUnderTest.getAuthor());
  }
  
  public void testSetIsActive() {
    RouteDefinition classUnderTest = new RouteDefinition();
    assertEquals(true, classUnderTest.isActive());
    classUnderTest.setActive(false);
    assertEquals(false, classUnderTest.isActive());
  }
  
  public void testSetGetRecipients() {
    RouteDefinition classUnderTest = new RouteDefinition();
    List<String> recipients = new ArrayList<String>();
    assertNotNull(classUnderTest.getRecipients());
    classUnderTest.setRecipients(recipients);
    List<String> result = classUnderTest.getRecipients();
    assertSame(recipients, result);
  }

  public void testSetGetRecipients_null() {
    RouteDefinition classUnderTest = new RouteDefinition();
    classUnderTest.setRecipients(null);
    assertNotNull(classUnderTest.getRecipients());
  }
  
  public void testSetGetName() {
    RouteDefinition classUnderTest = new RouteDefinition();
    assertNull(classUnderTest.getName());
    classUnderTest.setName("nisse");
    assertEquals("nisse", classUnderTest.getName());
  }
  
  public void testHandle() {
    MockControl iruleControl = MockControl.createControl(IRule.class);
    IRule irule = (IRule)iruleControl.getMock();
    
    IBltMessage msg = new IBltMessage() {};
    IBltMessage resmsg = new IBltMessage() {};
    irule.handle(msg);
    iruleControl.setReturnValue(resmsg);
    RouteDefinition classUnderTest = new RouteDefinition();
    classUnderTest.setRule(irule);
    
    iruleControl.replay();
    
    // Execute test
    IBltMessage result = classUnderTest.handle(msg);
    
    // Verify
    iruleControl.verify();
    assertSame(resmsg, result);
  }

  public void testHandle_inactive() {
    MockControl iruleControl = MockControl.createControl(IRule.class);
    IRule irule = (IRule)iruleControl.getMock();
    
    IBltMessage msg = new IBltMessage() {};
    RouteDefinition classUnderTest = new RouteDefinition();
    classUnderTest.setRule(irule);
    classUnderTest.setActive(false);
    
    iruleControl.replay();
    
    // Execute test
    IBltMessage result = classUnderTest.handle(msg);
    
    // Verify
    iruleControl.verify();
    assertNull(result);
  }

  public void testHandle_nullMessage() {
    MockControl iruleControl = MockControl.createControl(IRule.class);
    IRule irule = (IRule)iruleControl.getMock();
    
    RouteDefinition classUnderTest = new RouteDefinition();
    classUnderTest.setRule(irule);
    
    iruleControl.replay();
    
    // Execute test
    try {
      classUnderTest.handle(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException npe) {
      // pass
    }
    
    // Verify
    iruleControl.verify();
  }
  
  public void testGetRuleType() throws Exception {
    MockControl iruleControl = MockControl.createControl(IRule.class);
    IRule irule = (IRule)iruleControl.getMock();
    
    irule.getType();
    iruleControl.setReturnValue("atype");
    
    RouteDefinition classUnderTest = new RouteDefinition();
    classUnderTest.setRule(irule);
    
    iruleControl.replay();

    String result = classUnderTest.getRuleType();
    
    iruleControl.verify();
    assertEquals("atype", result);
  }

  public void testGetRuleType_noRule() throws Exception {
    
    RouteDefinition classUnderTest = new RouteDefinition();
    
    String result = classUnderTest.getRuleType();
    
    assertEquals(null, result);
  }
}
