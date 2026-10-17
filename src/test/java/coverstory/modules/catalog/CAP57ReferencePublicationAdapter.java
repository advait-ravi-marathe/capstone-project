package coverstory.modules.catalog;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;

/** Instructor reference transaction used only by CAP-57 integration tests. */
public final class CAP57ReferencePublicationAdapter {
    public record Result(int status, String code, UUID productId, UUID versionId) { }
    public record ExportRow(UUID productId, UUID versionId, String contentSha256, long amountMinor, String currency) { }

    private final DataSource dataSource;

    public CAP57ReferencePublicationAdapter(DataSource dataSource) { this.dataSource = dataSource; }

    Result publish(UUID editorId, UUID productId, UUID versionId, String requestedHash, boolean failBeforeCommit) {
        byte[] hash;
        try { hash = HexFormat.of().parseHex(requestedHash); }
        catch (RuntimeException invalid) { return new Result(400, "INVALID_CONTENT_HASH", productId, versionId); }
        if (hash.length != 32) return new Result(400, "INVALID_CONTENT_HASH", productId, versionId);

        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try {
                String sourceVersion;
                byte[] storedHash;
                try (PreparedStatement q = c.prepareStatement("""
                        SELECT source_version, content_sha256
                          FROM catalog.health_offer_version
                         WHERE product_id = ? AND version_id = ? FOR UPDATE
                        """)) {
                    q.setObject(1, productId); q.setObject(2, versionId);
                    try (ResultSet row = q.executeQuery()) {
                        if (!row.next()) { c.rollback(); return new Result(404, "PREPARED_VERSION_NOT_FOUND", productId, versionId); }
                        sourceVersion = row.getString(1); storedHash = row.getBytes(2);
                    }
                }
                if (!MessageDigest.isEqual(storedHash, hash)) {
                    c.rollback(); return new Result(409, "VERSION_CONTENT_CONFLICT", productId, versionId);
                }

                UUID activeVersion = null;
                try (PreparedStatement q = c.prepareStatement("SELECT version_id FROM catalog.active_health_offer WHERE product_id = ? FOR UPDATE")) {
                    q.setObject(1, productId);
                    try (ResultSet row = q.executeQuery()) { if (row.next()) activeVersion = row.getObject(1, UUID.class); }
                }
                if (versionId.equals(activeVersion)) {
                    c.commit(); return new Result(200, "DUPLICATE", productId, versionId);
                }

                try (PreparedStatement q = c.prepareStatement("""
                        INSERT INTO catalog.active_health_offer(product_id, version_id)
                        VALUES (?, ?)
                        ON CONFLICT (product_id) DO UPDATE
                            SET version_id = EXCLUDED.version_id, published_at = now()
                        """)) {
                    q.setObject(1, productId); q.setObject(2, versionId); q.executeUpdate();
                }
                try (PreparedStatement q = c.prepareStatement("""
                        INSERT INTO catalog.publication_audit_event
                            (audit_id, editor_id, event_code, product_id, version_id, source_version, content_sha256)
                        VALUES (?, ?, 'CATALOG_VERSION_PUBLISHED', ?, ?, ?, ?)
                        """)) {
                    q.setObject(1, UUID.randomUUID()); q.setObject(2, editorId); q.setObject(3, productId);
                    q.setObject(4, versionId); q.setString(5, sourceVersion); q.setBytes(6, storedHash); q.executeUpdate();
                }
                if (failBeforeCommit) throw new SQLException("synthetic CAP-57 failure before commit");
                c.commit();
                return new Result(201, "PUBLISHED", productId, versionId);
            } catch (SQLException failure) {
                c.rollback(); return new Result(503, "PUBLICATION_UNAVAILABLE", productId, versionId);
            }
        } catch (SQLException unavailable) {
            return new Result(503, "PUBLICATION_UNAVAILABLE", productId, versionId);
        }
    }

    /** Test-only CS90 recovery oracle: stage a new synthetic immutable row and publish it atomically. */
    Result stageAndPublish(UUID editorId, UUID productId, UUID versionId, String sourceVersion,
                           String sourceId, String requestedHash, boolean failBeforeCommit) {
        byte[] hash;
        try { hash = HexFormat.of().parseHex(requestedHash); }
        catch (RuntimeException invalid) { return new Result(400, "INVALID_CONTENT_HASH", productId, versionId); }
        if (hash.length != 32 || sourceVersion == null || sourceVersion.isBlank()
                || sourceId == null || sourceId.isBlank())
            return new Result(400, "INVALID_PUBLICATION_REQUEST", productId, versionId);
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement q = c.prepareStatement("""
                        SELECT source_version, content_sha256
                          FROM catalog.health_offer_version
                         WHERE product_id = ? AND version_id = ? FOR UPDATE
                        """)) {
                    q.setObject(1, productId); q.setObject(2, versionId);
                    try (ResultSet row = q.executeQuery()) {
                        if (row.next()) {
                            if (!sourceVersion.equals(row.getString(1))
                                    || !MessageDigest.isEqual(row.getBytes(2), hash)) {
                                c.rollback(); return new Result(409, "VERSION_CONTENT_CONFLICT", productId, versionId);
                            }
                            try (PreparedStatement active = c.prepareStatement("""
                                    SELECT version_id FROM catalog.active_health_offer WHERE product_id = ? FOR UPDATE
                                    """)) {
                                active.setObject(1, productId);
                                try (ResultSet current = active.executeQuery()) {
                                    if (current.next() && versionId.equals(current.getObject(1, UUID.class))) {
                                        c.commit(); return new Result(200, "DUPLICATE", productId, versionId);
                                    }
                                }
                            }
                        } else {
                            try (PreparedStatement insert = c.prepareStatement("""
                                    INSERT INTO catalog.health_offer_version
                                      (product_id, version_id, source_version, restrictions, amount_minor, currency, content_sha256)
                                    VALUES (?, ?, ?, '{"source":"synthetic"}'::jsonb, 133456, 'INR', ?)
                                    """)) {
                                insert.setObject(1, productId); insert.setObject(2, versionId);
                                insert.setString(3, sourceVersion); insert.setBytes(4, hash); insert.executeUpdate();
                            }
                            try (PreparedStatement insert = c.prepareStatement("""
                                    INSERT INTO catalog.health_offer_discovery_metadata
                                      (product_id, version_id, category, scenario_id, source_id, evidence_status, display_name)
                                    VALUES (?, ?, 'health', 'individual', ?, 'SYNTHETIC', ?)
                                    """)) {
                                insert.setObject(1, productId); insert.setObject(2, versionId);
                                insert.setString(3, sourceId); insert.setString(4, sourceId + " " + sourceVersion);
                                insert.executeUpdate();
                            }
                        }
                    }
                }
                try (PreparedStatement active = c.prepareStatement("""
                        INSERT INTO catalog.active_health_offer(product_id, version_id) VALUES (?, ?)
                        ON CONFLICT (product_id) DO UPDATE SET version_id = EXCLUDED.version_id, published_at = now()
                        """)) {
                    active.setObject(1, productId); active.setObject(2, versionId); active.executeUpdate();
                }
                try (PreparedStatement audit = c.prepareStatement("""
                        INSERT INTO catalog.publication_audit_event
                          (audit_id, editor_id, event_code, product_id, version_id, source_version, content_sha256)
                        VALUES (?, ?, 'CATALOG_VERSION_PUBLISHED', ?, ?, ?, ?)
                        """)) {
                    audit.setObject(1, UUID.randomUUID()); audit.setObject(2, editorId);
                    audit.setObject(3, productId); audit.setObject(4, versionId);
                    audit.setString(5, sourceVersion); audit.setBytes(6, hash); audit.executeUpdate();
                }
                if (failBeforeCommit) throw new SQLException("synthetic CS90 failure before commit");
                c.commit();
                return new Result(201, "PUBLISHED", productId, versionId);
            } catch (SQLException failure) {
                c.rollback(); return new Result(503, "PUBLICATION_UNAVAILABLE", productId, versionId);
            }
        } catch (SQLException unavailable) {
            return new Result(503, "PUBLICATION_UNAVAILABLE", productId, versionId);
        }
    }

    List<ExportRow> exportPublished() throws SQLException {
        List<ExportRow> rows = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement q = c.prepareStatement("""
                SELECT product_id, version_id, content_sha256, amount_minor, currency
                  FROM catalog.published_health_offer_export
                 ORDER BY product_id, version_id
                """ ); ResultSet result = q.executeQuery()) {
            while (result.next()) rows.add(new ExportRow(result.getObject(1, UUID.class), result.getObject(2, UUID.class),
                    result.getString(3), result.getLong(4), result.getString(5).trim()));
        }
        return List.copyOf(rows);
    }
}
