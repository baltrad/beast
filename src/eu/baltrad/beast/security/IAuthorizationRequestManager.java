/**
 * 
 */
package eu.baltrad.beast.security;

import java.util.List;

/**
 * Manages the authorization requests (preferrably in a database)
 * @author anders
 */
public interface IAuthorizationRequestManager {
  /**
   * Adds a request (note, requestuuid must be != null). Upon success, the id will be updated with the new serial
   * @param request the request to add to storage
   */
  public void add(AuthorizationRequest request);
  
  /**
   * @param id the unique id
   * @return the request with specified id
   */
  public AuthorizationRequest get(int id);

  /**
   * @param uuid the unique uuid
   * @param outgoing if the fetched request should be incoming/outgoing request object
   * @return the request with specified id
   */
  public AuthorizationRequest get(String uuid, boolean outgoing);

  /**
   * Returns an authorization request based on remote address. Should not be allowed with more than one.
   * @param remoteAddress the remote address
   * @return the authorization request if found, otherwise null
   */
  public AuthorizationRequest getByRemoteAddress(String remoteAddress);
  
  /**
   * Returns an authorization request based on node name. Should not be allowed with more than one.
   * @param nodeName the node name
   * @return the authorization request if found, otherwise null
   */
  public AuthorizationRequest getByNodeName(String nodeName);
  
  /**
   * @param address the remote host address
   * @return a list of requestes with specified remote host address
   */
  public List<AuthorizationRequest> findByRemoteHost(String address);
  
  /**
   * Allows searching based on outgoing or incomming requests
   * @param outgoing if outgoing or not
   * @return list of authorization requests
   */
  public List<AuthorizationRequest> findByOutgoing(boolean outgoing);
  
  /**
   * @return all authorization requests. Either incomming or outgoing.
   */
  public List<AuthorizationRequest> list();
  
  /**
   * Updates the authorization request by identifying row from id
   * @param request the request to update
   */
  public void update(AuthorizationRequest request);
  
  /**
   * @param requestId the request id to the request to be removed
   */
  public void remove(int requestId);
  
  /**
   * @param uuid the request uuid to the request to be removed
   */
  public void remove(String uuid);
  
  /**
   * @return a unique UUID that can be used as request uuid
   */
  public String createUUID();
  
  /**
   * @param request the request to be translated into an authorization
   * @return the authorization instance
   */
  public Authorization toAuthorization(AuthorizationRequest request);
  
  /**
   * Creates an authorization request
   * @return the authorization request
   */
  public AuthorizationRequest createRequest();
}
