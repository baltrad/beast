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
package eu.baltrad.beast.rules.bdb;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanInitializationException;

import eu.baltrad.bdb.FileCatalog;
import eu.baltrad.bdb.db.Database;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;

public class BdbTrimCountRuleTest extends EasyMockSupport {
  private BdbTrimCountRule classUnderTest = null;
  private BdbTrimCountRuleMethods methods = null;
  private Database db = null;
  private FileCatalog catalog = null;

  private static interface BdbTrimCountRuleMethods {
    public void execute();
    public int getFileCount();
  }

  @Before
  public void setUp() throws Exception {
    catalog = null;
    methods = createMock(BdbTrimCountRuleMethods.class);
    db = createMock(Database.class);
    catalog = createMock(FileCatalog.class);
    classUnderTest = new BdbTrimCountRule();
  }

  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  @Test
  public void testGetProperties() {
    classUnderTest.setFileCountLimit(1);
    Map<String, String> props = classUnderTest.getProperties();
    assertEquals("1", props.get("fileCountLimit"));
  }

  @Test
  public void testSetProperties() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("fileCountLimit", "1");
    classUnderTest.setProperties(props);
    assertEquals(1, classUnderTest.getFileCountLimit());
  }

  @Test
  public void testSetProperties_invalidType() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("fileCountLimit", "invalid");
    classUnderTest.setProperties(props);
    assertEquals(0, classUnderTest.getFileCountLimit());
  }

  @Test
  public void testSetProperties_missingProps() {
    Map<String, String> props = new HashMap<String, String>();
    classUnderTest.setProperties(props);
    assertEquals(0, classUnderTest.getFileCountLimit());
  }

  @Test
  public void testHandle_noLimitSet() {
    IBltMessage msg = new BltTriggerJobMessage();

    replayAll();

    classUnderTest.handle(msg);
    
    verifyAll();
  }

  @Test
  public void testHandle_wrongMessageType() {
    classUnderTest.setFileCountLimit(1);
    IBltMessage msg = new IBltMessage() {};

    replayAll();

    classUnderTest.handle(msg);
    
    verifyAll();
  }

  @Test
  public void testHandle() {
    classUnderTest = new BdbTrimCountRule() {
      protected void execute() {
        methods.execute();
      }
    };

    classUnderTest.setFileCountLimit(1);
    IBltMessage msg = new BltTriggerJobMessage();

    methods.execute();
    replayAll();

    classUnderTest.handle(msg);
    
    verifyAll();
  }

  @Test
  public void testExecute() {
    classUnderTest = new BdbTrimCountRule() {
      protected int getFileCount() {
        return methods.getFileCount();
      }
    };
    classUnderTest.setFileCatalog(catalog);
    classUnderTest.setFileCountLimit(1);

    expect(methods.getFileCount()).andReturn(2);
    expect(catalog.getDatabase()).andReturn(db);
    expect(db.removeFilesByCount(1, 100)).andReturn(1);
    
    replayAll();
  
    classUnderTest.execute();
  
    verifyAll();
  }

  @Test
  public void testGetFileCount() {
    classUnderTest.setFileCatalog(catalog);

    expect(catalog.getDatabase()).andReturn(db);
    expect(db.getFileCount()).andReturn(10L);
    
    replayAll();

    int result = classUnderTest.getFileCount();
    
    verifyAll();

    assertEquals(10, result);
  }
  
  @Test
  public void testAfterPropertiesSet() {
    classUnderTest = new BdbTrimCountRule();
    classUnderTest.setFileCatalog(catalog);
    classUnderTest.afterPropertiesSet();
  }
  
  @Test
  public void testAfterPropertiesSet_missingCatalog() {
    classUnderTest = new BdbTrimCountRule();
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected BeanInitializationException");
    } catch (BeanInitializationException e) {
      // pass
    }
  }
}
