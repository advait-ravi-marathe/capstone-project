package coverstory.platform.crypto;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Optional;

/** Standalone instructor smoke test; uses a transient synthetic key only. */
public final class FieldCipherSmokeTest {
    public static void main(String[] args) throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        SecretKey key = generator.generateKey();
        EncryptionKey encryptionKey = new EncryptionKey("test-key-v1", key);
        EncryptionKeyProvider provider = new EncryptionKeyProvider() {
            @Override public EncryptionKey activeKey() { return encryptionKey; }
            @Override public Optional<EncryptionKey> findById(String keyId) {
                return "test-key-v1".equals(keyId) ? Optional.of(encryptionKey) : Optional.empty();
            }
        };
        FieldCipher cipher = new AesGcmFieldCipher(provider);
        byte[] aad = "synthetic-owner:email".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        EncryptedField encrypted = cipher.encrypt("learner@example.invalid", aad);
        if (!"learner@example.invalid".equals(cipher.decrypt(encrypted, aad))) {
            throw new AssertionError("cipher round trip failed");
        }
        try {
            cipher.decrypt(encrypted, "different-field".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            throw new AssertionError("associated data tampering was accepted");
        } catch (javax.crypto.AEADBadTagException expected) {
            // Expected: GCM authenticates the field context.
        }
        byte[] exposed = encrypted.ciphertext();
        exposed[0] ^= 1;
        if (!"learner@example.invalid".equals(cipher.decrypt(encrypted, aad))) {
            throw new AssertionError("ciphertext accessor leaked mutable storage");
        }
        System.out.println("Field cipher infrastructure smoke checks passed.");
    }
}
