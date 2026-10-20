# CoverStory 30-day reboot contracts

This folder records the reviewed roadmap proposal for 20 October–18 November
2026. It is a set of contracts and schedule metadata, not a completion report.
The existing Day 1 feature remains the live starter; future-day files specify
the intended acceptance contract before learner implementations are created.
Days 2–30 have instructor scaffolds with explicit learner TODOs. Day 2 HTTP wiring has call-count checks and a real Boot app-context test. Days 8–10 and 14 use disposable PostgreSQL. Day 12 has an isolated real Spring Security HTTP chain for authentication and CSRF; the learner ownership policy remains open. Day 13 has a fail-closed GitHub Actions workflow. Day 21 exercises CSRF on the actual quote-selection route through the same test-only chain. Day 28 has a real Chromium acceptance harness that serves the built React test route from Spring, calls test-only endpoints backed by the actual learner services, and checks PostgreSQL persistence and browser reload. An independent validator reports that the revised browser/API/database harness passes with temporary conforming witnesses; those witnesses were restored, so this validates the harness only and does not establish learner completion. The dated DSA sequence groups Top Interview 150 problems by patterns and carries planned D+3/D+7/D+21 reviews in the manifest.

## Products and scope

CoverStory is the primary enterprise-insurance lifecycle application.
HomeSecure is one synthetic CoverStory product, not a separate flagship.
StreamForge is the only second flagship and remains deferred until a later
scope decision. This first 30-day slice does not add real insurers, payments,
customer data, deployment, Kafka, queues, Ktor, or AI features.

## Schedule and evidence

The fixed daily budget is 180 minutes: 60 minutes for Java DSA separately,
plus 120 engineering minutes (85 build/test, 25 concept/course, 10 evidence).
Dates schedule attempts only. Record `not attempted`, `attempted`, `contract
passed`, and `integrated passed` separately. A day or milestone is never
automatically complete because its date passed.

The chronological manifest holds each date, sprint, prerequisite note, focused
engineering concept and exact section link, DSA pattern/problem/review anchors,
guide path, and separate contract/scaffold/runtime/learner/integration statuses:
[manifest.json](manifest.json). Every day has a dated page under
[`contracts/`](contracts/). Day 1's active runnable guide and fixed tests stay
at [docs/day-1.md](../day-1.md); the Day 1 contract page points there.

For Scrum import, [scrum-owner-manifest.json](scrum-owner-manifest.json) has one
consolidated Story per date. Each Story links the independent 120-minute
engineering block and 60-minute Java DSA block, with concept references,
acceptance/evidence guide paths, and review anchors. Sprint 1 covers 20 October–2
November (Days 1–14, 14 Stories); Sprint 2 covers 3–16 November (Days 15–28,
14 Stories); Sprint 3 covers 17–30 November (Days 29–30 are populated on 17–18
November, 2 Stories). Dates and the 180-minute daily allocation are unchanged.
Actual attempts and review completions remain learner evidence; forecasts do
not mark work complete.

## Chronological index

| Day/date | Engineering contract | DSA block | State |
|---|---|---|---|
| 01 · 2026-10-20 | [Day 01](contracts/day-01.md) — Quote selection identity/order | LC 121 Best Time to Buy and Sell Stock — new pattern anchor | installed / not_attempted / blocked |
| 02 · 2026-10-21 | [Day 02](contracts/day-02.md) — Validate selection request at HTTP boundary | Consolidate/review; no new problem | installed / not_attempted / blocked |
| 03 · 2026-10-22 | [Day 03](contracts/day-03.md) — Map failures consistently | LC 1 Two Sum — new pattern anchor | installed / not_attempted / blocked |
| 04 · 2026-10-23 | [Day 04](contracts/day-04.md) — Read one synthetic product | Review due anchors: LC121; no new problem | installed / not_attempted / blocked |
| 05 · 2026-10-24 | [Day 05](contracts/day-05.md) — Decide HomeSecure eligibility | LC 383 Ransom Note — new pattern anchor | installed / not_attempted / blocked |
| 06 · 2026-10-25 | [Day 06](contracts/day-06.md) — Calculate synthetic premium | Review due anchors: LC1; no new problem | installed / not_attempted / blocked |
| 07 · 2026-10-26 | [Day 07](contracts/day-07.md) — Create and expire quote version | LC 49 Group Anagrams — new pattern anchor | installed / not_attempted / blocked |
| 08 · 2026-10-27 | [Day 08](contracts/day-08.md) — Define PostgreSQL quote schema | Review due anchors: LC121, LC383; no new problem | installed / not_attempted / blocked |
| 09 · 2026-10-28 | [Day 09](contracts/day-09.md) — Persist/read immutable quote snapshots | LC 238 Product of Array Except Self — new pattern anchor | installed / not_attempted / blocked |
| 10 · 2026-10-29 | [Day 10](contracts/day-10.md) — Persist selection batch atomically | Review due anchors: LC1, LC49; no new problem | installed / not_attempted / blocked |
| 11 · 2026-10-30 | [Day 11](contracts/day-11.md) — Reject stale quote selection | LC 125 Valid Palindrome — new pattern anchor | installed / not_attempted / blocked |
| 12 · 2026-10-31 | [Day 12](contracts/day-12.md) — Enforce owner-scoped access | Review due anchors: LC383, LC238; no new problem | installed / not_attempted / blocked |
| 13 · 2026-11-01 | [Day 13](contracts/day-13.md) — Add honest repeatable CI | LC 167 Two Sum II - Input Array Is Sorted — new pattern anchor | installed / not_attempted / blocked |
| 14 · 2026-11-02 | [Day 14](contracts/day-14.md) — Milestone 1 backend vertical slice | Review due anchors: LC49, LC125; no new problem | installed / not_attempted / blocked |
| 15 · 2026-11-03 | [Day 15](contracts/day-15.md) — Read journey summary | LC 11 Container With Most Water — new pattern anchor | installed / not_attempted / blocked |
| 16 · 2026-11-04 | [Day 16](contracts/day-16.md) — Bound quote-history reads | Review due anchors: LC238, LC167; no new problem | installed / not_attempted / blocked |
| 17 · 2026-11-05 | [Day 17](contracts/day-17.md) — Render product choices in React/TypeScript | LC 3 Longest Substring Without Repeating Characters — new pattern anchor | installed / not_attempted / blocked |
| 18 · 2026-11-06 | [Day 18](contracts/day-18.md) — Capture quote input | Review due anchors: LC125, LC11; no new problem | installed / not_attempted / blocked |
| 19 · 2026-11-07 | [Day 19](contracts/day-19.md) — Display quote snapshot | LC 209 Minimum Size Subarray Sum — new pattern anchor | installed / not_attempted / blocked |
| 20 · 2026-11-08 | [Day 20](contracts/day-20.md) — Connect selection action UI | Review due anchors: LC167, LC3; no new problem | installed / not_attempted / blocked |
| 21 · 2026-11-09 | [Day 21](contracts/day-21.md) — Protect cookie-authenticated writes | LC 20 Valid Parentheses — new pattern anchor | installed / not_attempted / blocked |
| 22 · 2026-11-10 | [Day 22](contracts/day-22.md) — Add safe request correlation | Review due anchors: LC121, LC11, LC209; no new problem | installed / not_attempted / blocked |
| 23 · 2026-11-11 | [Day 23](contracts/day-23.md) — Expose minimal operational health | LC 155 Min Stack — new pattern anchor | installed / not_attempted / blocked |
| 24 · 2026-11-12 | [Day 24](contracts/day-24.md) — Explain and improve one real query | Review due anchors: LC1, LC3, LC20; no new problem | installed / not_attempted / blocked |
| 25 · 2026-11-13 | [Day 25](contracts/day-25.md) — Handle concurrent repeated selection | LC 704 Binary Search — new pattern anchor | installed / not_attempted / blocked |
| 26 · 2026-11-14 | [Day 26](contracts/day-26.md) — Record minimal selection audit | Review due anchors: LC383, LC209, LC155; no new problem | installed / not_attempted / blocked |
| 27 · 2026-11-15 | [Day 27](contracts/day-27.md) — Fix one evidence-backed regression | LC 35 Search Insert Position — new pattern anchor | installed / not_attempted / blocked |
| 28 · 2026-11-16 | [Day 28](contracts/day-28.md) — Milestone 2 browser-to-database acceptance | Review due anchors: LC49, LC20, LC704; no new problem | partial / not_attempted / blocked |
| 29 · 2026-11-17 | [Day 29](contracts/day-29.md) — Make the slice reproducible | LC 153 Find Minimum in Rotated Sorted Array — new pattern anchor | installed / not_attempted / blocked |
| 30 · 2026-11-18 | [Day 30](contracts/day-30.md) — Defend and assess the slice | Review due anchors: LC238, LC155, LC35; no new problem | installed / not_attempted / blocked |

## Scaffold and dependency rules

Contract pages distinguish scaffolding from learner completion. Day 12 and Day 21 use actual Spring Security filters in an isolated HTTP test chain; production security policy/configuration remains a learner TODO. Day 13 has an instructor test for the runnable workflow and its fail-closed commands; local expected-red evidence is separate from remote Actions execution. Day 14 now has a real PostgreSQL integration path through actual learner components, tagged acceptance, that exposes the first incomplete TODO. Day 25 HTTP/auth composition remains open. Day 28's Playwright browser harness renders the actual React test route and checks the full quote, selection and reload path against disposable PostgreSQL; expected-red results are recorded separately from infrastructure checks. Day 29 has an embedded PostgreSQL positive reset path that does not need psql; the external URI mode remains guarded. System design is practiced inside the 85/25/10 engineering split: use
only the diagram/ADR or measurement suited to that day, with the blank prompts
in the [system design practice guide](system-design-practice.md). Every focused test must
instantiate only its own subject; prior collaborators use explicit test-only
canned values. A stub may provide fixed fixture data, but must not implement
an earlier learner rule. Never use a skipped/disabled test, catch-all
assertion, or missing-test tolerance to make a day appear green.

Infrastructure checks remain separate from learner acceptance. A cumulative
milestone requires its real prerequisites and completed production components.
When prerequisites are open, an explicit readiness check reports `blocked`
and names the missing days; it does not turn that integration test into a
pass. PostgreSQL behavior uses disposable real PostgreSQL, fixed clocks/IDs,
and synchronization barriers for concurrency checks.

Day 14 and Day 28 are integration gates, not extra features to start early.
Day 4–7 use the single instructor-owned [synthetic HomeSecure catalog
fixture](../../src/test/resources/assignments/reboot/homesecure-catalog.json),
whose values are pinned by an infrastructure integrity test. It is training
data only and is not an insurance offer or advice.
Day 29's concrete deliverable is a runbook plus a safe deterministic synthetic
fixture-reset command and safety test. Day 30's concrete deliverable is a
machine-checkable acceptance checklist that verifies evidence links and leaves
missing items open; the review discussion alone is not the deliverable.

## DSA companion

LeetCode Top Interview 150 is the baseline, organized by recurring patterns
and prerequisite concepts rather than rigid problem order. Every DSA block is
60 minutes: 25 minutes pattern learning, 20 minutes solving, and 15 minutes active
retrieval/error logging. Odd study days introduce the 15 mapped pattern anchors;
even days consolidate with due reviews and no new mandatory problem. The anchor
sequence is LC121 (running state), LC1 (hash lookup), LC383 (frequencies), LC49
(grouping key), LC238 (prefix/suffix), LC125 (inward pointers), LC167 (sorted
two pointers), LC11 (pointer elimination), LC3 (variable window), LC209
(shrinking valid window), LC20 (stack), LC155 (augmented stack), LC704 (binary-
search bridge outside Top 150), LC35 (boundary search), and LC153 (rotated search). Reviews stay within the same block. Compute due dates from the actual
first-attempt date and never mark a planned attempt or review complete
automatically. Reviews use the actual first-attempt date. Up to three due anchors may share a consolidation day; failed recall displaces a future anchor rather than adding overtime. Log every due review and result; overflow remains visibly due and carries to the next consolidation block instead of being silently dropped or marked complete. Day 1 remains LC 121 and is Advait's first independent attempt;
independent-solve credit for LC 121 is zero. This does not erase the historical
reports of three screenshots and seven assisted practice sessions; those remain
recorded as assisted practice, not independent solves. Today's planned reviews are 23 October, 27 October,
and 10 November if the attempt occurs on 20 October; otherwise recalculate
from the recorded actual date. Use the [error-log template](dsa-error-log-template.md).

## Understanding and interview readiness

Each engineering contract includes an understanding check inside its existing 10-minute evidence block. Choose one or two prompts, not a long questionnaire. The learner explains what the feature does, why its data structure/module/transaction choice fits, one alternative, and one failure case; then makes or debugs a small change without AI assistance. Milestone mock interviews replace implementation time within the 85-minute block and never add hours.

Track three distinct outcomes: **behavior** (fixed tests and exact command), **explanation** (invariants, boundaries, and trade-offs explained clearly), and **independent adaptation** (a small change or debugging task completed without AI). Passing tests or the presence of scaffolding earns no credit on the other axes. The final checklist keeps these results separate.

## Interview evidence and source confidence

The [interview evidence overlay](interview-evidence-overlay.md) separates official, role-dependent expectations from dated public first-person reports. Official guidance is not a guarantee of a particular team’s interview. Anecdotes are self-reported, selectively published and not representative; mapped questions are original practice prompts, not claimed verbatim questions. This month is foundation practice, not an SDE II readiness claim.

## Reference index

Official concept references from the reviewed proposal: [AWS API
objectives](https://docs.aws.amazon.com/aws-certification/latest/developer-associate-02/developer-associate-02-domain1.html),
[Spring web tests](https://spring.io/guides/gs/testing-web/),
[Java BigDecimal](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html),
[Java Clock](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Clock.html),
[PostgreSQL 14 constraints](https://www.postgresql.org/docs/14/ddl-constraints.html),
[Spring Data JPA](https://spring.io/guides/gs/accessing-data-jpa/),
[PostgreSQL 14 transactions](https://www.postgresql.org/docs/14/transaction-iso.html),
[Spring Security authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html),
[GitHub Actions Java/Maven](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven),
[React state](https://react.dev/learn/managing-state),
[React TypeScript](https://react.dev/learn/typescript),
[Spring Security CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html),
[Spring Boot actuator](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html),
and [PostgreSQL 14 EXPLAIN](https://www.postgresql.org/docs/14/using-explain.html).
Match examples to the repository's installed versions when a future stack is
introduced.

## Later isolated continuation

[Days 45–56](continuation-45-56/README.md) continue the synthetic HomeSecure
policy lifecycle with a read-only, non-binding renewal preview. Its canonical
manifest and Scrum owner export are additive planning artifacts; neither edits
the board or claims learner completion.

[Days 71–84](continuation-71-84/README.md) continue the same read-only renewal
preview with a bounded synchronous capacity gate and recovery evidence. The
manifest and Story export are documentation only; this work does not update
the Scrum board.
