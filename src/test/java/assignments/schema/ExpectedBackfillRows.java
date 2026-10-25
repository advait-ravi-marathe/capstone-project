package assignments.schema;

import java.util.Map;

/** Instructor-owned oracle pinned independently of the candidate transformation. */
final class ExpectedBackfillRows {
    static final Map<Integer, String> VALUES = Map.of(
            1, "policy alpha", 2, "policy bravo", 3, "policy charlie", 4, "policy delta", 5, "policy echo",
            6, "policy foxtrot", 7, "policy golf", 8, "policy hotel", 9, "policy india", 10, "policy juliet");
    private ExpectedBackfillRows() { }
}
