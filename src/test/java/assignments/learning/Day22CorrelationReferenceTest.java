package assignments.learning;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Independent toy filter reference. It does not implement the learner filter. */
class Day22CorrelationReferenceTest {
    static final class ToyFilter extends OncePerRequestFilter {
        private final AtomicInteger ids = new AtomicInteger();
        final List<String> errors = new ArrayList<>();
        @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                                   FilterChain chain) throws java.io.IOException, jakarta.servlet.ServletException {
            String supplied = request.getHeader("X-Trace-Id");
            String chosen = supplied != null && supplied.matches("[A-Za-z0-9._-]{1,16}")
                    ? supplied : "G" + ids.incrementAndGet();
            request.setAttribute("traceId", chosen);
            response.setHeader("X-Trace-Id", chosen);
            chain.doFilter(request, response);
            if (response.getStatus() >= 400) errors.add(chosen);
        }
    }
    @Test void selectsOnceBeforeChainAndAuditsOnlyErrorIdAfterward() throws Exception {
        ToyFilter filter = new ToyFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Trace-Id", "ticket-4");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> {
            assertEquals("ticket-4", req.getAttribute("traceId"));
            assertEquals("ticket-4", ((HttpServletResponse) res).getHeader("X-Trace-Id"));
            ((HttpServletResponse) res).setStatus(400);
        });
        assertEquals(List.of("ticket-4"), filter.errors);

        MockHttpServletRequest unsafe = new MockHttpServletRequest();
        unsafe.addHeader("X-Trace-Id", "bad\nline");
        MockHttpServletResponse ok = new MockHttpServletResponse();
        filter.doFilter(unsafe, ok, (req, res) -> ((HttpServletResponse) res).setStatus(200));
        assertEquals("G1", unsafe.getAttribute("traceId"));
        assertEquals("G1", ok.getHeader("X-Trace-Id"));
        assertEquals(List.of("ticket-4"), filter.errors);

        MockHttpServletRequest unsafeError = new MockHttpServletRequest();
        unsafeError.addHeader("X-Trace-Id", "bad\nline");
        MockHttpServletResponse error = new MockHttpServletResponse();
        filter.doFilter(unsafeError, error, (req, res) -> ((HttpServletResponse) res).setStatus(400));
        assertEquals("G2", error.getHeader("X-Trace-Id"));
        assertEquals(List.of("ticket-4", "G2"), filter.errors);
    }
}
