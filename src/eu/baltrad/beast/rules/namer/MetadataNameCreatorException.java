package eu.baltrad.beast.rules.namer;

public class MetadataNameCreatorException  extends RuntimeException {
  /**
   * The default serial uid 
   */
  private static final long serialVersionUID = 1L;

  /**
   * @see RuntimeException#RuntimeException()
   */
  public MetadataNameCreatorException() {
    super();
  }
  
  /**
   * @see RuntimeException#RuntimeException(String)
   */
  public MetadataNameCreatorException(String message) {
    super(message);
  }

  /**
   * @see RuntimeException#RuntimeException(Throwable)
   */
  public MetadataNameCreatorException(Throwable t) {
    super(t);
  }

  /**
   * @see RuntimeException#RuntimeException(String, Throwable)
   */
  public MetadataNameCreatorException(String message, Throwable t) {
    super(message, t);
  }
}
