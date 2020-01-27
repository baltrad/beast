/**
 * 
 */
package eu.baltrad.beast.exchange.json;

/**
 * @author anders
 *
 */
public class RequestMapperException  extends RuntimeException {
  /**
   * The default serial uid 
   */
  private static final long serialVersionUID = 1L;

  /**
   * @see RuntimeException#RuntimeException()
   */
  public RequestMapperException() {
    super();
  }
  
  /**
   * @see RuntimeException#RuntimeException(String)
   */
  public RequestMapperException(String message) {
    super(message);
  }

  /**
   * @see RuntimeException#RuntimeException(Throwable)
   */
  public RequestMapperException(Throwable t) {
    super(t);
  }

  /**
   * @see RuntimeException#RuntimeException(String, Throwable)
   */
  public RequestMapperException(String message, Throwable t) {
    super(message, t);
  }
}


