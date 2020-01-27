package eu.baltrad.beast.exchange.json;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.exchange.ExchangeMessage;
import eu.baltrad.beast.exchange.ExchangeResponse;
import eu.baltrad.beast.exchange.SendFileRequest;
import eu.baltrad.beast.security.AuthorizationRequest;
import eu.baltrad.beast.security.ISecurityManager;

public class HttpExchangeConnectorTest extends EasyMockSupport {
  private HttpExchangeConnector classUnderTest = null;
  private HttpPost post = null;
  private HttpClient client = null;
  private RequestMapper requestMapper = null;
  static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  private ISecurityManager securityManager = null;
  
  interface MethodMock {
    public ExchangeResponse send(String remoteAddress, String json);
    public HttpPost createPost(String url);
    public HttpClient createClient();
    public void shutdownClient(HttpClient httpClient);
    public ExchangeResponse createResponse(HttpResponse response);
    
  }
  private MethodMock methods;
  
  @Before
  public void setUp() throws Exception {
    client = createMock(HttpClient.class);
    requestMapper = createMock(RequestMapper.class);
    methods = createMock(MethodMock.class);
    securityManager = createMock(ISecurityManager.class);
    
    post = new HttpPost();
    classUnderTest = new HttpExchangeConnector() {
      @Override
      protected HttpPost createPost(String url) {
        return methods.createPost(url);
      }
      @Override
      protected HttpClient createClient() {
        return methods.createClient();
      }
      @Override
      protected void shutdownClient(HttpClient httpClient) {
        methods.shutdownClient(httpClient);
      }
      @Override
      protected ExchangeResponse createResponse(HttpResponse response) {
        return methods.createResponse(response);
      }
    };
    classUnderTest.setRequestMapper(requestMapper);
    classUnderTest.setSecurityManager(securityManager);
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
    client = null;
    post = null;
    requestMapper = null;
    securityManager = null;
  }
  
  @Test
  public void test_send_AuthorizationRequest() {
    AuthorizationRequest request = new AuthorizationRequest();
    String jsonString = "{}";
    classUnderTest = new HttpExchangeConnector() {
      @Override
      public ExchangeResponse send(String remoteAddress, String json) {
        return methods.send(remoteAddress, json);
      }
    };
    classUnderTest.setRequestMapper(requestMapper);
    
    expect(requestMapper.toJson(request)).andReturn(jsonString);
    expect(methods.send("http://somewhere", jsonString)).andReturn(new ExchangeResponse(HttpStatus.SC_OK));
    
    replayAll();
    
    classUnderTest.send("http://somewhere", request);
    
    verifyAll();
  }
  
  @Test
  public void test_send() throws Exception {
    HttpResponse httpResponse = createMock(HttpResponse.class);
    ExchangeResponse exchangeResponse = new ExchangeResponse(0);
    expect(methods.createPost("http://localhost")).andReturn(post);
    expect(methods.createClient()).andReturn(client);
    expect(client.execute(post)).andReturn(httpResponse);
    expect(httpResponse.getEntity()).andReturn(null);
    expect(methods.createResponse(httpResponse)).andReturn(exchangeResponse);
    methods.shutdownClient(client);
    
    replayAll();
    
    ExchangeResponse result = classUnderTest.send("http://localhost", "{\"ObjectClass\":\"AuthorizationRequest\",\"Object\":\"{}\"}");
    
    verifyAll();
    assertSame(result, exchangeResponse);
    assertTrue(post.getHeaders("Content-Type")[0].getValue().startsWith("application/json"));
    String jsonMessage = IOUtils.toString(post.getEntity().getContent(), "UTF-8");
    assertEquals("{\"ObjectClass\":\"AuthorizationRequest\",\"Object\":\"{}\"}", jsonMessage);
  }
  
  @Test
  public void test_send_FileRequest() throws Exception {
    HttpResponse response = createMock(HttpResponse.class);
    ExchangeResponse exchangeResponse = new ExchangeResponse(HttpStatus.SC_OK);

    SendFileRequest request = new SendFileRequest();
    request.setAddress("http://somewhere.se");
    request.setNodeName("nisse");
    request.setData("a".getBytes());
    request.setDate(dateFormat.parse("2020-01-07T10:00:00Z"));
    
    
    expect(methods.createClient()).andReturn(client);
    expect(methods.createPost("http://somewhere.se")).andReturn(post);
    expect(securityManager.getLocalNodeName()).andReturn("localname").anyTimes();
    expect(securityManager.createSignatureMessage(post)).andReturn("xyz");
    expect(securityManager.createSignature("xyz")).andReturn("abc");
    expect(client.execute(post)).andReturn(response);
    expect(response.getEntity()).andReturn(null);
    expect(methods.createResponse(response)).andReturn(exchangeResponse);
    methods.shutdownClient(client);
    replayAll();
    
    ExchangeResponse result = classUnderTest.send(request);
    
    verifyAll();
    assertSame(exchangeResponse, result);
  }
  
  @Test
  public void test_parse_AuthorizationRequest() throws Exception {
    InputStream json = new ByteArrayInputStream(new byte[0]);
    ExchangeMessage exchangeMessage = new ExchangeMessage();
    
    expect(requestMapper.parse(json)).andReturn(exchangeMessage);
    
    replayAll();
    
    ExchangeMessage result = classUnderTest.parse(json);
    
    verifyAll();
    assertSame(exchangeMessage, result);
  }
}
