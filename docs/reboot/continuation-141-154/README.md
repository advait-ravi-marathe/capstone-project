# Days 141–154 — two-artifact release evidence on unchanged schema

This scaffold plans a local, quiesced A→B→A compatibility rehearsal using distinct immutable JARs and the existing V001–V005 database. It adds only a read-only, versioned report path using existing recovery inspection/classification. It does not establish cloud/deployment readiness or learner completion.

Dates: 2027-03-09–2027-03-22 (Sprint 11); recovery days 147 and 154. Daily budget: DSA 60/75/90 minutes by substantive exercise count plus unchanged 120 engineering minutes (25 study, 85 practice/test, 10 evidence).

Course reference: **Master Spring Boot 3 & Spring Framework 6 with Java**, course 4993276, in28Minutes Official. [Course page](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners?utm_source=chatgpt&utm_medium=llm-integration&utm_audience=all&utm_campaign=chatgpt_integration_phase1). Catalog metadata was verified 2026-10-20; access and playback are unconfirmed, video content unaudited, and lesson deep links were not returned. Exact lesson IDs and durations are in `manifest.json`.

A is frozen before B-only behavior, with actual digest and source/overlay provenance. B must prove its additional capability. A runnable baseline JAR is not distributed by this scaffold: use the learner's actual prior checkpoint. If it is unavailable, report the matrix blocked at baseline setup; never copy B under A's name. An instructor conformance run may build A from pinned prior source plus a recorded temporary prerequisite witness, then build B separately with the same prerequisite witness and new report witnesses. Record source/overlay identities and recipes, delete solved temporary JARs/overlays, and restore source checksums.

Package B at repository root: `(cd frontend && npm ci && npm run build && npm run build:local)` then `./mvnw -Plocal-app -DskipTests package`. Matrix commands use `COVERSTORY_RELEASE_BASELINE_JAR` for immutable A and require its recorded source/build provenance; B is `target/capstone-starter-1.0-SNAPSHOT.jar`. Keep both artifacts byte-identical to their recorded hashes throughout.

The compatibility matrix uses one retained database and no reset. The new inspection context validates requests before constructing Spring, disables Flyway/SQL initialization and refuses absent schema. The v1 contract is [report-v1.md](report-v1.md). Its framed report is evidence, not authority: compare complete database snapshots independently.

No AWS resources, credentials, grants, spending, deployment, schema changes, public endpoint or later-day work is authorized. Temporary witness success is instructor evidence only.
