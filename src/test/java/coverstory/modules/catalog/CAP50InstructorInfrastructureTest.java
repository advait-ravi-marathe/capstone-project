package coverstory.modules.catalog;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Test-only boundary oracle for sequential same-hash import replay. */
class CAP50InstructorInfrastructureTest {
    private record Identity(String editor, String source) { }

    /** Deliberately local fake; this is not the learner's policy or a durable repository. */
    private static final class FakeCatalogImportService {
        private final Map<Identity, String> accepted = new HashMap<>();
        private int inserts;

        CatalogImportIdentityPort.Decision accept(String editor, String source, String hash) {
            Identity key = new Identity(editor, source);
            String previous = accepted.get(key);
            if (previous == null) {
                accepted.put(key, hash);
                inserts++;
                return CatalogImportIdentityPort.Decision.ACCEPT;
            }
            return previous.equals(hash)
                    ? CatalogImportIdentityPort.Decision.DUPLICATE
                    : CatalogImportIdentityPort.Decision.CONFLICT;
        }
    }

    @Test void sameHashReplayDoesNotInsertTwiceAndChangedHashConflicts() {
        var service = new FakeCatalogImportService();
        assertEquals(CatalogImportIdentityPort.Decision.ACCEPT,
                service.accept("E", "source-1", "sha256:abc"));
        assertEquals(CatalogImportIdentityPort.Decision.DUPLICATE,
                service.accept("E", "source-1", "sha256:abc"));
        assertEquals(CatalogImportIdentityPort.Decision.CONFLICT,
                service.accept("E", "source-1", "sha256:def"));
        assertEquals(1, service.inserts);
    }

    @Test void identityRemainsScopedToEditorAndSource() {
        var service = new FakeCatalogImportService();
        assertEquals(CatalogImportIdentityPort.Decision.ACCEPT,
                service.accept("E", "source-1", "sha256:abc"));
        assertEquals(CatalogImportIdentityPort.Decision.ACCEPT,
                service.accept("F", "source-1", "sha256:abc"));
        assertEquals(CatalogImportIdentityPort.Decision.ACCEPT,
                service.accept("E", "source-2", "sha256:abc"));
        assertEquals(3, service.inserts);
    }
}
