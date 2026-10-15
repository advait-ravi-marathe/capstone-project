# CAP-54 — Resume an owned journey safely

**Learner outcome:** Enforce authenticated ownership for UUID journeys and keep private resume summaries minimal.

- Learner API seam: `src/main/java/coverstory/modules/application/api/CAP54Seam.java`.
- Synthetic owner/revision vectors: `fixtures/coverstory/CAP-54/synthetic-v1.json`.
- Learner acceptance suite: `src/test/java/coverstory/modules/application/CAP54AcceptanceTest.java`.
- Instructor fixture and access-oracle checks: `src/test/java/coverstory/modules/application/CAP54InstructorInfrastructureTest.java`.

Cases cover a new session resuming the owner's same journey from the authoritative application revision, indistinguishable safe `404` responses for foreign and absent UUIDs, zero application-state reads on denial, rejection of caller-supplied owner identity, ignoring browser-supplied current step, anonymous private-route denial, and an owner-bound cursor with page size capped at 50. The instructor fixture adapter implements the actual `OwnedJourneyReader` and `ApplicationStateReader` ports; it verifies the exact linked revision, safe summaries, owner scoping, and missing/foreign equivalence independently of the learner TODO.

All identifiers and payload markers are synthetic. The safe summary includes only journey UUID, authoritative status, and recomputed resume action. No decrypt operation is part of this interface. The repository query belongs to Application; callers in other modules must use the Application port.

The two learner methods remain explicit TODOs. The unauthenticated session endpoint returns `401`. The anonymous public catalog dependency now returns a bounded synthetic health response through the Catalog module public port and is covered by an actual application-context HTTP test. This supplies only public discovery data; it does not implement private resume, owner identity, persistence, or the authenticated application stack. The CAP-54 harness tests the typed Application module ports with fresh fixture adapters; it is not a production endpoint or authentication adapter.

```sh
mvn -Dtest=CAP54InstructorInfrastructureTest test
mvn -Plearner-tests -Dtest=CAP54AcceptanceTest test
```

The instructor suite should pass. The learner suite currently stops at the TODO seams. Day 010 instructor scaffold readiness and its raw run/fixture evidence are in the [chronological readiness report](../../evidence/coverstory/phase1-days-001-010/README.md). Readiness establishes that the learner can start this bounded exercise; it does not establish production authorization/session behavior, learner completion, mastery, or measured duration.
