package coverstory.modules.application;

import java.util.UUID;

/** A journey UUID is only a lookup hint; implementations must verify the authenticated owner. */
public interface ApplicationPort {
    ResumeState resumeOwned(UUID authenticatedOwnerId, UUID journeyId);
    SubmissionResult submit(UUID authenticatedOwnerId, UUID applicationId, long expectedRevision);
}
