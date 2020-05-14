/**
 * 
 */
package eu.baltrad.beast.exchange;

/**
 * Callback used when sending a file request. Useful when using {@link ExchangeManager#sendAsync(SendFileRequest, SendFileRequestCallback)
 * @author anders
 */
public interface SendFileRequestCallback {
  /**
   * @param request the file request that has been published (with success)
   */
  public void filePublished(SendFileRequest request);
  
  /**
   * @param request the file request that has been published (with success)
   * @param redirectAddress the address redirected to
   * @param statusCode the status code indicating redirect
   */
  public void filePublished(SendFileRequest request, String redirectAddress, int statusCode);
  
  /**
   * Invoked whenever a file publication fails for some reason.
   * @param request the request
   * @param response the response indicating failure
   */
  public void filePublicationFailed(SendFileRequest request, ExchangeResponse response);
}
