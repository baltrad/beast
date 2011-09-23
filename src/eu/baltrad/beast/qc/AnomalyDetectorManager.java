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
package eu.baltrad.beast.qc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

/**
 * @author Anders Henja
 */
public class AnomalyDetectorManager implements IAnomalyDetectorManager {
  /**
   * The database access
   */
  private SimpleJdbcOperations template = null;
  
  /**
   * Sets the jdbc template to be used by this class
   * @param template the template
   */
  public void setJdbcTemplate(SimpleJdbcOperations template) {
    this.template = template;
  }
  
  /**
   * @see eu.baltrad.beast.qc.IAnomalyDetectorManager#add(AnomalyDetector)
   */
  @Override
  public void add(AnomalyDetector detector) {
    validateName(detector.getName());
    try {
      template.update("insert into beast_anomaly_detectors (name, description) values (?,?)",
          new Object[]{detector.getName(), detector.getDescription()});
    } catch (DataAccessException e) {
      throw new AnomalyException("Could not create detector", e);
    }
  }

  /**
   * @see eu.baltrad.beast.qc.IAnomalyDetectorManager#update(AnomalyDetector)
   */
  @Override
  public void update(AnomalyDetector detector) {
    try {
      int rowcnt = template.update("update beast_anomaly_detectors set description=? where name=?",
          new Object[]{detector.getDescription(), detector.getName()});
      if (rowcnt == 0) {
        throw new AnomalyException("Could not update detector");
      }
    } catch (DataAccessException e) {
      throw new AnomalyException("Could not update detector", e);
    }
  }
  
  /**
   * @see eu.baltrad.beast.qc.IAnomalyDetectorManager#list()
   */
  @Override
  public List<AnomalyDetector> list() {
    return template.query("select name,description from beast_anomaly_detectors order by name",
        getMapper(),
        (Object[])null);
  }

  /**
   * @see eu.baltrad.beast.qc.IAnomalyDetectorManager#get(java.lang.String)
   */
  @Override
  public AnomalyDetector get(String name) {
    try {
      return template.queryForObject("select name,description from beast_anomaly_detectors where name=?",
          getMapper(),
          new Object[]{name});
    } catch (DataAccessException e) {
      throw new AnomalyException("Could not find detector '"+name+"'", e);
    }
  }

  /**
   * @see eu.baltrad.beast.qc.IAnomalyDetectorManager#remove(java.lang.String)
   */
  @Override
  public void remove(String detector) {
    try {
      template.update("delete from beast_anomaly_detectors where name=?", 
          new Object[]{detector});
    } catch (DataAccessException e) {
      throw new AnomalyException("Failed to remove '"+detector+"'", e);
    }
  }
  
  /**
   * Validates that the name is in correct format
   * @param name the name to validate
   */
  protected void validateName(String name) {
    if (name == null || !name.matches("[A-Za-z0-9_.-]+")) {
      throw new AnomalyException("Valid names are in the format [A-Za-z0-9_.-]+");
    }   
  }
  
  
  /**
   * @return the row mapper for anomaly detectors
   */
  protected ParameterizedRowMapper<AnomalyDetector> getMapper() {
    return new ParameterizedRowMapper<AnomalyDetector>() {
      @Override
      public AnomalyDetector mapRow(ResultSet rs, int rownr) throws SQLException {
        AnomalyDetector result = new AnomalyDetector();
        result.setName(rs.getString("name"));
        result.setDescription(rs.getString("description"));
        return result;
      }
    };
  }
}
