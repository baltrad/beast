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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Anders Henja
 */
public class SubOperationHandler {
  private static final Pattern SUBOPERATION_PATTERN;
  private static final Pattern LTRIM = Pattern.compile("^\\s+");
  private static final Pattern RTRIM = Pattern.compile("\\s+$");
  
  static {
    SUBOPERATION_PATTERN = Pattern.compile(".(tolower|toupper|substring|trim|rtrim|ltrim)(\\((([0-9]+)(,([0-9]+))?)?\\))",Pattern.CASE_INSENSITIVE);
  }

  public SubOperationHandler() {
  }
  
  class SubopGroupInfo {
    boolean biAvailable,eiAvailable;
    int beginIndex,endIndex;
    SubopGroupInfo(boolean biAvailable, boolean eiAvailable, int beginIndex, int endIndex) {
      this.biAvailable = biAvailable;
      this.eiAvailable = eiAvailable;
      this.beginIndex = beginIndex;
      this.endIndex = endIndex;
    }
  }

  /**
   * Handles the suboperations, even chained ones.
   * @param suboperation the chained suboperations, starting with "."
   * @param value the value that suboperations should be performed on
   * @return the processed value
   */
  public String handle(String suboperation, String value) {
    String result = value;
    if (suboperation != null) {
      Matcher m = SUBOPERATION_PATTERN.matcher(suboperation); // Function in group 1, 
      while (m.find()) {
        String op = m.group(1);
        if (op.equalsIgnoreCase("tolower") && result != null) {
          SubopGroupInfo info = getBeginEndIndex(m, 4, 6);
          if (info.biAvailable && info.eiAvailable) {
            if (info.beginIndex >= 0 && info.beginIndex <= result.length() &&
                info.endIndex >= info.beginIndex && info.endIndex <= result.length()) {
              result = result.substring(0,info.beginIndex) + result.substring(info.beginIndex,info.endIndex).toLowerCase() + result.substring(info.endIndex);
            }
          } else if (info.eiAvailable) {
            if (info.endIndex >= 0 && info.endIndex+1 <= result.length()) {
              result = result.substring(0,info.endIndex) + result.substring(info.endIndex,info.endIndex+1).toLowerCase() + result.substring(info.endIndex+1);
            }
          } else {
            result = result.toLowerCase();
          }
        } else if (op.equalsIgnoreCase("toupper") && result != null) {
          SubopGroupInfo info = getBeginEndIndex(m, 4, 6);
          if (info.biAvailable && info.eiAvailable) {
            if (info.beginIndex >= 0 && info.beginIndex <= result.length() &&
                info.endIndex >= info.beginIndex && info.endIndex <= result.length()) {
              result = result.substring(0,info.beginIndex) + result.substring(info.beginIndex,info.endIndex).toUpperCase() + result.substring(info.endIndex);
            }
          } else if (info.eiAvailable) {
            if (info.endIndex >= 0 && info.endIndex+1 <= result.length()) {
              result = result.substring(0,info.endIndex) + result.substring(info.endIndex,info.endIndex+1).toUpperCase() + result.substring(info.endIndex+1);
            }
          } else {
            result = result.toUpperCase();
          }
        } else if (op.equalsIgnoreCase("substring") && result != null) {
          SubopGroupInfo info = getBeginEndIndex(m, 4, 6);
          if (info.biAvailable && info.eiAvailable) {
            if (info.beginIndex >= 0 && info.beginIndex <= result.length() &&
                info.endIndex >= info.beginIndex && info.endIndex <= result.length()) {
              result = result.substring(info.beginIndex, info.endIndex);
            }
          } else if (info.eiAvailable) {
            if (info.endIndex >= 0 && info.endIndex+1 <= result.length()) {
              result = result.substring(0, info.endIndex);
            }
          }
        } else if (op.equalsIgnoreCase("trim") && result != null) {
          result = result.trim();
        } else if (op.equalsIgnoreCase("ltrim") && result != null) {
          result = LTRIM.matcher(result).replaceAll("");
        } else if (op.equalsIgnoreCase("rtrim") && result != null) {
          result = RTRIM.matcher(result).replaceAll("");
        }
      }
    }
    return result;
  }

  /**
   * Returns the begin and end index for the command list
   * @param m matcher
   * @param g1 the position of group 1
   * @param g2 the position of group 2
   * @return the position information
   */
  private SubopGroupInfo getBeginEndIndex(Matcher m, int g1, int g2) {
    int endIndex = 0, beginIndex = 0;
    String bstr = m.group(g1);
    String estr = m.group(g2);
  
    if (bstr != null && estr != null) {
      try {
        beginIndex = Integer.parseInt(bstr);
        endIndex = Integer.parseInt(estr);
      } catch (NumberFormatException nfe) {
        // pass
      }
      return new SubopGroupInfo(true,true,beginIndex,endIndex);
    } else if (bstr != null) {
      try {
        endIndex = Integer.parseInt(bstr);
      } catch (NumberFormatException nfe) {
        // pass
      }
      return new SubopGroupInfo(false, true, beginIndex, endIndex);
    }
    return new SubopGroupInfo(false, false, beginIndex, endIndex);
  }
}
