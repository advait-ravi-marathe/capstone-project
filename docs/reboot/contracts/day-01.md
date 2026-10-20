# Day 01 — 2026-10-20: Quote selection identity/order

**State:** CONTRACT SCAFFOLD INSTALLED; learner implementation pending. Learner work is still pending.

Existing feature and acceptance paths are authoritative: `assignments.insurance.QuoteSelectionPolicy` and `POST /api/quote-selections` (existing).

## Fixed acceptance contract

**Course bridge (inside the existing 25-minute concept/course allocation):** [in28Minutes Official, *Master Spring Boot 3 & Spring Framework 6 with Java*](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/), section **“Creating a Java REST API with Spring Boot, Spring Framework and Hibernate.”** Follow the verified title sequence **“Step 00 - Creating a REST API with Spring Boot - An Overview,” “Step 01 - Initializing a REST API Project with Spring Boot,” “Step 02 - Creating a Hello World REST API with Spring Boot,”** then **“Step 03 - Enhancing the Hello World REST API to return a Bean”** (22:21 total). The 22:21 viewing sequence uses the full 25-minute course allocation; count pause/replay time and carry unfinished lessons forward. In the separate 85-minute build/test allocation, reproduce the lesson’s Hello World endpoint and returned bean, then make one tiny request/response change. Use 10 minutes of evidence time for the simple request → Java method → JSON explanation. This beginner bridge does not replace the fixed quote-selection acceptance contract below; its composite identity/order implementation remains deferred and incomplete until the API prerequisite is understood. No AWS certification task is assigned; the day’s 25-minute concept is local API learning. Confirmed AWS courses are Cloud Practitioner CLF-C02 and Solutions Architect Associate, not Developer Associate. See the [Days 1–30 course/lesson map](../course-learning-map.md).

Keep first occurrence of each exact `(journeyId, quoteVersionId)` pair in encounter order. Preserve existing null/validation behavior.

**Edge cases:** Duplicate, interleaving, same version across journeys, distinct versions within a journey, exact strings, and existing invalid-input cases.

**Prerequisite/isolation:** API concept prerequisite; use the guided Hello World example before the quote-selection TODO. Acceptance data is inline.

**Course/concept (25 minutes):** Watch the beginner API lesson sequence above (22:21 at 1×); use the remaining time for a pause or recall. Carry unfinished viewing forward. No AWS certification task.

**Beginner reflection (inside the existing 10-minute evidence block):** In one or two sentences, explain how the request reaches a Java method and how the returned bean becomes JSON. Do not do a design case study before the API lesson.

**Evidence:** See [`docs/day-1.md`](../../day-1.md) and the existing acceptance tests. Do not change the established public shape.

**Today’s first independent DSA pattern anchor:** LeetCode 121 Best Time to Buy and Sell Stock (running-state array scan). Planned first-attempt date: Oct 20; actual attempt and review status remain unrecorded. Use 25 minutes to learn the pattern, 20 minutes to solve, and 15 minutes for same-day recall and the error log. D+3/D+7/D+21 reviews fall on Oct 23, Oct 27, and Nov 10 if the actual attempt occurs Oct 20; recalculate from the actual date. Reviews stay within the 60-minute block and are never marked complete automatically. See [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** How did the course example turn an HTTP request into a JSON response? Composite selection identity/order is a later topic, not a Day 1 prerequisite.

**Observable evidence:** Point to the request path, mapped Java method, returned bean, and JSON response in the guided example. Record the separate LC 121 attempt honestly; do not require a quote-selection implementation explanation before the API lesson.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A3](../interview-evidence-overlay.md#a3) (anecdotal); [A8](../interview-evidence-overlay.md#a8) (anecdotal). These references guide practice; they do not establish question frequency or predict interview content.
