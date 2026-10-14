package coverstory.modules.application.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** CAP-54 application-owned resume contract. UUIDs are lookup identifiers, never authorization. */
public final class CAP54Seam {
    public record Principal(String userId) { }
    public record ResumeRequest(UUID journeyId, String ownerUserIdOverride, String browserCurrentStep) { }
    public record JourneyLink(UUID journeyId, UUID applicationId, long revision) { }
    public record AuthoritativeState(String status, boolean paymentConfirmed, boolean dispatchConfirmed) { }
    public record SafeSummary(UUID journeyId, String status, String resumeAction) { }
    public record Page(List<SafeSummary> journeys, String nextCursor) { }
    public record Reply(int status, String errorCode, SafeSummary journey, Page page) { }

    /** Must scope the query by authenticated owner and journey UUID in the same lookup. */
    public interface OwnedJourneyReader {
        Optional<JourneyLink> findOwned(String authenticatedUserId, UUID journeyId);
        Page listOwned(String authenticatedUserId, int limit, String ownerBoundCursor);
    }

    /** Loads only the authoritative application revision named by the owned journey link. */
    public interface ApplicationStateReader {
        Optional<AuthoritativeState> load(UUID applicationId, long revision);
    }

    @FunctionalInterface
    public interface ResumeActionPolicy {
        String recompute(AuthoritativeState state);
    }

    @FunctionalInterface
    public interface CursorOwnerVerifier {
        boolean validFor(String cursor, String authenticatedUserId);
    }

    private final OwnedJourneyReader journeys;
    private final ApplicationStateReader states;
    private final ResumeActionPolicy actions;
    private final CursorOwnerVerifier cursors;

    public CAP54Seam(OwnedJourneyReader journeys, ApplicationStateReader states,
                     ResumeActionPolicy actions, CursorOwnerVerifier cursors) {
        this.journeys = journeys;
        this.states = states;
        this.actions = actions;
        this.cursors = cursors;
    }

    public Reply resume(Principal principal, ResumeRequest request) {
        // LEARNER TODO: require a principal; reject owner overrides; perform an
        // owner-scoped lookup; return the same safe 404 for foreign/absent IDs;
        // derive summary/action from the authoritative linked revision only.
        throw new UnsupportedOperationException("CAP-54 learner TODO: implement owned resume");
    }

    public Reply listOwned(Principal principal, int limit, String cursor) {
        // LEARNER TODO: authenticate, bound page size to 1..50, validate any
        // cursor against the authenticated owner, and return safe summaries.
        throw new UnsupportedOperationException("CAP-54 learner TODO: implement owned journey listing");
    }
}
