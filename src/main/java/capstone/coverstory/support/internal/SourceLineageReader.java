package capstone.coverstory.support.internal;

/** Learner-owned read-only citation lineage seam for CAP-121. */
public final class SourceLineageReader {
    private SourceLineageReader() { }

    public record Principal(String id, String ownerId) { }
    public record Citation(String id, String ownerId, String sourceId, String versionId,
                           String batchId, String evidenceId, int startByte, int endByte) { }
    public record Request(String citationId) { }
    public record Lineage(String citationId, String sourceId, String versionId, String batchId,
                          String evidenceId, String contentSha256, int startByte, int endByte,
                          String evidenceClass) { }
    public record Result(String status, String code, Lineage lineage, String safeMessage) { }

    public interface ReadPort {
        Citation citation(String citationId);
        String exactVersionOwner(String versionId);
        String exactBatchOwner(String batchId);
        String exactEvidenceOwner(String evidenceId);
    }

    public static Result resolve(Principal principal, Request request, ReadPort readPort) {
        // TODO(CAP-121): authorize citation ownership, resolve only the exact
        // citation -> immutable source version -> ingest batch -> owning evidence
        // chain, validate content digest and UTF-8 byte offsets, and fail closed.
        throw new UnsupportedOperationException("TODO(CAP-121): implement exact source lineage resolution");
    }
}
