package assignments.reboot;

import assignments.insurance.RequestAudit;
import assignments.insurance.RequestCorrelationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("learnerAcceptance")
class Day22ContractTest {
    @Test
    void preservesValidIdAndEchoesItOnError() throws Exception {
        List<String> logged = new ArrayList<>();
        RequestCorrelationFilter filter = new RequestCorrelationFilter(() -> "generated-id", logged::add);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/quotes");
        request.addHeader(RequestCorrelationFilter.HEADER, "client.trace-7");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain endpoint = (req, res) -> ((HttpServletResponse) res).setStatus(400);

        filter.doFilter(request, response, endpoint);

        assertEquals("client.trace-7", response.getHeader(RequestCorrelationFilter.HEADER));
        assertEquals(List.of("client.trace-7"), logged);
    }

    @Test
    void replacesInvalidIdAndNeverPassesRequestSecretsToAudit() throws Exception {
        AtomicReference<String> generated = new AtomicReference<>("safe.id_1");
        List<String> logged = new ArrayList<>();
        RequestAudit audit = logged::add;
        RequestCorrelationFilter filter = new RequestCorrelationFilter(generated::get, audit);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/quotes");
        request.addHeader(RequestCorrelationFilter.HEADER, "../../bad\r\nsecret");
        request.addHeader("Authorization", "Bearer sensitive-token");
        request.setCookies(new jakarta.servlet.http.Cookie("session", "private-cookie"));
        request.setContent("applicant-secret".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain endpoint = (req, res) -> ((HttpServletResponse) res).setStatus(500);

        filter.doFilter(request, response, endpoint);

        assertEquals("safe.id_1", response.getHeader(RequestCorrelationFilter.HEADER));
        assertEquals(List.of("safe.id_1"), logged);
        String auditText = String.join(" ", logged);
        assertFalse(auditText.contains("sensitive-token"));
        assertFalse(auditText.contains("private-cookie"));
        assertFalse(auditText.contains("applicant-secret"));
    }

    @Test
    void doesNotLogSuccessfulResponse() throws Exception {
        List<String> logged = new ArrayList<>();
        RequestCorrelationFilter filter = new RequestCorrelationFilter(() -> "generated.id", logged::add);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> ((HttpServletResponse) res).setStatus(200));
        assertEquals(List.of(), logged);
    }
}
