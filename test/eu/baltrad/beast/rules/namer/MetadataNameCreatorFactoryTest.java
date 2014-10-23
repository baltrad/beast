package eu.baltrad.beast.rules.namer;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockSupport;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MetadataNameCreatorFactoryTest extends EasyMockSupport {
  private MetadataNameCreatorFactory classUnderTest = null;
  private List<MetadataNameCreator> creators = null;
  private MetadataNameCreator abcCreator;
  private MetadataNameCreator defCreator;
  
  @Before
  public void setUp() throws Exception {
    abcCreator = createMock(MetadataNameCreator.class);
    defCreator = createMock(MetadataNameCreator.class);
    creators = new ArrayList<MetadataNameCreator>();
    creators.add(abcCreator);
    creators.add(defCreator);
    classUnderTest = new MetadataNameCreatorFactory();
    classUnderTest.setCreators(creators);
  }

  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
    abcCreator = null;
    defCreator = null;
    creators = null;
  }
  
  @Test
  public void support_1() {
    expect(abcCreator.supports("abc")).andReturn(true);
    
    replayAll();

    boolean result = classUnderTest.supports("abc");
    
    verifyAll();
    assertEquals(true, result);
  }

  @Test
  public void support_2() {
    expect(abcCreator.supports("def")).andReturn(false);
    expect(defCreator.supports("def")).andReturn(true);
    
    replayAll();

    boolean result = classUnderTest.supports("def");
    
    verifyAll();
    assertEquals(true, result);
  }

  @Test
  public void support_3() {
    expect(abcCreator.supports("ghi")).andReturn(false);
    expect(defCreator.supports("ghi")).andReturn(false);
    
    replayAll();

    boolean result = classUnderTest.supports("ghi");
    
    verifyAll();
    assertEquals(false, result);
  }

  @Test
  public void get_1() {
    expect(abcCreator.supports("abc")).andReturn(true);
    
    replayAll();
    
    MetadataNameCreator result = classUnderTest.get("abc");
    
    verifyAll();
    assertSame(abcCreator, result);
  }
  
  @Test
  public void get_2() {
    expect(abcCreator.supports("def")).andReturn(false);
    expect(defCreator.supports("def")).andReturn(true);
    
    replayAll();
    
    MetadataNameCreator result = classUnderTest.get("def");
    
    verifyAll();
    assertSame(defCreator, result);
  }

  @Test
  public void get_3() {
    expect(abcCreator.supports("ghi")).andReturn(false);
    expect(defCreator.supports("ghi")).andReturn(false);
    
    replayAll();
    
    MetadataNameCreator result = classUnderTest.get("ghi");
    
    verifyAll();
    assertNull(result);
  }

}
