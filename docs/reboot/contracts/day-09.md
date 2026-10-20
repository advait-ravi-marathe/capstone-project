# Day 09 — 2026-10-28: Persist/read immutable quote snapshots

**State:** Instructor scaffold installed; learner implementation pending. Tests use real PostgreSQL and a fixed test-only schema/seed that bypass Days 7–8.

## Learner seam

`QuoteRepositoryAdapter`. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Round trip preserves `NUMERIC(14,2)`, UTC timestamps and version ID; unknown `(journeyId, quoteVersionId)` returns explicit absence; reads require supplied journey scope; snapshots are insert-only through this adapter.

## Required edge cases

Round-trip precision, absent ID, wrong journey, update attempt.

## Prerequisite and isolation

Day 8; seeded rows, no quote-creation service. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**PostgreSQL design prompt:** Identify primary/composite keys, normalization, constraints, and transaction/isolation boundary relevant to this day's schema/write; do not add infrastructure beyond the contract. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

SQL queries, repository boundaries and ORM mapping [F].

## Evidence (10 minutes)

Disposable PostgreSQL test output. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day09ContractTest test`

This selector runs the installed adapter tests against a real embedded PostgreSQL server. The expected learner TODO failures are not a passing result. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 238 Product of Array Except Self — prefix/suffix products. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** What SQL does this repository operation need? What can ORM obscure?

**Observable evidence:** Explain a scoped read, decimal/time round trip and explicit absence; inspect emitted SQL if available rather than claiming ORM makes all queries efficient or safe.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [A5](../interview-evidence-overlay.md#a5) (anecdotal). These sources motivate practice only; they do not establish question frequency or predict an interview.
