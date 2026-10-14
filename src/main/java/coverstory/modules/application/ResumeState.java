package coverstory.modules.application;

import java.util.UUID;

/** Authoritative application revision and quote reference resolved after owner authorization. */
public record ResumeState(UUID applicationId, long revision, UUID quoteId) { }
