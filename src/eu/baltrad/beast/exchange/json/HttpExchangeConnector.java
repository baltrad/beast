package eu.baltrad.beast.exchange.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.Timeout;
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
   * 
   */
  private List<String> sanBypassHosts = new ArrayList<String>();

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
    CloseableHttpClient httpClient = createClient();
    try {
      HttpPost httpPost = createPost(remoteAddress);
      httpPost.addHeader("content-type", "application/json; charset=utf-8");
      httpPost.addHeader("Beast-Message-Type", "json");
      httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
      logger.info("Sending authorization request to: " + remoteAddress);
      ClassicHttpResponse response = (ClassicHttpResponse) httpClient.executeOpen(null, httpPost, null);
      try {
        HttpEntity resEntity = response.getEntity();
        if (resEntity != null) {
          EntityUtils.consume(resEntity);
        }
        ExchangeResponse result = createResponse(response);
        logger.info("response status code: " + result.statusCode());
        return result;
      } finally {
        response.close();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        httpClient.close();
      } catch (IOException e) {
        logger.error("Error closing HTTP client", e);
      }
    }
  }
  
  /**
   * @see ExchangeConnector#sendDexStyle(String, AuthorizationRequest)
   */
  @Override
  public ExchangeResponse sendDexStyle(String remoteAddress, AuthorizationRequest request) {
    CloseableHttpClient httpClient = createClient();
    try {
      HttpPost httpPost = createPost(remoteAddress);
      httpPost.setEntity(new ByteArrayEntity(request.getPublicKey(), ContentType.create("application/zip")));
      httpPost.addHeader("Content-MD5", DigestUtils.md5Hex(request.getPublicKey()));
      httpPost.addHeader("Node-Name", securityManager.getLocalNodeName());
      httpPost.addHeader("Content-Type", "application/zip");
      httpPost.addHeader("DEX-Protocol-Version", "2.1");
      httpPost.addHeader("Date", dateFormat.format(new Date()));
      
      logger.info("Sending old-style authorization request to: " + remoteAddress);
      ClassicHttpResponse response = (ClassicHttpResponse) httpClient.executeOpen(null, httpPost, null);
      try {
        HttpEntity resEntity = response.getEntity();
        if (resEntity != null) {
          EntityUtils.consume(resEntity);
        }
        ExchangeResponse result = createResponse(response);
        logger.info("response status code: " + result.statusCode());
        return result;
      } finally {
        response.close();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        httpClient.close();
      } catch (IOException e) {
        logger.error("Error closing HTTP client", e);
      }
    }
  }

  /**
   * @see ExchangeConnector#send(SendFileRequest)
   */
  @Override
  public ExchangeResponse send(SendFileRequest request) {
    CloseableHttpClient httpClient = createClient();
    try {
      HttpPost httpPost = createPost(request.getAddress());
      httpPost.addHeader("Content-Type", request.getContentType());
      httpPost.addHeader("Node-Name", securityManager.getLocalNodeName());
      httpPost.addHeader("Dex-Protocol-Version", "2.1");
      httpPost.addHeader("Beast-Message-Type", "file");
      httpPost.addHeader("Date", dateFormat.format(request.getDate()));
      httpPost.addHeader("Content-MD5", DigestUtils.md5Hex(request.getData()));
      String signedMessage=securityManager.createSignatureMessage(httpPost);
      httpPost.addHeader("Authorization", securityManager.getLocalNodeName() + ":" + securityManager.createSignature(signedMessage));
      
      httpPost.setEntity(createByteArrayEntity(request.getData()));
      long st = System.currentTimeMillis();
      logger.info("Sending file data to: " + request.getAddress() + ", thread: " + Thread.currentThread().getName());
      ClassicHttpResponse response = (ClassicHttpResponse) httpClient.executeOpen(null, httpPost, null);
      try {
        logger.info("File data sent to "  + request.getAddress() +  " in " + (System.currentTimeMillis() - st) + " ms, thread: " + Thread.currentThread().getName());
        HttpEntity resEntity = response.getEntity();
        if (resEntity != null) {
          EntityUtils.consume(resEntity);
        }
        ExchangeResponse result = createResponse(response);
        logger.info("response status code: " + result.statusCode());
        return result;
      } finally {
        response.close();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        httpClient.close();
      } catch (IOException e) {
        logger.error("Error closing HTTP client", e);
      }
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
  protected ExchangeResponse createResponse(ClassicHttpResponse response) {
    ExchangeResponse result = new ExchangeResponse(response.getCode(), response.getReasonPhrase());
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
    return new ByteArrayEntity(arr, null);
  }

  /**
   * Checks if there is an indication that the http url has changed
   * @param response the response
   * @return true if there is an indication that address has been changed
   */
  protected boolean isRedirected(ClassicHttpResponse response) {
    int statusCode = response.getCode(); 
    return (statusCode == HttpStatus.SC_MOVED_TEMPORARILY || statusCode == HttpStatus.SC_MOVED_PERMANENTLY); 
  }
  
  /**
   * Extracts the redirect URL from response if there is one, otherwise null
   * @param response the response 
   * @return the redirect URL if there is any
   */
  protected String getRedirectURL(ClassicHttpResponse response) {
    if (isRedirected(response)) {
      return response.getFirstHeader("location").getValue();
    }
    return null;
  }

  /**
   * @return the san bypass hosts
   */
  public List<String> getSanBypassHosts() {
    return sanBypassHosts;
  }

  /**
   * @param sanBypassHosts the san bypass hosts
   */
  public void setSanBypassHosts(List<String> sanBypassHosts) {
    this.sanBypassHosts = sanBypassHosts;
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
   * Trust strategy that accepts any server certificate.
   */
  private static final TrustStrategy TRUST_ALL_CERTIFICATES = new TrustStrategy() {
    public boolean isTrusted(X509Certificate[] chain, String authType) {
      return true;
    }
  };

  /**
   * Hostname verifier that does the normal TLS hostname verification but skips it for the hosts
   * registered in {@link #getSanBypassHosts()}. Handles "No subject alternative names present".
   */
  static class SanBypassHostnameVerifier implements HostnameVerifier {
    private final HostnameVerifier defaultVerifier;
    private final List<String> sanBypassHosts;

    SanBypassHostnameVerifier(List<String> sanBypassHosts) {
      this(new DefaultHostnameVerifier(), sanBypassHosts);
    }

    SanBypassHostnameVerifier(HostnameVerifier defaultVerifier, List<String> sanBypassHosts) {
      this.defaultVerifier = defaultVerifier;
      this.sanBypassHosts = sanBypassHosts;
    }

    @Override
    public boolean verify(String hostname, javax.net.ssl.SSLSession session) {
      if (sanBypassHosts.contains(hostname)) {
        logger.warn("Skipping TLS hostname verification for bypass host " + hostname);
        return true;
      }
      return defaultVerifier.verify(hostname, session);
    }
  }

  /**
   * Creates a http client with the relevant http parameters set
   * @return the http client
   */
  protected CloseableHttpClient createClient() {
    try {
      SSLContext sslContext = SSLContextBuilder.create()
          .loadTrustMaterial(null, TRUST_ALL_CERTIFICATES)
          .build();

      DefaultClientTlsStrategy tlsStrategy = new DefaultClientTlsStrategy(
          sslContext,
          HostnameVerificationPolicy.CLIENT,
          new SanBypassHostnameVerifier(sanBypassHosts));

      PoolingHttpClientConnectionManager connMgr = PoolingHttpClientConnectionManagerBuilder.create()
          .setTlsSocketStrategy(tlsStrategy)
          .setMaxConnTotal(200)
          .setMaxConnPerRoute(20)
          .build();
      
      RequestConfig requestConfig = RequestConfig.custom()
          .setConnectionRequestTimeout(Timeout.ofMilliseconds(60000))
          .setResponseTimeout(Timeout.ofMilliseconds(60000))
          .build();
      
      return HttpClients.custom()
          .setConnectionManager(connMgr)
          .setDefaultRequestConfig(requestConfig)
          .build();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create HTTP client", e);
    }
  }
}
