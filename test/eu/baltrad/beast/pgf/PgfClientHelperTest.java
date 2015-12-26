/* --------------------------------------------------------------------
Copyright (C) 2009-2013 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.pgf;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import static org.easymock.EasyMock.*;

import eu.baltrad.beast.adaptor.IAdaptor;
import eu.baltrad.beast.adaptor.IAdaptorCallback;
import eu.baltrad.beast.adaptor.IBltAdaptorManager;
import eu.baltrad.beast.message.mo.BltGetQualityControlsMessage;
import eu.baltrad.beast.pgf.PgfClientHelper.AdaptorCallback;

/**
 * @author Anders Henja
 *
 */
public class PgfClientHelperTest extends EasyMockSupport {
  private interface Methods {
    IAdaptorCallback createCallback(String adaptorName, List<QualityControlInformation> qualityControlInformation);
    List<QualityControlInformation> createResultList();
  }
  private PgfClientHelper classUnderTest = null;
  private IBltAdaptorManager adaptorManager = null;
  private Methods methods = null;
  
  @Before
  public void setup() throws Exception {
    adaptorManager = createMock(IBltAdaptorManager.class);
    methods = createMock(Methods.class);
    
    classUnderTest = new PgfClientHelper() {
      @Override
      protected IAdaptorCallback createCallback(String adaptorName, List<QualityControlInformation> qualityControlInformation) {
        return methods.createCallback(adaptorName, qualityControlInformation);
      }
      @Override
      protected List<QualityControlInformation> createResultList() {
        return methods.createResultList();
      }
    };
    classUnderTest.setAdaptorManager(adaptorManager);
  }
  
  @Test
  public void getQualityControls() {
    IAdaptor adaptor1 = createMock(IAdaptor.class);
    IAdaptor adaptor2 = createMock(IAdaptor.class);
    IAdaptorCallback cb1 = createMock(IAdaptorCallback.class);
    IAdaptorCallback cb2 = createMock(IAdaptorCallback.class);
    
    List<QualityControlInformation> qcs = new ArrayList<QualityControlInformation>();
    BltGetQualityControlsMessage bltmsg = new BltGetQualityControlsMessage();
    List<String> names = new ArrayList<String>();
    names.add("adaptor1");
    names.add("adaptor2");
    
    expect(adaptorManager.getAdaptorNames()).andReturn(names);
    expect(methods.createResultList()).andReturn(qcs);
    expect(adaptorManager.getAdaptor("adaptor1")).andReturn(adaptor1);
    expect(methods.createCallback("adaptor1", qcs)).andReturn(cb1);
    adaptor1.handle(anyObject(BltGetQualityControlsMessage.class), same(cb1));
    expect(adaptorManager.getAdaptor("adaptor2")).andReturn(adaptor2);
    expect(methods.createCallback("adaptor2", qcs)).andReturn(cb2);
    adaptor2.handle(anyObject(BltGetQualityControlsMessage.class), same(cb2));

    replayAll();
    
    List<QualityControlInformation> result = classUnderTest.getQualityControls();
    
    verifyAll();
    assertSame(qcs, result);
  }

  @Test
  public void testAdaptorCallback() {
    List<QualityControlInformation> qcs = new ArrayList<QualityControlInformation>();
    AdaptorCallback testedClass = new AdaptorCallback("ABC", qcs);
    testedClass.success(new BltGetQualityControlsMessage(), new Object[][]{{"abc","abc description"},{"def","def description"}});
    assertEquals(2, qcs.size());
    QualityControlInformation qc1 = qcs.get(0);
    QualityControlInformation qc2 = qcs.get(1);
    assertEquals("ABC", qc1.getAdaptorName());
    assertEquals("abc", qc1.getName());
    assertEquals("abc description", qc1.getDescription());
    assertEquals("ABC", qc2.getAdaptorName());
    assertEquals("def", qc2.getName());
    assertEquals("def description", qc2.getDescription());
  }
}
