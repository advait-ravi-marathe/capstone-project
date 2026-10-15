package coverstory.modules.analytics;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Independent test-only HTTP/receipt and browser-queue witness; never called by production. */
final class CAP58InstructorIngressHarness {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final Map<String, byte[]> receipts = new HashMap<>();
    private final Set<String> authorizedJourneys;

    CAP58InstructorIngressHarness(Set<String> authorizedJourneys) { this.authorizedJourneys = Set.copyOf(authorizedJourneys); }

    Receipt ingest(byte[] body, boolean collectionAllowed, boolean authenticated, String owner, String journey) throws Exception {
        if (body.length > 8192) return new Receipt(413, "BODY_TOO_LARGE", 0);
        if (!collectionAllowed) return new Receipt(202, "DISCARDED_BY_POLICY", 0);
        if (!authenticated) return new Receipt(401, "AUTH_REQUIRED", 0);
        if (journey != null && (!authorizedJourneys.contains(owner + ":" + journey))) return new Receipt(403, "JOURNEY_NOT_AUTHORIZED", 0);
        JsonNode root;
        try { root = JSON.readTree(body); } catch (Exception invalid) { return new Receipt(400, "INVALID_JSON", 0); }
        JsonNode events = root.path("events");
        if (!events.isArray() || events.isEmpty() || events.size() > 10) return new Receipt(400, "INVALID_EVENT_COUNT", 0);
        Set<String> ids = new HashSet<>();
        for (JsonNode event : events) {
            if (!referenceAccepts(event)) return new Receipt(400, "INVALID_EVENT", 0);
            String id = event.path("eventId").asText();
            if (!ids.add(id)) return new Receipt(400, "DUPLICATE_ID_IN_BATCH", 0);
            if (event.has("journeyId") && (!Objects.equals(journey, event.path("journeyId").asText()) || journey == null))
                return new Receipt(403, "JOURNEY_NOT_AUTHORIZED", 0);
        }
        boolean duplicate = true;
        Map<String, byte[]> staged = new HashMap<>();
        for (JsonNode event : events) {
            String id = event.path("eventId").asText(); byte[] hash = hash(JSON.writeValueAsBytes(event));
            byte[] prior = receipts.get(id);
            if (prior == null) { duplicate = false; staged.put(id, hash); }
            else if (!MessageDigest.isEqual(prior, hash)) return new Receipt(409, "EVENT_ID_CONFLICT", 0);
        }
        receipts.putAll(staged);
        return new Receipt(202, duplicate ? "DUPLICATE" : "RECEIVED", events.size());
    }

    private static boolean referenceAccepts(JsonNode event) {
        Set<String> fields = Set.of("eventId", "eventType", "schemaVersion", "pseudonymousSessionId", "pageId", "targetId", "optionalDurationMs", "clientTime", "stepCode", "journeyId");
        for (String name : event.propertyNames()) if (!fields.contains(name)) return false;
        if (event.has("journeyId")) return false; // client IDs are not authoritative owner links
        if (!event.path("eventId").asText().matches("(?i)[0-9a-f-]{36}") || !event.path("pseudonymousSessionId").asText().matches("(?i)[0-9a-f-]{36}")) return false;
        if (event.path("schemaVersion").asInt(-1) != 1 || !Set.of("health_discovery", "compare", "quote_review", "home", "checkout", "onboarding", "final_review").contains(event.path("pageId").asText())) return false;
        String type = event.path("eventType").asText();
        if (type.equals("PAGE_VIEW") && event.has("targetId")) return false;
        if (type.equals("CONTROL_CLICK") && !Set.of("compare_plan", "save_draft").contains(event.path("targetId").asText())) return false;
        if (!type.equals("PAGE_VIEW") && !type.equals("CONTROL_CLICK")) return false;
        if (event.has("optionalDurationMs") && (!event.path("optionalDurationMs").isIntegralNumber() || event.path("optionalDurationMs").asInt(-1) < 0 || event.path("optionalDurationMs").asInt() > 300000)) return false;
        return true;
    }
    private static byte[] hash(byte[] value) throws Exception { return MessageDigest.getInstance("SHA-256").digest(value); }
    record Receipt(int status, String code, int acceptedCount) { }

    /** Bounded, lossy telemetry queue. Delivery failure never controls essential app actions. */
    static final class BrowserQueue {
        private final ArrayDeque<byte[]> pending = new ArrayDeque<>();
        boolean enqueue(byte[] event) { if (pending.size() == 20) return false; pending.add(event.clone()); return true; }
        Delivery flush(java.util.function.IntPredicate responseStatus) {
            int sent = 0, tries = 0, dropped = 0;
            while (!pending.isEmpty()) {
                byte[] next = pending.peek(); boolean delivered = false;
                while (tries < 3 && !delivered) { tries++; delivered = responseStatus.test(tries); }
                if (delivered) { pending.remove(); sent++; }
                else { pending.remove(); dropped++; break; }
            }
            return new Delivery(sent, tries, dropped, pending.size());
        }
        int size() { return pending.size(); }
    }
    record Delivery(int sent, int attempts, int dropped, int queued) { }
}
