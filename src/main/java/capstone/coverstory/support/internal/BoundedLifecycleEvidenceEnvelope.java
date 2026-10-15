package capstone.coverstory.support.internal;

import java.util.List;

/** Learner-owned bounded read-only evidence projection. */
public final class BoundedLifecycleEvidenceEnvelope {
    public record EvidenceRow(String id, String kind, String sourceVersion, String summary) { }
    public record Envelope(int includedCount, int availableCount, boolean truncated, List<EvidenceRow> rows) {
        public Envelope { rows = List.copyOf(rows); }
    }
    public static final class TooSmallException extends IllegalArgumentException {
        public TooSmallException() { super("TOO_SMALL"); }
        public String code() { return "TOO_SMALL"; }
    }

    public Envelope build(List<EvidenceRow> authorizedRows, int maxRows, int maxUtf8Bytes) {
        throw new UnsupportedOperationException("TODO(CAP-177): implement complete-row UTF-8 bounded evidence");
    }
}
