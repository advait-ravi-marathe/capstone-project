package coverstory.platform.crypto;

import java.security.GeneralSecurityException;

/** Authenticated encryption for consumer fields. Associated data should identify the row and field. */
public interface FieldCipher {
    EncryptedField encrypt(String plaintext, byte[] associatedData) throws GeneralSecurityException;
    String decrypt(EncryptedField encrypted, byte[] associatedData) throws GeneralSecurityException;
}
