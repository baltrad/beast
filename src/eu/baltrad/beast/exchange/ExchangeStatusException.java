/**
 * 
 */
package eu.baltrad.beast.exchange;

/**
 * @author anders
 *
 */
public class ExchangeStatusException extends ExchangeException {
  /**
   * The default serial uid 
   */
  private static final long serialVersionUID = 1L;

  /**
   * The status. Usually received when an error occurs in the actual communication with a remote node.
   */
  private int status = 0;
  
  /**
   * @see RuntimeException#RuntimeException()
   */
  public ExchangeStatusException() {
    super();
  }
  
  /**
   * @see RuntimeException#RuntimeException(String)
   */
  public ExchangeStatusException(String message) {
    super(message);
  }

  /**
   * @see RuntimeException#RuntimeException(Throwable)
   */
  public ExchangeStatusException(Throwable t) {
    super(t);
  }

  /**
   * @see RuntimeException#RuntimeException(String, Throwable)
   */
  public ExchangeStatusException(String message, Throwable t) {
    super(message, t);
  }  
  
  /**
   * Exception when passing the status information
   * @param message
   * @param status
   */
  public ExchangeStatusException(String message, int status) {
    super(message);
    this.status = status;
  }

  /**
   * Exception when passing the status information
   * @param message
   * @param status
   */
  public ExchangeStatusException(String message, Throwable t, int status) {
    super(message, t);
    this.status = status;
  }

  
  /**
   * @return the status
   */
  public int getStatus() {
    return status;
  }
}
