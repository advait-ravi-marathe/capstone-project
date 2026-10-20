package assignments.ops;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class AwsLabDesignPolicyAcceptanceTest {
    @Test void day137KeepsCloudExecutionOutsideOfflineDesign() {
        var policy = new AwsLabDesignPolicy();
        var safe = new AwsLabDesignPolicy.Design("account-pending", "region-pending", "operator-access-pending",
                false, false, false, List.of(), null, null, false);
        assertDoesNotThrow(() -> policy.validate(safe));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(new AwsLabDesignPolicy.Design(
                safe.accountId(), safe.region(), safe.accessModel(), true, false, false, List.of(), null, null, false)));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(new AwsLabDesignPolicy.Design(
                safe.accountId(), safe.region(), safe.accessModel(), false, true, false, List.of(), null, null, false)));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(new AwsLabDesignPolicy.Design(
                safe.accountId(), safe.region(), safe.accessModel(), false, false, true, List.of(), null, null, false)));
    }

    @Test void day138IncludesFictionalRetainedCostsAndNeverInventsZero() {
        var policy = new AwsLabDesignPolicy();
        var line = new AwsLabDesignPolicy.CostLine("fictional-compute", new BigDecimal("0.10"), new BigDecimal("2"), new BigDecimal("0.20"), "FICTIONAL_TEST_RATE");
        var safe = new AwsLabDesignPolicy.Design("pending", "pending", "pending", false, false, false,
                List.of(line), new BigDecimal("0.20"), "USD", false);
        assertDoesNotThrow(() -> policy.validate(safe));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(new AwsLabDesignPolicy.Design(
                "pending", "pending", "pending", false, false, false, List.of(), BigDecimal.ZERO, "USD", false)));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(new AwsLabDesignPolicy.Design(
                "pending", "pending", "pending", false, false, false,
                List.of(new AwsLabDesignPolicy.CostLine("database-backup", BigDecimal.ZERO, new BigDecimal("2"), BigDecimal.ZERO, "")), BigDecimal.ZERO, "USD", false)));
    }
}
