package assignments.events;

/** Input event used by the CAP-43 event-policy exercise. */
public record IncomingEvent(String ownerId, String eventId, String payload) {}
