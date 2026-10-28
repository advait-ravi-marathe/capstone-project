# Learning day 14

**Status: Not started.** A planned learning-day number advances only when this session is attempted; missed dates do not silently complete a day.

## Fixed daily budget

- DSA: 60 minutes — use the corresponding current item in the separate DSA plan; no problem statements or solutions are copied here.
- Role / certification bank: 30 minutes — Short unfamiliar-code rehearsal.
- AI engineering: 90 minutes total, including any listed visibility work.
- Total: 180 minutes. If DSA takes 75/90 minutes, AI reduces to 75/60 minutes; carry unfinished AI work forward rather than compressing it.

## Role interview prompt (30 minutes)

Read the supplied request trace without running the feature. Identify the first incorrect assumption, the smallest diagnostic experiment and one result that would falsify your diagnosis.

Output: Use 20 min diagnosis + 10 min review; log root cause and missing knowledge.

Score using the [common rubric](../role-bank.md). Do not claim an interview pass, real customer feedback, or a result that did not occur.

## AI engineering work (90 minutes)

Use 30 minutes to finish the malformed-output task assigned to U010. Start U011 for 30 minutes only: inspect the ten synthetic training records and manifest, review the assistant-supplied metadata table, then implement the first insert plus identical re-ingest check for one record. Do not claim all ten have been ingested or U011 complete; its remaining segments continue on a later actual learning day. The final 30 minutes are the scheduled private evidence note.

### Scheduled dependency-unit segments

| Unit | Segment | Minutes | Contract |
|---|---|---:|---|
| AIFDE-V2-U010 — Structured output without trusting it | support | 30 | [unit contract](../units/u010.md) |
| AIFDE-V2-U011 — Versioned small corpus and SQL metadata | primary | 30 | [unit contract](../units/u011.md) |

**Visibility/re-estimation:** 30 minutes included in the AI total. Use the output described in the accepted contract; do not add another time block.

### Learning references

- [Python Tutorial](https://docs.python.org/3/tutorial/)
- [Python unittest](https://docs.python.org/3/library/unittest.html)
- [SQLite UPSERT](https://www.sqlite.org/lang_UPSERT.html)
- [SQLite CREATE TABLE](https://www.sqlite.org/lang_createtable.html)

### Acceptance and evidence

Run only the acceptance selector named in this day's unit contract after its assigned segment is complete. Tests for future units are deliberately kept out of the daily command. Keep actual model/runtime traces separate from deterministic mocked tests.

Record: command, exit status, changed learner-owned files, relevant output, model/runtime identity where applicable, and unresolved blockers. A mock-only result, reference witness, scaffold output or model-generated artifact is not learner completion.
