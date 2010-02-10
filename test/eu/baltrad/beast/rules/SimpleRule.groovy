import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.rules.IScriptableRule;
import eu.baltrad.beast.message.mo.BltAlertMessage;
import eu.baltrad.beast.message.mo.BltCommandMessage;

public class SimpleRule implements IScriptableRule {
  IBltMessage handle(IBltMessage message) {
    BltAlertMessage result = null;
    if (message instanceof BltCommandMessage) {
      String cmd = ((BltCommandMessage)message).getCommand();
      result = new BltAlertMessage();
      result.setCode("A000");
      result.setMessage(cmd);
    }
    return result;
  }
}
