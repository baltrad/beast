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
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.baltrad.beast.adaptor.IAdaptor;
import eu.baltrad.beast.adaptor.IAdaptorCallback;
import eu.baltrad.beast.adaptor.IBltAdaptorManager;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltGetAreasMessage;
import eu.baltrad.beast.message.mo.BltGetPcsDefinitionsMessage;
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
  protected static class QCAdaptorCallback implements IAdaptorCallback {
    private String adaptorName;
    private List<QualityControlInformation> qci;
    public QCAdaptorCallback(String adaptorName, List<QualityControlInformation> qci) {
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
   * Callback for taking care of the quality control information 
   */
  protected static class AreaAdaptorCallback implements IAdaptorCallback {
    private String adaptorName;
    private List<AreaInformation> areas;
    public AreaAdaptorCallback(String adaptorName, List<AreaInformation> areas) {
      this.adaptorName = adaptorName;
      this.areas = areas;
    }
    
    private int getInt(Object o) {
      if (o instanceof String) {
        return Integer.parseInt(o.toString());
      }
      return (Integer)o;
    }

    private double getDouble(Object o) {
      if (o instanceof String) {
        return Double.parseDouble(o.toString());
      }
      return (Double)o;
    }

    private double[] getExtent(Object o) {
      double result[] = new double[4];
      result[0] = (Double)((Object[])o)[0];
      result[1] = (Double)((Object[])o)[1];
      result[2] = (Double)((Object[])o)[2];
      result[3] = (Double)((Object[])o)[3];
      return result;
    }
    
    /**
     * @see eu.baltrad.beast.adaptor.IAdaptorCallback#success(eu.baltrad.beast.message.IBltMessage, java.lang.Object)
     */
    @Override
    public void success(IBltMessage arg0, Object arg) {
      synchronized(areas) {
        try {
          HashMap<String, HashMap<String,Object>> hm = (HashMap<String, HashMap<String,Object>>)arg;
          for (String k : hm.keySet()) {
            HashMap<String, Object> v = hm.get(k);
            AreaInformation area = new AreaInformation(k);
            if (v.containsKey("xsize")) {
              area.setXsize(getInt(v.get("xsize")));
            }
            if (v.containsKey("ysize")) {
              area.setYsize(getInt(v.get("ysize")));
            }
            if (v.containsKey("xscale")) {
              area.setXscale(getDouble(v.get("xscale")));
            }
            if (v.containsKey("yscale")) {
              area.setYscale(getDouble(v.get("yscale")));
            }
            if (v.containsKey("extent")) {
              area.setExtent(getExtent(v.get("extent")));
            }
            if (v.containsKey("pcs")) {
              area.setPcs((String)v.get("pcs"));
            }
            areas.add(area);
          }
        } catch (Exception e) {
          logger.error("Failed to get areas information for " + adaptorName, e);
        }
      }
    }

    @Override
    public void error(IBltMessage arg0, Throwable arg1) {}

    @Override
    public void timeout(IBltMessage arg0) {}
  }
  
  /**
   * Callback for taking care of the quality control information 
   */
  protected static class PcsDefinitionAdaptorCallback implements IAdaptorCallback {
    private String adaptorName;
    private List<PcsDefinition> pcs;
    public PcsDefinitionAdaptorCallback(String adaptorName, List<PcsDefinition> pcs) {
      this.adaptorName = adaptorName;
      this.pcs = pcs;
    }
    
    /**
     * @see eu.baltrad.beast.adaptor.IAdaptorCallback#success(eu.baltrad.beast.message.IBltMessage, java.lang.Object)
     */
    @Override
    public void success(IBltMessage arg0, Object arg) {
      synchronized(pcs) {
        try {
          HashMap<String, HashMap<String,Object>> hm = (HashMap<String, HashMap<String,Object>>)arg;
          for (String k : hm.keySet()) {
            HashMap<String, Object> v = hm.get(k);
            PcsDefinition pdef = new PcsDefinition(k);
            if (v.containsKey("description")) {
              pdef.setDescription((String)v.get("description"));
            }
            if (v.containsKey("definition")) {
              pdef.setDefinition((String)v.get("definition"));
            }
            pcs.add(pdef);
          }
        } catch (Exception e) {
          logger.error("Failed to get pcs definition for " + adaptorName, e);
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
    List<QualityControlInformation> qualityControlInfo = createQCResultList();
    BltGetQualityControlsMessage qcMessage = new BltGetQualityControlsMessage();
    for (String adaptorName : adaptorNames) {
      IAdaptor adaptor = adaptorManager.getAdaptor(adaptorName);
      adaptor.handle(qcMessage, createQCCallback(adaptorName, qualityControlInfo));
    }
    return qualityControlInfo;
  }


  /**
   * @see eu.baltrad.beast.pgf.IPgfClientHelper#getAreas()
   */
  @Override
  public List<AreaInformation> getAreas() {
    List<String> adaptorNames = adaptorManager.getAdaptorNames();
    List<AreaInformation> areas = createAreaInformationList();
    for (String name : adaptorNames) {
      List<AreaInformation> adaptorAreas = getAreas(name);
      areas.addAll(adaptorAreas);
    }
    return areas;
  }

  /**
   * @see eu.baltrad.beast.pgf.IPgfClientHelper#getAreas(java.lang.String)
   */
  @Override
  public List<AreaInformation> getAreas(String adaptorName) {
    List<AreaInformation> areas = createAreaInformationList();
    BltGetAreasMessage message = new BltGetAreasMessage();
    IAdaptor adaptor = adaptorManager.getAdaptor(adaptorName);
    adaptor.handle(message, createAreaCallback(adaptorName, areas));
    return areas;
  }


  /**
   * @see eu.baltrad.beast.pgf.IPgfClientHelper#getUniqueAreaIds()
   */
  @Override
  public List<String> getUniqueAreaIds() {
    List<String> result = new ArrayList<String>();
    try {
      List<AreaInformation> areas = getAreas();
      for (AreaInformation area : areas) {
        if (!result.contains(area.getId())) {
          result.add(area.getId());
        }
      }
    } catch (Exception e) {
      logger.warn("Failed to get unique area ids", e);
    }
    return result;
  }

  /**
   * @see eu.baltrad.beast.pgf.IPgfClientHelper#getPcsDefinitions()
   */
  @Override
  public List<PcsDefinition> getPcsDefinitions() {
    List<String> adaptorNames = adaptorManager.getAdaptorNames();
    List<PcsDefinition> pcs = createPcsDefinitionList();
    for (String name : adaptorNames) {
      List<PcsDefinition> adaptorPcs = getPcsDefinitions(name);
      pcs.addAll(adaptorPcs);
    }
    return pcs;
  }

  /**
   * @see eu.baltrad.beast.pgf.IPgfClientHelper#getPcsDefinitions(java.lang.String)
   */
  @Override
  public List<PcsDefinition> getPcsDefinitions(String adaptorName) {
    List<PcsDefinition> pcs = createPcsDefinitionList();
    BltGetPcsDefinitionsMessage message = new BltGetPcsDefinitionsMessage();
    IAdaptor adaptor = adaptorManager.getAdaptor(adaptorName);
    adaptor.handle(message, createPcsDefinitionCallback(adaptorName, pcs));
    return pcs;
  }

  /**
   * @see eu.baltrad.beast.pgf.IPgfClientHelper#getUniquePcsIds()
   */
  @Override
  public List<String> getUniquePcsIds() {
    List<String> result = new ArrayList<String>();
    try {
      List<PcsDefinition> pcs = getPcsDefinitions();
      for (PcsDefinition pid : pcs) {
        if (!result.contains(pid.getId())) {
          result.add(pid.getId());
        }
      }
    } catch (Exception e) {
      logger.warn("Failed to get unique pcs ids", e);
    }
    return result;
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
  protected IAdaptorCallback createQCCallback(String adaptorName, List<QualityControlInformation> qualityControlInformation) {
    return new QCAdaptorCallback(adaptorName, qualityControlInformation);
  }

  /**
   * Creates and returns an instance of the adaptor callback 
   * @param adaptorName
   * @param areas
   * @return
   */
  protected IAdaptorCallback createAreaCallback(String adaptorName, List<AreaInformation> areas) {
    return new AreaAdaptorCallback(adaptorName, areas);
  }

  /**
   * Creates and returns an instance of the adaptor callback 
   * @param adaptorName
   * @param qualityControlInformation
   * @return
   */
  protected IAdaptorCallback createPcsDefinitionCallback(String adaptorName, List<PcsDefinition> pcs) {
    return new PcsDefinitionAdaptorCallback(adaptorName, pcs);
  }
  
  /**
   * @return the list to be filled with QC information
   */
  protected List<QualityControlInformation> createQCResultList() {
    return new ArrayList<QualityControlInformation>();
  }

  /**
   * @return the list to be filled with areas
   */
  protected List<AreaInformation> createAreaInformationList() {
    return new ArrayList<AreaInformation>();
  }

  /**
   * @return the list to be filled with pcs definitions
   */
  protected List<PcsDefinition> createPcsDefinitionList() {
    return new ArrayList<PcsDefinition>();
  }

//  public static void main(String[] args) {
//    XmlRpcAdaptor adaptor = new XmlRpcAdaptor();
//    XmlRpcCommandGenerator generator = new XmlRpcCommandGenerator();
//    adaptor.setGenerator(generator);
//    adaptor.setUrl("http://localhost:8085/RAVE");
//    
//    BltGetAreasMessage msg = new BltGetAreasMessage();
//    List<AreaInformation> areas = new ArrayList<AreaInformation>();
//    adaptor.handle(msg, new AreaAdaptorCallback("RAVE", areas));
//    for (AreaInformation info : areas) {
//      System.out.println(info.toString());
//    }
//  }
}
