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

import java.io.File;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.dom4j.Document;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataFrameMessage;

/**
 * @author Anders Henja
 *
 */
public class HttpConnector implements IHttpConnector {
  private String url = null;
  
  private final static String MIME_MULTIPART = "multipart/form-data";
  private static final String XML_ENCODING = "UTF-8";
  private static final Charset CHARSET = Charset.forName( XML_ENCODING );
  public static final String XML_PART = "<bf_xml/>";
  public static final String FILE_PART = "<bf_file/>";
  
  public HttpConnector() {
  }
  
  /**
   * @see eu.baltrad.beast.adaptor.http.IHttpConnector#send(IBltMessage)
   */
  @Override
  public void send(IBltMessage message) {
    HttpEntity entity = null;
    
    if (message.getClass() == BltDataFrameMessage.class) {
      entity = generateHttpEntity((BltDataFrameMessage)message);
    }
    
    if (entity != null) {
      send(entity);
    }
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
   * Sends the entity
   * @param entity
   */
  protected void send(HttpEntity entity) {
    HttpClient httpClient = new DefaultHttpClient();
    try {
      httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
          HttpVersion.HTTP_1_1);
      HttpPost httpPost = new HttpPost( getUrl() );
      httpPost.setEntity(entity);
      HttpResponse response = httpClient.execute( httpPost );
      HttpEntity resEntity = response.getEntity();
      if( resEntity != null ) {
        resEntity.consumeContent();
      }
    } catch (Exception e) {
      throw new HttpConnectorException(e);
    } finally {
      httpClient.getConnectionManager().shutdown();    
    }
  }
  
  /**
   * Generates a multipart entity for usage when sending messages to
   * the dex.
   * @param message
   * @return the http entity
   */
  protected HttpEntity generateHttpEntity(BltDataFrameMessage message) {
    MultipartEntity entity = new MultipartEntity();
    try {
      Document d = message.toDocument();
      d.setXMLEncoding(XML_ENCODING);
      String xml = d.asXML();
    
      StringBody xmlBody = new StringBody( xml, MIME_MULTIPART, CHARSET );
    
      File f = new File(message.getFilename());
      ContentBody contentBody = new FileBody(f, MIME_MULTIPART);
    
      entity.addPart( XML_PART, xmlBody );
      entity.addPart( FILE_PART, contentBody );
    
      return entity;
    } catch (Exception e) {
      throw new HttpConnectorException(e);
    }
  }
}
