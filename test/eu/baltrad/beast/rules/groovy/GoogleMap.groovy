package eu.baltrad.beast.rules.groovy;

import eu.baltrad.beast.ManagerContext;
import eu.baltrad.beast.db.Catalog;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltDataMessage;
import eu.baltrad.beast.message.mo.BltGenerateMessage;
import eu.baltrad.beast.rules.IScriptableRule;
import eu.baltrad.fc.FileEntry;
import eu.baltrad.fc.Oh5File;
import eu.baltrad.fc.Date;
import eu.baltrad.fc.Time;

/**
 * Rule for forwarding a specific area to the google map
 * generator plugin.
 *
 * @author Anders Henja
 */
class GenerateGoogleMap implements IScriptableRule {
  /**
   * The compositing areas
   */
  public static String[] AREAS=["swegmaps_2000", "baltrad_2000"];
  
  /**
   * The location where the generator plugin should store the .png
   * file.
   */
  public static String PATH="/opt/baltrad/rave_gmap/web/data";
  
  /**
   * Returns the supported area if it exists
   * @param source the source string
   * @return the found area or null if not found
   */
  protected String getSupportedArea(String source) {
    for (String s : AREAS) {
      if (source.indexOf(s)>=0) {
        return s;
      }
    }
    return null;
  }
  
  /**
   * Returns a BltGenerateMessage if the message is a BltDataMessage
   * containing a composite for the AREA.
   * @param msg the message
   * @return a BltGenerateMessage for generating the google map png image
   */
  @Override
  public IBltMessage handle(IBltMessage msg) {
    BltGenerateMessage result = null;
    Catalog cat = ManagerContext.getCatalog();
    if (msg != null && msg instanceof BltDataMessage) {
      FileEntry fe = ((BltDataMessage)msg).getFileEntry();
      String object = fe.what_object();
      if (object != null && object.equals("COMP")) {
        String source = fe.what_source();
        Date d = fe.what_date();
        Time t = fe.what_time();
        String area = getSupportedArea(source);
        if (area != null) {
          String oname = PATH + "/"+area+"/" + sprintf("%04d/%02d/%02d/%04d%02d%02d%02d%02d", [d.year(), d.month(), d.day(), d.year(), d.month(), d.day(), t.hour(), t.minute()] as int[]) + ".png";
          result = new BltGenerateMessage();
          result.setAlgorithm("se.smhi.rave.creategmapimage");
          result.setFiles([cat.getFileCatalogPath(fe.uuid())] as String[]);
          result.setArguments(["outfile",oname] as String[]);
        }
      }
    }
    return result;
  }
}
