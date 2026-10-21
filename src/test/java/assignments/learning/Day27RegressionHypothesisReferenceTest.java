package assignments.learning;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/** Instructor toy example: distinguish an inclusive boundary from an input-unit mistake. */
class Day27RegressionHypothesisReferenceTest {
    private static int feeForAtMostOneKilogram(int grams) {
        return grams <= 1_000 ? 5 : 8;
    }

    private static int strictBoundaryMutant(int grams) {
        return grams < 1_000 ? 5 : 8;
    }

    @Test
    void inclusiveBoundaryAndAdjacentValuesAreExplicit() {
        assertEquals(5, feeForAtMostOneKilogram(999));
        assertEquals(5, feeForAtMostOneKilogram(1_000));
        assertEquals(8, feeForAtMostOneKilogram(1_001));
    }

    @Test
    void strictComparisonWouldFailOnlyAtTheSavedBoundary() {
        assertEquals(5, strictBoundaryMutant(999));
        assertNotEquals(5, strictBoundaryMutant(1_000));
        assertEquals(8, strictBoundaryMutant(1_001));
    }
}
