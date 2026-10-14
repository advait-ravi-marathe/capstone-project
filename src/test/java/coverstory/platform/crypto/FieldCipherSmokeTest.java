package coverstory.platform.crypto;

import javax.crypto.KeyGenerator;
import javax.crypto.AEADBadTagException;
import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
        } catch (AEADBadTagException expected) {
            // Expected: GCM authenticates the field context.
        }

        byte[] alteredCiphertext = encrypted.ciphertext();
        alteredCiphertext[alteredCiphertext.length - 1] ^= 1;
        EncryptedField tampered = new EncryptedField(
                encrypted.formatVersion(), encrypted.keyId(), encrypted.nonce(), alteredCiphertext);
        try {
            cipher.decrypt(tampered, aad);
            throw new AssertionError("ciphertext tampering was accepted");
        } catch (AEADBadTagException expected) {
            // Expected: GCM authenticates the ciphertext and tag.
        }

        byte[] exposed = encrypted.ciphertext();
        exposed[0] ^= 1;
        if (!"learner@example.invalid".equals(cipher.decrypt(encrypted, aad))) {
            throw new AssertionError("ciphertext accessor leaked mutable storage");
        }

        Set<String> nonces = new HashSet<>();
        for (int i = 0; i < 1_000; i++) {
            if (!nonces.add(Arrays.toString(cipher.encrypt("synthetic", aad).nonce()))) {
                throw new AssertionError("nonce collision in 1,000 encryption samples");
            }
        }
        System.out.println("Field cipher infrastructure smoke checks passed.");
    }
}
