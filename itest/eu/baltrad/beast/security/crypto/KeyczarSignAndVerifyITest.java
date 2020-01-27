package eu.baltrad.beast.security.crypto;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.springframework.jdbc.core.JdbcOperations;

import eu.baltrad.beast.itest.BeastDBTestHelper;
import eu.baltrad.beast.security.SecurityManager;
import eu.baltrad.beast.security.keyczar.KeyCompressor;
import junit.framework.TestCase;

public class KeyczarSignAndVerifyITest extends TestCase {
  // Example private key
  private final static String PRIVATE_KEYCZAR_KEY = 
      "UEsDBBQACAAIAGlyGk8AAAAAAAAAAJECAAABABwAMVVUCQADRc5jXdiWZ111eAsAAQToAwAABOgD" + 
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
  
  // Example private key
  private static String OTHER_PRIVATE_KEYCZAR_KEY =
      "UEsDBBQACAAIAMZ7Gk8AAAAAAAAAAJECAAABABwAMVVUCQAD5N5jXRzfY111eAsAAQToAwAABOgD" + 
      "AABdkUuPokAURv9Kh/WYIJSIsysUildRCshrY7rkLSiCPDvz36ftzGru4iY3OV9ybr4vpulpVVyN" + 
      "ZGZ+f3wxzfdmoAb/jT4cPeF2D1c55YiSF12HVi+3DR9F61RqLVs28qC8e26T9JY/nIccjEKT+pZ9" + 
      "4HEuuC2tdzIxEI2mloKqNlX+PCimGma+vhm5Ddpnq4XkUGE7bkAa0IbINpQnuVp9sBY+u84kqE6N" + 
      "VIBt2NL0VG1GojvyYaU7anlqkiNsmF8fzPNH2qpPNTxo68vcO2m1EyE2V+5lJvud9IayN6SjXoY3" + 
      "28ir5C7mRH7JHuffzwk1s/z+bCx/7uFAex/iWNYWP3noXUmPRIlKCDVE4TgUNXcweX8qJOvu4ikP" + 
      "j7V7y/bAf17QA7vZtjljzE+GaMQv4Soaut7pWzRQt8dQZ2P3ZS5uprgXpQMXTdq4+SIlsTb7C4fu" + 
      "cc6TU7BrzrxK2bfzuxPGGdsRaHPG7ngQUX51rBDrtgYmcnIuPTsJQefZ0myQc7n3nWg9cAcAcQtK" + 
      "XFiVkKsiqLLqBOM9zLav5sYiDU7HzcnSDwumqtQTu3t4ygO4ZRFrhjVdOfBZR8FEYm+ReVmxLWQp" + 
      "o+2OOhgBFrZptBbpYHBxFSDc2iyJuE5+htLVEN/OXbEk39prlgN/vs/p5wWfLbLANqqYrClN82Az" + 
      "j3tYKmF8/T/yF1BLBwheiILoCQIAAJECAABQSwMEFAAIAAgAxnsaTwAAAAAAAAAArAAAAAQAHABt" + 
      "ZXRhVVQJAAPk3mNdHN9jXXV4CwABBOgDAAAE6AMAADWMywrCMBQFf6XctS7cugtUJQuDpFAQkZDW" + 
      "Kwh5kUdpKf13k0CXZ+YwKxipEc4N4Cy1U3gc0Uc4NOCSdzZU1dEbE4S1or9wen0WGxdXVdsR8eC0" + 
      "LwzN6BcX8ZPFV6qAmU3ow8+akNFr3RdLekCf0Sk/QpQxFQ+5cye85nF21kc5KNxb23v7A1BLBwgy" + 
      "w4+jhwAAAKwAAABQSwECHgMUAAgACADGexpPXoiC6AkCAACRAgAAAQAYAAAAAAABAAAApIEAAAAA" + 
      "MVVUBQAD5N5jXXV4CwABBOgDAAAE6AMAAFBLAQIeAxQACAAIAMZ7Gk8yw4+jhwAAAKwAAAAEABgA" + 
      "AAAAAAEAAACkgVQCAABtZXRhVVQFAAPk3mNddXgLAAEE6AMAAAToAwAAUEsFBgAAAAACAAIAkQAA" + 
      "ACkDAAAAAA==";
  
  // Example public key
  private static String OTHER_PUBLIC_KEYCZAR_KEY = 
      "UEsDBBQACAAIAOR7Gk8AAAAAAAAAAFACAAABABwAMVVUCQADHN9jXRzdZ111eAsAAQToAwAABOgD" + 
      "AAAtzsuymjAAgOFXOeO6ziBExO6CQriFKEQQNs6J3AVBkGun797a6b/+F9+vVbP6+bWCOvyfMZw8" + 
      "8fEM1hnjiZrlXYfWb9oGdd66pVYptoM8qOxfuzh5ZLVbK9dRbBLfdo4CzkTasmqvEBOxcGoZKCtL" + 
      "Ey6DamlB6hvbkd+iQ7peSAZVruMHpAN9CB1TfZG73V834nfXWQRViZmIsA1alpzL7UgMVzmuDVcr" + 
      "zk18gs3qx9fq9Q9tV+cKHvXNbe7dpNxLEFtrepvJYS9/pvQzGahX4MMxszJ+ShlR3orH+89LzKw0" + 
      "e74a2597OLDehzhS9MWPa6Mr2ImoYQGhjhgch7zij5bgT7lsPymesuBU0Ud6AP7rhmpM011zwViY" + 
      "TMmM3uJdMg2jM3ZoYLTH0OAi+rYWmqr0pnbgpstbmi1yHOmzv/DoGWUCOV/3zUXQGPcxzx+zO7Yj" + 
      "0OeU2wsgZML6VCKOtiYmSnwpPCcOQOc58mySS3Hw3XAz8EcAcQsKnNulmGkSKNPyDKMDTHfv5sEh" + 
      "HU6n7dk2jgtmmtwTp6s9tQa0yCPdtKc7D76r8DqRyFsUQVEdG9nq6NDRACPA4i4JNxIbTD4qrwi3" + 
      "DkdCvlNegXw3pY+5y5f4L3vD8eD3H1BLBwiffkKV3AEAAFACAABQSwMEFAAIAAgA5HsaTwAAAAAA" + 
      "AAAAogAAAAQAHABtZXRhVVQJAAMc32NdHN1nXXV4CwABBOgDAAAE6AMAADXMzQrCMBAE4Fcpe9aD" + 
      "V28VFXpQSkRBRCStKwj5WTYbaSl9d5NAj/PNMBM4bRG2FeCgLRlc98gCqwooMvlQqttBNcd7Rhmp" + 
      "yP5Sv9rrLhO6nkcSfCf/aBMw2Q85fL0LiR7Tks7RdsiJNmkRREvMPbSqOdWqvONAnkV3Bpev+Tn/" + 
      "AVBLBwh1wi0ogQAAAKIAAABQSwECHgMUAAgACADkexpPn35CldwBAABQAgAAAQAYAAAAAAABAAAA" + 
      "pIEAAAAAMVVUBQADHN9jXXV4CwABBOgDAAAE6AMAAFBLAQIeAxQACAAIAOR7Gk91wi0ogQAAAKIA" + 
      "AAAEABgAAAAAAAEAAACkgScCAABtZXRhVVQFAAMc32NddXgLAAEE6AMAAAToAwAAUEsFBgAAAAAC" + 
      "AAIAkQAAAPYCAAAAAA==";
  
  private CryptoFactory factory = null;
  private Path keyczarRootPath = null;
  
  public void setUp() throws Exception {
    keyczarRootPath = Files.createTempDirectory("beast_keyczarsignandverify");
    new KeyCompressor().unzip(new File(keyczarRootPath.toFile(), "one.priv"), Base64.getDecoder().decode(PRIVATE_KEYCZAR_KEY));
    new KeyCompressor().unzip(new File(keyczarRootPath.toFile(), "one.pub"), Base64.getDecoder().decode(PUBLIC_KEYCZAR_KEY));
    new KeyCompressor().unzip(new File(keyczarRootPath.toFile(), "other.priv"), Base64.getDecoder().decode(OTHER_PRIVATE_KEYCZAR_KEY));
    new KeyCompressor().unzip(new File(keyczarRootPath.toFile(), "other.pub"), Base64.getDecoder().decode(OTHER_PUBLIC_KEYCZAR_KEY));
    factory = new KeyczarCryptoFactory(keyczarRootPath.toFile());
  }
  
  public void tearDown() throws Exception {
    removeRecursiveDir(keyczarRootPath.toFile());
  }
  
  protected void removeRecursiveDir(File path) {
    if (path != null && path.exists()) {
      File[] files = path.listFiles();
      for (File f: files) {
        if (f.getName().equals(".") || f.getName().equals("..")) {
          continue;
        }
        if (f.isDirectory()) {
          removeRecursiveDir(f);
        } else {
          f.delete();
        }
      }
      path.delete();
    }
  }
  
  public void testSignVerify_OK() throws Exception {
    String signature = factory.createSigner("one").sign("this is a test message");
    assertEquals(true, factory.createVerifier("one").verify("this is a test message", signature));
  }
  
  public void testSignVerify_NOK_badSignature() throws Exception {
    String signature = factory.createSigner("one").sign("this is a test message");
    assertEquals(false, factory.createVerifier("one").verify("this is a test message xyz", signature));
  }
  
  public void testSignVerify_OK_other() throws Exception {
    String signature = factory.createSigner("other").sign("this is a test message");
    assertEquals(true, factory.createVerifier("other").verify("this is a test message", signature));
  }

  public void testSignVerify_NOK_other_badSignature() throws Exception {
    String signature = factory.createSigner("other").sign("this is a test message");
    assertEquals(false, factory.createVerifier("other").verify("this is a test", signature));
  }

  public void testSignVerify_NOK() throws Exception {
    String signature = factory.createSigner("other").sign("this is a test message");
    assertEquals(false, factory.createVerifier("one").verify("this is a test message", signature));
  }

  public void testSignVerify_NOK_other() throws Exception {
    String signature = factory.createSigner("one").sign("this is a test message");
    assertEquals(false, factory.createVerifier("other").verify("this is a test message", signature));
  }
}
