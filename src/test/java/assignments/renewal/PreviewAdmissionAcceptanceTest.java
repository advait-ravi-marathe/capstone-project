package assignments.renewal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("learnerAcceptance")
class PreviewAdmissionAcceptanceTest {
    @Test
    @Timeout(5)
    void admitsTwoAndRejectsThirdImmediately() {
        var admission = new PreviewAdmission(new PreviewCapacityPolicy(2, 4, 1000));
        var first = admission.tryEnter();
        var second = admission.tryEnter();
        assertTrue(first.isPresent());
        assertTrue(second.isPresent());
        assertTrue(admission.tryEnter().isEmpty(), "a full gate rejects immediately");
        // Permit release is the next day's PreviewLease contract and is tested there.
        assertTrue(first.orElseThrow() != second.orElseThrow());
    }
}
