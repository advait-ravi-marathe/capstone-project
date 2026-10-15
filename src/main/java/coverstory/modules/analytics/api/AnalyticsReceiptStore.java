package coverstory.modules.analytics.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Analytics-owned, transactional receipts; implementations persist allowlisted fields only. */
public interface AnalyticsReceiptStore {
    ReceiptResult recordBatch(byte[] ownerHash, List<ReceiptEvent> events);
    record ReceiptEvent(UUID eventId, byte[] payloadHash, byte[] sessionHash, String eventType,
                        String pageId, String stepCode, String targetId, Integer durationMs, Instant receivedAt) { }
    record ReceiptResult(boolean conflict, boolean allDuplicates, int count) { }
}
