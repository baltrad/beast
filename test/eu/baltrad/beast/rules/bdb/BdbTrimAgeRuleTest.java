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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.springframework.beans.factory.BeanInitializationException;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;
import eu.baltrad.fc.DateTime;
import eu.baltrad.fc.FileCatalog;
import eu.baltrad.fc.Database;
import eu.baltrad.fc.FileEntry;
import eu.baltrad.fc.FileQuery;
import eu.baltrad.fc.FileResult;


public class BdbTrimAgeRuleTest extends TestCase {
  private BdbTrimAgeRule classUnderTest = null;
  private MockControl methodsControl = null;
  private BdbTrimAgeRuleMethods methods = null;
  private MockControl dbControl = null;
  private Database db = null;
  private MockControl catalogControl = null;
  private FileCatalog catalog = null;
  private MockControl queryControl = null;
  private FileQuery query = null;
  private MockControl resultControl = null;
  private FileResult result = null;
  private MockControl entryControl = null;
  private FileEntry entry = null;

  private static interface BdbTrimAgeRuleMethods {
    public void execute();
    public FileQuery getExcessiveFileQuery();
    public DateTime getAgeLimitDateTime();
    public DateTime getCurrentDateTime();
  }

  public void setUp() throws Exception {
    classUnderTest = new BdbTrimAgeRule();
    methodsControl = MockControl.createControl(BdbTrimAgeRuleMethods.class);
    methods = (BdbTrimAgeRuleMethods)methodsControl.getMock();
    dbControl = MockClassControl.createControl(Database.class);
    db = (Database)dbControl.getMock();
    catalogControl = MockClassControl.createControl(FileCatalog.class);
    catalog = (FileCatalog)catalogControl.getMock();
    queryControl = MockClassControl.createControl(FileQuery.class);
    query = (FileQuery)queryControl.getMock();
    resultControl = MockClassControl.createControl(FileResult.class);
    result = (FileResult)resultControl.getMock();
    entryControl = MockClassControl.createControl(FileEntry.class);
    entry = (FileEntry)entryControl.getMock();
  }

  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  protected void replay() {
    methodsControl.replay();
    dbControl.replay();
    catalogControl.replay();
    queryControl.replay();
    resultControl.replay();
    entryControl.replay();
  }

  protected void verify() {
    methodsControl.verify();
    dbControl.verify();
    catalogControl.verify();
    queryControl.verify();
    resultControl.verify();
    entryControl.verify();
  }
  
  public void testGetProperties() {
    classUnderTest.setFileAgeLimit(1);
    Map<String, String> props = classUnderTest.getProperties();
    assertEquals("1", props.get("fileAgeLimit"));
  }

  public void testSetProperties() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("fileAgeLimit", "1");
    classUnderTest.setProperties(props);
    assertEquals(1, classUnderTest.getFileAgeLimit());
  }

  public void testSetProperties_invalidType() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("fileAgeLimit", "invalid");
    classUnderTest.setProperties(props);
    assertEquals(0, classUnderTest.getFileAgeLimit());
  }

  public void testSetProperties_missingProps() {
    Map<String, String> props = new HashMap<String, String>();
    classUnderTest.setProperties(props);
    assertEquals(0, classUnderTest.getFileAgeLimit());
  }

  public void testHandle_noLimitSet() {
    IBltMessage msg = new BltTriggerJobMessage();

    replay();

    classUnderTest.handle(msg);
    verify();
  }

  public void testHandle_wrongMessageType() {
    classUnderTest.setFileAgeLimit(1);
    IBltMessage msg = new IBltMessage() {};

    replay();

    classUnderTest.handle(msg);
    verify();
  }

  public void testHandle() {
    classUnderTest = new BdbTrimAgeRule() {
      protected void execute() {
        methods.execute();
      }
    };

    classUnderTest.setFileAgeLimit(1);
    IBltMessage msg = new BltTriggerJobMessage();

    methods.execute();
    replay();

    classUnderTest.handle(msg);
    verify();
  }

  public void testExecute() {
    classUnderTest = new BdbTrimAgeRule() {
      protected FileQuery getExcessiveFileQuery() {
        return methods.getExcessiveFileQuery();
      }
    };
    classUnderTest.setFileCatalog(catalog);
    classUnderTest.setFileAgeLimit(1);

    methods.getExcessiveFileQuery();
    methodsControl.setReturnValue(query);
    catalog.database();
    catalogControl.setReturnValue(db);
    db.execute(query);
    dbControl.setReturnValue(result);
    result.size();
    resultControl.setReturnValue(1);
    result.next();
    resultControl.setReturnValue(true);
    result.entry();
    resultControl.setReturnValue(entry);
    catalog.remove(entry);
    catalogControl.setReturnValue(true);
    result.next();
    resultControl.setReturnValue(false);
    result.delete();
    replay();
  
    classUnderTest.execute();
  
    verify();
  }
  
  public void testGetExcessiveFileQuery() {
    classUnderTest = new BdbTrimAgeRule() {
      protected DateTime getAgeLimitDateTime() {
        return methods.getAgeLimitDateTime();
      }
    };
    classUnderTest.setFileCatalog(catalog);
    DateTime dt = new DateTime(2011, 1, 7, 11, 0, 0);
    
    methods.getAgeLimitDateTime();
    methodsControl.setReturnValue(dt);
    replay();
  
    FileQuery q = classUnderTest.getExcessiveFileQuery();
    verify();
    assertNotNull(q);
    // XXX: assert valid filter
  }

  public void testGetAgeLimitDateTime() {
    classUnderTest = new BdbTrimAgeRule() {
      protected DateTime getCurrentDateTime() {
        return methods.getCurrentDateTime();
      }
    };
    classUnderTest.setFileAgeLimit(86400 + 3600 + 60);
    DateTime dt = new DateTime(2011, 1, 7, 11, 0, 0);
    DateTime expected = new DateTime(2011, 1, 6, 9, 59, 0);

    methods.getCurrentDateTime();
    methodsControl.setReturnValue(dt);
    replay();

    DateTime result = classUnderTest.getAgeLimitDateTime();
    verify();
    assertTrue(result.equals(expected));
  }

  public void testGetAgeLimitDateTimeLargeLimit() {
    classUnderTest = new BdbTrimAgeRule() {
      protected DateTime getCurrentDateTime() {
        return methods.getCurrentDateTime();
      }
    };
    classUnderTest.setFileAgeLimit(86400 * 45);
    DateTime now = new DateTime(2011, 1, 7, 11);
    DateTime expected = new DateTime(2010, 11, 23, 11);

    methods.getCurrentDateTime();
    methodsControl.setReturnValue(now);
    replay();

    DateTime result = classUnderTest.getAgeLimitDateTime();
    verify();
    assertTrue(result.equals(expected));
  }
  
  public void testAfterPropertiesSet() {
    classUnderTest = new BdbTrimAgeRule();
    classUnderTest.setFileCatalog(catalog);
    classUnderTest.afterPropertiesSet();
  }
  
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
