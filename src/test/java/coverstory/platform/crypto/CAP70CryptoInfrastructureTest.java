package coverstory.platform.crypto;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Exercises the existing AES-GCM field adapter; it does not implement envelope staging or key wrapping. */
class CAP70CryptoInfrastructureTest {
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-70/synthetic-v1.json");
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test void fixedVectorsAuthenticateAndMatchCiphertextBytes() throws Exception {
        JsonNode vectors = JSON.readTree(Files.readString(FIXTURE)).path("fixed_vectors");
        String[] markers = {"CAP70-SYNTHETIC-VECTOR-A", "CAP70-SYNTHETIC-VECTOR-B"};
        byte[][] fixedNonces = {sequence(0, 12), sequence(12, 12)};
        for (int i=0; i<vectors.size(); i++) {
            JsonNode vector = vectors.get(i);
            FieldCipher cipher = new AesGcmFieldCipher(provider(), new FixedNonceRandom(fixedNonces[i]));
            byte[] aad = vector.path("aad").asText().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            EncryptedField encrypted = cipher.encrypt(markers[i], aad);
            assertArrayEquals(fixedNonces[i], encrypted.nonce());
            assertArrayEquals(Base64.getDecoder().decode(vector.path("ciphertext_with_tag").asText()), encrypted.ciphertext());
            assertEquals(markers[i], cipher.decrypt(encrypted, aad));
            assertThrows(GeneralSecurityException.class, () -> cipher.decrypt(encrypted, (vector.path("aad").asText()+":wrong").getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            byte[] tamperedBytes = encrypted.ciphertext(); tamperedBytes[tamperedBytes.length-1] ^= 1;
            EncryptedField tampered = new EncryptedField(encrypted.formatVersion(), encrypted.keyId(), encrypted.nonce(), tamperedBytes);
            assertThrows(GeneralSecurityException.class, () -> cipher.decrypt(tampered, aad));
        }
    }

    @Test void ordinaryEncryptionGeneratesDistinctNoncesSeparateFromFixedVectors() throws Exception {
        FieldCipher cipher = new AesGcmFieldCipher(provider());
        byte[] aad = "synthetic-object:revision:1".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        EncryptedField first = cipher.encrypt("synthetic marker", aad);
        EncryptedField second = cipher.encrypt("synthetic marker", aad);
        assertFalse(Arrays.equals(first.nonce(), second.nonce()));
        assertEquals(12, first.nonce().length);
        assertEquals(12, second.nonce().length);
    }

    @Test void unavailableKeyFailsClosedWithoutPlaintextFallback() {
        EncryptionKeyProvider unavailable = new EncryptionKeyProvider() {
            @Override public EncryptionKey activeKey() { throw new IllegalStateException("No active fixture key"); }
            @Override public Optional<EncryptionKey> findById(String keyId) { return Optional.empty(); }
        };
        FieldCipher cipher = new AesGcmFieldCipher(unavailable);
        EncryptedField envelope = new EncryptedField(1, "missing-kek-v1", new byte[12], new byte[16]);
        assertThrows(GeneralSecurityException.class,
                () -> cipher.decrypt(envelope, "synthetic-object:revision:1".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    private static EncryptionKeyProvider provider() {
        EncryptionKey key = new EncryptionKey("fixture-kek-v1", new SecretKeySpec(sequence(0, 32), "AES"));
        return new EncryptionKeyProvider() {
            @Override public EncryptionKey activeKey() { return key; }
            @Override public Optional<EncryptionKey> findById(String keyId) { return keyId.equals(key.keyId()) ? Optional.of(key) : Optional.empty(); }
        };
    }
    private static byte[] sequence(int start, int length) { byte[] out = new byte[length]; for(int i=0;i<length;i++)out[i]=(byte)(start+i); return out; }
    private static final class FixedNonceRandom extends SecureRandom {
        private final byte[] nonce;
        private FixedNonceRandom(byte[] nonce) { this.nonce = nonce.clone(); }
        @Override public void nextBytes(byte[] bytes) { assertEquals(nonce.length, bytes.length); System.arraycopy(nonce, 0, bytes, 0, bytes.length); }
    }
}
