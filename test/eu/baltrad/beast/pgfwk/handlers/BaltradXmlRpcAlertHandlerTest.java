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

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlrpc.XmlRpcRequest;
import org.easymock.MockControl;

import eu.baltrad.beast.pgfwk.IAlertPlugin;

import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class BaltradXmlRpcAlertHandlerTest extends TestCase {
  public void testExecute() throws Exception {
    MockControl alert1Control = MockControl.createControl(IAlertPlugin.class);
    IAlertPlugin alert1 = (IAlertPlugin)alert1Control.getMock();
    MockControl alert2Control = MockControl.createControl(IAlertPlugin.class);
    IAlertPlugin alert2 = (IAlertPlugin)alert2Control.getMock();
    MockControl requestControl = MockControl.createControl(XmlRpcRequest.class);
    XmlRpcRequest request = (XmlRpcRequest)requestControl.getMock();
    
    List<IAlertPlugin> plugins = new ArrayList<IAlertPlugin>();
    plugins.add(alert1);
    plugins.add(alert2);
    
    request.getParameter(0);
    requestControl.setReturnValue("E0000");
    request.getParameter(1);
    requestControl.setReturnValue("message");
    
    alert1.alert("E0000", "message");
    alert2.alert("E0000", "message");
    
    alert1Control.replay();
    alert2Control.replay();
    requestControl.replay();
    
    // Execute test
    BaltradXmlRpcAlertHandler classUnderTest = new BaltradXmlRpcAlertHandler();
    classUnderTest.setPlugins(plugins);

    classUnderTest.execute(request);
    
    // verify
    alert1Control.verify();
    alert2Control.verify();
    requestControl.verify();
  }
}
