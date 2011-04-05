package eu.baltrad.beast.net;

import java.io.File;
import java.net.URI;

public class FileUploaderITest {
  public static void main(String[] args) throws Exception {
    FileUploader u = FileUploader.createDefault();
    u.upload(new File(args[0]), new URI(args[1]));
  }
}
