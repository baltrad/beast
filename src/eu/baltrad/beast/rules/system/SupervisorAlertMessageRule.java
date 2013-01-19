package eu.baltrad.beast.rules.system;

import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.rules.IRule;

public class SupervisorAlertMessageRule implements IRule  {
  /**
   * The type of rule
   */
  public static final String TYPE = "supervisor_system_alert";

  /**
   * @see eu.baltrad.beast.rules.IRule#handle(eu.baltrad.beast.message.IBltMessage)
   */
  @Override
  public IBltMessage handle(IBltMessage message) {
    if (message instanceof BltAlertMessage) {
      BltAlertMessage bmsg = (BltAlertMessage)message;
      
    }
    return null;
  }
  
  /**
   * @see eu.baltrad.beast.rules.IRule#getType()
   */
  @Override
  public String getType() {
    return TYPE;
  }

  /**
   * @see eu.baltrad.beast.rules.IRule#isValid()
   */
  @Override
  public boolean isValid() {
    return true;
  }
}
