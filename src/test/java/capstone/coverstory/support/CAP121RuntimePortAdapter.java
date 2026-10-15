package capstone.coverstory.support;

import coverstory.modules.support.api.SourceLineageReadPort;
import java.util.List;
import java.util.UUID;

/** Instructor-only synthetic adapter. It never uses the learner SourceLineageReader seam. */
public final class CAP121RuntimePortAdapter implements SourceLineageReadPort {
    public static final UUID OWNER_A = UUID.fromString("12100000-0000-4000-8000-000000000001");
    public static final UUID OWNER_B = UUID.fromString("12100000-0000-4000-8000-000000000002");
    private static final String TEXT = "Synthetic policy A v2: outpatient limit 5 visits per policy year.";
    private static final String HASH = "5b7b685add270143d03a6c037e4ba380d1a6f9677c248c9d6a93b7882433eac4";
    private final CAP121ReferenceOracle oracle = new CAP121ReferenceOracle();

    @Override public LineageResult read(UUID authenticatedOwnerId, String citationId) {
        String owner = OWNER_A.equals(authenticatedOwnerId) ? "owner-A"
                : OWNER_B.equals(authenticatedOwnerId) ? "owner-B" : "unknown";
        boolean missingV2 = "ai1-v1-only".equals(citationId);
        var a = new CAP121ReferenceOracle.Citation("ai1", "owner-A", "policy-A", "v2", "m2", "pA", 23, 39, HASH);
        var b = new CAP121ReferenceOracle.Citation("bi1", "owner-B", "policy-B", "v2", "m2B", "pB", 23, 39,
                "a487d45905c17349dd54172e548582488d7ca4e491d1b4f8e98e23abf3dc2606");
        var versions = List.of(new CAP121ReferenceOracle.Version("policy-A", "v2", "owner-A", "m2", "pA", TEXT, HASH),
                new CAP121ReferenceOracle.Version("policy-A", "v1", "owner-A", "m1", "pA",
                        "Synthetic policy A v1: outpatient limit 3 visits per policy year.",
                        "6f7aeff1c435218e6c218387746e077500d1b8b0440ebecf1376610e6b093e2c"));
        List<CAP121ReferenceOracle.Version> available = missingV2 ? versions.subList(1, 2) : versions;
        List<CAP121ReferenceOracle.Citation> citations = List.of(a, b).stream()
                .filter(c -> c.id().equals(missingV2 ? "ai1" : citationId)).toList();
        var result = oracle.resolve(new CAP121ReferenceOracle.Principal(owner, owner), missingV2 ? "ai1" : citationId,
                citations, available, List.of("pA"));
        return new LineageResult(result.status(), result.code(), result.sourceId(), result.versionId(),
                result.batchId(), result.evidenceId(), result.contentSha256(), result.startByte(), result.endByte(),
                result.evidenceClass(), result.writeCount(), result.safeMessage());
    }
}
