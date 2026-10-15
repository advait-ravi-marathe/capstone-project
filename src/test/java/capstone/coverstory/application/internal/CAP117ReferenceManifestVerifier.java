package capstone.coverstory.application.internal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Independent instructor oracle over frozen synthetic fixture facts; not production validation. */
final class CAP117ReferenceManifestVerifier {
    record Fact(String id, String type, String expectedSha256, String revision, boolean present) {}
    record Input(String acceptedRevision, String acceptedHash, String currentHash, Instant checkedAt,
                 Instant expiresAt, List<Fact> artifacts, String narrative) {}
    record Result(String status, String nextStep, List<String> issues) {}
    private static final Set<String> REQUIRED=Set.of("CAP-112-result","CAP-113-result","CAP-114-result","CAP-115-result","CAP-116-result");

    Result verify(Input in) {
        List<String> issues=new ArrayList<>();
        if(!in.checkedAt().isBefore(in.expiresAt())) issues.add("OFFER_EXPIRED");
        if(!in.acceptedHash().equals(in.currentHash())) issues.add("ACCEPTED_REVISION_MISMATCH");
        if(in.narrative().toLowerCase().matches(".*\\b(insurer approved|approved by insurer|active cover issued)\\b.*")) issues.add("UNSUPPORTED_INSURER_APPROVAL_CLAIM");
        Set<String> found=new java.util.HashSet<>();
        for(Fact f:in.artifacts()) {
            found.add(f.id());
            if(!f.present()) issues.add("ARTIFACT_MISSING:"+f.id());
            if(!f.expectedSha256().matches("[0-9a-f]{64}")) issues.add("ARTIFACT_HASH_MISMATCH:"+f.id());
        }
        for(String required:REQUIRED) if(!found.contains(required)) issues.add("ARTIFACT_MISSING:"+required);
        boolean invalid=issues.stream().anyMatch(i->i.startsWith("OFFER_")||i.startsWith("ACCEPTED_")||i.startsWith("UNSUPPORTED_")||i.startsWith("ARTIFACT_"));
        return new Result(invalid?"INVALID":"VERIFIED_REVIEW_REQUIRED",invalid?"NONE":"RECONCILE_BALANCE",List.copyOf(issues));
    }
}
