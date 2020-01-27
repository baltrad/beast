package eu.baltrad.beast.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.keyczar.exceptions.KeyczarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.baltrad.beast.security.crypto.CryptoFactory;
import eu.baltrad.beast.security.crypto.KeyczarCryptoFactory;
import eu.baltrad.beast.security.crypto.Signer;
import eu.baltrad.beast.security.crypto.Verifier;
import eu.baltrad.beast.security.keyczar.KeyCompressor;
import eu.baltrad.beast.security.mail.IAdminMailer;

public class SecurityManager implements ISecurityManager {
  /**
   * The authorization manager
   */
  private IAuthorizationManager authorizationManager;

  /**
   * The request manager
   */
  private IAuthorizationRequestManager authorizationRequestManager;

  /**
   * The location where the keyczar keys are located
   */
  private String keyczarKeyPath = null;
  
  /**
   * The crypto factory
   */
  private CryptoFactory cryptoFactory = null;
  
  /**
   * Local node name
   */
  private String localNodeName = null;
  
  /**
   * Local node name lock
   */
  private Object localNodeNameLock = new Object();
  
  /**
   * The logger
   */
  private static Logger logger = LogManager.getLogger(SecurityManager.class);
  
  /**
   * Constructor
   */
  public SecurityManager() {
  }
  
  /**
   * @param authorizationManager the authorization manager
   */
  @Autowired
  public void setAuthorizationManager(IAuthorizationManager authorizationManager) {
    this.authorizationManager = authorizationManager;
  }

  /**
   * @param factory the crypto factory
   */
  @Autowired
  public void setCryptoFactory(CryptoFactory factory) {
    this.cryptoFactory = factory;
  }
  
  /**
   * @param path the keyczar key path
   */
  public void setKeyczarKeyPath(String path) {
    this.keyczarKeyPath = path;
    this.cryptoFactory = new KeyczarCryptoFactory(new File(path));
  }
  
  /**
   * @returns path the keyczar key path
   */
  public String getKeyczarKeyPath() {
    return this.keyczarKeyPath;
  }

  /**
   * @see ISecurityManager#expandPublicKey(Authorization)
   */
  @Override
  public String expandPublicKey(Authorization authorization) {
    if (this.keyczarKeyPath == null) {
      throw new SecurityStorageException("No keyczar path defined");
    }
    byte[] keyContent = authorization.getPublicKey();
    File keypath = new File(this.keyczarKeyPath + File.separator + authorization.getNodeName() + ".pub"); // Keyczar public keys should be placed in .pub dir
    try {
      new KeyCompressor().unzip(keypath, keyContent);
    } catch (IOException e) {
      throw new SecurityStorageException(e);
    }
    return authorization.getNodeName() + ".pub";
  }

  /**
   * @see ISecurityManager#expandPrivateKey(Authorization)
   */
  @Override
  public String expandPrivateKey(Authorization authorization) {
    String result = null;
    if (this.keyczarKeyPath == null) {
      throw new SecurityStorageException("No keyczar path defined");
    }
    byte[] keyContent = authorization.getPrivateKey();
    if (keyContent != null && keyContent.length > 0) {
      File keypath = new File(this.keyczarKeyPath + File.separator + authorization.getNodeName() + ".priv"); // Keyczar private keys should be placed in .priv dir
      try {
        new KeyCompressor().unzip(keypath, keyContent);
      } catch (IOException e) {
        throw new SecurityStorageException(e);
      }
      result = authorization.getNodeName() + ".priv"; 
    }
    return result;
  }
  
  /**
   * @see ISecurityManager#getSigner(String)
   */
  @Override
  public Signer getSigner(String nodeName) {
    try {
      return cryptoFactory.createSigner(nodeName);
    } catch (Exception e) {
      throw new SecurityStorageException(e);
    }
  }

  /**
   * @see ISecurityManager#getVerifier(String)
   */
  @Override
  public Verifier getVerifier(String nodeName) {
    Authorization key = authorizationManager.getByNodeName(nodeName);
    logger.info("getVerifier for node=" + nodeName + ", key=" + key);
    if (key == null || key.isAuthorized() == false) {
      throw new SecurityStorageException("Not authorized");
    }
    try {
      return cryptoFactory.createVerifier(key.getNodeName());
    } catch (Exception e) {
      throw new SecurityStorageException(e);
    }
  }
  
  /**
   * @see ISecurityManager#validate(String, String, String)
   */
  @Override
  public boolean validate(String nodeName, String signature, String message) {
    try {
      return getVerifier(nodeName).verify(message, signature);
    } catch (KeyczarException e) {
      return false;
    }
  }

  /**
   * @see ISecurityManager#createSignature(String)
   */
  @Override
  public String createSignature(String message) {
    try {
      if (getLocalNodeName() == null) {
        localNodeName = getLocal().getNodeName();
      }
      return getSigner(localNodeName).sign(message);
    } catch (Exception e) {
      throw new SecurityStorageException(e);
    }
  }
  
  /**
   * @see IAuthorizationManager#getLocal()
   */
  @Override
  public Authorization getLocal() {
    return authorizationManager.getLocal();
  }

  /**
   * @see IAuthorizationManager#setLocal(Authorization)
   */
  @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
  @Override
  public void setLocal(Authorization auth) {
    Authorization currentLocal = getLocal();
    if (currentLocal != null) {
      currentLocal.setLocal(false);
      authorizationManager.update(currentLocal);
    }
    auth.setLocal(true);
    auth.setInjector(true);
    auth.setLastUpdated(new Date());
    if (authorizationManager.getByNodeName(auth.getNodeName()) == null) {
      authorizationManager.add(auth);
    } else {
      authorizationManager.updateByNodeName(auth);
    }
    setLocalNodeName(auth.getNodeName());
  }

  /**
   * @return the local node name
   */
  @Override
  public String getLocalNodeName() {
    if (localNodeName == null) {
      synchronized (localNodeNameLock) {
        localNodeName = getLocal().getNodeName();
      }
    }    
    return localNodeName;
  }

  /**
   * @param localNodeName the local node name
   */
  public void setLocalNodeName(String localNodeName) {
    this.localNodeName = localNodeName;
  }

  
  /**
   * @param authorizationRequestManager
   */
  public void setAuthorizationRequestManager(IAuthorizationRequestManager authorizationRequestManager) {
    this.authorizationRequestManager = authorizationRequestManager;
  }

  /**
   * @see ISecurityManager#isInjector(String)
   */
  @Override
  public boolean isInjector(String nodeName) {
    try {
      Authorization auth = authorizationManager.getByNodeName(nodeName);
      if (auth != null) {
        return auth.isAuthorized() && auth.isInjector();
      }
    } catch (Exception e) {
      logger.debug("Exception in isInjector", e);
    }
    return false;
  }

  /**
   * Creates specific message to be used for signing
   * @see ISecurityManager#createSignatureMessage(HttpUriRequest)
   */
  static final String[] SIGNING_HEADERS = {"Content-Type", "Content-MD5", "Date"};
  @Override
  public String createSignatureMessage(HttpUriRequest request) {
    List<String> result = new ArrayList<String>();
    result.add(request.getMethod());
    result.add(request.getURI().toString());
    for (String headerName : SIGNING_HEADERS) {
      Header header = request.getFirstHeader(headerName);
      if (header != null) {
        String headerValue = header.getValue();
        headerValue = StringUtils.strip(headerValue);
        result.add(headerValue);
      }
    }
    return StringUtils.join(result, '\n');
  }
}
