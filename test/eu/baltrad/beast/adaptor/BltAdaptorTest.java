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
package eu.baltrad.beast.adaptor;

import java.util.HashMap;
import java.util.Map;

import org.easymock.MockControl;

import eu.baltrad.beast.adaptor.AdaptorException;
import eu.baltrad.beast.adaptor.IAdaptor;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.router.Route;

import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class BltAdaptorTest extends TestCase {
  public void testHandle() throws Exception {
    MockControl adaptor1Control = MockControl.createControl(IAdaptor.class);
    IAdaptor adaptor1 = (IAdaptor)adaptor1Control.getMock();
    MockControl adaptor2Control = MockControl.createControl(IAdaptor.class);
    IAdaptor adaptor2 = (IAdaptor)adaptor2Control.getMock();
    Map<String, IAdaptor> adaptors = new HashMap<String, IAdaptor>();
    adaptors.put("A1", adaptor1);
    adaptors.put("A2", adaptor2);
    
    IBltMessage msg = new IBltMessage() {};
    Route r = new Route("A2", msg);
    
    BltAdaptor classUnderTest = new BltAdaptor();
    classUnderTest.setAdaptors(adaptors);

    adaptor2.handle(r);

    adaptor1Control.replay();
    adaptor2Control.replay();
    
    // execute test
    classUnderTest.handle(r);
    
    // verify
    adaptor1Control.verify();
    adaptor2Control.verify();
  }

  public void testHandle_noMatchingAdaptor() throws Exception {
    Map<String, IAdaptor> adaptors = new HashMap<String, IAdaptor>();
    
    IBltMessage msg = new IBltMessage() {};
    Route r = new Route("A2", msg);
    
    BltAdaptor classUnderTest = new BltAdaptor();
    classUnderTest.setAdaptors(adaptors);

    // execute test
    try {
      classUnderTest.handle(r);
      fail("Expected AdaptorException");
    } catch (AdaptorException e) {
      // pass
    }
    
    // verify
  }
}
