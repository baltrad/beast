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

package eu.baltrad.beast.rules.scansun;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;

import eu.baltrad.bdb.db.FileEntry;
import eu.baltrad.bdb.util.Date;
import eu.baltrad.bdb.util.Time;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.IRule;
import eu.baltrad.beast.rules.composite.CompositingRule;

/**
 * @author Anders Henja
 *
 */
public class ScansunRule implements IRule {
  /**
   * The name of this static scansun type
   */
  public final static String TYPE = "blt_scansun";
  
  /**
   * The unique rule id separating this scansun rule from the others.
   */
  private int ruleid = -1;
  
  /**
   * A list of sources (e.g. seang, sekkr, ...)
   */
  private List<String> sources = new ArrayList<String>();
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(ScansunRule.class);

  /**
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    logger.debug("ENTER: handle(IBltMessage)");
    
    try {
      if (message instanceof BltDataMessage) {
        FileEntry file = ((BltDataMessage)message).getFileEntry();
        String object = file.getMetadata().getWhatObject();
        String src = file.getMetadata().getSource().get("NOD");
        if (object != null && object.equals("PVOL") && src != null && sources.contains(src)) {
          BltGenerateMessage result = new BltGenerateMessage();
          result.setAlgorithm("eu.baltrad.beast.GenerateScansun");
          result.setFiles(new String[]{file.getUuid().toString()});
          return result;
        }
      }
      return null;
    } finally {
      logger.debug("EXIT: handle(IBltMessage)");
    }
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#getType()
   */
  @Override
  public String getType() {
    return TYPE;
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#isValid()
   */
  @Override
  public boolean isValid() {
    return true;
  }

  /**
   * @param ruleid the ruleid to set
   */
  public void setRuleId(int ruleid) {
    this.ruleid = ruleid;
  }

  /**
   * @return the ruleid
   */
  public int getRuleId() {
    return ruleid;
  }
  
  /**
   * @param sources the list of sources to set
   */
  public void setSources(List<String> sources) {
    this.sources = sources;
  }
  
  /**
   * @return a list of sources
   */
  public List<String> getSources() {
    return this.sources;
  }
}
