package amazon.sprint;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Typed Java contracts for the SDE II track. SQL Day 19 is in ../sql/day19.sql. */
public interface ExerciseSet {
    record Registration(String email, String id, Map<String, String> attributes) {}
    static final class ListNode {
        public final String value;
        public ListNode next;
        public ListNode(String value, ListNode next) { this.value = value; this.next = next; }
    }
    record StepNode(String id, List<StepNode> children) {}
    record Review(String id, int score) {}
    record Interval(int start, int end) {}
    record Dependency(String step, String prerequisite) {}
    record RetryTask(String id, int atMinute, int priority) {}
    record AccountLink(String left, String right) {}
    record IdempotencyEvent(String key, String value) {}
    record CacheOperation(String operation, String key, Integer value) {}
    record VersionedUpdate(String applicationId, long expectedVersion) {}
    record CountryRequirements(String country, List<String> fields) {}
    record Rule(String id, String requiredField) {}
    record RuleResult(String id, String decision) {}
    record OutboxEvent(String aggregateId, long sequence) {}
    record ExtractionResult(Map<String, String> accepted, List<String> rejected) {}
    record PracticeAttempt(String topic, int passed, int total) {}
    record TokenBucketRequest(String sellerId, double minute) {}

    List<Registration> deduplicateRegistrations(List<Registration> registrations);
    int longestRequestWindow(List<Integer> costs, int budget);
    boolean balancedExpression(String text);
    int eligibilityLowerBound(List<Integer> sortedThresholds, int target);
    ListNode reverseRequestList(ListNode head);
    List<String> breadthFirstStepIds(StepNode root);
    List<String> prioritizedReviewIds(List<Review> reviews, int k);
    List<Interval> mergeComplianceWindows(List<Interval> windows);
    List<String> prerequisiteOrder(List<String> steps, List<Dependency> dependencies);
    List<List<Integer>> uniqueRuleCombinations(List<Integer> candidates, int target);
    List<String> triePrefixFields(List<String> fieldNames, String prefix);
    int minimumOnboardingCost(List<Integer> stepCosts);
    List<String> retryOrder(List<RetryTask> tasks);
    int duplicateAccountComponents(List<String> ids, List<AccountLink> links);
    List<String> firstIdempotencyResults(List<IdempotencyEvent> events);
    List<Integer> simulatePolicyCache(int capacity, List<CacheOperation> operations);
    boolean hasConflictingApplicationWrite(List<VersionedUpdate> updates);
    List<Integer> boundedRetryDelays(int attempts, int base, int maximum);
    // Day 19 is a SQL query and index exercise in sde/sql/day19.sql.
    Map<String, String> allowlistedAnalytics(Map<String, String> event, Set<String> allowedKeys);
    Map<String, List<String>> unsupportedCountryFields(List<CountryRequirements> requirements, Set<String> supportedFields);
    List<RuleResult> evaluateCompliance(List<Rule> rules, Map<String, String> application);
    boolean outboxSequencesIncrease(List<OutboxEvent> events);
    double serviceAvailabilityPercent(long successful, long total);
    ExtractionResult validateAssistedExtraction(Map<String, Object> proposed, Set<String> allowedKeys);
    List<String> classifyRegistrationResponses(List<Integer> statusCodes);
    List<Integer> highestReviewScores(List<Integer> scores, int k);
    int shortestWindowAtLeast(List<Integer> positiveValues, int target);
    List<Boolean> acceptWithinSellerTokenBuckets(int capacity, double refillPerMinute, List<TokenBucketRequest> requests);
    List<String> weakestPracticeTopics(List<PracticeAttempt> attempts);
}
