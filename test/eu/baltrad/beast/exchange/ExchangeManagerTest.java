/**
 * 
 */
package eu.baltrad.beast.exchange;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.assertSame;
//import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.security.Authorization;
import eu.baltrad.beast.security.AuthorizationException;
import eu.baltrad.beast.security.AuthorizationRequest;
import eu.baltrad.beast.security.IAuthorizationManager;
import eu.baltrad.beast.security.IAuthorizationRequestManager;
import eu.baltrad.beast.security.ISecurityManager;

/**
 * @author anders
 *
 */
public class ExchangeManagerTest extends EasyMockSupport {
  public interface Methods {
    public ExchangeResponse send(SendFileRequest request, SendFileRequestCallback callback);
    public IPooledFileRequestPublisher createPooledFileRequestPublisher();
  };
  
  private ExchangeManager classUnderTest = null;
  private ExchangeConnector connector = null;
  private IAuthorizationManager authorizationManager = null;
  private IAuthorizationRequestManager authorizationRequestManager = null;
  private ISecurityManager securityManager = null;
  private Methods methods = null;
  
  @Before
  public void setUp() throws Exception {
    connector = createMock(ExchangeConnector.class);
    authorizationManager = createMock(IAuthorizationManager.class);
    authorizationRequestManager = createMock(IAuthorizationRequestManager.class);
    securityManager = createMock(ISecurityManager.class);
    methods = createMock(Methods.class);
    
    classUnderTest = new ExchangeManager() {
      @Override
      protected IPooledFileRequestPublisher createPooledFileRequestPublisher() {
        return methods.createPooledFileRequestPublisher();
      }
    };
    classUnderTest.setConnector(connector);
    classUnderTest.setAuthorizationManager(authorizationManager);
    classUnderTest.setAuthorizationRequestManager(authorizationRequestManager);
    classUnderTest.setSecurityManager(securityManager);
    classUnderTest.setExchangeUrlPart("/BaltradDex/exchangeManager.htm");
  }
  
  @After
  public void tearDown() throws Exception {
    connector = null;
    authorizationManager = null;
    authorizationRequestManager = null;
    securityManager = null;
    methods = null;
    classUnderTest = null;
  }

  /**
   * Requests an authorization to remote host.
   */
  @Test
  public void test_requestAuthorization() {
    String remoteAddress = "http://localhost:1234";
    String message = "hello world";
    AuthorizationRequest request = new AuthorizationRequest();
    request.setOutgoing(false);
    
    expect(authorizationRequestManager.getByRemoteAddress(remoteAddress)).andReturn(null);
    expect(authorizationManager.createAuthorizationRequest(message)).andReturn(request);
    expect(connector.send(remoteAddress + "/BaltradDex/exchangeManager.htm", request)).andReturn(new ExchangeResponse(HttpStatus.SC_OK));
    authorizationRequestManager.add(request);

    replayAll();
    
    AuthorizationRequest result = classUnderTest.requestAuthorization(remoteAddress, message);
    
    verifyAll();
    assertSame(request, result);
    assertEquals(true, result.isOutgoing());
  }

  /**
   * Requests an authorization to remote host.
   */
  @Test
  public void test_requestAuthorization_update() {
    String remoteAddress = "http://localhost:1234";
    String message = "hello world";
    AuthorizationRequest request = new AuthorizationRequest();
    request.setOutgoing(false);
    
    expect(authorizationRequestManager.getByRemoteAddress(remoteAddress)).andReturn(request);
    expect(connector.send(remoteAddress + "/BaltradDex/exchangeManager.htm", request)).andReturn(new ExchangeResponse(HttpStatus.SC_OK));
    authorizationRequestManager.update(request);

    replayAll();
    
    AuthorizationRequest result = classUnderTest.requestAuthorization(remoteAddress, message);
    
    verifyAll();
    assertSame(request, result);
    assertEquals(true, result.isOutgoing());
    assertEquals(message, result.getMessage());
  }
  
  /**
   * Requests an authorization, but new variant fails and then a request for old style is executed.
   */
  @Test
  public void test_requestAuthorization_notFound_dexStyleUsed() {
    String remoteAddress = "http://localhost:1234";
    String message = "hello world";
    AuthorizationRequest request = new AuthorizationRequest();
    request.setOutgoing(false);
    
    expect(authorizationRequestManager.getByRemoteAddress(remoteAddress)).andReturn(null);
    expect(authorizationManager.createAuthorizationRequest(message)).andReturn(request);
    expect(connector.send(remoteAddress + "/BaltradDex/exchangeManager.htm", request)).andReturn(new ExchangeResponse(HttpStatus.SC_NOT_FOUND));
    expect(connector.sendDexStyle(remoteAddress + "/BaltradDex/post_key.htm", request)).andReturn(new ExchangeResponse(HttpStatus.SC_OK));

    replayAll();

    AuthorizationRequest result = classUnderTest.requestAuthorization(remoteAddress, message);
    
    verifyAll();
    assertSame(request, result);
    assertEquals(true, result.isOutgoing());
  }

  /**
   * Requests an authorization. New variant not found, conflict with old dex style results in failed execution
   */
  @Test
  public void test_requestAuthorization_notFound_dexStyleUsed_conflict() {
    String remoteAddress = "http://localhost:1234";
    String message = "hello world";
    AuthorizationRequest request = new AuthorizationRequest();
    request.setOutgoing(false);
    
    expect(authorizationRequestManager.getByRemoteAddress(remoteAddress)).andReturn(null);
    expect(authorizationManager.createAuthorizationRequest(message)).andReturn(request);
    expect(connector.send(remoteAddress + "/BaltradDex/exchangeManager.htm", request)).andReturn(new ExchangeResponse(HttpStatus.SC_NOT_FOUND));
    expect(connector.sendDexStyle(remoteAddress + "/BaltradDex/post_key.htm", request)).andReturn(new ExchangeResponse(HttpStatus.SC_CONFLICT));

    replayAll();

    try {
      classUnderTest.requestAuthorization(remoteAddress, message);
      fail("Expected ExchangeStatusException");
    } catch (ExchangeStatusException e) {
      assertEquals(HttpStatus.SC_CONFLICT, e.getStatus());
    }
    
    verifyAll();
  }
  
  /**
   * Verifies that the exchange message can be created from an input stream.
   */
  @Test
  public void test_receive_AuthorizationRequest() {
    InputStream inputStream = new ByteArrayInputStream(new byte[0]);
    AuthorizationRequest request = new AuthorizationRequest();
    request.setOutgoing(true);
    request.setRequestUUID("abc");
    request.setNodeName("nodename");
    
    ExchangeMessage exchangeMessage = new ExchangeMessage();
    exchangeMessage.setAuthorizationRequest(request);
    
    expect(connector.parse(inputStream)).andReturn(exchangeMessage);
    expect(authorizationRequestManager.get("abc", false)).andReturn(null);
    expect(authorizationRequestManager.getByNodeName("nodename")).andReturn(null);
    authorizationRequestManager.add(request);
    
    replayAll();
    
    ExchangeMessage result = classUnderTest.receive(inputStream);
    
    verifyAll();
    assertEquals(false, request.isOutgoing());
    assertSame(exchangeMessage, result);
  }

  /**
   * Verifies that the exchange message can be created from an input stream.
   */
  @Test
  public void test_receive_AuthorizationRequest_removePrevious() {
    InputStream inputStream = new ByteArrayInputStream(new byte[0]);
    AuthorizationRequest request = new AuthorizationRequest();
    request.setOutgoing(true);
    request.setRequestUUID("abc");
    request.setNodeName("nodename");

    AuthorizationRequest oldrequest = new AuthorizationRequest();
    oldrequest.setRequestUUID("xyz");
    
    ExchangeMessage exchangeMessage = new ExchangeMessage();
    exchangeMessage.setAuthorizationRequest(request);
    
    expect(connector.parse(inputStream)).andReturn(exchangeMessage);
    expect(authorizationRequestManager.get("abc", false)).andReturn(null);
    expect(authorizationRequestManager.getByNodeName("nodename")).andReturn(oldrequest);
    authorizationRequestManager.remove("xyz");
    
    authorizationRequestManager.add(request);
    
    replayAll();
    
    ExchangeMessage result = classUnderTest.receive(inputStream);
    
    verifyAll();
    assertEquals(false, request.isOutgoing());
    assertSame(exchangeMessage, result);
  }
  
  /**
   * Verifies that the exchange message can be created from an input stream.
   */
  @Test
  public void test_receive_AuthorizationRequest_update() {
    InputStream inputStream = new ByteArrayInputStream(new byte[0]);
    AuthorizationRequest request = new AuthorizationRequest();
    request.setOutgoing(true);
    request.setRequestUUID("abc");
    request.setNodeName("nodename");

    AuthorizationRequest oldrequest = new AuthorizationRequest();
    oldrequest.setRequestUUID("abc");
    
    ExchangeMessage exchangeMessage = new ExchangeMessage();
    exchangeMessage.setAuthorizationRequest(request);
    
    expect(connector.parse(inputStream)).andReturn(exchangeMessage);
    expect(authorizationRequestManager.get("abc", false)).andReturn(oldrequest);
    authorizationRequestManager.update(request);
    
    replayAll();
    
    ExchangeMessage result = classUnderTest.receive(inputStream);
    
    verifyAll();
    assertEquals(false, request.isOutgoing());
    assertSame(exchangeMessage, result);
  }
  
  /**
   * Approves an authorization request and sends an auto request
   */
  @Test
  public void test_approve_AuthorizationRequest_andSend() {
    AuthorizationRequest request = new AuthorizationRequest();
    AuthorizationRequest savedRequest = new AuthorizationRequest();
    AuthorizationRequest outgoingRequest = new AuthorizationRequest();
    Authorization authorization = new Authorization();
    
    request.setNodeName("nodename");
    request.setRequestUUID("abc");
    request.setNodeAddress("http://somewhere.se");
    savedRequest.setNodeAddress(request.getNodeAddress());

    expect(authorizationRequestManager.get("abc", true)).andReturn(null);
    expect(authorizationRequestManager.get("abc", false)).andReturn(savedRequest);
    expect(authorizationRequestManager.toAuthorization(savedRequest)).andReturn(authorization);
    expect(authorizationManager.getByNodeName("nodename")).andReturn(null);
    expect(securityManager.expandPublicKey(authorization)).andReturn("publickey.pub");
    authorizationManager.add(authorization);
    authorizationRequestManager.remove("abc");
    expect(authorizationManager.createAuthorizationRequest("Autogenerated request for approval of key.")).andReturn(outgoingRequest);
    expect(connector.send("http://somewhere.se/BaltradDex/exchangeManager.htm", outgoingRequest)).andReturn(new ExchangeResponse(HttpStatus.SC_OK));
    
    replayAll();
    
    classUnderTest.approve(request);
    
    verifyAll();
    assertEquals(true, authorization.isAuthorized());
    assertEquals("publickey.pub", authorization.getPublicKeyPath());
    assertEquals(false, authorization.isLocal());
  }

  /**
   * Approves an authorization request and sends an auto request
   */
  @Test
  public void test_approve_AuthorizationRequest_notFound_andSendOldStyle() {
    AuthorizationRequest request = new AuthorizationRequest();
    AuthorizationRequest savedRequest = new AuthorizationRequest();
    AuthorizationRequest outgoingRequest = new AuthorizationRequest();
    Authorization authorization = new Authorization();
    
    request.setNodeName("nodename");
    request.setRequestUUID("abc");
    request.setNodeAddress("http://somewhere.se");
    savedRequest.setNodeAddress(request.getNodeAddress());

    expect(authorizationRequestManager.get("abc", true)).andReturn(null);
    expect(authorizationRequestManager.get("abc", false)).andReturn(savedRequest);
    expect(authorizationRequestManager.toAuthorization(savedRequest)).andReturn(authorization);
    expect(authorizationManager.getByNodeName("nodename")).andReturn(null);
    expect(securityManager.expandPublicKey(authorization)).andReturn("publickey.pub");
    authorizationManager.add(authorization);
    authorizationRequestManager.remove("abc");
    expect(authorizationManager.createAuthorizationRequest("Autogenerated request for approval of key.")).andReturn(outgoingRequest);
    expect(connector.send("http://somewhere.se/BaltradDex/exchangeManager.htm", outgoingRequest)).andReturn(new ExchangeResponse(HttpStatus.SC_NOT_FOUND));
    expect(authorizationManager.createAuthorizationRequest("Old style autogenerated request for approval of key.")).andReturn(outgoingRequest);
    expect(connector.sendDexStyle("http://somewhere.se/BaltradDex/post_key.htm", outgoingRequest)).andReturn(new ExchangeResponse(HttpStatus.SC_OK));
    
    replayAll();
    classUnderTest.approve(request);
    
    verifyAll();
    assertEquals(true, authorization.isAuthorized());
    assertEquals("publickey.pub", authorization.getPublicKeyPath());
    assertEquals(false, authorization.isLocal());
  }
  
  /**
   * Approves an authorization request and sends an auto requestprotected PooledFileRequestPublisher createPooledFileRequestPublisher()
   */
  @Test
  public void test_approve_AuthorizationRequest_redirected() {
    AuthorizationRequest request = new AuthorizationRequest();
    AuthorizationRequest savedRequest = new AuthorizationRequest();
    AuthorizationRequest outgoingRequest = new AuthorizationRequest();
    Authorization authorization = new Authorization();
    
    request.setNodeName("nodename");
    request.setRequestUUID("abc");
    request.setNodeAddress("http://somewhere.se");
    savedRequest.setNodeAddress(request.getNodeAddress());

    expect(authorizationRequestManager.get("abc", true)).andReturn(null);
    expect(authorizationRequestManager.get("abc", false)).andReturn(savedRequest);
    expect(authorizationRequestManager.toAuthorization(savedRequest)).andReturn(authorization);
    expect(authorizationManager.getByNodeName("nodename")).andReturn(null);
    expect(securityManager.expandPublicKey(authorization)).andReturn("publickey.pub");
    authorizationManager.add(authorization);
    authorizationRequestManager.remove("abc");
    expect(authorizationManager.createAuthorizationRequest("Autogenerated request for approval of key.")).andReturn(outgoingRequest);
    ExchangeResponse resp = new ExchangeResponse(HttpStatus.SC_TEMPORARY_REDIRECT);
    resp.setRedirected(true);
    resp.setRedirectAddress("http://somewhereelse.se");
    expect(connector.send("http://somewhere.se/BaltradDex/exchangeManager.htm", outgoingRequest)).andReturn(resp);
    expect(connector.send("http://somewhereelse.se/BaltradDex/exchangeManager.htm", outgoingRequest)).andReturn(new ExchangeResponse(HttpStatus.SC_OK));
    authorizationManager.update(authorization);
    replayAll();
    
    classUnderTest.approve(request);
    
    verifyAll();
    assertEquals(true, authorization.isAuthorized());
    assertEquals("publickey.pub", authorization.getPublicKeyPath());
    assertEquals(false, authorization.isLocal());
    assertEquals("http://somewhereelse.se", authorization.getRedirectedAddress());
  }
  
  @Test
  public void test_approve_AuthorizationRequest_local() {
    AuthorizationRequest request = new AuthorizationRequest();
    AuthorizationRequest savedRequest = new AuthorizationRequest();
    Authorization authorization = new Authorization();
    Authorization local = new Authorization();
    local.setLocal(true);
    
    request.setNodeName("nodename");
    request.setRequestUUID("abc");
    request.setNodeAddress("http://somewhere.se");
    savedRequest.setNodeAddress(request.getNodeAddress());

    expect(authorizationRequestManager.get("abc", true)).andReturn(null);
    expect(authorizationRequestManager.get("abc", false)).andReturn(savedRequest);
    expect(authorizationRequestManager.toAuthorization(savedRequest)).andReturn(authorization);
    expect(authorizationManager.getByNodeName("nodename")).andReturn(local);
    
    replayAll();

    try {
      classUnderTest.approve(request);
      fail("Expected AuthorizationException");
    } catch (AuthorizationException e) {
      // pass
    }
    
    verifyAll();
  }
  
  @Test
  public void test_approve_AuthorizationRequest_alreadySent() {
    AuthorizationRequest request = new AuthorizationRequest();
    AuthorizationRequest incommingRequest = new AuthorizationRequest();
    AuthorizationRequest sentRequest = new AuthorizationRequest();
    Authorization authorization = new Authorization();
    
    request.setNodeName("nodename");
    request.setRequestUUID("abc");
    request.setNodeAddress("http://somewhere.se");
    incommingRequest.setNodeAddress(request.getNodeAddress());
    
    expect(authorizationRequestManager.get("abc", true)).andReturn(sentRequest);
    expect(authorizationRequestManager.get("abc", false)).andReturn(incommingRequest);
    expect(authorizationRequestManager.toAuthorization(incommingRequest)).andReturn(authorization);
    expect(authorizationManager.getByNodeName("nodename")).andReturn(null);
    expect(securityManager.expandPublicKey(authorization)).andReturn("publickey.pub");
    authorizationManager.add(authorization);
    authorizationRequestManager.remove("abc");
    
    replayAll();
    
    classUnderTest.approve(request);
    
    verifyAll();
    assertEquals("publickey.pub", authorization.getPublicKeyPath());
    assertEquals(false, authorization.isLocal());
    assertEquals(true, authorization.isAuthorized());
  }
  
  @Test
  public void test_deny_AuthorizationRequest() {
    AuthorizationRequest request = new AuthorizationRequest();
    AuthorizationRequest savedRequest = new AuthorizationRequest();
    Authorization authorization = new Authorization();
    
    request.setNodeName("nodename");
    request.setRequestUUID("abc");
    request.setNodeAddress("http://somewhere.se");
    savedRequest.setNodeAddress(request.getNodeAddress());

    expect(authorizationRequestManager.get("abc", false)).andReturn(savedRequest);
    expect(authorizationRequestManager.toAuthorization(savedRequest)).andReturn(authorization);
    expect(authorizationManager.getByNodeName("nodename")).andReturn(null);
    authorizationRequestManager.remove("abc");
    
    replayAll();
    
    classUnderTest.deny(request);
    
    verifyAll();
    assertEquals(false, authorization.isAuthorized());
    assertEquals(null, authorization.getPublicKeyPath());
    assertEquals(false, authorization.isLocal());
  }
  
  @Test
  public void test_send() {
    classUnderTest = new ExchangeManager() {
      @Override
      public ExchangeResponse send(SendFileRequest request, SendFileRequestCallback callback) {
        return methods.send(request, callback);
      }
    };
    SendFileRequest request = new SendFileRequest();
    request.setAddress("http://localhost.se");
    ExchangeResponse response = new ExchangeResponse(HttpStatus.SC_OK);
    expect(methods.send(request,  null)).andReturn(response);
    
    replayAll();
    ExchangeResponse result = classUnderTest.send(request);
    verifyAll();
    assertSame(response, result);
  }
 
  @Test
  public void test_send_with_callback() {
    SendFileRequestCallback callback = createMock(SendFileRequestCallback.class);
    SendFileRequest request = new SendFileRequest();
    request.setAddress("http://localhost.se");

    ExchangeResponse response = new ExchangeResponse(HttpStatus.SC_OK);
    expect(connector.send(request)).andReturn(response);
    callback.filePublished(request);
    
    replayAll();
    ExchangeResponse result = classUnderTest.send(request, callback);
    verifyAll();
    assertSame(response, result);
  }

  @Test
  public void test_send_with_callback_serverError() {
    SendFileRequestCallback callback = createMock(SendFileRequestCallback.class);
    SendFileRequest request = new SendFileRequest();
    request.setAddress("http://localhost.se");

    ExchangeResponse response = new ExchangeResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    expect(connector.send(request)).andReturn(response);
    callback.filePublicationFailed(request, response);
    
    replayAll();
    ExchangeResponse result = classUnderTest.send(request, callback);
    verifyAll();
    assertSame(response, result);
  }
  
  @Test
  public void test_send_with_callback_redirected() {
    SendFileRequest request = new SendFileRequest();
    request.setNodeName("mynodename");
    request.setAddress("http://localhost.se");
    ExchangeResponse response = new ExchangeResponse(HttpStatus.SC_TEMPORARY_REDIRECT);
    response.setRedirectAddress("http://somewhere.else");
    response.setRedirected(true);
    Authorization authorization = new Authorization();
    authorization.setRedirectedAddress("http://localhost.se");
    
    expect(connector.send(request)).andReturn(response);
    expect(authorizationManager.getByNodeName("mynodename")).andReturn(authorization);
    expect(connector.send(request)).andReturn(new ExchangeResponse(HttpStatus.SC_OK));
    authorizationManager.update(authorization);

    replayAll();
    classUnderTest.send(request);
    verifyAll();
    assertEquals("http://somewhere.else", authorization.getRedirectedAddress());
    assertEquals("http://somewhere.else", request.getAddress());
  }
  
  @Test
  public void test_send_redirectFailed() {
    SendFileRequest request = new SendFileRequest();
    request.setNodeName("mynodename");
    request.setAddress("http://localhost.se");
    ExchangeResponse response = new ExchangeResponse(HttpStatus.SC_TEMPORARY_REDIRECT);
    response.setRedirectAddress("http://somewhere.else");
    response.setRedirected(true);
    Authorization authorization = new Authorization();
    
    expect(connector.send(request)).andReturn(response);
    expect(authorizationManager.getByNodeName("mynodename")).andReturn(authorization);
    expect(connector.send(request)).andReturn(new ExchangeResponse(HttpStatus.SC_NOT_FOUND));

    replayAll();
    try {
      classUnderTest.send(request);
    } catch (ExchangeStatusException ese) {
      assertEquals(HttpStatus.SC_NOT_FOUND, ese.getStatus());
    }
    verifyAll();
    assertEquals(null, authorization.getRedirectedAddress());
    assertEquals("http://somewhere.else", request.getAddress());
  }
  
  @Test
  public void test_sendAsync() {
    IPooledFileRequestPublisher publisher = createMock(IPooledFileRequestPublisher.class);
    
    SendFileRequest request = new SendFileRequest();
    request.setNodeName("nisse");
    SendFileRequestCallback callback = createMock(SendFileRequestCallback.class);
    
    expect(methods.createPooledFileRequestPublisher()).andReturn(publisher);
    publisher.publish(request, callback);
    
    replayAll();
    
    assertFalse(classUnderTest.getPooledPublishers().containsKey("nisse"));
    
    classUnderTest.sendAsync(request, callback);

    verifyAll();
    
    assertTrue(classUnderTest.getPooledPublishers().containsKey("nisse"));
    assertSame(publisher, classUnderTest.getPooledPublishers().get("nisse"));
  }

  @Test
  public void test_sendAsync_publisherAlreadyCreated() {
    IPooledFileRequestPublisher publisher = createMock(IPooledFileRequestPublisher.class);
    
    classUnderTest.getPooledPublishers().put("nisse", publisher);
    
    SendFileRequest request = new SendFileRequest();
    request.setNodeName("nisse");
    SendFileRequestCallback callback = createMock(SendFileRequestCallback.class);
    
    publisher.publish(request, callback);
    
    replayAll();
    
    classUnderTest.sendAsync(request, callback);

    verifyAll();
  }

}
