/**
 * 
 */
package eu.baltrad.beast.exchange;

/**
 * @author anders
 */
public interface IPooledFileRequestPublisher {
  /**
   * Publishes a send file request on the thread pool
   * @param request the request
   * @param callback the callback that should be updated with progress information
   */
  public void publish(SendFileRequest request, SendFileRequestCallback callback);
}
