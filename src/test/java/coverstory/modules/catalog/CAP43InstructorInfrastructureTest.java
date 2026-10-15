package coverstory.modules.catalog;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.catalog.CatalogImportIdentityPort.Decision;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Instructor witness is test-only; production CAP-43 methods remain TODO. */
class CAP43InstructorInfrastructureTest {
    @Test void syntheticFixtureEnumeratesSavedAcceptanceOutcomes() throws Exception {
        JsonNode fixture = new ObjectMapper().readTree(Files.readString(
                Path.of("fixtures/coverstory/CAP-43/synthetic-v1.json")));
        assertEquals("CAP-43", fixture.path("contract").asText());
        assertEquals("SYNTHETIC", fixture.path("classification").asText());
        assertEquals(5, fixture.path("cases").size());
        assertEquals("hash-p", fixture.path("identity").path("acceptedHash").asText());
    }

    @Test void instructorWitnessAndMutantsExerciseDuplicateConflictAndScope() {
        Map<ImportIdentityKey, String> accepted = new LinkedHashMap<>();
        var command = new ImportIdentityCommand("editor-E", "health-A", "v1", "hash-p");
        Map<ImportIdentityKey, String> before = Map.copyOf(accepted);
        assertEquals(Decision.ACCEPT, classifyWitness(accepted, command));
        assertEquals(before, accepted, "classify must not mutate");
        assertEquals(Decision.ACCEPT, acceptWitness(accepted, command));

        before = Map.copyOf(accepted);
        assertEquals(Decision.DUPLICATE, classifyWitness(accepted, command));
        assertEquals(Decision.DUPLICATE, acceptWitness(accepted, command));
        assertEquals(before, accepted);

        var changed = new ImportIdentityCommand("editor-E", "health-A", "v1", "hash-q");
        assertEquals(Decision.CONFLICT, classifyWitness(accepted, changed));
        assertEquals(Decision.CONFLICT, acceptWitness(accepted, changed));
        assertEquals("hash-p", accepted.get(new ImportIdentityKey("editor-E", "health-A", "v1")));

        var otherEditor = new ImportIdentityCommand("editor-F", "health-A", "v1", "hash-p");
        assertEquals(Decision.ACCEPT, classifyWitness(accepted, otherEditor));
        assertNotEquals(Decision.ACCEPT, versionOnlyMutant(accepted, otherEditor),
                "version/source-only identity must be rejected by this vector");
        assertEquals(Decision.CONFLICT, classifyWitness(accepted,
                new ImportIdentityCommand("editor-E", "health-A", "v1", " hash-p ")),
                "opaque nonblank hashes must not be trimmed");
    }

    private static Decision classifyWitness(Map<ImportIdentityKey, String> accepted, ImportIdentityCommand command) {
        if (accepted == null || command == null || blank(command.editorFixture()) || blank(command.sourceId())
                || blank(command.sourceVersion()) || blank(command.canonicalHash())) return Decision.INVALID;
        String previous = accepted.get(new ImportIdentityKey(command.editorFixture(), command.sourceId(), command.sourceVersion()));
        return previous == null ? Decision.ACCEPT : previous.equals(command.canonicalHash())
                ? Decision.DUPLICATE : Decision.CONFLICT;
    }

    private static Decision acceptWitness(Map<ImportIdentityKey, String> accepted, ImportIdentityCommand command) {
        Decision decision = classifyWitness(accepted, command);
        if (decision == Decision.ACCEPT) accepted.put(
                new ImportIdentityKey(command.editorFixture(), command.sourceId(), command.sourceVersion()), command.canonicalHash());
        return decision;
    }

    private static Decision versionOnlyMutant(Map<ImportIdentityKey, String> accepted, ImportIdentityCommand command) {
        return accepted.keySet().stream().anyMatch(key -> key.sourceId().equals(command.sourceId())
                && key.sourceVersion().equals(command.sourceVersion())) ? Decision.DUPLICATE : Decision.ACCEPT;
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
}
