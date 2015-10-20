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

import static org.easymock.EasyMock.expect;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcOperations;

public class PropertyManagerTest extends EasyMockSupport {
  private PropertyManager classUnderTest = null;
  private JdbcOperations jdbc = null;

  @Before
  public void setUp() throws Exception {
    classUnderTest = new PropertyManager();
    jdbc = createMock(JdbcOperations.class);
    classUnderTest.setJdbcTemplate(jdbc);
  }

  @After
  public void tearDown() throws Exception {
    jdbc = null;
    classUnderTest = null;
  }

  @Test
  public void testDeleteProperties() throws Exception {
    expect(jdbc.update("delete from beast_rule_properties where rule_id=?", new Object[]{0}))
      .andReturn(0);

    replayAll();

    classUnderTest.deleteProperties(0);

    verifyAll();
  }

  @Test
  public void testStoreProperties() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("a", "aval");
    props.put("b", "bval");
    String qry = "insert into beast_rule_properties (rule_id, key, value) values (?, ?, ?)";

    expect(jdbc.update(qry, new Object[]{0, "a", "aval"})).andReturn(1);
    expect(jdbc.update(qry, new Object[]{0, "b", "bval"})).andReturn(1);
    
    replayAll();

    classUnderTest.storeProperties(0, props);
    
    verifyAll();
  }

  @Test
  public void testUpdateProperties() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("a", "aval");
    props.put("b", "bval");
    String qry = "insert into beast_rule_properties (rule_id, key, value) values (?, ?, ?)";

    expect(jdbc.update("delete from beast_rule_properties where rule_id=?", new Object[]{0}))
      .andReturn(0);
    
    expect(jdbc.update(qry, new Object[]{0, "a", "aval"})).andReturn(1);
    expect(jdbc.update(qry, new Object[]{0, "b", "bval"})).andReturn(1);

    replayAll();

    classUnderTest.updateProperties(0, props);
    
    verifyAll();
  }
}
