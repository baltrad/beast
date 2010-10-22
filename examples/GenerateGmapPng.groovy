import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.IScriptableRule;
import eu.baltrad.fc.oh5.File;
import eu.baltrad.fc.Date;
import eu.baltrad.fc.Time;

public class GenerateGmapPng implements IScriptableRule {
  @Override
  public IBltMessage handle(IBltMessage message) {
    BltGenerateMessage result = null;
    if (message instanceof BltDataMessage) {
      File file = ((BltDataMessage)message).getFile();
      String object = file.what_object();
      String source = file.what_source();
      if (object != null && object.equals("COMP")) {
        Date d = file.what_date();
        Time t = file.what_time();
        if (source.indexOf("swegmaps_2000") >= 0) {
          String oname = "/tmp/" + sprintf("%04d%02d%02d%02d%02d%02d", [d.year(), d.month(), d.day(), t.hour(), t.minute(), t.second()] as int[]) + "_swegmaps.png";
          result = new BltGenerateMessage();
          result.setAlgorithm("se.smhi.rave.generategmapimage");
          result.setFiles([file.path()] as String[]);
          result.setArguments(["outfile",oname] as String[]);
        }
      }
    }
    return result;
  }
}

