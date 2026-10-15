package capstone.coverstory.support;

import capstone.coverstory.support.internal.LifecycleToolAuthorization;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class CAP125InstructorInfrastructureTest {
    private static final LifecycleToolAuthorization.Application APP_A =
            new LifecycleToolAuthorization.Application("app-A", "owner-A", "case-A", "CANCELLATION_PENDING");
    private static final LifecycleToolAuthorization.Application APP_B =
            new LifecycleToolAuthorization.Application("app-B", "owner-B", "case-B", "ACTIVE");

    @Test void fixtureIsSyntheticFrozenAndHasThreeCanonicalCases() throws Exception {
        String fixture = Files.readString(Path.of("docs/coverstory/fixtures/cap-125.json"));
        assertTrue(fixture.contains("\"synthetic_only\": true"));
        assertTrue(fixture.contains("CAP-125-CS-1"));
        assertTrue(fixture.contains("CAP-125-CS-2"));
        assertTrue(fixture.contains("CAP-125-CS-3"));
        assertTrue(fixture.contains("conversationClaimsAdmin"));
        assertTrue(fixture.contains("\"write_count\":0"));
    }

    @Test void fakeOwningModulePortCanResolveFactsAndAssignmentPerInvocation() {
        var port = fake(true);
        assertEquals(APP_A, port.application("app-A"));
        assertTrue(port.isCurrentlyAssigned("support-1", "case-A"));
        assertFalse(fake(false).isCurrentlyAssigned("support-1", "case-A"));
    }

    @Test void referenceOracleDefinesOwnerAssignmentAndClaimCasesWithoutRunningLearnerCode() {
        var owner = new LifecycleToolAuthorization.Principal("owner-A", LifecycleToolAuthorization.Role.OWNER,
                "owner-A", null, false);
        var assigned = new LifecycleToolAuthorization.Principal("support-1", LifecycleToolAuthorization.Role.SUPPORT,
                null, "case-A", false);
        var unassigned = new LifecycleToolAuthorization.Principal("support-2", LifecycleToolAuthorization.Role.SUPPORT,
                null, "case-A", false);
        var claimedModelAdmin = new LifecycleToolAuthorization.Principal("model", LifecycleToolAuthorization.Role.MODEL,
                "owner-A", "case-A", true);
        assertEquals("FOUND", CAP125ReferenceOracle.invoke(owner, req("app-A"), fake(false)).status());
        assertEquals("FOUND", CAP125ReferenceOracle.invoke(assigned, req("app-A"), fake(true)).status());
        var denied = CAP125ReferenceOracle.invoke(unassigned, req("app-A"), fake(false));
        assertEquals("NOT_FOUND", denied.status());
        assertNull(denied.applicationId());
        assertEquals("NOT_FOUND", CAP125ReferenceOracle.invoke(owner, req("app-B"), fake(false)).status());
        assertEquals("NOT_FOUND", CAP125ReferenceOracle.invoke(claimedModelAdmin, req("app-A"), fake(true)).status());
    }

    private static LifecycleToolAuthorization.Request req(String id) {
        return new LifecycleToolAuthorization.Request(id, "invocation-1");
    }
    static LifecycleToolAuthorization.FactsPort fake(boolean assigned) {
        return new LifecycleToolAuthorization.FactsPort() {
            public LifecycleToolAuthorization.Application application(String id) {
                return switch (id) { case "app-A" -> APP_A; case "app-B" -> APP_B; default -> null; };
            }
            public boolean isCurrentlyAssigned(String actor, String caseId) {
                return assigned && actor.equals("support-1") && caseId.equals("case-A");
            }
        };
    }
}
