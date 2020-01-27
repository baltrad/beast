package eu.baltrad.beast.security.crypto;

import org.keyczar.exceptions.KeyczarException;

/**
 * Interface for verifiers
 * @author anders
 */
public interface Verifier {
  /**
   * Verify a signature
   *
   * @param message message to verify
   * @param signature signature to verify
   *
   * @return True if the signature is valid
   */
  boolean verify(String message, String signature) throws KeyczarException;
}
