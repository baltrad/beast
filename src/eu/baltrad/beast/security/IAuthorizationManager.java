/**
 * 
 */
package eu.baltrad.beast.security;

import java.util.List;

/**
 * @author anders
 *
 */
public interface IAuthorizationManager {
  /**
   * @param auth the authorization to be added
   */
  public void add(Authorization auth);
  
  /**
   * @param auth the authorization to be updated. The connectionuuid is the primary identifier and will be updated accordingly.
   */
  public void update(Authorization auth);
  
  /**
   * Removed the authorization with specified uuid
   * @param uuid the uuid of authorization to be removed
   */
  public void delete(String uuid);
  
  /**
   * @param auth the authorization to be updated. The node name will be used as identifier.
   */
  public void updateByNodeName(Authorization auth);
  
  /**
   * @param uuid the uuid of the authorization to be fetched
   * @return the authorization if any found
   * @throws DataAccessException upon error
   */
  public Authorization get(String uuid);

  /**
   * Returns the authorization based on node name 
   * @param nodeName the node name
   * @return the authorization if found, otherwise null
   */
  public Authorization getByNodeName(String nodeName);
  
  /**
   * @return the local authorization entry if any
   */
  public Authorization getLocal();
  
  /**
   * @return a list of authorization entries
   */
  public List<Authorization> list();
  
  /**
   * Creates a request from the local authorization and the remote address
   * @param message the message to be passed to the target
   * @return the authorization request
   */
  public AuthorizationRequest createAuthorizationRequest(String message);

  /**
   * Creates a request from an authorization (usually the local authorization) and the remote address
   * @param authorization the authorization to be used
   * @param message the message to be passed to the target
   * @return the authorization request
   */
  public AuthorizationRequest toAuthorizationRequest(Authorization authorization, String message);
}
