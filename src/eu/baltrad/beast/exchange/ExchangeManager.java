/**
 * 
 */
package eu.baltrad.beast.exchange;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
public class ExchangeManager implements IExchangeManager {
  /**
   * The authorization manager
   */
  private IAuthorizationManager authorizationManager;
  
  /**
   * The authorization request manager
   */
  private IAuthorizationRequestManager authorizationRequestManager;

  /**
   * The security manager
   */
  private ISecurityManager securityManager;
  
  /**
   * The connector
   */
  private ExchangeConnector connector;
  
  /**
   * The exchange manager url part used when communication with other nodes.
   */
  private String exchangeUrlPart = "/BaltradDex/exchangeManager.htm";
  
  /**
   * A map of pooled publishers
   */
  private Map<String, IPooledFileRequestPublisher> pooledPublishers = new HashMap<String, IPooledFileRequestPublisher>();
  
  /**
   * Number of available slots that can be published before they are beeing rejected
   */
  private int queueSize = 100;
  
  /**
   * Number of idling threads (1 is usually a good idea)
   */
  private int corePoolSize = 1;
  
  /**
   * Max number of threads that will be created during high load
   */
  private int maxPoolSize = 5;
  
  /**
   * The logger
   */
  private final static Logger logger = LogManager.getLogger(ExchangeManager.class);
  
  /**
   * Default constructor 
   */
  public ExchangeManager() {
  }
  
  /**
   * @param authorizationManager the authorization manager
   */
  @Autowired
  public void setAuthorizationManager(IAuthorizationManager authorizationManager) {
    this.authorizationManager = authorizationManager;
  }

  /**
   * @param authorizationRequestManager the authorization request manager
   */
  @Autowired
  public void setAuthorizationRequestManager(IAuthorizationRequestManager authorizationRequestManager) {
    this.authorizationRequestManager = authorizationRequestManager;
  }
  /**
   * @param connector the connector
   */
  @Autowired
  public void setConnector(ExchangeConnector connector) {
    this.connector = connector;
  }

  /**
   * @see IExchangeManager#send(SendFileRequest)
   */
  @Override
  public ExchangeResponse send(SendFileRequest request) {
    return send(request, null);
  }

  /**
   * @see IExchangeManager#send(SendFileRequest, SendFileRequestCallback)
   */
  @Override
  public ExchangeResponse send(SendFileRequest request, SendFileRequestCallback callback) {
    ExchangeResponse response = connector.send(request);
    if (response.statusCode() != HttpStatus.SC_OK) {
      if (response.isRedirected()) {
        Authorization authorization = authorizationManager.getByNodeName(request.getNodeName());
        if (authorization != null && response.getRedirectAddress() != null && !response.getRedirectAddress().equals(authorization.getRedirectedAddress())) {
          request.setAddress(response.getRedirectAddress());
          ExchangeResponse newresponse = connector.send(request);
          if (newresponse.statusCode() == HttpStatus.SC_OK) {
            try {
              authorization.setRedirectedAddress(response.getRedirectAddress());
              authorizationManager.update(authorization);
            } catch (Exception e) {
              logger.warn("Failed to update redirect address", e);
            }
          } else {
            throw new ExchangeStatusException("Failed to send redirected message", newresponse.statusCode());
          }
          response = newresponse;
        }
      } else {
        logger.error("Failed to send file to " + request.getNodeName());
        if (callback != null) {
          try {
            callback.filePublicationFailed(request, response);
          } catch (Exception e) {
          }
        }
      }
    } else {
      if (callback != null) {
        try {
          callback.filePublished(request);
        } catch (Exception e) {
          logger.debug(e);
        }
      }
    }
    return response;
  }
  
  public void sendAsync(SendFileRequest request, SendFileRequestCallback callback) {
    if (!pooledPublishers.containsKey(request.getNodeName())) {
      synchronized (pooledPublishers) {
        if (!pooledPublishers.containsKey(request.getNodeName())) {
          pooledPublishers.put(request.getNodeName(), createPooledFileRequestPublisher());
        }
      }
    }
    pooledPublishers.get(request.getNodeName()).publish(request, callback);
  }
  
  /**
   * @see IExchangeManager#parse(InputStream)
   */
  @Override
  public ExchangeMessage parse(InputStream stream) {
    return connector.parse(stream);
  }

  /**
   * @see IExchangeManager#receive(ExchangeMessage)
   */
  @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
  @Override
  public ExchangeMessage receive(ExchangeMessage message) {
    if (message != null && message.isAuthorizationRequest()) {
      AuthorizationRequest request = message.getAuthorizationRequest();
      if (authorizationRequestManager.get(request.getRequestUUID(), false) != null) {
        request.setReceivedAt(new Date());
        request.setOutgoing(false);
        authorizationRequestManager.update(request);
      } else {
        AuthorizationRequest oldrequest = authorizationRequestManager.getByNodeName(request.getNodeName());
        if (oldrequest != null) {
          authorizationRequestManager.remove(oldrequest.getRequestUUID());
        }
        request.setReceivedAt(new Date());
        request.setOutgoing(false);
        authorizationRequestManager.add(request);
      }
    }
    return message;
  }
  
  /**
   * @see IExchangeManager#receive(InputStream)
   */
  @Override
  public ExchangeMessage receive(InputStream stream) {
    ExchangeMessage message = parse(stream);
    return receive(message);
  }


  /**
   * @see IExchangeManager#requestAuthorization(String, String)
   */
  @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
  @Override
  public AuthorizationRequest requestAuthorization(String remoteAddress, String message) {
    boolean storeRequestInDB = true;
    boolean update = false;
    AuthorizationRequest request = authorizationRequestManager.getByRemoteAddress(remoteAddress);
    if (request != null) {
      request.setMessage(message);
      update = true;
    } else {
      request = authorizationManager.createAuthorizationRequest(message);
    }
    
    ExchangeResponse response = connector.send(remoteAddress + this.exchangeUrlPart, request);
    if (response.statusCode() == HttpStatus.SC_NOT_FOUND) { // If Not found, they might be listening so try old dex style...
      ExchangeResponse newresponse = connector.sendDexStyle(remoteAddress +  "/BaltradDex/post_key.htm", request);
      if (newresponse.statusCode() == HttpStatus.SC_OK || newresponse.statusCode() == HttpStatus.SC_UNAUTHORIZED || 
          newresponse.statusCode() == HttpStatus.SC_CONFLICT || newresponse.statusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
        response = newresponse; // Any of these and they are probably running old style....
        // If sending old style, we can't connect requests anyway so no point in storing request in database.
        storeRequestInDB = false;
      }
    }
    if (response.statusCode() != HttpStatus.SC_ACCEPTED && response.statusCode() != HttpStatus.SC_OK) {
      throw new ExchangeStatusException("Could not send authorization request to " + remoteAddress + ", status = " + response.statusCode(), response.statusCode());
    }
    request.setOutgoing(true);
    request.setRemoteAddress(remoteAddress);
    if (storeRequestInDB == true) {
      if (!update) {
        authorizationRequestManager.add(request);
      } else {
        authorizationRequestManager.update(request);
      }
    }
    return request;
  }
  
  /**
   * @see IExchangeManager#approve(AuthorizationRequest)
   * @param request the authorization request to approve 
   */
  @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
  @Override
  public void approve(AuthorizationRequest request) {
    AuthorizationRequest outgoingRequest = authorizationRequestManager.get(request.getRequestUUID(), true);
    AuthorizationRequest incommingRequest = authorizationRequestManager.get(request.getRequestUUID(), false);
    Authorization authorization = authorizationRequestManager.toAuthorization(incommingRequest);
    Authorization current = authorizationManager.getByNodeName(request.getNodeName());
    if (current == null || (current != null && !current.isLocal())) {
      authorization.setLastUpdated(new Date());
      authorization.setLocal(false);
      authorization.setAuthorized(true);
      authorization.setLastUpdated(new Date());
      String publicKeyName = securityManager.expandPublicKey(authorization);
      authorization.setPublicKeyPath(publicKeyName);
      if (current != null) {
        authorizationManager.updateByNodeName(authorization);
      } else {
        authorizationManager.add(authorization);
      }
      authorizationRequestManager.remove(request.getRequestUUID());
      if (outgoingRequest == null && request.isAutorequest()) {
        outgoingRequest = authorizationManager.createAuthorizationRequest("Autogenerated request for approval of key.");
        outgoingRequest.setRequestUUID(incommingRequest.getRequestUUID());
        ExchangeResponse response = connector.send(incommingRequest.getNodeAddress() + exchangeUrlPart, outgoingRequest);
        if (!response.isRedirected() && response.statusCode() == HttpStatus.SC_NOT_FOUND) {
          request = authorizationManager.createAuthorizationRequest("Old style autogenerated request for approval of key.");
          response = connector.sendDexStyle(incommingRequest.getNodeAddress() + "/BaltradDex/post_key.htm", request);
        } else if (response.isRedirected()){
          authorization.setRedirectedAddress(response.getRedirectAddress());
          
          logger.info("Remote host: " + authorization.getNodeAddress() + " has been redirected to " + authorization.getRedirectedAddress());

          ExchangeResponse newresponse = connector.send(authorization.getRedirectedAddress() + exchangeUrlPart, outgoingRequest);
          if (newresponse.statusCode() == HttpStatus.SC_OK) { // If we are able to send response to new address, we update authorization with that information, otherwise just silently ignore any problem.
            authorizationManager.update(authorization);
          }
        }
        // We don't want an exception on failure here since we still want to approve key. We just haven't been able to send a request
      }
    } else {
      logger.warn("Got a request from a node with same node name as local ("+request.getNodeName()+") !?");
      throw new AuthorizationException("Got a request from node with same node name as local !?");
    }
  }

  /**
   * @see IExchangeManager#deny(AuthorizationRequest)
   * @param request the authorization request to deny
   */
  @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
  @Override
  public void deny(AuthorizationRequest request) {
    AuthorizationRequest incommingRequest = authorizationRequestManager.get(request.getRequestUUID(), false);
    Authorization authorization = authorizationRequestManager.toAuthorization(incommingRequest);
    Authorization current = authorizationManager.getByNodeName(request.getNodeName());
    if (current == null || (current != null && !current.isLocal())) {
      authorization.setLastUpdated(new Date());
      authorization.setLocal(false);
      authorization.setAuthorized(false);
      if (current != null) {
        authorizationManager.updateByNodeName(authorization);
      }
      authorizationRequestManager.remove(request.getRequestUUID());
    } else {
      logger.warn("Got a request from a node with same node name as local ("+request.getNodeName()+") !?");
      throw new AuthorizationException("Got a request from node with same node name as local !?");
    }
  }

  /**
   * @param request the request to remove
   */
  @Override
  public void delete(AuthorizationRequest request) {
    authorizationRequestManager.remove(request.getRequestUUID());
  }

  /**
   * Creates a pooled file request publisher
   * @return the publisher
   */
  protected IPooledFileRequestPublisher createPooledFileRequestPublisher() {
    PooledFileRequestPublisher publisher = new PooledFileRequestPublisher(queueSize, corePoolSize, maxPoolSize);
    publisher.setExchangeManager(this);
    publisher.afterPropertiesSet();
    return publisher;
  }
  
  /**
   * @return the security manager
   */
  public ISecurityManager getSecurityManager() {
    return securityManager;
  }

  /**
   * @param securityManager
   */
  @Autowired
  public void setSecurityManager(ISecurityManager securityManager) {
    this.securityManager = securityManager;
  }

  /**
   * @return the url part that should be placed after the destination url, as an example "/BaltradDex/exchangeManager.htm", if the destination URL is http://localhost:8080, then with the exchange url
   * part, the full URL will be http://localhost:8080/BaltradDex/exchangeManager.htm  
   */
  public String getExchangeUrlPart() {
    return exchangeUrlPart;
  }

  /**
   * @param exchangeUrlPart the exchange url part
   */
  public void setExchangeUrlPart(String exchangeUrlPart) {
    this.exchangeUrlPart = exchangeUrlPart;
  }

  /**
   * @return the pooled publisher map
   */
  public Map<String, IPooledFileRequestPublisher> getPooledPublishers() {
    return pooledPublishers;
  }

  /**
   * @param pooledPublishers the pooled publisher map
   */
  public void setPooledPublishers(Map<String, IPooledFileRequestPublisher> pooledPublishers) {
    this.pooledPublishers = pooledPublishers;
  }
  /**
   * @return the max number of entries in the queue
   */
  public int getQueueSize() {
    return queueSize;
  }

  /**
   * @param queueSize the max number of entries in the queue
   */
  public void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
  }

  /**
   * @return the core thread executor pool size
   */
  public int getCorePoolSize() {
    return corePoolSize;
  }

  /**
   * @param corePoolSize the min thread executor pool size
   */
  public void setCorePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
  }

  /**
   * @return the max thread executor pool size
   */
  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  /**
   * @param maxPoolSize the max thread executor pool size
   */
  public void setMaxPoolSize(int maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
  }
}
