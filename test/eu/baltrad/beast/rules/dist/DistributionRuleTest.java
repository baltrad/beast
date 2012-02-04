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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.net.FileUploader;

import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.expr.BooleanExpression;
import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.oh5.MetadataMatcher;
import eu.baltrad.bdb.storage.LocalStorage;
import eu.baltrad.bdb.util.FileEntryNamer;

public class DistributionRuleTest extends TestCase {
  private static interface DistributionRuleMethods {
    boolean match(FileEntry entry);
    void upload(FileEntry entry);
  };

  private MockControl methodsControl;
  private DistributionRuleMethods methods;
  private MockControl entryControl;
  private FileEntry entry;
  private MockControl metadataControl;
  private Metadata metadata;
  private MockControl matcherControl;
  private MetadataMatcher matcher;
  private MockControl namerControl;
  private FileEntryNamer namer;
  private MockControl localStorageControl;
  private LocalStorage localStorage;
  private MockControl filterControl;
  private IFilter filter;
  private MockControl uploaderControl;
  private FileUploader uploader;

  private DistributionRule classUnderTest;

  public void setUp() {
    methodsControl = MockControl.createControl(DistributionRuleMethods.class);
    methods = (DistributionRuleMethods)methodsControl.getMock();
    entryControl = MockControl.createControl(FileEntry.class);
    entry = (FileEntry)entryControl.getMock();
    metadataControl = MockClassControl.createControl(Metadata.class);
    metadata = (Metadata)metadataControl.getMock();
    matcherControl = MockClassControl.createControl(MetadataMatcher.class);
    matcher = (MetadataMatcher)matcherControl.getMock();
    namerControl = MockControl.createControl(FileEntryNamer.class);
    namer = (FileEntryNamer)namerControl.getMock();
    localStorageControl = MockControl.createControl(LocalStorage.class);
    localStorage = (LocalStorage)localStorageControl.getMock();
    filterControl = MockControl.createControl(IFilter.class);
    filter = (IFilter)filterControl.getMock();
    uploaderControl = MockClassControl.createControl(FileUploader.class);
    uploader = (FileUploader)uploaderControl.getMock();
    classUnderTest = new DistributionRule(localStorage);
    classUnderTest.setMatcher(matcher);
    classUnderTest.setFilter(filter);
    classUnderTest.setNamer(namer);
  }

  public void tearDown() {

  }

  protected void replay() {
    methodsControl.replay();
    entryControl.replay();
    matcherControl.replay();
    namerControl.replay();
    localStorageControl.replay();
    filterControl.replay();
    metadataControl.replay();
    uploaderControl.replay();
  }

  protected void verify() {
    methodsControl.verify();
    entryControl.verify();
    matcherControl.verify();
    namerControl.verify();
    localStorageControl.verify();
    filterControl.verify();
    metadataControl.verify();
    uploaderControl.verify();
  }

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
    
    methods.match(entry);
    methodsControl.setReturnValue(true);
    methods.upload(entry);

    replay();
    classUnderTest.handle(msg);
    verify();
  }

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
    
    methods.match(entry);
    methodsControl.setReturnValue(false);

    replay();
    classUnderTest.handle(msg);
    verify();
  }

  public void testMatch() {
    Expression expression = new BooleanExpression(true);

    filter.getExpression();
    filterControl.setReturnValue(expression);
    entry.getMetadata();
    entryControl.setReturnValue(metadata);
    matcher.match(metadata, expression);
    matcherControl.setReturnValue(true);
    replay();

    assertTrue(classUnderTest.match(entry));
    verify();
  }

  public void testUpload() throws IOException {
    File file = new File("/path/to/file");
    classUnderTest.setUploader(uploader);
    classUnderTest.setDestination("ftp://u:p@h/d");

    localStorage.store(entry);
    localStorageControl.setReturnValue(file);
    namer.name(entry);
    namerControl.setReturnValue("entryName");
    uploader.appendPath(URI.create("ftp://u:p@h/d"), "entryName");
    uploaderControl.setReturnValue(URI.create("ftp://u:p@h/d/entryName"));
    uploader.upload(file, URI.create("ftp://u:p@h/d/entryName"));
    replay();

    classUnderTest.upload(entry);
    verify();
  }

  public void testGetProperties() {
    classUnderTest.setDestination("scheme:///");
    Map<String, String> props = classUnderTest.getProperties();
    assertEquals("scheme:///", props.get("destination"));
    assertEquals(1, props.size());
  }

  public void testGetProperties_templateNamer() {
    classUnderTest.setDestination("scheme:///");
    classUnderTest.setMetadataNamingTemplate("namingTemplate");
    Map<String, String> props = classUnderTest.getProperties();
    assertEquals("scheme:///", props.get("destination"));
    assertEquals("namingTemplate", props.get("metadataNamingTemplate"));
    assertEquals(2, props.size());
  }

  public void testSetProperties() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("destination", "scheme:///");
    
    classUnderTest.setProperties(props);
    assertEquals(URI.create("scheme:///"), classUnderTest.getDestination());
    assertNull(classUnderTest.getMetadataNamingTemplate());
  }

  public void testSetProperties_templateNamer() {
    Map<String, String> props = new HashMap<String, String>();
    props.put("destination", "scheme:///");
    props.put("metadataNamingTemplate", "namingTemplate");
    
    classUnderTest.setProperties(props);
    assertEquals(URI.create("scheme:///"), classUnderTest.getDestination());
    assertEquals("namingTemplate", classUnderTest.getMetadataNamingTemplate());
  }
}
