package coverstory.modules.catalog;

import coverstory.modules.catalog.api.CurrentQuotePort;
import coverstory.modules.catalog.api.CurrentQuotePort.Snapshot;

import java.time.Clock;
import java.util.UUID;

/** Instructor quote recheck service. It only validates exact synthetic terms and never creates an order. */
public final class QuoteReviewService {
    private final CurrentQuotePort quotes;
    private final Clock clock;
    public QuoteReviewService(CurrentQuotePort quotes, Clock clock) { this.quotes=quotes; this.clock=clock; }

    public Reply validate(UUID owner, Request request) {
        if (owner == null) throw new Unauthorized();
        if (request == null || request.quoteId() == null || request.quoteId().isBlank() || request.quoteId().length()>120 ||
                request.displayedRevision()==null || !validHash(request.displayedTermsHash()) || request.displayedTotalPaise()<=0)
            throw new InvalidRequest();
        Snapshot current = quotes.findCurrent(owner, request.quoteId()).filter(q -> owner.equals(q.ownerId())).orElseThrow(NotFound::new);
        if (!current.available() || current.expiresAt()==null || !current.expiresAt().isAfter(clock.instant()))
            throw new Unavailable();
        if (current.illustration() || !current.taxKnown() || current.totalPaise()==null || current.totalPaise()<=0 ||
                !"INR".equals(current.currency()) || !validHash(current.termsHash())) throw new IncompleteQuote();
        final long delta;
        try { delta=Math.subtractExact(current.totalPaise(),request.displayedTotalPaise()); }
        catch (ArithmeticException overflow) { throw new InvalidRequest(); }
        boolean changed = delta != 0 || !current.termsHash().equals(request.displayedTermsHash()) ||
                !current.revision().equals(request.displayedRevision());
        boolean anyAcceptance = request.acceptedRevision()!=null || request.acceptedTotalPaise()!=null || request.acceptedTermsHash()!=null;
        boolean exactAcceptance = current.revision().equals(request.acceptedRevision()) &&
                current.totalPaise().equals(request.acceptedTotalPaise()) && current.termsHash().equals(request.acceptedTermsHash());
        if (changed && !exactAcceptance) return new Reply("REVIEW_REQUIRED", delta, true, Math.abs(delta)>100,
                current.quoteId(),current.revision(),current.totalPaise(),current.currency(),current.termsHash(),current.expiresAt(),false);
        if (!changed && anyAcceptance && !exactAcceptance) throw new StaleReview();
        return new Reply(changed ? "REVIEWED_NO_ORDER" : "VALIDATED", delta, false, Math.abs(delta)>100,
                current.quoteId(),current.revision(),current.totalPaise(),current.currency(),current.termsHash(),current.expiresAt(),false);
    }
    private static boolean validHash(String hash) { return hash!=null && hash.matches("[a-f0-9]{64}"); }

    public record Request(String quoteId, String displayedRevision, long displayedTotalPaise, String displayedTermsHash,
                          String acceptedRevision, Long acceptedTotalPaise, String acceptedTermsHash) { }
    public record Reply(String status, long deltaPaise, boolean explicitReviewRequired, boolean escalationRequired,
                        String quoteId, String revision, long currentTotalPaise, String currency, String termsHash,
                        java.time.Instant expiresAt, boolean orderCreated) { }
    public static final class InvalidRequest extends RuntimeException { }
    public static final class Unauthorized extends RuntimeException { }
    public static final class NotFound extends RuntimeException { }
    public static final class Unavailable extends RuntimeException { }
    public static final class IncompleteQuote extends RuntimeException { }
    public static final class StaleReview extends RuntimeException { }
}
