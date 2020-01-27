/**
 * 
 */
package eu.baltrad.beast.exchange;

import javax.servlet.http.HttpServletResponse;

/**
 * The returned response when sending an exchange message
 * @author anders
 */
public class ExchangeResponse {
  /**
   * The status code (http code). @see {@link HttpServletResponse} for possible codes
   */
  private int statusCode;
  
  /**
   * Message indicating an eventual problem
   */
  private String message;
  
  /**
   * If the targeted host indicates that the address is redirected
   */
  private boolean redirected;
  
  /**
   * The redirect address
   */
  private String redirectAddress;
  
  /**
   * The exchange response
   * @param statusCode the status code
   */
  public ExchangeResponse(int statusCode) {
    this.statusCode = statusCode;
  }

  /**
   * Constructor
   * @param statusCode the status code
   * @param message the message
   */
  public ExchangeResponse(int statusCode, String message) {
    this.statusCode = statusCode;
    this.message = message;
  }

  /**
   * @return the status code
   */
  public int statusCode() {
    return this.statusCode;
  }

  /**
   * @return if redirect indication
   */
  public boolean isRedirected() {
    return redirected;
  }

  /**
   * @param redirected if there is an indication that address has been redirect
   */
  public void setRedirected(boolean redirected) {
    this.redirected = redirected;
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
   * @return the redirect address
   */
  public String getRedirectAddress() {
    return redirectAddress;
  }

  /**
   * @param redirectAddress the redirect address
   */
  public void setRedirectAddress(String redirectAddress) {
    this.redirectAddress = redirectAddress;
  }
}
