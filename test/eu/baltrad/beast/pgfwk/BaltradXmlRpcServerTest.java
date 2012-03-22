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

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author Anders Henja
 */
public class BaltradXmlRpcServerTest {
  @Test
  public void testCreateServerFromArguments_illegalArgument() {
    try {
      String args[] = new String[]{"--notsupported=xyz", "--port=1234"};
      BaltradXmlRpcServer.createServerFromArguments(args);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }
  
  @Test
  public void testCreateServerFromArguments_badPort() {
    try {
      String args[] = new String[]{"--port=10"};
      BaltradXmlRpcServer.createServerFromArguments(args);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }
}
