package assignments.renewal;
import assignments.insurance.*; import org.junit.jupiter.api.Tag; import org.junit.jupiter.api.Test; import org.springframework.jdbc.CannotGetJdbcConnectionException;
import java.util.*; import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
@Tag("learnerAcceptance") class BoundedPolicyRenewalReadStoreAcceptanceTest {
 @Test void preservesOwnerScopeAndReleasesOnEmptySuccessAndFailures() {
  var admission=new PreviewAdmission(new PreviewCapacityPolicy(2,4,1000)); var calls=new AtomicInteger();
  PolicyRenewalFacts facts=new PolicyRenewalFacts("p-demo",java.time.LocalDate.parse("2026-01-01"),PolicyStatus.ISSUED,1,"product-v1","rate-v1",new java.math.BigDecimal("10.00"),"INR");
  PolicyRenewalReadStore delegate=(p,o)->{assertEquals("p-demo",p);assertEquals("owner-demo",o);calls.incrementAndGet();return Optional.of(facts);};
  var store=new BoundedPolicyRenewalReadStore(delegate,admission); assertEquals(Optional.of(facts),store.findByPolicyIdAndOwnerId("p-demo","owner-demo"));
  assertEquals(1,calls.get()); assertEquals(2,admission.availablePermitsForTest());
 }
 @Test void rejectsBeforeDelegateAndOnlyWrapsConnectionAcquisitionFailure() {
  var admission=new PreviewAdmission(new PreviewCapacityPolicy(2,4,1000)); var first=admission.tryEnter().orElseThrow(); var second=admission.tryEnter().orElseThrow(); var calls=new AtomicInteger();
  try { var store=new BoundedPolicyRenewalReadStore((p,o)->{calls.incrementAndGet();throw new CannotGetJdbcConnectionException("private details");},admission);
   assertThrows(PreviewBusyException.class,()->store.findByPolicyIdAndOwnerId("p","o")); assertEquals(0,calls.get());
  } finally {first.close();second.close();}
  var domain=new IllegalStateException("domain"); var store=new BoundedPolicyRenewalReadStore((p,o)->{throw domain;},admission);
  assertSame(domain,assertThrows(IllegalStateException.class,()->store.findByPolicyIdAndOwnerId("p","o")));
  var database=new BoundedPolicyRenewalReadStore((p,o)->{throw new CannotGetJdbcConnectionException("private details");},admission);
  assertTrue(assertThrows(PreviewUnavailableException.class,()->database.findByPolicyIdAndOwnerId("p","o")).getCause()
          instanceof CannotGetJdbcConnectionException);
  assertEquals(2,admission.availablePermitsForTest());
 }
}
