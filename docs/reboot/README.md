# CoverStory 30-day reboot contracts

This folder records the reviewed roadmap proposal for 20 October–18 November
2026. It is a set of contracts and schedule metadata, not a completion report.
The existing Day 1 feature remains the live starter; future-day files specify
the intended acceptance contract before learner implementations are created.
Days 2–30 have instructor scaffolds with explicit learner TODOs. Day 2 HTTP wiring has call-count checks and a real Boot app-context test. Days 8–10 and 14 use disposable PostgreSQL. Day 12 has an isolated real Spring Security HTTP chain for authentication and CSRF; the learner ownership policy remains open. Day 13 has a fail-closed GitHub Actions workflow. Day 21 exercises CSRF on the actual quote-selection route through the same test-only chain. Day 28 has a real Chromium acceptance harness that serves the built React test route from Spring, calls test-only endpoints backed by the actual learner services, and checks PostgreSQL persistence and browser reload. An independent validator reports that the revised browser/API/database harness passes with temporary conforming witnesses; those witnesses were restored, so this validates the harness only and does not establish learner completion. The DSA schedule is actual-attempt-led. Guided LeetCode Premium/editorial study is not independent-solve credit; reviews and transfers are recorded only when attempted. See the [shared DSA policy](dsa/guided-transfer-policy.md).

## Course and lesson guidance

The [Days 1–30 course/lesson map](course-learning-map.md) and ticket-ready [machine-readable course blocks](course-learning-blocks.json) give exact verified lecture titles where useful. The [short worked guides](course-learning-supplements.md) close the documented teaching gaps without replacing learner acceptance work. Course study remains one combined 25-minute allocation inside 120 engineering minutes; unfinished lessons carry forward and implementation scope shrinks. The supplied course catalogs were checked for titles, but videos were not watched. Additional in28Minutes recommendations and the preferred React course have unconfirmed access; do not purchase or enroll. The owned AWS courses are optional future references only; there is no required AWS lecture in Days 1–30.

## Products and scope

CoverStory is the primary enterprise-insurance lifecycle application.
HomeSecure is one synthetic CoverStory product, not a separate flagship.
StreamForge is the only second flagship and remains deferred until a later
scope decision. This first 30-day slice does not add real insurers, payments,
customer data, deployment, Kafka, queues, Ktor, or AI features.

## Schedule and evidence

The DSA block is workload-tiered: 1–2 substantive exercises use 60 minutes, 3 use 75, and 4 use 90. Engineering remains 120 minutes (85 build/test, 25 concept/course, 10 evidence), for 180/195/210 total minutes.
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
consolidated Story per date. Each Story links the unchanged 120-minute engineering block and workload-tiered Java DSA block (60/75/90 minutes), with concept references,
acceptance/evidence guide paths, and review anchors. Sprint 1 covers 20 October–2
November (Days 1–14, 14 Stories); Sprint 2 covers 3–16 November (Days 15–28,
14 Stories); Sprint 3 covers 17–30 November (Days 29–30 are populated on 17–18
November, 2 Stories). Engineering remains 120 minutes; total time varies by the DSA workload tier.
Actual attempts and review completions remain learner evidence; forecasts do
not mark work complete.

## Chronological index

| Day/date | Engineering contract | DSA block | State |
|---|---|---|---|
| 01 · 2026-10-20 | [Day 01](contracts/day-01.md) — Quote selection identity/order | LC121 guided | installed / not_attempted / blocked |
| 02 · 2026-10-21 | [Day 02](contracts/day-02.md) — Validate selection request at HTTP boundary | LC1 guided | installed / not_attempted / blocked |
| 03 · 2026-10-22 | [Day 03](contracts/day-03.md) — Map failures consistently | LC383 guided | installed / not_attempted / blocked |
| 04 · 2026-10-23 | [Day 04](contracts/day-04.md) — Read one synthetic product | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 05 · 2026-10-24 | [Day 05](contracts/day-05.md) — Decide HomeSecure eligibility | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 06 · 2026-10-25 | [Day 06](contracts/day-06.md) — Calculate synthetic premium | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 07 · 2026-10-26 | [Day 07](contracts/day-07.md) — Create and expire quote version | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 08 · 2026-10-27 | [Day 08](contracts/day-08.md) — Define PostgreSQL quote schema | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 09 · 2026-10-28 | [Day 09](contracts/day-09.md) — Persist/read immutable quote snapshots | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 10 · 2026-10-29 | [Day 10](contracts/day-10.md) — Persist selection batch atomically | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 11 · 2026-10-30 | [Day 11](contracts/day-11.md) — Reject stale quote selection | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 12 · 2026-10-31 | [Day 12](contracts/day-12.md) — Enforce owner-scoped access | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 13 · 2026-11-01 | [Day 13](contracts/day-13.md) — Add honest repeatable CI | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 14 · 2026-11-02 | [Day 14](contracts/day-14.md) — Milestone 1 backend vertical slice | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 15 · 2026-11-03 | [Day 15](contracts/day-15.md) — Read journey summary | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 16 · 2026-11-04 | [Day 16](contracts/day-16.md) — Bound quote-history reads | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 17 · 2026-11-05 | [Day 17](contracts/day-17.md) — Render product choices in React/TypeScript | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 18 · 2026-11-06 | [Day 18](contracts/day-18.md) — Capture quote input | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 19 · 2026-11-07 | [Day 19](contracts/day-19.md) — Display quote snapshot | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 20 · 2026-11-08 | [Day 20](contracts/day-20.md) — Connect selection action UI | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 21 · 2026-11-09 | [Day 21](contracts/day-21.md) — Protect cookie-authenticated writes | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 22 · 2026-11-10 | [Day 22](contracts/day-22.md) — Add safe request correlation | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 23 · 2026-11-11 | [Day 23](contracts/day-23.md) — Expose minimal operational health | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 24 · 2026-11-12 | [Day 24](contracts/day-24.md) — Explain and improve one real query | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 25 · 2026-11-13 | [Day 25](contracts/day-25.md) — Handle concurrent repeated selection | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 26 · 2026-11-14 | [Day 26](contracts/day-26.md) — Record minimal selection audit | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 27 · 2026-11-15 | [Day 27](contracts/day-27.md) — Fix one evidence-backed regression | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 28 · 2026-11-16 | [Day 28](contracts/day-28.md) — Milestone 2 browser-to-database acceptance | actual-attempt queue; tiered | partial / not_attempted / blocked |
| 29 · 2026-11-17 | [Day 29](contracts/day-29.md) — Make the slice reproducible | actual-attempt queue; tiered | installed / not_attempted / blocked |
| 30 · 2026-11-18 | [Day 30](contracts/day-30.md) — Defend and assess the slice | actual-attempt queue; tiered | installed / not_attempted / blocked |

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

Use LeetCode Premium statements and editorial explanations for guided first encounters; no separate DSA course is part of this plan. The initial guided sequence is LC121 on study Day1, LC1 on Day2, and LC383 on Day3. Their historical canonical anchor identities/days remain unchanged even where the planned encounter day moves. Day1 retains the historical three screenshots and seven assisted practice sessions, zero independent-solve credit, and no confirmed dated actual-attempt event.

The queue derives D+3/D+7/D+21 only from the earliest actual first-attempt date. Editorial or hint assistance is recorded as guided, not independent transfer. A fresh similar-pattern transfer must be attempted before its editorial, satisfy prerequisites, and be checked against prior exposure; unknown freshness stays blocked. Transfers do not create new anchor clocks. Failed/overdue retrieval displaces new guidance, and overflow retains its original due date, including D+21.

DSA time is 60 minutes for one or two substantive exercises, 75 for three, and 90 for four. Do not add problems to fill time or schedule more than four. Engineering remains 120 minutes (25 study, 85 practice/test, 10 evidence), so daily totals are 180/195/210. New guided introductions stop after roadmap Day150; Days151–154 are revision/transfer/repair only. The current repository contains 31 canonical anchors; that is inventory, not an agreed final curriculum limit. Conditional counts are not attempts or mastery. Read the [shared DSA policy](dsa/guided-transfer-policy.md), [canonical registry](dsa/canonical-anchors.json), and [verified seed catalog](dsa/guided-transfer-catalog-2026-10-21.json).
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

## Continued roadmap

The isolated [Days 99–112 contracts](continuation-99-112/README.md) add event-log-backed durable-work materialization and a manual one-shot local command. These are contracts and learner TODO scaffolding; they do not indicate completion.

[Days 113–126](continuation-113-126/README.md) add explicit local recovery inspection, quarantine, release, and one-key retry. [Days 127–140](continuation-127-140/README.md) extend the local work with a real PostgreSQL 18.6 logical archive restore into a fresh disposable database, full-state verification, and a loopback-only cutover rehearsal. The AWS course block is an offline planning/permission gate only; it creates no AWS resources or access grants.
