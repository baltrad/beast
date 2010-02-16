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
package eu.baltrad.beast.manager;

import java.util.ArrayList;
import java.util.List;

import org.easymock.MockControl;

import eu.baltrad.beast.adaptor.IAdaptor;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.router.IRouter;
import eu.baltrad.beast.router.Route;
import junit.framework.TestCase;


/**
 * @author Anders Henja
 */
public class BltMessageManagerTest extends TestCase {
  public void testManage() throws Exception {
    MockControl routerControl = MockControl.createControl(IRouter.class);
    IRouter router = (IRouter)routerControl.getMock();
    MockControl adaptorControl = MockControl.createControl(IAdaptor.class);
    IAdaptor adaptor = (IAdaptor)adaptorControl.getMock();
    
    IBltMessage message = new IBltMessage() { };
    List<Route> routes = new ArrayList<Route>();
    Route r1 = new Route();
    Route r2 = new Route();
    routes.add(r1);
    routes.add(r2);
    
    // the mocking sequence
    router.getRoutes(message);
    routerControl.setReturnValue(routes);
    adaptor.handle(r1);
    adaptor.handle(r2);
    
    BltMessageManager classUnderTest = new BltMessageManager();
    classUnderTest.setRouter(router);
    classUnderTest.setAdaptor(adaptor);

    routerControl.replay();
    adaptorControl.replay();
    
    // execute test
    classUnderTest.manage(message);
    
    // verify
    routerControl.verify();
    adaptorControl.verify();
  }
  
  public void testManage_exceptionInSequence() throws Exception {
    MockControl routerControl = MockControl.createControl(IRouter.class);
    IRouter router = (IRouter)routerControl.getMock();
    MockControl adaptorControl = MockControl.createControl(IAdaptor.class);
    IAdaptor adaptor = (IAdaptor)adaptorControl.getMock();
    
    IBltMessage message = new IBltMessage() { };
    List<Route> routes = new ArrayList<Route>();
    Route r1 = new Route();
    Route r2 = new Route();
    routes.add(r1);
    routes.add(r2);
    
    // the mocking sequence
    router.getRoutes(message);
    routerControl.setReturnValue(routes);
    adaptor.handle(r1);
    adaptorControl.setThrowable(new RuntimeException());
    adaptor.handle(r2);
    
    BltMessageManager classUnderTest = new BltMessageManager();
    classUnderTest.setRouter(router);
    classUnderTest.setAdaptor(adaptor);

    routerControl.replay();
    adaptorControl.replay();
    
    // execute test
    classUnderTest.manage(message);
    
    // verify
    routerControl.verify();
    adaptorControl.verify();
  }

}
