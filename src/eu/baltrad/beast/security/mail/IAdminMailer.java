package eu.baltrad.beast.security.mail;

import eu.baltrad.beast.security.AuthorizationRequest;

/**
 * Mailer for sending administrative emails
 * @author anders
 */
public interface IAdminMailer {
  /**
   * Send mail when a key approval request has arrived
   * @param to the recipient of the mail
   * @param subject the subject
   * @param uri the uri to be shown in the mail
   * @param message the message from the requestor
   * @param request the actual authorization request
   */
  public void sendKeyApprovalRequest(String to, String subject, String uri, String message, AuthorizationRequest request);
}
