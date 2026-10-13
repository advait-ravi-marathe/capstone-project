package assignments.events;

/** HTTP request shape provided for the CAP-43 request-validation slice. */
public record EventRequest(String ownerId, String eventId, String payload) {}
