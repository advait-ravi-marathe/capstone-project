package capstone.coverstory.payments;

/** Synthetic read-only reconciliation attempt; an epoch never authorizes a new charge. */
public record ReconciliationAttempt(String ownerId, String event, Long replayEpoch,
                                    Integer attemptNo, boolean terminal) { }
