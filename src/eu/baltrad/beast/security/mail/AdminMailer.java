package eu.baltrad.beast.security.mail;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import eu.baltrad.beast.security.AuthorizationRequest;

/**
 * Implements the admin mailer
 * @author anders
 */
public class AdminMailer implements IAdminMailer {
  /**
   * Sender
   */
  private JavaMailSender sender = null;
  
  /**
   * Default encoding
   */
  private String defaultEncoding = null;
  
  /**
   * mail server host address
   */
  private String host = null;
  
  /**
   * port number of the mail server
   */
  private int port = 0;
  
  /**
   * User name at mailserver
   */
  private String username = null;
  
  /**
   * Password for user at mailserver
   */
  private String password = null;
  
  /**
   * Name of sender (from).
   */
  private String from = null;
  
  /**
   * If mailing should be enabled or not
   */
  private boolean enabled = false;
  
  /**
   * Different properties that should be used in the connection with the mail server
   */
  private Map<String, String> properties = new HashMap<String, String>();
  
  /**
   * Sets the mail sender
   * @param sender the sender
   */
  @Autowired
  public void setJavaMailSender(JavaMailSender sender) {
    this.sender = sender;
  }
  
  /**
   * Creates a mail sender to be used.
   * @return the mail sender
   */
  protected JavaMailSender getMailSender() {
    JavaMailSender mailSender = sender;
    if (mailSender == null) {
      JavaMailSenderImpl mailSenderImpl = new JavaMailSenderImpl();
      if (defaultEncoding != null && !defaultEncoding.equals("")) {
        mailSenderImpl.setDefaultEncoding(defaultEncoding);
      }
      if (host != null && !host.equals("")) {
        mailSenderImpl.setHost(host);
      }
      if (port > 0) {
        mailSenderImpl.setPort(port);
      }
      mailSenderImpl.setUsername(username);
      mailSenderImpl.setPassword(password);
      for (String key: properties.keySet()) {
        mailSenderImpl.getJavaMailProperties().put(key, properties.get(key));
      }
      mailSender = mailSenderImpl;
    }
    return mailSender;
  }
  
  /**
   * Send a mail with a key approval request
   * @param to destination email address
   * @param subject the subject of the email
   * @param message an arbitrary message
   * @request the actual request providing content in the email
   */
  @Autowired
  @Override
  public void sendKeyApprovalRequest(String to, String subject, String uri, String message, AuthorizationRequest request) {
    if (!isEnabled())
      return;
    JavaMailSender sender = getMailSender();

    SimpleMailMessage mail = new SimpleMailMessage();
    mail.setTo(to);
    if (from != null && !from.equals(""))
      mail.setFrom(from);
    else
      mail.setFrom(to);
    mail.setSubject("Key approval request received from " + request.getNodeName() + " with IP " + request.getRemoteHost() + ".");
    
    mail.setText(
        "Key approval request received from " + request.getNodeName() + " with IP " + request.getRemoteHost() + ".\n" +
        "Information\n" +
        "Node name:  " + request.getNodeName() + "\n" +
        "Node email: " + request.getNodeEmail() + "\n" +
        "Remote host: " + request.getRemoteHost() + "\n" +
        "Received at: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:SS").format(request.getReceivedAt()) +  "\n" +
        "Message: \n" + message + "\n\n" +
        "Base encoded public key: \n" +
        Base64.getEncoder().encodeToString(request.getPublicKey()) +
        "\n\n" + 
        "If you believe this should be accepted, please click here " + uri);
    
    sender.send(mail);
  }

  /**
   * @return default encoding
   */
  public String getDefaultEncoding() {
    return defaultEncoding;
  }

  /**
   * @param defaultEncoding the default encoding
   */
  public void setDefaultEncoding(String defaultEncoding) {
    this.defaultEncoding = defaultEncoding;
  }

  /**
   * @return the mail server host address
   */
  public String getHost() {
    return host;
  }

  /**
   * @param host the mail server host address
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * @return port of the mail server
   */
  public int getPort() {
    return port;
  }

  /**
   * @param port of the mail server
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * @return username on mail server
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username username on mail server
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * @return password on mail server
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password on mail server
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * @return properties used when establishing connection with mail server
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * @param properties  used when establishing connection with mail server
   */
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  /**
   * @return from address
   */
  public String getFrom() {
    return from;
  }

  /**
   * @param from address
   */
  public void setFrom(String from) {
    this.from = from;
  }

  /**
   * @return the enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * @param enabled the enabled to set
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
