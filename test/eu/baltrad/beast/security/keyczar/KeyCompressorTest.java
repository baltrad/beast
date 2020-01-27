package eu.baltrad.beast.security.keyczar;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class KeyCompressorTest  extends EasyMockSupport {
  // Example private key
  private static String PRIVATE_KEYCZAR_KEY = "UEsDBBQACAAIAGlyGk8AAAAAAAAAAJECAAABABwAMVVUCQADRc5jXdiWZ111eAsAAQToAwAABOgD" + 
      "AABdkUmvolAUhP/KC+s2wcug9I55Ehkug7AhIjKPgsjlpf9760t602dRyUmqkq9S39jwTJrypt8R" + 
      "9vvrGxveirEq+++OhFwRgHodDIKci/45zw4o+Z2v1XETzpVngpTuGU6PVp7pc4jzouF0j4th+0gh" + 
      "6JNpBlTraNrIRSJOHKoxFmafG33DX6w0iOtT9gwJ3IgkfWfsOsbnA6LSNg6+2goJUKqPEb3TsgA8" + 
      "ioa9gHktfVJUG6omLDmhA9hmG/brCxt/oPWOSlw02nO/N/FTDkRBX8Q9AJL7Y8o/JrFAB76I42rP" + 
      "Rbtzokj6RAf8me1oiJYrWaSGX0OVjzrDsXhzCjpzEJjBDKwlBICPVAaqSaXikizLdXuq0eDED+M0" + 
      "49fsZrPvAt3Bq6rlXg6IVFPrDB5zUaTdyjbM69mGXPbKJ9yFQ5gOZysXiLunueEqJBJXalwYR/dY" + 
      "YlhylSCipw/zZxPMxaf2QjuO0d6mA5Nl1yWBr4VRiEJiqpYRrYjmTt4+fXX85uIjHymQkp9Hsigz" + 
      "WRrsuToL9QX0E4MipZ5NHa/SY2CfhwscBrg5k0AhchujMOncTfNFLdEsOaMQz/UeqKNnZk/gSTQC" + 
      "rWS6FhKuX1gcfkC9YnJbb+p12MjOBjPaSRjvwzyV2/2NvccB+ef9rp8KXolriB7yex50lz2/aCt7" + 
      "s0S7Karj/5G/UEsHCLY2wjYIAgAAkQIAAFBLAwQUAAgACABpchpPAAAAAAAAAACrAAAABAAcAG1l" + 
      "dGFVVAkAA0XOY13YlmdddXgLAAEE6AMAAAToAwAANYzLCsIwEEV/pcxaF27dBaqShUFSKIhISO0I" + 
      "QpuEPKS19N+dBLq851zOAkaPCMcKtOnRh735TRF2FbjknQ3FNPwiFBO1ak+Sn+/ZxtkVVTdM3SRv" + 
      "M0Pz8rOL2JN46yEgsS8lP9YEQo9lWyKNHXpCB3qEqGPKHqhzZbLkcXLWR90NuLXW5/oHUEsHCHS5" + 
      "JniHAAAAqwAAAFBLAQIeAxQACAAIAGlyGk+2NsI2CAIAAJECAAABABgAAAAAAAEAAACkgQAAAAAx" + 
      "VVQFAANFzmNddXgLAAEE6AMAAAToAwAAUEsBAh4DFAAIAAgAaXIaT3S5JniHAAAAqwAAAAQAGAAA" + 
      "AAAAAQAAAKSBUwIAAG1ldGFVVAUAA0XOY111eAsAAQToAwAABOgDAABQSwUGAAAAAAIAAgCRAAAA" + 
      "KAMAAAAA";
  
  // Example public key
  private static String PUBLIC_KEYCZAR_KEY = "UEsDBBQACAAIAGlyGk8AAAAAAAAAAFACAAABABwAMVVUCQADRc5jXb46ZV11eAsAAQToAwAABOgD" + 
      "AAA1z0mPokAAhuG/0vE8Jlhs1tzYN5GlWKQupGlE9kUQLSbz36ftZL7ze3i+P7tx9/tjJxjC/x1p" + 
      "raYB++RtmlnK4bEsPqikfWQ2aZssdeiAnBugaOGXBIcboiTF9vv7xfYiotPcyXFitvNNcxKxQtF8" + 
      "PaXyEolTZEerm8dpcyoeCU3ZWLX29r6HkRTTtbmJ6NnVREZqc8Tc3ixicC9b4QKWVxUxitGyDe1q" + 
      "GRejrth2vz520w/a6tksIJO3DAeHOt2AIlurcgBADX6i2ztSSsJLZZrWBxHvz5muWjMXS2eh5xBZ" + 
      "P5kyt6MGGRLubd+VnDnunVGGoxO7awKAhA2IjKw2KFXTtKY7NWT007t9WqjP4ssTvg/0fFjX67Ua" + 
      "CWPk7hncl7LM+5fQwuejS8TieZupAI1JPp7dm0xfQzNIXnKmipUpJim+pioUmJeKCDe/zeRtDqi5" + 
      "u3C+b3dfMw+L4nPN0HOFOl2qsO6g4mJOPIWH/NlLW0BNEtYRqz2OTFkVmjp6S32WmwsYZkiw3iyO" + 
      "RdX5MfbO4wWNI9r8WWYJs004yfpgMyPFzExXK1giiUMIGvwovBk86Fbm9MIyEzqISlekeDLojrgN" + 
      "jtUkreZvqOD8DIZv81xt12/2gQLM339QSwcIRT2X6t0BAABQAgAAUEsDBBQACAAIAGlyGk8AAAAA" + 
      "AAAAAKEAAAAEABwAbWV0YVVUCQADRc5jXb46ZV11eAsAAQToAwAABOgDAACrVspLzE1VslJQSsxL" + 
      "SS0q1s2rqihR0lFQKigtKsgvBsuEuQZ5ukWCBEsqC8AiLsGO8QGhTiCh1LzkosqCktQUoHhaYk5x" + 
      "KlCsDGhQZn5eMVAouhrG8yvNTUotAgoZAlUUlySWlILklQKCPH0dg8Cmp1YU5BeVJCblpMLMqo2t" + 
      "BQBQSwcI6EvZF4EAAAChAAAAUEsBAh4DFAAIAAgAaXIaT0U9l+rdAQAAUAIAAAEAGAAAAAAAAQAA" + 
      "AKSBAAAAADFVVAUAA0XOY111eAsAAQToAwAABOgDAABQSwECHgMUAAgACABpchpP6EvZF4EAAACh" + 
      "AAAABAAYAAAAAAABAAAApIEoAgAAbWV0YVVUBQADRc5jXXV4CwABBOgDAAAE6AMAAFBLBQYAAAAA" + 
      "AgACAJEAAAD3AgAAAAA=";

  private Path tempKeyczarPath = null;

  private KeyCompressor classUnderTest = null;
  
  @Before
  public void setUp() throws Exception {
    tempKeyczarPath = Files.createTempDirectory("beast_keycompressortest");
    classUnderTest = new KeyCompressor();
  }

  @After
  public void tearDown() throws Exception {
    removeFiles(tempKeyczarPath);
    classUnderTest = null;
  }
  
  protected void removeFiles(Path path) {
    if (path != null && path.toFile().exists()) {
      String[] entries = path.toFile().list();
      for(String s: entries){
        File currentFile = new File(path.toFile().getPath(), s);
        currentFile.delete();
      }
      path.toFile().delete();
    }
    
  }
  
  @Test
  public void unzip_publicKey() throws Exception {
    classUnderTest.unzip(tempKeyczarPath.toFile(), Base64.getDecoder().decode(PUBLIC_KEYCZAR_KEY));
    
    File file1 = new File(tempKeyczarPath.toFile(), "1");
    File fileMeta = new File(tempKeyczarPath.toFile(), "meta");
    
    assertEquals(true, file1.exists());
    assertEquals(true, fileMeta.exists());
    
    String file1data = FileUtils.readFileToString(file1);
    String fileMetaData = FileUtils.readFileToString(fileMeta);

    assertEquals("{\"p\": \"AIAAAAAAAAA83Gj325w7M34thouttR2iC-VJk_lYtjUO2d6o9BKZxC9ogS0CEMRnrXMQVyH36LOOW5mRJJqBZE037jq_DtVBqVMVvPdW_kLfuY30MZFK-M-n9VCW3jJzBSwmjyDSFk8Z6-JfW2rhlAX2txiV4EIl5k3PGb6WSmfz\", \"q\": \"AKn5" + 
        "bTyqQto1O0Lg2EDKvE122FTz\", \"g\": \"Ehy7Ch__j1BZ-NbHFKs6WCNAn6Syva4hdMVkSICZnMRPCOsWnOpD9pOWPvY22CZI9SIbjI0FGGGkmLkypR_rMLt0afcQA-n9n7Ujjveipy4IdPN2rthhdnxAl9wumYBfwgs0TSpYdpNPgD3eUJTYxDbFBiJBY_Z" + 
        "e_F9A4xFSy6s\", \"y\": \"T0smX6RRMmcs79ffavbSwv9H3hF9jm9EPZ6BLU1dwnCzT0qCZHS5Gu84hifGFpQtjNDkX2os9yZHktOK0jd8WQNpXSppSzRsD5y4zqZYbnTzJVEJbJPGf5yCBoU2kZufQs2u3lD6HfKJY3TVhPB07yoHOBzoOKkYlGRzSf6Rb9U" + 
        "\", \"size\": 1024}", file1data);
    assertEquals("{\"name\": \"anders-nzxt\", \"purpose\": \"VERIFY\", \"type\": \"DSA_PUB\", \"encrypted\": false, \"versions\": [{\"versionNumber\": 1, \"status\": \"PRIMARY\", \"exportable\": false}]}", fileMetaData);
  }
  
  @Test
  public void zip_publicKey() throws Exception {
    // First prepare folder to zip
    classUnderTest.unzip(tempKeyczarPath.toFile(), Base64.getDecoder().decode(PUBLIC_KEYCZAR_KEY));
    
    // Zip the folder to get a base encoding
    byte[] data = classUnderTest.zip(tempKeyczarPath.toFile());
    String result = Base64.getEncoder().encodeToString(data);

    // remove folder so that we can use it for the unzip
    removeFiles(tempKeyczarPath);
    
    classUnderTest.unzip(tempKeyczarPath.toFile(), data);

    File file1 = new File(tempKeyczarPath.toFile(), "1");
    File fileMeta = new File(tempKeyczarPath.toFile(), "meta");
    
    assertEquals(true, file1.exists());
    assertEquals(true, fileMeta.exists());
    
    String file1data = FileUtils.readFileToString(file1);
    String fileMetaData = FileUtils.readFileToString(fileMeta);

    assertEquals("{\"p\": \"AIAAAAAAAAA83Gj325w7M34thouttR2iC-VJk_lYtjUO2d6o9BKZxC9ogS0CEMRnrXMQVyH36LOOW5mRJJqBZE037jq_DtVBqVMVvPdW_kLfuY30MZFK-M-n9VCW3jJzBSwmjyDSFk8Z6-JfW2rhlAX2txiV4EIl5k3PGb6WSmfz\", \"q\": \"AKn5" + 
        "bTyqQto1O0Lg2EDKvE122FTz\", \"g\": \"Ehy7Ch__j1BZ-NbHFKs6WCNAn6Syva4hdMVkSICZnMRPCOsWnOpD9pOWPvY22CZI9SIbjI0FGGGkmLkypR_rMLt0afcQA-n9n7Ujjveipy4IdPN2rthhdnxAl9wumYBfwgs0TSpYdpNPgD3eUJTYxDbFBiJBY_Z" + 
        "e_F9A4xFSy6s\", \"y\": \"T0smX6RRMmcs79ffavbSwv9H3hF9jm9EPZ6BLU1dwnCzT0qCZHS5Gu84hifGFpQtjNDkX2os9yZHktOK0jd8WQNpXSppSzRsD5y4zqZYbnTzJVEJbJPGf5yCBoU2kZufQs2u3lD6HfKJY3TVhPB07yoHOBzoOKkYlGRzSf6Rb9U" + 
        "\", \"size\": 1024}", file1data);
    assertEquals("{\"name\": \"anders-nzxt\", \"purpose\": \"VERIFY\", \"type\": \"DSA_PUB\", \"encrypted\": false, \"versions\": [{\"versionNumber\": 1, \"status\": \"PRIMARY\", \"exportable\": false}]}", fileMetaData);

    // We can't compare Base64 encodings since date times will differ on extracted files
  }

  
  @Test
  public void unzip_privateKey() throws Exception {
    classUnderTest.unzip(tempKeyczarPath.toFile(), Base64.getDecoder().decode(PRIVATE_KEYCZAR_KEY));
    
    File file1 = new File(tempKeyczarPath.toFile(), "1");
    File fileMeta = new File(tempKeyczarPath.toFile(), "meta");
    
    assertEquals(true, file1.exists());
    assertEquals(true, fileMeta.exists());
    
    String file1data = FileUtils.readFileToString(file1);
    String fileMetaData = FileUtils.readFileToString(fileMeta);

    assertEquals("{\"publicKey\": {\"p\": \"AIAAAAAAAAA83Gj325w7M34thouttR2iC-VJk_lYtjUO2d6o9BKZxC9ogS0CEMRnrXMQVyH36LOOW5mRJJqBZE037jq_DtVBqVMVvPdW_kLfuY30MZFK-M-n9VCW3jJzBSwmjyDSFk8Z6-JfW2rhlAX2txiV4EIl5k3PGb6WSmfz\", \"q\": \"AKn5bTyqQto1O0Lg2EDKvE122FTz\", \"g\": \"Ehy7Ch__j1BZ-NbHFKs6WCNAn6Syva4hdMVkSICZnMRPCOsWnOpD9pOWPvY22CZI9SIbjI0FGGGkmLkypR_rMLt0afcQA-n9n7Ujjveipy4IdPN2rthhdnxAl9wumYBfwgs0TSpYdpNPgD3eUJTYxDbFBiJBY_Ze_F9A4xFSy6s\", \"y\": \"T0smX6RRMmcs79ffavbSwv9H3hF9jm9EPZ6BLU1dwnCzT0qCZHS5Gu84hifGFpQtjNDkX2os9yZHktOK0jd8WQNpXSppSzRsD5y4zqZYbnTzJVEJbJPGf5yCBoU2kZufQs2u3lD6HfKJY3TVhPB07yoHOBzoOKkYlGRzSf6Rb9U\", \"size\": 1024}, \"x\": \"Ui0Jy6pgegWnX1CvJxAcPEQlhj8\", \"size\": 1024}", file1data);
    assertEquals("{\"name\": \"anders-nzxt\", \"purpose\": \"SIGN_AND_VERIFY\", \"type\": \"DSA_PRIV\", \"encrypted\": false, \"versions\": [{\"versionNumber\": 1, \"status\": \"PRIMARY\", \"exportable\": false}]}", fileMetaData);
  }
  
  @Test
  public void zip_privateKey() throws Exception {
    // First prepare folder to zip
    classUnderTest.unzip(tempKeyczarPath.toFile(), Base64.getDecoder().decode(PRIVATE_KEYCZAR_KEY));
    
    // Zip the folder to get a base encoding
    byte[] data = classUnderTest.zip(tempKeyczarPath.toFile());
    String result = Base64.getEncoder().encodeToString(data);

    // remove folder so that we can use it for the unzip
    removeFiles(tempKeyczarPath);
    
    classUnderTest.unzip(tempKeyczarPath.toFile(), data);

    File file1 = new File(tempKeyczarPath.toFile(), "1");
    File fileMeta = new File(tempKeyczarPath.toFile(), "meta");
    
    assertEquals(true, file1.exists());
    assertEquals(true, fileMeta.exists());
    
    String file1data = FileUtils.readFileToString(file1);
    String fileMetaData = FileUtils.readFileToString(fileMeta);

    assertEquals("{\"publicKey\": {\"p\": \"AIAAAAAAAAA83Gj325w7M34thouttR2iC-VJk_lYtjUO2d6o9BKZxC9ogS0CEMRnrXMQVyH36LOOW5mRJJqBZE037jq_DtVBqVMVvPdW_kLfuY30MZFK-M-n9VCW3jJzBSwmjyDSFk8Z6-JfW2rhlAX2txiV4EIl5k3PGb6WSmfz\", \"q\": \"AKn5bTyqQto1O0Lg2EDKvE122FTz\", \"g\": \"Ehy7Ch__j1BZ-NbHFKs6WCNAn6Syva4hdMVkSICZnMRPCOsWnOpD9pOWPvY22CZI9SIbjI0FGGGkmLkypR_rMLt0afcQA-n9n7Ujjveipy4IdPN2rthhdnxAl9wumYBfwgs0TSpYdpNPgD3eUJTYxDbFBiJBY_Ze_F9A4xFSy6s\", \"y\": \"T0smX6RRMmcs79ffavbSwv9H3hF9jm9EPZ6BLU1dwnCzT0qCZHS5Gu84hifGFpQtjNDkX2os9yZHktOK0jd8WQNpXSppSzRsD5y4zqZYbnTzJVEJbJPGf5yCBoU2kZufQs2u3lD6HfKJY3TVhPB07yoHOBzoOKkYlGRzSf6Rb9U\", \"size\": 1024}, \"x\": \"Ui0Jy6pgegWnX1CvJxAcPEQlhj8\", \"size\": 1024}", file1data);
    assertEquals("{\"name\": \"anders-nzxt\", \"purpose\": \"SIGN_AND_VERIFY\", \"type\": \"DSA_PRIV\", \"encrypted\": false, \"versions\": [{\"versionNumber\": 1, \"status\": \"PRIMARY\", \"exportable\": false}]}", fileMetaData);

    // We can't compare Base64 encodings since date times will differ on extracted files
  }
  
}
