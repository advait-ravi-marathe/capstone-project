package coverstory.modules.analytics;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Allowlisted, bounded analytics value; it accepts no URLs, tokens, PII, or arbitrary properties. */
public record BehaviorEvent(UUID eventId, EventName eventName, ScreenKey screenKey,
                            byte[] sessionScopeHash, Instant occurredAt) {
    public BehaviorEvent {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(eventName, "eventName");
        Objects.requireNonNull(screenKey, "screenKey");
        Objects.requireNonNull(sessionScopeHash, "sessionScopeHash");
        Objects.requireNonNull(occurredAt, "occurredAt");
        if (sessionScopeHash.length != 32) {
            throw new IllegalArgumentException("sessionScopeHash must contain 32 bytes");
        }
        sessionScopeHash = sessionScopeHash.clone();
    }

    @Override public byte[] sessionScopeHash() { return sessionScopeHash.clone(); }

    public enum EventName {
        CATALOG_VIEWED("catalog_viewed"), SHORTLIST_CHANGED("shortlist_changed"),
        APPLICATION_STARTED("application_started"), SUPPORT_OPENED("support_opened");

        private final String databaseValue;
        EventName(String databaseValue) { this.databaseValue = databaseValue; }
        public String databaseValue() { return databaseValue; }
    }

    public enum ScreenKey {
        CATALOG("catalog"), SHORTLIST("shortlist"), APPLICATION("application"), SUPPORT("support");

        private final String databaseValue;
        ScreenKey(String databaseValue) { this.databaseValue = databaseValue; }
        public String databaseValue() { return databaseValue; }
    }
}
