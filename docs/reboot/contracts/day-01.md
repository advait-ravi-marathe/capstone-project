# Day 01 — 2026-10-20: Quote selection identity/order

**State:** CONTRACT SCAFFOLD INSTALLED; learner implementation pending. Learner work is still pending.

Existing feature and acceptance paths are authoritative: `assignments.insurance.QuoteSelectionPolicy` and `POST /api/quote-selections` (existing).

## Fixed acceptance contract

**Course bridge (inside the existing 25-minute concept/course allocation):** [in28Minutes Official, *Master Spring Boot 3 & Spring Framework 6 with Java*](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/), section **“Creating a Java REST API with Spring Boot, Spring Framework and Hibernate.”** Follow the verified title sequence **“Step 00 - Creating a REST API with Spring Boot - An Overview,” “Step 01 - Initializing a REST API Project with Spring Boot,” “Step 02 - Creating a Hello World REST API with Spring Boot,”** then **“Step 03 - Enhancing the Hello World REST API to return a Bean”** (22:21 total). Stop by 25 minutes; carry the next lesson forward. Reproduce the demonstrated endpoint before one tiny request adaptation. Explain request → controller method → Java result → JSON. This beginner bridge does not replace the fixed quote-selection acceptance contract below; its composite identity/order implementation remains deferred and incomplete until the API prerequisite is understood. No AWS certification task is assigned; the day’s 25-minute concept is local API learning. Confirmed AWS courses are Cloud Practitioner CLF-C02 and Solutions Architect Associate, not Developer Associate. See the [Days 1–30 course/lesson map](../course-learning-map.md).

Keep first occurrence of each exact `(journeyId, quoteVersionId)` pair in encounter order. Preserve existing null/validation behavior.

**Edge cases:** Duplicate, interleaving, same version across journeys, distinct versions within a journey, exact strings, and existing invalid-input cases.

**Prerequisite/isolation:** No implementation prerequisite; inline acceptance data.

**Concept (25 minutes):** Beginner API request flow, JSON response shape, composite identity, and encounter-order contract; no AWS certification task. Distinguish in-memory deduplication from later database uniqueness, atomic writes, and concurrent retry safety.

**System-design prompt (within the 25-minute concept block):** Sketch request → controller → policy → response, mark validation ownership, and explain the composite-identity/order invariants in your own words. Do not add persistence or infrastructure. Use only the applicable blank template in the [system design guide](../system-design-practice.md).

**Evidence:** See [`docs/day-1.md`](../../day-1.md) and the existing acceptance tests. Do not change the established public shape.

**Today’s first independent DSA pattern anchor:** LeetCode 121 Best Time to Buy and Sell Stock (running-state array scan). Planned first-attempt date: Oct 20; actual attempt and review status remain unrecorded. Use 25 minutes to learn the pattern, 20 minutes to solve, and 15 minutes for same-day recall and the error log. D+3/D+7/D+21 reviews fall on Oct 23, Oct 27, and Nov 10 if the actual attempt occurs Oct 20; recalculate from the actual date. Reviews stay within the 60-minute block and are never marked complete automatically. See [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** What makes two selections equal? Why must output retain encounter order? If journeys match but versions differ, what survives?

**Observable evidence:** Trace interleaved duplicates by hand; explain equality/hash contract and time/space cost without being told a collection choice. Separately explain LC 121’s buy-before-sell invariant and a falling-price counterexample.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A3](../interview-evidence-overlay.md#a3) (anecdotal); [A8](../interview-evidence-overlay.md#a8) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
