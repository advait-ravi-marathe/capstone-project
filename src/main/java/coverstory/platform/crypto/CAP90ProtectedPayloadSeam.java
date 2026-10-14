package coverstory.platform.crypto;

import java.util.Objects;

/**
 * Bounded CAP-90 learner seam. The key provider and authenticated cipher are supplied
 * infrastructure; this task is not an invitation to implement cryptography.
 */
public final class CAP90ProtectedPayloadSeam {
    public enum Status { OK, CONFIG_MISSING, KEY_UNAVAILABLE, FORBIDDEN, INVALID_ENVELOPE, AUTHENTICATION_FAILED }

    public record Metadata(String objectId, long objectRevision, String payloadKind, int schemaVersion) {
        public Metadata {
            Objects.requireNonNull(objectId, "objectId");
            Objects.requireNonNull(payloadKind, "payloadKind");
        }
    }

    public record Envelope(Metadata metadata, EncryptedField encryptedField) {
        public Envelope {
            Objects.requireNonNull(metadata, "metadata");
            Objects.requireNonNull(encryptedField, "encryptedField");
        }
    }

    public record Result(Status status, String payload) {
        public Result {
            Objects.requireNonNull(status, "status");
            if (status != Status.OK && payload != null)
                throw new IllegalArgumentException("failed result cannot contain payload");
        }
    }

    @FunctionalInterface
    public interface OwnerAuthorization {
        boolean mayRead(String principalId, String objectId);
    }

    private final FieldCipher cipher;
    private final EncryptionKeyProvider keys;
    private final OwnerAuthorization authorization;

    public CAP90ProtectedPayloadSeam(FieldCipher cipher, EncryptionKeyProvider keys, OwnerAuthorization authorization) {
        this.cipher = Objects.requireNonNull(cipher, "cipher");
        this.keys = Objects.requireNonNull(keys, "keys");
        this.authorization = Objects.requireNonNull(authorization, "authorization");
    }

    /**
     * Learner TODO: validate the request and envelope, authorize before decrypting,
     * use the supplied FieldCipher, and map failures to safe codes with no fallback.
     */
    public Result loadProtectedAccess(String principalId, Metadata expected, Envelope envelope) {
        // LEARNER TODO: preserve fail-closed behavior; do not implement encryption here.
        throw new UnsupportedOperationException("CAP-90 learner TODO: protected payload access");
    }

    /** A summary may expose configuration state only, never IDs or key material. */
    public record SafeSummary(boolean configured) { }

    public SafeSummary safeSummary() {
        // Configuration inspection is deliberately non-throwing and does not expose key IDs.
        try {
            EncryptionKey active = keys.activeKey();
            return new SafeSummary(active != null && active.key() != null && !active.keyId().isBlank());
        } catch (RuntimeException unavailable) {
            return new SafeSummary(false);
        }
    }

    /** Canonical non-secret AAD binds opaque identity and schema, never payload fields. */
    public static byte[] associatedData(Metadata metadata) {
        Objects.requireNonNull(metadata, "metadata");
        if (metadata.objectId().isBlank() || metadata.objectRevision() < 1
                || metadata.payloadKind().isBlank() || metadata.schemaVersion() < 1)
            throw new IllegalArgumentException("invalid protected payload metadata");
        try {
            var bytes = new java.io.ByteArrayOutputStream();
            try (var output = new java.io.DataOutputStream(bytes)) {
                output.writeInt(1); // AAD encoding version
                output.writeInt(metadata.schemaVersion());
                writeUtf8(output, metadata.objectId());
                output.writeLong(metadata.objectRevision());
                writeUtf8(output, metadata.payloadKind());
            }
            return bytes.toByteArray();
        } catch (java.io.IOException impossibleForMemoryStream) {
            throw new IllegalStateException("could not encode protected payload metadata");
        }
    }

    private static void writeUtf8(java.io.DataOutputStream output, String value) throws java.io.IOException {
        byte[] encoded = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        output.writeInt(encoded.length);
        output.write(encoded);
    }
}
