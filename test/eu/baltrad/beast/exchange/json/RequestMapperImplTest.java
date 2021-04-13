/**
 * 
 */
package eu.baltrad.beast.exchange.json;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Calendar;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.exchange.ExchangeMessage;
import eu.baltrad.beast.security.AuthorizationRequest;

/**
 * @author anders
 *
 */
public class RequestMapperImplTest extends EasyMockSupport {
  private RequestMapperImpl classUnderTest;
  
  private JsonNodeFactory factory = JsonNodeFactory.instance;
  private ObjectMapper jsonMapper;

  @Before
  public void setUp() {
    classUnderTest = new RequestMapperImpl();
    jsonMapper = new ObjectMapper();
  }
  
  @After
  public void tearDown() {
    classUnderTest = null;
    jsonMapper = null;
  }
  
  @Test
  public void toJson_AuthorizationRequest() throws Exception {
    AuthorizationRequest request = new AuthorizationRequest();
    request.setNodeName("othernode");
    request.setNodeEmail("a@be");
    request.setNodeAddress("http://other.se");
    request.setChecksum("123");
    request.setMessage("Hello");
    request.setOutgoing(false);
    request.setPublicKey("publickey".getBytes());
    Calendar c = Calendar.getInstance();
    c.set(2019, 0, 2, 10, 0, 0);
    c.set(Calendar.MILLISECOND, 0);
    request.setReceivedAt(c.getTime());
    request.setRemoteHost("123.123.123.123");
    request.setRequestUUID("g-h-i-j");
    
    String json = classUnderTest.toJson(request);
    
    System.out.println(json);
    
    JsonNode node = jsonMapper.readTree(json);
    assertEquals("DEX-Message", node.get("MessageType").asText());
    assertEquals("2.2", node.get("MessageVersion").asText());
    assertEquals("AuthorizationRequest", node.get("ObjectClass").asText());
    JsonNode objectNode = node.get("Object");
    assertEquals("othernode", objectNode.get("NodeName").asText());
    assertEquals("a@be", objectNode.get("NodeEmail").asText());
    assertEquals("http://other.se", objectNode.get("NodeAddress").asText());
    assertEquals("Hello", objectNode.get("Message").asText());
    assertEquals("publickey", new String(Base64.getDecoder().decode(objectNode.get("PublicKey").asText())));
    assertEquals("123", objectNode.get("Checksum").asText());
    assertEquals("g-h-i-j", objectNode.get("RequestUUID").asText());
  }
  
  @Test
  public void parse_AuthorizationRequest_str() {
    String json = "{\"MessageType\":\"DEX-Message\",\"MessageVersion\":\"2.2\",\"ObjectClass\":\"AuthorizationRequest\",\"Object\":{\"NodeName\":\"othernode\",\"NodeEmail\":\"a@be\",\"NodeAddress\":\"http://other.se\",\"Message\":\"Hello\",\"PublicKey\":\"cHVibGlja2V5\",\"Checksum\":\"123\",\"RequestUUID\":\"g-h-i-j\"}}";
    
    ExchangeMessage result = classUnderTest.parse(json);

    assertEquals("2.2", result.getVersion());
    assertEquals("othernode", result.getAuthorizationRequest().getNodeName());
    assertEquals("a@be", result.getAuthorizationRequest().getNodeEmail());
    assertEquals("http://other.se", result.getAuthorizationRequest().getNodeAddress());
    assertEquals("Hello", result.getAuthorizationRequest().getMessage());
    assertEquals("publickey", new String(result.getAuthorizationRequest().getPublicKey()));
    assertEquals("123", result.getAuthorizationRequest().getChecksum());
    assertEquals("g-h-i-j", result.getAuthorizationRequest().getRequestUUID());
  }

  @Test
  public void parse_AuthorizationRequest_inputStream() {
    String json = "{\"MessageType\":\"DEX-Message\",\"MessageVersion\":\"2.2\",\"ObjectClass\":\"AuthorizationRequest\",\"Object\":{\"NodeName\":\"othernode\",\"NodeEmail\":\"a@be\",\"NodeAddress\":\"http://other.se\",\"Message\":\"Hello\",\"PublicKey\":\"cHVibGlja2V5\",\"Checksum\":\"123\",\"RequestUUID\":\"g-h-i-j\"}}";
    InputStream inputStream = new ByteArrayInputStream(json.getBytes());
    ExchangeMessage result = classUnderTest.parse(inputStream);

    assertEquals("2.2", result.getVersion());
    assertEquals("othernode", result.getAuthorizationRequest().getNodeName());
    assertEquals("a@be", result.getAuthorizationRequest().getNodeEmail());
    assertEquals("http://other.se", result.getAuthorizationRequest().getNodeAddress());
    assertEquals("Hello", result.getAuthorizationRequest().getMessage());
    assertEquals("publickey", new String(result.getAuthorizationRequest().getPublicKey()));
    assertEquals("123", result.getAuthorizationRequest().getChecksum());
    assertEquals("g-h-i-j", result.getAuthorizationRequest().getRequestUUID());
  }

}
