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

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;

import eu.baltrad.fc.DateTime;
import eu.baltrad.fc.FileCatalog;
import eu.baltrad.fc.TimeDelta;
import eu.baltrad.fc.db.FileEntry;
import eu.baltrad.fc.db.FileQuery;
import eu.baltrad.fc.db.FileResult;


public class BdbTrimCountRuleTest extends TestCase {
  private BdbTrimCountRule classUnderTest = null;
  private MockControl methodsControl = null;
  private BdbTrimCountRuleMethods methods = null;
  private MockControl catalogControl = null;
  private FileCatalog catalog = null;
  private MockControl queryControl = null;
  private FileQuery query = null;
  private MockControl resultControl = null;
  private FileResult result = null;
  private MockControl entryControl = null;
  private FileEntry entry = null;

  private static interface BdbTrimCountRuleMethods {
    public void execute();
    public FileQuery getExcessiveFileQuery();
    public int getFileCount();
  }

  public void setUp() throws Exception {
    catalog = null;
    methodsControl = MockControl.createControl(BdbTrimCountRuleMethods.class);
    methods = (BdbTrimCountRuleMethods)methodsControl.getMock();
    classUnderTest = new BdbTrimCountRule();
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
    catalogControl.replay();
    queryControl.replay();
    resultControl.replay();
    entryControl.replay();
  }

  protected void verify() {
    methodsControl.verify();
    catalogControl.verify();
    queryControl.verify();
    resultControl.verify();
    entryControl.verify();
  }
  
  public void testGetProperties() {
    classUnderTest.setFileCountLimit(1);
    Map<String, String> props = classUnderTest.getProperties();
    assertEquals("1", props.get("fileCountLimit"));
  }

  public void testSetProperties() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("fileCountLimit", "1");
    classUnderTest.setProperties(props);
    assertEquals(1, classUnderTest.getFileCountLimit());
  }

  public void testSetProperties_invalidType() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("fileCountLimit", "invalid");
    classUnderTest.setProperties(props);
    assertEquals(0, classUnderTest.getFileCountLimit());
  }

  public void testSetProperties_missingProps() {
    Map<String, String> props = new HashMap<String, String>();
    classUnderTest.setProperties(props);
    assertEquals(0, classUnderTest.getFileCountLimit());
  }

  public void testHandle_noLimitSet() {
    IBltMessage msg = new BltTriggerJobMessage();

    replay();

    classUnderTest.handle(msg);
    verify();
  }

  public void testHandle_wrongMessageType() {
    classUnderTest.setFileCountLimit(1);
    IBltMessage msg = new IBltMessage() {};

    replay();

    classUnderTest.handle(msg);
    verify();
  }

  public void testHandle() {
    classUnderTest = new BdbTrimCountRule() {
      protected void execute() {
        methods.execute();
      }
    };

    classUnderTest.setFileCountLimit(1);
    IBltMessage msg = new BltTriggerJobMessage();

    methods.execute();
    replay();

    classUnderTest.handle(msg);
    verify();
  }

  public void testExecute() {
    classUnderTest = new BdbTrimCountRule() {
      protected FileQuery getExcessiveFileQuery() {
        return methods.getExcessiveFileQuery();
      }
    };
    classUnderTest.setFileCatalog(catalog);
    classUnderTest.setFileCountLimit(1);

    methods.getExcessiveFileQuery();
    methodsControl.setReturnValue(query);
    query.execute();
    queryControl.setReturnValue(result);
    result.next();
    resultControl.setReturnValue(true);
    result.entry();
    resultControl.setReturnValue(entry);
    catalog.remove(entry);
    catalogControl.setReturnValue(true);
    result.next();
    resultControl.setReturnValue(false);
    replay();
  
    classUnderTest.execute();
  
    verify();
  }
  
  public void testGetExcessiveFileQuery() {
    classUnderTest = new BdbTrimCountRule() {
      protected int getFileCount() {
        return methods.getFileCount();
      }
    };
    classUnderTest.setFileCountLimit(100);
    classUnderTest.setFileCatalog(catalog);
    
    catalog.query_file();
    catalogControl.setReturnValue(query);
    methods.getFileCount();
    methodsControl.setReturnValue(110);
    // XXX: ignore argument
    query.order_by(null, FileQuery.SortDir.ASC);
    queryControl.setMatcher(MockControl.ALWAYS_MATCHER);
    queryControl.setReturnValue(query);
    query.limit(10);
    queryControl.setReturnValue(query);
    replay();

    FileQuery q = classUnderTest.getExcessiveFileQuery();
    verify();
    assertEquals(q, query);
  }

  public void testGetFileCount() {
    classUnderTest.setFileCatalog(catalog);

    catalog.query_file();
    catalogControl.setReturnValue(query);
    query.execute();
    queryControl.setReturnValue(result);
    result.size();
    resultControl.setReturnValue(10);
    replay();

    int result = classUnderTest.getFileCount();
    verify();

    assertEquals(10, result);
  }
}
