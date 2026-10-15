package coverstory.modules.application.api;

import java.util.UUID;
import java.util.List;

/** Application-owned, read-only review status facts exposed to Support. */
public interface ReviewStatusFactsPort {
    ReviewStatusFact findSyntheticReview(UUID applicationId);
    record ReviewStatusFact(UUID applicationId, String status, List<String> requestedFieldCodes) {
        public ReviewStatusFact { requestedFieldCodes = List.copyOf(requestedFieldCodes); }
    }
}
