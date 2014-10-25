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

package eu.baltrad.beast.rules.namer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import eu.baltrad.bdb.oh5.Attribute;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.oh5.Node;

/**
 * Creates a 64-bit hex string representation of the quantities according to a
 * predefined list of quantities.
 * 
 * @author Anders Henja
 */
public class QuantityHexNameCreator implements MetadataNameCreator {
  /**
   * Bitmask to use when shifting from left to right.
   */
  private final static long SR_BITMASK = 0x8000000000000000L;

  /**
   * Bitmask to use when shifting from right to left.
   */
  private final static long SL_BITMASK = 0x1L;

  /**
   * The hexdata tag
   */
  private final static String HEXCODE_TAG = "_beast/hexcode";
  
  /**
   * The mapping between quantity and bit-position.
   */
  private Map<String, Integer> map;

  /**
   * If shifting should be performed to left or right. Default is to shift to
   * the right.
   */
  private boolean shiftLeft = false;

  /**
   * Default constructor
   */
  public QuantityHexNameCreator() {
  }

  /**
   * Constructor
   * 
   * @param map
   *          the mapping between quantity and bit position
   */
  public QuantityHexNameCreator(Map<String, Integer> map) {
    this(map, false);
  }

  /**
   * Constructor
   * 
   * @param map
   *          the mapping between quantity and bit position
   * @param shiftLeft
   *          if the bit pattern should be masked from left or right
   */
  public QuantityHexNameCreator(Map<String, Integer> map, boolean shiftLeft) {
    this.map = map;
    this.shiftLeft = shiftLeft;
  }

  /**
   * Constructor
   * 
   * @param file
   *          the mapping as a xml file
   */
  public QuantityHexNameCreator(File file) {
    parseFile(file);
  }


  /**
   * @see MetadataNameCreator#supports(String)
   */
  @Override
  public boolean supports(String tag) {
    return (tag != null && tag.equalsIgnoreCase(HEXCODE_TAG));
  }
  
  /**
   * @see eu.baltrad.beast.rules.namer.MetadataNameCreator#createName(eu.baltrad.bdb.oh5.Metadata)
   */
  @Override
  public String createName(String tag, Metadata metadata) {
    if (!supports(tag)) {
      throw new MetadataNameCreatorException("Not supported tag: " + tag);
    }
    Iterator<Node> iterator = metadata.iterator();
    List<Integer> integers = new ArrayList<Integer>();
    while (iterator.hasNext()) {
      Node n = iterator.next();
      if (n.getPath().toLowerCase().endsWith("what/quantity")) {
        String value = getAttributeValue(metadata, n.getPath());
        if (value != null) {
          Integer index = map.get(value.toUpperCase());
          if (index != null && !integers.contains(index)) {
            integers.add(index);
          }
        }
      }
    }
    return "0x" + Long.toHexString(createMaskFromIntegers(integers));
  }

  /**
   * Creates the bit pattern
   * 
   * @param integers
   *          a list of integer positions
   * @return the created long value
   */
  protected long createMaskFromIntegers(List<Integer> integers) {
    long result = 0L;
    for (Integer i : integers) {
      if (shiftLeft) {
        result = result | (SL_BITMASK << (long) i);
      } else {
        result = result | (SR_BITMASK >>> (long) i);
      }
    }
    return result;
  }

  /**
   * Returns the value for the specified path in the metadata
   * 
   * @param metadata
   *          the metadata
   * @param attrPath
   *          the node path
   * @return the string value if found, otherwise null
   */
  protected String getAttributeValue(Metadata metadata, String attrPath) {
    Attribute attr = metadata.getAttribute(attrPath);
    if (attr != null) {
      return attr.toString();
    }
    return null;
  }

  protected void parseFile(File file) {
    SAXReader xmlReader = new SAXReader();
    Document doc = null;

    try {
      doc = xmlReader.read(file);

      if (!doc.getRootElement().getName().equalsIgnoreCase("odim-quantities")) {
        throw new RuntimeException("Not a proper odim-quantities xml definition file");
      }
      
      boolean leftShifting = isLeftShifting(doc);
      Map<String,Integer> mapping = parseMapping(doc);
      
      this.shiftLeft = leftShifting;
      this.map = mapping;
    } catch (DocumentException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Verifies if the shifting should be performed left to right or right to left
   * @param doc the document
   * @return if shifting should be performed to left or right
   */
  protected boolean isLeftShifting(Document doc) {
    boolean result = false;
    org.dom4j.Attribute shifting = doc.getRootElement().attribute("shift");
    if (shifting != null) {
      result = shifting.getValue().equalsIgnoreCase("left");
    }
    return result;
  }

  /**
   * Parses the quantities xml file
   * 
   * @param file
   *          the file
   * @return a map
   */
  protected Map<String, Integer> parseMapping(Document doc) {
    Map<String, Integer> result = new HashMap<String, Integer>();
    @SuppressWarnings("unchecked")
    List<Element> nodes = (List<Element>) doc.selectNodes("//quantity");
    for (Element e : nodes) {
      String v = e.getText();
      int index = Integer.parseInt(e.attribute("index").getValue());
      result.put(v, index);
    }

    return result;
  }
}
