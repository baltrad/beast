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
import org.easymock.MockControl;

import eu.baltrad.beast.message.IBltMessage;

import junit.framework.TestCase;

/**
 * @author Anders Henja
 *
 */
public class ScriptedRuleTest extends TestCase {
  public void testConstructor() {
    ScriptedRule classUnderTest = new ScriptedRule(null, "groovy", "something");
    assertEquals("groovy", classUnderTest.getType());
    assertEquals("something", classUnderTest.getDefinition());
  }
  
  public void testHandle() {
    MockControl sruleControl = MockControl.createControl(IScriptableRule.class);
    IScriptableRule srule = (IScriptableRule)sruleControl.getMock();

    IBltMessage msg = new IBltMessage(){};
    IBltMessage resmsg = new IBltMessage(){};
    
    srule.handle(msg);
    sruleControl.setReturnValue(resmsg);
    sruleControl.replay();
    
    // Execute test
    ScriptedRule classUnderTest = new ScriptedRule(srule, null, null);
    IBltMessage result = classUnderTest.handle(msg);
    
    // verify
    sruleControl.verify();
    assertSame(result, resmsg);
  }
}
