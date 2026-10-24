# Assistant-supplied architecture decisions: Days 197–210

Assistant-supplied decisions and reasons; learner implements/tests. The Days 153/154 entry gate remains unmet until its exact baseline artifact and provenance exist.

<a id="day-197"></a>
## Day 197 — Inspect actual Day 153/154 recovery inputs
Inspect the authoritative Day 153/154 records and run their checker. They show null source SHA, empty artifact hashes, and not attempted/not run. Record that the pinned baseline JAR and source/build recipe are absent; do not create replacement artifacts.

<a id="day-198"></a>
## Day 198 — Recover CoverStory readiness gate
Implement the existing Day 57 readiness contract. Missing, null, false, empty, or irrelevant required evidence must fail closed, and the map must remain unchanged.

<a id="day-199"></a>
## Day 199 — Recover bounded security checkpoint
Implement the Day 63 checkpoint. Accept only 0–85 minutes, require recovery when evidence is unmet, and return no added work.

<a id="day-200"></a>
## Day 200 — Recover offline AWS resource/access boundary
Implement Day 137 offline AWS plan validation. Reject public exposure, wildcard admin, secrets, cloud action requests and claims of cloud verification. No deployment or AWS calls occur.

<a id="day-201"></a>
## Day 201 — Recover offline rehearsal evidence gate
Implement Day 139 local rehearsal evidence validation. Complete offline evidence can pass its local gate; it must never be reported as cloud execution or verification.

<a id="day-202"></a>
## Day 202 — Inspect owner-scoped renewal read acceptance
Run the existing Day 75 owner-scoped read-store acceptance and capture its first failing assertion. Keep its TODO state and distinguish harness status from learner implementation.

<a id="day-203"></a>
## Day 203 — Rebuild and run packaged browser acceptance
Run scripts/package-sprint5-demo.sh to build the fresh Sprint 5 JAR and package receipt. Check target/capstone-starter-1.0-SNAPSHOT-sprint5.jar and target/sprint5-demo/package-evidence.json against current HEAD, source fingerprint, and artifact digest before the packaged browser acceptance. Packaging, preflight and browser test are budgeted within this day; stale artifacts are rejected.

<a id="day-204"></a>
## Day 204 — Freeze independent synthetic expected cases
Write independent expected synthetic statuses/citations into the fixed cases before evaluating them. The test expectations must not call the evaluator to derive themselves.

<a id="day-205"></a>
## Day 205 — Filter candidates by owner before matching
Filter the immutable synthetic clauses by owner before matching. This illustrates operation order only and is not a real authorization boundary.

<a id="day-206"></a>
## Day 206 — Return exact citation or abstain
Return exact clause/version text only for one unique match. For missing or ambiguous evidence, abstain; do not call a model or generate a policy decision.

<a id="day-207"></a>
## Day 207 — Review official certification guide facts
Review both current official AWS certification guides and create one learner-authored record per target, with exact title, published version (or null only if the page has no version), official URL, exact visible headings, focused retrieval answers, and assistance. Validate against both guide identities; no readiness, exam attempt, or passing is implied.

<a id="day-208"></a>
## Day 208 — Count offline evaluator outcomes
Run the local evaluator and compare total, cited, abstained, exact-match and citation-mismatch counts to independent literal expected cases. This synthetic fixture is a debugging signal, not product quality evidence.

<a id="day-209"></a>
## Day 209 — Rebuild package and recover browser acceptance
Build a new Sprint 5 package and receipt from the current source tree, preflight source/JAR digests, then run packaged browser acceptance. Also check Days 153/154 separately; package success cannot satisfy that evidence gate.

<a id="day-210"></a>
## Day 210 — Verify fresh-process canonical output
Run the fresh-process CLI test and compare canonical JSON output from separate invocations. This checks deterministic serialization only.

## Existing evidence boundary

Day 153/154 remain not_attempted/not_run, with null source SHA and empty artifact hashes. Preserve those records; do not fabricate artifacts or provenance.
