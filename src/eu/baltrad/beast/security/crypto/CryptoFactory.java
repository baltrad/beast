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

import org.keyczar.exceptions.KeyczarException;

/**
 * Interface for a factory providing signer & verifier. Currently only supports Keyczar. 
 * @author anders
 */
public interface CryptoFactory {
  /**
   * Create a Signer instance identified by name
   *
   * @param name Factory specific name for the signer (most likely key
   *             lookup)
   * @throws KeyczarException
   */
  public Signer createSigner(String keyName) throws KeyczarException;
  
  /**
   * Create a Verifier instance identified by name
   *
   * @param name Factory specific name for the verifier (most likely key lookup)
   * @throws KeyczarException
   */
  public Verifier createVerifier(String alias) throws KeyczarException;
}
