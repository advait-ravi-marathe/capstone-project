package assignments.renewal;
import org.junit.jupiter.api.Tag; import org.junit.jupiter.api.Test;
import java.util.concurrent.*; import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
@Tag("learnerAcceptance") class PreviewAdmissionConcurrencyAcceptanceTest {
 @Test void neverHasMoreThanTwoConcurrentReadersAndRecoversAfterFailure() throws Exception {
  var admission=new PreviewAdmission(new PreviewCapacityPolicy(2,4,1000)); var entered=new AtomicInteger(); var max=new AtomicInteger(); var latch=new CountDownLatch(2); var release=new CountDownLatch(1); var pool=Executors.newFixedThreadPool(3);
  try {
   Runnable reader=()->{var lease=admission.tryEnter(); if(lease.isEmpty())return; try(var ignored=lease.get()){int active=entered.incrementAndGet();max.accumulateAndGet(active,Math::max);latch.countDown();try{release.await();}catch(InterruptedException e){Thread.currentThread().interrupt();}finally{entered.decrementAndGet();}}};
   var first=pool.submit(reader);var second=pool.submit(reader);assertReaderEnteredOrPropagateFailure(first);assertReaderEnteredOrPropagateFailure(second);
   assertTrue(latch.await(2,TimeUnit.SECONDS));assertTrue(admission.tryEnter().isEmpty());release.countDown();pool.shutdown();assertTrue(pool.awaitTermination(2,TimeUnit.SECONDS));
   assertEquals(2,max.get());assertEquals(2,admission.availablePermitsForTest());
  } finally {release.countDown();pool.shutdownNow();}
 }
 private static void assertReaderEnteredOrPropagateFailure(Future<?> future) throws Exception {
  try { future.get(100,TimeUnit.MILLISECONDS); }
  catch (TimeoutException expected) { return; }
  catch (ExecutionException failure) { throw new AssertionError("reader failed before the admission barrier",failure.getCause()); }
 }
}
