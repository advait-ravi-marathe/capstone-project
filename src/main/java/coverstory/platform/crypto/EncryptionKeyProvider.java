package coverstory.platform.crypto;

import java.util.Optional;

/** Key separation/rotation seam. Implementations must not store key material with ciphertext. */
public interface EncryptionKeyProvider {
    EncryptionKey activeKey();
    Optional<EncryptionKey> findById(String keyId);
}
