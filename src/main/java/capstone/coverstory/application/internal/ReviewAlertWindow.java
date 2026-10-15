package capstone.coverstory.application.internal;

import java.time.Instant;
import java.util.UUID;

/**
 * Learner-owned rule seam for a synthetic operational review window.
 * The fixed threshold in the assignment is a teaching fixture, not an insurer SLA.
 */
public final class ReviewAlertWindow {
    public static final int MIN_ELIGIBLE_REQUESTS = 100;
    public static final int STRICT_ERROR_PERCENT = 5;
    public static final int WINDOWS_TO_FIRE = 3;

    public enum AlertState { NORMAL, PENDING, FIRING, RESOLVED }
    public record Window(UUID id, Instant closedAt, int eligibleRequests, int errorRequests) { }
    public record Decision(AlertState state, int consecutiveQualifyingWindows,
                           boolean firingTransition, boolean resolutionTransition) { }

    /** LEARNER TODO: validate the window, apply the strict percentage/minimum-volume rule,
     * preserve persistence across windows, and emit a single firing/resolution transition. */
    public Decision evaluate(AlertState previousState, int previousQualifyingWindows, Window window) {
        throw new UnsupportedOperationException("CAP-114 learner TODO: implement persisted review alert window rule");
    }
}
