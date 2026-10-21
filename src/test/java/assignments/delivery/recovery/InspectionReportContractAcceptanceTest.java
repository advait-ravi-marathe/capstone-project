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
        assertEquals(RecoveryRecommendation.QUARANTINED_NO_RECEIPT, report.recommendation());
        assertTrue(report.historySupplementary());
        assertEquals(history.size(), report.history().size());
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
        assertEquals(20,report.history().size());
        assertEquals(RecoveryRecommendation.RECONCILE_ACKNOWLEDGMENT,report.recommendation());
        assertEquals(1,report.history().getFirst().controlRevision());
        assertEquals(20,report.history().getLast().controlRevision());
    }

    @Test void day143EncodesExactlyOneEscapedVersionedRecord() throws Exception {
        var report = report("policy-\nA", 4, RecoveryRecommendation.ELIGIBLE_FOR_ONE_ATTEMPT);
        String frame = codec.encode(report);
        assertTrue(frame.startsWith(InspectionReportCodec.PREFIX));
        assertFalse(frame.substring(InspectionReportCodec.PREFIX.length()).contains("\n"));
        assertTrue(frame.length() <= InspectionReportCodec.MAX_DOCUMENT_BYTES + InspectionReportCodec.PREFIX.length());
        assertEquals("policy-\nA", codec.decodeSingleFrame(frame, report.key()).key().policyId());
    }

    @Test void day144RejectsAmbiguousDuplicateOrUnboundReportFrames() {
        var requested = new InspectionReport.RequestedKey("policy-a", 1);
        assertThrows(Exception.class, () -> codec.decodeSingleFrame("diagnostic only", requested));
        assertThrows(Exception.class, () -> codec.decodeSingleFrame(InspectionReportCodec.PREFIX+"{}\n"+InspectionReportCodec.PREFIX+"{}", requested));
        assertThrows(Exception.class, () -> codec.decodeSingleFrame(InspectionReportCodec.PREFIX+"{\"schemaVersion\":1,\"schemaVersion\":1}", requested));
        assertThrows(Exception.class, () -> codec.decodeSingleFrame("x".repeat(InspectionReportCodec.MAX_DOCUMENT_BYTES+1), requested));
    }

    @Test void day144AcceptsReorderedFieldsAndIgnoredAdditiveOptionalField() throws Exception {
        var report = report("policy-a", 1, RecoveryRecommendation.ELIGIBLE_FOR_ONE_ATTEMPT);
        String frame = codec.encode(report);
        String json = frame.substring(InspectionReportCodec.PREFIX.length());
        String additive = InspectionReportCodec.PREFIX + json.substring(0,json.length()-1) + ",\"futureOptional\":\"ignored\"}";
        assertEquals(report, codec.decodeSingleFrame(additive, report.key()));
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
