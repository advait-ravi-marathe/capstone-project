package coverstory.modules.support.api;

import java.util.UUID;

/** Support-facing contract for an exact, owner-authorized immutable citation read. */
public interface SourceLineageReadPort {
    LineageResult read(UUID authenticatedOwnerId, String citationId);

    record LineageResult(String status, String code, String sourceId, String versionId,
                         String batchId, String evidenceId, String contentSha256,
                         int startByte, int endByte, String evidenceClass, int writeCount,
                         String safeMessage) { }
}
