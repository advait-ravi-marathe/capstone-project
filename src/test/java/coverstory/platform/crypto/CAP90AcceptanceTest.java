package coverstory.platform.crypto;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/** Learner acceptance entry point. Contract examples remain blocked at the explicit TODO. */
@Tag("learnerAcceptance")
class CAP90AcceptanceTest {
    @Test void learnerMustImplementProtectedPayloadAccessAfterInstructorSetup() {
        EncryptionKeyProvider unavailable = new EncryptionKeyProvider() {
            @Override public EncryptionKey activeKey() { return null; }
            @Override public java.util.Optional<EncryptionKey> findById(String keyId) { return java.util.Optional.empty(); }
        };
        CAP90ProtectedPayloadSeam seam = new CAP90ProtectedPayloadSeam(
                new AesGcmFieldCipher(unavailable), unavailable, (principal, objectId) -> true);
        var metadata = new CAP90ProtectedPayloadSeam.Metadata("synthetic-object-001", 1, "APPLICATION_ANSWERS", 1);
        var envelope = new CAP90ProtectedPayloadSeam.Envelope(metadata,
                new EncryptedField(1, "ephemeral-key-v1", new byte[12], new byte[16]));
        assertThrows(UnsupportedOperationException.class,
                () -> seam.loadProtectedAccess("synthetic-owner-A", metadata, envelope));
    }
}
