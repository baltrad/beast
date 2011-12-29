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

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import eu.baltrad.beast.db.IFilter;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.net.FileUploader;

import eu.baltrad.bdb.expr.Expression;
import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.oh5.MetadataMatcher;

public class DistributionRuleTest extends TestCase {
  private static interface DistributionRuleMethods {
    boolean match(FileEntry entry);
    void upload(FileEntry entry);
    File entryToFile(FileEntry entry);
    String name(FileEntry entry);
  };

  private MockControl methodsControl;
  private DistributionRuleMethods methods;
  private MockControl entryControl;
  private FileEntry entry;
  private MockControl expressionControl;
  private Expression expression;
  private MockControl metadataControl;
  private Metadata metadata;
  private MockControl matcherControl;
  private MetadataMatcher matcher;
  private MockControl filterControl;
  private IFilter filter;
  private MockControl fileControl;
  private File file;
  private MockControl uploaderControl;
  private FileUploader uploader;

  private DistributionRule classUnderTest;

  public void setUp() {
    methodsControl = MockControl.createControl(DistributionRuleMethods.class);
    methods = (DistributionRuleMethods)methodsControl.getMock();
    entryControl = MockClassControl.createControl(FileEntry.class);
    entry = (FileEntry)entryControl.getMock();
    expressionControl = MockControl.createControl(Expression.class);
    expression = (Expression)expressionControl.getMock();
    metadataControl = MockClassControl.createControl(Metadata.class);
    metadata = (Metadata)metadataControl.getMock();
    matcherControl = MockClassControl.createControl(MetadataMatcher.class);
    matcher = (MetadataMatcher)matcherControl.getMock();
    filterControl = MockControl.createControl(IFilter.class);
    filter = (IFilter)filterControl.getMock();
    fileControl = MockClassControl.createControl(File.class);
    file = (File)fileControl.getMock();
    uploaderControl = MockClassControl.createControl(FileUploader.class);
    uploader = (FileUploader)uploaderControl.getMock();
    classUnderTest = new DistributionRule();
    classUnderTest.setMatcher(matcher);
    classUnderTest.setFilter(filter);
  }

  public void tearDown() {

  }

  protected void replay() {
    methodsControl.replay();
    entryControl.replay();
    matcherControl.replay();
    filterControl.replay();
    fileControl.replay();
    metadataControl.replay();
    uploaderControl.replay();
  }

  protected void verify() {
    methodsControl.verify();
    entryControl.verify();
    matcherControl.verify();
    filterControl.verify();
    fileControl.verify();
    metadataControl.verify();
    uploaderControl.verify();
  }

  public void testHandle_match() {
    BltDataMessage msg = new BltDataMessage();
    msg.setFileEntry(entry);

    classUnderTest = new DistributionRule() {
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

    classUnderTest = new DistributionRule() {
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
    classUnderTest = new DistributionRule() {
      @Override
      protected File entryToFile(FileEntry entry) {
        return methods.entryToFile(entry);
      }
    };
    classUnderTest.setUploader(uploader);
    classUnderTest.setDestination("ftp://u:p@h/d");

    methods.entryToFile(entry);
    methodsControl.setReturnValue(file);
    uploader.upload(file, URI.create("ftp://u:p@h/d"));
    file.delete();
    fileControl.setReturnValue(true);
    replay();

    classUnderTest.upload(entry);
    verify();
  }
  
  @SuppressWarnings("deprecation")
  public void testEntryToFile() {
    classUnderTest = new DistributionRule() {
      @Override
      protected String name(FileEntry entry) {
        return methods.name(entry);
      }
    };

    File f = new File(System.getProperty("java.io.tmpdir"), "name");

    methods.name(entry);
    methodsControl.setReturnValue("name");
    entry.writeToFile(f); // XXX: deprecated
    replay();

    File result = classUnderTest.entryToFile(entry);
    assertEquals(result, f);
    verify();
  }
}
