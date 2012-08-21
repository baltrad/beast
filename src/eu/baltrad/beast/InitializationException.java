package eu.baltrad.beast;

public class InitializationException extends RuntimeException {
  /**
   * The default serial uid 
   */
  private static final long serialVersionUID = 1L;

  /**
   * @see RuntimeException#RuntimeException()
   */
  public InitializationException() {
    super();
  }
  
  /**
   * @see RuntimeException#RuntimeException(String)
   */
  public InitializationException(String message) {
    super(message);
  }

  /**
   * @see RuntimeException#RuntimeException(Throwable)
   */
  public InitializationException(Throwable t) {
    super(t);
  }

  /**
   * @see RuntimeException#RuntimeException(String, Throwable)
   */
  public InitializationException(String message, Throwable t) {
    super(message, t);
  }

}
