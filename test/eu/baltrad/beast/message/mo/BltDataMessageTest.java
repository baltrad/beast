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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import eu.baltrad.bdb.db.FileEntry;

/**
 * @author Anders Henja
 *
 */
public class BltDataMessageTest extends EasyMockSupport {
  @Test
  public void testSetFile() {
    FileEntry mockEntry = createMock(FileEntry.class);
    BltDataMessage classUnderTest = new BltDataMessage();
    assertEquals(null, classUnderTest.getFileEntry());
    classUnderTest.setFileEntry(mockEntry);
    assertSame(mockEntry, classUnderTest.getFileEntry());
  }
}
