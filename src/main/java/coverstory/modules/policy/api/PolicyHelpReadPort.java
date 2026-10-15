package coverstory.modules.policy.api;

import java.util.List;
import java.util.UUID;

/** Policy-owned read-only facts for the synthetic, grounded policy-help demonstration. */
public interface PolicyHelpReadPort {
    HelpResult read(UUID authenticatedOwnerId, String fixedQuestionId);

    record PolicyFact(String id, String applicationId, String issuanceReference,
                      String effectiveDate, String termsVersion) { }
    record TimelineEvent(String id, String occurredAt, String label) { }
    record Citation(String sourceId, String version, String excerpt) { }
    record HelpResult(String state, String wording, List<PolicyFact> policies,
                      List<TimelineEvent> timeline, List<Citation> citations,
                      int writes) {
        public HelpResult {
            policies = List.copyOf(policies);
            timeline = List.copyOf(timeline);
            citations = List.copyOf(citations);
        }
    }
}
