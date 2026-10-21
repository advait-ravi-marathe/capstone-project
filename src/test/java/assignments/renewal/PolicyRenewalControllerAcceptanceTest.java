package assignments.renewal;

import assignments.insurance.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Fixed Day 49–51 route acceptance; the fixture store is an explicit test collaborator. */
@Tag("learnerAcceptance")
class PolicyRenewalControllerAcceptanceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2027-11-15T12:00:00Z"), ZoneOffset.UTC);
    private static final PolicyRenewalFacts ISSUED = new PolicyRenewalFacts("policy-demo-1", LocalDate.parse("2026-12-01"),
            PolicyStatus.ISSUED, 3, "home-secure-v1", "home-secure-rate-v1", new BigDecimal("125.00"), "INR");

    @Test void day49OwnerComesOnlyFromPrincipalAndUnauthenticatedDoesNotRead() {
        AtomicInteger reads = new AtomicInteger();
        var controller = controller((policyId, ownerId) -> { reads.incrementAndGet(); return Optional.of(ISSUED); });
        assertEquals(Optional.of("owner-a"), controller.authenticatedOwner(() -> "owner-a"));
        assertTrue(controller.authenticatedOwner(null).isEmpty());
        assertTrue(controller.authenticatedOwner(() -> " ").isEmpty());
        var response = controller.getRenewalPreview("policy-demo-1", null);
        assertEquals(401, response.getStatusCode().value());
        assertEquals(0, reads.get());
    }

    @Test void day49MockMvcPassesPrincipalRatherThanForgedQueryOrHeaderToTheReadStore() throws Exception {
        AtomicInteger reads = new AtomicInteger();
        java.util.concurrent.atomic.AtomicReference<String> observedOwner = new java.util.concurrent.atomic.AtomicReference<>();
        var recording = (PolicyRenewalReadStore) (policyId, ownerId) -> {
            reads.incrementAndGet(); observedOwner.set(ownerId); return Optional.empty();
        };
        var mvc = mvc(recording);
        mvc.perform(get("/api/policies/policy-demo-1/renewal-preview").queryParam("ownerId", "owner-b")
                        .header("X-Owner-Id", "owner-b").principal(principal("owner-a")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("POLICY_NOT_FOUND"));
        assertEquals(1, reads.get());
        assertEquals("owner-a", observedOwner.get());

        mvc.perform(get("/api/policies/policy-demo-1/renewal-preview").header("X-Owner-Id", "owner-a"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        assertEquals(1, reads.get(), "a forged identity header cannot trigger a store lookup");
    }

    @Test void day50EndpointConcealsMissingAndForeignRowsAndUsesStableConflictCodes() throws Exception {
        var response = controller((policyId, ownerId) -> Optional.empty())
                .unavailable(PolicyRenewalEligibility.TOO_EARLY);
        assertEquals(409, response.getStatusCode().value());
        assertEquals("RENEWAL_PREVIEW_UNAVAILABLE", ((Map<?, ?>) response.getBody()).get("code"));
        assertEquals("Renewal preview is outside the eligible window or policy state.",
                ((Map<?, ?>) response.getBody()).get("message"));

        for (PolicyRenewalEligibility reason : new PolicyRenewalEligibility[]{PolicyRenewalEligibility.TOO_EARLY,
                PolicyRenewalEligibility.TERM_ENDED, PolicyRenewalEligibility.NOT_RENEWABLE}) {
            var mapped = controller((policyId, ownerId) -> Optional.of(ISSUED)).unavailable(reason);
            assertEquals(409, mapped.getStatusCode().value(), reason.toString());
            assertEquals(Map.of("code", "RENEWAL_PREVIEW_UNAVAILABLE",
                    "message", "Renewal preview is outside the eligible window or policy state."), mapped.getBody());
        }

        MockMvc missingAndForeign = mvc((policyId, ownerId) -> Optional.empty());
        var missing = missingAndForeign.perform(get("/api/policies/missing/renewal-preview").principal(principal("owner-a")))
                .andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();
        var foreign = missingAndForeign.perform(get("/api/policies/policy-demo-1/renewal-preview").principal(principal("owner-b")))
                .andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();
        assertEquals(missing, foreign, "missing and foreign rows have identical public errors");
        assertEquals("{\"code\":\"POLICY_NOT_FOUND\"}", missing);
    }

    @Test void day51PublicPreviewIsMinimalAndContainsNoPrivateOrMutationFields() {
        var preview = new PolicyRenewalPreview("policy-demo-1", 3, "home-secure-v1", "home-secure-rate-v1",
                new BigDecimal("125.00"), "INR", LocalDate.parse("2027-12-01"),
                "Reference only; not a renewal offer or coverage commitment.");
        var controller = controller((policyId, ownerId) -> Optional.of(ISSUED));
        ResponseEntity<?> response = controller.available(preview);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(preview, response.getBody());
        String serialized = response.getBody().toString();
        assertFalse(serialized.contains("owner-a"));
        assertFalse(serialized.contains("journey-hidden"));
        assertFalse(serialized.contains("quote-hidden"));
        assertFalse(serialized.toLowerCase().contains("accept"));
    }

    @Test void day51SerializedPreviewHasExactlyApprovedFieldsAndNumericPremium() throws Exception {
        var preview = new PolicyRenewalPreview("policy-demo-9", 9, "home-secure-v9", "home-secure-rate-v4",
                new BigDecimal("987.40"), "INR", LocalDate.parse("2028-04-17"),
                "Reference only; not a renewal offer or coverage commitment.");
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new PreviewSerializationController(
                controller((policyId, ownerId) -> Optional.of(ISSUED)), preview)).build();
        var content = mvc.perform(get("/teaching/renewal-preview"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var json = new tools.jackson.databind.ObjectMapper().readTree(content);
        assertEquals(java.util.Set.of("policyId", "sourcePolicyVersion", "productVersion", "rateVersion",
                        "currentAnnualPremiumRupees", "currency", "nextTermEffectiveDate", "notice"),
                iterableFields(json));
        assertEquals("policy-demo-9", json.get("policyId").asString());
        assertEquals(9, json.get("sourcePolicyVersion").asInt());
        assertEquals("home-secure-v9", json.get("productVersion").asString());
        assertEquals("home-secure-rate-v4", json.get("rateVersion").asString());
        assertTrue(json.get("currentAnnualPremiumRupees").isNumber());
        assertEquals(0, new BigDecimal("987.40").compareTo(json.get("currentAnnualPremiumRupees").decimalValue()),
                "wire JSON is numeric; presentation adds two display decimals separately");
        assertEquals("INR", json.get("currency").asString());
        assertEquals("2028-04-17", json.get("nextTermEffectiveDate").asString());
        assertEquals("Reference only; not a renewal offer or coverage commitment.", json.get("notice").asString());
        assertFalse(content.contains("journey-hidden"));
        assertFalse(content.contains("quote-hidden"));
        assertFalse(content.contains("owner-a"));
    }

    private static java.util.Set<String> iterableFields(tools.jackson.databind.JsonNode json) {
        java.util.Set<String> fields = new java.util.HashSet<>();
        fields.addAll(json.propertyNames());
        return fields;
    }

    @RestController
    static class PreviewSerializationController {
        private final PolicyRenewalPreviewController learner;
        private final PolicyRenewalPreview preview;
        PreviewSerializationController(PolicyRenewalPreviewController learner, PolicyRenewalPreview preview) {
            this.learner = learner; this.preview = preview;
        }
        @GetMapping("/teaching/renewal-preview") ResponseEntity<?> get() { return learner.available(preview); }
    }

    private static MockMvc mvc(PolicyRenewalReadStore store) {
        return MockMvcBuilders.standaloneSetup(controller(store)).build();
    }

    private static PolicyRenewalPreviewController controller(PolicyRenewalReadStore store) {
        return controller(store, new PolicyRenewalPreviewPolicy());
    }

    private static PolicyRenewalPreviewController controller(PolicyRenewalReadStore store, PolicyRenewalPreviewPolicy policy) {
        return new PolicyRenewalPreviewController(store, policy, CLOCK);
    }

    private static Principal principal(String name) { return () -> name; }
}
