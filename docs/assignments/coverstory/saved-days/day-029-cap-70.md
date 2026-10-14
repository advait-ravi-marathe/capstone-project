# Day 029 · 11 Nov 2026 — Stage protected consumer payload envelopes (150m)

**Date:** 2026-11-11 · **Phase:** 1 · **Primary ID:** `CAP-70` · **Daily minutes:** 150 maximum
**Saved contract source:** [`coverstory-final-saved-contracts-594-days.json`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) · description SHA-256 `4a6dd9d3c313f0b3b6668ddc0aa06ebb5878407f1d23366f74741a4cec8fb675`

This guide carries the saved card contract as source evidence. It does not claim that its planned paths exist, the instructor harness is ready, the timebox was rehearsed, or learner work passed. A linked component shares this day and adds zero minutes.

## Practical task from the saved contract

Retain immutable staging, byte integrity and safe quarantine while protected consumer content remains encrypted.

## Explicit repository paths in the saved contract

- `ai/coverstory/cap_70.py` (planned in source; existence/readiness is not implied)
- `ai/coverstory/tests/test_cap_70.py` (planned in source; existence/readiness is not implied)
- `fixtures/coverstory/CAP-70/synthetic-v1.json` (planned in source; existence/readiness is not implied)
- `docs/assignments/coverstory/CAP-70.md` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-70/harness-self-check.json` (planned in source; existence/readiness is not implied)
- `docs/evidence/coverstory/CAP-70/result.json` (planned in source; existence/readiness is not implied)

## Task-specific source excerpts

The task-specific excerpts below are transcribed from the saved card. The byte-exact complete description remains in the linked authoritative JSON. Acceptance cases and planned paths are specifications, not passing evidence.

### `CAP-70` · primary · Day 029 · 11 Nov 2026 — Stage protected consumer payload envelopes (150m)

Description SHA-256: `4a6dd9d3c313f0b3b6668ddc0aa06ebb5878407f1d23366f74741a4cec8fb675`

**Practical task:** Retain immutable staging, byte integrity and safe quarantine while protected consumer content remains encrypted.

Exact source description: [saved curriculum record `CAP-70`](../../../source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json)

#### Existing calendar and timebox

One existing 228-day CAP calendar, 14 October 2026–29 May 2027; this draft changes only Day001–076. Preserve IDs, active dates, roles, board membership, native links, original history and statuses. Each primary150 scheduled minutes; components0 additional minutes. ECOS twelve 15-day windows are source history, not a second schedule.
Original Java/DSA engineering outcomes remain inside the practical seam. Canonical Top Interview 150 order/history and due reviews remain unchanged, with the separate 15-minute minimum retention slot. Do not mark attempted as solved. Ktor is optional same-budget substitution only. AWS lesson/objective/practice/pathway prefix is preserved byte-for-byte; no extra course, account, enrollment or spending is authorized.
For a component, all numbers except scheduled_minutes describe the owning primary envelope and are not additional allocations. Sunday105 practical consists of95 task+10 existing individual evidence, no extra reflection block. Weekday95+30+15+10=150. Sunday95+10+30+15=150.

#### Instructor scaffold and learner TODO

Instructor supplies compatible Java21/Maven/JUnit/PostgreSQL or Python runner, typed DTOs/ports, schema/migrations, deterministic fixtures and failing TODO tests; no learner infrastructure work. For UI targets, supplies the thin React shell from the existing 87 designs, styles, routing, controls and test adapters. Only the declared seam is learner-owned.
Reuse earlier reviewed artifacts through public module interfaces. All new namespace/path/schema mappings are planned and require code-owner reconciliation; do not overwrite prior generic teaching fixtures.
Replace the old event-domain deliverable with this one bounded seam. If combined tests cannot fit95m, mark OVER_CAPACITY and explicitly reduce optional breadth before assigning; do not defer required failure tests or add evening work.
Planned paths (not existing implementation):
- ai/coverstory/cap_70.py
- ai/coverstory/tests/test_cap_70.py
- fixtures/coverstory/CAP-70/synthetic-v1.json
- docs/assignments/coverstory/CAP-70.md
- docs/evidence/coverstory/CAP-70/harness-self-check.json
- docs/evidence/coverstory/CAP-70/result.json

stage_envelopes(batchId, lines, protectedEnvelopeSink)->StageReport; instructor supplies the maintained AEAD adapter and validated SYNTHETIC envelope fixtures.

#### Concrete inputs and outputs

Stage only protected_payload_envelope records bound to owned immutable object IDs/revisions. Preserve ciphertext_with_tag, nonce, wrapped_dek, kek_reference and encryption version exactly; keep fixed-vector bytes separate from generated-nonce tests. The learner stages already encrypted values and records line/provenance and a ciphertext integrity hash in local evidence; no cipher implementation or plaintext database column. Malformed/unknown fields produce safe line-number/code diagnostics, never raw sensitive lines. Empty physical lines are skipped but source line numbering remains. ApplicationRevision owns the encrypted payload reference; journey links the revision without duplicating answers.

#### Acceptance cases · expected, NOT_RUN

- CAP-70-CS-01: Input: Two valid SYNTHETIC envelope lines with fixed ciphertext bytes and object bindings.. Expected: Two stored envelope records retain exact ciphertext/nonce/wrapped-key values and provenance; no plaintext consumer marker in persisted rows or logs..
- CAP-70-CS-02: Input: Blank line then valid envelope on line 2.. Expected: Only line 2 stages; provenance remains 2..
- CAP-70-CS-03: Input: Malformed envelope or extra plaintext email/healthAnswer field.. Expected: Safe rejection with line and reason; no raw payload in quarantine/logs; no plaintext fallback..
- CAP-70-CS-04: Input: Wrong authenticated owner, altered tag/AAD, unavailable key, or unsupported encryption version.. Expected: Owner denied before decrypt; other invalid envelopes fail closed through supplied adapter; no partial plaintext or domain revision link..
- CAP-70-CS-05: Input: Two normal encryptions under one data key versus an explicit fixed test vector.. Expected: Normal nonces differ; deterministic nonce reuse is permitted only inside the named isolated test-vector fixture..

#### Architecture/interview evidence delta

At most 5 minutes inside the existing docs/test-evidence allocation; at most 3 changed facts and either 6 nodes or 10 sequence messages. Do not draw a new full system or add implementation work. Use the instructor-supplied Protected payload data-flow template and today’s existing test/receipt in docs/evidence/coverstory/CAP-70/result.json. Annotate the supplied owner authorization → validated envelope → immutable ciphertext staging → domain revision reference → safe evidence flow. Record only these changed facts: Ownership is checked before decrypt.; Object ID and revision bind the envelope through AAD.; Ciphertext/nonce bytes are preserved while malformed plaintext-bearing input is excluded..

Alternative: Store plaintext for debugging versus retain protected payload references and redacted line diagnostics. Interview question: Why does an in-process encryption round-trip not establish cross-process recovery, and what must happen when the approved teaching key is unavailable? Use at most 3 minutes of the existing reflection slot; answer from today’s same artifact, not a new research task.
