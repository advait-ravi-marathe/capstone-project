package coverstory.modules.analytics;

/** Public Analytics module boundary for bounded low-sensitivity event ingestion. */
public interface AnalyticsIngestionPort {
    Receipt accept(byte[] utf8Body, String authenticatedOwnerId);
    record Receipt(int status, String code, int acceptedCount) { }
}
