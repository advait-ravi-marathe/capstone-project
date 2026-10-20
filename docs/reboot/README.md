# CoverStory 30-day reboot contracts

This folder records the reviewed roadmap proposal for 20 October–18 November
2026. It is a set of contracts and schedule metadata, not a completion report.
The existing Day 1 feature remains the live starter; future-day files specify
the intended acceptance contract before their instructor tests or learner
implementations are created.

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

The chronological manifest holds each date, prerequisite note, DSA problem,
and separate contract/scaffold/learner/integration statuses:
[manifest.json](manifest.json). Every day has a dated page under
[`contracts/`](contracts/). Day 1's active runnable guide and fixed tests stay
at [docs/day-1.md](../day-1.md); the Day 1 contract page points there.

## Chronological index

| Day/date | Engineering contract | DSA block | State |
|---|---|---|---|
| 01 · 2026-10-20 | [Day 01](contracts/day-01.md) — Quote selection identity/order | 121 Best Time to Buy and Sell Stock (not_attempted) | installed / not_attempted / blocked |
| 02 · 2026-10-21 | [Day 02](contracts/day-02.md) — Validate selection request at HTTP boundary | 88 Merge Sorted Array (not_attempted) | not_installed / not_attempted / blocked |
| 03 · 2026-10-22 | [Day 03](contracts/day-03.md) — Map failures consistently | 27 Remove Element (not_attempted) | not_installed / not_attempted / blocked |
| 04 · 2026-10-23 | [Day 04](contracts/day-04.md) — Read one synthetic product | 26 Remove Duplicates from Sorted Array (not_attempted) | not_installed / not_attempted / blocked |
| 05 · 2026-10-24 | [Day 05](contracts/day-05.md) — Decide HomeSecure eligibility | 80 Remove Duplicates from Sorted Array II (not_attempted) | not_installed / not_attempted / blocked |
| 06 · 2026-10-25 | [Day 06](contracts/day-06.md) — Calculate synthetic premium | 169 Majority Element (not_attempted) | not_installed / not_attempted / blocked |
| 07 · 2026-10-26 | [Day 07](contracts/day-07.md) — Create and expire quote version | 189 Rotate Array (not_attempted) | not_installed / not_attempted / blocked |
| 08 · 2026-10-27 | [Day 08](contracts/day-08.md) — Define PostgreSQL quote schema | 122 Best Time to Buy and Sell Stock II (not_attempted) | not_installed / not_attempted / blocked |
| 09 · 2026-10-28 | [Day 09](contracts/day-09.md) — Persist/read immutable quote snapshots | 55 Jump Game (not_attempted) | not_installed / not_attempted / blocked |
| 10 · 2026-10-29 | [Day 10](contracts/day-10.md) — Persist selection batch atomically | 45 Jump Game II (not_attempted) | not_installed / not_attempted / blocked |
| 11 · 2026-10-30 | [Day 11](contracts/day-11.md) — Reject stale quote selection | 13 Roman to Integer (not_attempted) | not_installed / not_attempted / blocked |
| 12 · 2026-10-31 | [Day 12](contracts/day-12.md) — Enforce owner-scoped access | 14 Longest Common Prefix (not_attempted) | not_installed / not_attempted / blocked |
| 13 · 2026-11-01 | [Day 13](contracts/day-13.md) — Add honest repeatable CI | 58 Length of Last Word (not_attempted) | not_installed / not_attempted / blocked |
| 14 · 2026-11-02 | [Day 14](contracts/day-14.md) — Milestone 1 backend vertical slice | 28 Find the Index of the First Occurrence in a String (not_attempted) | not_installed / not_attempted / blocked |
| 15 · 2026-11-03 | [Day 15](contracts/day-15.md) — Read journey summary | 151 Reverse Words in a String (not_attempted) | not_installed / not_attempted / blocked |
| 16 · 2026-11-04 | [Day 16](contracts/day-16.md) — Bound quote-history reads | 6 Zigzag Conversion (not_attempted) | not_installed / not_attempted / blocked |
| 17 · 2026-11-05 | [Day 17](contracts/day-17.md) — Render product choices in React/TypeScript | 125 Valid Palindrome (not_attempted) | not_installed / not_attempted / blocked |
| 18 · 2026-11-06 | [Day 18](contracts/day-18.md) — Capture quote input | 392 Is Subsequence (not_attempted) | not_installed / not_attempted / blocked |
| 19 · 2026-11-07 | [Day 19](contracts/day-19.md) — Display quote snapshot | 167 Two Sum II - Input Array Is Sorted (not_attempted) | not_installed / not_attempted / blocked |
| 20 · 2026-11-08 | [Day 20](contracts/day-20.md) — Connect selection action UI | 11 Container With Most Water (not_attempted) | not_installed / not_attempted / blocked |
| 21 · 2026-11-09 | [Day 21](contracts/day-21.md) — Protect cookie-authenticated writes | 15 3Sum (not_attempted) | not_installed / not_attempted / blocked |
| 22 · 2026-11-10 | [Day 22](contracts/day-22.md) — Add safe request correlation | 209 Minimum Size Subarray Sum (not_attempted) | not_installed / not_attempted / blocked |
| 23 · 2026-11-11 | [Day 23](contracts/day-23.md) — Expose minimal operational health | 3 Longest Substring Without Repeating Characters (not_attempted) | not_installed / not_attempted / blocked |
| 24 · 2026-11-12 | [Day 24](contracts/day-24.md) — Explain and improve one real query | 383 Ransom Note (not_attempted) | not_installed / not_attempted / blocked |
| 25 · 2026-11-13 | [Day 25](contracts/day-25.md) — Handle concurrent repeated selection | 205 Isomorphic Strings (not_attempted) | not_installed / not_attempted / blocked |
| 26 · 2026-11-14 | [Day 26](contracts/day-26.md) — Record minimal selection audit | 290 Word Pattern (not_attempted) | not_installed / not_attempted / blocked |
| 27 · 2026-11-15 | [Day 27](contracts/day-27.md) — Fix one evidence-backed regression | 242 Valid Anagram (not_attempted) | not_installed / not_attempted / blocked |
| 28 · 2026-11-16 | [Day 28](contracts/day-28.md) — Milestone 2 browser-to-database acceptance | 1 Two Sum (not_attempted) | not_installed / not_attempted / blocked |
| 29 · 2026-11-17 | [Day 29](contracts/day-29.md) — Make the slice reproducible | 49 Group Anagrams (not_attempted) | not_installed / not_attempted / blocked |
| 30 · 2026-11-18 | [Day 30](contracts/day-30.md) — Defend and assess the slice | 202 Happy Number (not_attempted) | not_installed / not_attempted / blocked |

## Scaffold and dependency rules

Future contract pages are not tests and do not claim those commands currently
run. Their exact Maven test selector becomes runnable when the instructor adds
that day's named fixed test. Every such test must instantiate only its own
subject; prior collaborators use explicit test-only canned values. A stub may
provide fixed fixture data, but must not implement an earlier learner rule.
Future TODOs must fail clearly. Never use a skipped/disabled test, catch-all
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

The dated LeetCode sequence is in the manifest. Day 1 remains LeetCode 121 and
is Advait's first independent attempt; prior completion is zero. Preserve
previously verified solve history and do not infer mastery from scaffolding.
Every day includes error-log/revisit time. The official [Top Interview 150
study plan](https://leetcode.com/studyplan/top-interview-150/) is the source
sequence; each attempt is independent unless the learner records otherwise.

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
