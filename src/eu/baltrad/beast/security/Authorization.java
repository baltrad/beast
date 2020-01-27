package eu.baltrad.beast.security;

import java.util.Date;

/**
 * Container for keeping track of an authorization. Used by both remote and local node
 * @author anders
 */
public class Authorization {
  /**
   * Node name
   */
  private String nodeName;
  
  /**
   * Email
   */
  private String nodeEmail;
  
  /**
   * Http IP address including port number. Not the full URL though 
   */
  private String nodeAddress;
  
  /**
   * Like @ref {@link #nodeAddress} but the redirected address 
   */
  private String redirectedAddress;
  
  /**
   * MD5 encoded public key
   */
  private byte[] publicKey;
  
  /**
   * Path where the public key has been stored after approval
   */
  private String publicKeyPath;
  
  /**
   * MD5 encoded private key
   */
  private byte[] privateKey;
  
  /**
   * Path where the private key is stored
   */
  private String privateKeyPath;
  
  /**
   * When the authorization was last updated.
   */
  private Date lastUpdated;
  
  /**
   * If the authorization is authorized to be used or not
   */
  private boolean authorized = false;
  
  /**
   * If the authorization indicates that no subscription checks needs to be performed. Just key check.
   */
  private boolean injector = false;
  
  /**
   * If this authorization reflects to the local node
   */
  private boolean local = false;
  
  /**
   * The connection uuid reflecting this node. 
   */
  private String connectionuuid;
  
  /**
   * @return the node name
   */
  public String getNodeName() {
    return nodeName;
  }
  
  /**
   * @param nodeName the node name
   */
  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }
  
  /**
   * @return the node email
   */
  public String getNodeEmail() {
    return nodeEmail;
  }
  
  /**
   * @param nodeEmail the node email
   */
  public void setNodeEmail(String nodeEmail) {
    this.nodeEmail = nodeEmail;
  }
  
  /**
   * @return the node address
   */
  public String getNodeAddress() {
    return nodeAddress;
  }
  
  /**
   * @param nodeAddress the node address
   */
  public void setNodeAddress(String nodeAddress) {
    this.nodeAddress = nodeAddress;
  }
  
  /**
   * @return the public key
   */
  public byte[] getPublicKey() {
    return publicKey;
  }
  
  /**
   * @param publicKey the public key
   */
  public void setPublicKey(byte[] publicKey) {
    this.publicKey = publicKey;
  }
  
  /**
   * @return the public key path
   */
  public String getPublicKeyPath() {
    return publicKeyPath;
  }
  
  /**
   * @param publicKeyPath the public key path
   */
  public void setPublicKeyPath(String publicKeyPath) {
    this.publicKeyPath = publicKeyPath;
  }
  
  /**
   * @return the private key
   */
  public byte[] getPrivateKey() {
    return privateKey;
  }
  
  /**
   * @param privateKey the private key
   */
  public void setPrivateKey(byte[] privateKey) {
    this.privateKey = privateKey;
  }
  
  /**
   * @return the private key path
   */
  public String getPrivateKeyPath() {
    return privateKeyPath;
  }
  
  /**
   * @param privateKeyPath the private key path
   */
  public void setPrivateKeyPath(String privateKeyPath) {
    this.privateKeyPath = privateKeyPath;
  }
  
  /**
   * @return last update
   */
  public Date getLastUpdated() {
    return lastUpdated;
  }
  
  /**
   * @param lastUpdated last update
   */
  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }
  
  /**
   * @return if this authorization is authorized access
   */
  public boolean isAuthorized() {
    return authorized;
  }
  
  /**
   * @param authorized if this authorization is authorized access
   */
  public void setAuthorized(boolean authorized) {
    this.authorized = authorized;
  }
  
  /**
   * @return if this authorization represents the local node
   */
  public boolean isLocal() {
    return local;
  }
  
  /**
   * @param local if this authorization represents the local node
   */
  public void setLocal(boolean local) {
    this.local = local;
  }
  
  /**
   * @return the connection uuid identifiying this node
   */
  public String getConnectionUUID() {
    return connectionuuid;
  }
  
  /**
   * @param connectionuuid the uuid identifying this node
   */
  public void setConnectionUUID(String connectionuuid) {
    this.connectionuuid = connectionuuid;
  }
  
  /**
   * @return if this authorization should be set with injector privileges
   */
  public boolean isInjector() {
    return injector;
  }
  
  /**
   * @param injector if this authorization should be set with injector privileges
   */
  public void setInjector(boolean injector) {
    this.injector = injector;
  }
  
  /**
   * @return the redirected address for this authorization
   */
  public String getRedirectedAddress() {
    return redirectedAddress;
  }
  
  /**
   * @param redirectedAddress the redirected address for this authorization
   */
  public void setRedirectedAddress(String redirectedAddress) {
    this.redirectedAddress = redirectedAddress;
  }

}
