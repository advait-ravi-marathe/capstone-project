package assignments.reboot;

import assignments.insurance.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.security.Principal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Test-only MVC adapter. Principal and quote facts are fixtures; this is not production auth or a route. */
@Tag("learnerAcceptance")
class Day34IssueHttpMappingAcceptanceTest {
    private static final String BODY = "{\"journeyId\":\"journey-a\",\"quoteVersionId\":\"quote-a\",\"clientRequestId\":\"req-a\",\"effectiveDate\":\"2026-12-01\"}";
    private final ObjectMapper json = new ObjectMapper();

    @Test void day34CreateAndReplayExposeOriginalPolicyAndLocation() throws Exception {
        var fixture = fixture();
        java.security.Principal principal = () -> "owner-a";
        var first = fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated()).andReturn();
        assertEquals("/api/policies/policy-a", first.getResponse().getHeader("Location"));
        var second = fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk()).andReturn();
        assertNull(second.getResponse().getHeader("Location"));
        assertEquals(json.readTree(first.getResponse().getContentAsString()), json.readTree(second.getResponse().getContentAsString()));
        assertEquals(1, fixture.constructed.get(), "replay reuses the original candidate");
    }

    @Test void day34ChangedIntentConflictsWithoutReplacingReceipt() throws Exception {
        var fixture = fixture(); java.security.Principal principal = () -> "owner-a";
        fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isCreated());
        for (String changed : new String[]{BODY.replace("quote-a", "quote-b"), BODY.replace("journey-a", "journey-b"), BODY.replace("2026-12-01", "2026-12-02")}) {
            fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(changed)).andExpect(status().isConflict());
            fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isOk());
        }
        assertEquals(1, fixture.constructed.get());
    }

    @Test void day34InvalidJsonOrCommandDoesNotReachIssue() throws Exception {
        var fixture = fixture(); java.security.Principal principal = () -> "owner-a";
        fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content("{broken")).andExpect(status().isBadRequest());
        fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY.replace("req-a", " "))).andExpect(status().isBadRequest());
        assertEquals(0, fixture.constructed.get());
    }

    @Test void day34ForeignAndAbsentQuotesAreIndistinguishable() throws Exception {
        var fixture = fixture(); java.security.Principal principal = () -> "owner-a";
        var foreign = fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY.replace("quote-a", "quote-foreign"))).andExpect(status().isNotFound()).andReturn();
        var absent = fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY.replace("quote-a", "quote-absent"))).andExpect(status().isNotFound()).andReturn();
        assertEquals(foreign.getResponse().getContentAsString(), absent.getResponse().getContentAsString());
        assertFalse(foreign.getResponse().getContentAsString().contains("owner-b"));
        assertEquals(0, fixture.constructed.get());
    }

    @Test void day34PrincipalDeterminesOwnerAndReceiptScope() throws Exception {
        var fixture = fixture();
        fixture.mvc.perform(post("/api/policies").principal(() -> "owner-a").contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isCreated());
        String body = BODY.replace("quote-a", "quote-owner-b");
        var response = fixture.mvc.perform(post("/api/policies").principal(() -> "owner-b").contentType(APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andReturn();
        assertTrue(response.getResponse().getContentAsString().contains("owner-b"));
        assertEquals(2, fixture.constructed.get());
    }

    @Test void day34UnscopedReceiptCannotDiscloseForeignPolicy() throws Exception {
        var fixture=fixture();
        fixture.controller.seedReceipt("owner-a:req-a",new PolicySnapshot("foreign-policy","owner-b","journey-b","quote-b","home-secure-v1","rate-v1",new BigDecimal("999.00"),"INR",LocalDate.parse("2026-12-01"),PolicyStatus.ISSUED,1,java.util.List.of(),null));
        var response=fixture.mvc.perform(post("/api/policies").principal(()->"owner-a").contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isNotFound()).andReturn();
        assertFalse(response.getResponse().getContentAsString().contains("foreign-policy"));
        assertFalse(response.getResponse().getContentAsString().contains("owner-b"));
        assertEquals(0,fixture.constructed.get());
    }

    @Test void day34ForgedOwnerFieldCannotOverridePrincipal() throws Exception {
        var fixture=fixture();
        String forged=BODY.substring(0,BODY.length()-1)+",\"ownerId\":\"owner-b\"}";
        fixture.mvc.perform(post("/api/policies").principal(()->"owner-a").contentType(APPLICATION_JSON).content(forged)).andExpect(status().isBadRequest());
        assertEquals(0,fixture.constructed.get());
    }

    @Test void day34MissingPrincipalDoesNotReachCommand() throws Exception {
        var fixture = fixture();
        fixture.mvc.perform(post("/api/policies").contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isUnauthorized());
        assertEquals(0, fixture.constructed.get());
    }

    @Test void day34UnexpectedFailureIsSanitizedAndNeverReportedAsSuccess() throws Exception {
        var fixture = fixture();
        var boom = BODY.replace("req-a", "boom");
        var response = fixture.mvc.perform(post("/api/policies").principal(() -> "owner-a")
                        .contentType(APPLICATION_JSON).content(boom))
                .andExpect(status().isInternalServerError()).andReturn();
        assertEquals("{\"code\":\"INTERNAL_ERROR\",\"message\":\"Request could not be completed\"}",
                response.getResponse().getContentAsString());
        assertFalse(response.getResponse().getContentAsString().contains("secret marker"));
        assertEquals(0, fixture.constructed.get());
    }

    private Fixture fixture() {
        AtomicInteger constructed = new AtomicInteger();
        PolicyIssueSnapshotFactory factory = (request, owner) -> {
            constructed.incrementAndGet();
            return new PolicySnapshot("policy-a", owner, request.journeyId(), request.quoteVersionId(), "home-secure-v1", "training-rate-v1",
                    new BigDecimal("125.00"), "INR", request.effectiveDate(), PolicyStatus.ISSUED, 1, java.util.List.of(), null);
        };
        var service = new PolicyLifecycleService(Clock.fixed(java.time.Instant.parse("2026-11-19T12:00:00Z"), java.time.ZoneOffset.UTC), factory);
        var controller = new FixtureIssueController(service);
        var strictJson = JsonMapper.builder().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();
        return new Fixture(constructed, controller, MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new SanitizedTestFailureAdvice())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(strictJson)).build());
    }
    private record Fixture(AtomicInteger constructed, FixtureIssueController controller, MockMvc mvc) {}

    @RestController
    static class FixtureIssueController {
        private final PolicyLifecycleService service;
        private final Map<String, PolicySnapshot> receipts = new ConcurrentHashMap<>();
        FixtureIssueController(PolicyLifecycleService service) { this.service = service; }
        void seedReceipt(String key,PolicySnapshot snapshot){receipts.put(key,snapshot);}

        @PostMapping("/api/policies") ResponseEntity<?> issue(@RequestBody PolicyIssueRequest request, Principal principal) {
            if (principal == null) return ResponseEntity.status(401).body(new ApiError("UNAUTHENTICATED", "Sign in required"));
            if (request == null || request.journeyId() == null || request.journeyId().isBlank()
                    || request.quoteVersionId() == null || request.quoteVersionId().isBlank()
                    || request.clientRequestId() == null || request.clientRequestId().isBlank() || request.effectiveDate() == null)
                return ResponseEntity.badRequest().body(new ApiError("INVALID_REQUEST", "Request validation failed"));
            String owner = principal.getName();
            if ("boom".equals(request.clientRequestId())) throw new IllegalStateException("secret marker");
            boolean accessible = ("quote-a".equals(request.quoteVersionId()) || "quote-b".equals(request.quoteVersionId())) && "owner-a".equals(owner)
                    || "quote-owner-b".equals(request.quoteVersionId()) && "owner-b".equals(owner);
            if (!accessible) return ResponseEntity.status(404).body(new ApiError("RESOURCE_NOT_FOUND", "Resource not found"));
            var facts = new PolicyIssueFacts(owner, owner, request.journeyId(), request.quoteVersionId(), true, true,
                    LocalDate.parse("2027-01-01"), true);
            var prior = Optional.ofNullable(receipts.get(owner + ":" + request.clientRequestId()));
            PolicyIssueResponse mapped = service.issueResponse(request, facts, prior);
            if (mapped.policy() != null) receipts.putIfAbsent(owner + ":" + request.clientRequestId(), mapped.policy());
            var builder = ResponseEntity.status(mapped.httpStatus());
            if (mapped.location() != null) builder.location(mapped.location());
            return builder.body(mapped.policy() != null ? mapped.policy() : mapped.error());
        }
    }

    @RestControllerAdvice
    static class SanitizedTestFailureAdvice {
        @ExceptionHandler(HttpMessageNotReadableException.class)
        ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException failure) {
            return ResponseEntity.badRequest().body(new ApiError("INVALID_REQUEST", "Request validation failed"));
        }

        @ExceptionHandler(Exception.class)
        ResponseEntity<ApiError> unexpected(Exception failure) {
            return ResponseEntity.status(500).body(new ApiError("INTERNAL_ERROR", "Request could not be completed"));
        }
    }
}
