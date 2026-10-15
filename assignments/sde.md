# Software Development Engineer II — Seller Springboard: 30 dated assignments

Amazon job ID: **10579529**. Each day takes 60 minutes: 35 minutes implement/solve, 15 minutes role-specific design or review, and 10 minutes Leadership Principle STAR practice. Dates and case fixtures are in [`../SPRINT-MANIFEST.json`](../SPRINT-MANIFEST.json).

## Day 01 — 2026-10-16: Deduplicate seller registrations
**Pattern:** arrays + hashing. **Leadership Principle:** Customer Obsession.

**35 min — code:** Return registration records in first-seen order, keeping only the first record for each normalized email (trim, lowercase). Preserve all other fields. Implement `sde.assignments.day_01`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Design the registration write path so retries cannot create duplicate seller accounts. Name the unique key and explain conflict behavior.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Customer Obsession. Do not invent work history or metrics.

## Day 02 — 2026-10-17: Longest safe verification burst
**Pattern:** sliding window. **Leadership Principle:** Ownership.

**35 min — code:** Given nonnegative per-request costs and a budget, return the maximum length of a contiguous request window whose sum is at most budget. Implement `sde.assignments.day_02`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** A seller identity provider sends bursts. Describe admission control and what metrics protect user experience.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Ownership. Do not invent work history or metrics.

## Day 03 — 2026-10-18: Validate onboarding workflow steps
**Pattern:** stack. **Leadership Principle:** Learn and Be Curious.

**35 min — code:** Return true if brackets in a workflow expression are balanced and correctly nested; ignore non-bracket characters. Implement `sde.assignments.day_03`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** How would you version workflow definitions while keeping in-flight applications safe?

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Learn and Be Curious. Do not invent work history or metrics.

## Day 04 — 2026-10-19: Find eligibility threshold
**Pattern:** binary search. **Leadership Principle:** Dive Deep.

**35 min — code:** For sorted numeric thresholds, return the first index whose value is at least target, or the list length if none exists. Implement `sde.assignments.day_04`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Explain how to make eligibility rules auditable when thresholds vary by country.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Dive Deep. Do not invent work history or metrics.

## Day 05 — 2026-10-20: First repeated request key
**Pattern:** linked list / idempotency. **Leadership Principle:** Invent and Simplify.

**35 min — code:** Given an ordered list of request keys, return the first key seen twice, or null. Treat keys as case-sensitive strings. Implement `sde.assignments.day_05`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Sketch an idempotent registration API response for first request, retry, and payload conflict.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Invent and Simplify. Do not invent work history or metrics.

## Day 06 — 2026-10-21: Validate prerequisite tree
**Pattern:** tree traversal. **Leadership Principle:** Deliver Results.

**35 min — code:** Given a nested tree of steps, return step IDs in breadth-first order. Implement `sde.assignments.day_06`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** How would you detect and communicate a blocked prerequisite without losing the seller’s progress?

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Deliver Results. Do not invent work history or metrics.

## Day 07 — 2026-10-22: Prioritize manual reviews
**Pattern:** heap / top-k. **Leadership Principle:** Insist on Highest Standards.

**35 min — code:** Return up to k seller IDs sorted by descending risk score, breaking ties by ascending ID. Implement `sde.assignments.day_07`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Set a safe review queue policy that avoids starvation and supports a reason for priority.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Insist on Highest Standards. Do not invent work history or metrics.

## Day 08 — 2026-10-23: Merge compliance windows
**Pattern:** sorting + intervals. **Leadership Principle:** Are Right, A Lot.

**35 min — code:** Merge overlapping or touching [start,end] intervals, returning sorted intervals. Implement `sde.assignments.day_08`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** How should the service resolve conflicting policy windows from two authoritative sources?

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Are Right, A Lot. Do not invent work history or metrics.

## Day 09 — 2026-10-24: Order registration prerequisites
**Pattern:** graph / topological sort. **Leadership Principle:** Think Big.

**35 min — code:** Return a valid topological order of a DAG; return null if a cycle exists. Use lexical order to break ties. Implement `sde.assignments.day_09`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Design dependency execution for country-specific onboarding without hard-coding one sequence.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Think Big. Do not invent work history or metrics.

## Day 10 — 2026-10-25: Find valid rule combinations
**Pattern:** backtracking. **Leadership Principle:** Bias for Action.

**35 min — code:** From candidate integer rule IDs, return all unique combinations summing to target; each candidate may be used once. Sort each combination and result lexically. Implement `sde.assignments.day_10`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** How would you explain a multi-rule rejection so that a seller can fix it?

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Bias for Action. Do not invent work history or metrics.

## Day 11 — 2026-10-26: Match required document fields
**Pattern:** trie / strings. **Leadership Principle:** Customer Obsession.

**35 min — code:** Return sorted required field names that are not present in a submitted object, comparing names case-insensitively. Implement `sde.assignments.day_11`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Describe safe extraction boundaries for synthetic document text; what must never be inferred automatically?

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Customer Obsession. Do not invent work history or metrics.

## Day 12 — 2026-10-27: Minimum-cost onboarding path
**Pattern:** dynamic programming. **Leadership Principle:** Frugality.

**35 min — code:** Given nonnegative costs for ordered steps and prerequisite alternatives represented as a sequence, return minimum cost to complete all steps with skip penalty. Use DP: each step can be paid or skipped at penalty. Implement `sde.assignments.day_12`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Discuss when an estimated risk score must not replace a deterministic compliance rule.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Frugality. Do not invent work history or metrics.

## Day 13 — 2026-10-28: Verification retry schedule
**Pattern:** greedy scheduling. **Leadership Principle:** Ownership.

**35 min — code:** For tasks with retry-after minute and priority, return IDs ordered by earliest retry time, then highest priority, then ID. Implement `sde.assignments.day_13`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** How do you bound retry storms and honor provider retry-after guidance?

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Ownership. Do not invent work history or metrics.

## Day 14 — 2026-10-29: Connected duplicate-account clusters
**Pattern:** union-find. **Leadership Principle:** Earn Trust.

**35 min — code:** Return connected component count for account IDs linked by duplicate pairs. IDs absent from pairs still count as components. Implement `sde.assignments.day_14`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** What evidence is needed before merging accounts, and how can a mistaken merge be reversed?

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Earn Trust. Do not invent work history or metrics.

## Day 15 — 2026-10-30: Registration idempotency lookup
**Pattern:** hash map. **Leadership Principle:** Have Backbone; Disagree and Commit.

**35 min — code:** Apply events to a key-value map: first value for each key wins; return values in input order for every event, with null for duplicate keys. Implement `sde.assignments.day_15`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Define idempotency scope, retention, and payload-mismatch behavior for a registration endpoint.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Have Backbone; Disagree and Commit. Do not invent work history or metrics.

## Day 16 — 2026-10-31: Policy version cache
**Pattern:** LRU cache. **Leadership Principle:** Ownership.

**35 min — code:** Simulate an LRU cache of positive capacity. Each op is ["get",key] or ["put",key,value]; return get results (null on miss). Implement `sde.assignments.day_16`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** How will policy version changes invalidate cached eligibility without serving stale decisions?

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Ownership. Do not invent work history or metrics.

## Day 17 — 2026-11-01: Detect overlapping application writes
**Pattern:** concurrency reasoning. **Leadership Principle:** Insist on Highest Standards.

**35 min — code:** Given versioned updates [applicationId, expectedVersion], return true if two updates target the same application version. Implement `sde.assignments.day_17`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Explain optimistic concurrency and the user-visible recovery when two devices save simultaneously.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Insist on Highest Standards. Do not invent work history or metrics.

## Day 18 — 2026-11-02: Compute bounded retry delays
**Pattern:** API / backoff. **Leadership Principle:** Customer Obsession.

**35 min — code:** Return capped exponential delays for attempts 0..n-1, starting at base and capped at max; no random jitter. Implement `sde.assignments.day_18`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Design an external identity-provider adapter that retries safely after timeout.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Customer Obsession. Do not invent work history or metrics.

## Day 19 — 2026-11-03: Aggregate registration states
**Pattern:** SQL / indexing. **Leadership Principle:** Dive Deep.

**35 min — code:** Given rows with seller_id and state, return counts by state, sorted by state name. Implement `sde.assignments.day_19`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Propose a relational schema and index for latest state plus an immutable audit trail.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Dive Deep. Do not invent work history or metrics.

## Day 20 — 2026-11-04: Allowlist analytics properties
**Pattern:** security / validation. **Leadership Principle:** Earn Trust.

**35 min — code:** Keep only allowlisted keys from an event; reject the entire event if any key contains a forbidden case-insensitive substring from [email,phone,token,health]. Return null on rejection. Implement `sde.assignments.day_20`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Threat-model seller onboarding telemetry. Separate business audit from best-effort analytics.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Earn Trust. Do not invent work history or metrics.

## Day 21 — 2026-11-05: Regional policy coverage map
**Pattern:** system design. **Leadership Principle:** Think Big.

**35 min — code:** Given country requirements and globally supported fields, return each country’s sorted required fields that are unsupported. Omit countries with no gaps. This is a deterministic planning helper; the separate design block is the architecture exercise. Implement `sde.assignments.day_21`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Draw the high-level architecture; state your consistency choices and how a seller resumes safely.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Think Big. Do not invent work history or metrics.

## Day 22 — 2026-11-06: Evaluate deterministic compliance rules
**Pattern:** LLD. **Leadership Principle:** Invent and Simplify.

**35 min — code:** For each rule in order, return a decision record with rule id and PASS/FAIL based on whether its required field is present and non-empty in the supplied application. Never infer missing evidence. Implement `sde.assignments.day_22`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Show interfaces for rule, context, decision, and explanation. Include deterministic ordering.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Invent and Simplify. Do not invent work history or metrics.

## Day 23 — 2026-11-07: Outbox event ordering
**Pattern:** distributed events. **Leadership Principle:** Ownership.

**35 min — code:** Given events with aggregate ID and sequence, return true if each aggregate’s sequence is strictly increasing in given order. Implement `sde.assignments.day_23`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Explain transactional outbox, consumer deduplication, and reconciliation when a notification is delayed.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Ownership. Do not invent work history or metrics.

## Day 24 — 2026-11-08: Summarize service health
**Pattern:** observability / SLO. **Leadership Principle:** Success and Scale Bring Broad Responsibility.

**35 min — code:** Compute availability percentage from successful and total requests as a number rounded to two decimals; zero total returns 100. Implement `sde.assignments.day_24`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Define one user-centric SLO, alert threshold, and an on-call runbook first action.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Success and Scale Bring Broad Responsibility. Do not invent work history or metrics.

## Day 25 — 2026-11-09: Validate assisted extraction
**Pattern:** LLM guardrails. **Leadership Principle:** Learn and Be Curious.

**35 min — code:** Given proposed fields and allowed field names, return only allowed keys whose values are strings; return a separate list of rejected keys sorted. Implement `sde.assignments.day_25`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Design human review, provenance, and deterministic compliance checks around an LLM suggestion.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Learn and Be Curious. Do not invent work history or metrics.

## Day 26 — 2026-11-10: Classify registration outcomes
**Pattern:** testing / fault injection. **Leadership Principle:** Insist on Highest Standards.

**35 min — code:** For each response code: 200/201 -> success, 409 -> conflict, 429/500/503 -> retryable, all others -> permanent; preserve input order. Implement `sde.assignments.day_26`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Write a failure-injection test plan for timeout-after-commit and duplicate delivery.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Insist on Highest Standards. Do not invent work history or metrics.

## Day 27 — 2026-11-11: Top-k review optimization
**Pattern:** refactoring / complexity. **Leadership Principle:** Insist on Highest Standards.

**35 min — code:** Implement top-k using any approach; explain complexity and avoid sorting all records when k is small. Implement `sde.assignments.day_27`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Revisit an earlier Striver pattern: compare heap and full sort with bounds and edge cases.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Insist on Highest Standards. Do not invent work history or metrics.

## Day 28 — 2026-11-12: Minimum window target sum
**Pattern:** timed DSA mock. **Leadership Principle:** Earn Trust.

**35 min — code:** Return shortest contiguous subarray length whose sum is at least target; return 0 if none. Values are positive. Implement `sde.assignments.day_28`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** After coding, state invariants and test a slightly modified case; do not reproduce an anecdote verbatim.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Earn Trust. Do not invent work history or metrics.

## Day 29 — 2026-11-13: Registration mock: classify decisions
**Pattern:** full role mock. **Leadership Principle:** Deliver Results.

**35 min — code:** For a timed mock, implement the decision classifier: APPROVED and PENDING_REVIEW remain unchanged; missing or unknown states become NEEDS_REVIEW. Then use the separate role mock prompt for a timed coding/design/STAR practice. Implement `sde.assignments.day_29`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Run a 15-minute design explanation, then 10 minutes STAR using a true experience; record what you would improve.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Deliver Results. Do not invent work history or metrics.

## Day 30 — 2026-11-14: Weakest-pattern redo
**Pattern:** retrospective. **Leadership Principle:** Customer Obsession & Ownership.

**35 min — code:** Choose your weakest prior algorithm. Solve a fresh instance in the provided notes; function returns the sum of a list as a simple warm-up. Implement `sde.assignments.day_30`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Explain time/space tradeoffs for one prior design and list three follow-up gaps for the next month.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Customer Obsession & Ownership. Do not invent work history or metrics.
