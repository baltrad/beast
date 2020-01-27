/**
 * 
 */
package eu.baltrad.beast.exchange.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import eu.baltrad.beast.exchange.ExchangeMessage;
import eu.baltrad.beast.security.AuthorizationRequest;

/**
 * @author anders
 *
 */
public class RequestMapperImpl implements RequestMapper {
  private JsonNodeFactory factory = JsonNodeFactory.instance;
  private ObjectMapper jsonMapper = new ObjectMapper();
  
  @Override
  public String toJson(AuthorizationRequest request) {
    ObjectNode jsonRequest = factory.objectNode();
    ObjectNode authorizationRequest = factory.objectNode();
    authorizationRequest.put("NodeName", request.getNodeName());
    authorizationRequest.put("NodeEmail", request.getNodeEmail());
    authorizationRequest.put("NodeAddress", request.getNodeAddress());
    authorizationRequest.put("Message",  request.getMessage());
    authorizationRequest.put("PublicKey", Base64.getEncoder().encodeToString(request.getPublicKey()));
    authorizationRequest.put("Checksum", request.getChecksum());
    authorizationRequest.put("RequestUUID", request.getRequestUUID());

    jsonRequest.put("MessageType", "DEX-Message");
    jsonRequest.put("MessageVersion", "2.2");
    jsonRequest.put("ObjectClass",  "AuthorizationRequest");
    jsonRequest.put("Object", authorizationRequest);
    try {
      return jsonMapper.writeValueAsString(jsonRequest);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public ExchangeMessage parse(InputStream jsonStream) {
    try {
      JsonNode node = jsonMapper.readTree(jsonStream);
      return parse(node);
    } catch (JsonProcessingException e) {
      throw new RequestMapperException(e);
    } catch (IOException e) {
      throw new RequestMapperException(e);
    }
  }

  @Override
  public ExchangeMessage parse(String json) {
    try {
      JsonNode node = jsonMapper.readTree(json);
      return parse(node);
    } catch (JsonProcessingException e) {
      throw new RequestMapperException(e);
    } catch (IOException e) {
      throw new RequestMapperException(e);
    }
  }

  public ExchangeMessage parse(JsonNode node) {
    ExchangeMessage message = new ExchangeMessage();
    if (node.has("MessageType") && node.get("MessageType").getValueAsText().equals("DEX-Message")) {
      message.setVersion(node.get("MessageVersion").getValueAsText());
      String objectClass = node.get("ObjectClass").getValueAsText();
      if (objectClass.equals("AuthorizationRequest")) {
        AuthorizationRequest request = new AuthorizationRequest();
        JsonNode objectNode = node.get("Object");
        request.setNodeName(objectNode.get("NodeName").getValueAsText());
        request.setNodeEmail(objectNode.get("NodeEmail").getValueAsText());
        request.setNodeAddress(objectNode.get("NodeAddress").getValueAsText());
        request.setMessage(objectNode.get("Message").getValueAsText());
        String publickey = objectNode.get("PublicKey").getValueAsText();
        if (publickey != null && publickey.length() > 0) {
          request.setPublicKey(Base64.getDecoder().decode(publickey));
        }
        request.setChecksum(objectNode.get("Checksum").getValueAsText());
        request.setRequestUUID(objectNode.get("RequestUUID").getValueAsText());
        message.setAuthorizationRequest(request);
      }
    } else {
      throw new RequestMapperException("Message not reckognized");
    }
    return message;
  }

}
