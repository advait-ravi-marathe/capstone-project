package assignments.events;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Starter smoke checks. Learner behavior is covered by the opt-in acceptance suite. */
class EventPolicyInfrastructureTest {
    @Test
    void assignmentTypesAreConstructible() {
        assertNotNull(new EventPolicy());
        assertNotNull(new EventKey("owner", "event"));
        assertNotNull(new IncomingEvent("owner", "event", "payload"));
        assertEquals(4, Decision.values().length);
        assertNotNull(new HashMap<EventKey, String>());
    }
}
