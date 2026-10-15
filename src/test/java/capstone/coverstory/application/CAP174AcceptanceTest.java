package capstone.coverstory.application;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP174AcceptanceTest {
    private final VerifyExactProposalRevisionDigests seam = new VerifyExactProposalRevisionDigests();

    @Test void exactAbcBytesMatchKnownDigest() {
        assertEquals(VerifyExactProposalRevisionDigests.Status.VERIFIED,
                seam.verify(new VerifyExactProposalRevisionDigests.Snapshot("proposal-A", "1",
                        "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", new byte[]{97,98,99}), "1").status());
    }
    @Test void newlineIsNotNormalizedAway() {
        assertEquals(VerifyExactProposalRevisionDigests.Status.DIGEST_MISMATCH,
                seam.verify(new VerifyExactProposalRevisionDigests.Snapshot("proposal-A", "1",
                        "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", new byte[]{97,98,99,10}), "1").status());
    }
    @Test void malformedDigestIsRejected() {
        assertEquals(VerifyExactProposalRevisionDigests.Status.INVALID_MANIFEST,
                seam.verify(new VerifyExactProposalRevisionDigests.Snapshot("proposal-A", "1", "xyz", new byte[]{97,98,99}), "1").status());
    }
    @Test void bytesMayArriveInDifferentChunks() {
        byte[] bytes = {97,98,99,100};
        assertEquals(VerifyExactProposalRevisionDigests.Status.VERIFIED,
                seam.verify(new VerifyExactProposalRevisionDigests.Snapshot("proposal-A", "1",
                        "88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589", bytes), "1").status());
    }
    @Test void emptyDigestDoesNotMeanCompleteProposal() {
        assertEquals(VerifyExactProposalRevisionDigests.Status.VERIFIED,
                seam.verify(new VerifyExactProposalRevisionDigests.Snapshot("proposal-A", "1",
                        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", new byte[0]), "1").status());
    }
    @Test void oldVerifiedRevisionRequiresReviewWhenCurrentRevisionChanges() {
        assertEquals(VerifyExactProposalRevisionDigests.Status.REVIEW_REQUIRED,
                seam.verify(new VerifyExactProposalRevisionDigests.Snapshot("proposal-A", "1",
                        "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", new byte[]{97,98,99}), "2").status());
    }
}
