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
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
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
        var expected = new PolicySnapshot("policy-a", "owner-a", "journey-a", "quote-a", "home-secure-v1", "training-rate-v1",
                new BigDecimal("125.00"), "INR", LocalDate.parse("2026-12-01"), PolicyStatus.ISSUED, 1, java.util.List.of(), null);
        assertEquals(json.readTree(json.writeValueAsString(expected)), json.readTree(first.getResponse().getContentAsString()));
        var second = fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk()).andReturn();
        assertNull(second.getResponse().getHeader("Location"));
        assertEquals(json.readTree(json.writeValueAsString(expected)), json.readTree(second.getResponse().getContentAsString()));
        assertEquals(1, fixture.constructed.get(), "replay reuses the original candidate");
        verify(fixture.service,times(1)).issue(any(PolicyIssueRequest.class),eq("owner-a"));
        assertEquals(1, fixture.controller.receiptCount());
    }

    @Test void day34ChangedIntentConflictsWithoutReplacingReceipt() throws Exception {
        var fixture = fixture(); java.security.Principal principal = () -> "owner-a";
        fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isCreated());
        var original = json.readTree(fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        fixture.controller.facts.put("owner-a:quote-a", new PolicyIssueFacts("owner-a", "owner-a", "journey-a", "quote-a", true, true,
                LocalDate.parse("2026-11-18"), true)); // catalog expiry changes after the receipt was created
        for (String changed : new String[]{BODY.replace("quote-a", "quote-b"), BODY.replace("journey-a", "journey-b"), BODY.replace("2026-12-01", "2026-12-02")}) {
            var conflict=fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(changed)).andExpect(status().isConflict()).andReturn();
            assertNull(conflict.getResponse().getHeader("Location"));
            assertEquals("{\"code\":\"IDEMPOTENCY_CONFLICT\",\"message\":\"Request key conflicts with an earlier request\"}",conflict.getResponse().getContentAsString());
            var replay=fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isOk()).andReturn();
            assertEquals(original,json.readTree(replay.getResponse().getContentAsString()),"conflicts and later catalog expiry leave original receipt unchanged");
        }
        assertEquals(1, fixture.constructed.get()); assertEquals(1,fixture.controller.receiptCount()); verify(fixture.service,times(1)).issue(any(PolicyIssueRequest.class),eq("owner-a"));
    }

    @Test void day34InvalidJsonOrCommandDoesNotReachIssue() throws Exception {
        var fixture = fixture(); java.security.Principal principal = () -> "owner-a";
        fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content("{broken")).andExpect(status().isBadRequest());
        fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY.replace("req-a", " "))).andExpect(status().isBadRequest());
        fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY.replace("journey-a", ""))).andExpect(status().isBadRequest());
        fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY.replace("quote-a", ""))).andExpect(status().isBadRequest());
        fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY.replace("2026-12-01", ""))).andExpect(status().isBadRequest());
        fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY.replace("\"journeyId\":\"journey-a\",", ""))).andExpect(status().isBadRequest());
        fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY.replace("\"quoteVersionId\":\"quote-a\",", ""))).andExpect(status().isBadRequest());
        fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY.replace(",\"effectiveDate\":\"2026-12-01\"", ""))).andExpect(status().isBadRequest());
        fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY.replace(",\"clientRequestId\":\"req-a\"", ""))).andExpect(status().isBadRequest());
        assertEquals(0, fixture.constructed.get()); verify(fixture.service,never()).issue(any(PolicyIssueRequest.class),anyString()); assertEquals(0,fixture.controller.receiptCount());
    }

    @Test void day34ForeignAndAbsentQuotesAreIndistinguishable() throws Exception {
        var fixture = fixture(); java.security.Principal principal = () -> "owner-a";
        var foreign = fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY.replace("quote-a", "quote-foreign"))).andExpect(status().isNotFound()).andReturn();
        var absent = fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(BODY.replace("quote-a", "quote-absent"))).andExpect(status().isNotFound()).andReturn();
        assertEquals(foreign.getResponse().getContentAsString(), absent.getResponse().getContentAsString());
        assertFalse(foreign.getResponse().getContentAsString().contains("owner-b"));
        assertEquals("{\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Resource not found\"}",foreign.getResponse().getContentAsString());
        assertEquals("{\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Resource not found\"}",absent.getResponse().getContentAsString());
        assertNull(foreign.getResponse().getHeader("Location")); assertNull(absent.getResponse().getHeader("Location"));
        assertEquals(0, fixture.constructed.get()); verify(fixture.service,never()).issue(any(PolicyIssueRequest.class),anyString()); assertEquals(0,fixture.controller.receiptCount());
        verify(fixture.service,times(1)).issueResponse(any(PolicyIssueRequest.class),eq(fixture.controller.facts.get("owner-a:quote-foreign")),any());

        for(String quote: new String[]{"quote-unselected","quote-ineligible","quote-expired","quote-stale","quote-mismatch"}) {
            String body=BODY.replace("quote-a",quote);
            var rejected=fixture.mvc.perform(post("/api/policies").principal(principal).contentType(APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound()).andReturn();
            assertEquals("{\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Resource not found\"}",rejected.getResponse().getContentAsString());
            assertNull(rejected.getResponse().getHeader("Location"));
        }
        assertEquals(0, fixture.constructed.get()); verify(fixture.service,never()).issue(any(PolicyIssueRequest.class),anyString()); assertEquals(0,fixture.controller.receiptCount());
    }

    @Test void day34PrincipalDeterminesOwnerAndReceiptScope() throws Exception {
        var fixture = fixture();
        fixture.mvc.perform(post("/api/policies").principal(() -> "owner-a").contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isCreated());
        String body = BODY.replace("journey-a", "journey-b").replace("quote-a", "quote-owner-b");
        var response = fixture.mvc.perform(post("/api/policies").principal(() -> "owner-b").contentType(APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andReturn();
        var expected=new PolicySnapshot("policy-b","owner-b","journey-b","quote-owner-b","household-cover-v4","rate-eur-v4",new BigDecimal("310.00"),"EUR",
                LocalDate.parse("2026-12-01"),PolicyStatus.ISSUED,1,java.util.List.of(),null);
        assertEquals(json.readTree(json.writeValueAsString(expected)),json.readTree(response.getResponse().getContentAsString()));
        assertEquals(2, fixture.constructed.get()); verify(fixture.service,times(2)).issue(any(PolicyIssueRequest.class),anyString());
    }

    @Test void day34UnscopedReceiptCannotDiscloseForeignPolicy() throws Exception {
        var fixture=fixture();
        fixture.controller.seedReceipt("owner-a:req-a",new PolicySnapshot("foreign-policy","owner-b","journey-b","quote-b","home-secure-v1","rate-v1",new BigDecimal("999.00"),"INR",LocalDate.parse("2026-12-01"),PolicyStatus.ISSUED,1,java.util.List.of(),null));
        var response=fixture.mvc.perform(post("/api/policies").principal(()->"owner-a").contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isNotFound()).andReturn();
        assertFalse(response.getResponse().getContentAsString().contains("foreign-policy"));
        assertFalse(response.getResponse().getContentAsString().contains("owner-b"));
        assertEquals("{\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Resource not found\"}",response.getResponse().getContentAsString());
        assertNull(response.getResponse().getHeader("Location"));
        assertEquals(0,fixture.constructed.get());
        assertEquals(1,fixture.controller.receiptCount()); assertEquals("foreign-policy",fixture.controller.receipt("owner-a:req-a").policyId()); verify(fixture.service,never()).issue(any(PolicyIssueRequest.class),anyString());
    }

    @Test void day34ForgedOwnerFieldCannotOverridePrincipal() throws Exception {
        var fixture=fixture();
        String forged=BODY.substring(0,BODY.length()-1)+",\"ownerId\":\"owner-b\"}";
        var response=fixture.mvc.perform(post("/api/policies").principal(()->"owner-a").contentType(APPLICATION_JSON).content(forged)).andExpect(status().isBadRequest()).andReturn();
        assertEquals("{\"code\":\"INVALID_REQUEST\",\"message\":\"Request validation failed\"}",response.getResponse().getContentAsString());
        assertNull(response.getResponse().getHeader("Location"));
        assertEquals(0,fixture.constructed.get());
    }

    @Test void day34MissingPrincipalDoesNotReachCommand() throws Exception {
        var fixture = fixture();
        var response=fixture.mvc.perform(post("/api/policies").contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isUnauthorized()).andReturn();
        assertEquals("{\"code\":\"UNAUTHENTICATED\",\"message\":\"Sign in required\"}",response.getResponse().getContentAsString());
        assertNull(response.getResponse().getHeader("Location"));
        assertEquals(0, fixture.constructed.get());
    }

    @Test void day34UnexpectedFailureIsSanitizedAndNeverReportedAsSuccess() throws Exception {
        var fixture = fixture();
        var boom = BODY.replace("quote-a", "quote-throw").replace("req-a", "req-throw");
        var response = fixture.mvc.perform(post("/api/policies").principal(() -> "owner-a")
                        .contentType(APPLICATION_JSON).content(boom))
                .andExpect(status().isInternalServerError()).andReturn();
        assertEquals("{\"code\":\"INTERNAL_ERROR\",\"message\":\"Request could not be completed\"}",
                response.getResponse().getContentAsString());
        assertFalse(response.getResponse().getContentAsString().contains("secret marker"));
        assertEquals(1,fixture.constructed.get(),"the valid command reaches the downstream factory that throws");
        verify(fixture.service,times(1)).issue(any(PolicyIssueRequest.class),eq("owner-a"));
        assertEquals(0,fixture.controller.receiptCount());
    }

    private Fixture fixture() {
        AtomicInteger constructed = new AtomicInteger();
        Map<String, PolicyIssueFacts> factMap = new HashMap<>();
        factMap.put("owner-a:quote-a",new PolicyIssueFacts("owner-a","owner-a","journey-a","quote-a",true,true,LocalDate.parse("2027-01-01"),true));
        factMap.put("owner-a:journey-b:quote-a",new PolicyIssueFacts("owner-a","owner-a","journey-b","quote-a",true,true,LocalDate.parse("2027-01-01"),true));
        factMap.put("owner-a:quote-foreign",new PolicyIssueFacts("owner-a","owner-b","journey-a","quote-foreign",true,true,LocalDate.parse("2027-01-01"),true));
        factMap.put("owner-a:quote-b",new PolicyIssueFacts("owner-a","owner-a","journey-a","quote-b",true,true,LocalDate.parse("2027-01-01"),true));
        factMap.put("owner-a:quote-unselected",new PolicyIssueFacts("owner-a","owner-a","journey-a","quote-unselected",false,true,LocalDate.parse("2027-01-01"),true));
        factMap.put("owner-a:quote-ineligible",new PolicyIssueFacts("owner-a","owner-a","journey-a","quote-ineligible",true,false,LocalDate.parse("2027-01-01"),true));
        factMap.put("owner-a:quote-expired",new PolicyIssueFacts("owner-a","owner-a","journey-a","quote-expired",true,true,LocalDate.parse("2026-11-18"),true));
        factMap.put("owner-a:quote-stale",new PolicyIssueFacts("owner-a","owner-a","journey-a","quote-stale",true,true,LocalDate.parse("2026-11-18"),true));
        factMap.put("owner-a:quote-mismatch",new PolicyIssueFacts("owner-a","owner-a","journey-other","quote-mismatch",true,true,LocalDate.parse("2027-01-01"),true));
        factMap.put("owner-a:quote-throw",new PolicyIssueFacts("owner-a","owner-a","journey-a","quote-throw",true,true,LocalDate.parse("2027-01-01"),true));
        factMap.put("owner-b:quote-owner-b",new PolicyIssueFacts("owner-b","owner-b","journey-b","quote-owner-b",true,true,LocalDate.parse("2027-01-01"),true));
        PolicyIssueSnapshotFactory factory = (request, owner) -> {
            constructed.incrementAndGet();
            if("quote-throw".equals(request.quoteVersionId())) throw new IllegalStateException("secret marker");
            String policyId=owner.equals("owner-b")?"policy-b":"policy-a";
            if("owner-b".equals(owner)) return new PolicySnapshot(policyId,owner,"journey-b","quote-owner-b","household-cover-v4","rate-eur-v4",new BigDecimal("310.00"),"EUR",request.effectiveDate(),PolicyStatus.ISSUED,1,java.util.List.of(),null);
            return new PolicySnapshot(policyId,owner,"journey-a","quote-a","home-secure-v1","training-rate-v1",new BigDecimal("125.00"),"INR",request.effectiveDate(),PolicyStatus.ISSUED,1,java.util.List.of(),null);
        };
        var service = spy(new PolicyLifecycleService(Clock.fixed(java.time.Instant.parse("2026-11-19T12:00:00Z"), java.time.ZoneOffset.UTC), factory));
        var controller = new FixtureIssueController(service,factMap);
        var strictJson = JsonMapper.builder().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();
        return new Fixture(constructed, service, controller, MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new SanitizedTestFailureAdvice())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(strictJson)).build());
    }
    private record Fixture(AtomicInteger constructed, PolicyLifecycleService service, FixtureIssueController controller, MockMvc mvc) {}

    @RestController
    static class FixtureIssueController {
        private final PolicyLifecycleService service;
        private final Map<String,PolicyIssueFacts> facts;
        private final Map<String, PolicySnapshot> receipts = new ConcurrentHashMap<>();
        FixtureIssueController(PolicyLifecycleService service,Map<String,PolicyIssueFacts> facts) { this.service = service;this.facts=facts; }
        void seedReceipt(String key,PolicySnapshot snapshot){receipts.put(key,snapshot);}
        PolicySnapshot receipt(String key){return receipts.get(key);}
        int receiptCount(){return receipts.size();}

        @PostMapping("/api/policies") ResponseEntity<?> issue(@RequestBody PolicyIssueRequest request, Principal principal) {
            if (principal == null) return ResponseEntity.status(401).body(new ApiError("UNAUTHENTICATED", "Sign in required"));
            if (request == null || request.journeyId() == null || request.journeyId().isBlank()
                    || request.quoteVersionId() == null || request.quoteVersionId().isBlank()
                    || request.clientRequestId() == null || request.clientRequestId().isBlank() || request.effectiveDate() == null)
                return ResponseEntity.badRequest().body(new ApiError("INVALID_REQUEST", "Request validation failed"));
            String owner = principal.getName();
            var quoteFacts=facts.get(owner+":"+request.journeyId()+":"+request.quoteVersionId());
            if(quoteFacts==null) quoteFacts=facts.get(owner+":"+request.quoteVersionId());
            if (quoteFacts==null) return ResponseEntity.status(404).body(new ApiError("RESOURCE_NOT_FOUND", "Resource not found"));
            var prior = Optional.ofNullable(receipts.get(owner + ":" + request.clientRequestId()));
            PolicyIssueResponse mapped = service.issueResponse(request, quoteFacts, prior);
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
