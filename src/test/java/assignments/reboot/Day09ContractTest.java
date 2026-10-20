package assignments.reboot;

import assignments.insurance.QuoteRepositoryAdapter;
import assignments.insurance.QuoteSnapshot;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Real PostgreSQL contract; the seed schema bypasses Days 7–8 services/migration. */
@Tag("learnerAcceptance")
class Day09ContractTest {
    private static EmbeddedPostgres postgres;
    private static DataSource dataSource;
    private final QuoteRepositoryAdapter adapter = new QuoteRepositoryAdapter(dataSource);

    @BeforeAll
    static void startPostgresAndInstallTestOnlySchema() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        dataSource = postgres.getPostgresDatabase();
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE quote_versions (
                      journey_id TEXT NOT NULL,
                      quote_version_id TEXT NOT NULL,
                      product_code TEXT NOT NULL,
                      product_version TEXT NOT NULL,
                      rate_version TEXT NOT NULL,
                      rebuild_amount_rupees NUMERIC(14,2) NOT NULL,
                      coverage_code TEXT NOT NULL,
                      currency TEXT NOT NULL,
                      annual_premium_rupees NUMERIC(14,2) NOT NULL,
                      created_at TIMESTAMPTZ NOT NULL,
                      expires_at TIMESTAMPTZ NOT NULL,
                      PRIMARY KEY (journey_id, quote_version_id)
                    )
                    """);
        }
    }

    @AfterAll
    static void stopPostgres() throws IOException {
        if (postgres != null) postgres.close();
    }

    @BeforeEach
    void clearRows() throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE quote_versions");
        }
    }

    @Test
    void roundTripPreservesDecimalUtcTimeAndVersion() throws Exception {
        QuoteSnapshot expected = snapshot("v-2026-10", "123456.78", "1499.35",
                Instant.parse("2026-10-20T10:15:30Z"), Instant.parse("2026-10-20T10:30:30Z"));
        adapter.insert("journey-A", expected);
        assertEquals(Optional.of(expected), adapter.find("journey-A", "v-2026-10"));
    }

    @Test
    void unknownAndWrongJourneyAreExplicitAbsence() throws Exception {
        adapter.insert("journey-A", snapshot("v1", "100000.00", "1000.00",
                Instant.parse("2026-10-20T10:15:30Z"), Instant.parse("2026-10-20T10:30:30Z")));
        assertEquals(Optional.empty(), adapter.find("journey-A", "missing"));
        assertEquals(Optional.empty(), adapter.find("journey-B", "v1"));
    }

    @Test
    void insertingExistingCompositeKeyDoesNotReplaceSnapshot() throws Exception {
        QuoteSnapshot original = snapshot("v1", "100000.00", "1000.00",
                Instant.parse("2026-10-20T10:15:30Z"), Instant.parse("2026-10-20T10:30:30Z"));
        adapter.insert("journey-A", original);
        QuoteSnapshot attemptedReplacement = snapshot("v1", "200000.00", "2000.00",
                Instant.parse("2026-10-20T10:16:30Z"), Instant.parse("2026-10-20T10:31:30Z"));
        org.junit.jupiter.api.Assertions.assertThrows(java.sql.SQLException.class,
                () -> adapter.insert("journey-A", attemptedReplacement));
        assertEquals(Optional.of(original), adapter.find("journey-A", "v1"));
    }

    private static QuoteSnapshot snapshot(String version, String rebuild, String premium,
                                          Instant created, Instant expires) {
        return new QuoteSnapshot(version, "HOME_SECURE", "hs-2026-01", "rate-2026-01",
                new BigDecimal(rebuild), "STANDARD", "INR", new BigDecimal(premium), created, expires);
    }
}
