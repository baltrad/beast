/**
 * 
 */
package eu.baltrad.beast.admin.objects;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.admin.objects.routes.CompositeRoute;

import static org.junit.Assert.assertEquals;

/**
 * @author anders
 *
 */
public class CompositeRouteTest extends EasyMockSupport {
  private CompositeRoute classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new CompositeRoute();
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }
  
  @Test
  public void isValid() throws Exception {
    classUnderTest.setName("N");
    classUnderTest.setArea("A");
    classUnderTest.getSources().add("S1");
    classUnderTest.getRecipients().add("R1");
    classUnderTest.getDetectors().add("D1");
    classUnderTest.setProdpar("500.0");
    classUnderTest.setMethod(CompositeRoute.PPI);
    classUnderTest.setQualityControlMode(CompositeRoute.QualityControlMode_ANALYZE);
    classUnderTest.setQuantity("DBZH");
    classUnderTest.setSelectionMethod(CompositeRoute.Selection_NEAREST_RADAR);

    boolean result = classUnderTest.isValid();
    
    assertEquals(true, result);
  }
 
  @Test
  public void isValid_missingName() throws Exception {
    //classUnderTest.setName("N");
    classUnderTest.setArea("A");
    classUnderTest.getSources().add("S1");
    classUnderTest.getRecipients().add("R1");
    classUnderTest.getDetectors().add("D1");
    classUnderTest.setProdpar("500.0");
    classUnderTest.setMethod(CompositeRoute.PPI);
    classUnderTest.setQualityControlMode(CompositeRoute.QualityControlMode_ANALYZE);
    classUnderTest.setQuantity("DBZH");
    classUnderTest.setSelectionMethod(CompositeRoute.Selection_NEAREST_RADAR);

    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }
  
  @Test
  public void isValid_missingArea() throws Exception {
    classUnderTest.setName("N");
    //classUnderTest.setArea("A");
    classUnderTest.getSources().add("S1");
    classUnderTest.getRecipients().add("R1");
    classUnderTest.getDetectors().add("D1");
    classUnderTest.setProdpar("500.0");
    classUnderTest.setMethod(CompositeRoute.PPI);
    classUnderTest.setQualityControlMode(CompositeRoute.QualityControlMode_ANALYZE);
    classUnderTest.setQuantity("DBZH");
    classUnderTest.setSelectionMethod(CompositeRoute.Selection_NEAREST_RADAR);

    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }
  
  @Test
  public void isValid_missingSources() throws Exception {
    classUnderTest.setName("N");
    classUnderTest.setArea("A");
    //classUnderTest.getSources().add("S1");
    classUnderTest.getRecipients().add("R1");
    classUnderTest.getDetectors().add("D1");
    classUnderTest.setProdpar("500.0");
    classUnderTest.setMethod(CompositeRoute.PPI);
    classUnderTest.setQualityControlMode(CompositeRoute.QualityControlMode_ANALYZE);
    classUnderTest.setQuantity("DBZH");
    classUnderTest.setSelectionMethod(CompositeRoute.Selection_NEAREST_RADAR);

    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }
  
  @Test
  public void isValid_missingRecipients() throws Exception {
    classUnderTest.setName("N");
    classUnderTest.setArea("A");
    classUnderTest.getSources().add("S1");
    //classUnderTest.getRecipients().add("R1");
    classUnderTest.getDetectors().add("D1");
    classUnderTest.setProdpar("500.0");
    classUnderTest.setMethod(CompositeRoute.PPI);
    classUnderTest.setQualityControlMode(CompositeRoute.QualityControlMode_ANALYZE);
    classUnderTest.setQuantity("DBZH");
    classUnderTest.setSelectionMethod(CompositeRoute.Selection_NEAREST_RADAR);

    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }
  
  @Test
  public void isValid_prodparPmax() throws Exception {
    classUnderTest.setName("N");
    classUnderTest.setArea("A");
    classUnderTest.getSources().add("S1");
    classUnderTest.getRecipients().add("R1");
    classUnderTest.getDetectors().add("D1");
    classUnderTest.setMethod(CompositeRoute.PMAX);
    classUnderTest.setProdpar("500.0,2000.0"); // Height range
    classUnderTest.setQualityControlMode(CompositeRoute.QualityControlMode_ANALYZE);
    classUnderTest.setQuantity("DBZH");
    classUnderTest.setSelectionMethod(CompositeRoute.Selection_NEAREST_RADAR);

    boolean result = classUnderTest.isValid();
    
    assertEquals(true, result);
  }
  
  @Test
  public void isValid_prodparPmax_onlyRange() throws Exception {
    classUnderTest.setName("N");
    classUnderTest.setArea("A");
    classUnderTest.getSources().add("S1");
    classUnderTest.getRecipients().add("R1");
    classUnderTest.getDetectors().add("D1");
    classUnderTest.setMethod(CompositeRoute.PMAX);
    classUnderTest.setProdpar(",2000.0"); // Height range
    classUnderTest.setQualityControlMode(CompositeRoute.QualityControlMode_ANALYZE);
    classUnderTest.setQuantity("DBZH");
    classUnderTest.setSelectionMethod(CompositeRoute.Selection_NEAREST_RADAR);

    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }

  @Test
  public void isValid_prodparPmax_onlyHeight() throws Exception {
    classUnderTest.setName("N");
    classUnderTest.setArea("A");
    classUnderTest.getSources().add("S1");
    classUnderTest.getRecipients().add("R1");
    classUnderTest.getDetectors().add("D1");
    classUnderTest.setMethod(CompositeRoute.PMAX);
    classUnderTest.setProdpar("500.0,"); // Height range
    classUnderTest.setQualityControlMode(CompositeRoute.QualityControlMode_ANALYZE);
    classUnderTest.setQuantity("DBZH");
    classUnderTest.setSelectionMethod(CompositeRoute.Selection_NEAREST_RADAR);

    boolean result = classUnderTest.isValid();
    
    assertEquals(true, result);
  }

  @Test
  public void isValid_prodparAny() throws Exception {
    classUnderTest.setName("N");
    classUnderTest.setArea("A");
    classUnderTest.getSources().add("S1");
    classUnderTest.getRecipients().add("R1");
    classUnderTest.getDetectors().add("D1");
    classUnderTest.setMethod(CompositeRoute.PCAPPI);
    classUnderTest.setProdpar("500.0"); // Height range
    classUnderTest.setQualityControlMode(CompositeRoute.QualityControlMode_ANALYZE);
    classUnderTest.setQuantity("DBZH");
    classUnderTest.setSelectionMethod(CompositeRoute.Selection_NEAREST_RADAR);

    boolean result = classUnderTest.isValid();
    
    assertEquals(true, result);
  }
  
  @Test
  public void isValid_prodparAny_invalid() throws Exception {
    classUnderTest.setName("N");
    classUnderTest.setArea("A");
    classUnderTest.getSources().add("S1");
    classUnderTest.getRecipients().add("R1");
    classUnderTest.getDetectors().add("D1");
    classUnderTest.setMethod(CompositeRoute.PCAPPI);
    classUnderTest.setProdpar("X"); // Height range
    classUnderTest.setQualityControlMode(CompositeRoute.QualityControlMode_ANALYZE);
    classUnderTest.setQuantity("DBZH");
    classUnderTest.setSelectionMethod(CompositeRoute.Selection_NEAREST_RADAR);

    boolean result = classUnderTest.isValid();
    
    assertEquals(false, result);
  }
}
