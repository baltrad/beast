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
import java.util.concurrent.ExecutorService;

import org.easymock.Capture;
import org.easymock.EasyMock;
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
import eu.baltrad.beast.net.FileDistribution;

public class DistributionRuleTest extends EasyMockSupport {
  private static interface DistributionRuleMethods {
    boolean match(FileEntry entry);
    void upload(FileEntry entry);
    void warnAboutOngoingUpload(File src, URI fullDestination);
  };

  private DistributionRuleMethods methods;
  private FileEntry entry;
  private Metadata metadata;
  private MetadataMatcher matcher;
  private FileEntryNamer namer;
  private LocalStorage localStorage;
  private IFilter filter;
  private ExecutorService distributionExecutor;
  
  private String dstBase;
  private String dstEntryName;
  private String dst;
  private File src;
  private URI destination;

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
    distributionExecutor = createMock(ExecutorService.class);

    classUnderTest = new DistributionRule(localStorage, distributionExecutor);
    classUnderTest.setMatcher(matcher);
    classUnderTest.setFilter(filter);
    classUnderTest.setNamer(namer);
    
    dstBase = "ftp://u:p@h/d";
    dstEntryName = "entryName";
    dst = dstBase + "/" + dstEntryName;
    
    src = new File("/path/to/file");
    destination = URI.create(dst);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testHandle_match() {
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(entry);

    classUnderTest = new DistributionRule(localStorage, distributionExecutor) {
      @Override
      public boolean match(FileEntry entry) {
        return methods.match(entry);
      }

      @Override
      public void upload(FileEntry entry) {
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

    classUnderTest = new DistributionRule(localStorage, distributionExecutor) {
      @Override
      public boolean match(FileEntry entry) {
        return methods.match(entry);
      }

      @Override
      public void upload(FileEntry entry) {
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
    classUnderTest.setDestination(dstBase);

    expect(localStorage.store(entry)).andReturn(src);
    expect(namer.name(entry)).andReturn(dstEntryName);
    expect(distributionExecutor.isShutdown()).andReturn(false);

    Capture<FileDistribution> capturedArgument = new Capture<FileDistribution>();
    distributionExecutor.execute(EasyMock.capture(capturedArgument));
    
    replayAll();

    classUnderTest.upload(entry);
    
    verifyAll();
    
    FileDistribution fileDistribution = capturedArgument.getValue();
    assertEquals(src, fileDistribution.getSourceFile());
    assertEquals(destination, fileDistribution.getFullDestination());
    
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
