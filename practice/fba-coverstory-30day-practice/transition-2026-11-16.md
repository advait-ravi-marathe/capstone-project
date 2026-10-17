# Transition to full CoverStory cadence — 16 November 2026

The combined FBA + CoverStory practice window ends 15 November. Starting 16 November, schedule CoverStory at its saved full cadence of **180 minutes per day** (150 practical + 30 AWS pathway study); stop adding FBA role sessions to the daily calendar. Preserve all source contracts and original history. This is a forward-looking transition, not a retrospective completion record.

## First-15-unit review gate

Before retiring any of the first 15 units from future scheduling, review the learner’s own implementation, focused tests, explain-back, timebox, and evidence against that unit’s saved acceptance contract. The FBA-only progress template intentionally records `NOT_STARTED`; it must not be interpreted as completed work. For each CoverStory unit, record one of `PASS`, `FAIL`, `BLOCKED`, or `NOT_RUN`, identify AI assistance, attach the exact command/output and fixture digest, and keep readiness separate from completion/mastery. Units without adequate evidence remain active for remediation; remove only the future recurrence after the source contract is demonstrably covered. Never delete source code, cards, tests, history, or prior outcomes.

Review these first 15 source contracts in their original order:

1. CAP-40 — preserve shortlist identity and first-seen order; linked CAP-41 exact wording retrieval.
2. CAP-42 — classify sequential catalog import identity; linked CAP-43 catalog lookup.
3. CAP-44 — rank exact public wording; linked CAP-45 SQL/fixture checks.
4. CAP-46 — bound FIFO work queue; linked CAP-47 learning evidence.
5. CAP-48 — deduplicate source-version updates; linked CAP-49 matching evidence.
6. CAP-50 — validate exact import contract; linked CAP-51 SQL constraints.
7. CAP-165 — distinguish immutable identity from mutable state.
8. CAP-52 — reject unsafe catalog import transport.
9. CAP-53 — preserve deterministic retrieval provenance.
10. CAP-54 — resume only an owner-authorized journey revision.
11. CAP-55 — validate source metadata and version boundaries.
12. CAP-56 — control cancellation and bounded asynchronous work.
13. CAP-57 — publish catalog changes atomically with history.
14. CAP-166 — verify persisted public catalog behavior across restart.
15. CAP-58 — validate allowlisted analytics ingress without leaking sensitive data.

The exact task and acceptance language remain in `docs/assignments/coverstory/saved-days/day-001-cap-40.md` through `day-015-cap-58.md`; the related existing setup and acceptance tests remain in their source directories. This transition document does not mark any of them complete.

## Add 15 substantive units (proposed forward schedule)

Each row below is a scheduled 180-minute CoverStory work unit beginning 16 November. Use the saved day card as the acceptance source, run the named harness independently from the learner suite, and do practical repository work in the named subsystem. Any unit that is not ready because of a missing adapter remains conditional and is reported as such; do not replace work with generic “review” tasks.

| Date | Saved primary | Repository focus | Required practical output |
|---|---:|---|---|
| Nov 16 | [CAP-59](transition-days/day-01-cap-59.md) | `ai/coverstory/cap_59.py`, Support citation fixtures | Exact wording/version provenance, with synthetic counterexamples and a saved acceptance run |
| Nov 17 | [CAP-60](transition-days/day-02-cap-60.md) | `src/main/java/coverstory/modules/application/`, journey test schemas | Race-safe authoritative transition behavior and rollback/replay evidence; preserve the product seam TODO |
| Nov 18 | [CAP-61](transition-days/day-03-cap-61.md) | `ai/coverstory/cap_61.py` | Threshold-gated answer behavior with explicit unknown/insufficient-evidence cases |
| Nov 19 | [CAP-62](transition-days/day-04-cap-62.md) | `ai/coverstory/cap_62.py` | Cancellation/deadline behavior that distinguishes timeout, cancellation, and empty answer |
| Nov 20 | [CAP-63](transition-days/day-05-cap-63.md) | evidence for the first implementation slice | Run independent contract checks, explain the slice, and record gaps rather than claiming readiness |
| Nov 21 | [CAP-64](transition-days/day-06-cap-64.md) | catalog publication transport | Offline safe request/response contract tests, including malformed and conflict cases |
| Nov 22 | [CAP-65](transition-days/day-07-cap-65.md) | Support evidence filtering | Owner/assignment filtering before scoring; fixture-backed foreign-owner rejection |
| Nov 23 | [CAP-66](transition-days/day-08-cap-66.md) | synthetic comparison traces | Reproducible cohort comparisons from recorded synthetic traces with explicit denominator |
| Nov 24 | [CAP-67](transition-days/day-09-cap-67.md) | `ai/coverstory/cap_67.py` | Reproducible retrieval metrics and fixed fixture-based acceptance evidence |
| Nov 25 | [CAP-68](transition-days/day-10-cap-68.md) | Shortlist durability/recovery harness | Response-loss/replay and owner-isolation exercise using only disposable PostgreSQL fixtures |
| Nov 26 | [CAP-69](transition-days/day-11-cap-69.md) | `ai/coverstory/cap_69.py` | Measured bounded-queue rationale using recorded workload and deterministic checks |
| Nov 27 | [CAP-70](transition-days/day-12-cap-70.md) | synthetic protected-payload staging | Exact-byte/ciphertext contract work; no real PII, KMS provisioning, or production key claims |
| Nov 28 | [CAP-71](transition-days/day-13-cap-71.md) | `ai/coverstory/cap_71.py` | Chunking with stable source coordinates and boundary fixtures |
| Nov 29 | [CAP-72](transition-days/day-14-cap-72.md) | provider-observation test contracts | Atomic synthetic observation/audit transaction checks; no provider call |
| Nov 30 | [CAP-73](transition-days/day-15-cap-73.md) | `ai/coverstory/cap_73.py` | Leakage-safe train/validation split checks with saved synthetic fixture evidence |

These 15 units are additive scheduled work drawn from the already saved curriculum. They do not delete or rewrite any later source cards or historical scheduling records. Before each assignment, inspect the source guide and the current generated coverage matrix: a mapped card is not automatically runnable, and an instructor oracle is not a completed learner task. Keep external AWS course access separate; do not provision infrastructure or spend money.

## End-of-window review

On 30 November, compare planned time with actual focused work and rerun only the evidence needed to establish current status. Carry every unmet acceptance case, blocked dependency, and unresolved TODO forward. Do not claim AWS certification progress from study time alone, or learner mastery from AI-generated solutions or green instructor tests.
