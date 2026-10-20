package assignments.reboot;

import assignments.insurance.PremiumCalculator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Day 6 exercises only PremiumCalculator with hand-calculated fixture constants. */
@Tag("learnerAcceptance")
class Day06ContractTest {
    private final PremiumCalculator calculator = new PremiumCalculator(
            new BigDecimal("0.00125"),
            Map.of("BUILDING", new BigDecimal("0.00"), "BUILDING_CONTENTS", new BigDecimal("125.00")));

    @Test
    void appliesHalfUpScaleTwoRoundingAndCoverageAdjustment() {
        assertEquals(new BigDecimal("0.13"),
                calculator.calculate(new BigDecimal("100.00"), "BUILDING"));
        assertEquals(new BigDecimal("125.13"),
                calculator.calculate(new BigDecimal("100.00"), "BUILDING_CONTENTS"));
    }

    @Test
    void zeroIsValidButNegativeNullAndUnknownCoverageAreRejected() {
        assertEquals(new BigDecimal("0.00"), calculator.calculate(BigDecimal.ZERO, "BUILDING"));
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculate(new BigDecimal("-0.01"), "BUILDING"));
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculate(null, "BUILDING"));
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculate(new BigDecimal("100.00"), "UNKNOWN"));
    }

    @Test
    void sameInputProducesSameExactDecimal() {
        var amount = new BigDecimal("123456.78");
        assertEquals(calculator.calculate(amount, "BUILDING_CONTENTS"),
                calculator.calculate(amount, "BUILDING_CONTENTS"));
    }
}
