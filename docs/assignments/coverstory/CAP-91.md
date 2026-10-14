# CAP-91 — Adapt read-only onboarding assistance

**Saved day:** 053 · 5 December 2026 · **Maximum allocation:** 150 minutes

## Purpose

Add a provider-independent Java boundary for optional source-grounded onboarding explanations. The fake port is the only source of model responses in this scaffold. This does not establish an external model integration or production assistance pipeline.

## Files

- Learner seam: `src/main/java/coverstory/application/AdaptOnboardingAssistance.java`
- Learner acceptance cases: `src/test/java/coverstory/application/CAP91AcceptanceTest.java`
- Instructor oracle: `src/test/java/coverstory/application/CAP91InstructorInfrastructureTest.java`
- Synthetic contract fixture: `fixtures/coverstory/CAP-91/synthetic-v1.json`
- Evidence: `docs/evidence/coverstory/CAP-91/`

Implement only `suggest`. It receives an authorized question, a bounded list of permitted evidence chunks and the existing manual draft. Call `OnboardingAssistPort` once. Preserve citation source/version/span and usage values exactly on valid success. A timeout returns `TIMEOUT`; missing or invalid citations, answer, or usage return `MALFORMED` with no answer. Keep the manual draft unchanged in every result. A different suggested PIN is an unconfirmed suggestion that requires human review. This seam has no application mutation, confirmation, requote, or submission port.

## Run checks

Instructor setup and oracle checks:

```sh
mvn -Dtest=CAP91InstructorInfrastructureTest test
```

Learner contract, separately (expected to stop at the explicit TODO until implemented):

```sh
mvn -Dtest=CAP91AcceptanceTest test
```

Passing instructor checks validate only the test fixture and independent reference. They do not establish a production pipeline or learner mastery.

## Acceptance map

| Case | Expected behavior |
|---|---|
| CAP-91-CS-01 | One fake port call; exact citation and usage preserved; manual draft unchanged; zero writes |
| CAP-91-CS-02 | Timeout maps to `TIMEOUT`; manual entry remains available; zero writes |
| CAP-91-CS-03 | Missing citation array maps to `MALFORMED`; no answer or write |
| CAP-91-CS-04 | Different PIN is shown as unconfirmed; no overwrite, confirmation, requote, or write |

## Design explanation

The boundary is `OnboardingAssistPort → unconfirmed suggestion → human review`. A typed response proves only that the fake port returned structured data. It cannot authorize changing the user's draft. Keep the manual draft authoritative until a separate Application command receives explicit user confirmation.
