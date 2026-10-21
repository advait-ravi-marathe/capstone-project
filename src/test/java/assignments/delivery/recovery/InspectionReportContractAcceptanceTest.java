package assignments.delivery.recovery;

import assignments.delivery.IssuanceEnvelope;
import assignments.delivery.IssuanceSourceKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class InspectionReportContractAcceptanceTest {
    private final RecoveryDecisionPolicy decisions = new RecoveryDecisionPolicy();
    private final InspectionReportMapper mapper = new InspectionReportMapper(decisions);
    private final InspectionReportCodec codec = new InspectionReportCodec(new ObjectMapper());

    @Test void day142ProjectsObservedWorkAndSupplementaryHistory() {
        var key = new IssuanceSourceKey("quoted-\"policy", 3);
        var envelope = new IssuanceEnvelope(key.policyId(), 3, "ISSUED", LocalDate.parse("2026-12-01"), 1);
        var observation = new RecoveryObservation(key, true, envelope, 2, null, Instant.parse("2026-10-01T00:00:00Z"), RecoveryReason.MANUAL_HOLD, ReceiptRelation.ABSENT);
        var history = List.of(new RecoveryHistoryEntry(1, RecoveryAction.QUARANTINE, RecoveryReason.MANUAL_HOLD, Instant.parse("2026-10-01T00:00:00Z")));
        var report = mapper.map(new RecoveryReadResult(observation, history));
        assertEquals(1, report.schemaVersion());
        assertEquals(key.policyId(), report.key().policyId());
        assertEquals(key.sourceVersion(), report.key().sourceVersion());
        assertTrue(report.workPresent());
        assertEquals(key.policyId(), report.envelope().policyId());
        assertEquals(3, report.envelope().sourceVersion());
        assertEquals("ISSUED", report.envelope().eventType());
        assertEquals(LocalDate.parse("2026-12-01"), report.envelope().effectiveDate());
        assertEquals(1, report.envelope().schemaVersion());
        assertEquals(2, report.controlRevision());
        assertNull(report.acknowledgedAt());
        assertEquals(observation.quarantinedAt(), report.quarantinedAt());
        assertEquals(RecoveryReason.MANUAL_HOLD, report.quarantineReason());
        assertEquals(ReceiptRelation.ABSENT, report.receiptRelation());
        assertEquals(RecoveryRecommendation.QUARANTINED_NO_RECEIPT, report.recommendation());
        assertTrue(report.historySupplementary());
        assertEquals(history.size(), report.history().size());
        assertEquals(1, report.history().getFirst().controlRevision());
        assertEquals(RecoveryAction.QUARANTINE, report.history().getFirst().action());
        assertEquals(RecoveryReason.MANUAL_HOLD, report.history().getFirst().reason());
        assertEquals(history.getFirst().decidedAt(), report.history().getFirst().decidedAt());
    }

    @Test void day142CapsOrderedHistoryAtTwentyAndPreservesReceiptEvidence() {
        var key=new IssuanceSourceKey("policy-history",1);
        var envelope=new IssuanceEnvelope(key.policyId(),1,"ISSUED",LocalDate.parse("2026-12-01"),1);
        var observation=new RecoveryObservation(key,true,envelope,4,null,null,null,ReceiptRelation.MATCHING);
        var history=java.util.stream.IntStream.rangeClosed(1,20).mapToObj(i->new RecoveryHistoryEntry(i,
                i%2==0?RecoveryAction.RELEASE:RecoveryAction.QUARANTINE,
                i%2==0?RecoveryReason.REVIEWED_FOR_RETRY:RecoveryReason.MANUAL_HOLD,
                Instant.parse("2026-10-01T00:00:00Z").plusSeconds(i))).toList();
        var report=mapper.map(new RecoveryReadResult(observation,history));
        assertEquals(20,report.history().size(), "the typed inspection input is already bounded to its first twenty entries");
        assertEquals(RecoveryRecommendation.RECONCILE_ACKNOWLEDGMENT,report.recommendation());
        assertEquals(1,report.history().getFirst().controlRevision());
        assertEquals(20,report.history().getLast().controlRevision());
    }

    @Test void day142ProjectsMissingMatchingAndConflictingReceiptStatesThroughClassifier() {
        var key = new IssuanceSourceKey("policy-states", 8);
        var envelope = new IssuanceEnvelope(key.policyId(), key.sourceVersion(), "ISSUED", LocalDate.parse("2026-12-01"), 1);
        var states = List.of(
                RecoveryObservation.notFound(key),
                new RecoveryObservation(key, true, envelope, 0, null, null, null, ReceiptRelation.ABSENT),
                new RecoveryObservation(key, true, envelope, 0, null, null, null, ReceiptRelation.MATCHING),
                new RecoveryObservation(key, true, envelope, 0, null, null, null, ReceiptRelation.CONFLICTING));
        for (var observation : states) {
            var report = mapper.map(new RecoveryReadResult(observation, List.of()));
            assertEquals(observation.key().policyId(), report.key().policyId());
            assertEquals(observation.key().sourceVersion(), report.key().sourceVersion());
            assertEquals(observation.workPresent(), report.workPresent());
            assertEquals(observation.envelope() == null ? null : observation.envelope().eventType(),
                    report.envelope() == null ? null : report.envelope().eventType());
            assertEquals(observation.controlRevision(), report.controlRevision());
            assertEquals(observation.acknowledgedAt(), report.acknowledgedAt());
            assertEquals(observation.quarantinedAt(), report.quarantinedAt());
            assertEquals(observation.quarantineReason(), report.quarantineReason());
            assertEquals(observation.receiptRelation(), report.receiptRelation());
            assertEquals(decisions.classify(observation).recommendation(), report.recommendation());
            assertTrue(report.historySupplementary());
            assertEquals(List.of(), report.history());
        }
    }

    @Test void day143EncodesExactlyOneEscapedVersionedRecord() throws Exception {
        var report = report("政策-\nA", 4, RecoveryRecommendation.ELIGIBLE_FOR_ONE_ATTEMPT);
        String frame = codec.encode(report);
        assertTrue(frame.startsWith(InspectionReportCodec.PREFIX));
        assertFalse(frame.substring(InspectionReportCodec.PREFIX.length()).contains("\n"));
        assertTrue(frame.substring(InspectionReportCodec.PREFIX.length()).getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= InspectionReportCodec.MAX_DOCUMENT_BYTES);
        var independentlyParsed = new ObjectMapper().readTree(frame.substring(InspectionReportCodec.PREFIX.length()));
        assertEquals(1, independentlyParsed.path("schemaVersion").asInt());
        assertEquals("政策-\nA", independentlyParsed.path("key").path("policyId").asString());
        assertEquals(4, independentlyParsed.path("key").path("sourceVersion").asInt());
        assertTrue(independentlyParsed.path("workPresent").asBoolean());
        assertEquals("政策-\nA", independentlyParsed.path("envelope").path("policyId").asString());
        assertEquals(4, independentlyParsed.path("envelope").path("sourceVersion").asInt());
        assertEquals("ISSUED", independentlyParsed.path("envelope").path("eventType").asString());
        assertEquals("2026-12-01", independentlyParsed.path("envelope").path("effectiveDate").asString());
        assertEquals(1, independentlyParsed.path("envelope").path("schemaVersion").asInt());
        assertEquals(0, independentlyParsed.path("controlRevision").asInt());
        assertTrue(independentlyParsed.path("acknowledgedAt").isNull());
        assertTrue(independentlyParsed.path("quarantinedAt").isNull());
        assertTrue(independentlyParsed.path("quarantineReason").isNull());
        assertEquals("ABSENT", independentlyParsed.path("receiptRelation").asString());
        assertEquals("ELIGIBLE_FOR_ONE_ATTEMPT", independentlyParsed.path("recommendation").asString());
        assertTrue(independentlyParsed.path("history").isArray());
        assertEquals(0, independentlyParsed.path("history").size());
        assertTrue(independentlyParsed.path("historySupplementary").asBoolean());

        String base = frame.substring(InspectionReportCodec.PREFIX.length());
        String optionalPrefix = ",\"futureOptional\":\"";
        String optionalSuffix = "\"}";
        int baseBytes = base.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        int exactPayload = InspectionReportCodec.MAX_DOCUMENT_BYTES - baseBytes - optionalPrefix.length() - optionalSuffix.length() + 1;
        String nearLimit = base.substring(0, base.length() - 1) + optionalPrefix + "x".repeat(exactPayload) + optionalSuffix;
        assertEquals(InspectionReportCodec.MAX_DOCUMENT_BYTES, nearLimit.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        assertDoesNotThrow(() -> codec.decodeSingleFrame(InspectionReportCodec.PREFIX + nearLimit, report.key()));
        String oversized = nearLimit.substring(0, nearLimit.length() - 2) + "xx" + optionalSuffix;
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame(InspectionReportCodec.PREFIX + oversized, report.key()));
        var unencodable = report("x".repeat(InspectionReportCodec.MAX_DOCUMENT_BYTES + 1), 4, RecoveryRecommendation.ELIGIBLE_FOR_ONE_ATTEMPT);
        assertThrows(java.io.IOException.class, () -> codec.encode(unencodable), "oversized serialization must fail before any success frame is returned");
    }

    @Test void day144RejectsAmbiguousDuplicateOrUnboundReportFrames() {
        var requested = new InspectionReport.RequestedKey("policy-a", 1);
        String valid = InspectionReportCodec.PREFIX + "{\"schemaVersion\":1,\"key\":{\"policyId\":\"policy-a\",\"sourceVersion\":1},\"workPresent\":false,\"envelope\":null,\"controlRevision\":0,\"acknowledgedAt\":null,\"quarantinedAt\":null,\"quarantineReason\":null,\"receiptRelation\":\"ABSENT\",\"recommendation\":\"NOT_FOUND\",\"history\":[],\"historySupplementary\":true}";
        assertEquals(RecoveryRecommendation.NOT_FOUND, assertDoesNotThrow(() -> codec.decodeSingleFrame(valid, requested)).recommendation());
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame("diagnostic only", requested));
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame(valid + "\n" + valid, requested));
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame(valid.replace("\"schemaVersion\":1", "\"schemaVersion\":1,\"schemaVersion\":1"), requested));
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame(valid.substring(0, valid.length()-1), requested));
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame(valid.replace("\"schemaVersion\":1", "\"schemaVersion\":2"), requested));
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame(valid.replace("\"sourceVersion\":1", "\"sourceVersion\":\"1\""), requested));
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame(valid.replace("\"workPresent\":false", "\"workPresent\":\"false\""), requested));
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame(valid.replace("\"quarantineReason\":null", "\"quarantineReason\":\"NOT_A_REASON\""), requested));
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame(valid.replace("\"policyId\":\"policy-a\"", "\"policyId\":\"policy-a\",\"policyId\":\"policy-a\""), requested));
        String oversized = valid.substring(0, valid.length()-1) + ",\"futureOptional\":\"" + "x".repeat(InspectionReportCodec.MAX_DOCUMENT_BYTES) + "\"}";
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame(oversized, requested));
        String deep = valid.substring(0, valid.length()-1) + ",\"futureOptional\":" + "[".repeat(20)+"0"+"]".repeat(20)+"}";
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame(deep, requested));
        String missingField = valid.replace(",\"historySupplementary\":true", "");
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame(InspectionReportCodec.PREFIX + missingField, requested));
        String nullRequiredEnum = valid.replace("\"recommendation\":\"NOT_FOUND\"", "\"recommendation\":null");
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame(InspectionReportCodec.PREFIX + nullRequiredEnum, requested));
        String largeInteger = valid.replace("\"sourceVersion\":1", "\"sourceVersion\":9223372036854775808");
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame(InspectionReportCodec.PREFIX + largeInteger, requested));
    }

    @Test void day144AcceptsReorderedFieldsAndIgnoredAdditiveOptionalField() throws Exception {
        var report = report("policy-a", 1, RecoveryRecommendation.ELIGIBLE_FOR_ONE_ATTEMPT);
        String frame = codec.encode(report);
        String additive = InspectionReportCodec.PREFIX + "{\"historySupplementary\":true,\"history\":[],\"recommendation\":\"ELIGIBLE_FOR_ONE_ATTEMPT\",\"receiptRelation\":\"ABSENT\",\"quarantineReason\":null,\"quarantinedAt\":null,\"acknowledgedAt\":null,\"controlRevision\":0,\"envelope\":{\"schemaVersion\":1,\"effectiveDate\":\"2026-12-01\",\"eventType\":\"ISSUED\",\"sourceVersion\":1,\"policyId\":\"policy-a\"},\"workPresent\":true,\"key\":{\"sourceVersion\":1,\"policyId\":\"policy-a\"},\"schemaVersion\":1,\"futureOptional\":\"ignored\"}";
        assertEquals(report, codec.decodeSingleFrame(additive, report.key()));
        String reordered = InspectionReportCodec.PREFIX + "{\"history\":[],\"key\":{\"policyId\":\"policy-a\",\"sourceVersion\":1},\"workPresent\":true,\"schemaVersion\":1,\"envelope\":{\"policyId\":\"policy-a\",\"sourceVersion\":1,\"eventType\":\"ISSUED\",\"effectiveDate\":\"2026-12-01\",\"schemaVersion\":1},\"controlRevision\":0,\"acknowledgedAt\":null,\"quarantinedAt\":null,\"quarantineReason\":null,\"receiptRelation\":\"ABSENT\",\"recommendation\":\"ELIGIBLE_FOR_ONE_ATTEMPT\",\"historySupplementary\":true}";
        assertEquals(report, codec.decodeSingleFrame(reordered, report.key()));
    }

    @Test void day144RejectsFractionalVersionMismatchedKeyAndCannedRecommendation() throws Exception {
        var report=report("policy-a",1,RecoveryRecommendation.ELIGIBLE_FOR_ONE_ATTEMPT);
        String frame=codec.encode(report);
        String json=frame.substring(InspectionReportCodec.PREFIX.length());
        assertThrows(java.io.IOException.class,()->codec.decodeSingleFrame(InspectionReportCodec.PREFIX+json.replace("\"sourceVersion\":1","\"sourceVersion\":1.5"),report.key()));
        assertThrows(java.io.IOException.class,()->codec.decodeSingleFrame(frame,new InspectionReport.RequestedKey("other-policy",1)));
        assertThrows(java.io.IOException.class,()->codec.decodeSingleFrame(InspectionReportCodec.PREFIX+json.replace("ELIGIBLE_FOR_ONE_ATTEMPT","ALREADY_COMPLETE"),report.key()));
    }

    private InspectionReport report(String id, long version, RecoveryRecommendation recommendation) {
        var key = new InspectionReport.RequestedKey(id, version);
        var envelope = new InspectionReport.CapturedEnvelope(id, version, "ISSUED", LocalDate.parse("2026-12-01"), 1);
        return new InspectionReport(1, key, true, envelope, 0, null, null, null, ReceiptRelation.ABSENT, recommendation, List.of(), true);
    }
}
