# CAP-94 — Compare two local service build artifacts

**Saved day:** 057 · 9 December 2026 · **Primary ID:** CAP-94 · **Maximum allocation:** 150 minutes

## Status and scope

Instructor seam, synthetic fixture, and independent local oracle are prepared. The learner comparison method remains an explicit TODO. The instructor tests compare synthetic byte strings only; they do not build the service, and no actual reproducible build result is claimed.

The comparison is read-only. It takes two artifact paths, build manifests, and test reports already produced by a person or a separate build process. It never launches Maven, alters an archive, normalizes timestamps, publishes, or deploys.

## Files

- Learner seam: `src/main/java/coverstory/application/CAP94Seam.java`
- Learner acceptance cases: `src/test/java/coverstory/application/CAP94AcceptanceTest.java`
- Independent synthetic oracle: `src/test/java/coverstory/application/CAP94InstructorInfrastructureTest.java`
- Fixture: `fixtures/coverstory/CAP-94/synthetic-v1.json`
- Evidence receipt: `docs/evidence/coverstory/CAP-94/harness-self-check.json`

Implement `CAP94Seam.compareBuilds(left, right)`. Hash each regular artifact file as the exact sequence of raw bytes using SHA-256. Do not unpack or normalize ZIP/JAR contents: timestamps are bytes and timestamp-only changes must fail. The comparison requires identical source commit, Java and Maven versions, dependency-manifest and build-configuration SHA-256, environment fingerprint, artifact identity, build command, test command, and all four test counts. A count total must equal passed + failed + skipped. Both manifests and reports must be complete; missing or unreadable evidence is `BLOCKED` / `INCOMPLETE`, never a success with invented fields. Different source commits are `INVALID_INPUT`; other provenance or test-count disagreements are invalid inputs. A byte mismatch or a recorded failed test is `FAIL`. `REPRODUCIBLE` is limited to matching bytes and complete matching passing evidence.

## Checks

Run the independent harness:

```sh
mvn -q -Dtest=CAP94InstructorInfrastructureTest test
```

Run the learner contract separately; until implemented, the four cases are expected to reach the explicit TODO:

```sh
mvn -q -Plearner-tests -Dtest=CAP94AcceptanceTest test
```

In this executor, Maven was invoked with Java 21 and a local compiler launcher. The exact observed commands and test outcomes are recorded in the evidence receipt. Use the ordinary Maven command above on a full JDK installation.

## Saved acceptance mapping

| Case | Input | Expected |
|---|---|---|
| CAP-94-01 | Same commit, environment, commands, passing test counts, and raw bytes | `REPRODUCIBLE`; return both SHA-256 digests |
| CAP-94-02 | Archive bytes differ only in a timestamp field | `FAIL`, naming the artifact; no normalization |
| CAP-94-03 | Second manifest names another source commit | `INVALID_INPUT`; no reproducibility claim |
| CAP-94-04 | Missing manifest, report, or artifact | `BLOCKED` / `INCOMPLETE` |
| CAP-94-05 | Toolchain, environment, config, or build/test command differs | `INVALID_INPUT` |
| CAP-94-06 | Test counts differ, are internally inconsistent, or include failures | Invalid input for inconsistent evidence; otherwise `FAIL` for recorded test failures |

## Evidence boundary

The synthetic fixture and oracle prove the local comparison rules and reject timestamp normalization. They do not demonstrate that two actual service builds have been run. An actual report must retain the observed source commit, toolchain/environment, configuration and dependency hashes, exact build/test commands, test report, artifact paths, and artifact hashes. Passing hash comparison says nothing about application correctness beyond the supplied test reports.

This bounded task remains within the saved 95-minute capstone envelope (docs 10m, build 55m, tests/evidence 30m); no measured timebox rehearsal is claimed here.
