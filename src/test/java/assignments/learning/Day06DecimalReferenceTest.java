package assignments.learning;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Day06DecimalReferenceTest {
    @Test void roundsOnlyAfterTheExactArithmetic() {
        var quantity = new BigDecimal("2");
        var unitFee = new BigDecimal("1.005");
        var adjustment = new BigDecimal("0.005");
        var exact = quantity.multiply(unitFee).add(adjustment);
        var finalFee = exact.setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("2.015"), exact);
        assertEquals(new BigDecimal("2.02"), finalFee);
        var roundedTooEarly = quantity.multiply(unitFee.setScale(2, RoundingMode.HALF_UP))
                .add(adjustment).setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("2.03"), roundedTooEarly);
    }

    @Test void variationUsesSameDeclaredFinalRoundingBoundary() {
        var exact = new BigDecimal("3").multiply(new BigDecimal("1.005"))
                .add(new BigDecimal("0.005"));
        assertEquals(new BigDecimal("3.020"), exact);
        assertEquals(new BigDecimal("3.02"), exact.setScale(2, RoundingMode.HALF_UP));
    }
}
