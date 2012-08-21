/* --------------------------------------------------------------------
Copyright (C) 2009-2010 Swedish Meteorological and Hydrological Institute, SMHI,

This file is part of the Beast library.

Beast library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Beast library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with the Beast library library.  If not, see <http://www.gnu.org/licenses/>.
------------------------------------------------------------------------*/

package eu.baltrad.beast.message;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.keyczar.Signer;
import org.keyczar.exceptions.KeyczarException;
import org.springframework.beans.factory.InitializingBean;

import eu.baltrad.beast.InitializationException;
import eu.baltrad.beast.message.mo.BltDataFrameMessage;

/**
 * Defines the message format the is used by DEX. Yes, we have a circular dependency
 * here that ought to be removed some time in the future. Requires that the pgfwk is
 * defined as a separate project with dependency towards DEX and BEAST but for now
 * this is enough since beast and dex is more or less one anyway.
 * @author Anders Henja
 */
public class BltDataFrameMessageFactory implements IBltDataFrameMessageFactory, InitializingBean {
  /**
   * Name of this node
   */
  private String nodeName = null;
  
  /**
   * Address of this node
   */
  private String nodeUrl = null;
  
  /**
   * The server url
   */
  private String serverUrl = null;

  /**
   * Location of the keyczar key
   */
  private String keyczarKey = null;
  
  /**
   * The keyczar signer
   */
  private Signer signer = null;
  
  /**
   * The date format used when providing recipient with a message
   */
  private SimpleDateFormat dateFormat = null;
  
  /**
   * The format string
   */
  private final static String DATE_FORMAT = "E, d MMM yyyy HH:mm:ss z";
  
  /**
   * The date format string
   */
  private String dateFormatString = DATE_FORMAT;
  
  public BltDataFrameMessageFactory() {
    dateFormat = new SimpleDateFormat(DATE_FORMAT);
  }
  
  @Override
  public BltDataFrameMessage createMessage(String filename) {
    String encodedString = Base64.encodeBase64String(getServerUrl().toString().getBytes());
    String dateString = getDateString();
    BltDataFrameMessage result = new BltDataFrameMessage();
    result.addHeader("Node-Name", getNodeName());
    result.addHeader("Node-Address", getNodeUrl());
    result.addHeader("Content-Type", "application/x-hdf5");
    result.addHeader("Content-MD5", encodedString);
    result.addHeader("Date", dateString);
    result.addHeader("Authorization", getNodeName() + ":" + createSignature("POST", getServerUrl(), "application/x-hdf5", encodedString, dateString));
    result.setFilename(filename);
    return result;
  }

  public String getNodeName() {
    return nodeName;
  }

  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  public String getNodeUrl() {
    return nodeUrl;
  }

  public void setNodeUrl(String nodeUrl) {
    this.nodeUrl = nodeUrl;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (nodeName == null || nodeUrl == null || serverUrl == null || keyczarKey == null) {
      throw new InitializationException("properties are missing");
    }
    
    try {
      signer = new Signer(keyczarKey);
    } catch (KeyczarException e) {
      throw new InitializationException(e);
    }
    dateFormat = new SimpleDateFormat(dateFormatString);
  }

  public String getDateFormatString() {
    return dateFormatString;
  }

  public void setDateFormatString(String dateFormatString) {
    if (dateFormatString == null) {
      throw new NullPointerException();
    }
    this.dateFormatString = dateFormatString;
  }

  public String getKeyczarKey() {
    return keyczarKey;
  }

  public void setKeyczarKey(String keyczarKey) {
    this.keyczarKey = keyczarKey;
  }

  /**
   * For test purposes.
   * @param signer the signer
   */
  protected void setSigner(Signer signer) {
    this.signer = signer;
  }
  
  /**
   * @return the signer
   */
  public Signer getSigner() {
    return this.signer;
  }
  
  
  /**
   * @return the date string for now as defined by the date format string
   */
  protected String getDateString() {
    return dateFormat.format(new Date());
  }
  
  /**
   * Creates a signature
   * @param method the method. Typically POST
   * @param url the url
   * @param contentType the content type
   * @param contentMD5 the MD5 content
   * @param date the date
   * @return a signed string
   */
  protected String createSignature(String method, String url, String contentType, String contentMD5, String date) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(method).append("\n");
    buffer.append(url).append("\n");
    buffer.append(contentType).append("\n");
    buffer.append(contentMD5).append("\n");
    buffer.append(date);
    try {
      return signer.sign(buffer.toString());
    } catch (KeyczarException e) {
      throw new DataFrameMessageException(e);
    }
  }
}
