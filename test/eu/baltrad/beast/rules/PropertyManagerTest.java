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
package eu.baltrad.beast.rules;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

public class PropertyManagerTest extends TestCase {
  private PropertyManager classUnderTest = null;
  private MockControl jdbcControl = null;
  private SimpleJdbcOperations jdbc = null;

  protected void setUp() throws Exception {
    classUnderTest = new PropertyManager();
    jdbcControl = MockControl.createControl(SimpleJdbcOperations.class);
    jdbc = (SimpleJdbcOperations)jdbcControl.getMock();
    classUnderTest.setJdbcTemplate(jdbc);
  }

  protected void tearDown() throws Exception {
    jdbc = null;
    jdbcControl = null;
    classUnderTest = null;
  }

  protected void replay() {
    jdbcControl.replay();
  }

  protected void verify() {
    jdbcControl.verify();
  }

  public void testDeleteProperties() throws Exception {
    jdbc.update("delete from beast_rule_properties where rule_id=?", new Object[]{0});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    replay();

    classUnderTest.deleteProperties(0);

    verify();
  }
  
  // XXX: loadProperties() not tested!

  public void testStoreProperties() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("a", "aval");
    props.put("b", "bval");
    String qry = "insert into beast_rule_properties (rule_id, key, value) values (?, ?, ?)";

    jdbc.update(qry, new Object[]{0, "a", "aval"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(1);
    jdbc.update(qry, new Object[]{0, "b", "bval"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(1);
    replay();

    classUnderTest.storeProperties(0, props);
    verify();
  }

  public void testUpdateProperties() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("a", "aval");
    props.put("b", "bval");
    String qry = "insert into beast_rule_properties (rule_id, key, value) values (?, ?, ?)";

    jdbc.update("delete from beast_rule_properties where rule_id=?", new Object[]{0});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(0);
    jdbc.update(qry, new Object[]{0, "a", "aval"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(1);
    jdbc.update(qry, new Object[]{0, "b", "bval"});
    jdbcControl.setMatcher(MockControl.ARRAY_MATCHER);
    jdbcControl.setReturnValue(1);
    replay();

    classUnderTest.updateProperties(0, props);
    verify();
  }
}
