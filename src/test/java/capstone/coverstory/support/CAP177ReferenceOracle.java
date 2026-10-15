package capstone.coverstory.support;

import capstone.coverstory.support.internal.BoundedLifecycleEvidenceEnvelope.Envelope;
import capstone.coverstory.support.internal.BoundedLifecycleEvidenceEnvelope.EvidenceRow;
import capstone.coverstory.support.internal.BoundedLifecycleEvidenceEnvelope;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Independent instructor oracle; never invokes the learner seam. */
final class CAP177ReferenceOracle {
    private static final ObjectMapper JSON = new ObjectMapper();
    record Result(Envelope envelope, byte[] utf8, String safeConclusion) { }

    static Result build(List<EvidenceRow> rows, int maxRows, int maxUtf8Bytes) {
        if (rows == null || maxRows < 0 || maxUtf8Bytes < 0) throw new IllegalArgumentException("invalid limits");
        for (EvidenceRow row : rows) {
            if (row == null) throw new IllegalArgumentException("null evidence row");
        }
        int count = Math.min(rows.size(), maxRows);
        while (count >= 0) {
            List<EvidenceRow> candidate = List.copyOf(rows.subList(0, count));
            Envelope envelope = new Envelope(count, rows.size(), count < rows.size(), candidate);
            byte[] bytes = serialize(envelope);
            if (bytes.length <= maxUtf8Bytes) {
                String conclusion = envelope.truncated()
                        ? "Evidence is incomplete; omitted events do not establish that dispatch did not happen."
                        : "Evidence rows are complete for this bounded result.";
                return new Result(envelope, bytes, conclusion);
            }
            count--;
        }
        throw new BoundedLifecycleEvidenceEnvelope.TooSmallException();
    }

    static byte[] serialize(Envelope envelope) {
        try { return JSON.writeValueAsBytes(envelope); }
        catch (Exception e) { throw new IllegalStateException("synthetic envelope serialization failed", e); }
    }

    static int utf8Length(Envelope envelope) { return serialize(envelope).length; }
}
