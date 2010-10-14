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
package eu.baltrad.beast.message.mo;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import eu.baltrad.beast.message.IBltXmlMessage;

/**
 * @author Anders Henja
 *
// Create XML string header using given encoding
StringBody sbXMLHeader = new StringBody( xmlHdrStr, MIME_MULTIPART, CHARSET );
// Create file content body
File f = new File( absFilePath );
ContentBody cbFile = new FileBody( f, MIME_MULTIPART );
// Add XML header string
this.addPart( XML_PART, sbXMLHeader );
// Add file body content
this.addPart( FILE_PART, cbFile );
*/
public class BltDataFrameMessage implements IBltXmlMessage  {
  /**
   * Root tag
   */
  public static final String BLT_FRAME = "baltrad_frame";
    
  private static final String BLT_FRAME_HEADER = "header";
  
  private static final String BLT_FRAME_MIME_TYPE = "mimetype";
  
  private static final String BLT_FRAME_CONTENT = "content";
  
  private static final String BLT_FRAME_CONTENT_TYPE = "type";
  
  private static final String BLT_FRAME_SENDER = "sender_node_name";
  
  private static final String BLT_FRAME_FILE_NAME = "name";
  
  private static final String BLT_FRAME_CHNL_NAME = "channel";
  
  /**
   * Form-data multipart string
   */
  private static final String BLT_FRAME_MIME_MULTIPART = "multipart/form-data";
  
  /**
   * Content type indicating file transfer
   */
  public static final String BLT_FRAME_CONTENT_TYPE_FILE = "file";
  
  /**
   * Sender
   */
  private String sender = null;
  
  /**
   * Filename
   */
  private String filename = null;
  
  /**
   * Channel
   */
  private String channel = null;
  
  /**
   * @see eu.baltrad.beast.message.IBltXmlMessage#fromDocument(org.dom4j.Document)
   */
  @Override
  public void fromDocument(Document dom) {
    // TODO Auto-generated method stub
  }

  /**
   * @see eu.baltrad.beast.message.IBltXmlMessage#toDocument()
   */
  @Override
  public Document toDocument() {
    Document document = DocumentHelper.createDocument();
    Element root = document.addElement(BLT_FRAME);
    Element header = root.addElement(BLT_FRAME_HEADER);
    header.addAttribute(BLT_FRAME_MIME_TYPE, BLT_FRAME_MIME_MULTIPART);
    header.addAttribute(BLT_FRAME_SENDER, getSender());
    Element content = root.addElement(BLT_FRAME_CONTENT);
    content.addAttribute(BLT_FRAME_CONTENT_TYPE, BLT_FRAME_CONTENT_TYPE_FILE);
    content.addAttribute(BLT_FRAME_FILE_NAME, getFileBasename());
    content.addAttribute(BLT_FRAME_CHNL_NAME, getChannel());
    
    return document;
  }
  /*
    // XML element / document encoding
    private static final String XML_ENCODING = "UTF-8";
    // XML elements / available MIME types
    public static final String MIME_MULTIPART = "multipart/form-data";
    // Character set
    private static final Charset CHARSET = Charset.forName( XML_ENCODING );
    // Multipart message parts identifiers
    public static final String XML_PART = "<bf_xml/>";
    public static final String FILE_PART = "<bf_file/>";   * 
  StringBody sbXMLHeader = new StringBody( xmlHdrStr, MIME_MULTIPART, CHARSET );
  // Create file content body
  File f = new File( absFilePath );
  ContentBody cbFile = new FileBody( f, MIME_MULTIPART );
  // Add XML header string
  this.addPart( XML_PART, sbXMLHeader );
  // Add file body content
  this.addPart( FILE_PART, cbFile );
  */
  
  /**
   * @param sender the sender to set
   */
  public void setSender(String sender) {
    this.sender = sender;
  }

  /**
   * @return the sender
   */
  public String getSender() {
    return sender;
  }

  /**
   * @param filename the filename to set
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }
  
  /**
   * @return the filename that is stripped of the directory name
   */
  public String getFileBasename() {
    if (filename != null) {
      return filename.substring(filename.lastIndexOf(File.separator)+1);
    }
    return null;
  }

  /**
   * @param channel the channel to set
   */
  public void setChannel(String channel) {
    this.channel = channel;
  }

  /**
   * @return the channel
   */
  public String getChannel() {
    return channel;
  }
}
