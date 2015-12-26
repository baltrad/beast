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

package eu.baltrad.beast.pgf;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.baltrad.beast.adaptor.IAdaptor;
import eu.baltrad.beast.adaptor.IAdaptorCallback;
import eu.baltrad.beast.adaptor.IBltAdaptorManager;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltGetQualityControlsMessage;

/**
 * @author Anders Henja
 *
 */
public class PgfClientHelper implements IPgfClientHelper {
  /**
   * The adaptor manager
   */
  private IBltAdaptorManager adaptorManager = null;
  
  /**
   * The logger
   */
  private final static Logger logger = LogManager.getLogger(PgfClientHelper.class);
  
  /**
   * Callback for taking care of the quality control information 
   */
  protected static class AdaptorCallback implements IAdaptorCallback {
    private String adaptorName;
    private List<QualityControlInformation> qci;
    public AdaptorCallback(String adaptorName, List<QualityControlInformation> qci) {
      this.adaptorName = adaptorName;
      this.qci = qci;
    }
    
    /**
     * @see eu.baltrad.beast.adaptor.IAdaptorCallback#success(eu.baltrad.beast.message.IBltMessage, java.lang.Object)
     */
    @Override
    public void success(IBltMessage arg0, Object arg) {
      synchronized(qci) {
        try {
          for (Object o : (Object[])arg) {
            Object[] r = (Object[])o;
            qci.add(new QualityControlInformation(adaptorName, r[0].toString(), r[1].toString()));
          }
        } catch (Exception e) {
          logger.error("Failed to get quality control information for " + adaptorName, e);
        }
      }
    }

    @Override
    public void error(IBltMessage arg0, Throwable arg1) {}

    @Override
    public void timeout(IBltMessage arg0) {}
  }
  
  /**
   * @see eu.baltrad.beast.pgf.IPgfClientHelper#getQualityControls()
   */
  @Override
  public List<QualityControlInformation> getQualityControls() {
    List<String> adaptorNames = adaptorManager.getAdaptorNames();
    List<QualityControlInformation> qualityControlInfo = createResultList();
    BltGetQualityControlsMessage qcMessage = new BltGetQualityControlsMessage();
    for (String adaptorName : adaptorNames) {
      IAdaptor adaptor = adaptorManager.getAdaptor(adaptorName);
      adaptor.handle(qcMessage, createCallback(adaptorName, qualityControlInfo));
    }
    return qualityControlInfo;
  }

  /**
   * @param adaptorManager the adaptor manager
   */
  @Autowired
  public void setAdaptorManager(IBltAdaptorManager adaptorManager) {
    this.adaptorManager = adaptorManager;
  }
  
  /**
   * Creates and returns an instance of the adaptor callback 
   * @param adaptorName
   * @param qualityControlInformation
   * @return
   */
  protected IAdaptorCallback createCallback(String adaptorName, List<QualityControlInformation> qualityControlInformation) {
    return new AdaptorCallback(adaptorName, qualityControlInformation);
  }

  protected List<QualityControlInformation> createResultList() {
    return new ArrayList<QualityControlInformation>();
  }
}
