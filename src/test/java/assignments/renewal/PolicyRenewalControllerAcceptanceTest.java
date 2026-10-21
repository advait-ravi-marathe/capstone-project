package assignments.renewal;

import assignments.insurance.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

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

    @Test void day50EndpointConcealsMissingAndForeignRowsAndUsesStableConflictCodes() throws Exception {
        var response = controller((policyId, ownerId) -> Optional.empty())
                .unavailable(PolicyRenewalEligibility.TOO_EARLY);
        assertEquals(409, response.getStatusCode().value());
        assertEquals("RENEWAL_PREVIEW_UNAVAILABLE", ((Map<?, ?>) response.getBody()).get("code"));
        assertEquals("Renewal preview is outside the eligible window or policy state.",
                ((Map<?, ?>) response.getBody()).get("message"));
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
