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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.UUID;

import org.codehaus.groovy.control.CompilationFailedException;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.Time;
import eu.baltrad.beast.ManagerContext;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.message.mo.BltCommandMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.IScriptableRule;
import eu.baltrad.beast.rules.RuleException;

/**
 * @author Anders Henja
 */
public class GroovyRuleTest extends EasyMockSupport {
  interface MethodMock {
    @SuppressWarnings("unchecked")
    public Class parseClass(String code);
    public int setScriptInternal(String script, boolean setscriptonfailure, boolean throwexception);
  };
  
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

  @Test
  public void testType() {
    assertEquals("groovy", GroovyRule.TYPE);
  }

  @Test
  public void testGetType() {
    GroovyRule rule = new GroovyRule();
    assertEquals(GroovyRule.TYPE, rule.getType());
  }

  @Test
  public void testGetState_init() {
    GroovyRule rule = new GroovyRule();
    assertEquals(GroovyRule.UNITIALIZED, rule.getState());
  }
  
  @Test
  public void testHandle() throws Exception {
    IBltMessage msg = new IBltMessage() { };
    IBltMessage hmsg = new IBltMessage() { };
    
    IScriptableRule srule = createMock(IScriptableRule.class);
    
    expect(srule.handle(msg)).andReturn(hmsg);
    
    GroovyRule classUnderTest = new GroovyRule();
    classUnderTest.setScriptableRule(srule);
    
    replayAll();
    
    IBltMessage result = classUnderTest.handle(msg);
    
    verifyAll();
    assertSame(hmsg, result);
  }

  @Test
  public void testHandle_noScript() throws Exception {
    IBltMessage msg = new IBltMessage() { };
    GroovyRule classUnderTest = new GroovyRule();
    IBltMessage result = classUnderTest.handle(msg);
    assertNull(result);
  }

  @Test
  public void testHandle_nullMessage() throws Exception {
    GroovyRule classUnderTest = new GroovyRule();
    
    try {
      classUnderTest.handle(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // pass
    }
  }

  public static class GoodClass implements IScriptableRule {
    public GoodClass() {}
    @Override
    public IBltMessage handle(IBltMessage message) {
      return null;
    }
  }
  
  public class CanNotInstantiateClass implements IScriptableRule {
    public CanNotInstantiateClass() {}
    @Override
    public IBltMessage handle(IBltMessage message) {
      return null;
    }    
  }
  
  public static class IllegalAccessClass implements IScriptableRule {
    private IllegalAccessClass() {
    }
    @Override
    public IBltMessage handle(IBltMessage message) {
      return null;
    }
  }
  
  @Test
  public void testSetScriptInternal_OK() throws Exception {
    final MethodMock method = createMock(MethodMock.class);
    
    expect(method.parseClass("some code")).andReturn(GoodClass.class);
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    replayAll();
    
    int result = rule.setScriptInternal("some code", true, false);
    
    verifyAll();
    assertEquals(GroovyRule.OK, result);
    assertNull(rule.getThrowable());
  }

  @Test
  public void testSetScriptInternal_CompilationError() throws Exception {
    final MethodMock method = createMock(MethodMock.class);
    
    CompilationFailedException exception = new CompilationFailedException(0, null);
    
    expect(method.parseClass("some code")).andThrow(exception);
    
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    replayAll();
    
    int result = rule.setScriptInternal("some code", true, false);
    
    verifyAll();
    assertEquals(GroovyRule.COMPILATION_ERROR, rule.getState());
    assertEquals(GroovyRule.COMPILATION_ERROR, result);
    assertSame(exception, rule.getThrowable());
  }

  @Test
  public void testSetScriptInternal_CanNotInstantiateException() throws Exception {
    final MethodMock method = createMock(MethodMock.class);
    
    expect(method.parseClass("some code")).andReturn(CanNotInstantiateClass.class);
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    replayAll();
    
    int result = rule.setScriptInternal("some code", true, false);
    
    verifyAll();
    
    assertEquals(GroovyRule.INSTANTIATION_EXCEPTION, result);
    assertEquals(GroovyRule.INSTANTIATION_EXCEPTION, rule.getState());
    assertNotNull(rule.getThrowable());
    assertTrue(rule.getThrowable() instanceof InstantiationException);
  }
  
  @Test
  public void testSetScriptInternal_IllegalAccessException() throws Exception {
    final MethodMock method = createMock(MethodMock.class);
    
    expect(method.parseClass("some code")).andReturn(IllegalAccessClass.class);
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    replayAll();
    
    int result = rule.setScriptInternal("some code", true, false);
    
    verifyAll();
    
    assertEquals(GroovyRule.ILLEGAL_ACCESS_EXCEPTION, result);
    assertEquals(GroovyRule.ILLEGAL_ACCESS_EXCEPTION, rule.getState());
    assertNotNull(rule.getThrowable());
    assertTrue(rule.getThrowable() instanceof IllegalAccessException);
  }

  @Test
  public void testSetScriptInternal_ClassCastException() throws Exception {
    final MethodMock method = createMock(MethodMock.class);
    
    expect(method.parseClass("some code")).andReturn(Object.class);

    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    replayAll();
    
    int result = rule.setScriptInternal("some code", true, false);
    
    verifyAll();
    
    assertEquals(GroovyRule.CLASS_CAST_EXCEPTION, result);
    assertEquals(GroovyRule.CLASS_CAST_EXCEPTION, rule.getState());
    assertNotNull(rule.getThrowable());
    assertTrue(rule.getThrowable() instanceof ClassCastException);
  }
  
  @Test
  public void testSetScriptInternal_Throwable() throws Exception {
    final MethodMock method = createMock(MethodMock.class);
    
    RuntimeException exception = new RuntimeException();
    
    expect(method.parseClass("some code")).andThrow(exception);
    
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    replayAll();
    
    int result = rule.setScriptInternal("some code", true, false);
    
    verifyAll();
    assertEquals(GroovyRule.THROWABLE, rule.getState());
    assertEquals(GroovyRule.THROWABLE, result);
    assertSame(exception, rule.getThrowable());
  }

  @Test
  public void testSetScriptInternal_Exception_ThrowExceptionSetScript() throws Exception {
    final MethodMock method = createMock(MethodMock.class);
    
    RuntimeException exception = new RuntimeException();
    
    expect(method.parseClass("some code")).andThrow(exception);
    
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    replayAll();
    
    try {
      rule.setScriptInternal("some code", true, true);
      fail("Expected RuleException");
    } catch (RuleException e) {
      // pass
    }
    
    verifyAll();
    assertEquals(GroovyRule.THROWABLE, rule.getState());
    assertSame(exception, rule.getThrowable());
    assertEquals("some code", rule.getScript());
  }

  @Test
  public void testSetScriptInternal_Exception_ThrowExceptionDontSetScript() throws Exception {
    final MethodMock method = createMock(MethodMock.class);
    
    RuntimeException exception = new RuntimeException();
    
    expect(method.parseClass("some code")).andThrow(exception);
    
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    replayAll();
    
    try {
      rule.setScriptInternal("some code", false, true);
      fail("Expected RuleException");
    } catch (RuleException e) {
      // pass
    }
    
    verifyAll();
    assertEquals(GroovyRule.THROWABLE, rule.getState());
    assertSame(exception, rule.getThrowable());
    assertNull(rule.getScript());
  }

  @Test
  public void testSetScriptInternal_Exception_DontThrowExceptionDontSetScript() throws Exception {
    final MethodMock method = createMock(MethodMock.class);
    
    RuntimeException exception = new RuntimeException();
    
    expect(method.parseClass("some code")).andThrow(exception);
    
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    replayAll();
    
    rule.setScriptInternal("some code", false, false);
    
    verifyAll();
    assertEquals(GroovyRule.THROWABLE, rule.getState());
    assertSame(exception, rule.getThrowable());
    assertNull(rule.getScript());
  }
  
  @Test
  public void testSetScript() throws Exception {
    final MethodMock method = createMock(MethodMock.class);
    
    expect(method.setScriptInternal("some code", false, true)).andReturn(GroovyRule.OK);
    
    GroovyRule rule = new GroovyRule() {
      int setScriptInternal(String script, boolean setscriptonfailure, boolean throwexception) {
        return method.setScriptInternal(script, false, true);
      }
    };
    
    replayAll();
    
    rule.setScript("some code");

    verifyAll();
  }
  
  @Test
  public void testSetScript_SimpleRule() throws Exception {
    BltCommandMessage msg = new BltCommandMessage();
    msg.setCommand("ls -la");

    GroovyRule rule = new GroovyRule();
    rule.setScript(readScript("SimpleRule.groovy"));
    
    // Verify that the rule has loaded properly
    BltAlertMessage result = (BltAlertMessage)rule.handle(msg);
    assertEquals("ls -la", result.getMessage());
    assertEquals(GroovyRule.OK, rule.getState());
  }
  
  @Test
  public void testSetScript_BadRule() throws Exception {
    GroovyRule rule = new GroovyRule();
    try {
      rule.setScript(readScript("BadRule.groovy"));
      fail("Expected RuleException");
    } catch (RuleException e) {
      // pass
    }
    assertEquals(GroovyRule.COMPILATION_ERROR, rule.getState());
  }
  
  @Test
  public void testSetScript_GoogleMapRule() throws Exception {
    FileEntry fileEntry = createMock(FileEntry.class);
    Metadata metadata = createMock(Metadata.class);
    Catalog catalog = createMock(Catalog.class);
    
    Date date = new Date(2011,2,1);
    Time time = new Time(10,15,0);
    
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(fileEntry);

    GroovyRule rule = new GroovyRule();
    rule.setScript(readScript("GoogleMap.groovy"));
    UUID fileEntryUuid = UUID.randomUUID();

    expect(fileEntry.getMetadata()).andReturn(metadata).times(4);
    expect(metadata.getWhatObject()).andReturn("COMP");
    expect(metadata.getWhatSource()).andReturn("ORG:82,CMT:baltrad_2000");
    expect(metadata.getWhatDate()).andReturn(date);
    expect(metadata.getWhatTime()).andReturn(time);
    expect(fileEntry.getUuid()).andReturn(fileEntryUuid);
    expect(catalog.getFileCatalogPath(fileEntryUuid.toString())).andReturn("/tmp/something.h5");
    
    replayAll();
    
    // Verify that the rule has loaded properly
    new ManagerContext().setCatalog(catalog);
    
    BltGenerateMessage result = (BltGenerateMessage)rule.handle(msg);
    
    verifyAll();
    assertEquals("se.smhi.rave.creategmapimage", result.getAlgorithm());
    
    assertEquals(1, result.getFiles().length);
    assertEquals("/tmp/something.h5", result.getFiles()[0]);
    
    assertEquals(2, result.getArguments().length);
    assertEquals("outfile", result.getArguments()[0]);
    assertEquals("/opt/baltrad/rave_gmap/web/data/baltrad_2000/2011/02/01/201102011015.png", result.getArguments()[1]);
  }

  @Test
  public void testSetScript_GoogleMapRule_notSupportedArea() throws Exception {
    FileEntry fileEntry = createMock(FileEntry.class);
    Metadata metadata = createMock(Metadata.class);
    Catalog catalog = createMock(Catalog.class);
    
    Date date = new Date(2011,2,1);
    Time time = new Time(10,15,0);
    
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(fileEntry);

    GroovyRule rule = new GroovyRule();
    rule.setScript(readScript("GoogleMap.groovy"));
    
    expect(fileEntry.getMetadata()).andReturn(metadata).times(4);
    expect(metadata.getWhatObject()).andReturn("COMP");
    expect(metadata.getWhatSource()).andReturn("ORG:82,CMT:mydummyarea");
    expect(metadata.getWhatDate()).andReturn(date);
    expect(metadata.getWhatTime()).andReturn(time);

    replayAll();
    
    // Verify that the rule has loaded properly
    new ManagerContext().setCatalog(catalog);
    
    BltGenerateMessage result = (BltGenerateMessage)rule.handle(msg);

    verifyAll();
    assertNull(result);
  }
}
