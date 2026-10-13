package assignments.events;

import java.util.Map;

/**
 * CAP-43 learner exercise. Values compare exactly; do not trim or normalize.
 * Assume a non-null mutable map and sequential calls.
 */
public class EventPolicy {
    /**
     * Classify without changing {@code acceptedEvents}.
     * Null event or null/blank owner, event ID, or payload => INVALID.
     * New composite key => ACCEPT; same key and same payload => DUPLICATE;
     * same key and different payload => CONFLICT.
     */
    public Decision classify(Map<EventKey, String> acceptedEvents, IncomingEvent event) {
        // LEARNER TODO: implement pure classification.
        return null;
    }

    /** Insert payload only when classification is ACCEPT, then return decision. */
    public Decision accept(Map<EventKey, String> acceptedEvents, IncomingEvent event) {
        // LEARNER TODO: classify, and insert only for ACCEPT.
        return null;
    }
}
