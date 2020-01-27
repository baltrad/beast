/**
 * 
 */
package eu.baltrad.beast.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * @author anders
 *
 */
public class AuthorizationRequestManager implements IAuthorizationRequestManager {
  /**
   * The jdbc template
   */
  private JdbcOperations template = null;

  /**
   * Logger
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
   * @see IAuthorizationRequestManager#add(AuthorizationRequest)
   */
  @Override
  public void add(AuthorizationRequest request) {
    KeyHolder holder = new GeneratedKeyHolder();
    
    String sqlstr = "INSERT INTO beast_authorization_request (nodename, nodeemail, nodeaddress, checksum, publickey, message, outgoing, requestuuid, remotehost, receivedat, autorequest, remoteaddress) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
    
    int result = template.update(new PreparedStatementCreator() {
      @Override
      public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sqlstr, new String[] { "id" });
        ps.setString(1, request.getNodeName());
        ps.setString(2, request.getNodeEmail());
        ps.setString(3, request.getNodeAddress());
        ps.setString(4, request.getChecksum());
        String publicKey = null;
        if (request.getPublicKey() != null && request.getPublicKey().length > 0) {
          publicKey = Base64.getEncoder().encodeToString(request.getPublicKey());
        }
        ps.setString(5, publicKey);
        ps.setString(6, request.getMessage());
        ps.setBoolean(7, request.isOutgoing());
        ps.setString(8, request.getRequestUUID());
        ps.setString(9, request.getRemoteHost());
        Timestamp timestamp = null;
        if (request.getReceivedAt() != null) {
          timestamp = new Timestamp(request.getReceivedAt().getTime());
        }
        ps.setTimestamp(10, timestamp);
        ps.setBoolean(11, request.isAutorequest());
        ps.setString(12, request.getRemoteAddress());
        return ps;
      }
    }, holder);
    if (result != 1) {
      throw new DataIntegrityViolationException("Could not update entry");
    }
    logger.info("Added request: " + request.getNodeAddress());
    request.setId(holder.getKey().intValue());
  }

  /**
   * @see IAuthorizationRequestManager#get(int)
   */
  @Override
  public AuthorizationRequest get(int id) {
    try {
      return template.queryForObject(
          "select * from beast_authorization_request where id=?",
          getMapper(),
          new Object[]{id});
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

  /**
   * @see IAuthorizationRequestManager#get(String, boolean)
   */
  @Override
  public AuthorizationRequest get(String uuid, boolean outgoing) {
    try {
      return template.queryForObject(
          "select * from beast_authorization_request where requestuuid=? and outgoing=?",
          getMapper(),
          new Object[]{uuid, outgoing});
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

  /**
   * @see IAuthorizationRequestManager#getByRemoteAddress(String)
   */
  @Override
  public AuthorizationRequest getByRemoteAddress(String remoteAddress) {
    try {
      return template.queryForObject(
          "select * from beast_authorization_request where remoteaddress=?",
          getMapper(),
          new Object[]{remoteAddress});
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }
  
  /**
   * @see IAuthorizationRequestManager#getByNodeName(String)
   */
  @Override
  public AuthorizationRequest getByNodeName(String nodeName) {
    try {
      return template.queryForObject(
          "select * from beast_authorization_request where nodeName=?",
          getMapper(),
          new Object[]{nodeName});
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

  /**
   * @see IAuthorizationRequestManager#findByRemoteHost(String)
   */
  @Override
  public List<AuthorizationRequest> findByRemoteHost(String address) {
    return template.query(
        "select * from beast_authorization_request where remotehost=?",
        getMapper(),
        new Object[] {address});
  }

  /**
   * @see IAuthorizationRequestManager#findByOutgoing(boolean)
   * 
   */
  @Override
  public List<AuthorizationRequest> findByOutgoing(boolean outgoing) {
    return template.query(
        "select * from beast_authorization_request where outgoing=?",
        getMapper(),
        new Object[] {outgoing});
  }
  
  /**
   * @see IAuthorizationRequestManager#list()
   */
  @Override
  public List<AuthorizationRequest> list() {
    return template.query(
        "select * from beast_authorization_request",
        getMapper());
  }
  
  /**
   * @see IAuthorizationRequestManager#update(AuthorizationRequest)
   */
  @Override
  public void update(AuthorizationRequest request) {
    String publicKey = null;
    Timestamp timestamp = null;
    if (request.getPublicKey() != null && request.getPublicKey().length > 0)  {
      publicKey = Base64.getEncoder().encodeToString(request.getPublicKey());
    }
    if (request.getReceivedAt() != null) {
      timestamp = new Timestamp(request.getReceivedAt().getTime());
    }    
    int count = template.update(
        "UPDATE beast_authorization_request " +
        "SET " +
        " nodename=?, nodeemail=?, nodeaddress=?, checksum=?, publickey=?, message=?, outgoing=?, requestuuid=?, remotehost=?, receivedat=?, autorequest=?, remoteaddress=? " +
        "WHERE id=?",
        new Object[] {request.getNodeName(), request.getNodeEmail(), request.getNodeAddress(), request.getChecksum(),
            publicKey, request.getMessage(), request.isOutgoing(), request.getRequestUUID(), request.getRemoteHost(), timestamp, request.isAutorequest(), 
            request.getRemoteAddress(), request.getId()});
    if (count != 1) {
      throw new DataIntegrityViolationException("Could not update authorization request="+request.getId());
    }
  }

  /**
   * @see IAuthorizationRequestManager#remove(int)
   */
  @Override
  public void remove(int requestId) {
    template.update(
        "DELETE FROM beast_authorization_request where id=?",
        new Object[] {requestId});
  }
  
  /**
   * @see IAuthorizationRequestManager#remove(String)
   */
  @Override
  public void remove(String uuid) {
    template.update(
        "DELETE FROM beast_authorization_request where requestuuid=?",
        new Object[] {uuid});
  }
  
  /**
   * @return the AuthorizationRequest mapper
   */
  protected RowMapper<AuthorizationRequest> getMapper() {
    return new RowMapper<AuthorizationRequest>() {
      @Override
      public AuthorizationRequest mapRow(ResultSet rs, int rnum)
          throws SQLException {
        AuthorizationRequest result = createRequest();
        result.setId(rs.getInt("id"));
        result.setNodeName(rs.getString("nodename"));
        result.setNodeEmail(rs.getString("nodeemail"));
        result.setNodeAddress(rs.getString("nodeaddress"));
        result.setChecksum(rs.getString("checksum"));
        String publickey = rs.getString("publickey");
        if (publickey != null && publickey.length() > 0)
          result.setPublicKey(Base64.getDecoder().decode(publickey));
        result.setMessage(rs.getString("message"));
        result.setOutgoing(rs.getBoolean("outgoing"));
        result.setRequestUUID(rs.getString("requestuuid"));
        result.setRemoteHost(rs.getString("remotehost"));
        Timestamp ts = rs.getTimestamp("receivedat");
        if (ts != null) {
          result.setReceivedAt(new Date(ts.getTime()));
        }
        result.setAutorequest(rs.getBoolean("autorequest"));
        result.setRemoteAddress(rs.getString("remoteaddress"));
        return result;
      }
    };
  }

  /**
   * @return a random uuid string
   */
  @Override
  public String createUUID() {
    return UUID.randomUUID().toString();
  }

  /**
   * @see IAuthorizationRequestManager#toAuthorization(AuthorizationRequest)
   */
  @Override
  public Authorization toAuthorization(AuthorizationRequest request) {
    Authorization result = null;
    if (request != null) {
      result = new Authorization();
      result.setNodeName(request.getNodeName());
      result.setNodeEmail(request.getNodeEmail());
      result.setNodeAddress(request.getNodeAddress());
      result.setPublicKey(request.getPublicKey());
      result.setConnectionUUID(request.getRequestUUID());
      result.setAuthorized(false);
      result.setLocal(false);
      result.setLastUpdated(new Date());
    }
    return result;
  }

  @Override
  public AuthorizationRequest createRequest() {
    return new AuthorizationRequest();
  }
}
