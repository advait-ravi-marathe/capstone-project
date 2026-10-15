package coverstory.modules.application.persistence;

import coverstory.platform.crypto.EncryptedField;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/** Stores authenticated ciphertext and its Application revision link; it never handles plaintext or key bytes. */
public final class PostgresProtectedPayloadEnvelopeStore {
    public record Stored(UUID envelopeId, UUID applicationId, UUID ownerId, long revision,
                         String payloadKind, int schemaVersion, EncryptedField encrypted) { }

    private final DataSource dataSource;

    public PostgresProtectedPayloadEnvelopeStore(DataSource dataSource) {
        this.dataSource = java.util.Objects.requireNonNull(dataSource);
    }

    public void save(Stored envelope) throws SQLException {
        String sql = """
                INSERT INTO application.protected_payload_envelope
                  (envelope_id, application_id, owner_id, application_revision, payload_kind,
                   schema_version, cipher_format_version, key_id, nonce, ciphertext)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, envelope.envelopeId());
            statement.setObject(2, envelope.applicationId());
            statement.setObject(3, envelope.ownerId());
            statement.setLong(4, envelope.revision());
            statement.setString(5, envelope.payloadKind());
            statement.setInt(6, envelope.schemaVersion());
            statement.setInt(7, envelope.encrypted().formatVersion());
            statement.setString(8, envelope.encrypted().keyId());
            statement.setBytes(9, envelope.encrypted().nonce());
            statement.setBytes(10, envelope.encrypted().ciphertext());
            statement.executeUpdate();
        }
    }

    /** Owner-scoped lookup; callers must still authorize from trusted server identity before decrypting. */
    public Optional<Stored> find(UUID ownerId, UUID applicationId, long revision, String payloadKind) throws SQLException {
        String sql = """
                SELECT envelope_id, application_id, owner_id, application_revision, payload_kind,
                       schema_version, cipher_format_version, key_id, nonce, ciphertext
                  FROM application.protected_payload_envelope
                 WHERE owner_id=? AND application_id=? AND application_revision=? AND payload_kind=?
                """;
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, ownerId);
            statement.setObject(2, applicationId);
            statement.setLong(3, revision);
            statement.setString(4, payloadKind);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) return Optional.empty();
                EncryptedField encrypted = new EncryptedField(rows.getInt("cipher_format_version"),
                        rows.getString("key_id"), rows.getBytes("nonce"), rows.getBytes("ciphertext"));
                return Optional.of(new Stored(rows.getObject("envelope_id", UUID.class),
                        rows.getObject("application_id", UUID.class), rows.getObject("owner_id", UUID.class),
                        rows.getLong("application_revision"), rows.getString("payload_kind"),
                        rows.getInt("schema_version"), encrypted));
            }
        }
    }
}
