# Day 13 — 2026-11-01: Add honest repeatable CI

**State:** Evidence-based readiness seam and fixed tests are installed. The repository still has no CI workflow, no learner attempt is recorded, and the focused suite is expected to fail at its TODO.

## Learner seam

`CiReadinessPolicy` for the evidence-checking part of CI readiness. The actual CI workflow/build configuration remains learner work; do not mark it complete from the readiness model alone.

## Acceptance invariants

Java 21 compile, infrastructure tests and explicitly selected verified contracts run from clean checkout; failures propagate nonzero; unfinished contracts remain pending, not green. No deploy credentials.

## Required edge cases

Clean-checkout commands; deliberate test failure proves nonzero propagation; manifest not calendar-derived.

## Prerequisite and isolation

Days 1–12 status manifest and fixture smoke. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

**System-design prompt (within the 25-minute concept block):** Show which evidence inputs make readiness blocked or ready; calendar date is not an input. See the [system design practice guide](../system-design-practice.md) and use only the applicable blank template.

## Concept (25 minutes)

Test pyramid, reproducibility, CI failure signals [I].

## Evidence (10 minutes)

Actual local/CI-equivalent output; do not claim remote run if absent. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day13ContractTest test`

This selector runs the installed readiness-policy acceptance tests and currently fails at the explicit TODO. It does not claim a workflow exists or a remote CI run occurred; local evidence still shows the Day 2–7 learner suites fail at TODOs. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

**New pattern anchor:** LeetCode 167 Two Sum II - Input Array Is Sorted — sorted two pointers. Learn the pattern for 25 minutes, solve independently for 20 minutes, and spend 15 minutes on same-day D recall plus the error log. Record the actual first-attempt date; schedule D+3, D+7 and D+21 reviews from that date. Planned dates in the manifest are forecasts only. A failed prerequisite/review displaces the next new anchor; do not add overtime. Use [`../dsa-error-log-template.md`](../dsa-error-log-template.md).

## Interview transfer (inside the current evidence block)

**Question:** How can a green build lie? What proves learner logic rather than fixture wiring?

**Observable evidence:** Distinguish infrastructure, isolated contract and integrated tests; show that an intentional contract failure exits nonzero and unfinished work stays visible.

**Assistance and gap:** Record unaided, hinted, or AI-assisted work honestly. For the change/debug prompt, work without AI. Score the relevant rubric dimensions 0–2 and name one bounded repair if a gap remains; do not treat tests as explanation credit.

**Related source:** [Microsoft official technical-interview guidance; curriculum inference](../interview-evidence-overlay.md#official-expectations). These sources motivate practice only; they do not establish question frequency or predict an interview.
