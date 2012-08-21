package eu.baltrad.beast.message;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;

import org.apache.commons.codec.binary.Base64;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.keyczar.Signer;
import org.keyczar.exceptions.KeyczarException;

import eu.baltrad.beast.InitializationException;
import eu.baltrad.beast.message.mo.BltDataFrameMessage;

public class BltDataFrameMessageFactoryTest extends EasyMockSupport {
  private final static String FIXTURE_PRIV_KEY = "fixtures/test-key.priv";
  
  private BltDataFrameMessageFactory classUnderTest = null;
  
  interface MockMethods {
    String createSignature(String method, String url, String contentType, String contentMD5, String date);
    String getDateString();
  };
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new BltDataFrameMessageFactory();
  }

  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }  
  
  @Test
  public void testNodeName() {
    assertEquals(null, classUnderTest.getNodeName());
    classUnderTest.setNodeName("my-name");
    assertEquals("my-name", classUnderTest.getNodeName());
  }

  @Test
  public void testNodeUrl() {
    assertEquals(null, classUnderTest.getNodeUrl());
    classUnderTest.setNodeUrl("my-name");
    assertEquals("my-name", classUnderTest.getNodeUrl());
  }
  
  @Test
  public void testServerUrl() {
    assertEquals(null, classUnderTest.getServerUrl());
    classUnderTest.setServerUrl("my-name");
    assertEquals("my-name", classUnderTest.getServerUrl());
  }

  @Test
  public void testDateFormatString() {
    assertEquals("E, d MMM yyyy HH:mm:ss z", classUnderTest.getDateFormatString());
    classUnderTest.setDateFormatString("MMM yyyy");
    assertEquals("MMM yyyy", classUnderTest.getDateFormatString());
  }

  @Test
  public void testDateFormatString_setNull() {
    try {
      classUnderTest.setDateFormatString(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // pass
    }
  }

  @Test
  public void testAfterPropertiesSet_missingNodeName() throws Exception {
    classUnderTest.setNodeUrl("my-url");
    classUnderTest.setServerUrl("server-url");
    classUnderTest.setKeyczarKey("keyczar-key");
    
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected InitializationError");
    } catch (InitializationException e) {
      // pass
    }
  }
  
  @Test
  public void testAfterPropertiesSet_missingNodeUrl() throws Exception {
    classUnderTest.setNodeName("my-name");
    classUnderTest.setServerUrl("server-url");
    classUnderTest.setKeyczarKey("keyczar-key");
    
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected InitializationError");
    } catch (InitializationException e) {
      // pass
    }
  }  

  @Test
  public void testAfterPropertiesSet_missingServerUrl() throws Exception {
    classUnderTest.setNodeName("my-name");
    classUnderTest.setNodeUrl("my-url");
    classUnderTest.setKeyczarKey("keyczar-key");
    
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected InitializationError");
    } catch (InitializationException e) {
      // pass
    }
  }  

  @Test
  public void testAfterPropertiesSet_missingKeyczarKey() throws Exception {
    classUnderTest.setNodeName("my-name");
    classUnderTest.setNodeUrl("my-url");
    classUnderTest.setServerUrl("server-url");
    
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected InitializationException");
    } catch (InitializationException e) {
      // pass
    }
  }
  
  @Test
  public void testAfterPropertiesSet_badKeyczarKey() throws Exception {
    classUnderTest.setNodeName("my-name");
    classUnderTest.setNodeUrl("my-url");
    classUnderTest.setServerUrl("server-url");
    classUnderTest.setKeyczarKey("nonexisting-key.priv");
    
    try {
      classUnderTest.afterPropertiesSet();
      fail("Expected InitializationException");
    } catch (InitializationException e) {
      // pass
    }
  }

  @Test
  public void testAfterPropertiesSet_keyczarKeyExists() throws Exception {
    classUnderTest.setNodeName("my-name");
    classUnderTest.setNodeUrl("my-url");
    classUnderTest.setServerUrl("server-url");
    classUnderTest.setKeyczarKey(getKeyczarPrivateKey());
    assertNull(classUnderTest.getSigner());
    classUnderTest.afterPropertiesSet();
    assertNotNull(classUnderTest.getSigner());
  }

  @Test
  public void testCreateSignature() throws Exception {
    Signer signer = createMock(Signer.class);
    classUnderTest.setSigner(signer);
    expect(signer.sign("POST\nmyurl\nmycontenttype\nmycontentMD5\nmydate")).andReturn("ABC");
    
    replayAll();
    
    String result = classUnderTest.createSignature("POST", "myurl", "mycontenttype", "mycontentMD5", "mydate");

    verifyAll();
    assertEquals("ABC", result);
  }
  
  @Test
  public void testCreateSignature_failure() throws Exception {
    Signer signer = createMock(Signer.class);
    classUnderTest.setSigner(signer);
    expect(signer.sign("POST\nmyurl\nmycontenttype\nmycontentMD5\nmydate")).andThrow(new KeyczarException(""));
    
    replayAll();

    try {
      classUnderTest.createSignature("POST", "myurl", "mycontenttype", "mycontentMD5", "mydate");
      fail("Expected DataFrameMessageException");
    } catch (DataFrameMessageException e) {
      // pass
    }

    verifyAll();
  }
  
  @Test
  public void testCreateMessage() throws Exception {
    final MockMethods methods = createMock(MockMethods.class);
    classUnderTest = new BltDataFrameMessageFactory() {
      protected String createSignature(String method, String url, String contentType, String contentMD5, String date) {
        return methods.createSignature(method, url, contentType, contentMD5, date);
      }
      protected String getDateString() {
        return methods.getDateString();
      }
    };
    
    classUnderTest.setNodeName("mynodename");
    classUnderTest.setNodeUrl("myurl");
    classUnderTest.setServerUrl("serverurl");
    classUnderTest.setDateFormatString("yyyy");
    
    String encodedString = Base64.encodeBase64String("serverurl".toString().getBytes());
    expect(methods.getDateString()).andReturn("1234");
    expect(methods.createSignature("POST", "serverurl", "application/x-hdf5", encodedString, "1234")).andReturn("ABC");
    
    replayAll();
    
    BltDataFrameMessage result = classUnderTest.createMessage("myfile.h5");
    
    verifyAll();
    assertEquals("mynodename", result.getHeader("Node-Name"));
    assertEquals("myurl", result.getHeader("Node-Address"));
    assertEquals("application/x-hdf5", result.getHeader("Content-Type"));
    assertEquals(encodedString, result.getHeader("Content-MD5"));
    assertEquals("1234", result.getHeader("Date"));
    assertEquals("mynodename:ABC", result.getHeader("Authorization"));
    assertEquals("myfile.h5", result.getFilename());
  }
  
  protected String getKeyczarPrivateKey() {
    File f = new File(this.getClass().getResource(FIXTURE_PRIV_KEY).getFile());
    return f.getAbsolutePath();
  }
}
