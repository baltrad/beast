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
package eu.baltrad.beast.pgfwk;

import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class BaltradXmlRpcServerTest extends TestCase {
  public void testGetContextUriFromArguments_noArg() throws Exception {
    String result = BaltradXmlRpcServer.getContextUriFromArguments(new String[0]);
    assertEquals("classpath:etc/xmlrpcserver-context.xml", result);
  }

  public void testGetContextUriFromArguments_oneArg() throws Exception {
    String[] args = new String[]{"file:/x/y/z"};
    String result = BaltradXmlRpcServer.getContextUriFromArguments(args);
    assertEquals("file:/x/y/z", result);
  }

  public void testGetContextUriFromArguments_twoArg() throws Exception {
    String[] args = new String[]{"file:/x/y/z", "abc"};
    try {
      BaltradXmlRpcServer.getContextUriFromArguments(args);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }
  
}
