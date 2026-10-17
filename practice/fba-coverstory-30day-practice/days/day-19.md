# Day 19 · 2026-11-04 · CAP-620 / CS90-19

## Session contract

- **FBA role:** Front-End Engineer II — FBA, job **10575321**; source assignment 19. **90 minutes**: 35 implementation, 15 design/review, 10 truthful STAR evidence, 30 focused component and adversarial tests.
- **CoverStory practical:** Implement owner-scoped UUID journey lookup and resume response. (saved primary CAP-54); **90 minutes**: 55 repository implementation/tests, 30 AWS official-pathway lesson/objective, 5 design/evidence.
- **Combined planned time:** 180 minutes (90 FBA + 90 CoverStory). Use checked-in synthetic fixtures only; do not enter real consumer or health data.

## FBA implementation

**Typed payload boundary** — `API decoding / rendering`. Read the full task in `practice/fba-coverstory-30day-practice/fba/assignments/frontend.md` under Day 19. Edit `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.jsx` and `practice/fba-coverstory-30day-practice/fba/frontend/src/assignments.js`; use `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json`.

```sh
cd practice/fba-coverstory-30day-practice/fba
make test-frontend-day DAY=19
```

Keep the supplied contract cases. **Additional 30-minute FBA deliverable:** add a focused component test in `practice/fba-coverstory-30day-practice/fba/frontend/src/ui.test.jsx` and an adversarial synthetic vector in `practice/fba-coverstory-30day-practice/fba/fixtures/cases.json` for this day's behavior: implement owner-scoped uuid journey lookup and resume response. Test keyboard interaction and visible failure/empty feedback where relevant, then run `cd practice/fba-coverstory-30day-practice/fba/frontend && npm test` and `cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=19`. Component behavior belongs in `ui.jsx`; the bounded helper belongs in `assignments.js`.

## CoverStory practical contract

Saved source: [`day-010-cap-54`](../../../docs/assignments/coverstory/saved-days/day-010-cap-54.md). Daily card CAP-620 maps to this specific task: **Implement owner-scoped UUID journey lookup and resume response.**.

**Learner seam:** `src/main/java/coverstory/modules/application/api/CAP54Seam.java` (`resume`). **Focused learner acceptance:** `src/test/java/coverstory/modules/application/CAP54AcceptanceTest.java`. **Instructor source:** `src/test/java/coverstory/modules/application/CAP54InstructorInfrastructureTest.java`. Additional acceptance focus: UUID is an identifier only; derive owner from trusted principal and return authoritative revision/step.

Do the 55-minute repository work against the actual existing learner seam above. If a source card's historical planned path differs from the checkout, use this verified checkout path and record the discrepancy; do not create a parallel fake implementation.

Setup/instructor check (separate from learner acceptance):

```sh
./mvnw -Dtest=CAP54InstructorInfrastructureTest test
```

Run focused learner acceptance separately (it may fail at the intentional TODO):

```sh
./mvnw -Plearner-tests -Dtest=CAP54AcceptanceTest test
```

An instructor pass proves only harness readiness. Do not claim learner completion from fixture/oracle/browser adapter checks.

## AWS pathway and evidence

Use the exact official AWS lesson/objective embedded in the CAP record in [`coverstory-final-saved-contracts-594-days.json`](../../../docs/source-inputs/2026-10-14/curriculum/coverstory-final-saved-contracts-594-days.json) for 30 minutes. No AWS account, credential, resource, payment, or paid service is created here. If the lesson link is unavailable, record the missing link and intended objective instead of claiming course progress.

Use the final 5 minutes for actual test results, one architecture/design decision, elapsed time, and a short truthful STAR note. Label AI-assisted work. Keep completion status unchanged until you have personally implemented and reviewed the work.
