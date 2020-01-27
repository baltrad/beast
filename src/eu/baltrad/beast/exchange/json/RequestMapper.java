/**
 * 
 */
package eu.baltrad.beast.exchange.json;

import java.io.InputStream;

import eu.baltrad.beast.exchange.ExchangeMessage;
import eu.baltrad.beast.security.AuthorizationRequest;

/**
 * Translates between json and objects used for exchange.
 * @author anders
 */
public interface RequestMapper {
  /**
   * Translates an authorization request into a json string
   * @param request the request
   * @return the json string
   */
  public String toJson(AuthorizationRequest request);
  
  /**
   * Parses an input stream containing json
   * @param jsonStream the json stream
   * @return the exchange message
   */
  public ExchangeMessage parse(InputStream jsonStream);
  
  /**
   * Parses a json string
   * @param json the json string
   * @return the exchange message
   */
  public ExchangeMessage parse(String json);
}
