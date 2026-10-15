package capstone.coverstory.application;

import coverstory.modules.payments.api.OfferPaymentFactsPort;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/** PostgreSQL-backed fixed oracle; it does not implement the main learner validator. */
class CAP115InstructorInfrastructureTest {
    @Test void sourceAndQuoteHashesAreFixedAndChangedTermsAndPaiseAreExact() throws Exception {
        var fixture=Path.of("docs/coverstory/fixtures/cap-115.json");
        var json=new tools.jackson.databind.ObjectMapper().readTree(Files.readString(fixture));
        assertEquals("CAP-115-synthetic-v1",json.path("fixture_id").asText());
        assertEquals("SYNTHETIC_ONLY",json.path("classification").asText());
        assertEquals(CAP115TestDatabase.ORIGINAL_HASH,json.path("original_offer").path("content_sha256").asText());
        assertEquals(CAP115TestDatabase.REVISED_HASH,json.path("revised_offer").path("content_sha256").asText());
        assertEquals(CAP115TestDatabase.ORIGINAL_HASH,contentHash(json.path("original_offer")));
        assertEquals(CAP115TestDatabase.REVISED_HASH,contentHash(json.path("revised_offer")));
        var db=CAP115TestDatabase.fresh();
        try(db) {
            var adapter=new CAP115ApplicationFactsAdapter(db.dataSource());
            var pair=adapter.findCurrentPair(CAP115TestDatabase.CASE);
            assertEquals(100_000,pair.original().amountMinor());assertEquals(110_000,pair.revised().amountMinor());
            assertEquals(10_000,pair.revised().amountMinor()-pair.original().amountMinor());
            assertEquals("INR",pair.original().currency());assertEquals("INR",pair.revised().currency());
            assertEquals("2027-01-20T00:00:00Z",pair.revised().expiresAt().toString());
            var diff=new java.util.HashSet<>(pair.original().termCodes());diff.removeAll(pair.revised().termCodes());
            var added=new java.util.HashSet<>(pair.revised().termCodes());added.removeAll(pair.original().termCodes());diff.addAll(added);
            assertEquals(java.util.Set.of("copay_10_percent","copay_0_percent","restore_benefit"),diff);
        }
    }

    @Test void answerOracleRejectsForeignStaleMissingAndUnknownClaimsAndKeepsAbstentionReadOnly() throws Exception {
        try(var db=CAP115TestDatabase.fresh()) {
            var app=new CAP115ApplicationFactsAdapter(db.dataSource());
            var pair=app.findCurrentPair(CAP115TestDatabase.CASE);
            var evidence=new CAP115SupportEvidenceAdapter(db.dataSource()).authorizedFor(CAP115TestDatabase.OWNER_A);
            var oracle=new CAP115ReferenceAnswerValidator();
            assertEquals("SUPPORTED",oracle.validate(pair,evidence,"supported").code());
            assertEquals("UNAUTHORIZED_CITATION",oracle.validate(pair,evidence,"foreign-source").code());
            assertEquals("UNKNOWN_FIELD",oracle.validate(pair,evidence,"invented-field").code());
            assertEquals("MISSING_STATUS",oracle.validate(pair,evidence,"missing-status").code());
            assertEquals("STALE_OR_FOREIGN_REVISION",oracle.validate(pair,evidence,"stale-hash").code());
            var abstention=oracle.validate(pair,evidence,"insufficient");
            assertTrue(abstention.valid());assertEquals("INSUFFICIENT_EVIDENCE",abstention.code());assertTrue(abstention.authorizedCitations().isEmpty());
            var payments=new CAP115PaymentsFactsAdapter().findForOffer(pair.revised().offerId());
            assertEquals("NOT_STARTED",payments.status());assertFalse(payments.dispatchConfirmed());
            assertEquals(0,new JdbcTemplate(db.dataSource()).queryForObject("SELECT count(*) FROM cap115_harness.explicit_quote_choice",Integer.class));
        }
    }

    @Test void explicitChoiceUsesCurrentRevisionAndNeverCreatesPaymentRows() throws Exception {
        try(var db=CAP115TestDatabase.fresh()) {
            var pair=new CAP115ApplicationFactsAdapter(db.dataSource()).findCurrentPair(CAP115TestDatabase.CASE);
            var choices=new CAP115ChoiceStore(db.dataSource());
            assertEquals(1,choices.record(CAP115TestDatabase.OWNER_A,pair.revised(),"ACCEPT_OFFER"));
            assertEquals(0,choices.record(CAP115TestDatabase.OWNER_A,pair.revised(),"DECLINE_OFFER"));
            assertEquals(1,choices.count());
            assertEquals("NOT_STARTED",new CAP115PaymentsFactsAdapter().findForOffer(pair.revised().offerId()).status());
            assertEquals(0,new JdbcTemplate(db.dataSource()).queryForObject("SELECT count(*) FROM information_schema.tables WHERE table_schema='cap115_harness' AND table_name LIKE '%payment%'",Integer.class));
        }
    }

    private static String contentHash(tools.jackson.databind.JsonNode offer) throws Exception {
        Map<String,Object> canonical=new TreeMap<>();
        canonical.put("amount_minor",offer.path("amount_minor").asLong());
        canonical.put("currency",offer.path("currency").asText());
        canonical.put("expires_at",offer.path("expires_at").asText());
        canonical.put("offer_id",offer.path("offer_id").asText());
        canonical.put("owner_id",offer.path("owner_id").asText());
        canonical.put("revision",offer.path("revision").asText());
        var terms=new ArrayList<String>();offer.path("term_codes").forEach(term->terms.add(term.asText()));canonical.put("term_codes",terms);
        String bytes=new tools.jackson.databind.ObjectMapper().writeValueAsString(canonical);
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes.getBytes(StandardCharsets.UTF_8)));
    }
}
