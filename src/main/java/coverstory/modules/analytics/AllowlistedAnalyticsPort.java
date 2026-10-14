package coverstory.modules.analytics;

/** Best-effort telemetry must never make a business transaction fail. */
public interface AllowlistedAnalyticsPort {
    void record(BehaviorEvent event);
}
