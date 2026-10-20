package assignments.insurance;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/** Learner-owned request correlation filter; IDs are bounded before they are echoed or logged. */
public final class RequestCorrelationFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Request-Id";
    private static final Pattern VALID_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");
    private final Supplier<String> idGenerator;
    private final RequestAudit audit;

    public RequestCorrelationFilter(Supplier<String> idGenerator, RequestAudit audit) {
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.audit = Objects.requireNonNull(audit);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // LEARNER TODO: preserve a valid incoming ID, otherwise generate a valid one;
        // set it in response/header and request attribute; after the chain, log only
        // the ID for error responses. Never log body, cookie, auth token, or ID as metric label.
        throw new UnsupportedOperationException("Day 22 learner TODO: implement safe request correlation");
    }

    static boolean isValid(String candidate) {
        return candidate != null && VALID_ID.matcher(candidate).matches();
    }
}
