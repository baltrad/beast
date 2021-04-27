/* --------------------------------------------------------------------
Copyright (C) 2009-2021 Swedish Meteorological and Hydrological Institute, SMHI,

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
package eu.baltrad.beast.admin;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import eu.baltrad.beast.admin.command_response.CommandResponseJsonObject;
import eu.baltrad.beast.admin.command_response.CommandResponseStatus;
import eu.baltrad.beast.admin.command_response.SettingCommandResponse;
import eu.baltrad.beast.admin.objects.Adaptor;
import eu.baltrad.beast.admin.objects.Settings;
import eu.baltrad.beast.admin.objects.User;
import eu.baltrad.beast.admin.objects.routes.Route;
import eu.baltrad.beast.qc.AnomalyDetector;
import eu.baltrad.beast.scheduler.CronEntry;
import net.schmizz.sshj.sftp.Response.StatusCode;

/**
 * Implementation of the json generator
 * @author anders
 */
public class JsonGeneratorImpl implements JsonGenerator {
  /**
   * The object mapper
   */
  private ObjectMapper mapper = null;
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(JsonGenerator.class);
  
  /**
   * Default constructor
   */
  public JsonGeneratorImpl() {
    mapper = new ObjectMapper();
    mapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);
  }
  
  /**
   * @see eu.baltrad.beast.admin.JsonGenerator#toJson(CommandResponse)
   */
  @Override
  public String toJson(CommandResponse response) {
    if (response instanceof CommandResponseStatus) {
      logger.info("CommandResponseStatus: " + ((CommandResponseStatus)response).wasSuccessful());
      if (((CommandResponseStatus)response).wasSuccessful()) {
        return "{\"status\":\"OK\"}";
      } else {
        return "{\"status\":\"ERROR\"}";
      }
    } else if (response instanceof CommandResponseJsonObject) {
      String str = ((CommandResponseJsonObject)response).getJsonString();
      logger.info("CommandResponseJsonObject: " + str);
      return str;
    } else if (response instanceof SettingCommandResponse) {
      try {
        return mapper.writeValueAsString(response);
      } catch (Exception e) {
        return "{\"status\":\"ERROR\"}";
      }
    } else if (response instanceof Settings) {
      try {
        return mapper.writeValueAsString(response);
      } catch (Exception e) {
        return "{\"status\":\"ERROR\"}";
      }
    }
    return "{\"status\":\"NOT_SUPPORTED\"}";
  }


  /**
   * @see eu.baltrad.beast.admin.JsonGenerator#toJson(adaptor)
   */
  @Override
  public String toJson(Adaptor adaptor) {
    try {
      return mapper.writeValueAsString(adaptor);
    } catch (Exception e) {
      throw new AdministratorException(e);
    }    
  }

  /**
   * @see eu.baltrad.beast.admin.JsonGenerator#toJsonFromAdaptors(List<Adaptor>)
   */
  @Override
  public String toJsonFromAdaptors(List<Adaptor> adaptors) {
    try {
      StringBuffer buffer = new StringBuffer();
      buffer.append("[");
      for (int i = 0; i < adaptors.size(); i++) {
        buffer.append(mapper.writeValueAsString(adaptors.get(i)));
        if (i < adaptors.size() - 1) {
          buffer.append(",");
        }
      }
      buffer.append("]");
      return buffer.toString();
    } catch (Exception e) {
      throw new AdministratorException(e);
    }
  }
  
  /**
   * @see eu.baltrad.beast.admin.JsonGenerator#toJson(Route)
   */
  @Override
  public String toJson(Route route) {
    try {
      return mapper.writeValueAsString(route);
    } catch (Exception e) {
      throw new AdministratorException(e);
    }    
  }
  
  /**
   * @see eu.baltrad.beast.admin.JsonGenerator#toJson(CronEntry)
   */
  @Override
  public String toJson(CronEntry entry) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      org.codehaus.jackson.JsonGenerator g = new JsonFactory().createJsonGenerator(baos);
      g.writeStartObject();
      g.writeObjectFieldStart("schedule");
      g.writeNumberField("identifier", entry.getId());
      g.writeStringField("expression", entry.getExpression());
      g.writeStringField("route-name", entry.getName());
      g.writeEndObject();
      g.writeEndObject();
      g.close();
      return new String(baos.toByteArray());
    } catch (Exception e) {
      throw new AdministratorException(e);
    }
  }
  
  /**
   * @see eu.baltrad.beast.admin.JsonGenerator#toJsonFromRoutes(List<Route>)
   */
  @Override
  public String toJsonFromRoutes(List<Route> routes) {
    try {
      StringBuffer buffer = new StringBuffer();
      buffer.append("[");
      for (int i = 0; i < routes.size(); i++) {
        buffer.append(mapper.writeValueAsString(routes.get(i)));
        if (i < routes.size() - 1) {
          buffer.append(",");
        }
      }
      buffer.append("]");
      return buffer.toString();
    } catch (Exception e) {
      throw new AdministratorException(e);
    }
  }

  /**
   * @see eu.baltrad.beast.admin.JsonGenerator#toJsonFromCronEntries(List<CronEntry>)
   */
  @Override
  public String toJsonFromCronEntries(List<CronEntry> entries) {
    try {
      StringBuffer buffer = new StringBuffer();
      buffer.append("[");
      for (int i = 0; i < entries.size(); i++) {
        buffer.append(toJson(entries.get(i)));
        if (i < entries.size() - 1) {
          buffer.append(",");
        }
      }
      buffer.append("]");
      return buffer.toString();
    } catch (Exception e) {
      throw new AdministratorException(e);
    }
  }

  /**
   * @see eu.baltrad.beast.admin.JsonGenerator#toJson(AnomalyDetector)
   */
  @Override
  public String toJson(AnomalyDetector detector) {
    try {
      return mapper.writeValueAsString(detector);
    } catch (Exception e) {
      throw new AdministratorException(e);
    }
  }

  /**
   * @see eu.baltrad.beast.admin.JsonGenerator#toJsonFromAnomalyDetectorList(List)
   */
  @Override
  public String toJsonFromAnomalyDetectorList(List<AnomalyDetector> detectors) {
    try {
      StringBuffer buffer = new StringBuffer();
      buffer.append("[");
      for (int i = 0; i < detectors.size(); i++) {
        buffer.append(toJson(detectors.get(i)));
        if (i < detectors.size() - 1) {
          buffer.append(",");
        }
      }
      buffer.append("]");
      return buffer.toString();
    } catch (Exception e) {
      throw new AdministratorException(e);
    }
  }

  /**
   * @see eu.baltrad.beast.admin.JsonGenerator#toJson(User)
   */
  @Override
  public String toJson(User user) {
    try {
      return mapper.writeValueAsString(user);
    } catch (Exception e) {
      throw new AdministratorException(e);
    }
  }

  /**
   * @see eu.baltrad.beast.admin.JsonGenerator#toJsonFromUsers(List)
   */
  @Override
  public String toJsonFromUsers(List<User> users) {
    try {
      StringBuffer buffer = new StringBuffer();
      buffer.append("[");
      for (int i = 0; i < users.size(); i++) {
        buffer.append(toJson(users.get(i)));
        if (i < users.size() - 1) {
          buffer.append(",");
        }
      }
      buffer.append("]");
      return buffer.toString();
    } catch (Exception e) {
      throw new AdministratorException(e);
    }
  }

  @Override
  public String toJson(Settings settings) {
    try {
      return mapper.writeValueAsString(settings);
    } catch (Exception e) {
      throw new AdministratorException(e);
    }
  }
}
