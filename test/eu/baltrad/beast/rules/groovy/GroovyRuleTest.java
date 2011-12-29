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
import java.util.UUID;

import junit.framework.TestCase;

import org.codehaus.groovy.control.CompilationFailedException;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

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
 *
 */
public class GroovyRuleTest extends TestCase {
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

  public void testType() {
    assertEquals("groovy", GroovyRule.TYPE);
  }

  public void testGetType() {
    GroovyRule rule = new GroovyRule();
    assertEquals(GroovyRule.TYPE, rule.getType());
  }

  public void testGetState_init() {
    GroovyRule rule = new GroovyRule();
    assertEquals(GroovyRule.UNITIALIZED, rule.getState());
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
    IBltMessage result = classUnderTest.handle(msg);
    assertNull(result);
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
  
  public void testSetScriptInternal_OK() throws Exception {
    MockControl methodControl = MockControl.createControl(MethodMock.class);
    final MethodMock method = (MethodMock)methodControl.getMock();
    
    method.parseClass("some code");
    methodControl.setReturnValue(GoodClass.class);
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    methodControl.replay();
    
    int result = rule.setScriptInternal("some code", true, false);
    
    methodControl.verify();
    assertEquals(GroovyRule.OK, result);
    assertNull(rule.getThrowable());
  }

  public void testSetScriptInternal_CompilationError() throws Exception {
    MockControl methodControl = MockControl.createControl(MethodMock.class);
    final MethodMock method = (MethodMock)methodControl.getMock();
    
    CompilationFailedException exception = new CompilationFailedException(0, null);
    
    method.parseClass("some code");
    methodControl.setThrowable(exception);
    
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    methodControl.replay();
    
    int result = rule.setScriptInternal("some code", true, false);
    
    methodControl.verify();
    assertEquals(GroovyRule.COMPILATION_ERROR, rule.getState());
    assertEquals(GroovyRule.COMPILATION_ERROR, result);
    assertSame(exception, rule.getThrowable());
  }

  public void testSetScriptInternal_CanNotInstantiateException() throws Exception {
    MockControl methodControl = MockControl.createControl(MethodMock.class);
    final MethodMock method = (MethodMock)methodControl.getMock();
    
    method.parseClass("some code");
    methodControl.setReturnValue(CanNotInstantiateClass.class);
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    methodControl.replay();
    
    int result = rule.setScriptInternal("some code", true, false);
    
    methodControl.verify();
    
    assertEquals(GroovyRule.INSTANTIATION_EXCEPTION, result);
    assertEquals(GroovyRule.INSTANTIATION_EXCEPTION, rule.getState());
    assertNotNull(rule.getThrowable());
    assertTrue(rule.getThrowable() instanceof InstantiationException);
  }
  
  public void testSetScriptInternal_IllegalAccessException() throws Exception {
    MockControl methodControl = MockControl.createControl(MethodMock.class);
    final MethodMock method = (MethodMock)methodControl.getMock();
    
    method.parseClass("some code");
    methodControl.setReturnValue(IllegalAccessClass.class);
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    methodControl.replay();
    
    int result = rule.setScriptInternal("some code", true, false);
    
    methodControl.verify();
    
    assertEquals(GroovyRule.ILLEGAL_ACCESS_EXCEPTION, result);
    assertEquals(GroovyRule.ILLEGAL_ACCESS_EXCEPTION, rule.getState());
    assertNotNull(rule.getThrowable());
    assertTrue(rule.getThrowable() instanceof IllegalAccessException);
  }

  public void testSetScriptInternal_ClassCastException() throws Exception {
    MockControl methodControl = MockControl.createControl(MethodMock.class);
    final MethodMock method = (MethodMock)methodControl.getMock();
    
    method.parseClass("some code");
    methodControl.setReturnValue(Object.class);
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    methodControl.replay();
    
    int result = rule.setScriptInternal("some code", true, false);
    
    methodControl.verify();
    
    assertEquals(GroovyRule.CLASS_CAST_EXCEPTION, result);
    assertEquals(GroovyRule.CLASS_CAST_EXCEPTION, rule.getState());
    assertNotNull(rule.getThrowable());
    assertTrue(rule.getThrowable() instanceof ClassCastException);
  }
  
  public void testSetScriptInternal_Throwable() throws Exception {
    MockControl methodControl = MockControl.createControl(MethodMock.class);
    final MethodMock method = (MethodMock)methodControl.getMock();
    
    RuntimeException exception = new RuntimeException();
    
    method.parseClass("some code");
    methodControl.setThrowable(exception);
    
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    methodControl.replay();
    
    int result = rule.setScriptInternal("some code", true, false);
    
    methodControl.verify();
    assertEquals(GroovyRule.THROWABLE, rule.getState());
    assertEquals(GroovyRule.THROWABLE, result);
    assertSame(exception, rule.getThrowable());
  }

  public void testSetScriptInternal_Exception_ThrowExceptionSetScript() throws Exception {
    MockControl methodControl = MockControl.createControl(MethodMock.class);
    final MethodMock method = (MethodMock)methodControl.getMock();
    
    RuntimeException exception = new RuntimeException();
    
    method.parseClass("some code");
    methodControl.setThrowable(exception);
    
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    methodControl.replay();
    
    try {
      rule.setScriptInternal("some code", true, true);
      fail("Expected RuleException");
    } catch (RuleException e) {
      // pass
    }
    
    methodControl.verify();
    assertEquals(GroovyRule.THROWABLE, rule.getState());
    assertSame(exception, rule.getThrowable());
    assertEquals("some code", rule.getScript());
  }

  public void testSetScriptInternal_Exception_ThrowExceptionDontSetScript() throws Exception {
    MockControl methodControl = MockControl.createControl(MethodMock.class);
    final MethodMock method = (MethodMock)methodControl.getMock();
    
    RuntimeException exception = new RuntimeException();
    
    method.parseClass("some code");
    methodControl.setThrowable(exception);
    
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    methodControl.replay();
    
    try {
      rule.setScriptInternal("some code", false, true);
      fail("Expected RuleException");
    } catch (RuleException e) {
      // pass
    }
    
    methodControl.verify();
    assertEquals(GroovyRule.THROWABLE, rule.getState());
    assertSame(exception, rule.getThrowable());
    assertNull(rule.getScript());
  }

  public void testSetScriptInternal_Exception_DontThrowExceptionDontSetScript() throws Exception {
    MockControl methodControl = MockControl.createControl(MethodMock.class);
    final MethodMock method = (MethodMock)methodControl.getMock();
    
    RuntimeException exception = new RuntimeException();
    
    method.parseClass("some code");
    methodControl.setThrowable(exception);
    
    GroovyRule rule = new GroovyRule() {
      @SuppressWarnings("unchecked")
      protected Class parseClass(String code) {
        return method.parseClass(code);
      }
    };
    
    methodControl.replay();
    
    rule.setScriptInternal("some code", false, false);
    
    methodControl.verify();
    assertEquals(GroovyRule.THROWABLE, rule.getState());
    assertSame(exception, rule.getThrowable());
    assertNull(rule.getScript());
  }
  
  public void testSetScript() throws Exception {
    MockControl methodControl = MockControl.createControl(MethodMock.class);
    final MethodMock method = (MethodMock)methodControl.getMock();
    
    method.setScriptInternal("some code", false, true);
    methodControl.setReturnValue(GroovyRule.OK);
    
    GroovyRule rule = new GroovyRule() {
      int setScriptInternal(String script, boolean setscriptonfailure, boolean throwexception) {
        return method.setScriptInternal(script, false, true);
      }
    };
    
    methodControl.replay();
    
    rule.setScript("some code");

    methodControl.verify();
  }
  
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
  
  public void testSetScript_GoogleMapRule() throws Exception {
    MockControl fileEntryControl = MockClassControl.createControl(FileEntry.class);
    FileEntry fileEntry = (FileEntry)fileEntryControl.getMock();
    MockControl metadataControl = MockClassControl.createControl(Metadata.class);
    Metadata metadata = (Metadata)metadataControl.getMock();
    MockControl catalogControl = MockClassControl.createControl(Catalog.class);
    Catalog catalog = (Catalog)catalogControl.getMock();
    
    Date date = new Date(2011,2,1);
    Time time = new Time(10,15,0);
    
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(fileEntry);

    GroovyRule rule = new GroovyRule();
    rule.setScript(readScript("GoogleMap.groovy"));
    UUID fileEntryUuid = UUID.randomUUID();

    fileEntry.getMetadata();
    fileEntryControl.setReturnValue(metadata, MockControl.ONE_OR_MORE);
    metadata.getWhatObject();
    metadataControl.setReturnValue("COMP");
    metadata.getWhatSource();
    metadataControl.setReturnValue("ORG:82,CMT:baltrad_2000");
    metadata.getWhatDate();
    metadataControl.setReturnValue(date);
    metadata.getWhatTime();
    metadataControl.setReturnValue(time);
    fileEntry.getUuid();
    fileEntryControl.setReturnValue(fileEntryUuid);
    catalog.getFileCatalogPath(fileEntryUuid.toString());
    catalogControl.setReturnValue("/tmp/something.h5");
    fileEntryControl.replay();
    metadataControl.replay();
    catalogControl.replay();
    
    // Verify that the rule has loaded properly
    new ManagerContext().setCatalog(catalog);
    
    BltGenerateMessage result = (BltGenerateMessage)rule.handle(msg);
    
    fileEntryControl.verify();
    metadataControl.verify();
    catalogControl.verify();
    
    assertEquals("se.smhi.rave.creategmapimage", result.getAlgorithm());
    
    assertEquals(1, result.getFiles().length);
    assertEquals("/tmp/something.h5", result.getFiles()[0]);
    
    assertEquals(2, result.getArguments().length);
    assertEquals("outfile", result.getArguments()[0]);
    assertEquals("/opt/baltrad/rave_gmap/web/data/baltrad_2000/2011/02/01/201102011015.png", result.getArguments()[1]);
  }

  public void testSetScript_GoogleMapRule_notSupportedArea() throws Exception {
    MockControl fileEntryControl = MockClassControl.createControl(FileEntry.class);
    FileEntry fileEntry = (FileEntry)fileEntryControl.getMock();
    MockControl metadataControl = MockClassControl.createControl(Metadata.class);
    Metadata metadata = (Metadata)metadataControl.getMock();
    MockControl catalogControl = MockClassControl.createControl(Catalog.class);
    Catalog catalog = (Catalog)catalogControl.getMock();
    
    Date date = new Date(2011,2,1);
    Time time = new Time(10,15,0);
    
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(fileEntry);

    GroovyRule rule = new GroovyRule();
    rule.setScript(readScript("GoogleMap.groovy"));
    
    fileEntry.getMetadata();
    fileEntryControl.setReturnValue(metadata, MockControl.ONE_OR_MORE);
    metadata.getWhatObject();
    metadataControl.setReturnValue("COMP");
    metadata.getWhatSource();
    metadataControl.setReturnValue("ORG:82,CMT:mydummyarea");
    metadata.getWhatDate();
    metadataControl.setReturnValue(date);
    metadata.getWhatTime();
    metadataControl.setReturnValue(time);

    fileEntryControl.replay();
    metadataControl.replay();
    catalogControl.replay();
    
    // Verify that the rule has loaded properly
    new ManagerContext().setCatalog(catalog);
    
    BltGenerateMessage result = (BltGenerateMessage)rule.handle(msg);
    
    fileEntryControl.verify();
    metadataControl.verify();
    catalogControl.verify();
    assertNull(result);
  }
}
