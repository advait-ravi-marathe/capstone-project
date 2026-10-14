package coverstory.modules.support;

import java.util.UUID;

/** Support access is owner-scoped and uses public module interfaces for related state. */
public interface SupportCasePort {
    UUID openForOwner(UUID authenticatedOwnerId, String category, String description);
}
