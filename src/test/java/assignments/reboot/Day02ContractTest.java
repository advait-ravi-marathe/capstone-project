package assignments.reboot;

import assignments.insurance.QuoteSelectionInput;
import assignments.insurance.QuoteSelectionRequest;
import assignments.insurance.QuoteSelectionRequestValidator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Day 2 request validation contract; the validator remains learner-owned. */
@Tag("learnerAcceptance")
class Day02ContractTest {
    private final QuoteSelectionRequestValidator validator = new QuoteSelectionRequestValidator();

    @Test
    void acceptsEmptySelectionListAndValidExactIdentifiers() {
        assertDoesNotThrow(() -> validator.validate(new QuoteSelectionRequest(List.of())));
        assertDoesNotThrow(() -> validator.validate(new QuoteSelectionRequest(List.of(
                new QuoteSelectionInput("journey-A", "home-secure-v1")))));
    }

    @Test
    void rejectsMissingRequestAndSelectionList() {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(null));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(new QuoteSelectionRequest(null)));
    }

    @Test
    void rejectsNullEntryAndMissingEmptyOrBlankKeyParts() {
        var nullEntry = new ArrayList<QuoteSelectionInput>();
        nullEntry.add(null);
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(new QuoteSelectionRequest(nullEntry)));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(new QuoteSelectionRequest(List.of(
                new QuoteSelectionInput(null, "home-secure-v1")))));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(new QuoteSelectionRequest(List.of(
                new QuoteSelectionInput("journey-A", null)))));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(new QuoteSelectionRequest(List.of(
                new QuoteSelectionInput("", "home-secure-v1")))));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(new QuoteSelectionRequest(List.of(
                new QuoteSelectionInput("journey-A", " ")))));
    }
}
