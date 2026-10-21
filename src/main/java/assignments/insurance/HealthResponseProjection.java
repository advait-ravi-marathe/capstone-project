package assignments.insurance;

import java.util.Set;

/** Learner-owned allowlist boundary for the public Actuator health response. */
public class HealthResponseProjection {
    /** Controls which aggregate details the public endpoint may serialize. */
    public record Options(boolean showComponents, Set<String> groups) {
        public Options {
            groups = Set.copyOf(groups);
        }
    }

    /** Preserve the evaluated status while selecting the public response fields. */
    public Options publicOptions() {
        // LEARNER TODO: hide components and groups; Boot will retain only the aggregate status.
        throw new UnsupportedOperationException("Day 23 learner TODO: choose the public health projection");
    }
}
