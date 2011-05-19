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
package eu.baltrad.beast.db;

import junit.framework.TestCase;

import eu.baltrad.fc.HlFile;
import eu.baltrad.fc.Oh5File;
import eu.baltrad.fc.Oh5FileMatcher;

public class AttributeFilterITest extends TestCase {
  private Oh5File file;
  private Oh5FileMatcher matcher;
  private AttributeFilter filter;

  private String getFilePath(String resource) throws Exception {
    java.io.File f = new java.io.File(this.getClass().getResource(resource).getFile());
    return f.getAbsolutePath();
  }

  protected void setUp() throws Exception {
    file = new HlFile(getFilePath("fixtures/Z_SCAN_C_ESWI_20101023180000_seang_000000.h5"));
    matcher = new Oh5FileMatcher();
    filter = new AttributeFilter();
  }

  public void testObjectEquals() {
    filter.setAttribute("what/object");
    filter.setOperator(AttributeFilter.Operator.EQ);
    filter.setValueType(AttributeFilter.ValueType.STRING);
    filter.setValue("SCAN");

    assertTrue(matcher.match(file, filter.getExpression()));    
  }

  public void testObjectEqualsNegated() {
    filter.setAttribute("what/object");
    filter.setOperator(AttributeFilter.Operator.EQ);
    filter.setValueType(AttributeFilter.ValueType.STRING);
    filter.setValue("SCAN");
    filter.setNegated(true);

    assertFalse(matcher.match(file, filter.getExpression()));
  }

  public void testSourceRADIn() {
    filter.setAttribute("what/source:RAD");
    filter.setOperator(AttributeFilter.Operator.IN);
    filter.setValueType(AttributeFilter.ValueType.STRING);
    filter.setValue("SE50, SE51");
    
    assertTrue(matcher.match(file, filter.getExpression()));
  }

}
