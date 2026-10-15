# Amazon 30-day interview sprint

A focused practice repository for the two Amazon roles you selected. The new branch contains only this interview-preparation project; CoverStory work and its branch remain separate.

- **Dates:** Friday 16 October through Saturday 14 November 2026, inclusive.
- **Every day:** one 60-minute SDE II assignment (job **10579529**) and one 60-minute FBA Front-End Engineer II assignment (job **10575321**). That is 120 minutes daily, below the 150-minute ceiling; the remaining 30 minutes are intentionally unallocated.
- **Resume CoverStory/Scrum:** Sunday 15 November 2026.
- **Assignment source of truth:** [`SPRINT-MANIFEST.json`](SPRINT-MANIFEST.json); detailed role tracks at [`assignments/sde.md`](assignments/sde.md) and [`assignments/frontend.md`](assignments/frontend.md).

## Start

```bash
python3 scripts/validate_sprint.py
python3 -m unittest sde.test_assignments -v  # expected to fail at 30 learner TODOs
cd frontend
npm ci
npm test                                   # expected to fail at 30 learner TODOs
npm run build                              # setup/build check, expected to pass
npm run dev                                # optional synthetic interface preview
```

The learner suites currently have one test per assignment and intentionally stop at `NotImplementedError`/`Learner TODO`. Those are the unimplemented practice tasks, not setup failures. `validate_sprint.py` and the frontend production build check the starter infrastructure without claiming learner completion. Do not mark assignments complete unless you solve and review them yourself.

## Daily format

For each role: 35 minutes solve/implement and test, 15 minutes role-specific system design or code review, 10 minutes truthful STAR/Leadership Principle practice. The two role assignments are distinct and sum to exactly 120 minutes each day. The exact prompts, synthetic fixtures, reference URLs, rationale, and source limitations are documented in `docs/role-research.md`.

This is a practice plan, not an Amazon interview format or hiring prediction. It does not promise repeated interview questions, job selection, or a particular assessment outcome. Past interview discussions are anecdotal study prompts; practice the underlying patterns with fresh variations.

`progress-template.csv` has one row per role assignment, all `NOT_STARTED`. It records no prior completion.
