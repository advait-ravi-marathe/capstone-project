package assignments.renewal;
import org.junit.jupiter.api.Tag; import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
@Tag("learnerAcceptance") class PreviewCapacityPolicyAcceptanceTest {
 @Test void acceptsTheFrozenLimitsAndRejectsUnsafeValues() {
  assertDoesNotThrow(() -> new PreviewCapacityPolicy(2,4,1000).validate());
  assertThrows(IllegalArgumentException.class,() -> new PreviewCapacityPolicy(0,4,1000).validate());
  assertThrows(IllegalArgumentException.class,() -> new PreviewCapacityPolicy(3,2,1000).validate());
  assertThrows(IllegalArgumentException.class,() -> new PreviewCapacityPolicy(2,4,249).validate());
  assertThrows(IllegalArgumentException.class,() -> new PreviewCapacityPolicy(2,4,30001).validate());
 }
}
