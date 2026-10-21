import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;

/** Independent Day 58 example: explicit constructor and factory dependencies. */
public class DeskCompositionReference {
    interface Shelf { int count(); }
    static final class ShelfSummary {
        private final Clock clock;
        private final Shelf shelf;
        ShelfSummary(Clock clock, Shelf shelf) {
            this.clock = Objects.requireNonNull(clock);
            this.shelf = Objects.requireNonNull(shelf);
        }
        String label() { return LocalDate.now(clock) + ": " + shelf.count() + " books"; }
    }
    record Assembly(Clock clock, Shelf shelf, ShelfSummary summary) { }
    static Assembly compose(Clock clock, Shelf shelf) {
        return new Assembly(clock, shelf, new ShelfSummary(clock, shelf));
    }
    public static void main(String[] args) {
        Clock firstClock = Clock.fixed(Instant.parse("2026-10-21T12:00:00Z"), ZoneOffset.UTC);
        Shelf firstShelf = () -> 7;
        Assembly first = compose(firstClock, firstShelf);
        if (first.clock() != firstClock || first.shelf() != firstShelf) throw new AssertionError("Ignored supplied dependency");
        if (!first.summary().label().equals("2026-10-21: 7 books")) throw new AssertionError("Wrong first trace");
        Assembly second = compose(Clock.fixed(Instant.parse("2027-01-03T12:00:00Z"), ZoneOffset.UTC), () -> 2);
        if (!second.summary().label().equals("2027-01-03: 2 books")) throw new AssertionError("Hardcoded result");
        if (!first.summary().label().equals("2026-10-21: 7 books")) throw new AssertionError("First assembly changed");
        System.out.println("PASS: supplied dependencies, changed assembly, first assembly unchanged");
    }
}
