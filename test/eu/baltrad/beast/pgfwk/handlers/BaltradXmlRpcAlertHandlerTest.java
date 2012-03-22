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

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlrpc.XmlRpcRequest;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import eu.baltrad.beast.pgfwk.IAlertPlugin;

/**
 * @author Anders Henja
 */
public class BaltradXmlRpcAlertHandlerTest extends EasyMockSupport {
  @Test
  public void testExecute() throws Exception {
    IAlertPlugin alert1 = createMock(IAlertPlugin.class);
    IAlertPlugin alert2 = createMock(IAlertPlugin.class);
    XmlRpcRequest request = createMock(XmlRpcRequest.class);
    
    List<IAlertPlugin> plugins = new ArrayList<IAlertPlugin>();
    plugins.add(alert1);
    plugins.add(alert2);
    
    expect(request.getParameter(0)).andReturn("E0000");
    expect(request.getParameter(1)).andReturn("message");
    
    alert1.alert("E0000", "message");
    alert2.alert("E0000", "message");
    
    replayAll();
    
    // Execute test
    BaltradXmlRpcAlertHandler classUnderTest = new BaltradXmlRpcAlertHandler();
    classUnderTest.setPlugins(plugins);

    classUnderTest.execute(request);
    
    // verify
    verifyAll();
  }
}
