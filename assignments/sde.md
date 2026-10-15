# Software Development Engineer II — Seller Springboard: 30 dated assignments

Amazon job ID: **10579529**. Each day is one 60-minute practice block: 35 minutes implementation, 15 minutes design/review, 10 minutes truthful STAR practice. Two role blocks total 120 minutes per day. Dates and exact fixtures live in [`../SPRINT-MANIFEST.json`](../SPRINT-MANIFEST.json).

Implementation surface: `sde/src/main/java/amazon/sprint/LearnerExercises.java`. Test command: `make test-sde`. There are 29 Java learner methods plus the Day 19 SQL query; each remains a learner TODO. Front-End tasks are wired into the React day navigator and exercise async service ports where specified.

## Day 01 — 2026-10-16: Deduplicate seller registrations
**Pattern:** arrays + hashing. **Leadership Principle:** Customer Obsession. **Implementation:** `LearnerExercises.deduplicateRegistrations` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Return registration records in first-seen order, keeping only the first record for each normalized email (trim, lowercase). Preserve all other fields. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=1`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Design the registration write path so retries cannot create duplicate seller accounts. Name the unique key and explain conflict behavior.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Customer Obsession. Never invent work or metrics.

## Day 02 — 2026-10-17: Sliding window: verification budget
**Pattern:** sliding window. **Leadership Principle:** Ownership. **Implementation:** `LearnerExercises.longestRequestWindow` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Given nonnegative per-request costs and a budget, return the maximum length of a contiguous request window whose sum is at most budget. Check empty input, zero budget, and a single cost above budget. Use the synthetic cases in `fixtures/cases.json` (3 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=2`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** A seller identity provider sends bursts. Describe admission control and what metrics protect user experience.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Ownership. Never invent work or metrics.

## Day 03 — 2026-10-18: Validate onboarding workflow steps
**Pattern:** stack. **Leadership Principle:** Learn and Be Curious. **Implementation:** `LearnerExercises.balancedExpression` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Return true if brackets in a workflow expression are balanced and correctly nested; ignore non-bracket characters. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=3`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** How would you version workflow definitions while keeping in-flight applications safe?

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Learn and Be Curious. Never invent work or metrics.

## Day 04 — 2026-10-19: Find eligibility threshold
**Pattern:** binary search. **Leadership Principle:** Dive Deep. **Implementation:** `LearnerExercises.eligibilityLowerBound` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** For sorted numeric thresholds, return the first index whose value is at least target, or the list length if none exists. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=4`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Explain how to make eligibility rules auditable when thresholds vary by country.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Dive Deep. Never invent work or metrics.

## Day 05 — 2026-10-20: Reverse a linked list of request nodes
**Pattern:** linked list. **Leadership Principle:** Invent and Simplify. **Implementation:** `LearnerExercises.reverseRequestList` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Reverse a singly linked list in place and return the new head. Do not copy node values into a replacement list; handle null and one-node lists. Use the synthetic cases in `fixtures/cases.json` (3 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=5`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Sketch an idempotent registration API response for first request, retry, and payload conflict.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Invent and Simplify. Never invent work or metrics.

## Day 06 — 2026-10-21: Validate prerequisite tree
**Pattern:** tree traversal. **Leadership Principle:** Deliver Results. **Implementation:** `LearnerExercises.breadthFirstStepIds` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Given a nested tree of steps, return step IDs in breadth-first order. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=6`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** How would you detect and communicate a blocked prerequisite without losing the seller’s progress?

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Deliver Results. Never invent work or metrics.

## Day 07 — 2026-10-22: Prioritize manual reviews
**Pattern:** heap / top-k. **Leadership Principle:** Insist on Highest Standards. **Implementation:** `LearnerExercises.prioritizedReviewIds` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Return up to k seller IDs sorted by descending risk score, breaking ties by ascending ID. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=7`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Set a safe review queue policy that avoids starvation and supports a reason for priority.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Insist on Highest Standards. Never invent work or metrics.

## Day 08 — 2026-10-23: Merge compliance windows
**Pattern:** sorting + intervals. **Leadership Principle:** Are Right, A Lot. **Implementation:** `LearnerExercises.mergeComplianceWindows` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Merge overlapping or touching [start,end] intervals, returning sorted intervals. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=8`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** How should the service resolve conflicting policy windows from two authoritative sources?

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Are Right, A Lot. Never invent work or metrics.

## Day 09 — 2026-10-24: Order registration prerequisites
**Pattern:** graph / topological sort. **Leadership Principle:** Think Big. **Implementation:** `LearnerExercises.prerequisiteOrder` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Return a valid topological order of a DAG; return null if a cycle exists. Use lexical order to break ties. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=9`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Design dependency execution for country-specific onboarding without hard-coding one sequence.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Think Big. Never invent work or metrics.

## Day 10 — 2026-10-25: Find valid rule combinations
**Pattern:** backtracking. **Leadership Principle:** Bias for Action. **Implementation:** `LearnerExercises.uniqueRuleCombinations` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** From candidate integer rule IDs, return all unique combinations summing to target; each candidate may be used once. Sort each combination and result lexically. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=10`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** How would you explain a multi-rule rejection so that a seller can fix it?

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Bias for Action. Never invent work or metrics.

## Day 11 — 2026-10-26: Trie prefix lookup for document fields
**Pattern:** trie / prefix search. **Leadership Principle:** Customer Obsession. **Implementation:** `LearnerExercises.triePrefixFields` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Build/use a trie to return the original field names that begin with a case-insensitive prefix, sorted lexicographically. Return all fields for an empty prefix. Use the synthetic cases in `fixtures/cases.json` (3 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=11`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Describe safe extraction boundaries for synthetic document text; what must never be inferred automatically?

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Customer Obsession. Never invent work or metrics.

## Day 12 — 2026-10-27: Minimum-cost onboarding path with DP
**Pattern:** dynamic programming. **Leadership Principle:** Frugality. **Implementation:** `LearnerExercises.minimumOnboardingCost` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Given a nonnegative cost for each ordered step, start before step 0 or step 1 and move forward by one or two steps. Return the minimum cost to reach beyond the final step. Implement a bottom-up DP and explain its state and complexity. Use the synthetic cases in `fixtures/cases.json` (3 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=12`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Discuss when an estimated risk score must not replace a deterministic compliance rule.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Frugality. Never invent work or metrics.

## Day 13 — 2026-10-28: Verification retry schedule
**Pattern:** greedy scheduling. **Leadership Principle:** Ownership. **Implementation:** `LearnerExercises.retryOrder` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** For tasks with retry-after minute and priority, return IDs ordered by earliest retry time, then highest priority, then ID. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=13`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** How do you bound retry storms and honor provider retry-after guidance?

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Ownership. Never invent work or metrics.

## Day 14 — 2026-10-29: Connected duplicate-account clusters
**Pattern:** union-find. **Leadership Principle:** Earn Trust. **Implementation:** `LearnerExercises.duplicateAccountComponents` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Return connected component count for account IDs linked by duplicate pairs. IDs absent from pairs still count as components. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=14`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** What evidence is needed before merging accounts, and how can a mistaken merge be reversed?

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Earn Trust. Never invent work or metrics.

## Day 15 — 2026-10-30: Registration idempotency lookup
**Pattern:** hash map. **Leadership Principle:** Have Backbone; Disagree and Commit. **Implementation:** `LearnerExercises.firstIdempotencyResults` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Apply events to a key-value map: first value for each key wins; return values in input order for every event, with null for duplicate keys. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=15`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Define idempotency scope, retention, and payload-mismatch behavior for a registration endpoint.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Have Backbone; Disagree and Commit. Never invent work or metrics.

## Day 16 — 2026-10-31: Policy version cache
**Pattern:** LRU cache. **Leadership Principle:** Ownership. **Implementation:** `LearnerExercises.simulatePolicyCache` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Simulate an LRU cache of positive capacity. Each op is ["get",key] or ["put",key,value]; return get results (null on miss). Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=16`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** How will policy version changes invalidate cached eligibility without serving stale decisions?

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Ownership. Never invent work or metrics.

## Day 17 — 2026-11-01: Detect overlapping application writes
**Pattern:** concurrency reasoning. **Leadership Principle:** Insist on Highest Standards. **Implementation:** `LearnerExercises.hasConflictingApplicationWrite` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Given versioned updates [applicationId, expectedVersion], return true if two updates target the same application version. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=17`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Explain optimistic concurrency and the user-visible recovery when two devices save simultaneously.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Insist on Highest Standards. Never invent work or metrics.

## Day 18 — 2026-11-02: Compute bounded retry delays
**Pattern:** API / backoff. **Leadership Principle:** Customer Obsession. **Implementation:** `LearnerExercises.boundedRetryDelays` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Return capped exponential delays for attempts 0..n-1, starting at base and capped at max; no random jitter. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=18`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Design an external identity-provider adapter that retries safely after timeout.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Customer Obsession. Never invent work or metrics.

## Day 19 — 2026-11-03: Latest registration status with SQL and an index
**Pattern:** SQL / window functions / indexing. **Leadership Principle:** Dive Deep. **Implementation:** `sde/sql/day19.sql`.

**35 min — implementation:** In `sde/sql/day19.sql`, write SQL that counts sellers by their latest event state (order by created_at descending, then event_id descending). Return state/count rows sorted by state. Create and justify a covering-friendly index for the latest-per-seller access pattern. Use the synthetic cases in `fixtures/cases.json` (3 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sql`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Propose a relational schema and index for latest state plus an immutable audit trail.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Dive Deep. Never invent work or metrics.

**Run:** `python3 scripts/test_day19_sql.py`. The starter reports a recognized learner TODO until the SQL query is written. The assignment is executable against SQLite fixtures, including tie-breaking by `event_id`.

## Day 20 — 2026-11-04: Allowlist analytics properties
**Pattern:** security / validation. **Leadership Principle:** Earn Trust. **Implementation:** `LearnerExercises.allowlistedAnalytics` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Keep only allowlisted keys from an event; reject the entire event if any key contains a forbidden case-insensitive substring from [email,phone,token,health]. Return null on rejection. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=20`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Threat-model seller onboarding telemetry. Separate business audit from best-effort analytics.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Earn Trust. Never invent work or metrics.

## Day 21 — 2026-11-05: Regional policy coverage map
**Pattern:** system design. **Leadership Principle:** Think Big. **Implementation:** `LearnerExercises.unsupportedCountryFields` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Given country requirements and globally supported fields, return each country’s sorted required fields that are unsupported. Omit countries with no gaps. This is a deterministic planning helper; the separate design block is the architecture exercise. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=21`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Draw the high-level architecture; state your consistency choices and how a seller resumes safely.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Think Big. Never invent work or metrics.

## Day 22 — 2026-11-06: Evaluate deterministic compliance rules
**Pattern:** LLD. **Leadership Principle:** Invent and Simplify. **Implementation:** `LearnerExercises.evaluateCompliance` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** For each rule in order, return a decision record with rule id and PASS/FAIL based on whether its required field is present and non-empty in the supplied application. Never infer missing evidence. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=22`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Show interfaces for rule, context, decision, and explanation. Include deterministic ordering.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Invent and Simplify. Never invent work or metrics.

## Day 23 — 2026-11-07: Outbox event ordering
**Pattern:** distributed events. **Leadership Principle:** Ownership. **Implementation:** `LearnerExercises.outboxSequencesIncrease` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Given events with aggregate ID and sequence, return true if each aggregate’s sequence is strictly increasing in given order. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=23`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Explain transactional outbox, consumer deduplication, and reconciliation when a notification is delayed.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Ownership. Never invent work or metrics.

## Day 24 — 2026-11-08: Summarize service health
**Pattern:** observability / SLO. **Leadership Principle:** Success and Scale Bring Broad Responsibility. **Implementation:** `LearnerExercises.serviceAvailabilityPercent` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Compute availability percentage from successful and total requests as a number rounded to two decimals; zero total returns 100. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=24`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Define one user-centric SLO, alert threshold, and an on-call runbook first action.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Success and Scale Bring Broad Responsibility. Never invent work or metrics.

## Day 25 — 2026-11-09: Validate assisted extraction
**Pattern:** LLM guardrails. **Leadership Principle:** Learn and Be Curious. **Implementation:** `LearnerExercises.validateAssistedExtraction` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Given proposed fields and allowed field names, return only allowed keys whose values are strings; return a separate list of rejected keys sorted. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=25`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Design human review, provenance, and deterministic compliance checks around an LLM suggestion.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Learn and Be Curious. Never invent work or metrics.

## Day 26 — 2026-11-10: Classify registration outcomes
**Pattern:** testing / fault injection. **Leadership Principle:** Insist on Highest Standards. **Implementation:** `LearnerExercises.classifyRegistrationResponses` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** For each response code: 200/201 -> success, 409 -> conflict, 429/500/503 -> retryable, all others -> permanent; preserve input order. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=26`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Write a failure-injection test plan for timeout-after-commit and duplicate delivery.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Insist on Highest Standards. Never invent work or metrics.

## Day 27 — 2026-11-11: Top-k review optimization
**Pattern:** refactoring / complexity. **Leadership Principle:** Insist on Highest Standards. **Implementation:** `LearnerExercises.highestReviewScores` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Implement top-k using any approach; explain complexity and avoid sorting all records when k is small. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=27`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Revisit an earlier Striver pattern: compare heap and full sort with bounds and edge cases.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Insist on Highest Standards. Never invent work or metrics.

## Day 28 — 2026-11-12: Minimum window target sum
**Pattern:** timed DSA mock. **Leadership Principle:** Earn Trust. **Implementation:** `LearnerExercises.shortestWindowAtLeast` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Return shortest contiguous subarray length whose sum is at least target; return 0 if none. Values are positive. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=28`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** After coding, state invariants and test a slightly modified case; do not reproduce an anecdote verbatim.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Earn Trust. Never invent work or metrics.

## Day 29 — 2026-11-13: Timed mock: per-seller token bucket
**Pattern:** full role mock / rate limiting. **Leadership Principle:** Deliver Results. **Implementation:** `LearnerExercises.acceptWithinSellerTokenBuckets` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** Implement per-seller token buckets. A new seller starts with capacity tokens at their first request time; elapsed minutes refill at refillPerMinute up to capacity; each request consumes one token if available. Process requests in given order and return an accepted boolean per request. Time is nondecreasing for each seller. Use the synthetic cases in `fixtures/cases.json` (3 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=29`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Run a 15-minute design explanation, then 10 minutes STAR using a true experience; record what you would improve.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Deliver Results. Never invent work or metrics.

## Day 30 — 2026-11-14: Retrospective: find weakest DSA topic
**Pattern:** retrospective / analysis. **Leadership Principle:** Customer Obsession & Ownership. **Implementation:** `LearnerExercises.weakestPracticeTopics` in `sde/src/main/java/amazon/sprint/LearnerExercises.java`.

**35 min — implementation:** From practice attempts grouped by topic, compute each topic’s total passed and total tests. Return every lowest pass-rate topic sorted by name; a topic with zero tests has rate 0. Include a tie case. Use the output to choose a truthful next-month goal in your notes. Use the synthetic cases in `fixtures/cases.json` (3 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `make test-sde-day DAY=30`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**15 min — design/review:** Explain time/space tradeoffs for one prior design and list three follow-up gaps for the next month.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Customer Obsession & Ownership. Never invent work or metrics.
