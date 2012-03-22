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
package eu.baltrad.beast.pgfwk.handlers;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.xmlrpc.XmlRpcRequest;
import org.easymock.EasyMockSupport;
import org.junit.Test;

/**
 * @author Anders Henja
 *
 */
public class BaltradXmlRpcCommandHandlerTest extends EasyMockSupport {
  @Test
  public void testExecute() throws Exception {
    BaltradXmlRpcCommandHandler classUnderTest = new BaltradXmlRpcCommandHandler();
    XmlRpcRequest request = createMock(XmlRpcRequest.class);
    
    expect(request.getParameter(0)).andReturn("ls -la");
    
    replayAll();
    
    // Execute
    Object[] result = (Object[])classUnderTest.execute(request);

    // Verify
    verifyAll();
    assertEquals(0, result[0]);
    assertFalse(result[1].equals(""));
    assertTrue(result[2].equals(""));
  }
  
  @Test
  public void testExecute_errStream() throws Exception {
    BaltradXmlRpcCommandHandler classUnderTest = new BaltradXmlRpcCommandHandler();
    XmlRpcRequest request = createMock(XmlRpcRequest.class);
    
    expect(request.getParameter(0)).andReturn("ls -la 1>&2");
    
    replayAll();
    
    // Execute
    Object[] result = (Object[])classUnderTest.execute(request);

    // Verify
    verifyAll();
    assertTrue(0 != (Integer)result[0]);
    assertTrue(result[1].equals(""));
    assertFalse(result[2].equals(""));
  }
  
  @Test
  public void testExecute_cmdNotFound() throws Exception {
    BaltradXmlRpcCommandHandler classUnderTest = new BaltradXmlRpcCommandHandler();
    XmlRpcRequest request = createMock(XmlRpcRequest.class);
    
    expect(request.getParameter(0)).andReturn("abcDEFghiJKL");
    
    replayAll();
    
    // Execute
    Object[] result = (Object[])classUnderTest.execute(request);

    // Verify
    verifyAll();
    assertTrue(0 != (Integer)result[0]);
    assertTrue(result[1].equals(""));
    assertFalse(result[2].equals(""));
  }
}
