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

    @Test void day143EncodesExactlyOneEscapedVersionedRecord() throws Exception {
        var report = report("政策-\nA", 4, RecoveryRecommendation.ELIGIBLE_FOR_ONE_ATTEMPT);
        String frame = codec.encode(report);
        assertTrue(frame.startsWith(InspectionReportCodec.PREFIX));
        assertFalse(frame.substring(InspectionReportCodec.PREFIX.length()).contains("\n"));
        assertTrue(frame.substring(InspectionReportCodec.PREFIX.length()).getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= InspectionReportCodec.MAX_DOCUMENT_BYTES);
        var independentlyParsed = new ObjectMapper().readTree(frame.substring(InspectionReportCodec.PREFIX.length()));
        assertEquals(1, independentlyParsed.path("schemaVersion").asInt());
        assertEquals("政策-\nA", independentlyParsed.path("key").path("policyId").asString());
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
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame("x".repeat(InspectionReportCodec.MAX_DOCUMENT_BYTES+1), requested));
        String deep = valid.replace("\"history\":[]", "\"history\":["+"[".repeat(20)+"0"+"]".repeat(20)+"]");
        assertThrows(java.io.IOException.class, () -> codec.decodeSingleFrame(deep, requested));
    }

    @Test void day144AcceptsReorderedFieldsAndIgnoredAdditiveOptionalField() throws Exception {
        var report = report("policy-a", 1, RecoveryRecommendation.ELIGIBLE_FOR_ONE_ATTEMPT);
        String frame = codec.encode(report);
        String json = frame.substring(InspectionReportCodec.PREFIX.length());
        String additive = InspectionReportCodec.PREFIX + json.substring(0,json.length()-1) + ",\"futureOptional\":\"ignored\"}";
        assertEquals(report, codec.decodeSingleFrame(additive, report.key()));
        int keyAt = json.indexOf("\"key\"");
        int keyEnd = json.indexOf(",\"workPresent\"", keyAt);
        int schemaEnd = json.indexOf(",\"key\"");
        assertTrue(keyAt >= 0 && keyEnd > keyAt && schemaEnd > 0);
        String reorderedJson = "{" + json.substring(keyAt, keyEnd) + "," + json.substring(1, schemaEnd) + "," + json.substring(keyEnd + 1);
        assertEquals(report, codec.decodeSingleFrame(InspectionReportCodec.PREFIX+reorderedJson, report.key()));
    }

    @Test void day144RejectsFractionalVersionMismatchedKeyAndCannedRecommendation() throws Exception {
        var report=report("policy-a",1,RecoveryRecommendation.ELIGIBLE_FOR_ONE_ATTEMPT);
        String frame=codec.encode(report);
        String json=frame.substring(InspectionReportCodec.PREFIX.length());
        assertThrows(Exception.class,()->codec.decodeSingleFrame(InspectionReportCodec.PREFIX+json.replace("\"sourceVersion\":1","\"sourceVersion\":1.5"),report.key()));
        assertThrows(Exception.class,()->codec.decodeSingleFrame(frame,new InspectionReport.RequestedKey("other-policy",1)));
        assertThrows(Exception.class,()->codec.decodeSingleFrame(InspectionReportCodec.PREFIX+json.replace("ELIGIBLE_FOR_ONE_ATTEMPT","ALREADY_COMPLETE"),report.key()));
    }

    private InspectionReport report(String id, long version, RecoveryRecommendation recommendation) {
        var key = new InspectionReport.RequestedKey(id, version);
        var envelope = new InspectionReport.CapturedEnvelope(id, version, "ISSUED", LocalDate.parse("2026-12-01"), 1);
        return new InspectionReport(1, key, true, envelope, 0, null, null, null, ReceiptRelation.ABSENT, recommendation, List.of(), true);
    }
}
