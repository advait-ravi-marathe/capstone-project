package assignments.renewal;

import assignments.insurance.PolicyRenewalFacts;
import assignments.insurance.PolicyRenewalReadStore;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class PreviewAdmissionConcurrencyAcceptanceTest {
    @Test
    void boundsConcurrentDelegateEntriesAndRecoversAfterFailure() throws Exception {
        var admission = new PreviewAdmission(new PreviewCapacityPolicy(2, 4, 1000));
        var active = new AtomicInteger();
        var maximum = new AtomicInteger();
        var delegateCalls = new AtomicInteger();
        var release = new CountDownLatch(1);
        var entered = new CountDownLatch(2);
        var failNext = new AtomicBoolean();
        PolicyRenewalReadStore delegate = (policyId, ownerId) -> {
            delegateCalls.incrementAndGet();
            int inFlight = active.incrementAndGet();
            maximum.accumulateAndGet(inFlight, Math::max);
            try {
                if ("fail".equals(policyId) && failNext.compareAndSet(true, false)) {
                    throw new IllegalStateException("synthetic delegate failure");
                }
                entered.countDown();
                if (!release.await(2, TimeUnit.SECONDS)) throw new AssertionError("test did not release delegate barrier");
                return Optional.empty();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new AssertionError(interrupted);
            } finally {
                active.decrementAndGet();
            }
        };
        var registry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        var store = new BoundedPolicyRenewalReadStore(delegate, admission, new PreviewMetrics(registry));
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            Future<Optional<PolicyRenewalFacts>> first = workers.submit(() -> store.findByPolicyIdAndOwnerId("a", "owner-a"));
            Future<Optional<PolicyRenewalFacts>> second = workers.submit(() -> store.findByPolicyIdAndOwnerId("b", "owner-b"));
            assertTrue(entered.await(2, TimeUnit.SECONDS), "both admitted calls must enter the actual wrapped delegate");
            assertEquals(2, maximum.get());
            assertThrows(PreviewBusyException.class, () -> store.findByPolicyIdAndOwnerId("third", "owner-a"));
            assertEquals(2, delegateCalls.get(), "the rejected third call must not enter the delegate");
            release.countDown();
            assertTrue(first.get(2, TimeUnit.SECONDS).isEmpty());
            assertTrue(second.get(2, TimeUnit.SECONDS).isEmpty());
            assertEquals(2, admission.availablePermitsForTest());

            failNext.set(true);
            assertThrows(IllegalStateException.class, () -> store.findByPolicyIdAndOwnerId("fail", "owner-a"));
            assertEquals(2, admission.availablePermitsForTest(), "delegate failure must return the permit");
            assertTrue(store.findByPolicyIdAndOwnerId("recovered", "owner-a").isEmpty());
            assertEquals(2, admission.availablePermitsForTest(), "repeated reads after a failure must not leak permits");
        } catch (ExecutionException failure) {
            throw new AssertionError("delegate worker failed", failure.getCause());
        } finally {
            release.countDown();
            workers.shutdownNow();
            assertTrue(workers.awaitTermination(2, TimeUnit.SECONDS));
        }
    }
}
