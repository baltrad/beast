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
package eu.baltrad.beast.message.mo;

import java.util.ArrayList;
import java.util.List;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.router.IMultiRoutedMessage;
import junit.framework.TestCase;

/**
 * @author Anders Henja
 *
 */
public class BltMultiRoutedMessageTest extends TestCase {
  private BltMultiRoutedMessage classUnderTest = null;
  
  public void setUp() throws Exception {
    super.setUp();
    classUnderTest = new BltMultiRoutedMessage();
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
    classUnderTest = null;
  }

  public void testIsRoutedMessage() throws Exception {
    assertTrue(classUnderTest instanceof IMultiRoutedMessage);
  }
  
  public void testMessage() {
    IBltMessage msg = new IBltMessage() { };
    classUnderTest.setMessage(msg);
    assertSame(msg, classUnderTest.getMessage());
  }
  
  public void testDestinations() {
    List<String> destinations = new ArrayList<String>();
    classUnderTest.setDestinations(destinations);
    assertSame(destinations, classUnderTest.getDestinations());
  }
}