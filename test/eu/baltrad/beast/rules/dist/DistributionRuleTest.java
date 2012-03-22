/* --------------------------------------------------------------------
Copyright (C) 2009-2011 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.rules.dist;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.expr.BooleanExpression;
import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.oh5.MetadataMatcher;
import eu.baltrad.bdb.storage.LocalStorage;
import eu.baltrad.bdb.util.FileEntryNamer;
import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.net.FileUploader;

public class DistributionRuleTest extends EasyMockSupport {
  private static interface DistributionRuleMethods {
    boolean match(FileEntry entry);
    void upload(FileEntry entry);
  };

  private DistributionRuleMethods methods;
  private FileEntry entry;
  private Metadata metadata;
  private MetadataMatcher matcher;
  private FileEntryNamer namer;
  private LocalStorage localStorage;
  private IFilter filter;
  private FileUploader uploader;

  private DistributionRule classUnderTest;

  @Before
  public void setUp() {
    methods = createMock(DistributionRuleMethods.class);
    entry = createMock(FileEntry.class);
    metadata = createMock(Metadata.class);
    matcher = createMock(MetadataMatcher.class);
    namer = createMock(FileEntryNamer.class);
    localStorage = createMock(LocalStorage.class);
    filter = createMock(IFilter.class);
    uploader = createMock(FileUploader.class);
    classUnderTest = new DistributionRule(localStorage);
    classUnderTest.setMatcher(matcher);
    classUnderTest.setFilter(filter);
    classUnderTest.setNamer(namer);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testHandle_match() {
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(entry);

    classUnderTest = new DistributionRule(localStorage) {
      @Override
      protected boolean match(FileEntry entry) {
        return methods.match(entry);
      }

      @Override
      protected void upload(FileEntry entry) {
        methods.upload(entry);
      }
    };
    
    expect(methods.match(entry)).andReturn(true);
    methods.upload(entry);

    replayAll();
    
    classUnderTest.handle(msg);
    
    verifyAll();
  }

  @Test
  public void testHandle_noMatch() {
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(entry);

    classUnderTest = new DistributionRule(localStorage) {
      @Override
      protected boolean match(FileEntry entry) {
        return methods.match(entry);
      }

      @Override
      protected void upload(FileEntry entry) {
        methods.upload(entry);
      }
    };
    
    expect(methods.match(entry)).andReturn(false);

    replayAll();
    
    classUnderTest.handle(msg);
    
    verifyAll();
  }

  @Test
  public void testMatch() {
    Expression expression = new BooleanExpression(true);

    expect(filter.getExpression()).andReturn(expression);
    expect(entry.getMetadata()).andReturn(metadata);
    expect(matcher.match(metadata, expression)).andReturn(true);
    replayAll();

    assertTrue(classUnderTest.match(entry));
    
    verifyAll();
  }

  @Test
  public void testUpload() throws IOException {
    File file = new File("/path/to/file");
    classUnderTest.setUploader(uploader);
    classUnderTest.setDestination("ftp://u:p@h/d");

    expect(localStorage.store(entry)).andReturn(file);
    expect(namer.name(entry)).andReturn("entryName");
    expect(uploader.appendPath(URI.create("ftp://u:p@h/d"), "entryName"))
      .andReturn(URI.create("ftp://u:p@h/d/entryName"));
    uploader.upload(file, URI.create("ftp://u:p@h/d/entryName"));
    
    replayAll();

    classUnderTest.upload(entry);
    
    verifyAll();
  }

  @Test
  public void testGetProperties() {
    classUnderTest.setDestination("scheme:///");
    Map<String, String> props = classUnderTest.getProperties();
    assertEquals("scheme:///", props.get("destination"));
    assertEquals(1, props.size());
  }

  @Test
  public void testGetProperties_templateNamer() {
    classUnderTest.setDestination("scheme:///");
    classUnderTest.setMetadataNamingTemplate("namingTemplate");
    Map<String, String> props = classUnderTest.getProperties();
    assertEquals("scheme:///", props.get("destination"));
    assertEquals("namingTemplate", props.get("metadataNamingTemplate"));
    assertEquals(2, props.size());
  }

  @Test
  public void testSetProperties() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("destination", "scheme:///");
    
    classUnderTest.setProperties(props);
    assertEquals(URI.create("scheme:///"), classUnderTest.getDestination());
    assertNull(classUnderTest.getMetadataNamingTemplate());
  }

  @Test
  public void testSetProperties_templateNamer() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("destination", "scheme:///");
    props.put("metadataNamingTemplate", "namingTemplate");
    
    classUnderTest.setProperties(props);
    assertEquals(URI.create("scheme:///"), classUnderTest.getDestination());
    assertEquals("namingTemplate", classUnderTest.getMetadataNamingTemplate());
  }
}
