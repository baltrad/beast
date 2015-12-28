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
import eu.baltrad.beast.message.mo.BltGetAreasMessage;
import eu.baltrad.beast.message.mo.BltGetPcsDefinitionsMessage;
import eu.baltrad.beast.message.mo.BltGetQualityControlsMessage;
import eu.baltrad.beast.pgf.PgfClientHelper.QCAdaptorCallback;

/**
 * @author Anders Henja
 *
 */
public class PgfClientHelperTest extends EasyMockSupport {
  private interface Methods {
    IAdaptorCallback createQCCallback(String adaptorName, List<QualityControlInformation> qualityControlInformation);
    List<QualityControlInformation> createQCResultList();
    IAdaptorCallback createAreaCallback(String adaptorName, List<AreaInformation> areas);
    List<AreaInformation> createAreaInformationList();
    List<AreaInformation> getAreas(String adaptorName);
    List<AreaInformation> getAreas();
    IAdaptorCallback createPcsDefinitionCallback(String adaptorName, List<PcsDefinition> pcs);
    List<PcsDefinition> createPcsDefinitionList();
    List<PcsDefinition> getPcsDefinitions(String adaptorName);
    List<PcsDefinition> getPcsDefinitions();
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
      protected IAdaptorCallback createQCCallback(String adaptorName, List<QualityControlInformation> qualityControlInformation) {
        return methods.createQCCallback(adaptorName, qualityControlInformation);
      }
      @Override
      protected List<QualityControlInformation> createQCResultList() {
        return methods.createQCResultList();
      }
      @Override
      protected IAdaptorCallback createAreaCallback(String adaptorName, List<AreaInformation> areas) {
        return methods.createAreaCallback(adaptorName, areas);
      }
      @Override
      protected List<AreaInformation> createAreaInformationList() {
        return methods.createAreaInformationList();
      }
      @Override
      protected IAdaptorCallback createPcsDefinitionCallback(String adaptorName, List<PcsDefinition> pcs) {
        return methods.createPcsDefinitionCallback(adaptorName, pcs);
      }
      @Override
      protected List<PcsDefinition> createPcsDefinitionList() {
        return methods.createPcsDefinitionList();
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
    List<String> names = new ArrayList<String>();
    names.add("adaptor1");
    names.add("adaptor2");
    
    expect(adaptorManager.getAdaptorNames()).andReturn(names);
    expect(methods.createQCResultList()).andReturn(qcs);
    expect(adaptorManager.getAdaptor("adaptor1")).andReturn(adaptor1);
    expect(methods.createQCCallback("adaptor1", qcs)).andReturn(cb1);
    adaptor1.handle(anyObject(BltGetQualityControlsMessage.class), same(cb1));
    expect(adaptorManager.getAdaptor("adaptor2")).andReturn(adaptor2);
    expect(methods.createQCCallback("adaptor2", qcs)).andReturn(cb2);
    adaptor2.handle(anyObject(BltGetQualityControlsMessage.class), same(cb2));

    replayAll();
    
    List<QualityControlInformation> result = classUnderTest.getQualityControls();
    
    verifyAll();
    assertSame(qcs, result);
  }

  @Test
  public void testQCAdaptorCallback() {
    List<QualityControlInformation> qcs = new ArrayList<QualityControlInformation>();
    QCAdaptorCallback testedClass = new QCAdaptorCallback("ABC", qcs);
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
  
  @Test 
  public void getAreas_forAdaptorName() {
    List<AreaInformation> areas = new ArrayList<AreaInformation>();
    IAdaptor adaptor = createMock(IAdaptor.class);
    IAdaptorCallback cb = createMock(IAdaptorCallback.class);
    expect(methods.createAreaInformationList()).andReturn(areas);
    expect(adaptorManager.getAdaptor("adaptor1")).andReturn(adaptor);
    expect(methods.createAreaCallback("adaptor1", areas)).andReturn(cb);
    adaptor.handle(anyObject(BltGetAreasMessage.class), same(cb));
    
    replayAll();
    
    List<AreaInformation> result = classUnderTest.getAreas("adaptor1");
    
    verifyAll();
    assertSame(areas, result);
  }
  
  @Test
  public void getAreas() {
    List<AreaInformation> a1 = new ArrayList<AreaInformation>();
    List<AreaInformation> a2 = new ArrayList<AreaInformation>();
    
    List<String> adaptorNames = new ArrayList<String>();
    adaptorNames.add("adaptor1");
    adaptorNames.add("adaptor2");
    
    a1.add(new AreaInformation("area1"));
    a1.add(new AreaInformation("area2"));
    a2.add(new AreaInformation("area3"));
    a2.add(new AreaInformation("area4"));
    
    expect(adaptorManager.getAdaptorNames()).andReturn(adaptorNames);
    expect(methods.getAreas("adaptor1")).andReturn(a1);
    expect(methods.getAreas("adaptor2")).andReturn(a2);
    
    classUnderTest = new PgfClientHelper() {
      @Override
      public List<AreaInformation> getAreas(String adaptorName) {
        return methods.getAreas(adaptorName);
      }
    };
    classUnderTest.setAdaptorManager(adaptorManager);
    
    replayAll();
    
    List<AreaInformation> result = classUnderTest.getAreas();
    
    verifyAll();
    assertEquals(4, result.size());
    assertEquals("area1", result.get(0).getId());
    assertEquals("area2", result.get(1).getId());
    assertEquals("area3", result.get(2).getId());
    assertEquals("area4", result.get(3).getId());
  }
  
  @Test
  public void getUniqueAreaIds() {
    List<AreaInformation> areas = new ArrayList<AreaInformation>();
    areas.add(new AreaInformation("area1"));
    areas.add(new AreaInformation("area2"));
    areas.add(new AreaInformation("area3"));
    areas.add(new AreaInformation("area1"));
    areas.add(new AreaInformation("area4"));
    
    expect(methods.getAreas()).andReturn(areas);
    
    classUnderTest = new PgfClientHelper() {
      @Override
      public List<AreaInformation> getAreas() {
        return methods.getAreas();
      }
    };
    classUnderTest.setAdaptorManager(adaptorManager);
    
    replayAll();
    
    List<String> result = classUnderTest.getUniqueAreaIds();
    
    verifyAll();
    assertEquals(4, result.size());
    assertEquals("area1", result.get(0));
    assertEquals("area2", result.get(1));
    assertEquals("area3", result.get(2));
    assertEquals("area4", result.get(3));
  }
  
  @Test
  public void getUniqueAreaIds_withException() {
    expect(methods.getAreas()).andThrow(new RuntimeException());
    
    classUnderTest = new PgfClientHelper() {
      @Override
      public List<AreaInformation> getAreas() {
        return methods.getAreas();
      }
    };
    classUnderTest.setAdaptorManager(adaptorManager);
    
    replayAll();
    
    List<String> result = classUnderTest.getUniqueAreaIds();
    
    verifyAll();
    assertEquals(0, result.size());
  }
  
  @Test 
  public void getPcsDefinition_forAdaptorName() {
    List<PcsDefinition> pcs = new ArrayList<PcsDefinition>();
    IAdaptor adaptor = createMock(IAdaptor.class);
    IAdaptorCallback cb = createMock(IAdaptorCallback.class);
    expect(methods.createPcsDefinitionList()).andReturn(pcs);
    expect(adaptorManager.getAdaptor("adaptor1")).andReturn(adaptor);
    expect(methods.createPcsDefinitionCallback("adaptor1", pcs)).andReturn(cb);
    adaptor.handle(anyObject(BltGetPcsDefinitionsMessage.class), same(cb));
    
    replayAll();
    
    List<PcsDefinition> result = classUnderTest.getPcsDefinitions("adaptor1");
    
    verifyAll();
    assertSame(pcs, result);
  }
  
  @Test
  public void getPcsDefinitions() {
    List<PcsDefinition> p1 = new ArrayList<PcsDefinition>();
    List<PcsDefinition> p2 = new ArrayList<PcsDefinition>();
    
    List<String> adaptorNames = new ArrayList<String>();
    adaptorNames.add("adaptor1");
    adaptorNames.add("adaptor2");
    
    p1.add(new PcsDefinition("pcs1"));
    p1.add(new PcsDefinition("pcs2"));
    p2.add(new PcsDefinition("pcs3"));
    p2.add(new PcsDefinition("pcs4"));
    
    expect(adaptorManager.getAdaptorNames()).andReturn(adaptorNames);
    expect(methods.getPcsDefinitions("adaptor1")).andReturn(p1);
    expect(methods.getPcsDefinitions("adaptor2")).andReturn(p2);
    
    classUnderTest = new PgfClientHelper() {
      @Override
      public List<PcsDefinition> getPcsDefinitions(String adaptorName) {
        return methods.getPcsDefinitions(adaptorName);
      }
    };
    classUnderTest.setAdaptorManager(adaptorManager);
    
    replayAll();
    
    List<PcsDefinition> result = classUnderTest.getPcsDefinitions();
    
    verifyAll();
    assertEquals(4, result.size());
    assertEquals("pcs1", result.get(0).getId());
    assertEquals("pcs2", result.get(1).getId());
    assertEquals("pcs3", result.get(2).getId());
    assertEquals("pcs4", result.get(3).getId());
  }
  
  @Test
  public void getUniquePcsIds() {
    List<PcsDefinition> pcs = new ArrayList<PcsDefinition>();
    pcs.add(new PcsDefinition("pcs1"));
    pcs.add(new PcsDefinition("pcs2"));
    pcs.add(new PcsDefinition("pcs3"));
    pcs.add(new PcsDefinition("pcs1"));
    pcs.add(new PcsDefinition("pcs4"));
    
    expect(methods.getPcsDefinitions()).andReturn(pcs);
    
    classUnderTest = new PgfClientHelper() {
      @Override
      public List<PcsDefinition> getPcsDefinitions() {
        return methods.getPcsDefinitions();
      }
    };
    classUnderTest.setAdaptorManager(adaptorManager);
    
    replayAll();
    
    List<String> result = classUnderTest.getUniquePcsIds();
    
    verifyAll();
    assertEquals(4, result.size());
    assertEquals("pcs1", result.get(0));
    assertEquals("pcs2", result.get(1));
    assertEquals("pcs3", result.get(2));
    assertEquals("pcs4", result.get(3));
  }
  
  @Test
  public void getUniquePcsIds_withException() {
    expect(methods.getPcsDefinitions()).andThrow(new RuntimeException());
    
    classUnderTest = new PgfClientHelper() {
      @Override
      public List<PcsDefinition> getPcsDefinitions() {
        return methods.getPcsDefinitions();
      }
    };
    classUnderTest.setAdaptorManager(adaptorManager);
    
    replayAll();
    
    List<String> result = classUnderTest.getUniquePcsIds();
    
    verifyAll();
    assertEquals(0, result.size());
  }  
}
