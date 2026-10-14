package coverstory.modules.catalog;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/** Instructor-only witness check, separate from the learner acceptance class. */
class CAP165HarnessDiscriminationTest {
    private static final List<List<String>> VECTORS = List.of(
            List.of("a:b", "c"), List.of("a", "b:c"),
            List.of("health-source-A", "v1"), List.of("health-source-A", "v1"),
            List.of("health-source-A", "v2"));

    @Test void acceptsAnExactPairWitnessAndRejectsDelimiterConcatenation() {
        assertTrue(discriminates(VECTORS, List::copyOf), "separate exact components remain distinguishable");
        Function<List<String>, Object> broken = pair -> pair.get(0) + ":" + pair.get(1);
        assertFalse(discriminates(VECTORS, broken), "the fixture must reject a delimiter-collision witness");
    }

    private static boolean discriminates(List<List<String>> vectors, Function<List<String>, Object> witness) {
        var outcomes = new HashSet<>();
        vectors.forEach(vector -> outcomes.add(witness.apply(vector)));
        // Five inputs include one exact duplicate, so a sound witness has four distinct identities.
        return outcomes.size() == 4;
    }
}
