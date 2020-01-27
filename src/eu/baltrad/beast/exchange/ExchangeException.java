/**
 * 
 */
package eu.baltrad.beast.exchange;

/**
 * @author anders
 *
 */
public class ExchangeException  extends RuntimeException {
  /**
   * The default serial uid 
   */
  private static final long serialVersionUID = 1L;

  /**
   * @see RuntimeException#RuntimeException()
   */
  public ExchangeException() {
    super();
  }
  
  /**
   * @see RuntimeException#RuntimeException(String)
   */
  public ExchangeException(String message) {
    super(message);
  }

  /**
   * @see RuntimeException#RuntimeException(Throwable)
   */
  public ExchangeException(Throwable t) {
    super(t);
  }

  /**
   * @see RuntimeException#RuntimeException(String, Throwable)
   */
  public ExchangeException(String message, Throwable t) {
    super(message, t);
  }
}


