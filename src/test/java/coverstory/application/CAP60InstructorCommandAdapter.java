package coverstory.application;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Callable test-only command adapter for the CAP-60 concurrency contract. */
final class CAP60InstructorCommandAdapter {
    static final UUID OWNER = UUID.fromString("60000000-0000-4000-8000-000000000002");
    record Outcome(int status, boolean replay) { }
    static Outcome advance(EmbeddedPostgres pg, UUID journey, byte[] key, byte[] payload, long expectedVersion,
                           CountDownLatch ready, CountDownLatch release, int worker) throws Exception {
        try (Connection c = pg.getPostgresDatabase().getConnection()) {
            c.setAutoCommit(false); ready.countDown();
            if (!release.await(5, TimeUnit.SECONDS)) throw new SQLTimeoutException("barrier release exceeded five seconds");
            int inserted;
            try (PreparedStatement p = c.prepareStatement("INSERT INTO cap60_harness.command_key VALUES (?, ?, ?, 201) ON CONFLICT (journey_id,key_hash) DO NOTHING")) {
                p.setObject(1, journey); p.setBytes(2, key); p.setBytes(3, payload); inserted = p.executeUpdate();
            }
            if (inserted == 0) {
                try (PreparedStatement p = c.prepareStatement("SELECT payload_hash,stored_status FROM cap60_harness.command_key WHERE journey_id=? AND key_hash=?")) {
                    p.setObject(1, journey); p.setBytes(2, key);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) throw new SQLException("idempotency row disappeared");
                        if (!Arrays.equals(payload, r.getBytes(1))) { c.rollback(); return new Outcome(409, false); }
                        int status = r.getInt(2); c.commit(); return new Outcome(status, true);
                    }
                }
            }
            int changed;
            try (PreparedStatement p = c.prepareStatement("UPDATE cap60_harness.journey SET version=version+1,current_step='COMPARE' WHERE journey_id=? AND version=?")) {
                p.setObject(1, journey); p.setLong(2, expectedVersion); changed = p.executeUpdate();
            }
            if (changed == 0) {
                try (PreparedStatement p = c.prepareStatement("UPDATE cap60_harness.command_key SET stored_status=409 WHERE journey_id=? AND key_hash=?")) {
                    p.setObject(1, journey); p.setBytes(2, key); p.executeUpdate();
                }
                c.commit(); return new Outcome(409, false);
            }
            UUID causation = UUID.nameUUIDFromBytes((journey + ":" + worker).getBytes());
            execute(c, "INSERT INTO cap60_harness.domain_effect VALUES (?, ?, ?, 5, ?)", UUID.randomUUID(), journey, OWNER, causation);
            execute(c, "INSERT INTO cap60_harness.audit_event VALUES (?, ?, ?, 'ADVANCED')", UUID.randomUUID(), journey, causation);
            execute(c, "INSERT INTO cap60_harness.step_event VALUES (?, ?, 5, ?, 'ADVANCED')", UUID.randomUUID(), journey, causation);
            c.commit(); return new Outcome(201, false);
        }
    }
    private static void execute(Connection c, String sql, Object... values) throws Exception {
        try (PreparedStatement p = c.prepareStatement(sql)) { for (int i=0;i<values.length;i++) p.setObject(i+1, values[i]); p.executeUpdate(); }
    }
}
