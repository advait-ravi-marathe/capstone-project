package coverstory.modules.policy.api;

/** Policy-owned consent decision used before optional telemetry ingestion. */
public interface CollectionConsentPort {
    boolean permitsAnalyticsCollection(String authenticatedOwnerId);
}
