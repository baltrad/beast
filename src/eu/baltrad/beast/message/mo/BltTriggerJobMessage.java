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
 * A message for triggering specific jobs/rules. In order to trigger
 * a specific rule, the job name has to be specified otherwise 
 * the message will be propagated to all rules as usual.
 * @author Anders Henja
 */
public class BltTriggerJobMessage implements IBltXmlMessage {
  /**
   * The static name of this message
   */
  public static final String BLT_TRIGGER_JOB = "blttriggerjob";

  /**
   * The id for this message
   */
  private String id = null;
  
  /**
   * The name of the scheduled job
   */
  private String name = null;
  
  /**
   * Optional information 
   */
  private String[] args = new String[0];
  
  /**
   * @see eu.baltrad.beast.message.IBltXmlMessage#fromDocument(org.dom4j.Document)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void fromDocument(Document dom) {
    int index = 0;
    if (!dom.getRootElement().getName().equals(BLT_TRIGGER_JOB)) {
      throw new MessageParserException("Atempting to create BltTriggerJob from: " + dom.asXML());
    }
    setId(dom.valueOf("//blttriggerjob/id"));
    setName(dom.valueOf("//blttriggerjob/name"));
    List<Node> nodes = dom.selectNodes("//blttriggerjob/arguments/arg");
    this.args = new String[nodes.size()];
    for (Node node : nodes) {
      this.args[index++] = node.getText();
    }
  }    

  /**
   * @see eu.baltrad.beast.message.IBltXmlMessage#toDocument()
   */
  @Override
  public Document toDocument() {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement(BLT_TRIGGER_JOB);
    el.addElement("id").addText(this.id);
    el.addElement("name").addText(this.name);

    Element elArgs = el.addElement("arguments");
    for (int i = 0; i < this.args.length; i++) {
      if (this.args[i] != null) {
        elArgs.addElement("arg").addText(this.args[i]);
      }
    }
    
    return document;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param args the args to set
   */
  public void setArgs(String[] args) {
    if (args == null) {
      this.args = new String[0];
    } else {
      this.args = args;
    }
  }

  /**
   * @return the args
   */
  public String[] getArgs() {
    return args;
  }
}
