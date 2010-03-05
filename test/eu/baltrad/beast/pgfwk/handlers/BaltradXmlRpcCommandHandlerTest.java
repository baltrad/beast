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

import junit.framework.TestCase;

import org.apache.xmlrpc.XmlRpcRequest;
import org.easymock.MockControl;

/**
 * @author Anders Henja
 *
 */
public class BaltradXmlRpcCommandHandlerTest extends TestCase {
  public void testExecute() throws Exception {
    BaltradXmlRpcCommandHandler classUnderTest = new BaltradXmlRpcCommandHandler();
    MockControl requestControl = MockControl.createControl(XmlRpcRequest.class);
    XmlRpcRequest request = (XmlRpcRequest)requestControl.getMock();
    
    request.getParameter(0);
    requestControl.setReturnValue("ls -la");
    
    requestControl.replay();
    
    // Execute
    Object[] result = (Object[])classUnderTest.execute(request);

    // Verify
    requestControl.verify();
    assertEquals(0, result[0]);
    assertFalse(result[1].equals(""));
    assertTrue(result[2].equals(""));
  }
  
  public void testExecute_errStream() throws Exception {
    BaltradXmlRpcCommandHandler classUnderTest = new BaltradXmlRpcCommandHandler();
    MockControl requestControl = MockControl.createControl(XmlRpcRequest.class);
    XmlRpcRequest request = (XmlRpcRequest)requestControl.getMock();
    
    request.getParameter(0);
    requestControl.setReturnValue("ls -la 1>&2");
    
    requestControl.replay();
    
    // Execute
    Object[] result = (Object[])classUnderTest.execute(request);

    // Verify
    requestControl.verify();
    assertTrue(0 != (Integer)result[0]);
    assertTrue(result[1].equals(""));
    assertFalse(result[2].equals(""));
  }
  
  public void testExecute_cmdNotFound() throws Exception {
    BaltradXmlRpcCommandHandler classUnderTest = new BaltradXmlRpcCommandHandler();
    MockControl requestControl = MockControl.createControl(XmlRpcRequest.class);
    XmlRpcRequest request = (XmlRpcRequest)requestControl.getMock();
    
    request.getParameter(0);
    requestControl.setReturnValue("abcDEFghiJKL");
    
    requestControl.replay();
    
    // Execute
    Object[] result = (Object[])classUnderTest.execute(request);

    // Verify
    requestControl.verify();
    assertTrue(0 != (Integer)result[0]);
    assertTrue(result[1].equals(""));
    assertFalse(result[2].equals(""));
  }
}
