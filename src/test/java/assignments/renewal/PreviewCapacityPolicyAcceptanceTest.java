package assignments.renewal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("learnerAcceptance")
class PreviewCapacityPolicyAcceptanceTest {
    @Test
    void acceptsTheFrozenLimitsAndRejectsUnsafeValues() {
        assertDoesNotThrow(() -> new PreviewCapacityPolicy(2, 4, 1000).validate());
        assertDoesNotThrow(() -> new PreviewCapacityPolicy(1, 1, 250).validate());
        assertDoesNotThrow(() -> new PreviewCapacityPolicy(4, 4, 30_000).validate());

        assertThrows(IllegalArgumentException.class, () -> new PreviewCapacityPolicy(0, 4, 1000).validate());
        assertThrows(IllegalArgumentException.class, () -> new PreviewCapacityPolicy(-1, 4, 1000).validate());
        assertThrows(IllegalArgumentException.class, () -> new PreviewCapacityPolicy(2, 0, 1000).validate());
        assertThrows(IllegalArgumentException.class, () -> new PreviewCapacityPolicy(2, -1, 1000).validate());
        assertThrows(IllegalArgumentException.class, () -> new PreviewCapacityPolicy(3, 2, 1000).validate());
        assertThrows(IllegalArgumentException.class, () -> new PreviewCapacityPolicy(2, 4, 0).validate());
        assertThrows(IllegalArgumentException.class, () -> new PreviewCapacityPolicy(2, 4, 249).validate());
        assertThrows(IllegalArgumentException.class, () -> new PreviewCapacityPolicy(2, 4, 30_001).validate());
    }
}
