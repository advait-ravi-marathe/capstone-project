package coverstory.modules.payments;

import coverstory.modules.payments.api.CAP80ReplayController;
import coverstory.modules.payments.api.CAP80Seam;
import coverstory.modules.payments.api.MockConfirmationFeedPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Exercises the real HTTP command, owner resolution and actual learner seam; it does not implement replay behavior. */
class CAP80ReplayControllerIntegrationTest {
    private static final UUID OWNER_A = UUID.fromString("a1000000-0000-4000-8000-000000000001");
    private static final UUID OWNER_B = UUID.fromString("b1000000-0000-4000-8000-000000000002");
    private final AtomicReference<UUID> lookedUpOwner = new AtomicReference<>();
    private final MockMvc mvc;

    CAP80ReplayControllerIntegrationTest() {
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        beans.registerSingleton("cap80ReplayCommand", new coverstory.modules.payments.api.CAP80ReplayCommand(new SyntheticFeed(lookedUpOwner), new CAP80Seam()));
        mvc = MockMvcBuilders.standaloneSetup(new CAP80ReplayController(beans.getBeanProvider(coverstory.modules.payments.api.CAP80ReplayCommand.class))).build();
    }

    @Test void serverLoadsOwnedFeedAndCheckpointThenStopsAtLearnerTodo() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/me/payments/mock-confirmations/evt-A/replay").with(principal(OWNER_A))).andReturn();
        assertEquals(501, result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentAsString().contains("REPLAY_NOT_IMPLEMENTED"));
        org.junit.jupiter.api.Assertions.assertEquals(OWNER_A, lookedUpOwner.get());
    }

    @Test void foreignOrUnknownFeedIsSafe404BeforeLearnerSeam() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/me/payments/mock-confirmations/evt-B/replay").with(principal(OWNER_A))).andReturn();
        assertEquals(404, result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentAsString().contains("NOT_FOUND"));
        org.junit.jupiter.api.Assertions.assertEquals(OWNER_A, lookedUpOwner.get());
    }

    @Test void anonymousRequestDoesNotReadFeed() throws Exception {
        lookedUpOwner.set(null);
        assertEquals(401, mvc.perform(post("/api/v1/me/payments/mock-confirmations/evt-A/replay")).andReturn().getResponse().getStatus());
        org.junit.jupiter.api.Assertions.assertNull(lookedUpOwner.get());
    }

    private static RequestPostProcessor principal(UUID owner) {
        return request -> { request.setUserPrincipal(() -> owner.toString()); return request; };
    }

    static class SyntheticFeed implements MockConfirmationFeedPort {
        private final AtomicReference<UUID> lookedUpOwner;
        SyntheticFeed(AtomicReference<UUID> lookedUpOwner) { this.lookedUpOwner = lookedUpOwner; }
        @Override public Optional<CAP80Seam.OwnerPaymentItem> findOwned(UUID ownerId, String eventId) {
            lookedUpOwner.set(ownerId);
            if (!OWNER_A.equals(ownerId) || !"evt-A".equals(eventId)) return Optional.empty();
            return Optional.of(new CAP80Seam.OwnerPaymentItem(OWNER_A, OWNER_A, "evt-A", 3,
                    UUID.fromString("a2000000-0000-4000-8000-000000000001"), "quote-health-A-v1", 100000, "INR", true));
        }
        @Override public Optional<CAP80Seam.PaymentCheckpoint> currentCheckpoint(UUID ownerId) {
            return OWNER_A.equals(ownerId) ? Optional.of(new CAP80Seam.PaymentCheckpoint(ownerId, 2)) : Optional.empty();
        }
        @Override public CAP80Seam.PaymentEffectPort effectPort() { return (owner, work) -> { throw new AssertionError("TODO seam must fail before effects"); }; }
    }
}
