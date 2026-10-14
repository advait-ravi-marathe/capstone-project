package coverstory.modules.catalog.api;

import coverstory.modules.catalog.CatalogImportIdentityPort;
import coverstory.modules.catalog.ImportIdentityCommand;
import coverstory.modules.catalog.ImportIdentityKey;
import org.springframework.stereotype.Component;

import java.util.Map;

/** CAP-43 learner policy seam; sequential map semantics only. */
@Component
public final class CAP43Seam implements CatalogImportIdentityPort {
    @Override
    public Decision classify(Map<ImportIdentityKey, String> accepted, ImportIdentityCommand command) {
        // LEARNER TODO: classify exact key/hash without mutating the map.
        throw new UnsupportedOperationException("CAP-43 learner TODO: implement classify");
    }

    @Override
    public Decision accept(Map<ImportIdentityKey, String> accepted, ImportIdentityCommand command) {
        // LEARNER TODO: insert only when classification is ACCEPT.
        throw new UnsupportedOperationException("CAP-43 learner TODO: implement accept");
    }
}
