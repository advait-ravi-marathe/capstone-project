package coverstory.modules.catalog;

import org.springframework.stereotype.Component;

import java.util.Map;

/** Catalog use-case boundary; CAP-43's learner policy controls classify versus mutation. */
@Component
public final class CatalogImportIdentityService {
    private final CatalogImportIdentityPort identity;

    public CatalogImportIdentityService(CatalogImportIdentityPort identity) {
        this.identity = identity;
    }

    public CatalogImportIdentityPort.Decision classify(
            Map<ImportIdentityKey, String> accepted, ImportIdentityCommand command) {
        return identity.classify(accepted, command);
    }

    public CatalogImportIdentityPort.Decision accept(
            Map<ImportIdentityKey, String> accepted, ImportIdentityCommand command) {
        return identity.accept(accepted, command);
    }
}
