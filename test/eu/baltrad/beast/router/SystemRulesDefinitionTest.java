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
public class SystemRulesDefinitionTest extends TestCase {
  private SystemRulesDefinition classUnderTest = null;
  private MockControl r1Control = null;
  private IRule r1 = null;
  private MockControl r2Control = null;
  private IRule r2 = null;
  private MockControl r3Control = null;
  private IRule r3 = null;  
  private List<IRule> rules = null;
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    r1Control = MockControl.createControl(IRule.class);
    r1 = (IRule)r1Control.getMock();
    r2Control = MockControl.createControl(IRule.class);
    r2 = (IRule)r2Control.getMock();
    r3Control = MockControl.createControl(IRule.class);
    r3 = (IRule)r3Control.getMock();
    rules = new ArrayList<IRule>();
    rules.add(r1);
    rules.add(r2);
    rules.add(r3);
    classUnderTest = new SystemRulesDefinition();
    classUnderTest.setRules(rules);
  }
  
  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
    r1 = null;
    r1Control = null;
    r2 = null;
    r2Control = null;
    r3 = null;
    r3Control = null;
    rules = null;
    classUnderTest = null;
  }
  
  protected void replay() {
    r1Control.replay();
    r2Control.replay();
    r3Control.replay();
  }

  protected void verify() {
    r1Control.verify();
    r2Control.verify();
    r3Control.verify();
  }

  public void testConstructor() throws Exception {
    classUnderTest = new SystemRulesDefinition();
    assertEquals(0, classUnderTest.getRules().size());
  }
  
  public void testSetNullRules() throws Exception {
    assertEquals(3, classUnderTest.getRules().size());
    classUnderTest.setRules(null);
    assertEquals(0, classUnderTest.getRules().size());
  }
  
  public void testHandle() throws Exception {
    IBltMessage msg = new IBltMessage() {};
    
    r1.handle(msg);
    r1Control.setReturnValue(null);
    r2.handle(msg);
    r2Control.setReturnValue(null);
    r3.handle(msg);
    r3Control.setReturnValue(null);
    
    replay();
    
    classUnderTest.handle(msg);
    
    verify();
  }

  public void testHandle_ruleReturnsMessage() throws Exception {
    IBltMessage msg = new IBltMessage() {};
    IBltMessage resmsg = new IBltMessage() {};
    
    r1.handle(msg);
    r1Control.setReturnValue(resmsg);
    r2.handle(msg);
    r2Control.setReturnValue(resmsg);
    r3.handle(msg);
    r3Control.setReturnValue(resmsg);
    
    replay();
    
    classUnderTest.handle(msg);
    
    verify();
  }

  public void testHandle_ruleThrowsException() throws Exception {
    IBltMessage msg = new IBltMessage() {};
    
    r1.handle(msg);
    r1Control.setThrowable(new NullPointerException());
    r2.handle(msg);
    r2Control.setReturnValue(null);
    r3.handle(msg);
    r3Control.setReturnValue(null);
    
    replay();
    
    classUnderTest.handle(msg);
    
    verify();
  }
  
  public void testHandle_ruleThrowsError() throws Exception {
    IBltMessage msg = new IBltMessage() {};
    boolean failed = false;
    
    r1.handle(msg);
    r1Control.setThrowable(new Error());
    
    replay();

    try {
      classUnderTest.handle(msg);
      failed = true;
    } catch (Error e) {
      // pass
    }
    
    verify();
    assertEquals(false, failed);
  }
}
