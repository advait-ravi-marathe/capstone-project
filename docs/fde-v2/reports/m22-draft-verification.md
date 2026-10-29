# M2.2 conditional draft verification

**Status: deterministic scaffolding only; M2.2 is not active.** This report covers the proposed `AIFDE-V2-M2.2` teaching detail and mechanics. It does not pass M2.1/P1, establish actual identity authentication, configure a real identity service, prove AWS deployment, or advance learner status. All M2.2 learner functions remain explicit `NotImplementedError` TODOs.

## Scope

- `docs/fde-v2/proposals/m22-detailed-days-62-82.md` proposes teaching/tasks for the accepted M2.2 outcome contract; no accepted contract or active daily page was edited.
- `ai/fde_v2/m22/draft_schedule.json` assigns relative Days62–82 while preserving 1,500 substantive, 240 buffer, 90 visibility, 30 M2.1 boundary buffer and 30 outside-M2.2 unallocated minutes. Stage totals and per-day 180-minute accounting are checked in tests.
- `ai/fde_v2/m22/learner.py` holds unsolved seams; `reference.py` is explicit instructor-only policy mechanics. The injected verified-token callback tests adapter claim handling only and is not cryptographic identity evidence.
- `ai/fde_v2/learner_acceptance/test_m22_units.py` is the selected learner acceptance. Its default learner candidate must fail at TODOs. Explicit reference substitution is used only for deterministic mechanics witnesses.

## Independent review fixes

The read-only reviewer rechecked the current worktree and cleared the three findings against the draft: retrieval now requires an authoritative active version and active chunk ID as well as read permission; audit metadata must match a strict pseudonym, action/outcome vocabulary, trace-ID format and timezone-bearing ISO timestamp; and Day62 cannot start M2.2 S01 until learner-evidenced M2.1 clearance, otherwise the whole relative schedule is deferred and reflowed. Regression fixtures include a stale same-tenant hit before the current one, a repeated current hit, foreign/deleted/missing hits and secret-canary values in each allowlisted field. The learner selector invokes the configured candidate directly; only the explicit reference command substitutes instructor code. The reviewer found no remaining issue in this bounded draft.

## Deterministic commands

Run after the repository's current pinned Python dependencies are installed:

```sh
python -m unittest scripts.test_fde_v2_m22_draft ai.fde_v2.tests.test_m22_draft -v
M22_CANDIDATE=ai.fde_v2.m22.reference python -m unittest ai.fde_v2.learner_acceptance.test_m22_units -v
python -m ai.fde_v2.m22.acceptance --milestone AIFDE-V2-M2.2 --mode learner
python -m ai.fde_v2.m22.acceptance --milestone AIFDE-V2-M2.2 --mode integration
python -m ai.fde_v2.m22.acceptance --milestone AIFDE-V2-M2.2 --mode evidence
```


The first command checks contract status, local documentation links, fixed schedule math, access decisions, retrieval post-validation, audit-field validation, deadline/retry limits, evidence-gate fail-closed behavior and TODO presence. On the reviewed worktree it passed **13 tests**. The second explicitly runs the acceptance suite against the instructor reference; it passed **6 tests**. The default learner selector ran and failed at all **6 explicit learner TODOs**, as expected. Integration/evidence modes returned `BLOCKED`/exit 2; there is no actual local identity exchange or M2.1 learner evidence. Synthetic callbacks, identities and records do not count as those gates.

Aggregate local verification for this worktree: `scripts/fde_v2_clean_check.sh` passed (40 governance tests, 70 selected AI/instructor checks, 6 M2.2 reference acceptance selectors and the two focused Vitest files with 11 tests each); Python script discovery passed **229 tests**; frontend production build passed; full frontend suite passed **24 Vitest tests plus 1 Node test**; `git diff --check` passed. The exact continuation workflow run **40811860501** on `acfcae2b84d5740112e085807ea026b4316a48d4` passed all 33 steps after fixing (a) Sprint 1 dependencies installed too late for aggregate Python discovery, and (b) the P1 container smoke not retrying startup `ConnectionResetError`. The latter was locally confirmed as a container-build DNS blocker (`pypi.org` name resolution failed), so the exact-head runner supplied the actual container validation. That successful run predates these M2.2 draft files; new exact-head CI is required after publication.

## Still blocked / not run

No real verified-token implementation, protected route, retrieval-permission integration, secret scanner, clean container build, actual trace sample, AWS C0 gap closure, cloud resource or deployment is claimed. The workflow step added for this draft and its exact-head result remain pending. Prior CI's local container smoke failure and its recovery are tracked separately from M2.2. No model access, weight download, credential, account or cloud request was made for this work.
