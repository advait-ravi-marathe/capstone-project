package capstone.coverstory.support;

import capstone.coverstory.support.internal.LifecycleToolAuthorization;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class LifecycleToolAuthorizationTest {
    @Test void ownerAndCurrentlyAssignedSupportGetExactlyOneReadWithoutCommandCapability() {
        var facts = CAP125InstructorInfrastructureTest.fake(true);
        var owner = new LifecycleToolAuthorization.Principal("owner-A", LifecycleToolAuthorization.Role.OWNER,
                "owner-A", null, false);
        var support = new LifecycleToolAuthorization.Principal("support-1", LifecycleToolAuthorization.Role.SUPPORT,
                null, "case-A", false);
        var ownerRead = LifecycleToolAuthorization.invokeReadOnlyTool(owner, request("app-A"), facts);
        var supportRead = LifecycleToolAuthorization.invokeReadOnlyTool(support, request("app-A"), facts);
        assertEquals("FOUND", ownerRead.status());
        assertEquals("CANCELLATION_PENDING", ownerRead.cancellationState());
        assertEquals("FOUND", supportRead.status());
        assertEquals("app-A", supportRead.applicationId());
    }

    @Test void unassignedAndForeignApplicationRequestsAreIndistinguishableAndRevealNoIdentifier() {
        var unassigned = new LifecycleToolAuthorization.Principal("support-2", LifecycleToolAuthorization.Role.SUPPORT,
                null, "case-A", false);
        var facts = CAP125InstructorInfrastructureTest.fake(false);
        var denied = LifecycleToolAuthorization.invokeReadOnlyTool(unassigned, request("app-A"), facts);
        var foreign = LifecycleToolAuthorization.invokeReadOnlyTool(ownerA(), request("app-B"), facts);
        assertEquals("NOT_FOUND", denied.status());
        assertEquals("NOT_FOUND", foreign.status());
        assertNull(denied.applicationId());
        assertNull(foreign.applicationId());
        assertEquals("Application not found.", denied.safeMessage());
        assertEquals(denied.safeMessage(), foreign.safeMessage());
    }

    @Test void modelAdminClaimAndEarlierApprovalDoNotGrantThisInvocation() {
        var model = new LifecycleToolAuthorization.Principal("model", LifecycleToolAuthorization.Role.MODEL,
                "owner-A", "case-A", true);
        var result = LifecycleToolAuthorization.invokeReadOnlyTool(model, request("app-A"),
                CAP125InstructorInfrastructureTest.fake(true));
        assertEquals("NOT_FOUND", result.status());
        assertNull(result.cancellationState());
    }

    private static LifecycleToolAuthorization.Principal ownerA() {
        return new LifecycleToolAuthorization.Principal("owner-A", LifecycleToolAuthorization.Role.OWNER,
                "owner-A", null, false);
    }
    private static LifecycleToolAuthorization.Request request(String applicationId) {
        return new LifecycleToolAuthorization.Request(applicationId, "invocation-current");
    }
}
