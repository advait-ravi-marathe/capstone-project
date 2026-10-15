package capstone.coverstory.application.internal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class ReviewEvidenceManifestTest {
    private final ReviewEvidenceManifest learner=new ReviewEvidenceManifest();
    private ReviewEvidenceManifest.Manifest manifest(){return new ReviewEvidenceManifest.Manifest("11500000-0000-4000-8000-000000000001","11500000-0000-4000-8000-000000000101","o2","f96df155cd21849d54e42885cdb9432a9182fcc894f1b4ce794df7eae828d91c",Instant.parse("2027-01-04T12:00:00Z"),Instant.parse("2027-01-20T00:00:00Z"),List.of("copay_0_percent","restore_benefit","waiting_30_days"),List.of(),"Synthetic evidence only.");}
    @Test void completeCurrentBundleRequestsBalanceReconciliation(){var v=learner.verify(manifest());assertEquals(ReviewEvidenceManifest.Status.VERIFIED_REVIEW_REQUIRED,v.status());assertEquals(ReviewEvidenceManifest.NextStep.RECONCILE_BALANCE,v.nextStep());}
    @Test void expiryBlocksOldBundle(){var m=manifest();var expired=new ReviewEvidenceManifest.Manifest(m.ownerId(),m.applicationId(),m.acceptedRevision(),m.acceptedOfferContentSha256(),Instant.parse("2027-01-21T00:00:00Z"),m.offerExpiresAt(),m.termCodes(),m.artifacts(),m.narrative());assertEquals(ReviewEvidenceManifest.Status.INVALID,learner.verify(expired).status());}
    @Test void changedTermsHashInvalidatesEvenWhenAmountIsUnchanged(){var m=manifest();var changed=new ReviewEvidenceManifest.Manifest(m.ownerId(),m.applicationId(),m.acceptedRevision(),"0".repeat(64),m.verifiedAt(),m.offerExpiresAt(),m.termCodes(),m.artifacts(),m.narrative());assertEquals(ReviewEvidenceManifest.Status.INVALID,learner.verify(changed).status());}
    @Test void tamperOrUnsupportedApprovalClaimInvalidatesBundle(){assertEquals(ReviewEvidenceManifest.Status.INVALID,learner.verify(manifest()).status());}
    @Test void learnerMethodRemainsNamedUnsupportedTodo(){assertThrows(UnsupportedOperationException.class,()->learner.verify(manifest()));}
}
