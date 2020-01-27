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
public class AuthorizationManagerITest  extends TestCase {
  private AbstractApplicationContext dbcontext = null;
  private AbstractApplicationContext context = null;
  
  private BeastDBTestHelper helper = null;
  private AuthorizationManager classUnderTest = null;
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
    classUnderTest = new AuthorizationManager();
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
    ITable expected = helper.getXlsTable(this, extras, "beast_authorization", new String[] {"nodename"});
    ITable actual = helper.getDatabaseTable("beast_authorization", new String[] {"nodename"});
    Assertion.assertEquals(expected, actual);
  }
  
  @Test
  public void test_add() throws Exception {
    // If receiving a request from another node, outgoing should be set to false. If sending a request, outgoing should be set to true
    Authorization auth = new Authorization();
    auth.setNodeName("d");
    auth.setNodeEmail("a@be");
    auth.setNodeAddress("http://other.se");
    auth.setPublicKey("publickey".getBytes());
    auth.setPublicKeyPath("/new/public/path");
    Calendar c = Calendar.getInstance();
    c.set(2019, 0, 2, 10, 0, 0);
    c.set(Calendar.MILLISECOND, 0);
    auth.setLastUpdated(c.getTime());
    auth.setAuthorized(true);
    auth.setLocal(false);
    auth.setConnectionUUID("a-z-d-e");
    classUnderTest.add(auth);

    verifyDatabaseTables("add");
  }

  @Test
  public void test_update() throws Exception {
    Authorization auth = new Authorization();
    auth.setNodeName("ba");
    auth.setNodeEmail("a@be");
    auth.setNodeAddress("http://other.se");
    auth.setPublicKey("publickey".getBytes());
    auth.setPublicKeyPath("/new/public/path");
    Calendar c = Calendar.getInstance();
    c.set(2019, 0, 2, 10, 0, 0);
    c.set(Calendar.MILLISECOND, 0);
    auth.setLastUpdated(c.getTime());
    auth.setAuthorized(true);
    auth.setInjector(false);
    auth.setLocal(false);
    auth.setConnectionUUID("4-3-2-1");
    
    classUnderTest.update(auth);
    
    verifyDatabaseTables("update");
  }

  @Test
  public void test_delete() throws Exception {
    classUnderTest.delete("4-3-2-1");
    
    verifyDatabaseTables("delete");
  }
  
  @Test
  public void test_updateByNodeName() throws Exception {
    Authorization auth = new Authorization();
    auth.setNodeName("b");
    auth.setNodeEmail("a@be");
    auth.setNodeAddress("http://other.se");
    auth.setPublicKey("publickey".getBytes());
    auth.setPublicKeyPath("/new/public/path");
    Calendar c = Calendar.getInstance();
    c.set(2019, 0, 2, 10, 0, 0);
    c.set(Calendar.MILLISECOND, 0);
    auth.setLastUpdated(c.getTime());
    auth.setAuthorized(true);
    auth.setInjector(false);
    auth.setLocal(false);
    auth.setConnectionUUID("5-6-7-8");
    
    classUnderTest.updateByNodeName(auth);
    
    verifyDatabaseTables("updatebynodename");
  }
  
  @Test
  public void test_get() {
    Authorization auth = classUnderTest.get("4-3-2-1");
    assertEquals("b", auth.getNodeName());
    assertEquals("a@second.selocal", auth.getNodeEmail());
    assertEquals("http://localhost.se", auth.getNodeAddress());
    assertEquals("public key", new String(auth.getPublicKey()));
    assertEquals("/public/path2", auth.getPublicKeyPath());
    assertEquals("private key", new String(auth.getPrivateKey()));
    assertEquals("/private/path1", auth.getPrivateKeyPath());
    assertEquals("2019-01-01 12:00:00", dateFormat.format(auth.getLastUpdated()));
    assertEquals(true, auth.isAuthorized());
    assertEquals(true, auth.isLocal());
    assertEquals("4-3-2-1", auth.getConnectionUUID());
  }

  
  @Test
  public void test_getLocal() {
    Authorization local = classUnderTest.getLocal();
    assertEquals("b", local.getNodeName());
    assertEquals("a@second.selocal", local.getNodeEmail());
    assertEquals("public key", new String(local.getPublicKey()));
    assertEquals("private key", new String(local.getPrivateKey()));
  }

  @Test
  public void test_getLocal_nolocal() {
    Authorization local = classUnderTest.getLocal();
    local.setLocal(false);
    classUnderTest.update(local);
    assertEquals(null, classUnderTest.getLocal());
  }

  @Test
  public void test_createAuthorizationRequest()
  {
    AuthorizationRequest request = classUnderTest.createAuthorizationRequest("hello world");
    assertEquals("b", request.getNodeName());
    assertEquals("http://localhost.se", request.getNodeAddress());
    assertEquals("a@second.selocal", request.getNodeEmail());
    assertEquals("hello world", request.getMessage());
    assertEquals("public key", new String(request.getPublicKey()));
    assertFalse("4-3-2-1".equals(request.getRequestUUID()));
    assertEquals(true, request.isAutorequest());
    assertEquals(null, request.getRemoteAddress());
  }
}
