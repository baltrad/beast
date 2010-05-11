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
package eu.baltrad.beast.rules.groovy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

import junit.framework.TestCase;

import org.easymock.MockControl;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.message.mo.BltCommandMessage;
import eu.baltrad.beast.rules.IScriptableRule;
import eu.baltrad.beast.rules.RuleException;

/**
 * @author Anders Henja
 *
 */
public class GroovyRuleTest extends TestCase {
  protected String readScript(String scriptname) throws Exception {
    StringBuffer buffer = new StringBuffer();
    URL url = this.getClass().getResource(scriptname);
    File f = new File(url.getPath());
    BufferedReader reader = new BufferedReader(new FileReader(f));
    char[] buf = new char[1024];
    int nread = 0;
    while ((nread = reader.read(buf)) != -1) {
      String d = String.valueOf(buf, 0, nread);
      buffer.append(d);
    }
    return buffer.toString();
  }

  public void testType() {
    assertEquals("groovy", GroovyRule.TYPE);
  }

  public void testGetType() {
    GroovyRule rule = new GroovyRule();
    assertEquals(GroovyRule.TYPE, rule.getType());
  }

  public void testHandle() throws Exception {
    IBltMessage msg = new IBltMessage() { };
    IBltMessage hmsg = new IBltMessage() { };
    
    MockControl sruleControl = MockControl.createControl(IScriptableRule.class);
    IScriptableRule srule = (IScriptableRule)sruleControl.getMock();
    
    srule.handle(msg);
    sruleControl.setReturnValue(hmsg);
    
    GroovyRule classUnderTest = new GroovyRule();
    classUnderTest.setScriptableRule(srule);
    
    sruleControl.replay();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    sruleControl.verify();
    assertSame(hmsg, result);
  }

  public void testHandle_noScript() throws Exception {
    IBltMessage msg = new IBltMessage() { };
    
    GroovyRule classUnderTest = new GroovyRule();
    
    try {
      classUnderTest.handle(msg);
      fail("Expected RuleException");
    } catch (RuleException e) {
      // pass
    }
  }

  public void testHandle_nullMessage() throws Exception {
    GroovyRule classUnderTest = new GroovyRule();
    
    try {
      classUnderTest.handle(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // pass
    }
  }
  
  
  public void testSetScript_SimpleRule() throws Exception {
    BltCommandMessage msg = new BltCommandMessage();
    msg.setCommand("ls -la");

    GroovyRule rule = new GroovyRule();
    rule.setScript(readScript("SimpleRule.groovy"));
    
    // Verify that the rule has loaded properly
    BltAlertMessage result = (BltAlertMessage)rule.handle(msg);
    assertEquals("ls -la", result.getMessage());
  }
  
  public void testSetScript_BadRule() throws Exception {
    GroovyRule rule = new GroovyRule();
    try {
      rule.setScript(readScript("BadRule.groovy"));
      fail("Expected RuleException");
    } catch (RuleException e) {
      // pass
    }
  }
}
