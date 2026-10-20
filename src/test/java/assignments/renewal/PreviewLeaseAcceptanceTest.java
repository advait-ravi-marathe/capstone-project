package assignments.renewal;
import org.junit.jupiter.api.Tag; import org.junit.jupiter.api.Test;
import java.util.concurrent.*; import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
@Tag("learnerAcceptance") class PreviewLeaseAcceptanceTest {
 @Test void concurrentAndRepeatedCloseReleaseExactlyOnce() throws Exception {
  var releases=new AtomicInteger(); var lease=new PreviewLease(releases::incrementAndGet);
  var pool=Executors.newFixedThreadPool(8); var ready=new CountDownLatch(8); var start=new CountDownLatch(1);
  try { for(int i=0;i<8;i++) pool.submit(()->{ready.countDown(); try { start.await(); lease.close(); } catch(InterruptedException e){Thread.currentThread().interrupt(); throw new AssertionError(e);} });
   assertTrue(ready.await(2,TimeUnit.SECONDS)); start.countDown(); pool.shutdown(); assertTrue(pool.awaitTermination(2,TimeUnit.SECONDS));
   lease.close(); assertEquals(1,releases.get());
  } finally { start.countDown(); pool.shutdownNow(); }
 }
}
