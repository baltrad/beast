/**
 * 
 */
package eu.baltrad.beast.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;

import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import junit.framework.TestCase;

/**
 * @author anders
 *
 */
public class AuthorizationRequestManagerITest  extends TestCase {
  private AbstractApplicationContext dbcontext = null;
  private AbstractApplicationContext context = null;
  
  private BeastDBTestHelper helper = null;
  private AuthorizationRequestManager classUnderTest = null;
  private JdbcOperations template = null;
  
  private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  
  @Before
  public void setUp() throws Exception {
    dbcontext = BeastDBTestHelper.loadDbContext(this);
    helper = (BeastDBTestHelper)dbcontext.getBean("helper");
    helper.tearDown();
    helper.purgeBaltradDB();
    
    helper.cleanInsert(this);
    template = (JdbcOperations)dbcontext.getBean("jdbcTemplate");
    
    context = BeastDBTestHelper.loadContext(this);
    classUnderTest = new AuthorizationRequestManager();
    classUnderTest.setJdbcTemplate(template);
  }
  
  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
    helper = null;
    template = null;
    context.close();
    dbcontext.close();
  }
  
  protected void verifyDatabaseTables(String extras) throws Exception {
    ITable expected = helper.getXlsTable(this, extras, "beast_authorization_request");
    ITable actual = helper.getDatabaseTable("beast_authorization_request");
    Assertion.assertEquals(expected, actual);
  }
  
  @Test
  public void test_add() throws Exception {
    // If receiving a request from another node, outgoing should be set to false. If sending a request, outgoing should be set to true
    AuthorizationRequest request = new AuthorizationRequest();
    request.setChecksum("123");
    request.setMessage("Hello");
    request.setNodeAddress("http://other.se");
    request.setNodeEmail("a@be");
    request.setNodeName("othernode");
    request.setOutgoing(false);
    request.setPublicKey("publickey".getBytes());
    request.setAutorequest(true);
    Calendar c = Calendar.getInstance();
    c.set(2019, 0, 2, 10, 0, 0);
    c.set(Calendar.MILLISECOND, 0);
    request.setReceivedAt(c.getTime());
    request.setRemoteHost("123.123.123.123");
    request.setRequestUUID("g-h-i-j");
    request.setRemoteAddress("http://y.se");
    classUnderTest.add(request);

    verifyDatabaseTables("add");
    assertEquals(3, request.getId());
  }

  @Test
  public void test_add_duplicate_uuid() throws Exception {
    // If receiving a request from another node, outgoing should be set to false. If sending a request, outgoing should be set to true
    AuthorizationRequest request = new AuthorizationRequest();
    request.setChecksum("123");
    request.setMessage("Hello");
    request.setNodeAddress("http://other.se");
    request.setNodeEmail("a@be");
    request.setNodeName("othernode");
    request.setOutgoing(false);
    request.setPublicKey("publickey".getBytes());
    request.setAutorequest(true);
    Calendar c = Calendar.getInstance();
    c.set(2019, 0, 2, 10, 0, 0);
    c.set(Calendar.MILLISECOND, 0);
    request.setReceivedAt(c.getTime());
    request.setRemoteHost("123.123.123.123");
    request.setRequestUUID("a-b-c-d");
    request.setRemoteAddress("http://y.se");
    try {
      classUnderTest.add(request);
      fail("Expected DataAccessException");
    } catch (DataAccessException e) {
      // pass
    }

    verifyDatabaseTables(null);
  }
  
  @Test
  public void test_update() throws Exception {
    // If receiving a request from another node, outgoing should be set to false. If sending a request, outgoing should be set to true
    AuthorizationRequest request = new AuthorizationRequest();
    request.setId(2);
    request.setChecksum("123");
    request.setMessage("Hello");
    request.setNodeAddress("http://other.se");
    request.setNodeEmail("a@be");
    request.setNodeName("othernode");
    request.setOutgoing(false);
    request.setPublicKey("publickey".getBytes());
    request.setAutorequest(true);
    Calendar c = Calendar.getInstance();
    c.set(2019, 0, 2, 10, 0, 0);
    c.set(Calendar.MILLISECOND, 0);
    request.setReceivedAt(c.getTime());
    request.setRemoteHost("123.123.123.123");
    request.setRequestUUID("g-h-i-j");
    request.setRemoteAddress("http://z.se");
    classUnderTest.update(request);

    verifyDatabaseTables("update");
  }
  
  @Test
  public void test_update_noSuchId() throws Exception {
    // If receiving a request from another node, outgoing should be set to false. If sending a request, outgoing should be set to true
    AuthorizationRequest request = new AuthorizationRequest();
    request.setId(3);
    request.setChecksum("123");
    request.setMessage("Hello");
    request.setNodeAddress("http://other.se");
    request.setNodeEmail("a@be");
    request.setNodeName("othernode");
    request.setOutgoing(false);
    request.setPublicKey("publickey".getBytes());
    Calendar c = Calendar.getInstance();
    c.set(2019, 0, 2, 10, 0, 0);
    c.set(Calendar.MILLISECOND, 0);
    request.setReceivedAt(c.getTime());
    request.setRemoteHost("123.123.123.123");
    request.setRequestUUID("g-h-i-j");
    request.setAutorequest(true);
    
    try {
      classUnderTest.update(request);
      fail("Expected DataAccessException");
    } catch (DataAccessException e) {
      // pass
    }

    verifyDatabaseTables(null);
  }
  
  @Test
  public void test_remove_byid() throws Exception {
    classUnderTest.remove(2);
    verifyDatabaseTables("remove");
  }

  @Test
  public void test_remove_byrequestuuid() throws Exception {
    classUnderTest.remove("d-e-f-g");
    verifyDatabaseTables("remove");
  }

  @Test
  public void test_get_by_id() throws Exception {
    AuthorizationRequest request = classUnderTest.get(2);
    assertEquals("my_second_node", request.getNodeName());
    assertEquals("a@second.se", request.getNodeEmail());
    assertEquals("http://second.se:1234", request.getNodeAddress());
    assertEquals("daadsaas", request.getChecksum());
    assertEquals("asasdadsa\n", new String(request.getPublicKey()));
    assertEquals(null, request.getMessage());
    assertEquals(true, request.isOutgoing());
    assertEquals("1.2.3.5", request.getRemoteHost());
    assertEquals("2019-01-01 12:00:00", dateFormat.format(request.getReceivedAt()));
    assertEquals("d-e-f-g", request.getRequestUUID());
    assertEquals(false, request.isAutorequest());
  }

  @Test
  public void test_get_by_uuid() throws Exception {
    AuthorizationRequest request = classUnderTest.get("d-e-f-g", true);
    assertEquals("my_second_node", request.getNodeName());
    assertEquals("a@second.se", request.getNodeEmail());
    assertEquals("http://second.se:1234", request.getNodeAddress());
    assertEquals("daadsaas", request.getChecksum());
    assertEquals("asasdadsa\n", new String(request.getPublicKey()));
    assertEquals(null, request.getMessage());
    assertEquals(true, request.isOutgoing());
    assertEquals("1.2.3.5", request.getRemoteHost());
    assertEquals("2019-01-01 12:00:00", dateFormat.format(request.getReceivedAt()));
    assertEquals("d-e-f-g", request.getRequestUUID());
    assertEquals(false, request.isAutorequest());
  }
}
