package assignments.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Visible learner acceptance tests; expected to fail until EventPolicy TODOs are implemented. */
@Tag("learnerAcceptance")
class EventPolicyAcceptanceTest {
    private EventPolicy policy;
    private Map<EventKey, String> events;

    @BeforeEach
    void setUp() {
        policy = new EventPolicy();
        events = new HashMap<>();
    }

    @Test
    void acceptsNewPairAndStoresIt() {
        IncomingEvent event = event("owner-A", "event-1", "payload");
        assertEquals(Decision.ACCEPT, policy.accept(events, event));
        assertEquals("payload", events.get(new EventKey("owner-A", "event-1")));
    }

    @Test
    void classifyIsPure() {
        IncomingEvent event = event("owner-A", "event-1", "payload");
        assertEquals(Decision.ACCEPT, policy.classify(events, event));
        assertTrue(events.isEmpty());
    }

    @Test
    void identicalReplayIsDuplicateWithoutMutation() {
        EventKey key = new EventKey("owner-A", "event-1");
        events.put(key, "payload");
        Map<EventKey, String> before = new HashMap<>(events);
        assertEquals(Decision.DUPLICATE, policy.accept(events, event("owner-A", "event-1", "payload")));
        assertEquals(before, events);
    }

    @Test
    void changedPayloadConflictsWithoutOverwrite() {
        EventKey key = new EventKey("owner-A", "event-1");
        events.put(key, "original");
        assertEquals(Decision.CONFLICT, policy.accept(events, event("owner-A", "event-1", "changed")));
        assertEquals("original", events.get(key));
    }

    @Test
    void sameEventIdUnderDifferentOwnersIsIndependent() {
        assertEquals(Decision.ACCEPT, policy.accept(events, event("owner-A", "shared", "one")));
        assertEquals(Decision.ACCEPT, policy.accept(events, event("owner-B", "shared", "two")));
        assertEquals(2, events.size());
    }

    @Test
    void compositeKeyDoesNotCollideByConcatenation() {
        assertEquals(Decision.ACCEPT, policy.accept(events, event("ab", "c", "first")));
        assertEquals(Decision.ACCEPT, policy.accept(events, event("a", "bc", "second")));
        assertEquals(2, events.size());
    }

    @Test
    void nullAndBlankFieldsAreInvalidWithoutMutation() {
        assertInvalid(null);
        assertInvalid(event(null, "id", "payload"));
        assertInvalid(event(" ", "id", "payload"));
        assertInvalid(event("owner", null, "payload"));
        assertInvalid(event("owner", "\t", "payload"));
        assertInvalid(event("owner", "id", null));
        assertInvalid(event("owner", "id", "  "));
        assertTrue(events.isEmpty());
    }

    @Test
    void validStringsAreNotTrimmedOrNormalized() {
        assertEquals(Decision.ACCEPT, policy.accept(events, event(" owner ", "id", "payload")));
        assertEquals(Decision.ACCEPT, policy.accept(events, event("owner", "id", "payload")));
        assertEquals(Decision.CONFLICT, policy.classify(events, event(" owner ", "id", "payload ")));
        assertEquals(2, events.size());
    }

    @Test
    void emptyStringIsBlankAndDoesNotCreateAnEntry() {
        assertEquals(Decision.INVALID, policy.accept(events, event("owner", "", "payload")));
        assertTrue(events.isEmpty());
    }

    private void assertInvalid(IncomingEvent event) {
        Map<EventKey, String> before = new HashMap<>(events);
        assertEquals(Decision.INVALID, policy.accept(events, event));
        assertEquals(before, events);
    }

    private static IncomingEvent event(String owner, String id, String payload) {
        return new IncomingEvent(owner, id, payload);
    }
}
