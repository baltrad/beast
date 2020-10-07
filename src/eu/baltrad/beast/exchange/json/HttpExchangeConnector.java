package eu.baltrad.beast.exchange.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.baltrad.beast.exchange.ExchangeConnector;
import eu.baltrad.beast.exchange.ExchangeMessage;
import eu.baltrad.beast.exchange.SendFileRequest;
import eu.baltrad.beast.exchange.ExchangeResponse;
import eu.baltrad.beast.security.AuthorizationRequest;
import eu.baltrad.beast.security.ISecurityManager;
import eu.baltrad.beast.security.SecurityManager;

public class HttpExchangeConnector implements ExchangeConnector {
  /**
   * Request mapper
   */
  private RequestMapper requestMapper;
  
  /**
   * Security manager
   */
  private ISecurityManager securityManager;
  
  /**
   * This logger
   */
  private static Logger logger = LogManager.getLogger(HttpExchangeConnector.class);
  
  /**
   * Format used in the file request
   */
  final static String DATE_FORMAT = "E, d MMM yyyy HH:mm:ss z";
  
  /**
   * Formatter
   */
  final static SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
  
  /**
   * The http headers used for signing
   */
  static final String[] SIGNING_HEADERS = {"Content-Type", "Content-MD5", "Date"};
  
  /**
   * Default constructor
   */
  public HttpExchangeConnector() {
    requestMapper = new RequestMapperImpl(); // Default behaviour
  }
  
  /**
   * @return the request mapper
   */
  public RequestMapper getRequestMapper() {
    return requestMapper;
  }

  /**
   * @param requestMapper the request mapper
   */
  @Autowired
  public void setRequestMapper(RequestMapper requestMapper) {
    this.requestMapper = requestMapper;
  }
  
  /**
   * Sends the authorization request as a json message over http
   * @param request - the message
   * @returns the status code
   */
  @Override
  public ExchangeResponse send(String remoteAddress, AuthorizationRequest request) {
    return send(remoteAddress, requestMapper.toJson(request));
  }
  
  /**
   * Sends the authorization request as a json message over http
   * @param remoteAddress the remote address
   * @param json the json string
   * @return the status code
   */
  public ExchangeResponse send(String remoteAddress, String json) {
    HttpClient httpClient = createClient();
    HttpPost httpPost = createPost(remoteAddress);
    httpPost.addHeader("content-type", "application/json; charset=utf-8");
    httpPost.addHeader("Beast-Message-Type", "json");
    try {
      httpPost.setEntity(new StringEntity(json));
      logger.info("Sending authorization request to: " + remoteAddress);
      HttpResponse response = httpClient.execute(httpPost);
      HttpEntity resEntity = response.getEntity();
      if (resEntity != null) {
        EntityUtils.consume(resEntity);
      }
      ExchangeResponse result = createResponse(response);
      logger.info("response status code: " + result.statusCode());
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      shutdownClient(httpClient);
    }
  }
  
  /**
   * @see ExchangeConnector#sendDexStyle(String, AuthorizationRequest)
   */
  @Override
  public ExchangeResponse sendDexStyle(String remoteAddress, AuthorizationRequest request) {
    HttpClient httpClient = createClient();
    HttpPost httpPost = createPost(remoteAddress);
    httpPost.setEntity(new ByteArrayEntity(request.getPublicKey()));
    httpPost.addHeader("Content-MD5", DigestUtils.md5Hex(request.getPublicKey()));
    httpPost.addHeader("Node-Name", securityManager.getLocalNodeName());
    httpPost.addHeader("Content-Type", "application/zip");
    httpPost.addHeader("DEX-Protocol-Version", "2.1");
    httpPost.addHeader("Date", dateFormat.format(new Date()));
    
    try {
      logger.info("Sending old-style authorization request to: " + remoteAddress);
      HttpResponse response = httpClient.execute(httpPost);
      HttpEntity resEntity = response.getEntity();
      if (resEntity != null) {
        EntityUtils.consume(resEntity);
      }
      ExchangeResponse result = createResponse(response);
      logger.info("response status code: " + result.statusCode());
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      shutdownClient(httpClient);
    }
  }

  /**
   * @see ExchangeConnector#send(SendFileRequest)
   */
  @Override
  public ExchangeResponse send(SendFileRequest request) {
    HttpClient httpClient = createClient();
    HttpPost httpPost = createPost(request.getAddress());
    httpPost.addHeader("Content-Type", request.getContentType());
    httpPost.addHeader("Node-Name", securityManager.getLocalNodeName());
    httpPost.addHeader("Dex-Protocol-Version", "2.1");
    httpPost.addHeader("Beast-Message-Type", "file");
    httpPost.addHeader("Date", dateFormat.format(request.getDate()));
    httpPost.addHeader("Content-MD5", DigestUtils.md5Hex(request.getData()));
    String signedMessage=securityManager.createSignatureMessage(httpPost);
    httpPost.addHeader("Authorization", securityManager.getLocalNodeName() + ":" + securityManager.createSignature(signedMessage));
    
    try {
      httpPost.setEntity(createByteArrayEntity(request.getData()));
      long st = System.currentTimeMillis();
      logger.info("Sending file data to: " + request.getAddress() + ", thread: " + Thread.currentThread().getName());
      HttpResponse response = httpClient.execute(httpPost);
      logger.info("File data sent to "  + request.getAddress() +  " in " + (System.currentTimeMillis() - st) + " ms, thread: " + Thread.currentThread().getName());
      HttpEntity resEntity = response.getEntity();
      if (resEntity != null) {
        EntityUtils.consume(resEntity);
      }
      ExchangeResponse result = createResponse(response);
      logger.info("response status code: " + result.statusCode());
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      shutdownClient(httpClient);
    }
  }

  
  /**
   * Parses a http key authorization request according to protocol version 2.2
   *  @param request the http request
   * @return the parsed authorization request if parsable otherwise a HttpConnectorException will be thrown
   */
  @Override
  public ExchangeMessage parse(InputStream json) {
    return requestMapper.parse(json);
  }
 
  /**
   * Creates an exchange response from a http response instance
   * @param response the http response
   * @return the exchange response
   */
  protected ExchangeResponse createResponse(HttpResponse response) {
    ExchangeResponse result = new ExchangeResponse(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
    if (isRedirected(response)) {
      result.setRedirected(true);
      result.setRedirectAddress(response.getFirstHeader("location").getValue());
    }
    return result;
  }

  /**
   * Creates a byte array entity for use in a http post
   * @param arr the byte array to be wrapped in the byte array entity
   * @return the byte array entity
   */
  protected ByteArrayEntity createByteArrayEntity(byte[] arr) {
    return new ByteArrayEntity(arr);
  }

  /**
   * Checks if there is an indication that the http url has changed
   * @param response the response
   * @return true if there is an indication that address has been changed
   */
  protected boolean isRedirected(HttpResponse response) {
    int statusCode = response.getStatusLine().getStatusCode(); 
    return (statusCode == HttpStatus.SC_MOVED_TEMPORARILY || statusCode == HttpStatus.SC_MOVED_PERMANENTLY); 
  }
  
  /**
   * Extracts the redirect URL from response if there is one, otherwise null
   * @param response the response 
   * @return the redirect URL if there is any
   */
  protected String getRedirectURL(HttpResponse response) {
    if (isRedirected(response)) {
      return response.getFirstHeader("location").getValue();
    }
    return null;
  }
  
  /**
   * Creates a HttpPost
   * @param url the url that the post is for
   * @return the http post
   */
  protected HttpPost createPost(String url) {
    return new HttpPost(url);
  }
 
  /**
   * @param securityManager the security manager to be used
   */
  @Autowired
  public void setSecurityManager(ISecurityManager securityManager) {
    this.securityManager = securityManager;
  }
  
  /**
   * Creates a http client with the relevant http parameters set
   * @return the http client
   */
  protected HttpClient createClient() {
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    registerHttpScheme(schemeRegistry);
    registerHttpsScheme(schemeRegistry);
    
    ThreadSafeClientConnManager connMgr = new ThreadSafeClientConnManager(
            schemeRegistry);
    connMgr.setMaxTotal(200);
    connMgr.setDefaultMaxPerRoute(20);
    
    HttpParams httpParams = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(httpParams, 60000);
    HttpConnectionParams.setSoTimeout(httpParams, 60000);
    HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
    HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
    HttpProtocolParams.setHttpElementCharset(httpParams, HTTP.UTF_8);
    httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
    return new DefaultHttpClient(connMgr, httpParams);
  }

  /**
   * @param httpClient
   */
  protected void shutdownClient(HttpClient httpClient) {
    httpClient.getConnectionManager().shutdown();
  }

  /**
   * Registers HTTP scheme.
   * @param schemeReg Scheme registry
   */
  private void registerHttpScheme(SchemeRegistry schemeReg) {
      Scheme http = new Scheme("http", 80, new PlainSocketFactory());
      schemeReg.register(http);
  }
  /**
   * Register https scheme handler so that we don't require remote site to have certificates
   * @param schemeReg Scheme registry
   */
  private void registerHttpsScheme(SchemeRegistry schemeReg) {
      try {
          SSLContext sslContext = SSLContext.getInstance("SSL");
          sslContext.init(
              null,
              new TrustManager[] {
                  new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                      return null;
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    }
                  }
              },
              new SecureRandom()
          );
          Scheme https = new Scheme("https", 443, new SSLSocketFactory(
              sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER));
          schemeReg.register(https);
      } catch (Exception e) {
          throw new RuntimeException("Failed to register https scheme", e);
      }
  } 
}
