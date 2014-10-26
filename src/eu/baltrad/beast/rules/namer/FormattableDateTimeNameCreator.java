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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.baltrad.bdb.oh5.Attribute;
import eu.baltrad.bdb.oh5.Metadata;

/**
 * Date time formatter that can be useful when creating date time strings from what/date, what/time
 * @author Anders Henja
 */
public class FormattableDateTimeNameCreator implements MetadataNameCreator {
  private final static Pattern pattern;
  static {
    pattern = Pattern.compile("_beast/datetime(:[A-Za-z0-9\\-/: ]+)?"); // \\(:[:A-Za-z0-9/.\\- ]*\\)
  }

  private SimpleDateFormat DATETIME_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");
  
  /**
   * @see eu.baltrad.beast.rules.namer.MetadataNameCreator#supports(java.lang.String)
   */
  @Override
  public boolean supports(String tag) {
    if (tag != null) {
      Matcher m = pattern.matcher(tag);
      return m.matches();
    }
    return false;
  }

  /**
   * @see eu.baltrad.beast.rules.namer.MetadataNameCreator#createName(java.lang.String, eu.baltrad.bdb.oh5.Metadata)
   */
  @Override
  public String createName(String tag, Metadata metadata) {
    String result = null;
    if (supports(tag)) {
      Attribute dstr = metadata.getAttribute("what/date");
      Attribute tstr = metadata.getAttribute("what/time");
      if (dstr != null && tstr != null && dstr.toString() != null && tstr.toString() != null) {
        String dtstr = dstr.toString() + tstr.toString();
        try {
          Date d = DATETIME_FORMATTER.parse(dtstr);
          String nformat = "yyyyMMddHHmmss";
          if (tag.contains(":")) {
            nformat = tag.substring(tag.indexOf(":")+1);
          }
          result = new SimpleDateFormat(nformat).format(d);
        } catch (ParseException e) {
          //pass
        }
      }
    }
    return result;
  }
}
