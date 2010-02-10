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
package eu.baltrad.beast.rules;

import java.util.ArrayList;
import java.util.List;

import org.easymock.MockControl;

import eu.baltrad.beast.message.IBltMessage;

import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class RuleFactoryTest extends TestCase {
  public void testCreate() throws Exception {
    // Setup
    MockControl creator1Control = MockControl.createControl(IRuleCreator.class);
    IRuleCreator creator1 = (IRuleCreator)creator1Control.getMock();
    MockControl creator2Control = MockControl.createControl(IRuleCreator.class);
    IRuleCreator creator2 = (IRuleCreator)creator2Control.getMock();
    List<IRuleCreator> creators = new ArrayList<IRuleCreator>();
    creators.add(creator1);
    creators.add(creator2);
    
    IRule rule = new IRule(){
      public String getDefinition() {return null;}
      public String getType() {return null;}
      public IBltMessage handle(IBltMessage message) {return null;}
    };
    
    RuleFactory classUnderTest = new RuleFactory();
    classUnderTest.setCreators(creators);
    
    creator1.getType();
    creator1Control.setReturnValue("t1");
    creator2.getType();
    creator2Control.setReturnValue("t2");
    
    creator2.create("xyz");
    creator2Control.setReturnValue(rule);

    creator1Control.replay();
    creator2Control.replay();
    
    //execute test
    IRule result = classUnderTest.create("t2", "xyz");
    
    // verify
    creator1Control.verify();
    creator2Control.verify();
    
    assertSame(rule, result);
  }
  
  public void testCreate_notSupportedType() throws Exception {
    // Setup
    MockControl creator1Control = MockControl.createControl(IRuleCreator.class);
    IRuleCreator creator1 = (IRuleCreator)creator1Control.getMock();
    MockControl creator2Control = MockControl.createControl(IRuleCreator.class);
    IRuleCreator creator2 = (IRuleCreator)creator2Control.getMock();
    List<IRuleCreator> creators = new ArrayList<IRuleCreator>();
    creators.add(creator1);
    creators.add(creator2);
    RuleFactory classUnderTest = new RuleFactory();
    classUnderTest.setCreators(creators);
    
    creator1.getType();
    creator1Control.setReturnValue("t1");
    creator2.getType();
    creator2Control.setReturnValue("t2");

    creator1Control.replay();
    creator2Control.replay();
    
    //execute test
    try {
      classUnderTest.create("t3", "xyz");
      fail("Expected RuleException");
    } catch (RuleException e) {
      // pass
    }
    
    // verify
    creator1Control.verify();
    creator2Control.verify();
  }

  public void testGetTypes() throws Exception {
    // Setup
    MockControl creator1Control = MockControl.createControl(IRuleCreator.class);
    IRuleCreator creator1 = (IRuleCreator)creator1Control.getMock();
    MockControl creator2Control = MockControl.createControl(IRuleCreator.class);
    IRuleCreator creator2 = (IRuleCreator)creator2Control.getMock();
    List<IRuleCreator> creators = new ArrayList<IRuleCreator>();
    creators.add(creator1);
    creators.add(creator2);
    RuleFactory classUnderTest = new RuleFactory();
    classUnderTest.setCreators(creators);
    
    creator1.getType();
    creator1Control.setReturnValue("t1");
    creator2.getType();
    creator2Control.setReturnValue("t2");

    creator1Control.replay();
    creator2Control.replay();
    
    //execute test
    String[] result = classUnderTest.getTypes();

    // verify
    creator1Control.verify();
    creator2Control.verify();
    assertEquals(2, result.length);
    assertEquals("t1", result[0]);
    assertEquals("t2", result[1]);
  }    

  public void testGetTypes_noneDefined() throws Exception {
    // Setup
    RuleFactory classUnderTest = new RuleFactory();
    
    //execute test
    String[] result = classUnderTest.getTypes();
    
    // verify
    assertEquals(0, result.length);
  }    
}
