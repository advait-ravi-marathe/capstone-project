package capstone.coverstory.payments;

import capstone.coverstory.payments.internal.AcceptedRevisionBalance.*;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Frozen synthetic inputs and independent calculator checks; none invokes a payment action. */
class CAP116InstructorInfrastructureTest {
    private final tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
    private tools.jackson.databind.JsonNode fixture() throws Exception {
        return mapper.readTree(Files.readString(Path.of("docs/coverstory/fixtures/cap-116.json")));
    }
    @Test void canonicalFixtureAndOfferRevisionHashesAreRecomputed() throws Exception {
        var root = fixture();
        assertEquals("CAP-116-synthetic-v1", root.path("fixture_id").asText());
        assertEquals("SYNTHETIC_ONLY", root.path("classification").asText());
        assertEquals("46428e99560b155258523f0ba33e9437cb036ced7a907e167b03485ceede1b45", root.path("source_contract_sha256").asText());
        assertHash(root.path("historical_original_offer"));
        for (var scenario : root.path("scenarios")) assertHash(scenario.path("accepted_offer"));
    }

    @Test void acceptedH2ProducesOnlyOneEligibleAdditionalPaymentDifference() throws Exception {
        var scenario = scenario("accepted-h2");
        var result = calculator().calculate(offer(scenario.path("accepted_offer")), effects(scenario.path("effects")), List.of());
        assertEquals(100_000, result.confirmedNetMinor()); assertEquals(10_000, result.adjustmentMinor());
        assertEquals(NextAction.CREATE_ADDITIONAL_PAYMENT, result.nextAction()); assertTrue(result.discrepancyCodes().isEmpty());
        assertEquals("o2", result.acceptedRevision()); assertEquals(scenario.path("accepted_offer").path("content_sha256").asText(), result.acceptedContentSha256());
    }

    @Test void pendingBalanceReservesTheOldRevisionAndBlocksASecondAdjustment() throws Exception {
        var scenario = scenario("pending-h2-new-h3");
        var result = calculator().calculate(offer(scenario.path("accepted_offer")), effects(scenario.path("effects")), pending(scenario.path("pending_allocations")));
        assertEquals(100_000, result.confirmedNetMinor()); assertEquals(-10_000, result.adjustmentMinor());
        assertEquals(10_000, result.pendingAdditionalMinor()); assertEquals(NextAction.WAIT_OR_RECONCILE, result.nextAction());
        var unstarted = pending(scenario.path("pending_allocations")).stream().map(p -> new PendingAllocation(p.allocationId(),p.ownerId(),p.applicationId(),p.idempotencyKey(),p.offerId(),p.revision(),p.contentSha256(),p.kind(),AllocationStatus.UNSTARTED,p.amountMinor(),p.currency())).toList();
        assertEquals(NextAction.WAIT_OR_RECONCILE, calculator().calculate(offer(scenario.path("accepted_offer")),effects(scenario.path("effects")),unstarted).nextAction());
    }

    @Test void duplicateEffectReceiptCountsOnceAndForeignOwnerDoesNotInflateNet() throws Exception {
        var scenario = scenario("duplicate-foreign-orphan-mismatch");
        var result = calculator().calculate(offer(scenario.path("accepted_offer")),effects(scenario.path("effects")),List.of());
        assertEquals(100_000, result.confirmedNetMinor());
        assertEquals(NextAction.REVIEW_DISCREPANCY, result.nextAction());
        assertEquals(java.util.Set.of("CURRENCY_MISMATCH","ORPHAN_EFFECT_OBSERVATION"),result.discrepancyCodes());
    }

    @Test void confirmedRefundIsNettedAndCompletedAllocationIsNotReserved() throws Exception {
        var scenario = scenario("h3-confirmed-refund");
        var completed = new PendingAllocation(java.util.UUID.fromString("11600000-0000-4000-8000-000000000399"),offer(scenario.path("accepted_offer")).ownerId(),offer(scenario.path("accepted_offer")).applicationId(),"done",offer(scenario.path("accepted_offer")).offerId(),offer(scenario.path("accepted_offer")).revision(),offer(scenario.path("accepted_offer")).contentSha256(),AllocationKind.REFUND,AllocationStatus.CONFIRMED,10_000,"INR");
        var result = calculator().calculate(offer(scenario.path("accepted_offer")),effects(scenario.path("effects")),List.of(completed));
        assertEquals(90_000,result.confirmedNetMinor()); assertEquals(0,result.pendingRefundMinor()); assertEquals(NextAction.NO_ADJUSTMENT,result.nextAction());
    }

    private tools.jackson.databind.JsonNode scenario(String id) throws Exception {
        for (var scenario : fixture().path("scenarios")) if (id.equals(scenario.path("id").asText())) return scenario;
        throw new IllegalArgumentException("Missing fixture scenario " + id);
    }
    private CAP116ReferenceBalanceCalculator calculator() { return new CAP116ReferenceBalanceCalculator(); }
    private AcceptedOffer offer(tools.jackson.databind.JsonNode n) {
        return new AcceptedOffer(uuid(n,"owner_id"),uuid(n,"application_id"),uuid(n,"offer_id"),n.path("revision").asText(),n.path("content_sha256").asText(),n.path("total_minor").asLong(),n.path("currency").asText(),n.path("accepted").asBoolean());
    }
    private List<ConfirmedEffect> effects(tools.jackson.databind.JsonNode arr) {
        List<ConfirmedEffect> result = new ArrayList<>();
        for (var n : arr) result.add(new ConfirmedEffect(n.path("effect_id").isNull()?null:uuid(n,"effect_id"),uuid(n,"owner_id"),uuid(n,"application_id"),n.path("provider_reference").asText(),EffectKind.valueOf(n.path("kind").asText()),n.path("amount_minor").asLong(),n.path("currency").asText(),uuid(n,"offer_id"),n.path("revision").asText(),n.path("content_sha256").asText(),n.path("attached_to_authoritative_effect").asBoolean()));
        return result;
    }
    private List<PendingAllocation> pending(tools.jackson.databind.JsonNode arr) {
        List<PendingAllocation> result = new ArrayList<>();
        for (var n : arr) result.add(new PendingAllocation(uuid(n,"allocation_id"),uuid(n,"owner_id"),uuid(n,"application_id"),n.path("idempotency_key").asText(),uuid(n,"offer_id"),n.path("revision").asText(),n.path("content_sha256").asText(),AllocationKind.valueOf(n.path("kind").asText()),AllocationStatus.valueOf(n.path("status").asText()),n.path("amount_minor").asLong(),n.path("currency").asText()));
        return result;
    }
    private void assertHash(tools.jackson.databind.JsonNode n) throws Exception {
        var terms=new ArrayList<String>(); n.path("term_codes").forEach(t->terms.add(t.asText())); terms.sort(Comparator.naturalOrder());
        String canonical=String.join("|",n.path("owner_id").asText(),n.path("application_id").asText(),n.path("offer_id").asText(),n.path("revision").asText(),Long.toString(n.path("total_minor").asLong()),n.path("currency").asText(),String.join(",",terms));
        String actual=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8)));
        assertEquals(actual,n.path("content_sha256").asText());
    }
    private static java.util.UUID uuid(tools.jackson.databind.JsonNode n,String field){return java.util.UUID.fromString(n.path(field).asText());}
}
