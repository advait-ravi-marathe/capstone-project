package coverstory.modules.payments;

import coverstory.modules.payments.api.CAP80Seam;
import coverstory.modules.payments.api.CAP80Seam.OwnerPaymentItem;
import coverstory.modules.payments.api.CAP80Seam.PaymentCheckpoint;
import coverstory.modules.payments.api.CAP80Seam.PaymentEffectPort;
import coverstory.modules.payments.api.CAP80Seam.ReplayResult;
import coverstory.modules.payments.api.CAP80Seam.ReplayTransaction;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Test-only JDBC adapter for one atomic synthetic Payments/Application unit of work. */
final class CAP80JdbcHarness implements AutoCloseable {
    enum Fault { NONE, BEFORE_CHECKPOINT }

    private final EmbeddedPostgres pg;
    private final Connection connection;
    private final Map<UUID, Integer> checkpointReads = new HashMap<>();
    private int transactionCalls;

    CAP80JdbcHarness(UUID ownerA, UUID appA, UUID ownerB, UUID appB) throws Exception {
        pg = EmbeddedPostgres.builder().start();
        connection = pg.getPostgresDatabase().getConnection();
        try (Statement statement = connection.createStatement()) {
            String schema;
            try (var in = CAP80JdbcHarness.class.getResourceAsStream("/db/schema/payment_replay.sql")) {
                if (in == null) throw new IOException("missing CAP-80 test schema");
                schema = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            for (String sql : schema.split(";")) if (!sql.isBlank()) statement.execute(sql);
        }
        seedOwner(ownerA, appA, 2, "quote-health-A-v1", "evt-existing-A");
        seedOwner(ownerB, appB, 2, "quote-health-A-v1", "evt-existing-B");
    }

    private void seedOwner(UUID owner, UUID app, int checkpoint, String quote, String event) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement(
                "INSERT INTO payments.payment_checkpoint(owner_id,last_sequence) VALUES (?,?)")) {
            p.setObject(1, owner); p.setInt(2, checkpoint); p.executeUpdate();
        }
        try (PreparedStatement p = connection.prepareStatement(
                "INSERT INTO application.onboarding_case(application_id,owner_id,quote_id,state,payment_event_id) VALUES (?,?,?,'AWAITING_PAYMENT',?)")) {
            p.setObject(1, app); p.setObject(2, owner); p.setString(3, quote); p.setString(4, event); p.executeUpdate();
        }
    }

    PaymentEffectPort port(Fault fault) {
        return new PaymentEffectPort() {
            @Override public ReplayResult inUnitOfWork(UUID ownerId, java.util.function.Function<ReplayTransaction, ReplayResult> work) {
                transactionCalls++;
                try {
                    connection.setAutoCommit(false);
                    ReplayResult result = work.apply(new Tx(fault));
                    connection.commit();
                    return result;
                } catch (RuntimeException | SQLException e) {
                    try { connection.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                    if (e instanceof RuntimeException runtime) throw runtime;
                    throw new IllegalStateException("synthetic database transaction failed", e);
                } finally {
                    try { connection.setAutoCommit(true); } catch (SQLException e) { throw new IllegalStateException(e); }
                }
            }
        };
    }

    private final class Tx implements ReplayTransaction {
        private final Fault fault;
        private Tx(Fault fault) { this.fault = fault; }

        @Override public PaymentCheckpoint lockCheckpoint(UUID ownerId) {
            checkpointReads.merge(ownerId, 1, Integer::sum);
            try (PreparedStatement p = connection.prepareStatement(
                    "SELECT last_sequence FROM payments.payment_checkpoint WHERE owner_id=? FOR UPDATE")) {
                p.setObject(1, ownerId);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) return null;
                    return new PaymentCheckpoint(ownerId, r.getInt(1));
                }
            } catch (SQLException e) { throw new IllegalStateException(e); }
        }

        @Override public void appendMockCapture(OwnerPaymentItem item) {
            try (PreparedStatement p = connection.prepareStatement(
                    "INSERT INTO payments.mock_capture_effect(event_id,owner_id,sequence,application_id,quote_id,amount_minor,currency) VALUES (?,?,?,?,?,?,?)")) {
                p.setString(1, item.eventId()); p.setObject(2, item.feedOwnerId()); p.setInt(3, item.sequence());
                p.setObject(4, item.applicationId()); p.setString(5, item.quoteId()); p.setLong(6, item.amountMinor()); p.setString(7, item.currency()); p.executeUpdate();
            } catch (SQLException e) { throw new IllegalStateException(e); }
        }

        @Override public void unlockOnboarding(UUID ownerId, OwnerPaymentItem item) {
            // Test adapter stands in for Application's public module operation; it uses the caller's transaction.
            try (PreparedStatement p = connection.prepareStatement(
                    "UPDATE application.onboarding_case SET state='ONBOARDING', payment_event_id=? WHERE application_id=? AND owner_id=? AND quote_id=? AND state='AWAITING_PAYMENT'")) {
                p.setString(1, item.eventId()); p.setObject(2, item.applicationId()); p.setObject(3, ownerId); p.setString(4, item.quoteId());
                if (p.executeUpdate() != 1) throw new IllegalStateException("owned onboarding transition was not available");
            } catch (SQLException e) { throw new IllegalStateException(e); }
            try (PreparedStatement p = connection.prepareStatement(
                    "INSERT INTO application.onboarding_timeline(application_id,owner_id,payment_event_id,from_state,to_state) VALUES (?,?,?,'AWAITING_PAYMENT','ONBOARDING')")) {
                p.setObject(1, item.applicationId()); p.setObject(2, ownerId); p.setString(3, item.eventId()); p.executeUpdate();
            } catch (SQLException e) { throw new IllegalStateException(e); }
        }

        @Override public void advanceCheckpoint(UUID ownerId, int sequence) {
            if (fault == Fault.BEFORE_CHECKPOINT) throw new IllegalStateException("injected failure after effects before checkpoint write");
            try (PreparedStatement p = connection.prepareStatement(
                    "UPDATE payments.payment_checkpoint SET last_sequence=? WHERE owner_id=?")) {
                p.setInt(1, sequence); p.setObject(2, ownerId);
                if (p.executeUpdate() != 1) throw new IllegalStateException("checkpoint missing");
            } catch (SQLException e) { throw new IllegalStateException(e); }
        }
    }

    int count(String table) throws SQLException {
        String allowed = switch (table) {
            case "payments.mock_capture_effect", "application.onboarding_case", "application.onboarding_timeline" -> table;
            default -> throw new IllegalArgumentException("table not allowed");
        };
        try (Statement s = connection.createStatement(); ResultSet r = s.executeQuery("SELECT count(*) FROM " + allowed)) {
            r.next(); return r.getInt(1);
        }
    }

    int countForOwner(String table, UUID owner) throws SQLException {
        String allowed = switch (table) {
            case "payments.mock_capture_effect", "application.onboarding_case", "application.onboarding_timeline" -> table;
            default -> throw new IllegalArgumentException("table not allowed");
        };
        try (PreparedStatement p = connection.prepareStatement("SELECT count(*) FROM " + allowed + " WHERE owner_id=?")) {
            p.setObject(1, owner); try (ResultSet r = p.executeQuery()) { r.next(); return r.getInt(1); }
        }
    }

    int onboardingCount(UUID owner) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement(
                "SELECT count(*) FROM application.onboarding_case WHERE owner_id=? AND state='ONBOARDING'")) {
            p.setObject(1, owner); try (ResultSet r = p.executeQuery()) { r.next(); return r.getInt(1); }
        }
    }

    int timelineCount(UUID owner) throws SQLException {
        return countForOwner("application.onboarding_timeline", owner);
    }

    int mockCaptureCount(UUID owner) throws SQLException {
        return countForOwner("payments.mock_capture_effect", owner);
    }

    String mockCaptureMoney(UUID owner) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement(
                "SELECT amount_minor || '|' || btrim(currency) FROM payments.mock_capture_effect WHERE owner_id=?")) {
            p.setObject(1, owner); try (ResultSet r = p.executeQuery()) { if (!r.next()) return null; return r.getString(1); }
        }
    }

    int checkpoint(UUID owner) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement("SELECT last_sequence FROM payments.payment_checkpoint WHERE owner_id=?")) {
            p.setObject(1, owner); try (ResultSet r = p.executeQuery()) { if (!r.next()) throw new SQLException("missing checkpoint"); return r.getInt(1); }
        }
    }

    String state(UUID app) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement("SELECT state FROM application.onboarding_case WHERE application_id=?")) {
            p.setObject(1, app); try (ResultSet r = p.executeQuery()) { if (!r.next()) throw new SQLException("missing onboarding case"); return r.getString(1); }
        }
    }

    int transactionCalls() { return transactionCalls; }
    int checkpointReads(UUID owner) { return checkpointReads.getOrDefault(owner, 0); }

    @Override public void close() throws Exception { connection.close(); pg.close(); }
}
