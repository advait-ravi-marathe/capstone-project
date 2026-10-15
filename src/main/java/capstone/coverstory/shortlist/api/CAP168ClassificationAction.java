package capstone.coverstory.shortlist.api;

import java.util.List;

/** Injectable adapter for the learner-owned strict quote-review line classifier. */
@FunctionalInterface
public interface CAP168ClassificationAction {
    CAP168Seam.Classification classify(List<String> physicalLines);
}
