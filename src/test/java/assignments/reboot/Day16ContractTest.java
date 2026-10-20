package assignments.reboot;

import assignments.insurance.QuoteHistoryPage;
import assignments.insurance.QuoteHistoryQuery;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Real PostgreSQL pagination contract; fixture rows bypass quote creation and access policy. */
@Tag("learnerAcceptance")
class Day16ContractTest {
    private static EmbeddedPostgres postgres;
    private static DataSource dataSource;
    private QuoteHistoryQuery query;

    @BeforeAll
    static void startPostgresAndInstallFixedSchema() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        dataSource = postgres.getPostgresDatabase();
        try (Connection connection = dataSource.getConnection(); Statement sql = connection.createStatement()) {
            sql.execute("""
                    CREATE TABLE quote_versions (
                    journey_id TEXT NOT NULL, quote_version_id TEXT NOT NULL, product_code TEXT NOT NULL,
                    product_version TEXT NOT NULL, rate_version TEXT NOT NULL, rebuild_amount_rupees NUMERIC(14,2) NOT NULL,
                    coverage_code TEXT NOT NULL, currency TEXT NOT NULL, annual_premium_rupees NUMERIC(14,2) NOT NULL,
                    created_at TIMESTAMPTZ NOT NULL, expires_at TIMESTAMPTZ NOT NULL,
                    PRIMARY KEY (journey_id, quote_version_id))""");
        }
    }

    @AfterAll
    static void closePostgres() throws IOException { if (postgres != null) postgres.close(); }

    @BeforeEach
    void resetRows() throws Exception {
        query = new QuoteHistoryQuery(dataSource);
        try (Connection connection = dataSource.getConnection(); Statement sql = connection.createStatement()) {
            sql.execute("TRUNCATE quote_versions");
            sql.execute("INSERT INTO quote_versions VALUES " + row("journey-A", "vB", "2026-10-20T10:00:00Z") + ","
                    + row("journey-A", "vA", "2026-10-20T10:00:00Z") + ","
                    + row("journey-A", "vC", "2026-10-20T09:00:00Z") + ","
                    + row("journey-A", "vD", "2026-10-20T08:00:00Z") + ","
                    + row("journey-B", "other", "2026-10-20T11:00:00Z"));
        }
    }

    @Test
    void tieBreaksByVersionAndAdjacentPagesDoNotOverlap() throws Exception {
        QuoteHistoryPage first = query.page("journey-A", 0, 2);
        QuoteHistoryPage second = query.page("journey-A", 1, 2);
        assertEquals(4, first.totalElements());
        assertEquals(java.util.List.of("vA", "vB"), ids(first));
        assertEquals(java.util.List.of("vC", "vD"), ids(second));
        Set<String> unique = new HashSet<>(ids(first));
        unique.addAll(ids(second));
        assertEquals(4, unique.size());
    }

    @Test
    void emptyAndFinalPageAreValidAndJourneyScoped() throws Exception {
        assertEquals(java.util.List.of(), ids(query.page("missing", 0, 10)));
        assertEquals(java.util.List.of("vD"), ids(query.page("journey-A", 1, 3)));
    }

    @Test
    void rejectsUnboundedOrInvalidPaginationInputs() {
        assertThrows(IllegalArgumentException.class, () -> query.page("journey-A", -1, 10));
        assertThrows(IllegalArgumentException.class, () -> query.page("journey-A", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> query.page("journey-A", 0, 21));
    }

    private static String row(String journey, String version, String createdAt) {
        return "('%s','%s','HOME_SECURE','hs-2026-01','rate-2026-01',100000.00,'STANDARD','INR',1250.25,'%s','2026-10-20T10:15:00Z')"
                .formatted(journey, version, createdAt);
    }

    private static java.util.List<String> ids(QuoteHistoryPage page) {
        return page.quotes().stream().map(quote -> quote.quoteVersionId()).toList();
    }
}
