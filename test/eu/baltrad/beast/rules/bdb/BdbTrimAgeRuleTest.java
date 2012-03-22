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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.db.FileQuery;
import eu.baltrad.bdb.db.FileResult;
import eu.baltrad.bdb.util.DateTime;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;

public class BdbTrimAgeRuleTest extends EasyMockSupport {
  private BdbTrimAgeRule classUnderTest = null;
  private BdbTrimAgeRuleMethods methods = null;
  private Database db = null;
  private FileCatalog catalog = null;
  private FileQuery query = null;
  private FileResult result = null;
  private FileEntry entry = null;

  private static interface BdbTrimAgeRuleMethods {
    public void execute();
    public FileQuery getExcessiveFileQuery();
    public DateTime getAgeLimitDateTime();
    public DateTime getCurrentDateTime();
  }

  @Before
  public void setUp() throws Exception {
    classUnderTest = new BdbTrimAgeRule();
    methods = createMock(BdbTrimAgeRuleMethods.class);
    db = createMock(Database.class);
    catalog = createMock(FileCatalog.class);
    query = createMock(FileQuery.class);
    result = createMock(FileResult.class);
    entry = createMock(FileEntry.class);
  }

  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  @Test
  public void testGetProperties() {
    classUnderTest.setFileAgeLimit(1);
    Map<String, String> props = classUnderTest.getProperties();
    assertEquals("1", props.get("fileAgeLimit"));
  }

  @Test
  public void testSetProperties() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("fileAgeLimit", "1");
    classUnderTest.setProperties(props);
    assertEquals(1, classUnderTest.getFileAgeLimit());
  }

  @Test
  public void testSetProperties_invalidType() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("fileAgeLimit", "invalid");
    classUnderTest.setProperties(props);
    assertEquals(0, classUnderTest.getFileAgeLimit());
  }

  @Test
  public void testSetProperties_missingProps() {
    Map<String, String> props = new HashMap<String, String>();
    classUnderTest.setProperties(props);
    assertEquals(0, classUnderTest.getFileAgeLimit());
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
    classUnderTest.setFileAgeLimit(1);
    IBltMessage msg = new IBltMessage() {};

    replayAll();

    classUnderTest.handle(msg);
    
    verifyAll();
  }

  @Test
  public void testHandle() {
    classUnderTest = new BdbTrimAgeRule() {
      protected void execute() {
        methods.execute();
      }
    };

    classUnderTest.setFileAgeLimit(1);
    IBltMessage msg = new BltTriggerJobMessage();

    methods.execute();
    replayAll();

    classUnderTest.handle(msg);
    
    verifyAll();
  }

  @Test
  public void testExecute() {
    classUnderTest = new BdbTrimAgeRule() {
      protected FileQuery getExcessiveFileQuery() {
        return methods.getExcessiveFileQuery();
      }
    };
    classUnderTest.setFileCatalog(catalog);
    classUnderTest.setFileAgeLimit(1);

    expect(methods.getExcessiveFileQuery()).andReturn(query);
    expect(catalog.getDatabase()).andReturn(db);
    expect(db.execute(query)).andReturn(result);
    expect(result.size()).andReturn(1);
    expect(result.next()).andReturn(true);
    expect(result.getFileEntry()).andReturn(entry);
    catalog.remove(entry);
    expect(result.next()).andReturn(false);
    result.close();
    
    replayAll();
  
    classUnderTest.execute();
  
    verifyAll();
  }
  
  @Test
  public void testGetExcessiveFileQuery() {
    classUnderTest = new BdbTrimAgeRule() {
      protected DateTime getAgeLimitDateTime() {
        return methods.getAgeLimitDateTime();
      }
    };
    classUnderTest.setFileCatalog(catalog);
    DateTime dt = new DateTime(2011, 1, 7, 11, 0, 0);
    
    expect(methods.getAgeLimitDateTime()).andReturn(dt);

    replayAll();
  
    FileQuery q = classUnderTest.getExcessiveFileQuery();
    
    verifyAll();
    assertNotNull(q);
  }

  @Test
  public void testGetAgeLimitDateTime() {
    classUnderTest = new BdbTrimAgeRule() {
      protected DateTime getCurrentDateTime() {
        return methods.getCurrentDateTime();
      }
    };
    classUnderTest.setFileAgeLimit(86400 + 3600 + 60);
    DateTime dt = new DateTime(2011, 1, 7, 11, 0, 0);
    DateTime expected = new DateTime(2011, 1, 6, 9, 59, 0);

    expect(methods.getCurrentDateTime()).andReturn(dt);

    replayAll();

    DateTime result = classUnderTest.getAgeLimitDateTime();
    
    verifyAll();
    assertTrue(result.equals(expected));
  }

  @Test
  public void testGetAgeLimitDateTimeLargeLimit() {
    classUnderTest = new BdbTrimAgeRule() {
      protected DateTime getCurrentDateTime() {
        return methods.getCurrentDateTime();
      }
    };
    classUnderTest.setFileAgeLimit(86400 * 45);
    DateTime now = new DateTime(2011, 1, 7, 11, 0);
    DateTime expected = new DateTime(2010, 11, 23, 11, 0);

    expect(methods.getCurrentDateTime()).andReturn(now);

    replayAll();

    DateTime result = classUnderTest.getAgeLimitDateTime();
    
    verifyAll();
    assertTrue(result.equals(expected));
  }
  
  @Test
  public void testAfterPropertiesSet() {
    classUnderTest = new BdbTrimAgeRule();
    classUnderTest.setFileCatalog(catalog);
    classUnderTest.afterPropertiesSet();
  }
  
  @Test
  public void testAfterPropertiesSet_missingCatalog() {
    classUnderTest = new BdbTrimAgeRule();
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected BeanInitializationException");
    } catch (BeanInitializationException e) {
      // pass
    }
  }
}
