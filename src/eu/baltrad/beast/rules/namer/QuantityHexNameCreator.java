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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import eu.baltrad.bdb.oh5.Attribute;
import eu.baltrad.bdb.oh5.Metadata;
import eu.baltrad.bdb.oh5.Node;

/**
 * Creates a 64-bit hex string representation of the quantities according to a
 * predefined list of quantities.
 * 
 * @author Anders Henja
 */
public class QuantityHexNameCreator implements MetadataNameCreator, InitializingBean {
  /**
   * Bitmask to use when shifting from left to right.
   */
  private final static long SR_BITMASK = 0x8000000000000000L;
  
  /**
   * Bitmask to use when shifting from right to left.
   */
  private final static long SL_BITMASK = 0x1L;
  
  /**
   * The mapping between quantity and bit-position. 
   */
  private Map<String, Integer> map;
  
  /**
   * If shifting should be performed to left or right. Default is to shift to the right.
   */
  private boolean shiftLeft = false;

  /**
   * Default constructor
   */
  public QuantityHexNameCreator() {
  }

  /**
   * Constructor
   * @param map the mapping between quantity and bit position
   */
  public QuantityHexNameCreator(Map<String, Integer> map) {
    this(map, false);
  }

  /**
   * Constructor
   * @param map the mapping between quantity and bit position
   * @param shiftLeft if the bit pattern should be masked from left or right
   */
  public QuantityHexNameCreator(Map<String, Integer> map, boolean shiftLeft) {
    this.map = map;
    this.shiftLeft = shiftLeft;
  }
  
  /**
   * @see eu.baltrad.beast.rules.namer.MetadataNameCreator#createName(eu.baltrad.bdb.oh5.Metadata)
   */
  @Override
  public String createName(Metadata metadata) {
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
    return "0x"+Long.toHexString(createMaskFromIntegers(integers));
  }

  /**
   * Creates the bit pattern
   * @param integers a list of integer positions
   * @return the created long value
   */
  protected long createMaskFromIntegers(List<Integer> integers) {
    long result = 0L;
    for (Integer i : integers) {
      if (shiftLeft) {
        result = result | (SL_BITMASK << (long)i);
      } else {
        result = result | (SR_BITMASK >>> (long)i);
      }
    }
    return result;
  }
  
  /**
   * Returns the value for the specified path in the metadata
   * @param metadata the metadata
   * @param attrPath the node path
   * @return the string value if found, otherwise null
   */
  protected String getAttributeValue(Metadata metadata, String attrPath) {
    Attribute attr = metadata.getAttribute(attrPath);
    if (attr != null) {
      return attr.toString();
    }
    return null;
  }
  
  /**
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
  }

}
