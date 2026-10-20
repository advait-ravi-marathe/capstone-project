package assignments.reboot;

import assignments.insurance.HomeSecureCatalogFixtureLoader;
import assignments.insurance.HomeSecureEligibilityPolicy;
import assignments.insurance.JourneySummaryQuery;
import assignments.insurance.PremiumCalculator;
import assignments.insurance.ProductCatalogService;
import assignments.insurance.QuoteRepositoryAdapter;
import assignments.insurance.QuoteVersionService;
import assignments.insurance.SelectionService;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Cumulative integration path: actual fixture, learner components, and disposable PostgreSQL. */
@Tag("learnerAcceptance")
class Day14BackendSliceIT {
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
            new SelectionService(db).persistBatch(java.util.List.of(
                    new assignments.insurance.QuoteSelectionKey("journey-d14", "quote-d14-v1")));
            var persisted = quoteRepository.find("journey-d14", "quote-d14-v1").orElseThrow();
            var summary = new JourneySummaryQuery(db).find("journey-d14", "owner-a").orElseThrow();

            assertNotNull(persisted);
            assertEquals("quote-d14-v1", persisted.quoteVersionId());
            assertNotNull(summary);
            assertEquals("journey-d14", summary.journeyId());
            assertEquals(java.util.List.of("quote-d14-v1"), summary.selections().stream()
                    .map(selection -> selection.quoteVersionId()).toList());
        }
    }
}
