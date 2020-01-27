/**
 * 
 */
package eu.baltrad.beast.exchange;

import java.io.InputStream;

import eu.baltrad.beast.security.AuthorizationRequest;

/**
 * @author anders
 */
public interface IExchangeManager {
  /**
   * Sends a file
   * @param request the file request to send
   * @returns the exchange response
   */
  public ExchangeResponse send(SendFileRequest request);
  
  /**
   * Sends a file
   * @param request the request
   * @param callback status updates during transmission
   * @returns the exchange response
   */
  public ExchangeResponse send(SendFileRequest request, SendFileRequestCallback callback);

  /**
   * Publishes a send file request asynchronously.
   * @param request the request
   * @param callback the callback
   */
  public void sendAsync(SendFileRequest request, SendFileRequestCallback callback);
  
  /**
   * Parses incomming data stream and tries to termine what type of message that is arriving
   * @param stream
   * @return
   */
  public ExchangeMessage parse(InputStream stream);
  public ExchangeMessage receive(ExchangeMessage message);
  public ExchangeMessage receive(InputStream stream);
  
  /**
   * Requests authorization from the remote address 
   * @param remoteAddress the remote address without page specific information. For example use http://localhost:8080 instead of http://localhost:8080/BaltradDex/...
   * @param message the message
   * @return the send authorization request
   */
  public AuthorizationRequest requestAuthorization(String remoteAddress, String message);
  
  /**
   * Approves an authorization request
   * @param request the request to approve
   */
  public void approve(AuthorizationRequest request);
  
  /**
   * Denies an authorization request
   * @param request the request to deny
   */
  public void deny(AuthorizationRequest request);
  
  /**
   * @param request the request to remove
   */
  void delete(AuthorizationRequest request);
}
