package assignments.insurance;

/** Client request intentionally carries no authority; ownerId is an untrusted field. */
public record JourneyReadRequest(String journeyId, String ownerId) { }
