# Engineering units 141–154 · calendar study days 155–168 · 2027-03-29–2027-04-11 — two-artifact release evidence on unchanged schema

Calendar mapping: stable engineering unit IDs 141–154 occupy study days 155–168 (2027-03-29 through 2027-04-11).

This scaffold plans a local, quiesced A→B→A compatibility rehearsal using distinct immutable JARs and the existing V001–V005 database. It adds only a read-only, versioned report path using existing recovery inspection/classification. It does not establish cloud/deployment readiness or learner completion.


Course reference: **Master Spring Boot 3 & Spring Framework 6 with Java**, course 4993276, in28Minutes Official. [Course page](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners?utm_source=chatgpt&utm_medium=llm-integration&utm_audience=all&utm_campaign=chatgpt_integration_phase1). Catalog metadata was verified 2026-10-20; access and playback are unconfirmed, video content unaudited, and lesson deep links were not returned. Exact lesson IDs and durations are in `manifest.json`.

A is frozen before B-only behavior, with actual digest and source/overlay provenance. B must prove its additional capability. A runnable baseline JAR is not distributed by this scaffold: use the learner's actual prior checkpoint. If it is unavailable, report the matrix blocked at baseline setup; never copy B under A's name. An instructor conformance run may build A from pinned prior source plus a recorded temporary prerequisite witness, then build B separately with the same prerequisite witness and new report witnesses. Record source/overlay identities and recipes, delete solved temporary JARs/overlays, and restore source checksums.

For instructor validation, create two JSON overlay manifests outside the repository. Each is an array of `{ "source": "/absolute/path/to/witness.java", "destination": "src/main/java/package/Witness.java", "sha256": "..." }` entries. Every overlay is temporary and its exact bytes are recorded. Then, from the repository root, run `scripts/prepare-release-artifacts.sh "$COVERSTORY_RELEASE_PAIR_DIR" "$CANDIDATE_SHA" "$A_OVERLAY_MANIFEST" "$B_OVERLAY_MANIFEST"`. The helper builds isolated detached worktrees from pinned baseline `105bbc9dd448460f9bd5f7e6b9c67eafa99a16c3` and the candidate commit, verifies overlay checksums, packages each JAR independently, checks that only B contains `ReleaseInspectionOnlyApplication`, and records JAR/source/overlay/migration hashes in `artifact-pair-manifest.json`. Keep the generated directory outside the checkout, remove solved witnesses after the run, and rebuild the restored starter. If truthful overlays or a runnable learner baseline are unavailable, report the package matrix blocked; do not label the same JAR twice.

The matrix selectors consume the helper's immutable `baseline.jar`, `candidate.jar`, and `artifact-pair-manifest.json`. They no longer accept free-form strings as provenance. The exact local matrix command is listed in Day 148, Day 149, and Day 153 contracts.

Before Day 146 packaged checks, build the ordinary candidate from the repository root with `./mvnw -Plocal-app -DskipTests package`; this creates `target/capstone-starter-1.0-SNAPSHOT.jar` and does not depend on Day 148 artifact-pair setup. The inspection context validates requests before constructing Spring, disables Flyway/SQL initialization, refuses absent or mismatched migration history, and verifies the exact public column names and formatted types for V001–V005. This bounded preflight does not certify every check constraint, foreign key, default, or index. The v1 contract is [report-v1.md](report-v1.md). Its framed report is evidence, not authority: compare complete database snapshots independently.

No AWS resources, credentials, grants, spending, deployment, schema changes, public endpoint or later-day work is authorized. Temporary witness success is instructor evidence only.

Each day uses 120 engineering minutes: 25 study, 85 practice/testing, and 10 evidence.

## Focused official references

These links support the existing engineering study block; they are not extra study time. Days 147 and 154 review earlier material only.

- Day 141: [Maven build lifecycle: package and verify](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html) — default lifecycle, package, verify.
- Day 142: [Spring Boot JSON support](https://docs.spring.io/spring-boot/reference/features/json.html) — JSON mapper auto-configuration.
- Day 142: [RFC 8259](https://www.rfc-editor.org/rfc/rfc8259) — 4 Objects, 6 Numbers, 7 Strings, 9 Parsers.
- Day 143: [Spring Boot JSON support](https://docs.spring.io/spring-boot/reference/features/json.html) — JSON mapper auto-configuration.
- Day 143: [RFC 8259](https://www.rfc-editor.org/rfc/rfc8259) — 4 Objects, 6 Numbers, 7 Strings, 9 Parsers.
- Day 144: [Spring Boot JSON support](https://docs.spring.io/spring-boot/reference/features/json.html) — JSON mapper auto-configuration.
- Day 144: [RFC 8259](https://www.rfc-editor.org/rfc/rfc8259) — 4 Objects, 6 Numbers, 7 Strings, 9 Parsers.
- Day 145: [Spring Boot application arguments](https://docs.spring.io/spring-boot/reference/features/spring-application.html) — ApplicationArguments.
- Day 146: [Spring Boot application arguments and runners](https://docs.spring.io/spring-boot/reference/features/spring-application.html) — ApplicationRunner, ApplicationArguments.
- Day 148: [Maven build lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html) — default lifecycle, package, verify.
- Day 149: [AWS rollback safety](https://aws.amazon.com/builders-library/ensuring-rollback-safety-during-deployments/) — testing rollback safety, version transitions.
- Day 150: [AWS rollback safety](https://aws.amazon.com/builders-library/ensuring-rollback-safety-during-deployments/) — testing rollback safety, version transitions.
- Day 151: [JUnit 6.1.3 User Guide](https://docs.junit.org/6.1.3/writing-tests/assertions.html) — Assertions.
- Day 152: [Java 21 Process API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Process.html) — waitFor, destroy, destroyForcibly, descendants, onExit.
- Day 153: [AWS rollback safety](https://aws.amazon.com/builders-library/ensuring-rollback-safety-during-deployments/) — testing rollback safety, version transitions.

## Interview practice and supplied capstone architecture

Days 141, 144, 148, and 151 reserve four spaced 45-minute system-design question sessions, two per week. Each replaces 45 minutes of the existing 85-minute engineering practice block and leaves a task-specific 40-minute implementation slice. Interview answers stay in the separate question bank; assistant notes supply capstone architecture for all 14 days. No extra daily time or capstone design proposal is assigned.

DSA assignments, supporting code, review schedules, and attempt history remain exclusively in the [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0).
