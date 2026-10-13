package assignments.events;

/** Composite identity for an event within its owner scope. */
public record EventKey(String ownerId, String eventId) {}
