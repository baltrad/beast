package eu.baltrad.beast.security;

import org.apache.http.client.methods.HttpUriRequest;

import eu.baltrad.beast.admin.Command;
import eu.baltrad.beast.security.crypto.Signer;
import eu.baltrad.beast.security.crypto.Verifier;

public interface ISecurityManager {
  /**
   * Adds the public key for usage by the security manager.
   * @param authorization the authorization
   * @returns the folder name where the key has been placed
   */
  public String expandPublicKey(Authorization authorization);

  /**
   * Adds the private key for usage by the security manager.
   * @param authorization the authorization
   * @returns the folder name where the key has been placed
   */
  public String expandPrivateKey(Authorization authorization);

  /**
   * Returns the signer
   * @param nodeName name of the signer, usually local node name
   * @return the node name
   */
  public Signer getSigner(String nodeName);
  
  /**
   * Returns the verifier
   * @param nodeName the node name
   * @return the verifier
   */
  public Verifier getVerifier(String nodeName);
  
  /**
   * Validates the message according to the public key verifier
   * @param nodeName the node name
   * @param signature the signature
   * @param message the message validated
   * @return if the message is signed properly
   */
  public boolean validate(String nodeName, String signature, String message);
  
  /**
   * Validates a command so that it is valid to run
   * @param nodeName The node name
   * @param messageDate The date when sending message
   * @param signature The signature in the message
   * @param command The command to be verified
   * @return if command should be accepted or not
   */
  public boolean validate(String nodeName, String messageDate, String signature, Command command);

  
  /**
   * @return the local node authorization
   */
  public Authorization getLocal();
  
  /**
   * @param auth the local node authorization
   */
  public void setLocal(Authorization auth);

  /**
   * @param message the message to create a signature from
   * @return the signature
   */
  public String createSignature(String message);
  
  /**
   * @param nodeName the node name
   * @return if specified node is classified as an injector and also if it has been authorized.
   */
  public boolean isInjector(String nodeName);
  
  /**
   * @return the local node name
   */
  public String getLocalNodeName();
  
  /**
   * Function that creates the message to be signed from an uri request. This is ill-placed here and should be moved somewhere more appropriate.
   * @param request the request the request
   * @return the signature message
   */
  public String createSignatureMessage(HttpUriRequest request);
}
