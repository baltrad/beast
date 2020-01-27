package eu.baltrad.beast.security;

import java.util.Date;

/**
 * An authorization request
 * @author anders
 */
public class AuthorizationRequest {
  /**
   * Unique ID
   */
  private int id;
  
  /**
   * The node name of the requestor  
   */
  private String nodeName;
  
  /**
   * The node email of the requestor
   */
  private String nodeEmail;
  
  /**
   * THe node address of the requestor
   */
  private String nodeAddress;
  
  /**
   * Checksum of the content
   */
  private String checksum;
  
  /**
   * The MD5 encoded public key 
   */
  private byte[] publicKey;
  
  /**
   * The message
   */
  private String message;
  
  /**
   * Indicates from where the request came from
   */
  private String remotehost;
  
  /**
   * The destination address 
   */
  private String remoteAddress;
  
  /**
   * When request arrived
   */
  private Date receivedAt;
  
  /**
   * The uuid used within the request, can usually be matched against @ref {@link Authorization#getConnectionUUID()}
   */
  private String requestuuid;
  
  /**
   * If request is outgoing or incomming. I.e. If outgoing, the authorization locally will have outgoing = true, and on the remote host it's going to be outgoing = false
   * and vice versa.
   */
  private boolean outgoing;
  
  /**
   * When approving a request and autorequest is set to true, then a request will automatically
   * be sent to the requestor unless there already is an approved authorization to the remote
   * host.
   */
  private boolean autorequest = true;
  
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
   * @return the checksum (off the public key)
   */
  public String getChecksum() {
    return checksum;
  }
  
  /**
   * @param checksum (off the public key)
   */
  public void setChecksum(String checksum) {
    this.checksum = checksum;
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
   * @return the message
   */
  public String getMessage() {
    return message;
  }
  
  /**
   * @param message the message
   */
  public void setMessage(String message) {
    this.message = message;
  }
  
  /**
   * @return if request is outoing or incomming
   */
  public boolean isOutgoing() {
    return outgoing;
  }
  
  /**
   * @param outgoing if request is outoing or incomming
   */
  public void setOutgoing(boolean outgoing) {
    this.outgoing = outgoing;
  }
  
  /**
   * @return unique id
   */
  public int getId() {
    return id;
  }
  
  /**
   * @param id unique id
   */
  public void setId(int id) {
    this.id = id;
  }
  
  /**
   * @return the time of reception of the request
   */
  public Date getReceivedAt() {
    return receivedAt;
  }
  
  /**
   * @param receivedAt the time of reception of the request
   */
  public void setReceivedAt(Date receivedAt) {
    this.receivedAt = receivedAt;
  }
  
  /**
   * @return the remote host where the message originated from. Not to be confused with remoteaddress.
   */
  public String getRemoteHost() {
    return remotehost;
  }
  
  /**
   * @param remotehost the remote host where the message originated from.  Not to be confused with remoteaddress.
   */
  public void setRemoteHost(String remotehost) {
    this.remotehost = remotehost;
  }
  
  /**
   * @return the request UUID
   */
  public String getRequestUUID() {
    return requestuuid;
  }
  
  /**
   * @param requestuuid the request UUID
   */
  public void setRequestUUID(String requestuuid) {
    this.requestuuid = requestuuid;
  }

  /**
   * @return if auto request should be performed
   */
  public boolean isAutorequest() {
    return autorequest;
  }

  /**
   * @param autorequest if auto request should be performed
   */
  public void setAutorequest(boolean autorequest) {
    this.autorequest = autorequest;
  }

  /**
   * @return the remote address (to which request has been sent)
   */
  public String getRemoteAddress() {
    return remoteAddress;
  }

  /**
   * @param remoteAddress the remote address (to which request has been sent)
   */
  public void setRemoteAddress(String remoteAddress) {
    this.remoteAddress = remoteAddress;
  }
}
