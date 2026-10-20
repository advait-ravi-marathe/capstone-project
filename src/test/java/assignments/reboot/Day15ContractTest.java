package assignments.reboot;

import assignments.insurance.JourneySummary;
import assignments.insurance.JourneySummaryQuery;
import assignments.insurance.QuoteHistoryQuery;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Real PostgreSQL read-model contract with fixed owners and rows, bypassing prior services. */
@Tag("learnerAcceptance")
class Day15ContractTest {
    private static EmbeddedPostgres postgres;
    private static DataSource dataSource;
    private JourneySummaryQuery query;

    @BeforeAll
    static void startPostgresAndInstallFixedSchema() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        dataSource = postgres.getPostgresDatabase();
        try (Connection connection = dataSource.getConnection(); Statement sql = connection.createStatement()) {
            sql.execute("CREATE TABLE journeys (journey_id TEXT PRIMARY KEY, owner_id TEXT NOT NULL)");
            sql.execute("""
                    CREATE TABLE quote_versions (
                    journey_id TEXT NOT NULL, quote_version_id TEXT NOT NULL, product_code TEXT NOT NULL,
                    product_version TEXT NOT NULL, rate_version TEXT NOT NULL, rebuild_amount_rupees NUMERIC(14,2) NOT NULL,
                    coverage_code TEXT NOT NULL, currency TEXT NOT NULL, annual_premium_rupees NUMERIC(14,2) NOT NULL,
                    created_at TIMESTAMPTZ NOT NULL, expires_at TIMESTAMPTZ NOT NULL,
                    PRIMARY KEY (journey_id, quote_version_id))""");
            sql.execute("CREATE TABLE selections (journey_id TEXT NOT NULL, quote_version_id TEXT NOT NULL, selection_order INTEGER NOT NULL)");
        }
    }

    @AfterAll
    static void closePostgres() throws IOException { if (postgres != null) postgres.close(); }

    @BeforeEach
    void resetRows() throws Exception {
        query = new JourneySummaryQuery(dataSource);
        try (Connection connection = dataSource.getConnection(); Statement sql = connection.createStatement()) {
            sql.execute("TRUNCATE selections, quote_versions, journeys");
            sql.execute("INSERT INTO journeys VALUES ('journey-A', 'owner-A'), ('journey-B', 'owner-B')");
            sql.execute("""
                    INSERT INTO quote_versions VALUES
                    ('journey-A','v1','HOME_SECURE','hs-2026-01','rate-2026-01',100000.00,'STANDARD','INR',1250.25,'2026-10-20T10:00:00Z','2026-10-20T10:15:00Z'),
                    ('journey-A','v2','HOME_SECURE','hs-2026-01','rate-2026-01',110000.00,'STANDARD','INR',1350.25,'2026-10-20T09:00:00Z','2026-10-20T09:15:00Z'),
                    ('journey-A','v3','HOME_SECURE','hs-2026-01','rate-2026-01',120000.00,'STANDARD','INR',1450.25,'2026-10-20T08:00:00Z','2026-10-20T08:15:00Z')
                    """);
            sql.execute("INSERT INTO selections VALUES ('journey-A','v1',2), ('journey-A','v2',0), ('journey-A','v3',1)");
        }
    }

    @Test
    void emptyJourneySummaryHasNullQuoteAndEmptySelections() throws Exception {
        Optional<JourneySummary> summary = query.find("journey-B", "owner-B");
        assertEquals(Optional.of(new JourneySummary("journey-B", null, java.util.List.of())), summary);
    }

    @Test
    void returnsOrderedSelectionsAndAllowlistedPublicProjection() throws Exception {
        JourneySummary summary = query.find("journey-A", "owner-A").orElseThrow();
        assertEquals(java.util.List.of(0, 1, 2), summary.selections().stream()
                .map(item -> item.selectionOrder()).toList());
        assertEquals("v1", summary.quote().quoteVersionId());
        assertEquals(java.util.List.of("v2", "v3", "v1"), summary.selections().stream()
                .map(item -> item.quoteVersionId()).toList());
        var json = new ObjectMapper().valueToTree(summary);
        assertEquals(java.util.Set.of("journeyId", "quote", "selections"), fieldNames(json));
        assertEquals(java.util.Set.of("quoteVersionId", "productCode", "productVersion", "rateVersion",
                "rebuildAmountRupees", "coverageCode", "currency", "annualPremiumRupees", "createdAt", "expiresAt"),
                fieldNames(json.get("quote")));
        assertFalse(json.toString().contains("ownerId"));
    }

    @Test
    void unknownAndOtherOwnedJourneysAreIndistinguishableAbsence() throws Exception {
        assertEquals(Optional.empty(), query.find("missing", "owner-A"));
        assertEquals(Optional.empty(), query.find("journey-A", "owner-B"));
    }

    private static java.util.Set<String> fieldNames(tools.jackson.databind.JsonNode node) {
        java.util.Set<String> names = new java.util.HashSet<>();
        node.propertyNames().forEach(names::add);
        return names;
    }
}
