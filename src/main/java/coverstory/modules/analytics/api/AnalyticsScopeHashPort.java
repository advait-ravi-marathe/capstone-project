package coverstory.modules.analytics.api;

/** Keyed, domain-separated pseudonymization for Analytics owner/session identifiers. */
public interface AnalyticsScopeHashPort {
    byte[] hashOwner(String ownerId);
    byte[] hashSession(String pseudonymousSessionId);
}
