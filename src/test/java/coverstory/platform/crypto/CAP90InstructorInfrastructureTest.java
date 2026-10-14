package coverstory.platform.crypto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Independent test-only oracle; it does not implement or repair the learner seam. */
class CAP90InstructorInfrastructureTest {
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-90/synthetic-v1.json");
    private static final String MARKER = "CAP90-SYNTHETIC-PRIVATE-MARKER-DO-NOT-LOG";
    private static final CAP90ProtectedPayloadSeam.Metadata META = new CAP90ProtectedPayloadSeam.Metadata(
            "synthetic-application-revision-001", 3, "APPLICATION_ANSWERS", 1);

    @Test void fixtureStatesPreciselyWhatIsSupportedAndRestoreIsBlocked() throws Exception {
        JsonNode fixture = new ObjectMapper().readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_CRYPTO_CONTRACT", fixture.path("classification").asText());
        assertFalse(fixture.path("actual_provider_call").asBoolean());
        assertEquals(7, fixture.path("cases").size());
        assertEquals("BLOCKED_CROSS_PROCESS_RESTORE", fixture.path("cases").get(5).path("expected").asText());
        assertTrue(fixture.path("cross_process_restore").asText().contains("BLOCKED"));
        assertEquals(MARKER, fixture.path("marker").asText());
    }

    @Test void suppliedCipherRoundTripsSyntheticMarkerAndNeverStoresPlaintextInCipherEnvelope() throws Exception {
        EphemeralKeys keys = new EphemeralKeys();
        FieldCipher cipher = new AesGcmFieldCipher(keys);
        EncryptedField encrypted = cipher.encrypt(MARKER, CAP90ProtectedPayloadSeam.associatedData(META));
        assertEquals(MARKER, cipher.decrypt(encrypted, CAP90ProtectedPayloadSeam.associatedData(META)));
        assertFalse(new String(encrypted.ciphertext(), StandardCharsets.UTF_8).contains(MARKER));
        assertFalse(encrypted.toString().contains(MARKER));
        assertFalse(java.util.Arrays.equals(encrypted.ciphertext(), keys.activeKey().key().getEncoded()));
        // This is an in-memory ciphertext representation check, not database evidence.
    }

    @Test void missingKeyFailsClosedAndSafeSummaryExposesOnlyConfiguredBoolean() throws Exception {
        EphemeralKeys keys = new EphemeralKeys();
        EncryptedField encrypted = new AesGcmFieldCipher(keys).encrypt(MARKER, CAP90ProtectedPayloadSeam.associatedData(META));
        KeyProvider missing = new KeyProvider(null);
        CountingCipher cipher = new CountingCipher(new AesGcmFieldCipher(missing));
        RefResult result = referenceLoad("synthetic-owner-A", "synthetic-owner-A", META,
                new CAP90ProtectedPayloadSeam.Envelope(META, encrypted), cipher, missing);
        assertEquals(CAP90ProtectedPayloadSeam.Status.KEY_UNAVAILABLE, result.status);
        assertNull(result.payload);
        CAP90ProtectedPayloadSeam seam = new CAP90ProtectedPayloadSeam(cipher, missing, (p, o) -> true);
        assertEquals(new CAP90ProtectedPayloadSeam.SafeSummary(false), seam.safeSummary());
        assertFalse(result.toString().contains(MARKER));
        assertFalse(seam.safeSummary().toString().contains("key"));
    }

    @Test void foreignOwnerIsDeniedBeforeCipherOrProviderDecrypt() throws Exception {
        EphemeralKeys keys = new EphemeralKeys();
        EncryptedField encrypted = new AesGcmFieldCipher(keys).encrypt(MARKER, CAP90ProtectedPayloadSeam.associatedData(META));
        CountingCipher cipher = new CountingCipher(new AesGcmFieldCipher(keys));
        RefResult result = referenceLoad("synthetic-owner-B", "synthetic-owner-A", META,
                new CAP90ProtectedPayloadSeam.Envelope(META, encrypted), cipher, keys);
        assertEquals(CAP90ProtectedPayloadSeam.Status.FORBIDDEN, result.status);
        assertEquals(0, cipher.decryptCalls.get());
        assertNull(result.payload);
    }

    @Test void authenticationAndEnvelopeFailuresReturnNoPartialPayloadOrSecretText() throws Exception {
        EphemeralKeys keys = new EphemeralKeys();
        FieldCipher cipher = new AesGcmFieldCipher(keys);
        EncryptedField valid = cipher.encrypt(MARKER, CAP90ProtectedPayloadSeam.associatedData(META));
        byte[] altered = valid.ciphertext(); altered[altered.length - 1] ^= 1;
        EncryptedField changedTag = new EncryptedField(1, valid.keyId(), valid.nonce(), altered);
        RefResult tag = referenceLoad("synthetic-owner-A", "synthetic-owner-A", META,
                new CAP90ProtectedPayloadSeam.Envelope(META, changedTag), new CountingCipher(cipher), keys);
        assertEquals(CAP90ProtectedPayloadSeam.Status.AUTHENTICATION_FAILED, tag.status);
        assertNull(tag.payload);

        var otherRevision = new CAP90ProtectedPayloadSeam.Metadata(META.objectId(), 4, META.payloadKind(), META.schemaVersion());
        RefResult revisionAad = referenceLoad("synthetic-owner-A", "synthetic-owner-A", otherRevision,
                new CAP90ProtectedPayloadSeam.Envelope(otherRevision, valid), new CountingCipher(cipher), keys);
        assertEquals(CAP90ProtectedPayloadSeam.Status.AUTHENTICATION_FAILED, revisionAad.status);
        assertNull(revisionAad.payload);
        var otherObject = new CAP90ProtectedPayloadSeam.Metadata("synthetic-application-revision-002", 3,
                META.payloadKind(), META.schemaVersion());
        RefResult objectAad = referenceLoad("synthetic-owner-A", "synthetic-owner-A", otherObject,
                new CAP90ProtectedPayloadSeam.Envelope(otherObject, valid), new CountingCipher(cipher), keys);
        assertEquals(CAP90ProtectedPayloadSeam.Status.AUTHENTICATION_FAILED, objectAad.status);
        assertNull(objectAad.payload);

        RefResult mismatch = referenceLoad("synthetic-owner-A", "synthetic-owner-A", otherRevision,
                new CAP90ProtectedPayloadSeam.Envelope(META, valid), new CountingCipher(cipher), keys);
        assertEquals(CAP90ProtectedPayloadSeam.Status.INVALID_ENVELOPE, mismatch.status);

        assertThrows(IllegalArgumentException.class, () -> new EncryptedField(1, valid.keyId(), new byte[11], valid.ciphertext()));
        assertFalse(tag.toString().contains(MARKER));
        assertFalse(revisionAad.toString().contains(MARKER));
    }

    @Test void normalEncryptionGeneratesDistinctNoncesAndSameProcessRotationRetainsBusinessIdentity() throws Exception {
        EphemeralKeys keys = new EphemeralKeys();
        FieldCipher cipher = new AesGcmFieldCipher(keys);
        byte[] aad = CAP90ProtectedPayloadSeam.associatedData(META);
        EncryptedField first = cipher.encrypt(MARKER, aad);
        EncryptedField second = cipher.encrypt(MARKER, aad);
        assertFalse(java.util.Arrays.equals(first.nonce(), second.nonce()));
        String objectId = META.objectId(); long revision = META.objectRevision();
        keys.rotate();
        assertEquals(MARKER, cipher.decrypt(first, aad));
        assertEquals(MARKER, cipher.decrypt(second, aad));
        assertEquals(objectId, META.objectId()); assertEquals(revision, META.objectRevision());
        // Cross-process restore is intentionally not claimed: these keys exist only in this process.
    }

    @Test void providerFailureDetailsAndSyntheticCanaryNeverEnterSafeReceipt() throws Exception {
        EphemeralKeys keys = new EphemeralKeys();
        EncryptedField encrypted = new AesGcmFieldCipher(keys).encrypt(MARKER, CAP90ProtectedPayloadSeam.associatedData(META));
        EncryptionKeyProvider broken = new EncryptionKeyProvider() {
            @Override public EncryptionKey activeKey() { throw new IllegalStateException("SECRET_SENTINEL " + MARKER); }
            @Override public Optional<EncryptionKey> findById(String id) { throw new IllegalStateException("SECRET_SENTINEL " + MARKER); }
        };
        StringBuilder capturedOutput = new StringBuilder();
        RefResult result = referenceLoad("synthetic-owner-A", "synthetic-owner-A", META,
                new CAP90ProtectedPayloadSeam.Envelope(META, encrypted), new CountingCipher(new AesGcmFieldCipher(broken)), broken);
        capturedOutput.append(result.status).append('|').append(result.payload == null ? "" : result.payload);
        assertEquals(CAP90ProtectedPayloadSeam.Status.KEY_UNAVAILABLE, result.status);
        assertFalse(capturedOutput.toString().contains("SECRET_SENTINEL"));
        assertFalse(capturedOutput.toString().contains(MARKER));
    }

    private static RefResult referenceLoad(String principal, String owner, CAP90ProtectedPayloadSeam.Metadata expected,
            CAP90ProtectedPayloadSeam.Envelope envelope, CountingCipher cipher, EncryptionKeyProvider keys) {
        if (principal == null || principal.isBlank()) return new RefResult(CAP90ProtectedPayloadSeam.Status.FORBIDDEN, null);
        if (!principal.equals(owner)) return new RefResult(CAP90ProtectedPayloadSeam.Status.FORBIDDEN, null);
        if (expected == null || envelope == null || !expected.equals(envelope.metadata()))
            return new RefResult(CAP90ProtectedPayloadSeam.Status.INVALID_ENVELOPE, null);
        try {
            // Probe only the key reference before decrypt; no exception detail crosses this boundary.
            if (keys.findById(envelope.encryptedField().keyId()).isEmpty())
                return new RefResult(CAP90ProtectedPayloadSeam.Status.KEY_UNAVAILABLE, null);
        } catch (RuntimeException unavailable) {
            return new RefResult(CAP90ProtectedPayloadSeam.Status.KEY_UNAVAILABLE, null);
        }
        try {
            String payload = cipher.decrypt(envelope.encryptedField(), CAP90ProtectedPayloadSeam.associatedData(expected));
            return new RefResult(CAP90ProtectedPayloadSeam.Status.OK, payload);
        } catch (GeneralSecurityException | RuntimeException safeFailure) {
            return new RefResult(CAP90ProtectedPayloadSeam.Status.AUTHENTICATION_FAILED, null);
        }
    }

    private record RefResult(CAP90ProtectedPayloadSeam.Status status, String payload) { }

    private static class CountingCipher implements FieldCipher {
        private final FieldCipher delegate; private final AtomicInteger decryptCalls = new AtomicInteger();
        private CountingCipher(FieldCipher delegate) { this.delegate = delegate; }
        @Override public EncryptedField encrypt(String plaintext, byte[] aad) throws GeneralSecurityException { return delegate.encrypt(plaintext, aad); }
        @Override public String decrypt(EncryptedField encrypted, byte[] aad) throws GeneralSecurityException {
            decryptCalls.incrementAndGet(); return delegate.decrypt(encrypted, aad);
        }
    }

    private static class EphemeralKeys extends KeyProvider {
        private int sequence = 1;
        private EphemeralKeys() throws Exception { super(generate("cap90-ephemeral-v1")); }
        private void rotate() throws Exception { add(generate("cap90-ephemeral-v" + (++sequence))); }
    }

    private static class KeyProvider implements EncryptionKeyProvider {
        private final Map<String, EncryptionKey> retained = new HashMap<>(); private EncryptionKey active;
        private KeyProvider(EncryptionKey key) { this.active = key; if (key != null) retained.put(key.keyId(), key); }
        protected void add(EncryptionKey key) { retained.put(key.keyId(), key); active = key; }
        @Override public EncryptionKey activeKey() { return active; }
        @Override public Optional<EncryptionKey> findById(String id) { return Optional.ofNullable(retained.get(id)); }
    }

    private static EncryptionKey generate(String id) throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES"); generator.init(256);
        SecretKey secretKey = generator.generateKey(); return new EncryptionKey(id, secretKey);
    }
}
