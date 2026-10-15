package capstone.coverstory.support;

import capstone.coverstory.support.internal.OwnedEvidenceRetriever;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class OwnedEvidenceRetrieverTest {
    private static final OwnedEvidenceRetriever.Source POLICY_A =
            new OwnedEvidenceRetriever.Source("policy-A", "owner-A", "case-A", "POLICY", "Synthetic A policy");
    private static final OwnedEvidenceRetriever.Source HELP_A =
            new OwnedEvidenceRetriever.Source("help-A", "owner-A", "case-A", "HELP", "Synthetic A help");
    private static final OwnedEvidenceRetriever.Source POLICY_B =
            new OwnedEvidenceRetriever.Source("policy-B", "owner-B", "case-B", "POLICY", "BETA_CANARY");

    @Test void ownerReceivesOnlyItsSyntheticPolicyAndHelpEvidence() {
        var owner = new OwnedEvidenceRetriever.Principal("owner-a", OwnedEvidenceRetriever.Role.OWNER, "owner-A");
        var result = OwnedEvidenceRetriever.retrieve(owner,
                new OwnedEvidenceRetriever.Request("case-A", null), (principal, caseId) -> null,
                (ownerId, caseId) -> List.of(POLICY_A, HELP_A));
        assertEquals("ALLOW", result.status());
        assertEquals(List.of("policy-A", "help-A"), result.sources().stream().map(OwnedEvidenceRetriever.Source::id).toList());
    }

    @Test void forgedOwnerRequestIsDeniedBeforeCandidateLookup() {
        var owner = new OwnedEvidenceRetriever.Principal("owner-a", OwnedEvidenceRetriever.Role.OWNER, "owner-A");
        var result = OwnedEvidenceRetriever.retrieve(owner,
                new OwnedEvidenceRetriever.Request("case-A", "owner-B"), (principal, caseId) -> null,
                (ownerId, caseId) -> fail("must deny before candidate lookup"));
        assertEquals("DENY", result.status());
        assertEquals("OWNER_SCOPE_MISMATCH", result.code());
        assertTrue(result.sources().isEmpty());
    }

    @Test void unassignedSupportIsDeniedBeforeCandidateLookup() {
        var support = new OwnedEvidenceRetriever.Principal("support-unassigned", OwnedEvidenceRetriever.Role.SUPPORT, null);
        var result = OwnedEvidenceRetriever.retrieve(support,
                new OwnedEvidenceRetriever.Request("case-A", null), (principal, caseId) -> null,
                (ownerId, caseId) -> fail("must deny before candidate lookup"));
        assertEquals("DENY", result.status());
        assertEquals("SUPPORT_NOT_ASSIGNED", result.code());
        assertTrue(result.sources().isEmpty());
    }

    @Test void assignedSupportReceivesOnlySourcesForTheAssignedCase() {
        var support = new OwnedEvidenceRetriever.Principal("support-a-assigned", OwnedEvidenceRetriever.Role.SUPPORT, null);
        var result = OwnedEvidenceRetriever.retrieve(support,
                new OwnedEvidenceRetriever.Request("case-A", null),
                (principalId, caseId) -> principalId.equals("support-a-assigned") && caseId.equals("case-A") ? "owner-A" : null,
                (ownerId, caseId) -> List.of(POLICY_A, HELP_A));
        assertEquals("ALLOW", result.status());
        assertEquals(List.of("policy-A", "help-A"), result.sources().stream().map(OwnedEvidenceRetriever.Source::id).toList());
    }

    @Test void faultyAdapterForeignCanaryIsRejectedBeforeEitherSink() {
        var owner = new OwnedEvidenceRetriever.Principal("owner-a", OwnedEvidenceRetriever.Role.OWNER, "owner-A");
        var result = OwnedEvidenceRetriever.retrieve(owner,
                new OwnedEvidenceRetriever.Request("case-A", null), (principal, caseId) -> null,
                (ownerId, caseId) -> List.of(POLICY_A, POLICY_B));
        assertEquals("DENY", result.status());
        assertEquals("RESULT_SCOPE_VIOLATION", result.code());
        assertTrue(result.sources().isEmpty());
        assertFalse(result.sources().stream().anyMatch(s -> s.text().contains("BETA_CANARY")));
    }
}
