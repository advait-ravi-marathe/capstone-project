package assignments.delivery.recovery;

import assignments.delivery.IssuanceSourceKey;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class ReleaseInspectionCommandRunnerTest {
    @Test void oneValidRequestPerformsExactlyOneSchemaCheckAndInspection() {
        var requests = mock(ReleaseInspectionRequestPolicy.class);
        var schema = mock(ReleaseSchemaVerifier.class);
        var recovery = mock(ManualRecoveryService.class);
        var mapper = mock(InspectionReportMapper.class);
        var codec = mock(InspectionReportCodec.class);
        var runner = new ReleaseInspectionCommandRunner(requests, schema, recovery, mapper, codec, null);
        var key = new IssuanceSourceKey("one-read", 4);
        var request = new ReleaseInspectionRequest(key);
        var read = new RecoveryReadResult(RecoveryObservation.notFound(key), java.util.List.of());
        var report = new InspectionReport(1, new InspectionReport.RequestedKey("one-read", 4), false,
                null, 0, null, null, null, ReceiptRelation.ABSENT, RecoveryRecommendation.NOT_FOUND,
                java.util.List.of(), true);
        when(recovery.inspect(key)).thenReturn(read);
        when(mapper.map(read)).thenReturn(report);

        assertSame(report, runner.inspectOnce(request));
        verify(schema, times(1)).requireV001ThroughV005();
        verify(recovery, times(1)).inspect(key);
        verify(mapper, times(1)).map(read);
        verifyNoMoreInteractions(schema, recovery, mapper);
    }
}
