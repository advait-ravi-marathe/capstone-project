package assignments.teaching;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** A recording adapter reference, not transaction or durable idempotency evidence. */
class ParcelDeskReferenceTest {
    record SendParcel(String requestKey, String parcelId) { }
    record Ticket(String ticketId, String parcelId) { }
    enum Kind { CREATED, REPLAYED, CONFLICT }
    record Decision(Kind kind, Ticket ticket) { }
    interface Dispatcher { Decision dispatch(SendParcel request, String actor); }

    static class Desk {
        private final Dispatcher dispatcher;
        Desk(Dispatcher dispatcher) { this.dispatcher = dispatcher; }
        ResponseEntity<?> send(SendParcel request, Principal principal) {
            if (principal == null) return ResponseEntity.status(401).body(Map.of("code", "NO_IDENTITY"));
            Decision decision = dispatcher.dispatch(request, principal.getName());
            return switch (decision.kind()) {
                case CREATED -> ResponseEntity.status(201).body(decision.ticket());
                case REPLAYED -> ResponseEntity.ok(decision.ticket());
                case CONFLICT -> ResponseEntity.status(409).body(Map.of("code", "PARCEL_CONFLICT"));
            };
        }
    }

    static class RecordingDispatcher implements Dispatcher {
        final Decision decision;
        int calls;
        SendParcel seenRequest;
        String seenActor;
        RecordingDispatcher(Decision decision) { this.decision = decision; }
        @Override public Decision dispatch(SendParcel request, String actor) {
            calls++; seenRequest = request; seenActor = actor; return decision;
        }
    }

    @Test void adapterReturnsTheDecisionAndTrustedActor() {
        SendParcel request = new SendParcel("key-7", "parcel-9");
        Ticket ticket = new Ticket("ticket-2", "parcel-9");
        for (Kind kind : new Kind[]{Kind.CREATED, Kind.REPLAYED}) {
            RecordingDispatcher service = new RecordingDispatcher(new Decision(kind, ticket));
            ResponseEntity<?> response = new Desk(service).send(request, () -> "clerk-a");
            assertEquals(kind == Kind.CREATED ? 201 : 200, response.getStatusCode().value());
            assertSame(ticket, response.getBody());
            assertEquals(1, service.calls);
            assertSame(request, service.seenRequest);
            assertEquals("clerk-a", service.seenActor);
        }
    }

    @Test void conflictIsNotSuccessAndAnonymousDoesNotDispatch() {
        SendParcel request = new SendParcel("key-7", "different-parcel");
        RecordingDispatcher service = new RecordingDispatcher(new Decision(Kind.CONFLICT, null));
        Desk desk = new Desk(service);
        ResponseEntity<?> anonymous = desk.send(request, null);
        assertEquals(401, anonymous.getStatusCode().value());
        assertEquals(0, service.calls);
        ResponseEntity<?> conflict = desk.send(request, () -> "clerk-a");
        assertEquals(409, conflict.getStatusCode().value());
        assertEquals(Map.of("code", "PARCEL_CONFLICT"), conflict.getBody());
        assertEquals(1, service.calls);
    }
}
