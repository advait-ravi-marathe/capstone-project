package coverstory.modules.support.api;

import java.util.List;
import java.util.UUID;

/** Support-owned read-only boundary for an authenticated owner's lifecycle evidence. */
public interface LifecycleEvidenceReadPort {
    EvidenceResult read(UUID authenticatedOwnerId, UUID applicationId, int maxRows, int maxUtf8Bytes);

    final class TooSmallException extends RuntimeException {
        public TooSmallException() { super("TOO_SMALL"); }
    }

    record EvidenceRow(String id, String kind, String sourceVersion, String summary) { }
    record EvidenceResult(int includedCount, int availableCount, boolean truncated,
                          List<EvidenceRow> rows, int serializedEnvelopeBytes,
                          String safeConclusion) {
        public EvidenceResult { rows = List.copyOf(rows); }
    }
}
