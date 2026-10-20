package assignments.insurance;

import java.util.List;

/** Result of the Day 28 prerequisite gate; BLOCKED is not an integration pass. */
public record Day28ReadinessReport(String status, List<Integer> missingPrerequisiteDays) {
    public Day28ReadinessReport {
        missingPrerequisiteDays = List.copyOf(missingPrerequisiteDays);
    }
}
