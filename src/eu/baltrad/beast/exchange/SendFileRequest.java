/**
 * 
 */
package eu.baltrad.beast.exchange;

import java.util.Date;

/**
 * The file request used for posting / sending files to a remote node.
 * Required information when posting a request is 
 * @author anders
 */
public class SendFileRequest {
  /**
   * Remote address including end point (i.e. full URL)
   */
  private String address;
  
  /**
   * This hosts node name
   */
  private String nodeName;
  
  /**
   * The content type. Typically application/x-hdf5 when posting HDF5 files.
   */
  private String contentType;
  
  /**
   * The data
   */
  private byte[] data;
  
  /**
   * Date when posting the request
   */
  private Date date;
  
  /**
   * Metadata that can be used in for example callback to identify data
   */
  private Object metadata;
  
  /**
   * @return this hosts address
   */
  public String getAddress() {
    return address;
  }
  
  /**
   * @param address this hosts address
   */
  public void setAddress(String address) {
    this.address = address;
  }
  
  /**
   * @return the remote host node name
   */
  public String getNodeName() {
    return nodeName;
  }
  
  /**
   * @param nodeName the remote host node name
   */
  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }
  
  /**
   * @return the content type
   */
  public String getContentType() {
    return contentType;
  }
  
  /**
   * @param contentType the content type
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
  
  /**
   * @return the data
   */
  public byte[] getData() {
    return data;
  }
  
  /**
   * @param data the data
   */
  public void setData(byte[] data) {
    this.data = data;
  }
  
  /**
   * @return date of sending
   */
  public Date getDate() {
    return date;
  }
  
  /**
   * @param date the date of sending
   */
  public void setDate(Date date) {
    this.date = date;
  }
  
  /**
   * @return metadata that can be used in the callbacks to idenfity / get relevant information 
   */
  public Object getMetadata() {
    return metadata;
  }

  /**
   * @param metadata that can be used in the callbacks to idenfity / get relevant information
   */
  public void setMetadata(Object metadata) {
    this.metadata = metadata;
  }
}
