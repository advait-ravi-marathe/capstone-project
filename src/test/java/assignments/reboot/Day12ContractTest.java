package assignments.reboot;

import assignments.insurance.JourneyAccessPolicy;
import assignments.insurance.JourneyReadRequest;
import assignments.insurance.ResourceNotFoundException;
import assignments.insurance.UnauthenticatedException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Day 12 isolates owner policy with a fixed principal and persisted owner fixture. */
@Tag("learnerAcceptance")
class Day12ContractTest {
    private final JourneyAccessPolicy policy = new JourneyAccessPolicy();

    private record FixedPrincipal(String name) implements Principal {
        @Override public String getName() { return name; }
    }

    @Test
    void anonymousPrincipalCannotRead() {
        assertThrows(UnauthenticatedException.class, () -> policy.assertCanAccess(
                null, "owner-a", new JourneyReadRequest("journey-1", null)));
    }

    @Test
    void authenticatedOwnerCanReadOwnJourney() {
        assertDoesNotThrow(() -> policy.assertCanAccess(
                new FixedPrincipal("owner-a"), "owner-a", new JourneyReadRequest("journey-1", null)));
    }

    @Test
    void differentOwnerGetsNonDisclosingNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> policy.assertCanAccess(
                new FixedPrincipal("owner-a"), "owner-b", new JourneyReadRequest("journey-1", null)));
    }

    @Test
    void forgedClientOwnerDoesNotEstablishIdentity() {
        assertThrows(ResourceNotFoundException.class, () -> policy.assertCanAccess(
                new FixedPrincipal("owner-a"), "owner-b", new JourneyReadRequest("journey-1", "owner-b")));
    }

    @Test
    void clientOwnerCannotReplaceAuthenticatedIdentityForOwnedJourney() {
        assertDoesNotThrow(() -> policy.assertCanAccess(
                new FixedPrincipal("owner-a"), "owner-a", new JourneyReadRequest("journey-1", "owner-b")));
    }
}
