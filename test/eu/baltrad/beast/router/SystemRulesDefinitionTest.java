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

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.rules.IRule;

/**
 * @author Anders Henja
 */
public class SystemRulesDefinitionTest extends EasyMockSupport {
  private SystemRulesDefinition classUnderTest = null;
  private IRule r1 = null;
  private IRule r2 = null;
  private IRule r3 = null;  
  private List<IRule> rules = null;

  @Before
  public void setUp() throws Exception {
    r1 = createMock(IRule.class);
    r2 = createMock(IRule.class);
    r3 = createMock(IRule.class);
    rules = new ArrayList<IRule>();
    rules.add(r1);
    rules.add(r2);
    rules.add(r3);
    classUnderTest = new SystemRulesDefinition();
    classUnderTest.setRules(rules);
  }

  @After
  public void tearDown() throws Exception {
    r1 = null;
    r2 = null;
    r3 = null;
    rules = null;
    classUnderTest = null;
  }

  @Test
  public void testConstructor() throws Exception {
    classUnderTest = new SystemRulesDefinition();
    assertEquals(0, classUnderTest.getRules().size());
  }

  @Test
  public void testSetNullRules() throws Exception {
    assertEquals(3, classUnderTest.getRules().size());
    classUnderTest.setRules(null);
    assertEquals(0, classUnderTest.getRules().size());
  }
  
  @Test
  public void testHandle() throws Exception {
    IBltMessage msg = new IBltMessage() {};
    
    expect(r1.handle(msg)).andReturn(null);
    expect(r2.handle(msg)).andReturn(null);
    expect(r3.handle(msg)).andReturn(null);
    
    replayAll();
    
    classUnderTest.handle(msg);
    
    verifyAll();
  }

  @Test
  public void testHandle_ruleReturnsMessage() throws Exception {
    IBltMessage msg = new IBltMessage() {};
    IBltMessage resmsg = new IBltMessage() {};
    
    expect(r1.handle(msg)).andReturn(resmsg);
    expect(r2.handle(msg)).andReturn(resmsg);
    expect(r3.handle(msg)).andReturn(resmsg);
    
    replayAll();
    
    classUnderTest.handle(msg);
    
    verifyAll();
  }

  @Test
  public void testHandle_ruleThrowsException() throws Exception {
    IBltMessage msg = new IBltMessage() {};
    
    expect(r1.handle(msg)).andThrow(new NullPointerException());
    expect(r2.handle(msg)).andReturn(null);
    expect(r3.handle(msg)).andReturn(null);
    
    replayAll();
    
    classUnderTest.handle(msg);
    
    verifyAll();
  }
  
  @Test
  public void testHandle_ruleThrowsError() throws Exception {
    IBltMessage msg = new IBltMessage() {};
    boolean failed = false;
    
    expect(r1.handle(msg)).andThrow(new Error());
    
    replayAll();

    try {
      classUnderTest.handle(msg);
      failed = true;
    } catch (Error e) {
      // pass
    }
    
    verifyAll();
    assertEquals(false, failed);
  }
}
