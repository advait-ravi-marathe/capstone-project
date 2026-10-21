package assignments.teaching;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Independent, different-domain example for owner-bound JDBC and nullable dates. */
class RenewalJdbcTeachingReferenceTest {
    record LoanView(String id, long revision, BigDecimal deposit, LocalDate dueDate, LocalDate returnedOn) { }

    @Test
    void scopedReadBindsBothValuesMapsNullsAndLeavesEveryRowUnchanged() throws Exception {
        try (var postgres = EmbeddedPostgres.builder().start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE toy_loan (id text PRIMARY KEY, owner_id text NOT NULL, revision bigint NOT NULL, deposit numeric(12,2) NOT NULL, due_date date NOT NULL, returned_on date)");
                statement.execute("INSERT INTO toy_loan VALUES ('loan-a','reader-a',1,12.50,DATE '2026-12-20',NULL), ('loan-b','reader-b',2,19.75,DATE '2026-12-24',DATE '2026-12-22')");
            }
            String before = fullRows(dataSource);
            assertEquals(Optional.of(new LoanView("loan-a", 1, new BigDecimal("12.50"),
                    LocalDate.parse("2026-12-20"), null)), read(dataSource, "loan-a", "reader-a"));
            assertEquals(before, fullRows(dataSource));
            assertEquals(Optional.of(new LoanView("loan-b", 2, new BigDecimal("19.75"),
                    LocalDate.parse("2026-12-24"), LocalDate.parse("2026-12-22"))), read(dataSource, "loan-b", "reader-b"));
            assertEquals(before, fullRows(dataSource));
            assertEquals(Optional.empty(), read(dataSource, "loan-a", "reader-b"));
            assertEquals(before, fullRows(dataSource));
            assertEquals(Optional.empty(), read(dataSource, "missing", "reader-a"));
            assertEquals(before, fullRows(dataSource));
            assertEquals(Optional.empty(), read(dataSource, "loan-a' OR '1'='1", "reader-a"));
            assertEquals(before, fullRows(dataSource));
        }
    }

    private static Optional<LoanView> read(DataSource dataSource, String id, String owner) throws Exception {
        try (Connection connection = dataSource.getConnection();
             var statement = connection.prepareStatement("SELECT id,revision,deposit,due_date,returned_on FROM toy_loan WHERE id=? AND owner_id=?")) {
            statement.setString(1, id);
            statement.setString(2, owner);
            try (var rows = statement.executeQuery()) {
                if (!rows.next()) return Optional.empty();
                var returned = rows.getDate("returned_on");
                return Optional.of(new LoanView(rows.getString("id"), rows.getLong("revision"),
                        rows.getBigDecimal("deposit"), rows.getDate("due_date").toLocalDate(),
                        returned == null ? null : returned.toLocalDate()));
            }
        }
    }

    private static String fullRows(DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             var result = statement.executeQuery("SELECT COALESCE(jsonb_agg(to_jsonb(t) ORDER BY id), '[]'::jsonb)::text FROM toy_loan t")) {
            assertTrue(result.next());
            return result.getString(1);
        }
    }
}
