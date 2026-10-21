package assignments.learning;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Day04ImmutableValuesReferenceTest {
    record Menu(List<String> items) {
        Menu { items = List.copyOf(items); }
    }

    @Test void copiedMembershipIsStableAndExposedListIsUnmodifiable() {
        var original = new ArrayList<>(List.of("tea", "water"));
        var menu = new Menu(original);
        original.add("juice");
        assertEquals(List.of("tea", "water", "juice"), original);
        assertEquals(List.of("tea", "water"), menu.items());
        assertThrows(UnsupportedOperationException.class, () -> menu.items().add("coffee"));
    }

    @Test void sourceRemovalDoesNotChangeCopiedOrder() {
        var original = new ArrayList<>(List.of("water", "tea"));
        var menu = new Menu(original);
        original.remove(0);
        assertEquals(List.of("water", "tea"), menu.items());
        assertEquals(List.of("tea"), original);
    }
}
