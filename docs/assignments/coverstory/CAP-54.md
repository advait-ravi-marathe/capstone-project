# CAP-54 — Resume an owned journey safely

**Learner outcome:** Enforce authenticated ownership for UUID journeys and keep private resume summaries minimal.

- Learner API seam: `src/main/java/coverstory/modules/application/api/CAP54Seam.java`.
- Synthetic owner/revision vectors: `fixtures/coverstory/CAP-54/synthetic-v1.json`.
- Learner acceptance suite: `src/test/java/coverstory/modules/application/CAP54AcceptanceTest.java`.
- Instructor fixture and access-oracle checks: `src/test/java/coverstory/modules/application/CAP54InstructorInfrastructureTest.java`.

Cases cover a new session resuming the owner's same journey from the authoritative application revision, indistinguishable safe `404` responses for foreign and absent UUIDs, zero application-state reads on denial, rejection of caller-supplied owner identity, ignoring browser-supplied current step, anonymous private-route denial, and an owner-bound cursor with page size capped at 50. The instructor setup now exercises actual Spring routes and an Application-owned JDBC reader over disposable PostgreSQL. A separate test-only adapter provides the correct reference response without implementing the learner seam.

All identifiers and payload markers are synthetic. The safe summary includes only journey UUID, authoritative status, and recomputed resume action. No decrypt operation is part of this interface. The repository query belongs to Application; callers in other modules must use the Application port.

`GET /api/session`, `GET /api/v1/me/journeys`, and `GET /api/v1/me/journeys/{journeyId}/resume` are wired. Session identity comes only from a trusted servlet principal; anonymous callers receive `401`. `JdbcCAP54ResumeReader` scopes journey lookup by owner and joins the exact linked application revision. Summaries contain only journey UUID, authoritative status, and recomputed resume action. Owner override is rejected before repository access; `current_step` is ignored. A 50-item page is accepted; excessive limit and foreign cursor are rejected.

The runtime witness uses disposable PostgreSQL and test-only synthetic session/HTTP adapters. Its Chromium flow reloads a fresh session, switches account, checks private cache clearing, logs out, and verifies that anonymous public catalog remains available. These adapters provide instructor verification only. No production login or identity-issuing adapter is installed; a trusted deployment adapter must populate the servlet principal. The two `CAP54Seam` methods remain learner TODOs and authenticated application calls return safe `501` until they are implemented.

```sh
mvn -Dtest=CAP54InstructorInfrastructureTest test
mvn -Dcoverstory.cap54.browser=true -Dtest=CAP54HttpRuntimeIntegrationTest test
cd web && npm run build && npm run test:security
mvn -Plearner-tests -Dtest=CAP54AcceptanceTest test
```

The instructor suite should pass. The learner suite currently stops at the TODO seams. Day 010 instructor scaffold readiness and its raw run/fixture evidence are in the [chronological readiness report](../../evidence/coverstory/phase1-days-001-010/README.md). Readiness establishes that the learner can start this bounded exercise; it does not establish production authorization/session behavior, learner completion, mastery, or measured duration.
