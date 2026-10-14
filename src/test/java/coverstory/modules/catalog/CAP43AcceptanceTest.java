package coverstory.modules.catalog;

import coverstory.modules.catalog.CatalogImportIdentityPort.Decision;
import coverstory.modules.catalog.api.CAP43Seam;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Learner contract for the intentionally sequential CAP-43 map policy. */
@Tag("learnerAcceptance")
class CAP43AcceptanceTest {
    private final CAP43Seam seam = new CAP43Seam();
    private final CatalogImportIdentityService useCase = new CatalogImportIdentityService(seam);
    private final ImportIdentityKey acceptedKey = new ImportIdentityKey("editor-E", "source-health-A", "v1");

    @Test void emptyMapClassifiesWithoutMutationThenAcceptsOneExactEntry() {
        Map<ImportIdentityKey, String> accepted = new LinkedHashMap<>();
        ImportIdentityCommand command = command("editor-E", "source-health-A", "v1", "hash-p");
        assertEquals(Decision.ACCEPT, useCase.classify(accepted, command));
        assertTrue(accepted.isEmpty(), "classify must be read-only");
        assertEquals(Decision.ACCEPT, useCase.accept(accepted, command));
        assertEquals(Map.of(acceptedKey, "hash-p"), accepted);
    }

    @Test void identicalKeyAndHashIsDuplicateAndLeavesMapUnchanged() {
        Map<ImportIdentityKey, String> accepted = seeded();
        Map<ImportIdentityKey, String> before = Map.copyOf(accepted);
        var command = command("editor-E", "source-health-A", "v1", "hash-p");
        assertEquals(Decision.DUPLICATE, useCase.classify(accepted, command));
        assertEquals(before, accepted);
        assertEquals(Decision.DUPLICATE, useCase.accept(accepted, command));
        assertEquals(before, accepted);
    }

    @Test void changedHashConflictsAndCannotReplaceTheAcceptedValue() {
        Map<ImportIdentityKey, String> accepted = seeded();
        assertEquals(Decision.CONFLICT, useCase.classify(accepted,
                command("editor-E", "source-health-A", "v1", "hash-q")));
        assertEquals(Decision.CONFLICT, useCase.accept(accepted,
                command("editor-E", "source-health-A", "v1", "hash-q")));
        assertEquals("hash-p", accepted.get(acceptedKey));
    }

    @Test void editorFixtureIsPartOfTheExactSequentialKey() {
        Map<ImportIdentityKey, String> accepted = seeded();
        var command = command("editor-F", "source-health-A", "v1", "hash-p");
        assertEquals(Decision.ACCEPT, useCase.classify(accepted, command));
        assertEquals(Decision.ACCEPT, useCase.accept(accepted, command));
        assertEquals(2, accepted.size());
        assertEquals("hash-p", accepted.get(new ImportIdentityKey("editor-F", "source-health-A", "v1")));
    }

    @Test void invalidFieldsAreRejectedAndNonblankHashComparisonIsExact() {
        Map<ImportIdentityKey, String> accepted = seeded();
        var validCommand = command("editor-E", "source-health-A", "v1", "hash-p");
        assertEquals(Decision.INVALID, useCase.classify(null, validCommand));
        assertEquals(Decision.INVALID, useCase.accept(null, validCommand));
        for (ImportIdentityCommand invalid : new ImportIdentityCommand[] {
                null, command(null, "source-health-A", "v1", "hash-p"),
                command(" ", "source-health-A", "v1", "hash-p"),
                command("editor-E", "", "v1", "hash-p"),
                command("editor-E", "source-health-A", " ", "hash-p"),
                command("editor-E", "source-health-A", "v1", " ")
        }) {
            assertEquals(Decision.INVALID, useCase.classify(accepted, invalid));
            assertEquals(Decision.INVALID, useCase.accept(accepted, invalid));
        }
        assertEquals(Decision.CONFLICT, useCase.classify(accepted,
                command("editor-E", "source-health-A", "v1", " hash-p ")),
                "nonblank hashes are opaque; do not trim before exact comparison");
        assertEquals("hash-p", accepted.get(acceptedKey));
    }

    private Map<ImportIdentityKey, String> seeded() {
        Map<ImportIdentityKey, String> accepted = new LinkedHashMap<>();
        accepted.put(acceptedKey, "hash-p");
        return accepted;
    }

    private static ImportIdentityCommand command(String editor, String source, String version, String hash) {
        return new ImportIdentityCommand(editor, source, version, hash);
    }
}
