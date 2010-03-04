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

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import eu.baltrad.beast.message.IBltXmlMessage;
import eu.baltrad.beast.message.MessageParserException;

/**
 * @author Anders Henja
 */
public class BltGenerateMessage implements IBltXmlMessage {
  public static final String BLT_GENERATE = "bltgenerate";
  
  /**
   * The algorithm identifier.
   */
  private String algorithm = null;
  
  /**
   * The file(s) that should be used.
   */
  private String[] files = new String[]{}; 
                 
  /**
   * The arguments
   */
  private String[] arguments = new String[]{};
  
  /**
   * @param algorithm the algorithm to set
   */
  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  /**
   * @return the algorithm
   */
  public String getAlgorithm() {
    return algorithm;
  }

  /**
   * @param files the files to set
   */
  public void setFiles(String[] files) {
    if (files == null) {
      throw new IllegalArgumentException("files must not be null");
    }
    this.files = files;
  }

  /**
   * @return the files
   */
  public String[] getFiles() {
    return files;
  }

  /**
   * @param arguments the arguments to set
   */
  public void setArguments(String[] arguments) {
    if (arguments == null) {
      throw new IllegalArgumentException("arguments must not be null");
    }    
    this.arguments = arguments;
  }

  /**
   * @return the arguments
   */
  public String[] getArguments() {
    return arguments;
  }
  
  /**
   * @see eu.baltrad.beast.message.IBltXmlMessage#fromDocument(org.dom4j.Document)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void fromDocument(Document dom) {
    int index = 0;
    if (!dom.getRootElement().getName().equals(BLT_GENERATE)) {
      throw new MessageParserException("Atempting to create BltGenerate from: " + dom.asXML());
    }
    setAlgorithm(dom.valueOf("//bltgenerate/algorithm"));
    List<Node> nodes = dom.selectNodes("//bltgenerate/filelist/file");
    this.files = new String[nodes.size()];
    for (Node node : nodes) {
      this.files[index++] = node.getText();
    }
    
    index = 0;
    nodes = dom.selectNodes("//bltgenerate/arguments/arg");
    this.arguments = new String[nodes.size()];
    for (Node node : nodes) {
      this.arguments[index++] = node.getText();
    }
  }
  
  /**
   * @see eu.baltrad.beast.message.IBltXmlMessage#toDocument()
   */
  @Override
  public Document toDocument() {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement(BLT_GENERATE);
    el.addElement("algorithm").addText(this.algorithm);
    Element elFiles = el.addElement("filelist");
    for (int i = 0; i < this.files.length; i++) {
      if (this.files[i] != null) {
        elFiles.addElement("file").addText(this.files[i]);
      }
    }
    
    Element elArgs = el.addElement("arguments");
    for (int i = 0; i < this.arguments.length; i++) {
      if (this.arguments[i] != null) {
        elArgs.addElement("arg").addText(this.arguments[i]);
      }
    }
    return document;
  }
}
