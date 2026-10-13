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
        EventKey existing = new EventKey("owner-A", "event-1");
        EventKey unrelated = new EventKey("owner-B", "seed");
        events.put(existing, "stored");
        events.put(unrelated, "leave-me-alone");
        Map<EventKey, String> before = new HashMap<>(events);

        assertEquals(Decision.ACCEPT, policy.classify(events, event("owner-A", "new", "payload")));
        assertEquals(before, events);
        assertEquals(Decision.DUPLICATE, policy.classify(events, event("owner-A", "event-1", "stored")));
        assertEquals(before, events);
        assertEquals(Decision.CONFLICT, policy.classify(events, event("owner-A", "event-1", "changed")));
        assertEquals(before, events);
        assertEquals(Decision.INVALID, policy.classify(events, event("owner-A", " ", "payload")));
        assertEquals(before, events);
    }

    @Test
    void identicalReplayIsDuplicateWithoutMutation() {
        EventKey key = new EventKey("owner-A", "event-1");
        EventKey unrelated = new EventKey("owner-B", "seed");
        events.put(key, "payload");
        events.put(unrelated, "keep");
        Map<EventKey, String> before = new HashMap<>(events);
        assertEquals(Decision.DUPLICATE, policy.accept(events, event("owner-A", "event-1", "payload")));
        assertEquals(before, events);
    }

    @Test
    void changedPayloadConflictsWithoutOverwrite() {
        EventKey key = new EventKey("owner-A", "event-1");
        EventKey unrelated = new EventKey("owner-B", "seed");
        events.put(key, "original");
        events.put(unrelated, "keep");
        Map<EventKey, String> before = new HashMap<>(events);
        assertEquals(Decision.CONFLICT, policy.accept(events, event("owner-A", "event-1", "changed")));
        assertEquals(before, events);
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
        assertEquals(Map.of(new EventKey("owner-seed", "seed"), "unrelated"), events);
    }

    @Test
    void validStringsAreNotTrimmedOrNormalized() {
        assertEquals(Decision.ACCEPT, policy.accept(events, event(" owner ", "id", "payload")));
        assertEquals(Decision.ACCEPT, policy.accept(events, event("owner", "id", "payload")));
        Map<EventKey, String> beforeConflict = new HashMap<>(events);
        assertEquals(Decision.CONFLICT, policy.classify(events, event(" owner ", "id", "payload ")));
        assertEquals(beforeConflict, events);
        assertEquals(2, events.size());
    }

    @Test
    void distinctEventIdsAreNotTrimmedOrNormalized() {
        assertEquals(Decision.ACCEPT, policy.accept(events, event("owner", "id", "first")));
        Map<EventKey, String> afterFirst = new HashMap<>(events);

        assertEquals(Decision.ACCEPT, policy.accept(events, event("owner", " id ", "second")));
        assertEquals("first", events.get(new EventKey("owner", "id")));
        assertEquals("second", events.get(new EventKey("owner", " id ")));
        assertEquals(2, events.size());
        assertTrue(events.containsKey(new EventKey("owner", "id")));
        assertTrue(events.containsKey(new EventKey("owner", " id ")));
        assertEquals("first", afterFirst.get(new EventKey("owner", "id")));
    }

    @Test
    void whitespacePayloadIsStoredAndReplayedExactly() {
        String payload = "  payload\t ";
        IncomingEvent incoming = event("owner", "event", payload);

        assertEquals(Decision.ACCEPT, policy.accept(events, incoming));
        assertEquals(payload, events.get(new EventKey("owner", "event")));
        Map<EventKey, String> beforeReplay = new HashMap<>(events);
        assertEquals(Decision.DUPLICATE, policy.classify(events, incoming));
        assertEquals(beforeReplay, events);
        assertEquals(Decision.DUPLICATE, policy.accept(events, incoming));
        assertEquals(beforeReplay, events);
    }

    @Test
    void emptyStringIsBlankAndDoesNotCreateAnEntry() {
        events.put(new EventKey("other", "seed"), "keep");
        Map<EventKey, String> before = new HashMap<>(events);
        assertEquals(Decision.INVALID, policy.accept(events, event("owner", "", "payload")));
        assertEquals(before, events);
    }

    private void assertInvalid(IncomingEvent event) {
        events.putIfAbsent(new EventKey("owner-seed", "seed"), "unrelated");
        Map<EventKey, String> before = new HashMap<>(events);
        assertEquals(Decision.INVALID, policy.accept(events, event));
        assertEquals(before, events);
    }

    private static IncomingEvent event(String owner, String id, String payload) {
        return new IncomingEvent(owner, id, payload);
    }
}
