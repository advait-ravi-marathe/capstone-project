package capstone.coverstory.support.internal;

import java.math.BigDecimal;
import java.util.List;
import java.util.OptionalLong;

/** Learner-owned mock usage accounting. AI estimates use separate currency/provenance from premium paise. */
public final class ExplanationUsageLedger {
    private ExplanationUsageLedger() { }
    public record Usage(String ownerId,String requestId,String eventId,OptionalLong inputTokens,OptionalLong outputTokens) { }
    public record Tariff(String version,String currency,BigDecimal inputPerMillion,BigDecimal outputPerMillion) { }
    public record Entry(String ownerId,String requestId,String eventId,String status,BigDecimal amount,String currency,String tariffVersion,String provenance) { }
    public record Ledger(List<Entry> entries) { }
    public static Ledger record(Ledger ledger,Usage usage,Tariff tariff) {
        // TODO(CAP-113): deduplicate by owner/request and retain UNKNOWN rather than inventing missing usage.
        throw new UnsupportedOperationException("TODO(CAP-113): implement deterministic mock usage ledger");
    }
}
