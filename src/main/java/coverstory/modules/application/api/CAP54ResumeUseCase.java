package coverstory.modules.application.api;

import java.util.UUID;

/** Application-owned boundary used by the private journey HTTP adapter. */
public interface CAP54ResumeUseCase {
    CAP54Seam.Reply resume(String authenticatedOwnerId, UUID journeyId);
    CAP54Seam.Reply list(String authenticatedOwnerId, int limit, String ownerBoundCursor);
}
