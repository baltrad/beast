/* --------------------------------------------------------------------
Copyright (C) 2009-2014 Swedish Meteorological and Hydrological Institute, SMHI,

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

package eu.baltrad.beast.rules.namer;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import eu.baltrad.bdb.oh5.Attribute;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.oh5.MetadataNamer;
import eu.baltrad.beast.InitializationException;

/**
 * Extendable MetadataNamer that can be extended with different MetadataNameCreators
 * for allowing a dynamic extension of the metadata naming. Original code can be found
 * in bdb:s TemplateMetadataNamer.
 * 
 * @author Anders Henja
 */
public class TemplateNameCreatorMetadataNamer implements MetadataNamer, InitializingBean {
  private String template;

  private static final Pattern pattern;

  static {
    pattern = Pattern.compile("\\$(?:" + "(\\$)|" + // group 1 matches escaped
                                                    // delimiter ($$)
        "\\{([_/a-z][_:/a-z0-9 #@+\\-\\.]*)\\}" + // group 2 matches the placeholder
        ")", Pattern.CASE_INSENSITIVE);
  }
  ///#@+_-.<>
  private MetadataNameCreatorFactory factory = null;
  
  /**
   * The logger
   */
  private static final Logger logger = LogManager.getLogger(TemplateNameCreatorMetadataNamer.class);
  
  /**
   * Constructor
   * @param template the string template
   */
  public TemplateNameCreatorMetadataNamer(String template) {
    this.template = template;
  }
  
  /**
   * @return the template
   */
  public String getTemplate() {
    return this.template;
  }
  
  /**
   * @see eu.baltrad.bdb.oh5.MetadataNamer#name(eu.baltrad.bdb.oh5.Metadata)
   */
  @Override
  public String name(Metadata metadata) {
    StringBuffer result = new StringBuffer();
    Matcher m = pattern.matcher(template);
    while (m.find()) {
      String placeholder = m.group(2);
      if (placeholder != null) {
        if (placeholder.toLowerCase().startsWith("_bdb/source:")) {
          String r = getSourceItem("_bdb/source", placeholder.substring(12),
              metadata);
          m.appendReplacement(result, r);
        } else if (placeholder.toLowerCase().startsWith("what/source:")) {
          String r = getSourceItem("what/source", placeholder.substring(12),
              metadata);
          m.appendReplacement(result, r);
        } else if (placeholder.toLowerCase().startsWith("/what/source:")) {
          String r = getSourceItem("what/source", placeholder.substring(13),
              metadata);
          m.appendReplacement(result, r);
        } else if (factory.supports(placeholder)) {
          MetadataNameCreator creator = factory.get(placeholder);
          String r = null;
          if (creator != null) {
            r = creator.createName(placeholder, metadata);
          }
          if (r != null) {
            m.appendReplacement(result, r);
          } else {
            m.appendReplacement(result,  "null");
          }
        } else {
          m.appendReplacement(result, getAttributeValue(metadata, placeholder));
        }
      }
    }
    m.appendTail(result);
    
    logger.debug(""+template+" => translated into => " + result.toString());
    
    return result.toString();
  }
  
  public MetadataNameCreatorFactory getFactory() {
    return factory;
  }

  public void setFactory(MetadataNameCreatorFactory factory) {
    this.factory = factory;
  }

  /**
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() {
    if (factory == null) {
      throw new InitializationException("Missing MetadataNameCreatorFactory");
    }
  }


  private String getAttributeValue(Metadata metadata, String attrPath) {
    Attribute attr = metadata.getAttribute(attrPath);
    if (attr != null) {
      return attr.toString();
    }
    return "null";
  }

  private String getSourceItem(String path, String tok, Metadata metadata) {
    String nxt = getAttributeValue(metadata, path.toLowerCase());
    if (nxt != null) {
      StringTokenizer t = new StringTokenizer(nxt, ",");
      while (t.hasMoreElements()) {
        String n = t.nextToken();
        if (n.toLowerCase().startsWith(tok.toLowerCase())) {
          return n.substring(4);
        }
      }
    }
    return "null";
  }
}
