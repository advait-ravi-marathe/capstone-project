package assignments.renewal;
import org.junit.jupiter.api.Tag; import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
@Tag("learnerAcceptance") class PreviewAdmissionAcceptanceTest {
 @Test void admitsTwoAndRejectsThirdWithoutQueueing() {
  var admission=new PreviewAdmission(new PreviewCapacityPolicy(2,4,1000));
  var one=admission.tryEnter(); var two=admission.tryEnter();
  assertTrue(one.isPresent()); assertTrue(two.isPresent()); assertTrue(admission.tryEnter().isEmpty());
  one.orElseThrow().close(); two.orElseThrow().close(); assertTrue(admission.tryEnter().isPresent());
 }
}
