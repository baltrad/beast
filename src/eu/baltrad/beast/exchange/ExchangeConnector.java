/**
 * 
 */
package eu.baltrad.beast.exchange;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.methods.HttpUriRequest;

import eu.baltrad.beast.security.AuthorizationRequest;

/**
 * Interface that helps out when communicating with a remote node. 
 * @author anders
 */
public interface ExchangeConnector {
  /**
   * Sends an authorization request to a remote host
   * @param remoteAddress the remote address
   * @param request the request
   * @return a response
   */
  public ExchangeResponse send(String remoteAddress, AuthorizationRequest request);
  
  /**
   * Uses "OLD" style DEX for sending an authorization request to a remote node. If
   * for example 404 is returned when using @ref {@link #send(String, AuthorizationRequest)}
   * then a good idea can be to try this API. Note, since this is old style it is set to
   * deprecated at creation.
   * @deprecated
   * @param remoteAddress the remote address
   * @param request the request
   * @return the response
   */
  public ExchangeResponse sendDexStyle(String remoteAddress, AuthorizationRequest request);
  
  /**
   * Sends a file request to the remote node. Uses "OLD" style DEX protocol which currently is the
   * only available.
   * @param request the request
   * @return the response
   */
  public ExchangeResponse send(SendFileRequest request);
  
  /**
   * Parses an input stream (most likely formatted as a json object) and translates it into an object within the exchange message
   * @param json the input stream
   * @return the exchange message
   */
  public ExchangeMessage parse(InputStream json);
}
