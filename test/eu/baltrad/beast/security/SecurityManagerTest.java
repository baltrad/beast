/**
 * 
 */
package eu.baltrad.beast.security;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.keyczar.exceptions.KeyczarException;

import eu.baltrad.beast.security.crypto.CryptoFactory;
import eu.baltrad.beast.security.crypto.Signer;

/**
 * @author anders
 *
 */
public class SecurityManagerTest extends EasyMockSupport {
  private IAuthorizationManager authorizationManager;
  private IAuthorizationRequestManager authorizationRequestManager;
  private SecurityManager classUnderTest;
  private CryptoFactory cryptoFactory;
  
  @Before
  public void setUp() throws Exception {
    classUnderTest = new SecurityManager();
    authorizationManager = createMock(IAuthorizationManager.class);
    authorizationRequestManager = createMock(IAuthorizationRequestManager.class);
    cryptoFactory = createMock(CryptoFactory.class);
    classUnderTest.setAuthorizationManager(authorizationManager);
    classUnderTest.setAuthorizationRequestManager(authorizationRequestManager);
    classUnderTest.setCryptoFactory(cryptoFactory);
//    classUnderTest.setAuthorizationRequestManager(authorizationRequestManager);
    
  }
  
  @After
  public void tearDown() throws Exception {
    authorizationManager = null;
    authorizationRequestManager = null;
    classUnderTest = null;
  }
  
  @Test
  public void getSigner() throws Exception {
    Signer signer = createMock(Signer.class);
    expect(cryptoFactory.createSigner("nodename")).andReturn(signer);
    replayAll();
    Signer result = classUnderTest.getSigner("nodename");
    verifyAll();
    assertSame(signer, result);
  }
  
  @Test
  public void getSigner_notFound() throws Exception {
    expect(cryptoFactory.createSigner("nodename")).andThrow(new KeyczarException(""));
    replayAll();
    try {
      classUnderTest.getSigner("nodename");
      fail("Expected SecurityException");
    } catch (SecurityStorageException e) {
      // pass
    }
    verifyAll();
  }
  
//  @Test
//  public void add_request() {
//    AuthorizationRequest request = new AuthorizationRequest();
//    
//    authorizationRequestManager.add(request);
//    
//    replayAll();
//    
//    classUnderTest.add(request);
//    
//    verifyAll();
//  }
//  
//  @Test
//  public void add_request_same_uuid() {
//    AuthorizationRequest request = new AuthorizationRequest();
//    
//    authorizationRequestManager.add(request);
//    
//    replayAll();
//    
//    classUnderTest.add(request);
//    
//    verifyAll();
//  }

}
