package coverstory.application;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.Map;

/** Stable CoverStory extensions on top of RFC 9457 / Spring ProblemDetail fields. */
public record CoverStoryProblemDetail(
        HttpStatus status,
        String code,
        String message,
        Map<String, String> fieldErrors,
        String requestId,
        boolean retryable
) {
    public CoverStoryProblemDetail {
        fieldErrors = Map.copyOf(fieldErrors);
    }

    public ProblemDetail toProblemDetail() {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setType(URI.create("about:blank"));
        detail.setTitle(status.getReasonPhrase());
        // A request path can contain an owner/resource identifier. A constant
        // instance prevents 404 responses from revealing whether it existed.
        detail.setInstance(URI.create("urn:coverstory:api:v1:error"));
        detail.setProperty("code", code);
        detail.setProperty("message", message);
        detail.setProperty("fieldErrors", fieldErrors);
        detail.setProperty("requestId", requestId);
        detail.setProperty("retryable", retryable);
        return detail;
    }
}
