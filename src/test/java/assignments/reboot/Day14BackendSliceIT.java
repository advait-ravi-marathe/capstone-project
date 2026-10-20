package assignments.reboot;

import assignments.insurance.HomeSecureCatalogFixtureLoader;
import assignments.insurance.HomeSecureEligibilityPolicy;
import assignments.insurance.JourneyAccessPolicy;
import assignments.insurance.JourneyReadRequest;
import assignments.insurance.PremiumCalculator;
import assignments.insurance.ProductCatalogService;
import assignments.insurance.QuoteExpiredException;
import assignments.insurance.QuoteFreshnessPolicy;
import assignments.insurance.QuoteFreshnessState;
import assignments.insurance.QuoteRepositoryAdapter;
import assignments.insurance.QuoteSelectionFreshnessGuard;
import assignments.insurance.QuoteSelectionInput;
import assignments.insurance.QuoteSelectionKey;
import assignments.insurance.QuoteSelectionPolicy;
import assignments.insurance.QuoteVersionStaleException;
import assignments.insurance.ResourceNotFoundException;
import assignments.insurance.QuoteVersionService;
import assignments.insurance.SelectionService;
import assignments.insurance.UnauthenticatedException;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cumulative integration path: actual fixture, learner components, and disposable PostgreSQL. */
@Tag("learnerAcceptance")
class Day14BackendSliceIT {
    private static final Instant NOW = Instant.parse("2026-10-20T07:00:00Z");

    @Test
    void createsPersistsAndReadsHomeSecureQuoteThroughRealCollaborators() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource db = postgres.getPostgresDatabase();
            String ddl = Files.readString(Path.of("src/main/resources/db/migration/V001__homesecure_quotes.sql"));
            try (Connection connection = db.getConnection(); Statement statement = connection.createStatement()) {
                for (String sql : ddl.split(";")) if (!sql.isBlank()) statement.execute(sql);
                statement.execute("ALTER TABLE journeys ADD COLUMN owner_id TEXT");
                statement.execute("INSERT INTO journeys (journey_id, owner_id) VALUES ('journey-d14', 'owner-a')");
            }

            var catalog = new ProductCatalogService(new HomeSecureCatalogFixtureLoader().load());
            var product = catalog.findByCode("homesecure").orElseThrow();
            var eligibility = new HomeSecureEligibilityPolicy(
                    "OWNER_OCCUPIED", new BigDecimal("100000.00"), new BigDecimal("10000000.00"));
            var calculator = new PremiumCalculator(new BigDecimal("0.00125"), Map.of(
                    "BUILDING", new BigDecimal("0.00"), "CONTENTS", new BigDecimal("125.00")));
            var versionService = new QuoteVersionService(product.productCode(), product.productVersion(),
                    "training-rate-v1", product.currency(), Duration.ofMinutes(15), eligibility, calculator,
                    Clock.systemUTC(), () -> "quote-d14-v1");
            var quote = versionService.createQuote(new assignments.insurance.QuoteCreationInput(
                    "OWNER_OCCUPIED", new BigDecimal("100000.00"), "BUILDING"));
            var quoteRepository = new QuoteRepositoryAdapter(db);
            quoteRepository.insert("journey-d14", quote);
            new JourneyAccessPolicy().assertCanAccess(() -> "owner-a", "owner-a",
                    new JourneyReadRequest("journey-d14", "forged-owner"));
            new QuoteFreshnessPolicy().assertSelectable(new QuoteFreshnessState(
                    "journey-d14", "journey-d14", quote.quoteVersionId(), quote.quoteVersionId(), quote.expiresAt()),
                    Clock.systemUTC().instant());
            new SelectionService(db).persistBatch(java.util.List.of(
                    new assignments.insurance.QuoteSelectionKey("journey-d14", "quote-d14-v1")));
            var persisted = quoteRepository.find("journey-d14", "quote-d14-v1").orElseThrow();
            assertNotNull(persisted);
            assertEquals("quote-d14-v1", persisted.quoteVersionId());
            assertEquals("home-secure-v1", persisted.productVersion());
            assertEquals(new BigDecimal("125.00"), persisted.annualPremiumRupees());
            try (Connection connection = db.getConnection();
                 var statement = connection.prepareStatement("SELECT journey_id, quote_version_id, selection_order FROM selections WHERE journey_id = ? ORDER BY selection_order")) {
                statement.setString(1, "journey-d14");
                try (var rows = statement.executeQuery()) {
                    assertTrue(rows.next(), "the previous-day selection writer must persist the selected quote");
                    assertEquals("journey-d14", rows.getString("journey_id"));
                    assertEquals("quote-d14-v1", rows.getString("quote_version_id"));
                    assertEquals(0, rows.getInt("selection_order"));
                    assertFalse(rows.next(), "the fixed slice writes exactly one selection");
                }
            }
        }
    }

    @Test
    void duplicateCompositePairIsCollapsedBeforePersistence() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource db = postgres.getPostgresDatabase();
            initializeDatabase(db, "journey-duplicate");
            new QuoteRepositoryAdapter(db).insert("journey-duplicate", quoteService().createQuote(
                    new assignments.insurance.QuoteCreationInput("OWNER_OCCUPIED", new BigDecimal("100000.00"), "BUILDING")));
            var duplicates = List.of(
                    new QuoteSelectionInput("journey-duplicate", "quote-d14-v1"),
                    new QuoteSelectionInput("journey-duplicate", "quote-d14-v1"));
            var unique = new QuoteSelectionPolicy().uniqueSelections(duplicates);
            new SelectionService(db).persistBatch(unique);
            assertEquals(1, countSelections(db, "journey-duplicate"));
        }
    }

    @Test
    void invalidQuoteInputDoesNotCreateOrPersistSnapshot() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource db = postgres.getPostgresDatabase();
            initializeDatabase(db, "journey-invalid");
            var service = quoteService();
            assertThrows(IllegalArgumentException.class, () -> service.createQuote(
                    new assignments.insurance.QuoteCreationInput("RENTED", new BigDecimal("99999.00"), "BUILDING")));
            assertEquals(0, countQuotes(db, "journey-invalid"));
        }
    }

    @Test
    void expiredAndSupersededQuotesAreRejectedBeforePostgresWrite() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource db = postgres.getPostgresDatabase();
            initializeDatabase(db, "journey-d14");
            var repository = new QuoteRepositoryAdapter(db);
            repository.insert("journey-d14", quoteSnapshot("quote-v1", NOW));
            repository.insert("journey-d14", quoteSnapshot("quote-v2", NOW.plusSeconds(60)));
            var writer = writeSelection(db, new QuoteSelectionKey("journey-d14", "quote-v1"));
            var guard = new QuoteSelectionFreshnessGuard(new QuoteFreshnessPolicy());
            var expired = new QuoteFreshnessState("journey-d14", "journey-d14", "quote-v1", "quote-v1", NOW);
            assertThrows(QuoteExpiredException.class, () -> guard.select(expired, NOW, writer));
            assertEquals(0, countSelections(db, "journey-d14"));

            var stale = new QuoteFreshnessState("journey-d14", "journey-d14", "quote-v1", "quote-v2", NOW.plusSeconds(60));
            assertThrows(QuoteVersionStaleException.class, () -> guard.select(stale, NOW, writer));
            assertEquals(0, countSelections(db, "journey-d14"));
        }
    }

    @Test
    void wrongJourneyIsNonDisclosingAndRejectedBeforePostgresWrite() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource db = postgres.getPostgresDatabase();
            initializeDatabase(db, "journey-d14");
            insertJourney(db, "journey-other", "owner-b");
            new QuoteRepositoryAdapter(db).insert("journey-other", quoteSnapshot("quote-other", NOW.plusSeconds(60)));
            var crossJourney = new QuoteFreshnessState("journey-d14", "journey-other", "quote-other", "quote-other", NOW.plusSeconds(60));
            var guard = new QuoteSelectionFreshnessGuard(new QuoteFreshnessPolicy());
            assertThrows(ResourceNotFoundException.class, () -> guard.select(crossJourney, NOW,
                    writeSelection(db, new QuoteSelectionKey("journey-d14", "quote-other"))));
            assertEquals(0, countSelections(db, "journey-d14"));
        }
    }

    @Test
    void ownershipRejectsAnonymousAndForgedOtherOwnerBeforePostgresWrite() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource db = postgres.getPostgresDatabase();
            initializeDatabase(db, "journey-d14");
            new QuoteRepositoryAdapter(db).insert("journey-d14", quoteSnapshot("quote-d14-v1", NOW.plusSeconds(60)));
            var policy = new JourneyAccessPolicy();
            var request = new JourneyReadRequest("journey-d14", "owner-a");
            var key = new QuoteSelectionKey("journey-d14", "quote-d14-v1");
            assertThrows(UnauthenticatedException.class, () -> {
                policy.assertCanAccess(null, "owner-a", request);
                new SelectionService(db).persistBatch(List.of(key));
            });
            assertEquals(0, countSelections(db, "journey-d14"));
            assertThrows(ResourceNotFoundException.class, () -> {
                policy.assertCanAccess(() -> "owner-b", "owner-a", request);
                new SelectionService(db).persistBatch(List.of(key));
            });
            assertEquals(0, countSelections(db, "journey-d14"));
        }
    }

    private static void initializeDatabase(DataSource db, String journeyId) throws Exception {
        String ddl = Files.readString(Path.of("src/main/resources/db/migration/V001__homesecure_quotes.sql"));
        try (Connection connection = db.getConnection(); Statement statement = connection.createStatement()) {
            for (String sql : ddl.split(";")) if (!sql.isBlank()) statement.execute(sql);
            statement.execute("ALTER TABLE journeys ADD COLUMN owner_id TEXT");
            statement.execute("INSERT INTO journeys (journey_id, owner_id) VALUES ('" + journeyId + "', 'owner-a')");
        }
    }

    private static void insertJourney(DataSource db, String journeyId, String ownerId) throws Exception {
        try (Connection connection = db.getConnection(); var statement = connection.prepareStatement("INSERT INTO journeys (journey_id, owner_id) VALUES (?, ?)")) {
            statement.setString(1, journeyId);
            statement.setString(2, ownerId);
            statement.executeUpdate();
        }
    }

    private static assignments.insurance.QuoteSnapshot quoteSnapshot(String id, Instant expiresAt) {
        return new assignments.insurance.QuoteSnapshot(id, "homesecure", "home-secure-v1", "training-rate-v1",
                new BigDecimal("100000.00"), "BUILDING", "INR", new BigDecimal("125.00"),
                NOW.minusSeconds(60), expiresAt);
    }

    private static Runnable writeSelection(DataSource db, QuoteSelectionKey key) {
        return () -> {
            try { new SelectionService(db).persistBatch(List.of(key)); }
            catch (java.sql.SQLException failure) { throw new IllegalStateException(failure); }
        };
    }

    private static QuoteVersionService quoteService() {
        return new QuoteVersionService("homesecure", "home-secure-v1", "training-rate-v1", "INR",
                Duration.ofMinutes(15),
                new HomeSecureEligibilityPolicy("OWNER_OCCUPIED", new BigDecimal("100000.00"), new BigDecimal("10000000.00")),
                new PremiumCalculator(new BigDecimal("0.00125"), Map.of("BUILDING", BigDecimal.ZERO)),
                Clock.fixed(NOW, java.time.ZoneOffset.UTC), () -> "quote-d14-v1");
    }

    private static int countQuotes(DataSource db, String journeyId) throws Exception {
        try (Connection connection = db.getConnection(); var statement = connection.prepareStatement("SELECT count(*) FROM quote_versions WHERE journey_id=?")) {
            statement.setString(1, journeyId);
            try (var rows = statement.executeQuery()) { rows.next(); return rows.getInt(1); }
        }
    }

    private static int countSelections(DataSource db, String journeyId) throws Exception {
        try (Connection connection = db.getConnection(); var statement = connection.prepareStatement("SELECT count(*) FROM selections WHERE journey_id=?")) {
            statement.setString(1, journeyId);
            try (var rows = statement.executeQuery()) { rows.next(); return rows.getInt(1); }
        }
    }
}
