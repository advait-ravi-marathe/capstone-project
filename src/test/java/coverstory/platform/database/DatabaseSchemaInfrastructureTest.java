package coverstory.platform.database;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Instructor-only PostgreSQL smoke checks for ownership schemas and safety constraints. */
class DatabaseSchemaInfrastructureTest {
    @Test
    void createsOwnedSchemasAndEnforcesPaymentAndAnalyticsBoundaries() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
             Connection connection = postgres.getPostgresDatabase().getConnection()) {
            executeScript(connection, "/db/schema/ownership.sql");
            executeScript(connection, "/db/schema/constraints.sql");

            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("""
                         SELECT COUNT(*) FROM information_schema.schemata
                         WHERE schema_name IN ('catalog','shortlist','application','payments',
                           'policy','support','business_audit','analytics')
                         """)) {
                result.next();
                assertEquals(8, result.getInt(1));
            }

            UUID owner = UUID.randomUUID();
            UUID payment = UUID.randomUUID();
            UUID application = UUID.randomUUID();
            try (PreparedStatement insert = connection.prepareStatement("""
                    INSERT INTO payments.payment_intent
                      (payment_id, owner_id, application_id, application_revision,
                       idempotency_key_hash, amount_minor, currency, state)
                    VALUES (?, ?, ?, 1, decode('01ab', 'hex'), 2500, 'INR', 'RECORDED')
                    """)) {
                insert.setObject(1, payment);
                insert.setObject(2, owner);
                insert.setObject(3, application);
                assertEquals(1, insert.executeUpdate());
                assertThrows(SQLException.class, insert::executeUpdate,
                        "owner plus idempotency hash must be unique");
            }

            UUID journal = UUID.randomUUID();
            try (PreparedStatement insert = connection.prepareStatement("""
                    INSERT INTO payments.payment_journal
                      (journal_id, payment_id, entry_type, amount_minor, currency)
                    VALUES (?, ?, 'PAYMENT_RECORDED', 2500, 'INR')
                    """)) {
                insert.setObject(1, journal);
                insert.setObject(2, payment);
                insert.executeUpdate();
            }
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE payments.payment_journal SET amount_minor = 1 WHERE journal_id = ?")) {
                update.setObject(1, journal);
                assertThrows(SQLException.class, update::executeUpdate, "journal updates must be rejected");
            }

            try (PreparedStatement analytics = connection.prepareStatement("""
                    INSERT INTO analytics.behavior_event
                      (event_id, event_name, screen_key, session_scope_hash, occurred_at)
                    VALUES (?, 'catalog_viewed', 'catalog', decode('aabb', 'hex'), now())
                    """)) {
                analytics.setObject(1, UUID.randomUUID());
                assertEquals(1, analytics.executeUpdate());
            }
            try (PreparedStatement invalidAnalytics = connection.prepareStatement("""
                    INSERT INTO analytics.behavior_event
                      (event_id, event_name, screen_key, session_scope_hash, occurred_at)
                    VALUES (?, 'consumer_email', 'catalog', decode('aabb', 'hex'), now())
                    """)) {
                invalidAnalytics.setObject(1, UUID.randomUUID());
                assertThrows(SQLException.class, invalidAnalytics::executeUpdate,
                        "unknown event names must be rejected");
            }
        }
    }

    private static void executeScript(Connection connection, String resourcePath) throws Exception {
        String script;
        try (var input = DatabaseSchemaInfrastructureTest.class.getResourceAsStream(resourcePath)) {
            if (input == null) throw new IllegalStateException("Missing instructor resource " + resourcePath);
            script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (Statement statement = connection.createStatement()) {
            for (String sql : splitStatements(script)) {
                if (!sql.isBlank()) statement.execute(sql);
            }
        }
    }

    private static List<String> splitStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inDollarQuote = false;
        for (int i = 0; i < script.length(); i++) {
            if (script.startsWith("$$", i)) {
                inDollarQuote = !inDollarQuote;
                current.append("$$");
                i++;
            } else if (script.charAt(i) == ';' && !inDollarQuote) {
                addStatement(statements, current.toString());
                current.setLength(0);
            } else {
                current.append(script.charAt(i));
            }
        }
        if (!current.isEmpty()) addStatement(statements, current.toString());
        return statements;
    }

    private static void addStatement(List<String> statements, String candidate) {
        String sql = candidate.lines()
                .filter(line -> !line.stripLeading().startsWith("--"))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("").trim();
        if (!sql.isEmpty()) statements.add(sql);
    }
}
