package coverstory.platform.crypto;

import javax.crypto.SecretKey;
import java.util.Objects;

/** A key reference and key material supplied by an external key-management adapter. */
public record EncryptionKey(String keyId, SecretKey key) {
    public EncryptionKey {
        Objects.requireNonNull(keyId, "keyId");
        Objects.requireNonNull(key, "key");
        if (keyId.isBlank()) throw new IllegalArgumentException("keyId must not be blank");
    }
}
