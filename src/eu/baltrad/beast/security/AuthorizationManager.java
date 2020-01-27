/**
 * 
 */
package eu.baltrad.beast.security;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author anders
 *
 */
public class AuthorizationManager implements IAuthorizationManager {
  /**
   * The jdbc template
   */
  private JdbcOperations template = null;

  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(AuthorizationRequestManager.class);

  /**
   * @param template the jdbc template to set
   */
  @Autowired
  public void setJdbcTemplate(JdbcOperations template) {
    this.template = template;
  }

  /**
   * @see IAuthorizationManager#add(Authorization)
   */
  @Override
  public void add(Authorization auth) {
    String publickey = null;
    String privatekey = null;
    if (auth.getPublicKey() != null && auth.getPublicKey().length > 0) {
      publickey = Base64.getEncoder().encodeToString(auth.getPublicKey());
    }
    if (auth.getPrivateKey() != null && auth.getPrivateKey().length > 0) {
      privatekey = Base64.getEncoder().encodeToString(auth.getPrivateKey());
    }
    if (auth.getConnectionUUID() == null) {
      auth.setConnectionUUID(UUID.randomUUID().toString());
    }
    Timestamp lastupdated = null;
    if (auth.getLastUpdated() == null) {
      auth.setLastUpdated(new Date());
    }
    lastupdated = new Timestamp(auth.getLastUpdated().getTime());
    template.update(
        "INSERT INTO beast_authorization (nodename,nodeemail,nodeaddress,redirected_address,publickey,publickeypath,privatekey,privatekeypath,lastupdated,authorized,injector,local,connectionuuid) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
        new Object[] {auth.getNodeName(), auth.getNodeEmail(), auth.getNodeAddress(), auth.getRedirectedAddress(), publickey, auth.getPublicKeyPath(), privatekey, auth.getPrivateKeyPath(), lastupdated, auth.isAuthorized(), auth.isInjector(), auth.isLocal(), auth.getConnectionUUID()});
  }

  /**
   * @see IAuthorizationManager#update(Authorization)
   */
  @Override
  public void update(Authorization auth) {
    String publickey = null;
    String privatekey = null;
    if (auth.getPublicKey() != null && auth.getPublicKey().length > 0) {
      publickey = Base64.getEncoder().encodeToString(auth.getPublicKey());
    }
    if (auth.getPrivateKey() != null && auth.getPrivateKey().length > 0) {
      privatekey = Base64.getEncoder().encodeToString(auth.getPrivateKey());
    }
    Timestamp lastupdated = null;
    if (auth.getLastUpdated() == null) {
      auth.setLastUpdated(new Date());
    }
    lastupdated = new Timestamp(auth.getLastUpdated().getTime());
    int result = template.update(
        "UPDATE beast_authorization " +
        "SET nodename=?, nodeemail=?, nodeaddress=?, redirected_address=?, publickey=?, publickeypath=?, privatekey=?, privatekeypath=?, lastupdated=?, authorized=?, injector=?, local=? " +
        "WHERE connectionuuid=?",
        new Object[] {auth.getNodeName(), auth.getNodeEmail(), auth.getNodeAddress(), auth.getRedirectedAddress(), publickey, auth.getPublicKeyPath(), privatekey, auth.getPrivateKeyPath(), 
            lastupdated, auth.isAuthorized(), auth.isInjector(), auth.isLocal(), auth.getConnectionUUID()});
    if (result != 1) {
      throw new DataIntegrityViolationException("Could not update authorization request="+auth.getConnectionUUID());
    }
  }

  /**
   * @see IAuthorizationManager#delete(String)
   */
  @Override
  public void delete(String uuid) {
    int result = template.update(
        "DELETE from beast_authorization WHERE connectionuuid=?", new Object[] {uuid});
    logger.info("delete = " + result);
  }
  
  /**
   * @see IAuthorizationManager#updateByNodeName(Authorization)
   */
  @Override
  public void updateByNodeName(Authorization auth) {
    String publickey = null;
    String privatekey = null;
    if (auth.getPublicKey() != null && auth.getPublicKey().length > 0) {
      publickey = Base64.getEncoder().encodeToString(auth.getPublicKey());
    }
    if (auth.getPrivateKey() != null && auth.getPrivateKey().length > 0) {
      privatekey = Base64.getEncoder().encodeToString(auth.getPrivateKey());
    }
    Timestamp lastupdated = null;
    if (auth.getLastUpdated() == null) {
      auth.setLastUpdated(new Date());
    }
    lastupdated = new Timestamp(auth.getLastUpdated().getTime());
    int result = template.update(
        "UPDATE beast_authorization " +
        "SET nodeemail=?, nodeaddress=?, redirected_address=?, publickey=?, publickeypath=?, privatekey=?, privatekeypath=?, lastupdated=?, authorized=?, injector=?, local=?, connectionuuid=? " +
        "WHERE nodename=?",
        new Object[] {auth.getNodeEmail(), auth.getNodeAddress(), auth.getRedirectedAddress(), publickey, auth.getPublicKeyPath(), privatekey, auth.getPrivateKeyPath(), 
            lastupdated, auth.isAuthorized(), auth.isInjector(), auth.isLocal(), auth.getConnectionUUID(), auth.getNodeName() });
    if (result != 1) {
      throw new DataIntegrityViolationException("Could not update authorization request="+auth.getNodeName());
    }
  }
  
  /**
   * @see IAuthorizationManager#get(String)
   */
  @Override
  public Authorization get(String uuid) {
    return template.queryForObject(
        "SELECT * from beast_authorization WHERE connectionuuid=?",
        getMapper(),
        new Object[] {uuid});
  }

  /**
   * @see IAuthorizationManager#getByNodeName(String)
   */
  @Override
  public Authorization getByNodeName(String nodeName) {
    try {
      return template.queryForObject(
          "SELECT * from beast_authorization WHERE nodename=?",
          getMapper(),
          new Object[] {nodeName});
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }
  
  /**
   * @see IAuthorizationManager#getLocal()
   */
  @Override
  public Authorization getLocal() {
    try {
      return template.queryForObject(
          "SELECT * from beast_authorization WHERE local=true",
          getMapper());
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }
 
  /**
   * @see IAuthorizationManager#list()
   */
  @Override
  public List<Authorization> list() {
    return template.query(
        "SELECT * from beast_authorization",
        getMapper());
  }
  
  /**
   * @return the Authorization mapper
   */
  protected RowMapper<Authorization> getMapper() {
    return new RowMapper<Authorization>() {
      @Override
      public Authorization mapRow(ResultSet rs, int rnum)
          throws SQLException {
        Authorization result = createAuthorization();
        result.setNodeName(rs.getString("nodename"));
        result.setNodeEmail(rs.getString("nodeemail"));
        result.setNodeAddress(rs.getString("nodeaddress"));
        result.setRedirectedAddress(rs.getString("redirected_address"));
        String publickey = rs.getString("publickey");
        if (publickey != null && publickey.length() > 0)
          result.setPublicKey(Base64.getDecoder().decode(publickey));
        result.setPublicKeyPath(rs.getString("publickeypath"));
        String privatekey = rs.getString("privatekey");
        if (privatekey != null && privatekey.length() > 0)
          result.setPrivateKey(Base64.getDecoder().decode(privatekey));
        result.setPrivateKeyPath(rs.getString("privatekeypath"));
        Timestamp ts = rs.getTimestamp("lastupdated");
        if (ts != null) {
          result.setLastUpdated(new Date(ts.getTime()));
        }
        result.setAuthorized(rs.getBoolean("authorized"));
        result.setInjector(rs.getBoolean("injector"));
        result.setLocal(rs.getBoolean("local"));
        result.setConnectionUUID(rs.getString("connectionuuid"));
        return result;
      }
    };
  }
  
  /**
   * @return a new instance of authorization. Used due to testing purposes.
   */
  protected Authorization createAuthorization() {
    return new Authorization();
  }

  /**
   * @see IAuthorizationManager#createAuthorizationRequest(String) 
   */
  @Override
  public AuthorizationRequest createAuthorizationRequest(String message) {
    Authorization local = getLocal();
    AuthorizationRequest request = null;
    if (local == null) {
      throw new AuthorizationException("No local authorization");
    }
    request = toAuthorizationRequest(local, message);
    request.setRequestUUID(UUID.randomUUID().toString());
    return request;
  }
  
  /**
   * @see IAuthorizationManager#toAuthorizationRequest(Authorization, String)
   */
  @Override
  public AuthorizationRequest toAuthorizationRequest(Authorization authorization, String message) {
    AuthorizationRequest request = new AuthorizationRequest();
    request.setNodeName(authorization.getNodeName());
    request.setNodeEmail(authorization.getNodeEmail());
    request.setNodeAddress(authorization.getNodeAddress());
    request.setPublicKey(authorization.getPublicKey());
    request.setChecksum(DigestUtils.md5Hex(request.getPublicKey()));
    request.setMessage(message);
    request.setRequestUUID(authorization.getConnectionUUID());
    request.setAutorequest(true);
    return request;
  }
}
