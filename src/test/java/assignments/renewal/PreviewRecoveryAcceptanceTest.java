package assignments.renewal;
import assignments.insurance.PolicyRenewalReadStore; import org.junit.jupiter.api.Tag; import org.junit.jupiter.api.Test; import java.util.Optional; import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
@Tag("learnerAcceptance") class PreviewRecoveryAcceptanceTest {
 @Test void busyThenReleasedCapacityAllowsNextRead() {
  var gate=new PreviewAdmission(new PreviewCapacityPolicy(2,4,1000));var a=gate.tryEnter().orElseThrow();var b=gate.tryEnter().orElseThrow();var count=new AtomicInteger();
  var store=new BoundedPolicyRenewalReadStore((p,o)->{count.incrementAndGet();return Optional.empty();},gate);
  try{assertThrows(PreviewBusyException.class,()->store.findByPolicyIdAndOwnerId("p","o"));assertEquals(0,count.get());}finally{a.close();b.close();}
  assertTrue(store.findByPolicyIdAndOwnerId("p","o").isEmpty());assertEquals(1,count.get());assertEquals(2,gate.availablePermitsForTest());
 }
}
