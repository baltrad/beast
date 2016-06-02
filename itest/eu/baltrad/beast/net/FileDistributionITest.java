package eu.baltrad.beast.net;

import java.io.File;
import java.net.URI;

public class FileDistributionITest {
  public static void main(String[] args) throws Exception {
    FileDistribution fileDistribution = new FileDistribution(new File(args[0]), new URI(args[1]), args[2]);
    fileDistribution.run();
  }
}
