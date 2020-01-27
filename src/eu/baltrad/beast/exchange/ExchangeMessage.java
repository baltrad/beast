/**
 * 
 */
package eu.baltrad.beast.exchange;

import eu.baltrad.beast.security.AuthorizationRequest;

/**
 * The incomming exchange message. Currently only supports authorization requests
 * @author anders
 */
public class ExchangeMessage {
  /**
   * Protocol version
   */
  private String version;
  
  /**
   * The authorization request
   */
  private AuthorizationRequest authorizationRequest;
  
  /**
   * @return the protocol version
   */
  public String getVersion() {
    return version;
  }

  /**
   * @param version the protocol version
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * @return if the authorization request object is set
   */
  public boolean isAuthorizationRequest()  {
    return getAuthorizationRequest() != null;
  }
  
  /**
   * @return the authorization request
   */
  public AuthorizationRequest getAuthorizationRequest() {
    return authorizationRequest;
  }

  /**
   * @param authorizationRequest the authorization request
   */
  public void setAuthorizationRequest(AuthorizationRequest authorizationRequest) {
    this.authorizationRequest = authorizationRequest;
  }
  
}
