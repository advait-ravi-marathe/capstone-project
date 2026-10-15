package coverstory.modules.analytics;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.analytics.api.AnalyticsReceiptStore;
import coverstory.modules.analytics.api.AnalyticsScopeHashPort;
import coverstory.modules.analytics.api.CAP58Seam;
import coverstory.modules.application.api.ApplicationJourneyOwnershipPort;
import coverstory.modules.policy.api.CollectionConsentPort;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.*;

/** Production Analytics boundary: consent/ownership context -> learner validator -> durable receipts. */
public final class CAP58AnalyticsAdapter implements AnalyticsIngestionPort {
    private static final int MAX_BYTES = 8192;
    private final CAP58Seam validator;
    private final CollectionConsentPort consent;
    private final ApplicationJourneyOwnershipPort journeys;
    private final AnalyticsScopeHashPort hashes;
    private final AnalyticsReceiptStore receipts;
    private final ObjectMapper json;
    private final Clock clock;

    public CAP58AnalyticsAdapter(CAP58Seam validator, CollectionConsentPort consent,
                                 ApplicationJourneyOwnershipPort journeys, AnalyticsScopeHashPort hashes,
                                 AnalyticsReceiptStore receipts, ObjectMapper json, Clock clock) {
        this.validator=Objects.requireNonNull(validator); this.consent=consent; this.journeys=journeys;
        this.hashes=hashes; this.receipts=receipts; this.json=Objects.requireNonNull(json); this.clock=Objects.requireNonNull(clock);
    }

    @Override public Receipt accept(byte[] body, String ownerId) {
        if (body == null || body.length > MAX_BYTES) return new Receipt(413,"BODY_TOO_LARGE",0);
        if (ownerId == null || ownerId.isBlank()) return new Receipt(401,"AUTH_REQUIRED",0);
        if (consent == null) return unavailable();
        final boolean collectionAllowed;
        try { collectionAllowed=consent.permitsAnalyticsCollection(ownerId); }
        catch (RuntimeException unavailable) { return unavailable(); }
        if (!collectionAllowed) return new Receipt(202,"DISCARDED_BY_POLICY",0);
        if (hashes == null || receipts == null) return unavailable();

        InstantHolder received = new InstantHolder(clock.instant());
        Set<String> ownedJourneyIds;
        try {
            Set<UUID> claimed = claimedJourneyIds(body);
            if (claimed.isEmpty()) ownedJourneyIds=Set.of();
            else {
                if (journeys == null) return new Receipt(503,"APPLICATION_OWNERSHIP_UNAVAILABLE",0);
                Set<UUID> owned=journeys.ownedJourneyIds(ownerId,claimed);
                Set<String> values=new HashSet<>(); for(UUID id:owned)values.add(id.toString()); ownedJourneyIds=Set.copyOf(values);
            }
        } catch (RuntimeException unavailable) { return new Receipt(503,"APPLICATION_OWNERSHIP_UNAVAILABLE",0); }

        CAP58Seam.ValidationResult validated;
        try {
            validated=validator.validateAnalyticsBatch(body,
                    new CAP58Seam.TrustedAnalyticsContext(ownerId, true, ownedJourneyIds, received.value));
        } catch (UnsupportedOperationException todo) {
            return new Receipt(501,"VALIDATOR_NOT_IMPLEMENTED",0);
        } catch (RuntimeException failure) { return unavailable(); }
        if (validated == null || validated.status() == null) return unavailable();
        if (validated.status() == CAP58Seam.ResultStatus.DISCARDED_BY_POLICY)
            return new Receipt(202,"DISCARDED_BY_POLICY",0);
        if (validated.status() == CAP58Seam.ResultStatus.INVALID)
            return new Receipt(400,safeFieldCode(validated.fieldCode()),0);
        if (validated.events() == null || validated.events().isEmpty() || validated.events().size() > 10)
            return new Receipt(503,"VALIDATOR_CONTRACT_INVALID",0);
        try {
            List<AnalyticsReceiptStore.ReceiptEvent> batch=new ArrayList<>();
            for (CAP58Seam.SanitizedEvent event:validated.events()) {
                UUID id=UUID.fromString(event.eventId());
                byte[] sessionHash=hashes.hashSession(Objects.requireNonNull(event.pseudonymousSessionId()));
                byte[] payloadHash=canonicalHash(event);
                batch.add(new AnalyticsReceiptStore.ReceiptEvent(id,payloadHash,sessionHash,event.eventType(),event.pageId(),
                        event.stepCode(),event.targetId(),event.durationMs(),received.value));
            }
            AnalyticsReceiptStore.ReceiptResult result=receipts.recordBatch(hashes.hashOwner(ownerId),batch);
            if (result.conflict()) return new Receipt(409,"EVENT_ID_CONFLICT",0);
            return new Receipt(202,result.allDuplicates()?"DUPLICATE":"RECEIVED",result.count());
        } catch (RuntimeException failure) { return unavailable(); }
    }

    private Set<UUID> claimedJourneyIds(byte[] body) {
        try {
            JsonNode root=json.readTree(body), events=root.path("events");
            if(!events.isArray()||events.size()>10)return Set.of();
            Set<UUID> ids=new HashSet<>();
            for(JsonNode event:events) if(event.has("journeyId")&&event.path("journeyId").isTextual()) {
                try { ids.add(UUID.fromString(event.path("journeyId").asText())); }
                catch(IllegalArgumentException ignored) { /* learner validator returns a safe field error */ }
            }
            return Set.copyOf(ids);
        } catch(Exception malformed) { return Set.of(); }
    }

    private byte[] canonicalHash(CAP58Seam.SanitizedEvent event) {
        String canonical=String.join("\u0000",event.eventId(),event.eventType(),event.pageId(),
                Objects.toString(event.stepCode(),""),Objects.toString(event.targetId(),""),
                Objects.toString(event.durationMs(),""),event.pseudonymousSessionId());
        try { return MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8)); }
        catch(java.security.GeneralSecurityException impossible){throw new IllegalStateException(impossible);}
    }
    private static String safeFieldCode(String code){return code!=null&&code.matches("[A-Z][A-Z0-9_]{0,39}")?code:"INVALID_EVENT";}
    private static Receipt unavailable(){return new Receipt(503,"ANALYTICS_UNAVAILABLE",0);}
    private record InstantHolder(java.time.Instant value){}
}
