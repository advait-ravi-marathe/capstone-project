package capstone.coverstory.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

/** Fixed instructor reference model, separate from learner seam. */
final class CAP121ReferenceOracle {
    record Principal(String id, String ownerId) { }
    record Citation(String id, String ownerId, String sourceId, String versionId, String batchId,
                    String evidenceId, int startByte, int endByte, String contentSha256) { }
    record Version(String sourceId, String versionId, String ownerId, String batchId,
                   String evidenceId, String text, String contentSha256) { }
    record Result(String status, String code, String sourceId, String versionId, String batchId,
                  String evidenceId, String contentSha256, int startByte, int endByte,
                  String evidenceClass, int writeCount, String safeMessage) { }

    Result resolve(Principal principal, String citationId, List<Citation> citations,
                   List<Version> versions, List<String> authorizedEvidenceIds) {
        Citation citation = citations.stream().filter(c -> c.id().equals(citationId)).findFirst().orElse(null);
        if (citation == null || principal == null || !principal.ownerId().equals(citation.ownerId()))
            return missing();
        Version v = versions.stream().filter(x -> x.sourceId().equals(citation.sourceId())
                && x.versionId().equals(citation.versionId())).findFirst().orElse(null);
        if (v == null) return broken("EXACT_VERSION_MISSING");
        if (!v.ownerId().equals(citation.ownerId()) || !v.batchId().equals(citation.batchId())
                || !v.evidenceId().equals(citation.evidenceId())) return broken("LINEAGE_OWNER_OR_BATCH_MISMATCH");
        if (!authorizedEvidenceIds.contains(v.evidenceId())) return missing();
        byte[] content = v.text().getBytes(StandardCharsets.UTF_8);
        String digest = sha256(content);
        if (!digest.equals(v.contentSha256()) || !digest.equals(citation.contentSha256())
                || citation.startByte() < 0 || citation.endByte() <= citation.startByte()
                || citation.endByte() > content.length) return broken("CONTENT_OR_OFFSET_MISMATCH");
        String exactText = new String(content, citation.startByte(), citation.endByte() - citation.startByte(), StandardCharsets.UTF_8);
        if (!exactText.equals("outpatient limit")) return broken("CITATION_SPAN_MISMATCH");
        return new Result("FOUND", null, v.sourceId(), v.versionId(), v.batchId(), v.evidenceId(), digest,
                citation.startByte(), citation.endByte(), "POLICY_FACT", 0, null);
    }

    private static Result missing() { return new Result("NOT_FOUND", null, null, null, null, null, null, 0, 0, null, 0, "Citation not found."); }
    private static Result broken(String code) { return new Result("LINEAGE_BROKEN", code, null, null, null, null, null, 0, 0, null, 0, "Citation lineage unavailable."); }
    private static String sha256(byte[] bytes) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
}
