package assignments.interview;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Small Spring JDBC/PostgreSQL observations for interview practice. These
 * tests create their own embedded database and do not implement a CoverStory
 * learner TODO or alter the application's schema.
 */
class Sprint1DesignExperimentsTest {
    private static EmbeddedPostgres postgres;
    private static DataSource dataSource;
    private JdbcTemplate jdbc;

    @BeforeAll
    static void startDatabase() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        dataSource = postgres.getPostgresDatabase();
    }

    @AfterAll
    static void stopDatabase() throws Exception {
        if (postgres != null) postgres.close();
    }

    @BeforeEach
    void createFreshTables() {
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("DROP TABLE IF EXISTS idempotency_request");
        jdbc.execute("DROP TABLE IF EXISTS training_policy");
    }

    @Test
    void ownerScopedKeyRejectsOnlySameOwnerDuplicate() {
        jdbc.execute("""
                CREATE TABLE idempotency_request (
                    owner_id text NOT NULL,
                    request_key text NOT NULL,
                    PRIMARY KEY (owner_id, request_key)
                )
                """);

        jdbc.update("INSERT INTO idempotency_request(owner_id, request_key) VALUES (?, ?)", "owner-a", "key-1");

        assertThrows(DuplicateKeyException.class, () -> jdbc.update(
                "INSERT INTO idempotency_request(owner_id, request_key) VALUES (?, ?)", "owner-a", "key-1"));
        assertEquals(1, jdbc.update(
                "INSERT INTO idempotency_request(owner_id, request_key) VALUES (?, ?)", "owner-b", "key-1"));
        assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM idempotency_request", Integer.class));
    }

    @Test
    void rollbackKeepsAggregateAndReceiptConsistent() {
        createPolicyAndReceiptTables();
        jdbc.update("INSERT INTO training_policy(id, owner_id) VALUES (?, ?)", "policy-old", "owner-a");
        jdbc.update("INSERT INTO idempotency_request(owner_id, request_key, policy_id) VALUES (?, ?, ?)",
                "owner-a", "retry-key", "policy-old");

        var transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        assertThrows(DuplicateKeyException.class, () -> transaction.executeWithoutResult(status -> {
            jdbc.update("INSERT INTO training_policy(id, owner_id) VALUES (?, ?)", "policy-new", "owner-a");
            jdbc.update("INSERT INTO idempotency_request(owner_id, request_key, policy_id) VALUES (?, ?, ?)",
                    "owner-a", "retry-key", "policy-new");
        }));

        assertEquals(0, jdbc.queryForObject(
                "SELECT count(*) FROM training_policy WHERE id = ?", Integer.class, "policy-new"));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM training_policy", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM idempotency_request", Integer.class));
    }

    @Test
    void parameterizedOwnerReadConcealsForeignAndLiteralInput() {
        jdbc.execute("CREATE TABLE training_policy (id text PRIMARY KEY, owner_id text NOT NULL, premium numeric NOT NULL)");
        jdbc.update("INSERT INTO training_policy(id, owner_id, premium) VALUES (?, ?, ?)",
                "policy-a", "owner-a", 125.00);
        jdbc.update("INSERT INTO training_policy(id, owner_id, premium) VALUES (?, ?, ?)",
                "policy-b", "owner-b", 90.00);

        assertEquals(Optional.of(new PolicySummary("policy-a", 125.00)),
                findForOwner("policy-a", "owner-a"));
        assertTrue(findForOwner("policy-b", "owner-a").isEmpty());
        assertTrue(findForOwner("missing", "owner-a").isEmpty());
        assertTrue(findForOwner("policy-a' OR '1'='1", "owner-a").isEmpty());
        assertFalse(findForOwner("policy-a", "owner-b").isPresent());
    }

    private Optional<PolicySummary> findForOwner(String policyId, String authenticatedOwner) {
        List<PolicySummary> rows = jdbc.query(
                "SELECT id, premium FROM training_policy WHERE id = ? AND owner_id = ?",
                (rs, rowNum) -> new PolicySummary(rs.getString("id"), rs.getDouble("premium")),
                policyId, authenticatedOwner);
        return rows.stream().findFirst();
    }

    private void createPolicyAndReceiptTables() {
        jdbc.execute("CREATE TABLE training_policy (id text PRIMARY KEY, owner_id text NOT NULL)");
        jdbc.execute("""
                CREATE TABLE idempotency_request (
                    owner_id text NOT NULL,
                    request_key text NOT NULL,
                    policy_id text NOT NULL REFERENCES training_policy(id),
                    PRIMARY KEY (owner_id, request_key)
                )
                """);
    }

    private record PolicySummary(String id, double premium) { }
}
