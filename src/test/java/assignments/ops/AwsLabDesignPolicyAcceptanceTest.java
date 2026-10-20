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
        var safe = design(false, false, false, false, false, false, List.of(), null, null, false);
        assertDoesNotThrow(() -> policy.validate(safe));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(design(true, false, false, false, false, false, List.of(), null, null, false)));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(design(false, true, false, false, false, false, List.of(), null, null, false)));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(design(false, false, true, false, false, false, List.of(), null, null, false)));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(design(false, false, false, true, false, false, List.of(), null, null, false)));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(design(false, false, false, false, true, false, List.of(), null, null, false)));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(design(false, false, false, false, false, false, List.of(), null, null, true)));
    }

    @Test void day138CostsRetainedResourcesWithoutInventingPrices() {
        var policy = new AwsLabDesignPolicy();
        var lines = List.of(
                new AwsLabDesignPolicy.CostLine("compute", new BigDecimal("0.10"), "instance-hour", new BigDecimal("2"), new BigDecimal("0.20"), "USD", "FICTIONAL_TEST_RATE"),
                new AwsLabDesignPolicy.CostLine("database-storage", new BigDecimal("0.02"), "GB-month", new BigDecimal("5"), new BigDecimal("0.10"), "USD", "FICTIONAL_TEST_RATE"),
                new AwsLabDesignPolicy.CostLine("backups-snapshots", new BigDecimal("0.01"), "GB-month", new BigDecimal("3"), new BigDecimal("0.03"), "USD", "FICTIONAL_TEST_RATE"),
                new AwsLabDesignPolicy.CostLine("logs", new BigDecimal("0.01"), "GB-month", new BigDecimal("2"), new BigDecimal("0.02"), "USD", "FICTIONAL_TEST_RATE"),
                new AwsLabDesignPolicy.CostLine("secrets", new BigDecimal("0.05"), "secret-month", new BigDecimal("1"), new BigDecimal("0.05"), "USD", "FICTIONAL_TEST_RATE"),
                new AwsLabDesignPolicy.CostLine("transfer", new BigDecimal("0.01"), "GB", new BigDecimal("4"), new BigDecimal("0.04"), "USD", "FICTIONAL_TEST_RATE"));
        var safe = design(false, false, false, false, false, false, lines, new BigDecimal("0.44"), "USD", false);
        assertDoesNotThrow(() -> policy.validate(safe));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(design(false, false, false, false, false, false, lines.subList(0, 1), new BigDecimal("0.20"), "USD", false)));
        var wrongUnit = new java.util.ArrayList<>(lines); wrongUnit.set(1, new AwsLabDesignPolicy.CostLine("database-storage", new BigDecimal("0.02"), "instance-hour", new BigDecimal("5"), new BigDecimal("0.10"), "USD", "FICTIONAL_TEST_RATE"));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(design(false, false, false, false, false, false, wrongUnit, new BigDecimal("0.44"), "USD", false)));
        var wrongCurrency = new java.util.ArrayList<>(lines); wrongCurrency.set(1, new AwsLabDesignPolicy.CostLine("database-storage", new BigDecimal("0.02"), "GB-month", new BigDecimal("5"), new BigDecimal("0.10"), "EUR", "FICTIONAL_TEST_RATE"));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(design(false, false, false, false, false, false, wrongCurrency, new BigDecimal("0.44"), "USD", false)));
        var badAmount = new java.util.ArrayList<>(lines); badAmount.set(0, new AwsLabDesignPolicy.CostLine("compute", new BigDecimal("0.10"), "instance-hour", new BigDecimal("2"), new BigDecimal("0.19"), "USD", "FICTIONAL_TEST_RATE"));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(design(false, false, false, false, false, false, badAmount, new BigDecimal("0.43"), "USD", false)));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(design(false, false, false, false, false, true,
                lines, new BigDecimal("0.44"), "USD", false)), "selected networking must be included explicitly");
    }

    private static AwsLabDesignPolicy.Design design(boolean publicApp, boolean publicDb, boolean wildcard,
            boolean secrets, boolean cloudRequest, boolean networkingSelected, List<AwsLabDesignPolicy.CostLine> costs,
            BigDecimal total, String currency, boolean cloudVerified) {
        return new AwsLabDesignPolicy.Design("pending", "pending", "operator-access-pending", publicApp, publicDb,
                wildcard, secrets, cloudRequest, networkingSelected, costs, total, currency, cloudVerified);
    }
}
