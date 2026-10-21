package assignments.teaching;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Independent library-membership examples; none call renewal learner seams. */
class RenewalTeachingReferenceTest {
    record MembershipTerm(LocalDate start, LocalDate next, LocalDate opens) { }
    enum MemberState { ACTIVE, STOPPED }
    enum ReminderDecision { AVAILABLE, TOO_EARLY, TERM_ENDED, NOT_AVAILABLE }

    @Test void membershipAnniversaryIsNot365Days() {
        assertEquals(new MembershipTerm(LocalDate.parse("2023-07-01"), LocalDate.parse("2024-07-01"),
                LocalDate.parse("2024-06-17")), membershipTerm(LocalDate.parse("2023-07-01")));
        assertEquals(new MembershipTerm(LocalDate.parse("2028-02-29"), LocalDate.parse("2029-02-28"),
                LocalDate.parse("2029-02-14")), membershipTerm(LocalDate.parse("2028-02-29")));
        assertEquals(LocalDate.parse("2024-07-01"), LocalDate.parse("2023-07-01").plusYears(1));
        assertNotEquals(LocalDate.parse("2023-07-01").plusDays(365), LocalDate.parse("2024-07-01"));
    }

    @Test void membershipReminderChecksStateAndBothBounds() {
        var term = new MembershipTerm(LocalDate.parse("2023-07-01"), LocalDate.parse("2024-07-01"),
                LocalDate.parse("2024-06-17"));
        assertEquals(ReminderDecision.TOO_EARLY, reminder(MemberState.ACTIVE, term, LocalDate.parse("2024-06-16")));
        assertEquals(ReminderDecision.AVAILABLE, reminder(MemberState.ACTIVE, term, LocalDate.parse("2024-06-17")));
        assertEquals(ReminderDecision.AVAILABLE, reminder(MemberState.ACTIVE, term, LocalDate.parse("2024-06-30")));
        assertEquals(ReminderDecision.TERM_ENDED, reminder(MemberState.ACTIVE, term, LocalDate.parse("2024-07-01")));
        for (String date : new String[]{"2024-06-16", "2024-06-17", "2024-06-30", "2024-07-01"})
            assertEquals(ReminderDecision.NOT_AVAILABLE,
                    reminder(MemberState.STOPPED, term, LocalDate.parse(date)));
    }

    @Test void membershipNoticeCopiesSourceWithoutPricing() {
        var old = new MembershipFacts("m7", 4, "library-plan-v1", new BigDecimal("80.00"), "EUR",
                LocalDate.parse("2024-07-01"));
        var oldNotice = notice(old);
        var currentCatalog = new MembershipFacts("m7", 5, "library-plan-v2", new BigDecimal("95.00"), "EUR",
                LocalDate.parse("2024-07-01"));
        assertEquals(new MembershipNotice("m7", 4, "library-plan-v1", new BigDecimal("80.00"), "EUR",
                LocalDate.parse("2024-07-01"), "Reference information only; not a renewal offer."), oldNotice);
        assertEquals(new BigDecimal("95.00"), notice(currentCatalog).referenceFee());
        assertEquals(4, oldNotice.sourceVersion());
        assertEquals(new BigDecimal("80.00"), oldNotice.referenceFee());
    }

    @Test void scopedReadBindsValuesAndPreservesCompleteRows() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource ds = postgres.getPostgresDatabase();
            try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
                s.execute("CREATE TABLE toy_membership(id text PRIMARY KEY, owner_id text NOT NULL, version bigint NOT NULL, fee numeric(12,2) NOT NULL, currency char(3) NOT NULL)");
                s.execute("CREATE TABLE toy_event(id text, revision int, kind text, PRIMARY KEY(id, revision))");
                s.execute("CREATE TABLE toy_receipt(owner_id text, request_id text, fingerprint text, PRIMARY KEY(owner_id, request_id))");
                s.execute("INSERT INTO toy_membership VALUES ('m7','reader-a',4,80.00,'EUR'),('m8','reader-b',6,95.40,'EUR')");
                s.execute("INSERT INTO toy_event VALUES ('m7',4,'FEE_RECORDED'),('m8',6,'FEE_RECORDED')");
                s.execute("INSERT INTO toy_receipt VALUES ('reader-a','key-a','original-a'),('reader-b','key-b','original-b')");
            }
            var before = snapshot(ds);
            assertEquals(Optional.of(new MemberRead("m7", 4, new BigDecimal("80.00"), "EUR")), readMember(ds, "m7", "reader-a"));
            assertTrue(readMember(ds, "m7", "reader-b").isEmpty());
            assertTrue(readMember(ds, "missing", "reader-a").isEmpty());
            assertTrue(readMember(ds, "m7' OR '1'='1", "reader-a").isEmpty());
            assertEquals(Optional.of(new MemberRead("m8", 6, new BigDecimal("95.40"), "EUR")), readMember(ds, "m8", "reader-b"));
            assertEquals(before, snapshot(ds));

            try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
                s.executeUpdate("UPDATE toy_membership SET fee=81.00 WHERE id='m7'");
            }
            assertNotEquals(before, snapshot(ds), "the full ordered rows expose a same-count update");
        }
    }

    @Test void principalWinsAndAnonymousDoesNotRead() throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        MembershipInfoController controller = new MembershipInfoController((id, owner) -> {
            captured.set(id + "/" + owner); return Optional.empty();
        });
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(get("/teaching/membership/m7").header("X-Owner", "reader-a"))
                .andExpect(status().isUnauthorized());
        assertNull(captured.get());
        mvc.perform(get("/teaching/membership/m7").principal(() -> "reader-a")
                        .queryParam("owner", "reader-b").header("X-Owner", "reader-b"))
                .andExpect(status().isNotFound());
        assertEquals("m7/reader-a", captured.get());
    }

    @Test void unavailableOutcomeIsNotSuccessfulContent() {
        var response = unavailable();
        assertEquals(409, response.getStatusCode().value());
        assertEquals(Map.of("code", "REMINDER_UNAVAILABLE",
                "message", "Membership reminder is unavailable for this date or state."), response.getBody());
    }

    private static MembershipTerm membershipTerm(LocalDate start) {
        LocalDate next = start.plusYears(1);
        return new MembershipTerm(start, next, next.minusDays(14));
    }
    private static ReminderDecision reminder(MemberState state, MembershipTerm term, LocalDate today) {
        if (state != MemberState.ACTIVE) return ReminderDecision.NOT_AVAILABLE;
        if (today.isBefore(term.opens())) return ReminderDecision.TOO_EARLY;
        if (!today.isBefore(term.next())) return ReminderDecision.TERM_ENDED;
        return ReminderDecision.AVAILABLE;
    }
    record MembershipFacts(String id, long version, String plan, BigDecimal fee, String currency, LocalDate anniversary) { }
    record MembershipNotice(String id, long sourceVersion, String plan, BigDecimal referenceFee,
                            String currency, LocalDate anniversary, String notice) { }
    private static MembershipNotice notice(MembershipFacts facts) {
        return new MembershipNotice(facts.id(), facts.version(), facts.plan(), facts.fee(), facts.currency(),
                facts.anniversary(), "Reference information only; not a renewal offer.");
    }
    record MemberRead(String id, long version, BigDecimal fee, String currency) { }
    interface MembershipLookup { Optional<MemberRead> find(String id, String owner); }
    @RestController static class MembershipInfoController {
        private final MembershipLookup lookup;
        MembershipInfoController(MembershipLookup lookup) { this.lookup = lookup; }
        @GetMapping("/teaching/membership/{id}") ResponseEntity<?> read(@PathVariable("id") String id, java.security.Principal principal) {
            if (principal == null || principal.getName() == null || principal.getName().isBlank())
                return ResponseEntity.status(401).body(Map.of("code", "NO_IDENTITY"));
            return lookup.find(id, principal.getName()).<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(404).body(Map.of("code", "MEMBERSHIP_NOT_FOUND")));
        }
    }
    private static ResponseEntity<Map<String, String>> unavailable() {
        return ResponseEntity.status(409).body(Map.of("code", "REMINDER_UNAVAILABLE",
                "message", "Membership reminder is unavailable for this date or state."));
    }
    private static Optional<MemberRead> readMember(DataSource ds, String id, String trustedOwner) throws Exception {
        try (Connection c = ds.getConnection(); var p = c.prepareStatement(
                "SELECT id, version, fee, currency FROM toy_membership WHERE id = ? AND owner_id = ?")) {
            p.setString(1, id); p.setString(2, trustedOwner);
            try (var rows = p.executeQuery()) {
                if (!rows.next()) return Optional.empty();
                return Optional.of(new MemberRead(rows.getString("id"), rows.getLong("version"),
                        rows.getBigDecimal("fee"), rows.getString("currency")));
            }
        }
    }
    private static Map<String, String> snapshot(DataSource ds) throws Exception {
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            return Map.of("members", jsonRows(s, "toy_membership", "id"),
                    "events", jsonRows(s, "toy_event", "id,revision"),
                    "receipts", jsonRows(s, "toy_receipt", "owner_id,request_id"));
        }
    }
    private static String jsonRows(Statement s, String table, String order) throws Exception {
        try (ResultSet rows = s.executeQuery("SELECT COALESCE(jsonb_agg(to_jsonb(t) ORDER BY " + order + "),'[]'::jsonb)::text FROM " + table + " t")) {
            assertTrue(rows.next()); return rows.getString(1);
        }
    }
}
