package assignments.reboot;

import assignments.insurance.HomeSecureEligibilityInput;
import assignments.insurance.HomeSecureEligibilityPolicy;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Day 5 exercises only the pure eligibility subject using frozen fixture values. */
@Tag("learnerAcceptance")
class Day05ContractTest {
    private static final BigDecimal MIN = new BigDecimal("100000.00");
    private static final BigDecimal MAX = new BigDecimal("10000000.00");
    private final HomeSecureEligibilityPolicy policy = new HomeSecureEligibilityPolicy("OWNER_OCCUPIED", MIN, MAX);

    @Test
    void acceptsInclusiveMinimumAndMaximum() {
        assertEquals(new assignments.insurance.EligibilityDecision(true, "ELIGIBLE"),
                policy.decide(new HomeSecureEligibilityInput("OWNER_OCCUPIED", MIN)));
        assertEquals(new assignments.insurance.EligibilityDecision(true, "ELIGIBLE"),
                policy.decide(new HomeSecureEligibilityInput("OWNER_OCCUPIED", MAX)));
    }

    @Test
    void rejectsAmountsOutsideInclusiveBounds() {
        assertEquals(new assignments.insurance.EligibilityDecision(false, "REBUILD_AMOUNT_OUT_OF_RANGE"),
                policy.decide(new HomeSecureEligibilityInput("OWNER_OCCUPIED", new BigDecimal("99999.99"))));
        assertEquals(new assignments.insurance.EligibilityDecision(false, "REBUILD_AMOUNT_OUT_OF_RANGE"),
                policy.decide(new HomeSecureEligibilityInput("OWNER_OCCUPIED", new BigDecimal("10000000.01"))));
    }

    @Test
    void appliesRequiredFieldThenOccupancyThenAmountReasonPrecedence() {
        assertEquals(new assignments.insurance.EligibilityDecision(false, "REQUIRED_FIELD_MISSING"),
                policy.decide(new HomeSecureEligibilityInput(null, new BigDecimal("1.00"))));
        assertEquals(new assignments.insurance.EligibilityDecision(false, "REQUIRED_FIELD_MISSING"),
                policy.decide(new HomeSecureEligibilityInput("TENANT", null)));
        assertEquals(new assignments.insurance.EligibilityDecision(false, "OWNER_OCCUPIED_REQUIRED"),
                policy.decide(new HomeSecureEligibilityInput("TENANT", new BigDecimal("1.00"))));
        assertEquals(new assignments.insurance.EligibilityDecision(false, "REBUILD_AMOUNT_OUT_OF_RANGE"),
                policy.decide(new HomeSecureEligibilityInput("OWNER_OCCUPIED", BigDecimal.ZERO)));
    }
}
