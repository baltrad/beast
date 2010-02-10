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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

import junit.framework.TestCase;
import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.message.mo.BltCommandMessage;

/**
 * @author Anders Henja
 *
 */
public class GroovyRuleCreatorTest extends TestCase {
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
  
  public void testCreate_simpleRule() throws Exception {
    // setup
    String script = readScript("SimpleRule.groovy");
    GroovyRuleCreator classUnderTest = new GroovyRuleCreator();
    BltCommandMessage msg = new BltCommandMessage();
    msg.setCommand("ls -la");

    // Test
    IRule rule = classUnderTest.create(script);
    
    // Verify
    BltAlertMessage result = (BltAlertMessage)rule.handle(msg);
    assertEquals(GroovyRuleCreator.TYPE, rule.getType());
    assertEquals(script, rule.getDefinition());
    assertEquals("ls -la", result.getMessage());
  }
  
  public void testCreate_badRule() throws Exception {
    // setup
    String script = readScript("BadRule.groovy");
    GroovyRuleCreator classUnderTest = new GroovyRuleCreator();

    // Test
    try {
      classUnderTest.create(script);
      fail("Expected RuleException");
    } catch (RuleException e) {
      // pass
    }
  }  
  
  public void testGetType() {
    GroovyRuleCreator classUnderTest = new GroovyRuleCreator();
    assertEquals("groovy", classUnderTest.getType());
  }
}
