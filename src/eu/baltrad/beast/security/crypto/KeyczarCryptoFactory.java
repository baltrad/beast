/*
Copyright 2012 Estonian Meteorological and Hydrological Institute

This file is part of BaltradFrame.

BaltradFrame is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

BaltradFrame is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with BaltradFrame. If not, see <http://www.gnu.org/licenses/>.
*/

package eu.baltrad.beast.security.crypto;

import java.io.*;
import org.keyczar.exceptions.KeyczarException;

/**
 * Construct Keyczar signers and verifiers.
 * 
 * It assumes a certain structure from your keystore:
 *  - private keys must be stored in $keyStoreRoot/$name.priv
 *  - public keys must be stored in $keyStoreRoot/$name.pub
 */
public class KeyczarCryptoFactory implements CryptoFactory {
  private File keyStoreRoot;
  
  /**
   * Constructor.
   *
   * @param keyStoreRoot - root directory of the keystore
   */
  public KeyczarCryptoFactory(File keyStoreRoot) {
    this.keyStoreRoot = keyStoreRoot;
  }

  /**
   * Create a signer using key from $keyStoreRoot/$name.priv
   * @throws KeyczarException
   */
  @Override
  public Signer createSigner(String name) throws KeyczarException {
    File keyLocation = getKeyLocation(name + ".priv");
    return new KeyczarSigner(keyLocation.toString());
  }

  /**
   * Create a signer using key from $keyStoreRoot/$name.pub
   * @throws KeyczarException
   */
  @Override
  public Verifier createVerifier(String name) throws KeyczarException {
    File keyLocation = getKeyLocation(name + ".pub");
    return new KeyczarVerifier(keyLocation.toString());
  }

  /**
   * @param name the name of the key without path to keystore
   * @return the file
   */
  protected File getKeyLocation(String name) {
    return new File(keyStoreRoot, name);
  }
}
