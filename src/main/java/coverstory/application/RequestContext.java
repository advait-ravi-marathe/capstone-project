package coverstory.application;

import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;
import java.util.regex.Pattern;

/** Request correlation inputs; legacy correlationId is normalized to requestId. */
public record RequestContext(String requestId, String correlationId) {
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9._:-]{1,80}");

    public static RequestContext from(HttpServletRequest request) {
        String legacy = request.getHeader("X-Correlation-Id");
        if (legacy == null) legacy = request.getHeader("correlationId");
        return new RequestContext(request.getHeader("X-Request-Id"), legacy);
    }

    public String effectiveRequestId() {
        String current = safe(requestId);
        if (current != null) return current;
        String legacy = safe(correlationId);
        if (legacy != null) return legacy;
        return UUID.randomUUID().toString();
    }

    private static String safe(String candidate) {
        return candidate != null && SAFE_ID.matcher(candidate).matches() ? candidate : null;
    }
}
