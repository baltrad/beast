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
package eu.baltrad.beast.adaptor.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataFrameMessage;

/**
 * @author Anders Henja
 */
public class HttpConnector implements IHttpConnector {
  /**
   * The url this connector is communicating with
   */
  private String url = null;

  public HttpConnector() {
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.http.IHttpConnector#send(IBltMessage)
   */
  @Override
  public void send(IBltMessage message) {
    if (message.getClass() == BltDataFrameMessage.class) {
      BltDataFrameMessage msg = (BltDataFrameMessage)message;
      HttpClient httpClient = new DefaultHttpClient();
      HttpPost httpPost = createPost( getUrl() );
      HttpEntity entity = generateHttpEntity(msg);
      httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
      httpPost.setEntity(entity);
      Iterator<String> keys = msg.getHeaders();

      while (keys.hasNext()) {
        String key = keys.next();
        httpPost.addHeader(key, msg.getHeader(key));
      }
      try {
        HttpResponse response = httpClient.execute( httpPost );
        HttpEntity resEntity = response.getEntity();
        if( resEntity != null ) {
          EntityUtils.consume(resEntity);
        }
      } catch (Exception e) {
        throw new HttpConnectorException(e);
      } finally {
        httpClient.getConnectionManager().shutdown();    
      }
    }
  }
  
  protected HttpPost createPost(String url) {
    return new HttpPost(url);
  }

  /**
   * @param url the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }
  
  /**
   * Generates a multipart entity for usage when sending messages to
   * the dex.
   * @param message
   * @return the http entity
   */
  protected HttpEntity generateHttpEntity(BltDataFrameMessage message) {
    try {
      FileInputStream fis = new FileInputStream(message.getFilename());
      ByteArrayEntity entity = new ByteArrayEntity(IOUtils.toByteArray(fis));
      return entity;
    } catch (IOException e) {
      throw new HttpConnectorException();
    }
  }
}
