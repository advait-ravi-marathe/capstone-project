package assignments.insurance;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Fixed behavioral contract. Expected to fail until the learner TODO is implemented. */
@Tag("learnerAcceptance")
class QuoteSelectionAcceptanceTest {
    private final QuoteSelectionPolicy policy = new QuoteSelectionPolicy();

    @Test
    void removesRepeatedCompositeKeyKeepingFirstSeenOrderWithoutMutation() {
        var input = new ArrayList<>(List.of(
                new QuoteSelectionInput("journey-B", "home-secure-v1"),
                new QuoteSelectionInput("journey-A", "home-secure-v1"),
                new QuoteSelectionInput("journey-B", "home-secure-v1")));
        var before = List.copyOf(input);

        assertEquals(List.of(
                new QuoteSelectionKey("journey-B", "home-secure-v1"),
                new QuoteSelectionKey("journey-A", "home-secure-v1")), policy.uniqueSelections(input));
        assertEquals(before, input);
    }

    @Test
    void emptyInputReturnsEmptyList() {
        assertEquals(List.of(), policy.uniqueSelections(List.of()));
    }

    @Test
    void journeyAndQuoteVersionBothFormIdentity() {
        assertEquals(List.of(
                new QuoteSelectionKey("journey-A", "home-secure-v1"),
                new QuoteSelectionKey("journey-B", "home-secure-v1")),
                policy.uniqueSelections(List.of(
                        new QuoteSelectionInput("journey-A", "home-secure-v1"),
                        new QuoteSelectionInput("journey-B", "home-secure-v1"))));
    }

    @Test
    void rejectsNullAndBlankKeysWithoutPartialOutput() {
        assertThrows(IllegalArgumentException.class, () -> policy.uniqueSelections(null));
        assertThrows(IllegalArgumentException.class, () -> policy.uniqueSelections(List.of(
                new QuoteSelectionInput("journey-A", "home-secure-v1"),
                new QuoteSelectionInput("journey-B", " "))));
        assertThrows(IllegalArgumentException.class, () -> policy.uniqueSelections(List.of(
                new QuoteSelectionInput(" ", "home-secure-v1"))));
        var withNull = new ArrayList<QuoteSelectionInput>();
        withNull.add(new QuoteSelectionInput("journey-A", "home-secure-v1"));
        withNull.add(null);
        assertThrows(IllegalArgumentException.class, () -> policy.uniqueSelections(withNull));
    }

    @Test
    void doesNotTrimOrNormalizeKeyParts() {
        assertEquals(List.of(
                new QuoteSelectionKey("journey-A", "home-secure-v1"),
                new QuoteSelectionKey("journey-A", " home-secure-v1")),
                policy.uniqueSelections(List.of(
                        new QuoteSelectionInput("journey-A", "home-secure-v1"),
                        new QuoteSelectionInput("journey-A", " home-secure-v1"))));
    }
}
