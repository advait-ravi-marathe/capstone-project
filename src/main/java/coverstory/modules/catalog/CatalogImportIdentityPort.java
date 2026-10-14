package coverstory.modules.catalog;

import java.util.Map;

/** Catalog-owned sequential identity boundary. It does not claim durable or concurrent safety. */
public interface CatalogImportIdentityPort {
    enum Decision { ACCEPT, DUPLICATE, CONFLICT, INVALID }
    Decision classify(Map<ImportIdentityKey, String> accepted, ImportIdentityCommand command);
    Decision accept(Map<ImportIdentityKey, String> accepted, ImportIdentityCommand command);
}
