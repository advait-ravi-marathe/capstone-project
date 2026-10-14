package coverstory.application;

import org.springframework.stereotype.Component;

/** Learner-owned status and safe-message mapping for existing draft/payment handlers. */
@Component
public final class ReturnSafeCheckoutAndDraftErrors implements ExceptionToProblemDetail {
    @Override
    public CoverStoryProblemDetail mapException(KnownException ex, RequestContext ctx) {
        // LEARNER TODO: map the known request/session/not-found/conflict/stale/
        // incomplete categories to the saved status/code/message contract.
        // Unknown failures must be generic; never copy exception details,
        // answer values, SQL, or secrets into this response.
        throw new UnsupportedOperationException("CAP-83 learner TODO: implement mapException");
    }
}
