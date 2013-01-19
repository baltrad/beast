/* --------------------------------------------------------------------
Copyright (C) 2009-2013 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.system;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * @author Anders Henja
 */
public class XmlSystemStatusGenerator {

  /**
   * Each status is represented as
   * component | value | status. For example radars | seang | OK.
   */
  static class StatusItem {
    private String component;
    private String value;
    private String status;
    StatusItem(String component, String value, String status) {
      this.component = component;
      this.value = value;
      this.status = status;
    }
    String getComponent() {
      return this.component;
    }
    String getValue() {
      return this.value;
    }
    String getStatus() {
      return this.status;
    }
  }

  /**
   * The status components
   */
  private List<StatusItem> components;
  
  /**
   * Default constructor
   * Essentially the same as calling the constructor with '&lt;?xml version='1.0' encoding='UTF-8'?&gt;'
   */
  public XmlSystemStatusGenerator() {
    this.components = new ArrayList<XmlSystemStatusGenerator.StatusItem>();
  }

  /**
   * Adds one component to the status report
   * @param component the component
   * @param value the value of the component
   * @param status the status of the component with specified value
   */
  public void add(String component, String value, Set<SystemStatus> status) {
    this.components.add(new StatusItem(component, value, generateStatusString(status)));
  }

  /**
   * @return the xml string with UTF-8 encoding
   */
  public String getXmlString() {
    return getXmlString("UTF-8");
  }
  
  /**
   * Returns the string representation
   * @param encoding the encoding to use
   * @return the string representation
   */
  public String getXmlString(String encoding) {
    Document doc = toDocument();
    StringWriter swriter = new StringWriter();
    OutputFormat oformat = new OutputFormat("", true);
    oformat.setEncoding(encoding);
    XMLWriter xwriter = new XMLWriter(swriter, oformat);
    try {
      xwriter.write(doc);
    } catch (IOException e) {
      // really not much I can do here.
    }
    return swriter.toString();
  }

  /**
   * @return the dom document
   */
  public Document toDocument() {
    Document document = DocumentHelper.createDocument();
    Element el = document.addElement("system-status");
    
    for (StatusItem item : components) {
      Element comp = el.addElement("component");
      comp.addAttribute("name", item.getComponent());
      comp.addAttribute("value", item.getValue());
      comp.addAttribute("status", item.getStatus());
    }
    
    return document;
  }
  
  /**
   * Generates a status string
   * @param status the system status set
   * @return the status string with | between each unique status
   */
  protected String generateStatusString(Set<SystemStatus> status) {
    StringBuffer buffer = new StringBuffer();
    if (status == null || status.size() == 0) {
      buffer.append("UNDEFINED");
    } else {
      SystemStatus[] sarr = new SystemStatus[] {
        SystemStatus.UNDEFINED,  
        SystemStatus.OK,
        SystemStatus.COMMUNICATION_PROBLEM,
        SystemStatus.MEMORY_PROBLEM,
        SystemStatus.EXCHANGE_PROBLEM,
        SystemStatus.PROCESSING_PROBLEM
      };
      for (SystemStatus s : sarr) {
        if (status.contains(s)) {
          if (buffer.length() > 0) {
            buffer.append("|");
          }
          buffer.append(s.toString());
        }
      }
    }
    return buffer.toString();
  }
}
